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

export type PlayerTrack = {
  id: number
  title: string
  artist: string
  cover: string
  source: MusicSource
  qqSongMid?: string
}

export type PlaySource = 'random' | 'playlist' | 'library'
export type PlayMode = 'sequence' | 'loop_one' | 'shuffle'

const PREF_KEY = 'mp_player_pref_v1'
const MAX_AUTO_SKIP = 5
const PERSIST_DEBOUNCE_MS = 300

let persistTimer: ReturnType<typeof setTimeout> | null = null

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
    source: 'playlist' as PlaySource,
    playlistTracks: [] as PlayerTrack[],
    dailyHotPlatform: null as 'netease' | 'qq' | null,
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
      return { id: m.id, title: m.name, artist: m.artist, cover: m.cover, source: 'netease' }
    },

    metaToQqTrack(m: { songmid: string; name: string; artist: string; cover: string }): PlayerTrack {
      return { id: 0, title: m.name, artist: m.artist, cover: m.cover, source: 'qq', qqSongMid: m.songmid }
    },

    rowToPlayerTrack(row: SongMeta): PlayerTrack {
      if (row.songmid) {
        return this.metaToQqTrack({ songmid: row.songmid, name: row.name, artist: row.artist, cover: row.cover })
      }
      return this.metaToTrack(row)
    },

    // ---- shared URL resolution (eliminates 4x duplication between loadSong / tryAutoSkip) ----

    async _resolveTrackUrl(t: PlayerTrack): Promise<{ url: string; error?: string; canSkip?: boolean }> {
      if (t.source === 'qq') {
        if (!t.qqSongMid) return { url: '', error: '缺少 QQ songmid' }
        if (!getToken() || !this.qqBound) {
          return { url: '', error: '播放 QQ 音乐请先在本站绑定 QQ Cookie（音乐中心或播放器内）' }
        }
        const dto = await fetchQqSongUrl(t.qqSongMid, '128')
        if (dto.playable && dto.url) return { url: dto.url }
        return { url: '', error: dto.reasonMessage || 'QQ 音乐暂时无法播放', canSkip: true }
      }
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
        if (t.source === 'qq' && t.qqSongMid) {
          const data = await fetchQqLyric(t.qqSongMid)
          this.lyricLrc = data.lrc || ''
          this.lyricTLrc = data.tlyric || ''
          return
        }
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

    _applyPlaylistRows(rows: SongMeta[], platform: 'netease' | 'qq' | null) {
      this.playlistTracks = rows.map((r) => this.rowToPlayerTrack(r))
      this.queue = this.playlistTracks.slice()
      this.currentIndex = 0
      this.source = 'playlist'
      this.dailyHotPlatform = platform
      this.loadError = ''
    },

    async loadDefaultPlaylist() {
      const day = new Date().getDate()
      const preferNeteaseFirst = day % 2 === 1

      const loadHot = async (source: 'netease' | 'qq') => {
        try { return await fetchHotTracks(source, 80) } catch { return [] as SongMeta[] }
      }

      const tryApplyHot = async (rows: SongMeta[], platform: 'netease' | 'qq') => {
        if (rows.length === 0) return false
        this._applyPlaylistRows(rows, platform)
        await this.loadSong(this.currentIndex)
        return true
      }

      try {
        const [firstRows, secondRows] = preferNeteaseFirst
          ? await Promise.all([loadHot('netease'), loadHot('qq')])
          : await Promise.all([loadHot('qq'), loadHot('netease')])

        const firstPlatform = preferNeteaseFirst ? 'netease' as const : 'qq' as const
        const secondPlatform = preferNeteaseFirst ? 'qq' as const : 'netease' as const

        if (await tryApplyHot(firstRows, firstPlatform)) return
        if (await tryApplyHot(secondRows, secondPlatform)) return

        const rows = await fetchPublicPlaylist(DEFAULT_NETEASE_PLAYLIST_ID, false)
        if (rows.length > 0) {
          this._applyPlaylistRows(rows, null)
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
        this.qqBound = false
        this.qqNickname = null
        return
      }
      const [neteaseResult, qqResult] = await Promise.allSettled([
        fetchNeteaseStatus(),
        fetchQqStatus(),
      ])
      if (neteaseResult.status === 'fulfilled') {
        this.neteaseBound = neteaseResult.value.bound
        this.neteaseNickname = neteaseResult.value.neteaseNickname
      } else {
        this.neteaseBound = false
        this.neteaseNickname = null
      }
      if (qqResult.status === 'fulfilled') {
        this.qqBound = qqResult.value.bound
        this.qqNickname = qqResult.value.qqNickname
      } else {
        this.qqBound = false
        this.qqNickname = null
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
