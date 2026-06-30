<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()

type Difficulty = 'easy' | 'medium' | 'hard'
interface Cell {
  mine: boolean
  revealed: boolean
  flagged: boolean
  adjacent: number
}

const difficulties: Record<Difficulty, { rows: number; cols: number; mines: number; label: string }> = {
  easy: { rows: 9, cols: 9, mines: 10, label: '简单 9×9 / 10雷' },
  medium: { rows: 16, cols: 16, mines: 40, label: '中等 16×16 / 40雷' },
  hard: { rows: 16, cols: 30, mines: 99, label: '困难 16×30 / 99雷' },
}

const difficulty = ref<Difficulty>('easy')
const rows = computed(() => difficulties[difficulty.value].rows)
const cols = computed(() => difficulties[difficulty.value].cols)
const totalMines = computed(() => difficulties[difficulty.value].mines)

const grid = ref<Cell[][]>([])
const gameState = ref<'idle' | 'playing' | 'won' | 'lost'>('idle')
const flagCount = ref(0)
const firstClick = ref(true)
const startedAt = ref(0)
const elapsed = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

function initGrid() {
  const r = rows.value
  const c = cols.value
  grid.value = Array.from({ length: r }, () =>
    Array.from({ length: c }, () => ({ mine: false, revealed: false, flagged: false, adjacent: 0 }))
  )
  flagCount.value = 0
  firstClick.value = true
  gameState.value = 'idle'
  elapsed.value = 0
  if (timer) { clearInterval(timer); timer = null }
}

function placeMines(safeRow: number, safeCol: number) {
  const r = rows.value
  const c = cols.value
  const m = totalMines.value
  const placed: { row: number; col: number }[] = []
  const safeZone = new Set<string>()
  // First-click safe: protect clicked cell + 8 neighbors
  for (let dr = -1; dr <= 1; dr++) {
    for (let dc = -1; dc <= 1; dc++) {
      const nr = safeRow + dr
      const nc = safeCol + dc
      if (nr >= 0 && nr < r && nc >= 0 && nc < c) {
        safeZone.add(`${nr},${nc}`)
      }
    }
  }
  let attempts = 0
  while (placed.length < m && attempts < 10000) {
    attempts++
    const row = Math.floor(Math.random() * r)
    const col = Math.floor(Math.random() * c)
    if (safeZone.has(`${row},${col}`)) continue
    if (grid.value[row][col].mine) continue
    grid.value[row][col].mine = true
    placed.push({ row, col })
  }
  // Compute adjacency
  for (let row = 0; row < r; row++) {
    for (let col = 0; col < c; col++) {
      if (grid.value[row][col].mine) continue
      let count = 0
      for (let dr = -1; dr <= 1; dr++) {
        for (let dc = -1; dc <= 1; dc++) {
          if (dr === 0 && dc === 0) continue
          const nr = row + dr
          const nc = col + dc
          if (nr >= 0 && nr < r && nc >= 0 && nc < c && grid.value[nr][nc].mine) count++
        }
      }
      grid.value[row][col].adjacent = count
    }
  }
}

function reveal(row: number, col: number) {
  if (gameState.value === 'won' || gameState.value === 'lost') return
  const cell = grid.value[row][col]
  if (cell.revealed || cell.flagged) return
  if (firstClick.value) {
    firstClick.value = false
    placeMines(row, col)
    gameState.value = 'playing'
    startedAt.value = Date.now()
    timer = setInterval(() => {
      elapsed.value = Math.floor((Date.now() - startedAt.value) / 1000)
    }, 250)
  }
  cell.revealed = true
  if (cell.mine) {
    gameState.value = 'lost'
    // Reveal all mines
    for (let r = 0; r < rows.value; r++) {
      for (let c = 0; c < cols.value; c++) {
        if (grid.value[r][c].mine) grid.value[r][c].revealed = true
      }
    }
    if (timer) { clearInterval(timer); timer = null }
    return
  }
  // Flood fill for empty cells
  if (cell.adjacent === 0) {
    for (let dr = -1; dr <= 1; dr++) {
      for (let dc = -1; dc <= 1; dc++) {
        if (dr === 0 && dc === 0) continue
        const nr = row + dr
        const nc = col + dc
        if (nr >= 0 && nr < rows.value && nc >= 0 && nc < cols.value) {
          reveal(nr, nc)
        }
      }
    }
  }
  // Win check
  checkWin()
}

function checkWin() {
  let safeRevealed = 0
  let totalSafe = rows.value * cols.value - totalMines.value
  for (let r = 0; r < rows.value; r++) {
    for (let c = 0; c < cols.value; c++) {
      if (!grid.value[r][c].mine && grid.value[r][c].revealed) safeRevealed++
    }
  }
  if (safeRevealed === totalSafe) {
    gameState.value = 'won'
    if (timer) { clearInterval(timer); timer = null }
  }
}

function toggleFlag(row: number, col: number, e: MouseEvent) {
  e.preventDefault()
  if (gameState.value === 'won' || gameState.value === 'lost') return
  const cell = grid.value[row][col]
  if (cell.revealed) return
  cell.flagged = !cell.flagged
  flagCount.value += cell.flagged ? 1 : -1
}

function reset(diff?: Difficulty) {
  if (diff) difficulty.value = diff
  initGrid()
}

