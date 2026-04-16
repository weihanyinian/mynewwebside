<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

/** 台词池：按顺序循环（点击依次换下一句，列表用尽后从头再来） */
const quotes = [
  '我也要死吗？',
  '我也要加班吗？',
  '我也要上学吗？',
  '我也要吃土吗？',
  '我也要卷吗？',
  '（忍住不吐的恐惧...）',
  '这药的副作用好大...',
  '今天也是活人微死的一天',
  '躺平式 emo 中...',
  '咋地？我也得没啊？',
  '我都要扑街咩？',
  '俺也得走？中不中啊？',
  '我也要学汉语吗？',
]

const currentQuote = ref(quotes[0])
/** 下一句要从 quotes 的哪一格开始读（顺序循环） */
let quoteOrderIndex = 0

const isMinimized = ref(false)
const showBubble = ref(true)
/** 点击锁：冷却期内为 true，防止连点刷屏 */
const isSpeaking = ref(false)
/** 点击缩放反馈 */
const showPressFeedback = ref(false)

let hideTimer: ReturnType<typeof setTimeout> | null = null
/** 1 秒点击冷却 */
let speakCooldownTimer: ReturnType<typeof setTimeout> | null = null

const live2dCanvas = ref<HTMLCanvasElement | null>(null)
let app: any = null
let model: any = null

// Draggable State
const position = ref({ x: 20, y: 20 })
let isDragging = false
let hasDragged = false
let startMouse = { x: 0, y: 0 }
let startPos = { x: 0, y: 0 }
/** 记录按下起点，避免点在「收起」钮上结束时误触台词 */
let dragStartTarget: EventTarget | null = null

/** 同一次轻点内可能重复触发，用时间窗去重 */
let lastPokeEventAt = 0

const BUBBLE_AUTO_HIDE_MS = 3200
const CLICK_COOLDOWN_MS = 1000
const POKE_DEDUPE_MS = 120
const PRESS_FEEDBACK_MS = 220

function resetHideTimer() {
  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    showBubble.value = false
  }, BUBBLE_AUTO_HIDE_MS)
}

/** 顺序取下一句台词（不修改 currentQuote，仅返回值） */
function takeNextQuoteLine(): string {
  const line = quotes[quoteOrderIndex % quotes.length]
  quoteOrderIndex = (quoteOrderIndex + 1) % quotes.length
  return line
}

/** 展示气泡并启动自动淡出 */
function showQuoteLine(text: string) {
  currentQuote.value = text
  showBubble.value = true
  resetHideTimer()
}

function triggerPressFeedback() {
  showPressFeedback.value = true
  window.setTimeout(() => {
    showPressFeedback.value = false
  }, PRESS_FEEDBACK_MS)
}

function armSpeakCooldown() {
  isSpeaking.value = true
  if (speakCooldownTimer) clearTimeout(speakCooldownTimer)
  speakCooldownTimer = setTimeout(() => {
    isSpeaking.value = false
    speakCooldownTimer = null
  }, CLICK_COOLDOWN_MS)
}

function handlePoke() {
  const now = Date.now()
  if (now - lastPokeEventAt < POKE_DEDUPE_MS) return
  lastPokeEventAt = now

  if (isMinimized.value) {
    isMinimized.value = false
    showBubble.value = true
    resetHideTimer()
    return
  }

  // 冷却内：完全不响应，不切换台词
  if (isSpeaking.value) return

  armSpeakCooldown()
  triggerPressFeedback()
  showQuoteLine(takeNextQuoteLine())

  if (model && model.internalModel && model.internalModel.motionManager) {
    try {
      model.internalModel.motionManager.expressionManager?.setRandomExpression()
    } catch {
      /* ignore */
    }
  }
}

function toggleMinimize(e: Event) {
  e.stopPropagation()
  isMinimized.value = !isMinimized.value
  showBubble.value = false
}

// --- Drag Logic（与点击台词互不干扰：拖动结束后 hasDragged 会吞掉误触） ---
function startDrag(e: MouseEvent | TouchEvent) {
  if ((e.target as HTMLElement).classList.contains('close-btn')) return

  dragStartTarget = e.target
  isDragging = true
  hasDragged = false
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY

  startMouse = { x: clientX, y: clientY }
  startPos = { x: position.value.x, y: position.value.y }

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag, { passive: false })
  document.addEventListener('touchend', stopDrag)
}

function onDrag(e: MouseEvent | TouchEvent) {
  if (!isDragging) return

  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY

  const dx = clientX - startMouse.x
  const dy = clientY - startMouse.y

  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
    hasDragged = true
  }

  if (hasDragged) {
    e.preventDefault()
    position.value.x = startPos.x + dx
    position.value.y = startPos.y - dy
  }
}

function stopDrag() {
  isDragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)

  const dragged = hasDragged
  const startEl = dragStartTarget as HTMLElement | null
  const fromCloseBtn = !!(startEl?.classList?.contains('close-btn') || startEl?.closest?.('.close-btn'))
  dragStartTarget = null

  // 仅在「未发生有效拖动」时视为点击看板娘；拖动过程不触发台词（与冷却无关）
  requestAnimationFrame(() => {
    if (!dragged && !fromCloseBtn) {
      handlePoke()
    }
    hasDragged = false
  })
}

declare global {
  interface Window {
    PIXI: any
  }
}

onMounted(async () => {
  await nextTick()

  let checkCount = 0
  const initLive2D = async () => {
    if (window.PIXI && window.PIXI.live2d) {
      setupLive2D()
    } else {
      checkCount++
      if (checkCount < 20) {
        setTimeout(initLive2D, 500)
      } else {
        console.error('Live2D scripts not loaded.')
      }
    }
  }

  initLive2D()

  // 首句欢迎：不计入冷却；下一句点击从 quotes[1] 起顺序循环，避免与首句重复
  setTimeout(() => {
    showQuoteLine(quotes[0])
    quoteOrderIndex = 1
  }, 1000)
})

