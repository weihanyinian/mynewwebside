<script setup lang="ts">
import { ref } from 'vue'
import GlassCard from '../../components/GlassCard.vue'

const size = 5
const grid = ref<number[][]>([])
const next = ref(1)
const timer = ref('0.0')
const running = ref(false)
const finished = ref(false)
let startTime = 0
let timerId: ReturnType<typeof setInterval> | null = null
let flatCells: number[] = []

function initGrid() {
  const arr = Array.from({ length: size * size }, (_, i) => i + 1)
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]]
  }
  grid.value = []
  for (let r = 0; r < size; r++) {
    grid.value.push(arr.slice(r * size, (r + 1) * size))
  }
  flatCells = arr
  next.value = 1
  timer.value = '0.0'
  running.value = false
  finished.value = false
  if (timerId) { clearInterval(timerId); timerId = null }
}

function clickCell(r: number, c: number) {
  if (finished.value) return
  const val = grid.value[r][c]
  if (val !== next.value) return

  if (!running.value) {
    running.value = true
    startTime = Date.now()
    timerId = setInterval(() => {
      timer.value = ((Date.now() - startTime) / 1000).toFixed(1)
    }, 100)
  }

  next.value++
  if (val === size * size) {
    finished.value = true
    if (timerId) { clearInterval(timerId); timerId = null }
  }
}

initGrid()
</script>

<template>
  <div class="page-container max-w-xl text-center">
    <button @click="$router.push('/tools')" class="glass-button text-sm mb-8">← 返回工具箱</button>
    <h1 class="text-3xl font-bold mb-6 gradient-text">🧠 舒尔特方格</h1>

    <GlassCard>
      <p class="text-[var(--text-secondary)] mb-4 text-sm">按 1→{{ size * size }} 顺序依次点击</p>
      <div class="text-2xl font-bold gradient-text mb-4">{{ timer }}s</div>

      <div class="inline-grid gap-1.5" :style="{ gridTemplateColumns: `repeat(${size}, 52px)` }">
        <template v-for="r in size" :key="r">
          <button
            v-for="c in size" :key="c"
            class="w-12 h-12 rounded-lg text-sm font-bold transition-all border"
            :class="grid[r-1]?.[c-1] < next
              ? 'border-green-500/50 bg-green-500/20 text-green-400'
              : grid[r-1]?.[c-1] === next
              ? 'border-[#62a7ea] bg-[#62a7ea1a] text-white ring-1 ring-[#62a7ea50]'
              : 'border-[var(--glass-border)] bg-[var(--glass-bg)] text-[var(--text-secondary)]'"
            @click="grid[r-1]?.[c-1] && clickCell(r-1, c-1)"
          >
            {{ grid[r-1]?.[c-1] }}
          </button>
        </template>
      </div>

      <p v-if="finished" class="mt-4 text-lg font-bold gradient-text">完成！用时 {{ timer }}s</p>

      <button @click="initGrid" class="glass-button text-sm mt-6">重新开始</button>
    </GlassCard>
  </div>
</template>
