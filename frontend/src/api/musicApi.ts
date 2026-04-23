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

export async function neteaseLogin(phone: string, password: string) {
  const { data } = await http.post<ApiResponse<NeteaseStatus>>('/api/music/login', { phone, password })
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
