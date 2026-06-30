<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()
const gridSize = ref(5)
const numbers = ref<{ value: number; clicked: boolean }[]>([])
const nextNumber = ref(1)
const gameStarted = ref(false)
const gameOver = ref(false)
const startTime = ref(0)
const elapsedTime = ref(0)
const bestTime = ref(Infinity)
const message = ref('')
let timer: number | null = null

onMounted(() => {
  generateGrid()
})

function generateGrid() {
  gameStarted.value = false
  gameOver.value = false
  nextNumber.value = 1
  elapsedTime.value = 0
  message.value = ''
  const total = gridSize.value * gridSize.value
  const arr = Array.from({ length: total }, (_, i) => i + 1)
  // Shuffle
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]]
  }
  numbers.value = arr.map(v => ({ value: v, clicked: false }))
}

function clickNumber(num: { value: number; clicked: boolean }) {
  if (num.clicked || gameOver.value) return
  if (!gameStarted.value) {
    gameStarted.value = true
    startTime.value = Date.now()
    timer = window.setInterval(() => {
      elapsedTime.value = (Date.now() - startTime.value) / 1000
    }, 100)
  }
  if (num.value === nextNumber.value) {
    num.clicked = true
    nextNumber.value++
    if (nextNumber.value > gridSize.value * gridSize.value) {
      gameOver.value = true
      if (timer) clearInterval(timer)
      const t = parseFloat(elapsedTime.value.toFixed(2))
      if (t < bestTime.value) bestTime.value = t
      message.value = `🎉 完成！用时 ${t} 秒`
    }
  } else {
    message.value = `❌ 请点击 ${nextNumber.value}`
    setTimeout(() => { message.value = '' }, 1500)
  }
}

function changeSize(size: number) {
  gridSize.value = size
  bestTime.value = Infinity
  generateGrid()
}

function newGame() {
  if (timer) clearInterval(timer)
  generateGrid()
}
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center px-4 py-8">
    <GlassCard class="!p-6 md:!p-8 text-center max-w-lg w-full">
      <h1 class="text-2xl font-bold mb-1 gradient-text">舒尔特方格</h1>
      <p class="text-sm text-[var(--text-muted)] mb-4">按顺序从 1 点到最大数</p>

      <!-- Size Selector -->
      <div class="flex justify-center gap-2 mb-4">
        <button v-for="s in [3, 4, 5, 6]" :key="s" @click="changeSize(s)"
          :class="['glass-button text-xs !py-1 !px-3', gridSize === s ? 'primary' : '']">
          {{ s }}×{{ s }}
        </button>
      </div>

      <!-- Timer & Info -->
      <div class="flex justify-center gap-6 mb-4 text-sm">
        <span class="text-[var(--text-muted)]">⏱ {{ elapsedTime.toFixed(1) }}s</span>
        <span class="text-[var(--text-muted)]">下一个: <strong class="text-[var(--primary)]">{{ nextNumber }}</strong></span>
        <span v-if="bestTime < Infinity" class="text-[var(--text-muted)]">🏆 {{ bestTime }}s</span>
      </div>

      <!-- Grid -->
      <div
        class="grid gap-2 mx-auto"
        :style="{
          gridTemplateColumns: `repeat(${gridSize}, 1fr)`,
          maxWidth: gridSize * 60 + 'px'
        }"
      >
        <button
          v-for="(num, i) in numbers"
          :key="i"
          @click="clickNumber(num)"
          :class="[
            'aspect-square rounded-lg font-bold text-lg transition-all duration-200',
            num.clicked
              ? 'bg-green-500/20 text-green-400 border border-green-500/30 cursor-default'
              : 'bg-white/10 hover:bg-white/20 text-[var(--text-primary)] border border-white/10 cursor-pointer'
          ]"
          :style="{ fontSize: gridSize > 5 ? '0.9rem' : '1.1rem' }"
        >
          {{ num.value }}
        </button>
      </div>

      <!-- Message -->
      <p v-if="message" class="mt-4 text-sm font-medium">{{ message }}</p>

      <!-- Actions -->
      <div class="flex justify-center gap-3 mt-6">
        <button @click="newGame" class="glass-button primary text-sm">🔄 新游戏</button>
        <button @click="router.push('/')" class="glass-button text-sm">← 返回</button>
      </div>
    </GlassCard>
  </div>
</template>
