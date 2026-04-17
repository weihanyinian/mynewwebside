<script setup lang="ts">
/** Flappy Bird 风格（自研 Canvas） */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const gameId = 'flappy'
const W = 360
const H = 480

const canvasRef = ref<HTMLCanvasElement | null>(null)
const score = ref(0)
const hi = ref(getMoyuHighScore(gameId))

let birdY = H / 2
let vy = 0
let pipes: { x: number; gapY: number; passed: boolean }[] = []
let running = false
let over = false
let raf = 0
let last = 0

function reset() {
  birdY = H / 2
  vy = 0
  pipes = []
  score.value = 0
  running = false
  over = false
  hi.value = getMoyuHighScore(gameId)
}

function start() {
  if (running && !over) return
  if (over) reset()
  running = true
  over = false
  vy = -4
  last = performance.now()
}

function flap() {
  if (!running) {
    start()
    return
  }
  vy = -6.5
}

function spawnPipe() {
  const lastX = pipes.length ? pipes[pipes.length - 1].x : W
  if (lastX < W - 160) {
    const gapY = 120 + Math.random() * (H - 240)
    pipes.push({ x: W + 30, gapY, passed: false })
  }
}

function loop(t: number) {
  raf = requestAnimationFrame(loop)
  const dt = Math.min(32, t - last)
  last = t
  if (!running || over) {
    draw()
    return
  }
  vy += 0.35 * (dt / 16)
  birdY += vy * (dt / 16)
  spawnPipe()
  for (const p of pipes) {
    p.x -= 3 * (dt / 16)
    if (!p.passed && p.x + 40 < 80) {
      p.passed = true
      score.value++
      hi.value = trySetMoyuHighScore(gameId, score.value)
    }
  }
  pipes = pipes.filter((p) => p.x > -60)
  const bx = 70
  const by = birdY
  const br = 14
  if (by - br < 0 || by + br > H) {
    end()
  }
  for (const p of pipes) {
    const gap = 100
    const topH = p.gapY - gap / 2
    const botT = p.gapY + gap / 2
    if (bx + br > p.x && bx - br < p.x + 50) {
      if (by - br < topH || by + br > botT) end()
    }
  }
  draw()
}

function end() {
  over = true
  running = false
  hi.value = trySetMoyuHighScore(gameId, score.value)
}

function draw() {
  const c = canvasRef.value
  if (!c) return
  const ctx = c.getContext('2d')
  if (!ctx) return
  const dark = document.documentElement.classList.contains('dark-mode')
  const g1 = dark ? '#0c4a6e' : '#7dd3fc'
  const g2 = dark ? '#082f49' : '#bae6fd'
  const grd = ctx.createLinearGradient(0, 0, 0, H)
  grd.addColorStop(0, g1)
  grd.addColorStop(1, g2)
  ctx.fillStyle = grd
  ctx.fillRect(0, 0, W, H)
  ctx.fillStyle = dark ? '#166534' : '#86efac'
  ctx.fillRect(0, H - 36, W, 36)
  for (const p of pipes) {
    const gap = 100
    const topH = p.gapY - gap / 2
    const botT = p.gapY + gap / 2
    ctx.fillStyle = dark ? '#22c55e' : '#15803d'
    ctx.fillRect(p.x, 0, 50, topH)
    ctx.fillRect(p.x, botT, 50, H - botT)
  }
  ctx.fillStyle = '#fbbf24'
  ctx.beginPath()
  ctx.arc(70, birdY, 14, 0, Math.PI * 2)
  ctx.fill()
  ctx.fillStyle = dark ? '#fff' : '#0f172a'
  ctx.font = 'bold 22px system-ui'
  ctx.fillText(String(score.value), W / 2 - 8, 40)
  if (!running && !over) {
    ctx.fillStyle = 'rgba(0,0,0,0.35)'
    ctx.fillRect(0, H / 2 - 40, W, 80)
    ctx.fillStyle = '#fff'
    ctx.font = '15px system-ui'
    ctx.textAlign = 'center'
    ctx.fillText('点击 / 空格开始并扇动翅膀', W / 2, H / 2)
  }
  if (over) {
    ctx.fillStyle = 'rgba(0,0,0,0.5)'
    ctx.fillRect(0, 0, W, H)
    ctx.fillStyle = '#fff'
    ctx.font = 'bold 18px system-ui'
    ctx.textAlign = 'center'
    ctx.fillText('再点一次重来', W / 2, H / 2)
  }
}

function onKey(e: KeyboardEvent) {
  if (e.code === 'Space') {
    flap()
    e.preventDefault()
  }
}

const highText = computed(() => `最高分 ${hi.value} · 管道 ${score.value}`)

onMounted(() => {
  reset()
  last = performance.now()
  raf = requestAnimationFrame(loop)
  window.addEventListener('keydown', onKey)
})
onUnmounted(() => {
  cancelAnimationFrame(raf)
  window.removeEventListener('keydown', onKey)
})
</script>

<template>
  <MoyuGameShell title="Flappy Bird" :high-text="highText">
    <div class="wrap">
      <p class="hint">点击画面或空格：起飞 / 扇动</p>
      <canvas
        ref="canvasRef"
        class="cv"
        :width="W"
        :height="H"
        @click="flap"
      />
    </div>
  </MoyuGameShell>
</template>

<style scoped>
.wrap {
  text-align: center;
}
.hint {
  color: var(--text-color);
  opacity: 0.85;
  font-size: 0.9rem;
}
.cv {
  max-width: 100%;
  height: auto;
  border-radius: 16px;
  cursor: pointer;
  display: block;
  margin: 12px auto;
}
</style>
