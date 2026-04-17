<script setup lang="ts">
/** 贪吃蛇（自研 Canvas） */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'
import { getMoyuHighScore, trySetMoyuHighScore } from '../../utils/moyuScore'

const gameId = 'snake'
const COLS = 20
const ROWS = 20
const CELL = 16

const canvasRef = ref<HTMLCanvasElement | null>(null)
const score = ref(0)
const hi = ref(getMoyuHighScore(gameId))
const over = ref(false)
const paused = ref(false)

type Pt = { x: number; y: number }
let snake: Pt[] = []
let dir: Pt = { x: 1, y: 0 }
let nextDir: Pt = { x: 1, y: 0 }
let food: Pt = { x: 0, y: 0 }
let tickTimer: ReturnType<typeof setInterval> | null = null

function randFood() {
  for (;;) {
    const x = Math.floor(Math.random() * COLS)
    const y = Math.floor(Math.random() * ROWS)
    if (!snake.some((s) => s.x === x && s.y === y)) {
      food = { x, y }
      return
    }
  }
}

function init() {
  if (tickTimer) clearInterval(tickTimer)
  snake = [
    { x: 4, y: 10 },
    { x: 3, y: 10 },
    { x: 2, y: 10 },
  ]
  dir = { x: 1, y: 0 }
  nextDir = { x: 1, y: 0 }
  score.value = 0
  over.value = false
  paused.value = false
  hi.value = getMoyuHighScore(gameId)
  randFood()
  draw()
  tickTimer = setInterval(tick, 120)
}

function tick() {
  if (over.value || paused.value) return
  dir = nextDir
  const head = { x: snake[0].x + dir.x, y: snake[0].y + dir.y }
  if (head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS) {
    end()
    return
  }
  if (snake.some((s) => s.x === head.x && s.y === head.y)) {
    end()
    return
  }
  snake.unshift(head)
  if (head.x === food.x && head.y === food.y) {
    score.value += 10
    hi.value = trySetMoyuHighScore(gameId, score.value)
    randFood()
  } else {
    snake.pop()
  }
  draw()
}

function end() {
  over.value = true
  if (tickTimer) clearInterval(tickTimer)
  tickTimer = null
  draw()
}

function draw() {
  const c = canvasRef.value
  if (!c) return
  const ctx = c.getContext('2d')
  if (!ctx) return
  const isDark = document.documentElement.classList.contains('dark-mode')
  ctx.fillStyle = isDark ? 'rgba(15,23,42,0.95)' : 'rgba(241,245,249,0.95)'
  ctx.fillRect(0, 0, c.width, c.height)
  ctx.strokeStyle = isDark ? 'rgba(148,163,184,0.35)' : 'rgba(100,116,139,0.25)'
  for (let x = 0; x <= COLS; x++) {
    ctx.beginPath()
    ctx.moveTo(x * CELL, 0)
    ctx.lineTo(x * CELL, ROWS * CELL)
    ctx.stroke()
  }
  for (let y = 0; y <= ROWS; y++) {
    ctx.beginPath()
    ctx.moveTo(0, y * CELL)
    ctx.lineTo(COLS * CELL, y * CELL)
    ctx.stroke()
  }
  ctx.fillStyle = '#f472b6'
  ctx.fillRect(food.x * CELL + 2, food.y * CELL + 2, CELL - 4, CELL - 4)
  snake.forEach((s, i) => {
    ctx.fillStyle = i === 0 ? '#38bdf8' : '#22d3ee'
    ctx.fillRect(s.x * CELL + 1, s.y * CELL + 1, CELL - 2, CELL - 2)
  })
  if (over.value) {
    ctx.fillStyle = 'rgba(0,0,0,0.45)'
    ctx.fillRect(0, 0, c.width, c.height)
    ctx.fillStyle = '#fff'
    ctx.font = 'bold 16px system-ui'
    ctx.textAlign = 'center'
    ctx.fillText('游戏结束', c.width / 2, c.height / 2 - 8)
  } else if (paused.value) {
    ctx.fillStyle = 'rgba(0,0,0,0.35)'
    ctx.fillRect(0, 0, c.width, c.height)
    ctx.fillStyle = '#fff'
    ctx.font = '14px system-ui'
    ctx.textAlign = 'center'
    ctx.fillText('已暂停 (P)', c.width / 2, c.height / 2)
  }
}

function setDir(dx: number, dy: number) {
  if (over.value) return
  if (dx === -dir.x && dy === -dir.y) return
  nextDir = { x: dx, y: dy }
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'ArrowLeft') setDir(-1, 0)
  if (e.key === 'ArrowRight') setDir(1, 0)
  if (e.key === 'ArrowUp') setDir(0, -1)
  if (e.key === 'ArrowDown') setDir(0, 1)
  if (e.key === 'p' || e.key === 'P') {
    paused.value = !paused.value
    draw()
  }
  e.preventDefault()
}

const highText = computed(() => `最高分 ${hi.value} · 得分 ${score.value}`)

onMounted(() => {
  init()
  window.addEventListener('keydown', onKey)
})
onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
  if (tickTimer) clearInterval(tickTimer)
})
</script>

<template>
  <MoyuGameShell title="贪吃蛇" :high-text="highText">
    <div class="wrap">
      <p class="hint">方向键移动 · P 暂停</p>
      <canvas
        ref="canvasRef"
        class="cv"
        :width="COLS * CELL"
        :height="ROWS * CELL"
        @touchstart.prevent
      />
      <div class="mob">
        <button type="button" class="site-pill" @click="setDir(0, -1)">↑</button>
        <div class="mob-mid">
          <button type="button" class="site-pill" @click="setDir(-1, 0)">←</button>
          <button type="button" class="site-pill" @click="setDir(1, 0)">→</button>
        </div>
        <button type="button" class="site-pill" @click="setDir(0, 1)">↓</button>
      </div>
      <button v-if="over" type="button" class="site-pill site-pill--active mt" @click="init">再来一局</button>
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
  display: block;
  margin: 12px auto;
  border-radius: 16px;
  touch-action: none;
  max-width: 100%;
  height: auto;
}
.mob {
  display: none;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}
.mob-mid {
  display: flex;
  gap: 40px;
}
@media (max-width: 640px) {
  .mob {
    display: flex;
  }
}
.mt {
  margin-top: 12px;
}
</style>
