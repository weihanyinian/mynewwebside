import { defineStore } from 'pinia'
import { http, type ApiResponse } from '../api/http'
import { DEFAULT_NETEASE_PLAYLIST_ID } from '../config/music'
import { getToken } from '../utils/token'

/** 与播放器、音乐中心共用的曲目结构 */
export type PlayerTrack = {
  id: number
  title: string
  artist: string
  cover: string
}

export type PlaySource = 'random' | 'playlist' | 'library'

export const useMusicPlayerStore = defineStore('musicPlayer', {
  state: () => ({
    neteaseBound: false,
    neteaseNickname: null as string | null,
    queue: [] as PlayerTrack[],
    currentIndex: -1,
    resolvedUrl: '',
    urlLoading: false,
    loadError: '',
    lyricLrc: '',
    /** 默认使用本站配置的网易云歌单（见 config/music.ts），避免依赖第三方随机接口 */
    source: 'playlist' as PlaySource,
    playlistLoaded: false,
    playlistTracks: [] as PlayerTrack[],
  }),
  getters: {
    currentTrack(state): PlayerTrack | null {
      if (state.currentIndex < 0 || state.currentIndex >= state.queue.length) return null
      return state.queue[state.currentIndex]
    },
  },
  actions: {
    metaToTrack(m: { id: number; name: string; artist: string; cover: string }): PlayerTrack {
      return {
        id: m.id,
        title: m.name,
        artist: m.artist,
        cover: m.cover,
      }
    },

    async refreshNeteaseStatus() {
      if (!getToken()) {
        this.neteaseBound = false
        this.neteaseNickname = null
        return
      }
      try {
        const { data } = await http.get<
          ApiResponse<{ bound: boolean; neteaseUid: number | null; neteaseNickname: string | null }>
        >('/api/music/netease/status')
        const d = data.data
        this.neteaseBound = d.bound
        this.neteaseNickname = d.neteaseNickname
      } catch {
        this.neteaseBound = false
        this.neteaseNickname = null
      }
    },

    async fetchSongUrlDto(id: number) {
      const useAuth = !!getToken() && this.neteaseBound
      const path = useAuth ? '/api/music/netease/song/url' : '/api/public/music/song/url'
      const { data } = await http.get<
        ApiResponse<{ url: string | null; playable: boolean; reasonCode: string; reasonMessage: string | null }>
      >(path, { params: { id } })
      return data.data
    },

    async loadLyricForCurrent() {
      const t = this.currentTrack
      if (!t || !t.id) {
        this.lyricLrc = ''
        return
      }
      try {
        const { data } = await http.get<ApiResponse<{ lrc: string; tlyric: string }>>('/api/public/music/lyric', {
          params: { id: t.id },
        })
        this.lyricLrc = data.data.lrc || ''
      } catch {
        this.lyricLrc = ''
      }
    },

    async loadCurrentUrl() {
      const t = this.currentTrack
      if (!t || !t.id) {
        this.resolvedUrl = ''
        return
      }
      this.loadError = ''
      this.urlLoading = true
      try {
        const dto = await this.fetchSongUrlDto(t.id)
        if (dto.playable && dto.url) {
          this.resolvedUrl = dto.url
        } else {
          this.resolvedUrl = ''
          this.loadError =
            dto.reasonMessage ||
            (dto.reasonCode === 'NO_COPYRIGHT' ? '无版权或需登录网易云后播放' : '暂时无法播放')
        }
      } catch (e: unknown) {
        this.resolvedUrl = ''
        this.loadError = e instanceof Error ? e.message : '播放失败'
      } finally {
        this.urlLoading = false
      }
      await this.loadLyricForCurrent()
    },

    async playTracks(tracks: PlayerTrack[], startIndex: number) {
      if (tracks.length === 0) return
      this.queue = tracks.slice()
      this.currentIndex = Math.max(0, Math.min(startIndex, tracks.length - 1))
      this.source = 'library'
      await this.loadCurrentUrl()
    },

    async playTrack(t: PlayerTrack) {
      await this.playTracks([t], 0)
    },

    async playNext(opts?: { shuffle?: boolean; loopOne?: boolean }) {
      if (opts?.loopOne) {
        await this.loadCurrentUrl()
        return
      }
      if (this.source === 'random') {
        await this.playRandom()
        return
      }
      if (this.queue.length === 0) return
      if (opts?.shuffle) {
        this.currentIndex = Math.floor(Math.random() * this.queue.length)
      } else {
        this.currentIndex = (this.currentIndex + 1) % this.queue.length
      }
      await this.loadCurrentUrl()
    },

    async playPrev(opts?: { shuffle?: boolean }) {
      if (this.source === 'random') return
      if (this.queue.length === 0) return
      if (opts?.shuffle) {
        this.currentIndex = Math.floor(Math.random() * this.queue.length)
      } else {
        this.currentIndex = (this.currentIndex - 1 + this.queue.length) % this.queue.length
      }
      await this.loadCurrentUrl()
    },

    async fetchRandomTrack(): Promise<PlayerTrack | null> {
      const RANDOM_API = 'https://api.52vmy.cn/api/music/wy/rand'
      try {
        const res = await fetch(RANDOM_API, { cache: 'no-store' })
        if (res.ok) {
          const json = await res.json()
          if (json.code === 200 && json.data?.Music) {
            return {
              id: json.data.id || 0,
              title: json.data.song || '未知歌曲',
              artist: json.data.singer || '',
              cover: json.data.cover || '',
            }
          }
        }
      } catch {
        /* ignore */
      }
      return null
    },

    async playRandom() {
      const t = await this.fetchRandomTrack()
      if (!t) {
        await this.loadDefaultPlaylist()
        if (!this.currentTrack) {
          this.loadError = this.loadError || '暂无可用曲目'
        }
        return
      }
      this.source = 'random'
      this.queue = [t]
      this.currentIndex = 0
      await this.loadCurrentUrl()
    },

    async loadDefaultPlaylist() {
      try {
        const { data } = await http.get<
          ApiResponse<Array<{ id: number; name: string; artist: string; cover: string }>>
        >('/api/public/music/playlist', { params: { id: DEFAULT_NETEASE_PLAYLIST_ID, shuffle: false } })
        const rows = data.data
        if (rows.length > 0) {
          this.playlistTracks = rows.map((r) => ({
            id: r.id,
            title: r.name,
            artist: r.artist,
            cover: r.cover,
          }))
          this.queue = this.playlistTracks.slice()
          this.currentIndex = 0
          this.source = 'playlist'
          this.playlistLoaded = true
          this.loadError = ''
          await this.loadCurrentUrl()
          return
        }
        this.loadError = '歌单暂无曲目'
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : String(e)
        this.loadError =
          msg.includes('Network Error') || msg.includes('ERR_CONNECTION_REFUSED')
            ? '无法连接后端：请先在本机启动 Spring Boot（端口 8080），并执行 npm run dev 使用 Vite 代理 /api'
            : msg || '歌单加载失败'
      }
      this.source = 'random'
    },

    /** 将当前队列随机重排并从第一首开始 */
    async shufflePlaylistQueue() {
      if (this.playlistTracks.length === 0) return
      const arr = [...this.playlistTracks]
      for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1))
        ;[arr[i], arr[j]] = [arr[j], arr[i]]
      }
      this.queue = arr
      this.currentIndex = 0
      this.source = 'playlist'
      await this.loadCurrentUrl()
    },
  },
})
