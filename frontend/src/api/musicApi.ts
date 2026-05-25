import { http, type ApiResponse } from './http'

export type NeteaseStatus = {
  bound: boolean
  neteaseUid: number | null
  neteaseNickname: string | null
}

export type SongMeta = {
  id: number
  name: string
  artist: string
  cover: string
}

/** 与后端 MusicSearchHitDto 一致：网易云搜索 */
export type MusicSearchHit = {
  kind: 'song' | 'artist' | 'album' | 'playlist'
  id: number
  mid: string
  title: string
  subtitle: string
  cover: string
}

export type PlaylistItem = {
  id: number
  name: string
  coverUrl: string
  trackCount: number
}

export type SongUrlDto = {
  url: string | null
  playable: boolean
  reasonCode: string
  reasonMessage: string | null
}

export type LyricDto = {
  lrc: string
  tlyric: string
}

export async function fetchNeteaseStatus() {
  const { data } = await http.get<ApiResponse<NeteaseStatus>>('/api/music/status')
  return data.data
}

/** countrycode 默认后端置为 86，与非中国区号需求见 @neteasecloudmusicapienhanced/api */
export async function neteaseLogin(phone: string, password: string, countrycode?: string) {
  const { data } = await http.post<ApiResponse<NeteaseStatus>>('/api/music/login', {
    phone,
    password,
    countrycode,
  })
  return data.data
}

/** 扫码登录成功后，将上游返回的 Cookie 绑定到当前本站账号 */
export async function neteaseLoginWithCookie(cookie: string) {
  const { data } = await http.post<ApiResponse<NeteaseStatus>>('/api/music/login/cookie', { cookie })
  return data.data
}

export async function neteaseLogout() {
  await http.post<ApiResponse<void>>('/api/music/logout')
}

export async function fetchUserPlaylists(offset = 0, limit = 30) {
  const { data } = await http.get<ApiResponse<PlaylistItem[]>>('/api/music/playlists', {
    params: { offset, limit },
  })
  return data.data
}

export async function fetchLikelist() {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/music/likelist')
  return data.data
}

export async function fetchRecent(limit = 50) {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/music/recent', { params: { limit } })
  return data.data
}

export async function fetchPlaylistTracks(playlistId: number) {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/music/playlist/tracks', {
    params: { id: playlistId },
  })
  return data.data
}

export async function fetchSongUrl(id: number, br?: number, useAuth = false) {
  const path = useAuth ? '/api/music/song/url' : '/api/public/music/song/url'
  const { data } = await http.get<ApiResponse<SongUrlDto>>(path, { params: { id, br } })
  return data.data
}

export async function fetchSongLyric(id: number, useAuth = false) {
  const path = useAuth ? '/api/music/lyric' : '/api/public/music/lyric'
  const { data } = await http.get<ApiResponse<LyricDto>>(path, { params: { id } })
  return data.data
}

export async function fetchPublicPlaylist(id: string, shuffle = false) {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/public/music/playlist', {
    params: { id, shuffle },
  })
  return data.data
}

/** 近期热歌（后端：网易云热歌榜歌单） */
export async function fetchHotTracks(source: 'netease', limit = 80) {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/public/music/hot', {
    params: { source, limit },
  })
  return data.data
}

/** 网易云搜索（cloudsearch）；type: song | artist | album | playlist */
export async function searchNetease(q: string, limit = 30, type = 'song') {
  const { data } = await http.get<ApiResponse<MusicSearchHit[]>>('/api/music/search', {
    params: { q, limit, type },
  })
  return data.data
}
