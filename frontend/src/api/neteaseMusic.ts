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

export async function fetchNeteaseStatus() {
  const { data } = await http.get<ApiResponse<NeteaseStatus>>('/api/music/netease/status')
  return data.data
}

export async function neteaseLogin(phone: string, password: string) {
  const { data } = await http.post<ApiResponse<NeteaseStatus>>('/api/music/netease/login', { phone, password })
  return data.data
}

export async function neteaseLogout() {
  await http.post<ApiResponse<void>>('/api/music/netease/logout')
}

export async function fetchUserPlaylists(offset = 0, limit = 30) {
  const { data } = await http.get<ApiResponse<PlaylistItem[]>>('/api/music/netease/playlists', {
    params: { offset, limit },
  })
  return data.data
}

export async function fetchLikelist() {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/music/netease/likelist')
  return data.data
}

export async function fetchRecent(limit = 50) {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/music/netease/recent', { params: { limit } })
  return data.data
}

export async function fetchPlaylistTracks(playlistId: number) {
  const { data } = await http.get<ApiResponse<SongMeta[]>>('/api/music/netease/playlist/tracks', {
    params: { id: playlistId },
  })
  return data.data
}
