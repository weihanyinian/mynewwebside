/**
 * 首页全屏背景 MP4：请将文件放在 `frontend/public/videos/`，文件名与下列常量完全一致（含空格、书名号、扩展名）。
 * 路径故意不用 encodeURIComponent，避免部分环境下 Vite/静态服务对编码路径解析不一致导致 404。
 */
export const HOME_BG_LIGHT_FILENAME = '初音.mp4'
export const HOME_BG_DARK_FILENAME =
  'livetune feat 初音ミク「Redial」Music Video_final_ver.mp4'

export function publicVideoPath(filename: string): string {
  return `/videos/${filename}`
}

export const HOME_BG_LIGHT_SRC = publicVideoPath(HOME_BG_LIGHT_FILENAME)
export const HOME_BG_DARK_SRC = publicVideoPath(HOME_BG_DARK_FILENAME)