async function setupLive2D() {
  if (!live2dCanvas.value) return

  const parentRect = live2dCanvas.value.parentElement?.getBoundingClientRect()
  const canvasWidth = parentRect?.width || 250
  const canvasHeight = parentRect?.height || 250

  app = new window.PIXI.Application({
    view: live2dCanvas.value,
    transparent: true,
    autoStart: true,
    backgroundAlpha: 0,
    width: canvasWidth,
    height: canvasHeight,
    resolution: window.devicePixelRatio || 1,
    autoDensity: true,
  })

  try {
    model = await window.PIXI.live2d.Live2DModel.from('/14酱/我也要死吗.model3.json')
    app.stage.addChild(model)

    app.ticker.add(() => {
      if (model?.internalModel?.coreModel) {
        model.internalModel.coreModel.setParameterValueById('CheekPuff2', 1)
      }
    })

    resizeModel()

    // 台词仅在 stopDrag 中根据是否拖动判定，避免 pointerdown 与拖拽冲突

    setTimeout(resizeModel, 500)
  } catch (err) {
    console.error('Live2D Load Error:', err)
  }
}

function resizeModel() {
  if (!model || !app) return

  model.scale.set(1)

  const containerWidth = live2dCanvas.value?.parentElement?.clientWidth || 250
  const containerHeight = live2dCanvas.value?.parentElement?.clientHeight || 250

  const scaleX = containerWidth / (model.width || containerWidth)
  const scaleY = containerHeight / (model.height || containerHeight)
  const scale = Math.min(scaleX, scaleY) * 1.2

  model.scale.set(scale)

  if (model.anchor) {
    model.anchor.set(0.5, 0.5)
    model.x = containerWidth / 2
    model.y = containerHeight / 2 + 10
  } else {
    model.x = (containerWidth - model.width * scale) / 2
    model.y = containerHeight - model.height * scale + 150
  }
}

onBeforeUnmount(() => {
  if (hideTimer) clearTimeout(hideTimer)
  if (speakCooldownTimer) clearTimeout(speakCooldownTimer)
  if (app) {
    app.destroy(false, { children: true })
  }
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
})
</script>

<template>
  <div
    class="mascot-container"
    :class="{ 'is-minimized': isMinimized }"
    :style="{ left: position.x + 'px', bottom: position.y + 'px' }"
  >
    <!-- 气泡固定在看板娘上方；mode=out-in + key 保证换句时淡入淡出 -->
    <transition name="bubble-fade" mode="out-in">
      <div
        v-if="showBubble && !isMinimized"
        :key="currentQuote"
        class="speech-bubble glass-ui"
      >
        {{ currentQuote }}
      </div>
    </transition>

    <div
      class="mascot-avatar"
      :class="{ 'is-press-feedback': showPressFeedback }"
      @mousedown.prevent="startDrag"
      @touchstart.prevent="startDrag"
    >
      <div class="close-btn" @click.stop="toggleMinimize" @mousedown.stop @touchstart.stop>
        {{ isMinimized ? '➕' : '➖' }}
      </div>
      <canvas ref="live2dCanvas" class="live2d-canvas"></canvas>
    </div>
  </div>
</template>

<style scoped>
.mascot-container {
  position: fixed;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.mascot-container.is-minimized {
  transform: scale(0.7);
  opacity: 0.7;
}

/* 气泡居容器上方、宽度稳定，避免拖拽位移后「乱跑」的观感（相对看板娘锚点不变） */
.speech-bubble {
  order: -1;
  padding: 12px 18px;
  border-radius: 16px;
  border-bottom-left-radius: 4px;
  max-width: 200px;
  font-size: 0.9rem;
  font-weight: 600;
  line-height: 1.4;
  transform-origin: bottom center;
  text-align: center;
}

.glass-ui {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.mascot-avatar {
  width: 250px;
  height: 250px;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  position: relative;
  transition: transform 0.2s ease-out;
}

.mascot-avatar:active {
  cursor: grabbing;
}

.mascot-avatar:hover:not(.is-press-feedback) {
  transform: translateY(-5px);
}

/* 点击手感：轻微缩小后回弹（与 hover 上浮二选一，避免 transform 冲突） */
.mascot-avatar.is-press-feedback {
  animation: mascot-press-pop 0.22s ease-out;
}

@keyframes mascot-press-pop {
  0% {
    transform: translateY(0) scale(1);
  }
  45% {
    transform: translateY(0) scale(0.95);
  }
  100% {
    transform: translateY(0) scale(1);
  }
}

.live2d-canvas {
  width: 100%;
  height: 100%;
  pointer-events: auto;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(5px);
  color: #fff;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  z-index: 10;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.mascot-avatar:hover .close-btn,
.mascot-container.is-minimized .close-btn {
  opacity: 1;
}

.bubble-fade-enter-active,
.bubble-fade-leave-active {
  transition:
    opacity 0.28s ease,
    transform 0.28s ease;
}

.bubble-fade-enter-from,
.bubble-fade-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.92);
}

@media (max-width: 768px) {
  .mascot-avatar {
    width: 120px;
    height: 150px;
  }

  .speech-bubble {
    max-width: 150px;
    font-size: 0.8rem;
  }

  .close-btn {
    opacity: 1;
    width: 24px;
    height: 24px;
    font-size: 0.8rem;
    top: 0;
    right: 0;
  }
}
</style>
