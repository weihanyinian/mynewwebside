<script setup lang="ts">
/** 内置 2048（玩法参考 gabrielecirulli/2048，自研实现） */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const SIZE = 4
const gameId = '2048'

const grid = ref<number[]>(Array(16).fill(0))
const score = ref(0)
const over = ref(false)
const hi = ref(getMoyuHighScore(gameId))

function emptyCells(): number[] {
  const e: number[] = []
  for (let i = 0; i < SIZE * SIZE; i++) if (grid.value[i] === 0) e.push(i)
  return e
}

function addRandom() {
  const e = emptyCells()
  if (!e.length) return
  const i = e[Math.floor(Math.random() * e.length)]
  const g = [...grid.value]
  g[i] = Math.random() < 0.9 ? 2 : 4
  grid.value = g
}

function init() {
  grid.value = Array(16).fill(0)
  score.value = 0
  over.value = false
  addRandom()
  addRandom()
}

function slideLine(line: number[]): { line: number[]; gained: number } {
  const filtered = line.filter((x) => x !== 0)
  const out: number[] = []
  let gained = 0
  for (let i = 0; i < filtered.length; i++) {
    if (i < filtered.length - 1 && filtered[i] === filtered[i + 1]) {
      const v = filtered[i] * 2
      out.push(v)
      gained += v
      i++
    } else {
      out.push(filtered[i])
    }
  }
  while (out.length < SIZE) out.push(0)
  return { line: out, gained }
}

function move(dir: 'l' | 'r' | 'u' | 'd') {
  if (over.value) return
  let g = [...grid.value]
  let moved = false
  let gained = 0

  if (dir === 'l' || dir === 'r') {
    for (let r = 0; r < SIZE; r++) {
      let line = []
      for (let c = 0; c < SIZE; c++) line.push(g[r * SIZE + c])
      if (dir === 'r') line.reverse()
      const { line: nl, gained: gg } = slideLine(line)
      gained += gg
      if (dir === 'r') nl.reverse()
      for (let c = 0; c < SIZE; c++) {
        const v = nl[c]
        if (g[r * SIZE + c] !== v) moved = true
        g[r * SIZE + c] = v
      }
    }
  } else {
    for (let c = 0; c < SIZE; c++) {
      let line = []
      for (let r = 0; r < SIZE; r++) line.push(g[r * SIZE + c])
      if (dir === 'd') line.reverse()
      const { line: nl, gained: gg } = slideLine(line)
      gained += gg
      if (dir === 'd') nl.reverse()
      for (let r = 0; r < SIZE; r++) {
        const v = nl[r]
        if (g[r * SIZE + c] !== v) moved = true
        g[r * SIZE + c] = v
      }
    }
  }

  if (!moved) return
  grid.value = g
  score.value += gained
  hi.value = trySetMoyuHighScore(gameId, score.value)
  addRandom()
  if (!emptyCells().length && !canMove()) over.value = true
}

function canMove(): boolean {
  const g = grid.value
  for (let r = 0; r < SIZE; r++) {
    for (let c = 0; c < SIZE; c++) {
      const v = g[r * SIZE + c]
      if (v === 0) return true
      if (c < SIZE - 1 && g[r * SIZE + c + 1] === v) return true
      if (r < SIZE - 1 && g[(r + 1) * SIZE + c] === v) return true
    }
  }
  return false
}

function onKey(e: KeyboardEvent) {
  const k = e.key
  if (k === 'ArrowLeft') {
    move('l')
    e.preventDefault()
  }
  if (k === 'ArrowRight') {
    move('r')
    e.preventDefault()
  }
  if (k === 'ArrowUp') {
    move('u')
    e.preventDefault()
  }
  if (k === 'ArrowDown') {
    move('d')
    e.preventDefault()
  }
}

const highText = computed(() => `最高分 ${hi.value} · 当前 ${score.value}`)

onMounted(() => {
  init()
  window.addEventListener('keydown', onKey)
})
onUnmounted(() => window.removeEventListener('keydown', onKey))

function cellBg(v: number) {
  if (v === 0) return 'transparent'
  const colors: Record<number, string> = {
    2: '#e0f2fe',
    4: '#bae6fd',
    8: '#7dd3fc',
    16: '#38bdf8',
    32: '#0ea5e9',
    64: '#0284c7',
    128: '#fbbf24',
    256: '#f59e0b',
    512: '#ea580c',
    1024: '#dc2626',
    2048: '#7c3aed',
  }
  return colors[v] || '#4c1d95'
}
</script>

<template>
  <MoyuGameShell title="2048" :high-text="highText">
    <div class="g2048">
      <p class="hint">方向键移动；合并相同数字，达成 2048！</p>
      <div class="board">
        <div
          v-for="(v, i) in grid"
          :key="i"
          class="cell"
          :style="{ background: cellBg(v), color: v > 4 && v <= 64 ? '#fff' : '#0f172a' }"
        >
          {{ v || '' }}
        </div>
      </div>
      <div v-if="over" class="over glass-surface">游戏结束 · <button type="button" class="site-pill site-pill--active" @click="init">再来</button></div>
      <button type="button" class="site-pill mt" @click="init">重新开始</button>
    </div>
  </MoyuGameShell>
</template>

<style scoped>
.g2048 {
  text-align: center;
}
.hint {
  color: var(--text-color);
  opacity: 0.85;
  margin: 0 0 12px;
  font-size: 0.9rem;
}
.board {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  max-width: 360px;
  margin: 0 auto;
  padding: 12px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.12);
}
.cell {
  aspect-ratio: 1;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.25);
}
.over {
  margin-top: 16px;
  padding: 12px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: var(--text-color);
}
.mt {
  margin-top: 16px;
}
</style>
