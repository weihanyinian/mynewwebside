/**
 * 全站默认网易云歌单（与后端 netease.proxy.default-playlist-id 保持一致）
 * @see https://music.163.com/playlist?id=489057279
 */
export const DEFAULT_NETEASE_PLAYLIST_ID =
  (import.meta.env.VITE_DEFAULT_NETEASE_PLAYLIST_ID as string | undefined)?.trim() || '489057279'

/** 默认音质码率，需与后端 netease.proxy.default-br 协同。 */
export const DEFAULT_NETEASE_BR = Number(import.meta.env.VITE_DEFAULT_NETEASE_BR || 320000)
