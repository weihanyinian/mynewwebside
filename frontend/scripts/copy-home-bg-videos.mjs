/**
 * 将已有 light.mp4 / dark.mp4 复制为 config 约定的文件名（UTF-8）。
 * 用法：在 frontend 目录执行 node scripts/copy-home-bg-videos.mjs
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const videos = path.join(__dirname, '../public/videos')
const light = path.join(videos, 'light.mp4')
const dark = path.join(videos, 'dark.mp4')
const dstDay = path.join(videos, '初音.mp4')
const dstNight = path.join(
  videos,
  'livetune feat 初音ミク「Redial」Music Video_final_ver.mp4',
)

function main() {
  if (!fs.existsSync(light) || !fs.existsSync(dark)) {
    console.error('缺少 public/videos/light.mp4 或 dark.mp4，请先放入素材。')
    process.exit(1)
  }
  fs.copyFileSync(light, dstDay)
  fs.copyFileSync(dark, dstNight)
  console.log('已写入:', dstDay, fs.statSync(dstDay).size, 'bytes')
  console.log('已写入:', dstNight, fs.statSync(dstNight).size, 'bytes')
}

main()
