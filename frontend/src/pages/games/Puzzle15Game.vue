<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()

// 4x4 board, 15 tiles + 1 empty
const N = 4
const TOTAL = N * N
type Board = number[] // 0 = empty, 1..15 = tile index

const board = ref<Board>([])
const moves = ref(0)
const seconds = ref(0)
const started = ref(false)
const won = ref(false)
const bestTime = ref<number | null>(null)
let timer: ReturnType<typeof setInterval> | null = null

// Correct goal: [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0]
const GOAL: Board = Array.from({ length: TOTAL }, (_, i) => (i + 1) % TOTAL)

function isSolved(b: Board) {
  return b.every((v, i) => v === GOAL[i])
}

function shuffle(): Board {
  // Do random legal moves from solved state to ensure solvability
  const b: Board = [...GOAL]
  const dirs = [
    [-1, 0], [1, 0], [0, -1], [0, 1]
  ]
  let lastDir = -1
  for (let s = 0; s < 200; s++) {
    const emptyIdx = b.indexOf(0)
    const ex = emptyIdx % N
    const ey = Math.floor(emptyIdx / N)
    const moves: { idx: number; dir: number }[] = []
    dirs.forEach((d, i) => {
      if (i === lastDir) return // avoid immediate backtrack
      const nx = ex + d[0]
      const ny = ey + d[1]
      if (nx >= 0 && nx < N && ny >= 0 && ny < N) {
        moves.push({ idx: ny * N + nx, dir: i })
      }
    })
    if (moves.length) {
      const m = moves[Math.floor(Math.random() * moves.length)]
      b[emptyIdx] = b[m.idx]
      b[m.idx] = 0
      lastDir = m.dir
    }
  }
  return b
}

function start() {
  board.value = shuffle()
  moves.value = 0
  seconds.value = 0
  started.value = true
  won.value = false
  if (timer) clearInterval(timer)
  timer = setInterval(() => { seconds.value++ }, 1000)
}

function stop() {
  if (timer) { clearInterval(timer); timer = null }
  started.value = false
}

function tryMove(idx: number) {
  if (won.value || !started.value) return
  const emptyIdx = board.value.indexOf(0)
  const ex = emptyIdx % N
  const ey = Math.floor(emptyIdx / N)
  const tx = idx % N
  const ty = Math.floor(idx / N)
  // Must be in same row or column, and adjacent
  const isAdjacent =
    (Math.abs(ex - tx) === 1 && ey === ty) ||
    (Math.abs(ey - ty) === 1 && ex === tx)
  if (!isAdjacent) return
  // Slide the clicked tile into the empty slot
  board.value[emptyIdx] = board.value[idx]
  board.value[idx] = 0
  moves.value++
  if (isSolved(board.value)) {
    won.value = true
    stop()
    if (bestTime.value === null || seconds.value < bestTime.value) {
      bestTime.value = seconds.value
      try { localStorage.setItem('puzzle15-best', String(seconds.value)) } catch {}
    }
  }
}

function key(e: KeyboardEvent) {
  if (!started.value || won.value) return
  const emptyIdx = board.value.indexOf(0)
  const ex = emptyIdx % N
  const ey = Math.floor(emptyIdx / N)
  let targetIdx = -1
  if (e.key === 'ArrowUp' && ey < N - 1) targetIdx = (ey + 1) * N + ex
  else if (e.key === 'ArrowDown' && ey > 0) targetIdx = (ey - 1) * N + ex
  else if (e.key === 'ArrowLeft' && ex < N - 1) targetIdx = ey * N + (ex + 1)
  else if (e.key === 'ArrowRight' && ex > 0) targetIdx = ey * N + (ex - 1)
  if (targetIdx >= 0) {
    e.preventDefault()
    tryMove(targetIdx)
  }
}

