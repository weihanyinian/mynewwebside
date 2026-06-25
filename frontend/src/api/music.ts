import api from './index'

export interface Song {
  id: number
  name: string
  artists: { id: number; name: string }[]
  album: { id: number; name: string; picUrl: string }
  duration: number
}

export interface Playlist {
  id: number
  name: string
  coverImgUrl: string
  description: string
  trackCount: number
  playCount: number
}

export interface Lyric {
  time: number
  text: string
}

export const musicApi = {
  // Search songs
  search(keywords: string, limit = 20) {
    return api.get('/music/search', { params: { keywords, limit } })
  },

  // Get song detail
  getSongDetail(ids: number | number[]) {
    const idsStr = Array.isArray(ids) ? ids.join(',') : String(ids)
    return api.get('/music/song/detail', { params: { ids: idsStr } })
  },

  // Get song URL (playable)
  getSongUrl(id: number) {
    return api.get('/music/song/url', { params: { id } })
  },

  // Get playlist detail
  getPlaylistDetail(id: number) {
    return api.get('/music/playlist/detail', { params: { id } })
  },

  // Get lyrics
  getLyric(id: number) {
    return api.get('/music/lyric', { params: { id } })
  },

  // Get hot playlists
  getTopPlaylists(limit = 10) {
    return api.get('/music/top/playlist', { params: { limit } })
  },

  // Get daily recommended songs
  getRecommendSongs() {
    return api.get('/music/recommend/songs')
  }
}
