<script setup lang="ts">
/** 俄罗斯方块（自研简化版） */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const W = 10
const H = 20
const gameId = 'tetris'

const SHAPES = [
  [[1, 1, 1, 1]],
  [
    [1, 1],
    [1, 1],
  ],
  [
    [0, 1, 0],
    [1, 1, 1],
  ],
  [
    [1, 0, 0],
    [1, 1, 1],
  ],
  [
    [0, 0, 1],
    [1, 1, 1],
  ],
  [
    [1, 1, 0],
    [0, 1, 1],
  ],
  [
    [0, 1, 1],
    [1, 1, 0],
  ],
]

const board = ref<number[][]>([])
const piece = ref<{ shape: number[][]; x: number; y: number; c: number } | null>(null)
const runScore = ref(0)
const over = ref(false)
const hi = ref(getMoyuHighScore(gameId))

const colors = ['', '#38bdf8', '#4ade80', '#fbbf24', '#f472b6', '#a78bfa', '#fb7185']

function emptyBoard() {
  return Array.from({ length: H }, () => Array(W).fill(0))
}

function collide(shape: number[][], x: number, y: number) {
  for (let r = 0; r < shape.length; r++) {
    for (let c = 0; c < shape[r].length; c++) {
      if (!shape[r][c]) continue
      const nx = x + c
      const ny = y + r
      if (nx < 0 || nx >= W || ny >= H) return true
      if (ny >= 0 && board.value[ny][nx]) return true
    }
  }
  return false
}

function spawn() {
  const shape = SHAPES[Math.floor(Math.random() * SHAPES.length)].map((r) => [...r])
  const c = Math.floor(Math.random() * 6) + 1
  const x = Math.floor((W - shape[0].length) / 2)
  if (collide(shape, x, 0)) {
    over.value = true
    piece.value = null
    if (timer) clearInterval(timer)
    timer = null
    return
  }
  piece.value = { shape, x, y: 0, c }
}

function mergePiece() {
  const p = piece.value!
  for (let r = 0; r < p.shape.length; r++) {
    for (let c = 0; c < p.shape[r].length; c++) {
      if (!p.shape[r][c]) continue
      const y = p.y + r
      const x = p.x + c
      if (y >= 0) board.value[y][x] = p.c
    }
  }
  let cleared = 0
  const nb = board.value.filter((row) => {
    if (row.every((x) => x > 0)) {
      cleared++
      return false
    }
    return true
  })
  while (nb.length < H) nb.unshift(Array(W).fill(0))
  board.value = nb
  runScore.value += cleared * 100 + cleared * cleared * 20
  hi.value = trySetMoyuHighScore(gameId, runScore.value)
  piece.value = null
  if (!over.value) spawn()
}

function softDrop() {
  if (!piece.value || over.value) return
  const p = piece.value
  if (!collide(p.shape, p.x, p.y + 1)) {
    piece.value = { ...p, y: p.y + 1 }
  } else {
    mergePiece()
  }
}

function moveH(dx: number) {
  if (!piece.value || over.value) return
  const p = piece.value
  if (!collide(p.shape, p.x + dx, p.y)) piece.value = { ...p, x: p.x + dx }
}

function rotate() {
  if (!piece.value || over.value) return
  const p = piece.value
  const s = p.shape
  const rows = s.length
  const cols = s[0].length
  const ns = Array.from({ length: cols }, (_, c) =>
    Array.from({ length: rows }, (_, r) => s[rows - 1 - r][c]),
  )
  if (!collide(ns, p.x, p.y)) piece.value = { ...p, shape: ns }
}

let timer: ReturnType<typeof setInterval> | null = null

function init() {
  if (timer) clearInterval(timer)
  board.value = emptyBoard()
  runScore.value = 0
  over.value = false
  hi.value = getMoyuHighScore(gameId)
  spawn()
  timer = setInterval(softDrop, 600)
}

function onKey(e: KeyboardEvent) {
  if (over.value) return
  if (e.key === 'ArrowLeft') moveH(-1)
  if (e.key === 'ArrowRight') moveH(1)
  if (e.key === 'ArrowDown') softDrop()
  if (e.key === 'ArrowUp') rotate()
  e.preventDefault()
}

const display = computed(() => {
  const g = board.value.map((r) => [...r])
  const p = piece.value
  if (p) {
    for (let r = 0; r < p.shape.length; r++) {
      for (let c = 0; c < p.shape[r].length; c++) {
        if (!p.shape[r][c]) continue
        const y = p.y + r
        const x = p.x + c
        if (y >= 0 && y < H && x >= 0 && x < W) g[y][x] = p.c
      }
    }
  }
  return g
})

const highText = computed(() => `最高分 ${hi.value} · 得分 ${runScore.value}`)

onMounted(() => {
  init()
  window.addEventListener('keydown', onKey)
})
onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
  if (timer) clearInterval(timer)
})
</script>

<template>
  <MoyuGameShell title="俄罗斯方块" :high-text="highText">
    <div class="tet">
      <p class="hint">← → 移动，↑ 旋转，↓ 加速下落</p>
      <div class="grid">
        <div v-for="(row, r) in display" :key="r" class="row">
          <div
            v-for="(v, c) in row"
            :key="c"
            class="cell"
            :style="{ background: v ? colors[v % colors.length] : 'rgba(255,255,255,0.06)' }"
          />
        </div>
      </div>
      <button v-if="over" type="button" class="site-pill site-pill--active mt" @click="init">再来一局</button>
      <button type="button" class="site-pill mt" @click="init">重置</button>
    </div>
  </MoyuGameShell>
</template>

<style scoped>
.tet {
  text-align: center;
}
.hint {
  color: var(--text-color);
  opacity: 0.85;
  font-size: 0.9rem;
}
.grid {
  display: inline-block;
  border-radius: 16px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.15);
}
.row {
  display: flex;
}
.cell {
  width: 22px;
  height: 22px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-sizing: border-box;
}
.mt {
  margin: 8px 4px 0;
}
@media (min-width: 480px) {
  .cell {
    width: 26px;
    height: 26px;
  }
}
</style>
