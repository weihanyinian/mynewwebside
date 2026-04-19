/** 解析网易云 LRC（[mm:ss.xx] 文本）为带时间轴的行 */
export type LrcLine = { time: number; text: string }

export function parseLrc(raw: string): LrcLine[] {
  if (!raw || !raw.trim()) return []
  const lines: LrcLine[] = []
  const re = /\[(\d{1,2}):(\d{1,2})(?:\.(\d{1,3}))?\]([^\n\r]*)/g
  let m: RegExpExecArray | null
  while ((m = re.exec(raw)) !== null) {
    const min = Number(m[1])
    const sec = Number(m[2])
    const ms = m[3] ? Number(m[3].padEnd(3, '0').slice(0, 3)) : 0
    const time = min * 60 + sec + ms / 1000
    const text = (m[4] || '').trim()
    if (text) lines.push({ time, text })
  }
  lines.sort((a, b) => a.time - b.time)
  return lines
}

export function activeLrcIndex(lines: LrcLine[], currentSec: number): number {
  if (lines.length === 0) return -1
  let lo = 0
  let hi = lines.length - 1
  let best = -1
  while (lo <= hi) {
    const mid = (lo + hi) >> 1
    if (lines[mid].time <= currentSec) {
      best = mid
      lo = mid + 1
    } else {
      hi = mid - 1
    }
  }
  return best
}