const tileColor = (v: number) => {
  if (v === 0) return 'transparent'
  // Color gradient from cool to warm based on value
  const hue = 200 - (v / 15) * 160 // 200 (blue) → 40 (orange)
  return `linear-gradient(135deg, hsl(${hue}, 70%, 60%), hsl(${hue + 20}, 70%, 50%))`
}

const tileStyle = (v: number) => ({
  background: tileColor(v),
  border: v === 0 ? '2px dashed var(--glass-border)' : '1px solid rgba(255,255,255,0.2)',
  color: v === 0 ? 'transparent' : 'white',
  textShadow: v === 0 ? 'none' : '0 1px 2px rgba(0,0,0,0.3)',
})

onMounted(() => {
  try {
    const saved = localStorage.getItem('puzzle15-best')
    if (saved) bestTime.value = parseInt(saved)
  } catch {}
  window.addEventListener('keydown', key)
})
onUnmounted(() => {
  stop()
  window.removeEventListener('keydown', key)
})

const formatTime = (s: number) => {
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${sec.toString().padStart(2, '0')}`
}
</script>

<template>
  <div class="page-container max-w-md mx-auto text-center">
    <button @click="router.push('/')" class="glass-button text-sm mb-6">← 返回首页</button>
    <GlassCard class="heavy !p-6">
      <h1 class="text-2xl font-bold mb-1 gradient-text">🧩 数字华容道</h1>
      <p class="text-sm text-[var(--text-muted)] mb-3">
        点击方块或方向键移动 | {{ started && !won ? '用时：' + formatTime(seconds) : '让 1-15 按顺序排列' }}
      </p>

      <div v-if="!started || won" class="mb-4">
        <button @click="start" class="glass-button primary text-sm">
          {{ won ? '🎉 再来一局' : '开始游戏' }}
        </button>
        <p v-if="bestTime !== null" class="text-xs text-[var(--text-muted)] mt-2">
          🏆 最佳成绩：{{ formatTime(bestTime) }}
        </p>
      </div>

      <div v-if="started || won" class="mb-4 flex justify-around text-sm">
        <div>
          <span class="text-[var(--text-muted)]">步数</span>
          <div class="text-xl font-bold gradient-text">{{ moves }}</div>
        </div>
        <div v-if="!won">
          <span class="text-[var(--text-muted)]">用时</span>
          <div class="text-xl font-bold gradient-text">{{ formatTime(seconds) }}</div>
        </div>
        <div v-else>
          <span class="text-[var(--text-muted)]">总用时</span>
          <div class="text-xl font-bold text-green-400">{{ formatTime(seconds) }}</div>
        </div>
      </div>

      <div
        v-if="started || won"
        class="mx-auto rounded-2xl p-2 grid gap-1.5"
        :style="{
          width: 'min(90vw, 360px)',
          aspectRatio: '1',
          background: 'var(--glass-bg)',
          border: '1.5px solid var(--glass-border)',
          gridTemplateColumns: `repeat(${N}, 1fr)`,
          gridTemplateRows: `repeat(${N}, 1fr)`
        }"
      >
        <button
          v-for="(v, i) in board"
          :key="i"
          @click="tryMove(i)"
          :disabled="v === 0"
          class="rounded-lg font-bold text-2xl md:text-3xl transition-all duration-200 select-none flex items-center justify-center"
          :class="v === 0 ? 'cursor-default' : 'hover:scale-105 active:scale-95 cursor-pointer shadow-md'"
          :style="tileStyle(v)"
        >
          {{ v === 0 ? '' : v }}
        </button>
      </div>

      <p v-if="started && !won" class="text-xs text-[var(--text-muted)] mt-3">
        💡 提示：点击与空格同行/列的方块即可移动
      </p>
      <p v-if="won" class="text-sm text-green-400 mt-3 font-medium">
        🎊 恭喜完成！用时 {{ formatTime(seconds) }}，{{ moves }} 步
      </p>
    </GlassCard>
  </div>
</template>