function chord(row: number, col: number) {
  // Click on revealed numbered cell: auto-reveal neighbors if flags match
  if (gameState.value !== 'playing') return
  const cell = grid.value[row][col]
  if (!cell.revealed || cell.adjacent === 0) return
  let flagAround = 0
  for (let dr = -1; dr <= 1; dr++) {
    for (let dc = -1; dc <= 1; dc++) {
      if (dr === 0 && dc === 0) continue
      const nr = row + dr
      const nc = col + dc
      if (nr >= 0 && nr < rows.value && nc >= 0 && nc < cols.value && grid.value[nr][nc].flagged) flagAround++
    }
  }
  if (flagAround === cell.adjacent) {
    for (let dr = -1; dr <= 1; dr++) {
      for (let dc = -1; dc <= 1; dc++) {
        if (dr === 0 && dc === 0) continue
        const nr = row + dr
        const nc = col + dc
        if (nr >= 0 && nr < rows.value && nc >= 0 && nc < cols.value) {
          if (!grid.value[nr][nc].flagged) reveal(nr, nc)
        }
      }
    }
  }
}

const numberColors = [
  '', // 0
  '#3b82f6', // 1 blue
  '#22c55e', // 2 green
  '#ef4444', // 3 red
  '#7c3aed', // 4 purple
  '#a16207', // 5 maroon
  '#06b6d4', // 6 cyan
  '#1f2937', // 7 black
  '#6b7280', // 8 gray
]

const cellSize = computed(() => {
  // Adjust to fit screen width
  return `min(calc((100vw - 64px) / ${cols.value}), 36px)`
})

onMounted(() => initGrid())
onUnmounted(() => { if (timer) clearInterval(timer) })

const formatTime = (s: number) => s.toString().padStart(3, '0')
</script>

<template>
  <div class="page-container max-w-5xl mx-auto">
    <button @click="router.push('/')" class="glass-button text-sm mb-6">← 返回首页</button>
    <GlassCard class="heavy !p-4 md:!p-6">
      <h1 class="text-2xl font-bold mb-1 gradient-text text-center">💣 扫雷</h1>
      <p class="text-sm text-[var(--text-muted)] mb-3 text-center">
        左键揭开 | 右键标旗 | 难度：
        <button v-for="d in (Object.keys(difficulties) as Difficulty[])" :key="d"
          @click="reset(d)"
          class="ml-1 px-2 py-0.5 rounded text-xs transition-all"
          :class="difficulty === d
            ? 'bg-[#62a7ea30] border border-[#62a7ea] text-white'
            : 'bg-[var(--glass-bg)] border border-[var(--glass-border)] text-[var(--text-secondary)] hover:text-white'">
          {{ difficulties[d].label }}
        </button>
      </p>

      <div class="flex items-center justify-between mb-3 px-2">
        <div class="text-sm font-mono flex items-center gap-1.5">
          <span class="text-red-400">💣</span>
          <span class="font-bold">{{ totalMines - flagCount }}</span>
        </div>
        <button @click="reset()" class="glass-button !p-2 !rounded-full text-xl" title="重新开始">
          {{ gameState === 'won' ? '😎' : gameState === 'lost' ? '😵' : '🙂' }}
        </button>
        <div class="text-sm font-mono flex items-center gap-1.5">
          <span class="text-yellow-400">⏱</span>
          <span class="font-bold">{{ formatTime(elapsed) }}</span>
        </div>
      </div>

      <div v-if="gameState === 'won'" class="text-center text-green-400 font-bold mb-3">
        🎉 胜利！用时 {{ elapsed }} 秒
      </div>
      <div v-if="gameState === 'lost'" class="text-center text-red-400 font-bold mb-3">
        💥 游戏结束！点击 😵 重新开始
      </div>

      <div class="overflow-x-auto pb-2">
        <div
          class="grid gap-0.5 mx-auto w-fit"
          :style="{
            gridTemplateColumns: `repeat(${cols}, ${cellSize})`,
            gridTemplateRows: `repeat(${rows}, ${cellSize})`
          }"
        >
          <template v-for="(row, ri) in grid" :key="`row-${ri}`">
            <button
              v-for="(cell, ci) in row"
              :key="`cell-${ri}-${ci}`"
              @click="reveal(ri, ci)"
              @contextmenu="toggleFlag(ri, ci, $event)"
              @dblclick="chord(ri, ci)"
              class="rounded font-bold flex items-center justify-center transition-all duration-100 select-none"
              :class="[
                cell.revealed
                  ? (cell.mine ? '!bg-red-500/80' : '!bg-[var(--glass-bg)] border border-[var(--glass-border)]')
                  : '!bg-gradient-to-br !from-[var(--glass-bg-heavy)] !to-[var(--glass-bg)] border border-[var(--glass-border)] hover:!from-[#62a7ea30] hover:!to-[#62a7ea20] active:scale-95 cursor-pointer shadow-sm'
              ]"
              :style="{
                width: cellSize,
                height: cellSize,
                fontSize: cell.revealed ? 'calc(' + cellSize + ' * 0.5)' : 'calc(' + cellSize + ' * 0.6)',
                color: cell.revealed && !cell.mine ? numberColors[cell.adjacent] : ''
              }"
            >
              <template v-if="cell.revealed && cell.mine">💣</template>
              <template v-else-if="cell.revealed && cell.adjacent > 0">{{ cell.adjacent }}</template>
              <template v-else-if="cell.flagged">🚩</template>
            </button>
          </template>
        </div>
      </div>

      <p class="text-xs text-[var(--text-muted)] mt-3 text-center">
        💡 双击已揭开的数字，若周围旗帜数=数字，则自动揭开其余格子
      </p>
    </GlassCard>
  </div>
</template>
