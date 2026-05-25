import { defineStore } from 'pinia'
import {
  fetchHotTracks,
  fetchNeteaseStatus,
  fetchPublicPlaylist,
  fetchSongLyric,
  fetchSongUrl,
  type SongMeta,
} from '../api/musicApi'
import { DEFAULT_NETEASE_BR, DEFAULT_NETEASE_PLAYLIST_ID } from '../config/music'
import { getToken } from '../utils/token'

export type PlaySource = 'random' | 'playlist' | 'library'
export type PlayMode = 'sequence' | 'loop_one' | 'shuffle'

export type PlayerTrack = {
  id: number
  title: string
  artist: string
  cover: string
}

const PREF_KEY = 'mp_player_pref_v1'
const MAX_AUTO_SKIP = 5
const PERSIST_DEBOUNCE_MS = 300

let persistTimer: ReturnType<typeof setTimeout> | null = null

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
    lyricTLrc: '',
    source: 'playlist' as PlaySource,
    playlistTracks: [] as PlayerTrack[],
    /** 当前「歌单」来源：网易云默认歌单 / 热歌榜 */
    dailyHotPlatform: null as 'netease' | 'netease_playlist' | null,
    playMode: 'shuffle' as PlayMode,
    volume: 0.65,
  }),
  getters: {
    currentTrack(state): PlayerTrack | null {
      if (state.currentIndex < 0 || state.currentIndex >= state.queue.length) return null
      return state.queue[state.currentIndex]
    },
    playlistLoaded(state): boolean {
      return state.playlistTracks.length > 0
    },
  },
  actions: {
    // ---- track construction ----

    metaToTrack(m: { id: number; name: string; artist: string; cover: string }): PlayerTrack {
      return { id: m.id, title: m.name, artist: m.artist, cover: m.cover }
    },

    rowToPlayerTrack(row: SongMeta): PlayerTrack {
      return this.metaToTrack(row)
    },

    // ---- shared URL resolution (eliminates 4x duplication between loadSong / tryAutoSkip) ----

    async _resolveTrackUrl(t: PlayerTrack): Promise<{ url: string; error?: string; canSkip?: boolean }> {
      if (!t.id) return { url: '' }
      const useAuth = !!getToken() && this.neteaseBound
      const targetBr = useAuth ? DEFAULT_NETEASE_BR : 128000
      const dto = await fetchSongUrl(t.id, targetBr, useAuth)
      if (dto.playable && dto.url) return { url: dto.url }
      const message =
        dto.reasonMessage ||
        (dto.reasonCode === 'NO_COPYRIGHT' ? '无版权或需登录网易云后播放' : '暂时无法播放')
      const canSkip = dto.reasonCode === 'NO_COPYRIGHT' || dto.reasonCode === 'NO_DATA' || dto.reasonCode === 'UPSTREAM'
      return { url: '', error: message, canSkip }
    },

    // ---- lyric ----

    async loadLyricForCurrent() {
      const t = this.currentTrack
      if (!t) { this.lyricLrc = ''; this.lyricTLrc = ''; return }
      try {
        if (!t.id) { this.lyricLrc = ''; this.lyricTLrc = ''; return }
        const useAuth = !!getToken() && this.neteaseBound
        const data = await fetchSongLyric(t.id, useAuth)
        this.lyricLrc = data.lrc || ''
        this.lyricTLrc = data.tlyric || ''
      } catch {
        this.lyricLrc = ''
        this.lyricTLrc = ''
      }
    },

    // ---- song loading & auto-skip ----

    /** 按当前队列下标重新拉取播放地址与歌词（用于播放键重试、绑定账号后刷新等） */
    async loadCurrentUrl() {
      if (this.queue.length === 0) {
        await this.loadDefaultPlaylist()
      }
      if (this.queue.length === 0) return
      if (this.currentIndex < 0 || this.currentIndex >= this.queue.length) {
        this.currentIndex = 0
      }
      await this.loadSong(this.currentIndex)
    },

    async loadSong(index: number) {
      const t = this.queue[index]
      if (!t) { this.resolvedUrl = ''; return }
      this.loadError = ''
      this.urlLoading = true
      try {
        const result = await this._resolveTrackUrl(t)
        if (result.url) {
          this.resolvedUrl = result.url
        } else {
          this.resolvedUrl = ''
          this.loadError = result.error || '暂时无法播放'
          if (result.canSkip) {
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
        const nt = this.queue[nextIdx]
        if (!nt) continue
        try {
          const result = await this._resolveTrackUrl(nt)
          if (result.url) {
            this.currentIndex = nextIdx
            this.resolvedUrl = result.url
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

    // ---- queue management ----

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
      if (opts?.loopOne) { await this.loadSong(this.currentIndex); return }
      if (this.source === 'random') { await this.playRandom(); return }
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

    // ---- playlist loading ----

    _applyPlaylistRows(rows: SongMeta[], platform: 'netease' | 'netease_playlist' | null) {
      this.playlistTracks = rows.map((r) => this.rowToPlayerTrack(r))
      this.queue = this.playlistTracks.slice()
      this.currentIndex = 0
      this.source = 'playlist'
      this.dailyHotPlatform = platform
      this.loadError = ''
    },

    async loadDefaultPlaylist() {
      const loadHot = async () => {
        try {
          return await fetchHotTracks('netease', 80)
        } catch {
          return [] as SongMeta[]
        }
      }

      const tryApplyHot = async (rows: SongMeta[], platform: 'netease' | 'netease_playlist') => {
        if (rows.length === 0) return false
        this._applyPlaylistRows(rows, platform)
        await this.loadSong(this.currentIndex)
        return true
      }

      try {
        // 优先：配置的网易云公开歌单（与 VITE_DEFAULT_NETEASE_PLAYLIST_ID / 后端 default-playlist-id 一致）
        let playlistRows: SongMeta[] = []
        try {
          playlistRows = await fetchPublicPlaylist(DEFAULT_NETEASE_PLAYLIST_ID, false)
        } catch {
          /* 歌单拉取失败则回退热歌，不阻断 */
        }
        if (playlistRows.length > 0) {
          this._applyPlaylistRows(playlistRows, 'netease_playlist')
          await this.loadSong(this.currentIndex)
          return
        }

        if (await tryApplyHot(await loadHot(), 'netease')) return

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

    // ---- status refresh ----

    async refreshNeteaseStatus() {
      if (!getToken()) {
        this.neteaseBound = false
        this.neteaseNickname = null
        return
      }
      try {
        const result = await fetchNeteaseStatus()
        this.neteaseBound = result.bound
        this.neteaseNickname = result.neteaseNickname
      } catch {
        this.neteaseBound = false
        this.neteaseNickname = null
      }
    },

    // ---- playback mode & volume ----

    cyclePlayMode() {
      const order: PlayMode[] = ['sequence', 'loop_one', 'shuffle']
      this.playMode = order[(order.indexOf(this.playMode) + 1) % order.length]
      this._schedulePersist()
    },

    setVolume(v: number) {
      this.volume = Math.max(0, Math.min(1, v))
      this._schedulePersist()
    },

    _schedulePersist() {
      if (persistTimer) clearTimeout(persistTimer)
      persistTimer = setTimeout(() => this.persistPlayerPref(), PERSIST_DEBOUNCE_MS)
    },

    persistPlayerPref() {
      try {
        localStorage.setItem(
          PREF_KEY,
          JSON.stringify({ volume: this.volume, playMode: this.playMode }),
        )
      } catch { /* ignore */ }
    },

    hydratePlayerPref() {
      try {
        const raw = localStorage.getItem(PREF_KEY)
        if (!raw) return
        const obj = JSON.parse(raw) as { volume?: number; playMode?: PlayMode }
        if (typeof obj.volume === 'number') this.volume = Math.max(0, Math.min(1, obj.volume))
        if (obj.playMode && ['sequence', 'loop_one', 'shuffle'].includes(obj.playMode)) this.playMode = obj.playMode
      } catch { /* ignore */ }
    },

    // ---- init ----

    async initPlayer() {
      this.hydratePlayerPref()
      if (getToken()) {
        await this.refreshNeteaseStatus()
      }
      if (!this.playlistLoaded) {
        await this.loadDefaultPlaylist()
      }
    },
  },
})
