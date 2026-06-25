<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()

type Cell = number | null
const grid = ref<Cell[][]>([])
const score = ref(0)
const over = ref(false)
const won = ref(false)

const tileColors: Record<number, string> = {
  2: '#eee4da', 4: '#ede0c8', 8: '#f2b179', 16: '#f59563',
  32: '#f67c5f', 64: '#f65e3b', 128: '#edcf72', 256: '#edcc61',
  512: '#edc850', 1024: '#edc53f', 2048: '#edc22e',
}

function empty() {
  const cells: { r: number; c: number }[] = []
  grid.value.forEach((row, r) => row.forEach((cell, c) => { if (!cell) cells.push({ r, c }) }))
  return cells
}

function spawn() {
  const cells = empty()
  if (cells.length === 0) return
  const { r, c } = cells[Math.floor(Math.random() * cells.length)]
  grid.value[r][c] = Math.random() < 0.9 ? 2 : 4
}

function init() {
  grid.value = Array.from({ length: 4 }, () => Array(4).fill(null))
  score.value = 0; over.value = false; won.value = false
  spawn(); spawn()
}

function slide(row: Cell[]): Cell[] {
  const filtered = row.filter(c => c !== null) as number[]
  for (let i = 0; i < filtered.length - 1; i++) {
    if (filtered[i] === filtered[i + 1]) {
      filtered[i] *= 2
      score.value += filtered[i]
      if (filtered[i] === 2048) won.value = true
      filtered.splice(i + 1, 1)
    }
  }
  while (filtered.length < 4) filtered.push(null)
  return filtered
}

function move(dir: 'up' | 'down' | 'left' | 'right') {
  const g = grid.value
  const old = g.map(r => [...r])
  let moved = false

  for (let i = 0; i < 4; i++) {
    let row: Cell[] = []
    if (dir === 'left') row = slide(g[i])
    else if (dir === 'right') row = slide([...g[i]].reverse()).reverse()
    else if (dir === 'up') row = slide([g[0][i], g[1][i], g[2][i], g[3][i]])
    else row = slide([g[3][i], g[2][i], g[1][i], g[0][i]]).reverse()

    if (dir === 'left') g[i] = row
    else if (dir === 'right') g[i] = row
    else if (dir === 'up') { for (let r = 0; r < 4; r++) g[r][i] = row[r] }
    else { for (let r = 0; r < 4; r++) g[3 - r][i] = row[r] }
  }

  for (let r = 0; r < 4; r++) for (let c = 0; c < 4; c++) if (g[r][c] !== old[r][c]) { moved = true; break }

  if (moved) {
    spawn()
    if (empty().length === 0 && !canMove()) over.value = true
  }
}

function canMove() {
  const g = grid.value
  for (let r = 0; r < 4; r++) for (let c = 0; c < 4; c++) {
    if (!g[r][c]) return true
    if (c < 3 && g[r][c] === g[r][c + 1]) return true
    if (r < 3 && g[r][c] === g[r + 1][c]) return true
  }
  return false
}

function getColor(v: number) { return tileColors[v] || '#3c3a32' }
function getTextColor(v: number) { return v <= 4 ? '#776e65' : '#f9f6f2' }

function key(e: KeyboardEvent) {
  if (over.value) return
  const dirs: Record<string, 'up' | 'down' | 'left' | 'right'> = {
    ArrowUp: 'up', ArrowDown: 'down', ArrowLeft: 'left', ArrowRight: 'right',
    w: 'up', s: 'down', a: 'left', d: 'right',
  }
  const d = dirs[e.key]
  if (d) { move(d); e.preventDefault() }
}

onMounted(() => { init(); window.addEventListener('keydown', key) })
onUnmounted(() => window.removeEventListener('keydown', key))
</script>

<template>
  <div class="page-container max-w-sm mx-auto text-center">
    <button @click="router.push('/games')" class="glass-button text-sm mb-6">← 返回游戏大厅</button>
    <GlassCard class="heavy !p-6">
      <div class="flex items-center justify-between mb-4">
        <h1 class="text-xl font-bold gradient-text">🔢 2048</h1>
        <span class="glass-card !px-4 !py-2 text-lg font-bold text-white bg-gradient-to-r from-[#62a7ea] to-[#a58eea]">{{ score }}</span>
      </div>
      <div class="grid grid-cols-4 gap-2 p-3 rounded-xl" style="background:var(--glass-bg)">
        <div v-for="(row, r) in grid" :key="r" v-for-x class="contents">
          <div v-for="(cell, c) in row" :key="c"
            class="aspect-square rounded-lg flex items-center justify-center text-lg font-bold transition-all duration-150 select-none"
            :style="{ background: cell ? getColor(cell) : 'rgba(255,255,255,0.1)', color: cell ? getTextColor(cell) : 'transparent' }"
          >{{ cell }}</div>
        </div>
      </div>
      <div v-if="over" class="mt-4 space-y-2">
        <p class="text-red-400 font-bold">游戏结束！</p>
        <button @click="init" class="glass-button primary text-sm">再来一局</button>
      </div>
      <div v-if="won && !over" class="mt-3">
        <p class="text-yellow-400 font-bold text-sm">🎉 达到 2048！继续挑战更高分</p>
      </div>
      <p class="text-xs text-[var(--text-muted)] mt-3">方向键 / WASD 操作</p>
    </GlassCard>
  </div>
</template>
