<script setup lang="ts">
/** 恐龙跑酷（简化自研，参考 Chrome 离线恐龙） */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const gameId = 'trex'
const W = 600
const H = 160
const GROUND = 120

const canvasRef = ref<HTMLCanvasElement | null>(null)
const score = ref(0)
const hi = ref(getMoyuHighScore(gameId))
const playing = ref(false)
const gameOver = ref(false)

let vy = 0
let y = GROUND - 40
let obs: { x: number; w: number; h: number }[] = []
let speed = 5
let raf = 0
let last = 0

function resetRun() {
  vy = 0
  y = GROUND - 40
  obs = []
  speed = 5
  score.value = 0
  gameOver.value = false
  playing.value = true
  last = performance.now()
}

function init() {
  hi.value = getMoyuHighScore(gameId)
  cancelAnimationFrame(raf)
  resetRun()
  loop(last)
}

function jump() {
  if (!playing.value) {
    init()
    return
  }
  if (y >= GROUND - 40 - 0.5) vy = -11
}

function spawnObs() {
  if (!obs.length || obs[obs.length - 1].x < W - 200 - Math.random() * 120) {
    const h = 24 + Math.floor(Math.random() * 20)
    obs.push({ x: W + 20, w: 18, h })
  }
}

function hit(): boolean {
  const px = 80
  const pw = 34
  const ph = 36
  const top = y
  for (const o of obs) {
    if (o.x < px + pw && o.x + o.w > px && top + ph > GROUND - o.h) return true
  }
  return false
}

function loop(t: number) {
  raf = requestAnimationFrame(loop)
  const dt = Math.min(40, t - last)
  last = t
  if (!playing.value) {
    draw()
    return
  }
  score.value += Math.floor(speed * (dt / 16))
  if (score.value % 600 === 0) speed += 0.15
  vy += 0.55 * (dt / 16)
  y += vy * (dt / 16)
  if (y > GROUND - 40) {
    y = GROUND - 40
    vy = 0
  }
  spawnObs()
  for (const o of obs) o.x -= speed * (dt / 16)
  obs = obs.filter((o) => o.x > -40)
  if (hit()) {
    playing.value = false
    gameOver.value = true
    hi.value = trySetMoyuHighScore(gameId, score.value)
  }
  draw()
}

function draw() {
  const c = canvasRef.value
  if (!c) return
  const ctx = c.getContext('2d')
  if (!ctx) return
  const dark = document.documentElement.classList.contains('dark-mode')
  ctx.fillStyle = dark ? '#0f172a' : '#e2e8f0'
  ctx.fillRect(0, 0, W, H)
  ctx.fillStyle = dark ? '#334155' : '#94a3b8'
  ctx.fillRect(0, GROUND, W, H - GROUND)
  ctx.fillStyle = '#38bdf8'
  ctx.fillRect(80, y, 34, 36)
  ctx.fillStyle = dark ? '#64748b' : '#475569'
  for (const o of obs) {
    ctx.fillRect(o.x, GROUND - o.h, o.w, o.h)
  }
  if (gameOver.value) {
    ctx.fillStyle = 'rgba(0,0,0,0.45)'
    ctx.fillRect(0, 0, W, H)
    ctx.fillStyle = '#fff'
    ctx.font = 'bold 18px system-ui'
    ctx.textAlign = 'center'
    ctx.fillText('点击或空格重新开始', W / 2, H / 2)
  } else if (!playing.value) {
    ctx.fillStyle = dark ? '#e2e8f0' : '#0f172a'
    ctx.font = '16px system-ui'
    ctx.textAlign = 'center'
    ctx.fillText('空格 / 点击跳跃', W / 2, H / 2)
  }
}

function onKey(e: KeyboardEvent) {
  if (e.code === 'Space') {
    jump()
    e.preventDefault()
  }
}

const highText = computed(() => `最高分 ${hi.value} · 距离 ${score.value}`)

onMounted(() => {
  hi.value = getMoyuHighScore(gameId)
  resetRun()
  playing.value = false
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
  <MoyuGameShell title="恐龙跑酷" :high-text="highText">
    <div class="wrap">
      <p class="hint">空格或点击画布跳跃 · 躲避障碍</p>
      <canvas
        ref="canvasRef"
        class="cv"
        :width="W"
        :height="H"
        @click="jump"
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
