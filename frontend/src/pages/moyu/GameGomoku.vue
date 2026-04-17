<script setup lang="ts">
/** 五子棋 vs 简单 AI（自研） */
import { computed, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const gameId = 'gomoku'
const N = 15
const EMPTY = 0
const HUM = 1
const AI = 2

const board = ref<number[][]>([])
const msg = ref('你执黑先行')
const over = ref(false)
const wins = ref(0)
const hi = ref(getMoyuHighScore(gameId))

function initBoard() {
  board.value = Array.from({ length: N }, () => Array(N).fill(EMPTY))
  over.value = false
  msg.value = '你执黑先行 · 连赢 AI 计连胜'
}

function init() {
  wins.value = 0
  hi.value = getMoyuHighScore(gameId)
  initBoard()
}

const dirs = [
  [1, 0],
  [0, 1],
  [1, 1],
  [1, -1],
]

function checkWin(x: number, y: number, p: number) {
  for (const [dx, dy] of dirs) {
    let c = 1
    for (let s = 1; s < 5; s++) {
      const nx = x + dx * s
      const ny = y + dy * s
      if (nx < 0 || ny < 0 || nx >= N || ny >= N || board.value[ny][nx] !== p) break
      c++
    }
    for (let s = 1; s < 5; s++) {
      const nx = x - dx * s
      const ny = y - dy * s
      if (nx < 0 || ny < 0 || nx >= N || ny >= N || board.value[ny][nx] !== p) break
      c++
    }
    if (c >= 5) return true
  }
  return false
}

function scorePoint(x: number, y: number, p: number): number {
  let sc = 0
  for (const [dx, dy] of dirs) {
    let a = 0
    let b = 0
    for (let s = 1; s < 5; s++) {
      const nx = x + dx * s
      const ny = y + dy * s
      if (nx < 0 || ny < 0 || nx >= N || ny >= N) break
      if (board.value[ny][nx] === p) a++
      else break
    }
    for (let s = 1; s < 5; s++) {
      const nx = x - dx * s
      const ny = y - dy * s
      if (nx < 0 || ny < 0 || nx >= N || ny >= N) break
      if (board.value[ny][nx] === p) b++
      else break
    }
    const t = a + b + 1
    if (t >= 5) sc += 100000
    else if (t === 4) sc += 5000
    else if (t === 3) sc += 200
    else if (t === 2) sc += 20
  }
  return sc
}

function aiMove() {
  let best = -1
  let bx = 7
  let by = 7
  for (let y = 0; y < N; y++) {
    for (let x = 0; x < N; x++) {
      if (board.value[y][x] !== EMPTY) continue
      const atk = scorePoint(x, y, AI)
      const def = scorePoint(x, y, HUM)
      const s = atk * 1.05 + def
      if (s > best) {
        best = s
        bx = x
        by = y
      }
    }
  }
  board.value[by][bx] = AI
  board.value = board.value.map((r) => [...r])
  if (checkWin(bx, by, AI)) {
    over.value = true
    wins.value = 0
    msg.value = 'AI 获胜 · 连胜已清零'
  }
}

function onCell(y: number, x: number) {
  if (over.value || board.value[y][x] !== EMPTY) return
  board.value[y][x] = HUM
  board.value = board.value.map((r) => [...r])
  if (checkWin(x, y, HUM)) {
    over.value = true
    wins.value++
    hi.value = trySetMoyuHighScore(gameId, wins.value)
    msg.value = '你赢了！'
    return
  }
  aiMove()
}

const highText = computed(() => `连胜纪录 ${hi.value} · 当前连胜 ${wins.value}`)

init()
</script>

<template>
  <MoyuGameShell title="五子棋" :high-text="highText">
    <div class="go">
      <p class="hint">{{ msg }}</p>
      <div class="board" :class="{ over }">
        <div v-for="(row, y) in board" :key="y" class="row">
          <button
            v-for="(v, x) in row"
            :key="x"
            type="button"
            class="cell"
            :disabled="over"
            @click="onCell(y, x)"
          >
            <span v-if="v === HUM" class="b" />
            <span v-else-if="v === AI" class="w" />
          </button>
        </div>
      </div>
      <button type="button" class="site-pill site-pill--active mt" @click="initBoard">再来一局</button>
      <button type="button" class="site-pill mt" @click="init">重置连胜</button>
    </div>
  </MoyuGameShell>
</template>

<style scoped>
.go {
  text-align: center;
}
.hint {
  color: var(--text-color);
  opacity: 0.9;
  font-size: 0.95rem;
}
.board {
  display: inline-block;
  padding: 8px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.12);
  margin: 12px auto;
}
.row {
  display: flex;
}
.cell {
  width: 22px;
  height: 22px;
  margin: 1px;
  padding: 0;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cell:disabled {
  opacity: 0.95;
  cursor: default;
}
.b,
.w {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: block;
}
.b {
  background: #0f172a;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2);
}
.w {
  background: #f8fafc;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.15);
}
.mt {
  margin: 8px 6px;
}
@media (min-width: 480px) {
  .cell {
    width: 26px;
    height: 26px;
  }
  .b,
  .w {
    width: 18px;
    height: 18px;
  }
}
</style>
