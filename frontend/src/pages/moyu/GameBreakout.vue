<script setup lang="ts">
/** 打砖块（Canvas 自研） */
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const gameId = 'breakout'
const W = 480
const H = 360

const canvasRef = ref<HTMLCanvasElement | null>(null)
const score = ref(0)
const lives = ref(3)
const hi = ref(getMoyuHighScore(gameId))

type Brick = { x: number; y: number; w: number; h: number; alive: boolean; hue: number }
let bricks: Brick[] = []
let px: number
let py: number
let pw = 72
let ph = 10
let bx: number
let by: number
let bvx: number
let bvy: number
let br = 6
let running = false
let raf = 0
let last = 0

function buildBricks() {
  bricks = []
  const cols = 10
  const bw = (W - 40) / cols
  const bh = 18
  for (let r = 0; r < 5; r++) {
    for (let c = 0; c < cols; c++) {
      bricks.push({
        x: 20 + c * bw,
        y: 40 + r * (bh + 4),
        w: bw - 4,
        h: bh,
        alive: true,
        hue: 190 + r * 18,
      })
    }
  }
}

function resetBall() {
  px = W / 2 - pw / 2
  py = H - 36
  bx = W / 2
  by = H - 50
  bvx = 3.2 * (Math.random() > 0.5 ? 1 : -1)
  bvy = -3.4
}

function init() {
  score.value = 0
  lives.value = 3
  hi.value = getMoyuHighScore(gameId)
  buildBricks()
  resetBall()
  running = true
  last = performance.now()
}

function loseLife() {
  lives.value--
  if (lives.value <= 0) {
    running = false
    hi.value = trySetMoyuHighScore(gameId, score.value)
  } else {
    resetBall()
  }
}

function loop(t: number) {
  raf = requestAnimationFrame(loop)
  const dt = Math.min(32, t - last)
  last = t
  if (!running) {
    draw()
    return
  }
  bx += bvx * (dt / 16)
  by += bvy * (dt / 16)
  if (bx < br || bx > W - br) bvx *= -1
  if (by < br) bvy *= -1
  if (by > H + 20) {
    loseLife()
  }
  if (by > py - br && by < py + ph && bx > px && bx < px + pw) {
    bvy = -Math.abs(bvy)
    const hit = (bx - (px + pw / 2)) / (pw / 2)
    bvx += hit * 1.2
  }
  for (const b of bricks) {
    if (!b.alive) continue
    if (bx + br > b.x && bx - br < b.x + b.w && by + br > b.y && by - br < b.y + b.h) {
      b.alive = false
      score.value += 10
      hi.value = trySetMoyuHighScore(gameId, score.value)
      const overlapX = Math.min(bx + br - b.x, b.x + b.w - (bx - br))
      const overlapY = Math.min(by + br - b.y, b.y + b.h - (by - br))
      if (overlapX < overlapY) bvx *= -1
      else bvy *= -1
      break
    }
  }
  if (!bricks.some((b) => b.alive)) {
    buildBricks()
    resetBall()
    score.value += 50
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
  ctx.fillStyle = dark ? '#0f172a' : '#f1f5f9'
  ctx.fillRect(0, 0, W, H)
  for (const b of bricks) {
    if (!b.alive) continue
    ctx.fillStyle = `hsl(${b.hue} 80% ${dark ? 45 : 55}%)`
    ctx.fillRect(b.x, b.y, b.w, b.h)
  }
  ctx.fillStyle = '#38bdf8'
  ctx.fillRect(px, py, pw, ph)
  ctx.fillStyle = '#f472b6'
  ctx.beginPath()
  ctx.arc(bx, by, br, 0, Math.PI * 2)
  ctx.fill()
  ctx.fillStyle = dark ? '#e2e8f0' : '#0f172a'
  ctx.font = '14px system-ui'
  ctx.fillText(`得分 ${score.value} · 生命 ${lives.value}`, 12, 24)
  if (!running && lives.value <= 0) {
    ctx.fillStyle = 'rgba(0,0,0,0.45)'
    ctx.fillRect(0, 0, W, H)
    ctx.fillStyle = '#fff'
    ctx.font = 'bold 18px system-ui'
    ctx.textAlign = 'center'
    ctx.fillText('点击重新开始', W / 2, H / 2)
  }
}

function onMove(e: MouseEvent | TouchEvent) {
  const c = canvasRef.value
  if (!c || !running) return
  const r = c.getBoundingClientRect()
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const x = ((clientX - r.left) / r.width) * W
  px = Math.max(0, Math.min(W - pw, x - pw / 2))
}

function onClick() {
  if (!running && lives.value <= 0) init()
}

onMounted(() => {
  init()
  raf = requestAnimationFrame(loop)
  void nextTick(() => {
    const c = canvasRef.value
    c?.addEventListener('mousemove', onMove)
    c?.addEventListener('touchmove', onMove, { passive: true })
  })
})
onUnmounted(() => {
  cancelAnimationFrame(raf)
  const c = canvasRef.value
  c?.removeEventListener('mousemove', onMove)
  c?.removeEventListener('touchmove', onMove)
})

const highText = computed(() => `最高分 ${hi.value} · 得分 ${score.value}`)
</script>

<template>
  <MoyuGameShell title="打砖块" :high-text="highText">
    <div class="wrap">
      <p class="hint">鼠标 / 手指移动挡板 · 清空砖块加关</p>
      <canvas
        ref="canvasRef"
        class="cv"
        :width="W"
        :height="H"
        @click="onClick"
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
  touch-action: none;
}
</style>
