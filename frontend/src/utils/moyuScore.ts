/** 摸鱼小游戏本地最高分（纯前端） */
export function getMoyuHighScore(gameId: string): number {
  const v = localStorage.getItem(`moyu-hi-${gameId}`)
  return v ? Number(v) : 0
}

export function trySetMoyuHighScore(gameId: string, score: number): number {
  const cur = getMoyuHighScore(gameId)
  if (score > cur) {
    localStorage.setItem(`moyu-hi-${gameId}`, String(score))
    return score
  }
  return cur
}
