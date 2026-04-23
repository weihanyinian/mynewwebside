import { defineStore } from 'pinia'
import { fetchNeteaseStatus, fetchPublicPlaylist, fetchSongLyric, fetchSongUrl, type SongMeta } from '../api/musicApi'
import { DEFAULT_NETEASE_BR, DEFAULT_NETEASE_PLAYLIST_ID } from '../config/music'
import { getToken } from '../utils/token'

/** 与播放器、音乐中心共用的曲目结构 */
export type PlayerTrack = {
  id: number
  title: string
  artist: string
  cover: string
}

export type PlaySource = 'random' | 'playlist' | 'library'
export type PlayMode = 'sequence' | 'loop_one' | 'shuffle'

const PREF_KEY = 'mp_player_pref_v1'
const MAX_AUTO_SKIP = 5

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
    playMode: 'shuffle' as PlayMode,
    volume: 0.65,
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
        const d = await fetchNeteaseStatus()
        this.neteaseBound = d.bound
        this.neteaseNickname = d.neteaseNickname
      } catch {
        this.neteaseBound = false
        this.neteaseNickname = null
      }
    },

    async loadLyricForCurrent() {
      const t = this.currentTrack
      if (!t || !t.id) {
        this.lyricLrc = ''
        return
      }
      try {
        const useAuth = !!getToken() && this.neteaseBound
        const data = await fetchSongLyric(t.id, useAuth)
        const main = data.lrc || ''
        const sub = data.tlyric || ''
        this.lyricLrc = main || sub ? [main, sub].filter(Boolean).join('\n') : ''
      } catch {
        this.lyricLrc = ''
      }
    },

    async loadCurrentUrl() {
      await this.loadSong(this.currentIndex)
    },

    async loadSong(index: number) {
      const t = this.currentTrack
      if (!t || !t.id || index < 0 || this.queue.length === 0) {
        this.resolvedUrl = ''
        return
      }
      this.loadError = ''
      this.urlLoading = true
      const useAuth = !!getToken() && this.neteaseBound
      try {
        const dto = await fetchSongUrl(t.id, DEFAULT_NETEASE_BR, useAuth)
        if (dto.playable && dto.url) {
          this.resolvedUrl = dto.url
        } else {
          this.resolvedUrl = ''
          const message =
            dto.reasonMessage ||
            (dto.reasonCode === 'NO_COPYRIGHT' ? '无版权或需登录网易云后播放' : '暂时无法播放')
          this.loadError = message
          // 无版权或不可播放时自动跳过，避免播放器卡死在当前曲目
          if (dto.reasonCode === 'NO_COPYRIGHT' || dto.reasonCode === 'NO_DATA' || dto.reasonCode === 'UPSTREAM') {
            const skipped = await this.tryAutoSkip(index, MAX_AUTO_SKIP)
            if (skipped) return
          }
        }
      } catch (e: unknown) {
        this.resolvedUrl = ''
        this.loadError = e instanceof Error ? e.message : '播放失败'
      } finally {
        this.urlLoading = false
      }
      await this.loadLyricForCurrent()
    },

    async tryAutoSkip(startIndex: number, maxSkip: number) {
      if (this.queue.length <= 1) return false
      for (let i = 1; i <= maxSkip; i++) {
        const nextIdx = (startIndex + i) % this.queue.length
        this.currentIndex = nextIdx
        try {
          const useAuth = !!getToken() && this.neteaseBound
          const dto = await fetchSongUrl(this.queue[nextIdx].id, DEFAULT_NETEASE_BR, useAuth)
          if (dto.playable && dto.url) {
            this.resolvedUrl = dto.url
            this.loadError = '检测到当前歌曲不可播放，已自动切到下一首'
            await this.loadLyricForCurrent()
            return true
          }
        } catch {
          // continue
        }
      }
      return false
    },

    async playTracks(tracks: PlayerTrack[], startIndex: number) {
      if (tracks.length === 0) return
      this.queue = tracks.slice()
      this.currentIndex = Math.max(0, Math.min(startIndex, tracks.length - 1))
      this.source = 'library'
      await this.loadSong(this.currentIndex)
    },

    async playTrack(t: PlayerTrack) {
      await this.playTracks([t], 0)
    },

    async playNext(opts?: { shuffle?: boolean; loopOne?: boolean }) {
      if (opts?.loopOne) {
        await this.loadSong(this.currentIndex)
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
      await this.loadSong(this.currentIndex)
    },

    async playPrev(opts?: { shuffle?: boolean }) {
      if (this.source === 'random') return
      if (this.queue.length === 0) return
      if (opts?.shuffle) {
        this.currentIndex = Math.floor(Math.random() * this.queue.length)
      } else {
        this.currentIndex = (this.currentIndex - 1 + this.queue.length) % this.queue.length
      }
      await this.loadSong(this.currentIndex)
    },

    async playRandom() {
      if (this.playlistTracks.length === 0) {
        await this.loadDefaultPlaylist()
      }
      if (this.playlistTracks.length === 0) {
        this.loadError = this.loadError || '暂无可用曲目'
        return
      }
      const randomIndex = Math.floor(Math.random() * this.playlistTracks.length)
      this.source = 'random'
      this.queue = [...this.playlistTracks]
      this.currentIndex = randomIndex
      await this.loadSong(this.currentIndex)
    },

    async loadDefaultPlaylist() {
      try {
        const rows = await fetchPublicPlaylist(DEFAULT_NETEASE_PLAYLIST_ID, false)
        if (rows.length > 0) {
          this.playlistTracks = rows.map((r: SongMeta) => this.metaToTrack(r))
          this.queue = this.playlistTracks.slice()
          this.currentIndex = 0
          this.source = 'playlist'
          this.playlistLoaded = true
          this.loadError = ''
          await this.loadSong(this.currentIndex)
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
      await this.loadSong(this.currentIndex)
    },

    cyclePlayMode() {
      const order: PlayMode[] = ['sequence', 'loop_one', 'shuffle']
      this.playMode = order[(order.indexOf(this.playMode) + 1) % order.length]
      this.persistPlayerPref()
    },

    setVolume(v: number) {
      this.volume = Math.max(0, Math.min(1, v))
      this.persistPlayerPref()
    },

    persistPlayerPref() {
      try {
        localStorage.setItem(
          PREF_KEY,
          JSON.stringify({
            volume: this.volume,
            playMode: this.playMode,
          }),
        )
      } catch {
        // ignore
      }
    },

    hydratePlayerPref() {
      try {
        const raw = localStorage.getItem(PREF_KEY)
        if (!raw) return
        const obj = JSON.parse(raw) as { volume?: number; playMode?: PlayMode }
        if (typeof obj.volume === 'number') this.volume = Math.max(0, Math.min(1, obj.volume))
        if (obj.playMode && ['sequence', 'loop_one', 'shuffle'].includes(obj.playMode)) this.playMode = obj.playMode
      } catch {
        // ignore
      }
    },

    async initPlayer() {
      this.hydratePlayerPref()
      await this.refreshNeteaseStatus()
      if (!this.playlistLoaded || this.playlistTracks.length === 0) {
        await this.loadDefaultPlaylist()
      }
    },
  },
})
