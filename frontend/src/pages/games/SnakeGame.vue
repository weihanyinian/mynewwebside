<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()

const canvas = ref<HTMLCanvasElement>()
let ctx: CanvasRenderingContext2D | null = null
const grid = 20
const count = 20
let snake: { x: number; y: number }[] = []
let food = { x: 10, y: 10 }
let dx = 0; let dy = 0
let score = 0
let over = false
let loop: ReturnType<typeof setInterval> | null = null
let started = false

const cell = computed(() => Math.floor(Math.min(window.innerWidth * 0.6, 400) / count))

function draw() {
  if (!ctx || !canvas.value) return
  const s = cell.value
  canvas.value.width = count * s; canvas.value.height = count * s
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  snake.forEach((p, i) => {
    ctx!.fillStyle = i === 0 ? '#62a7ea' : '#a58eea'
    ctx!.fillRect(p.x * s, p.y * s, s - 2, s - 2)
  })
  ctx.fillStyle = '#f06262'
  ctx.fillRect(food.x * s, food.y * s, s - 2, s - 2)
}

function tick() {
  if (over) return
  const h = { x: snake[0].x + dx, y: snake[0].y + dy }
  if (h.x < 0 || h.x >= count || h.y < 0 || h.y >= count || snake.some(p => p.x === h.x && p.y === h.y)) {
    over = true; if (loop) { clearInterval(loop); loop = null }; return
  }
  snake.unshift(h)
  if (h.x === food.x && h.y === food.y) { score += 10; place() }
  else { snake.pop() }
  draw()
}

function place() {
  const f: { x: number; y: number }[] = []
  for (let x = 0; x < count; x++) for (let y = 0; y < count; y++)
    if (!snake.some(p => p.x === x && p.y === y)) f.push({ x, y })
  if (f.length === 0) { over = true; return }
  food = f[Math.floor(Math.random() * f.length)]
}

function init() { snake = [{ x: 10, y: 10 }]; dx = 0; dy = 0; score = 0; over = false; started = false; place(); draw() }
function start() { if (started) return; started = true; loop = setInterval(tick, 100) }
function restart() { if (loop) { clearInterval(loop); loop = null }; init() }

function key(e: KeyboardEvent) {
  if (over) return; if (!started) start()
  const k = e.key
  if (k === 'ArrowUp' && dy !== 1) { dx = 0; dy = -1 }
  else if (k === 'ArrowDown' && dy !== -1) { dx = 0; dy = 1 }
  else if (k === 'ArrowLeft' && dx !== 1) { dx = -1; dy = 0 }
  else if (k === 'ArrowRight' && dx !== -1) { dx = 1; dy = 0 }
  else return
  e.preventDefault()
}

onMounted(() => { ctx = canvas.value!.getContext('2d'); init(); window.addEventListener('keydown', key) })
onUnmounted(() => { if (loop) clearInterval(loop); window.removeEventListener('keydown', key) })
</script>

<template>
  <div class="page-container max-w-lg mx-auto text-center">
    <button @click="router.push('/')" class="glass-button text-sm mb-6">← 返回首页</button>
    <GlassCard class="heavy !p-6">
      <h1 class="text-2xl font-bold mb-1 gradient-text">🐍 贪吃蛇</h1>
      <p class="text-sm text-[var(--text-muted)] mb-3">方向键控制 | 得分：{{ score }}</p>
      <canvas ref="canvas" class="mx-auto rounded-xl border-2 border-[var(--glass-border)]" style="background:var(--glass-bg)" />
      <div v-if="over" class="mt-4">
        <p class="text-lg font-bold text-red-400 mb-2">游戏结束！得分：{{ score }}</p>
        <button @click="restart" class="glass-button primary text-sm">再来一局</button>
      </div>
      <p v-if="!started && !over" class="text-xs text-[var(--text-muted)] mt-3">按方向键开始</p>
    </GlassCard>
  </div>
</template>
