<script setup lang="ts">
/** 扫雷（自研；首次点开安全区） */
import { computed, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const gameId = 'minesweeper'
const R = 9
const C = 9
const MINES = 10

type Cell = { mine: boolean; open: boolean; flag: boolean; n: number }
const grid = ref<Cell[][]>([])
const started = ref(false)
const dead = ref(false)
const win = ref(false)
const flags = ref(0)
const hi = ref(getMoyuHighScore(gameId))
/** 胜利得分：剩余未挖格子为雷时结算，越快分越高 */
const runScore = ref(0)
const t0 = ref(0)

function empty() {
  return Array.from({ length: R }, () =>
    Array.from({ length: C }, () => ({ mine: false, open: false, flag: false, n: 0 })),
  )
}

function countNeighbors(gr: Cell[][], y: number, x: number) {
  let n = 0
  for (let dy = -1; dy <= 1; dy++) {
    for (let dx = -1; dx <= 1; dx++) {
      if (!dy && !dx) continue
      const ny = y + dy
      const nx = x + dx
      if (ny >= 0 && ny < R && nx >= 0 && nx < C && gr[ny][nx].mine) n++
    }
  }
  return n
}

function placeMines(sy: number, sx: number) {
  const gr = empty()
  let left = MINES
  while (left > 0) {
    const y = Math.floor(Math.random() * R)
    const x = Math.floor(Math.random() * C)
    if (gr[y][x].mine) continue
    if (Math.abs(y - sy) <= 1 && Math.abs(x - sx) <= 1) continue
    gr[y][x].mine = true
    left--
  }
  for (let y = 0; y < R; y++) {
    for (let x = 0; x < C; x++) {
      if (!gr[y][x].mine) gr[y][x].n = countNeighbors(gr, y, x)
    }
  }
  return gr
}

function flood(gr: Cell[][], y: number, x: number) {
  const st: [number, number][] = [[y, x]]
  while (st.length) {
    const [cy, cx] = st.pop()!
    const cell = gr[cy][cx]
    if (cell.open || cell.flag) continue
    cell.open = true
    if (cell.n === 0) {
      for (let dy = -1; dy <= 1; dy++) {
        for (let dx = -1; dx <= 1; dx++) {
          if (!dy && !dx) continue
          const ny = cy + dy
          const nx = cx + dx
          if (ny >= 0 && ny < R && nx >= 0 && nx < C) st.push([ny, nx])
        }
      }
    }
  }
}

function checkWin(gr: Cell[][]) {
  for (let y = 0; y < R; y++) {
    for (let x = 0; x < C; x++) {
      const c = gr[y][x]
      if (!c.mine && !c.open) return false
    }
  }
  return true
}

function onCellClick(y: number, x: number, ev: MouseEvent) {
  if (dead.value || win.value) return
  const right = ev.button === 2 || ev.ctrlKey
  const gr = grid.value
  const cell = gr[y][x]
  if (right) {
    if (cell.open) return
    cell.flag = !cell.flag
    flags.value += cell.flag ? 1 : -1
    grid.value = [...gr.map((row) => [...row])]
    return
  }
  if (cell.flag) return
  if (!started.value) {
    grid.value = placeMines(y, x)
    started.value = true
    t0.value = Date.now()
  }
  const g = grid.value
  const c = g[y][x]
  if (c.open) return
  if (c.mine) {
    c.open = true
    dead.value = true
    for (let yy = 0; yy < R; yy++) {
      for (let xx = 0; xx < C; xx++) {
        if (g[yy][xx].mine) g[yy][xx].open = true
      }
    }
    grid.value = [...g.map((row) => [...row])]
    return
  }
  flood(g, y, x)
  if (checkWin(g)) {
    win.value = true
    const sec = Math.max(1, Math.floor((Date.now() - t0.value) / 1000))
    const pts = 5000 - sec * 50 + MINES * 20
    runScore.value = Math.max(0, pts)
    hi.value = trySetMoyuHighScore(gameId, runScore.value)
  }
  grid.value = [...g.map((row) => [...row])]
}

function init() {
  grid.value = empty()
  started.value = false
  dead.value = false
  win.value = false
  flags.value = 0
  runScore.value = 0
  hi.value = getMoyuHighScore(gameId)
}

const highText = computed(() => {
  if (win.value) return `最高分 ${hi.value} · 本局 ${runScore.value} 分`
  return `最高分 ${hi.value}`
})

init()
</script>

<template>
  <MoyuGameShell title="扫雷" :high-text="highText">
    <div class="ms">
      <p class="hint">左键翻开 · 右键或 Ctrl+左键 插旗 · {{ MINES - flags }} 雷待标</p>
      <div class="board" @contextmenu.prevent>
        <div v-for="(row, y) in grid" :key="y" class="row">
          <button
            v-for="(cell, x) in row"
            :key="x"
            type="button"
            class="cell"
            :class="{
              open: cell.open,
              mine: cell.open && cell.mine,
              flag: cell.flag,
              [`n-${cell.n}`]: cell.open && !cell.mine && cell.n > 0,
            }"
            @click="onCellClick(y, x, $event)"
            @contextmenu.prevent="onCellClick(y, x, $event)"
          >
            <span v-if="cell.flag">🚩</span>
            <span v-else-if="cell.open && cell.mine">💣</span>
            <span v-else-if="cell.open && cell.n">{{ cell.n }}</span>
          </button>
        </div>
      </div>
      <div class="acts">
        <button type="button" class="site-pill site-pill--active" @click="init">新局</button>
        <span v-if="dead" class="bad">踩到雷了</span>
        <span v-if="win" class="ok">胜利！</span>
      </div>
    </div>
  </MoyuGameShell>
</template>

<style scoped>
.ms {
  text-align: center;
}
.hint {
  color: var(--text-color);
  opacity: 0.85;
  font-size: 0.9rem;
}
.board {
  display: inline-block;
  margin: 12px auto;
  padding: 8px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.12);
}
.row {
  display: flex;
}
.cell {
  width: 32px;
  height: 32px;
  margin: 2px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.12);
  color: var(--text-color);
  font-weight: 800;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}
.cell.open {
  background: rgba(255, 255, 255, 0.06);
}
.cell.mine {
  background: rgba(239, 68, 68, 0.35);
}
.n-1 {
  color: #38bdf8;
}
.n-2 {
  color: #4ade80;
}
.n-3 {
  color: #f87171;
}
.n-4 {
  color: #a78bfa;
}
.n-5 {
  color: #fbbf24;
}
.n-6 {
  color: #22d3ee;
}
.n-7,
.n-8 {
  color: #94a3b8;
}
.acts {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}
.bad {
  color: #f87171;
  font-weight: 700;
}
.ok {
  color: #4ade80;
  font-weight: 700;
}
@media (max-width: 400px) {
  .cell {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
}
</style>
