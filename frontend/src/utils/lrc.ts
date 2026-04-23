/** 解析网易云 LRC（[mm:ss.xx] 文本）为带时间轴的行 */
export type LrcLine = { time: number; text: string }
export type BilingualLrcLine = { time: number; primary: string; secondary?: string }

function hasCjk(text: string): boolean {
  return /[\u3400-\u9fff]/.test(text)
}

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

export function parseBilingualLrc(primaryRaw: string, secondaryRaw: string): BilingualLrcLine[] {
  const primary = parseLrc(primaryRaw)
  const secondary = parseLrc(secondaryRaw)
  if (!primary.length && !secondary.length) return []

  const merged: BilingualLrcLine[] = primary.map((p) => ({
    time: p.time,
    primary: p.text,
  }))
  const usedPrimary = new Set<number>()
  const PAIR_TOLERANCE_SEC = 0.45

  function findNearestPrimaryIndex(targetTime: number): number {
    let bestIdx = -1
    let bestDiff = Number.POSITIVE_INFINITY
    for (let i = 0; i < merged.length; i++) {
      if (usedPrimary.has(i) || merged[i].secondary) continue
      const diff = Math.abs(merged[i].time - targetTime)
      if (diff < bestDiff) {
        bestDiff = diff
        bestIdx = i
      }
    }
    return bestDiff <= PAIR_TOLERANCE_SEC ? bestIdx : -1
  }

  for (const s of secondary) {
    const idx = findNearestPrimaryIndex(s.time)
    if (idx >= 0) {
      const prev = merged[idx]
      const p = prev.primary
      const aHasCjk = hasCjk(p)
      const bHasCjk = hasCjk(s.text)
      // 外语曲目经常出现主/副歌词源反转：优先把“非中文行”放在主行，中文放副行。
      if (aHasCjk && !bHasCjk) {
        prev.primary = s.text
        prev.secondary = p
      } else {
        prev.secondary = s.text
      }
      usedPrimary.add(idx)
    } else {
      merged.push({ time: s.time, primary: s.text })
    }
  }
  return merged.sort((a, b) => a.time - b.time)
}

export function activeBilingualLrcIndex(lines: BilingualLrcLine[], currentSec: number): number {
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
