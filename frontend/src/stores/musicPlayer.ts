import { defineStore } from 'pinia'
import {
  fetchHotTracks,
  fetchNeteaseStatus,
  fetchPublicPlaylist,
  fetchQqLyric,
  fetchQqSongUrl,
  fetchQqStatus,
  fetchSongLyric,
  fetchSongUrl,
  type SongMeta,
} from '../api/musicApi'
import { DEFAULT_NETEASE_BR, DEFAULT_NETEASE_PLAYLIST_ID } from '../config/music'
import { getToken } from '../utils/token'

export type MusicSource = 'netease' | 'qq'

/** 与播放器、音乐中心共用的曲目结构 */
export type PlayerTrack = {
  id: number
  title: string
  artist: string
  cover: string
  source: MusicSource
  /** QQ 曲目的 songmid，source 为 qq 时必填 */
  qqSongMid?: string
}

export type PlaySource = 'random' | 'playlist' | 'library'
export type PlayMode = 'sequence' | 'loop_one' | 'shuffle'

const PREF_KEY = 'mp_player_pref_v1'
const MAX_AUTO_SKIP = 5

export const useMusicPlayerStore = defineStore('musicPlayer', {
  state: () => ({
    neteaseBound: false,
    neteaseNickname: null as string | null,
    qqBound: false,
    qqNickname: null as string | null,
    queue: [] as PlayerTrack[],
    currentIndex: -1,
    resolvedUrl: '',
    urlLoading: false,
    loadError: '',
    lyricLrc: '',
    lyricTLrc: '',
    /** 迷你播放器「歌单 / 随机」模式下的队列来源 */
    source: 'playlist' as PlaySource,
    playlistLoaded: false,
    playlistTracks: [] as PlayerTrack[],
    /** 当前列表来自日更热歌时标记平台（用于 UI）；为 null 表示普通歌单或回退歌单 */
    dailyHotPlatform: null as 'netease' | 'qq' | null,
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
        source: 'netease',
      }
    },

    metaToQqTrack(m: { songmid: string; name: string; artist: string; cover: string }): PlayerTrack {
      return {
        id: 0,
        title: m.name,
        artist: m.artist,
        cover: m.cover,
        source: 'qq',
        qqSongMid: m.songmid,
      }
    },

    /** 后端 SongMeta：含 songmid 时按 QQ 曲目解析 */
    rowToPlayerTrack(row: SongMeta): PlayerTrack {
      if (row.songmid) {
        return this.metaToQqTrack({
          songmid: row.songmid,
          name: row.name,
          artist: row.artist,
          cover: row.cover,
        })
      }
      return this.metaToTrack(row)
    },

    async refreshNeteaseStatus() {
      if (!getToken()) {
        this.neteaseBound = false
        this.neteaseNickname = null
        this.qqBound = false
        this.qqNickname = null
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
      try {
        const q = await fetchQqStatus()
        this.qqBound = q.bound
        this.qqNickname = q.qqNickname
      } catch {
        this.qqBound = false
        this.qqNickname = null
      }
    },

    async loadLyricForCurrent() {
      const t = this.currentTrack
      if (!t) {
        this.lyricLrc = ''
        this.lyricTLrc = ''
        return
      }
      try {
        if (t.source === 'qq' && t.qqSongMid) {
          const data = await fetchQqLyric(t.qqSongMid)
          this.lyricLrc = data.lrc || ''
          this.lyricTLrc = data.tlyric || ''
          return
        }
        if (!t.id) {
          this.lyricLrc = ''
          this.lyricTLrc = ''
          return
        }
        const useAuth = !!getToken() && this.neteaseBound
        const data = await fetchSongLyric(t.id, useAuth)
        this.lyricLrc = data.lrc || ''
        this.lyricTLrc = data.tlyric || ''
      } catch {
        this.lyricLrc = ''
        this.lyricTLrc = ''
      }
    },

    async loadCurrentUrl() {
      await this.loadSong(this.currentIndex)
    },

    async loadSong(index: number) {
      const t = this.currentTrack
      if (!t || index < 0 || this.queue.length === 0) {
        this.resolvedUrl = ''
        return
      }
      this.loadError = ''
      this.urlLoading = true
      try {
        if (t.source === 'qq') {
          if (!t.qqSongMid) {
            this.resolvedUrl = ''
            this.loadError = '缺少 QQ songmid'
            return
          }
          if (!getToken() || !this.qqBound) {
            this.resolvedUrl = ''
            this.loadError = '播放 QQ 音乐请先在本站绑定 QQ Cookie（音乐中心或播放器内）'
            return
          }
          const dto = await fetchQqSongUrl(t.qqSongMid, '128')
          if (dto.playable && dto.url) {
            this.resolvedUrl = dto.url
          } else {
            this.resolvedUrl = ''
            this.loadError = dto.reasonMessage || 'QQ 音乐暂时无法播放'
            const skipped = await this.tryAutoSkip(index, MAX_AUTO_SKIP)
            if (skipped) return
          }
        } else {
          if (!t.id) {
            this.resolvedUrl = ''
            return
          }
          const useAuth = !!getToken() && this.neteaseBound
          const targetBr = useAuth ? DEFAULT_NETEASE_BR : 128000
          const dto = await fetchSongUrl(t.id, targetBr, useAuth)
          if (dto.playable && dto.url) {
            this.resolvedUrl = dto.url
          } else {
            this.resolvedUrl = ''
            const message =
              dto.reasonMessage ||
              (dto.reasonCode === 'NO_COPYRIGHT' ? '无版权或需登录网易云后播放' : '暂时无法播放')
            this.loadError = message
            if (dto.reasonCode === 'NO_COPYRIGHT' || dto.reasonCode === 'NO_DATA' || dto.reasonCode === 'UPSTREAM') {
              const skipped = await this.tryAutoSkip(index, MAX_AUTO_SKIP)
              if (skipped) return
            }
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
        const nt = this.queue[nextIdx]
        try {
          if (nt.source === 'qq' && nt.qqSongMid && getToken() && this.qqBound) {
            const dto = await fetchQqSongUrl(nt.qqSongMid, '128')
            if (dto.playable && dto.url) {
              this.resolvedUrl = dto.url
              this.loadError = '检测到当前歌曲不可播放，已自动切到下一首'
              await this.loadLyricForCurrent()
              return true
            }
          } else if (nt.id) {
            const useAuth = !!getToken() && this.neteaseBound
            const targetBr = useAuth ? DEFAULT_NETEASE_BR : 128000
            const dto = await fetchSongUrl(nt.id, targetBr, useAuth)
            if (dto.playable && dto.url) {
              this.resolvedUrl = dto.url
              this.loadError = '检测到当前歌曲不可播放，已自动切到下一首'
              await this.loadLyricForCurrent()
              return true
            }
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
      this.dailyHotPlatform = null
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

    /**
     * 默认曲库：按本地日历日「奇数日→网易云热歌、偶数日→QQ 热歌」，失败则互换再试，最后回退配置的网易云歌单。
     */
    async loadDefaultPlaylist() {
      const day = new Date().getDate()
      const preferNeteaseFirst = day % 2 === 1

      const loadHot = async (source: 'netease' | 'qq') => {
        try {
          return await fetchHotTracks(source, 80)
        } catch {
          return []
        }
      }

      const tryApplyHot = async (rows: SongMeta[], platform: 'netease' | 'qq') => {
        if (rows.length === 0) return false
        this.playlistTracks = rows.map((r) => this.rowToPlayerTrack(r))
        this.queue = this.playlistTracks.slice()
        this.currentIndex = 0
        this.source = 'playlist'
        this.playlistLoaded = true
        this.dailyHotPlatform = platform
        this.loadError = ''
        await this.loadSong(this.currentIndex)
        return true
      }

      try {
        if (preferNeteaseFirst) {
          let rows = await loadHot('netease')
          if (await tryApplyHot(rows, 'netease')) return
          rows = await loadHot('qq')
          if (await tryApplyHot(rows, 'qq')) return
        } else {
          let rows = await loadHot('qq')
          if (await tryApplyHot(rows, 'qq')) return
          rows = await loadHot('netease')
          if (await tryApplyHot(rows, 'netease')) return
        }

        const rows = await fetchPublicPlaylist(DEFAULT_NETEASE_PLAYLIST_ID, false)
        if (rows.length > 0) {
          this.playlistTracks = rows.map((r) => this.rowToPlayerTrack(r))
          this.queue = this.playlistTracks.slice()
          this.currentIndex = 0
          this.source = 'playlist'
          this.playlistLoaded = true
          this.dailyHotPlatform = null
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
      // dailyHotPlatform 不变，仍为热歌来源提示
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
      if (getToken()) {
        await this.refreshNeteaseStatus()
      }
      if (!this.playlistLoaded || this.playlistTracks.length === 0) {
        await this.loadDefaultPlaylist()
      }
    },
  },
})
