<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

const quotes = [
  '你好呀～欢迎来到我的网站',
  '今天也要加油哦',
  '要不要听我唱歌呢',
  '摸摸头～',
  '别一直点我啦，痒~'
]

const currentQuote = ref(quotes[0])
const isMinimized = ref(false)
const showBubble = ref(true)
const isBouncing = ref(false)
let hideTimer: ReturnType<typeof setTimeout> | null = null

const live2dCanvas = ref<HTMLCanvasElement | null>(null)
let app: any = null
let model: any = null

// Draggable State
const position = ref({ x: 20, y: 20 }) // Distance from left and bottom
let isDragging = false
let hasDragged = false
let startMouse = { x: 0, y: 0 }
let startPos = { x: 0, y: 0 }

function changeQuote(quoteStr?: string) {
  const next = quoteStr || quotes[Math.floor(Math.random() * quotes.length)]
  currentQuote.value = next
  showBubble.value = true
  resetHideTimer()
}

function handlePoke() {
  if (hasDragged) {
    hasDragged = false
    return
  }
  
  if (isMinimized.value) {
    isMinimized.value = false
    showBubble.value = true
    resetHideTimer()
  } else {
    changeQuote()
    isBouncing.value = true
    setTimeout(() => {
      isBouncing.value = false
    }, 300)
    
    if (model && model.internalModel && model.internalModel.motionManager) {
      try {
        model.internalModel.motionManager.expressionManager?.setRandomExpression()
      } catch (e) {}
    }
  }
}

function onMouseEnter() {
  if (isDragging || isMinimized.value || showBubble.value) return
  changeQuote('你好呀～欢迎来到我的网站')
}

function resetHideTimer() {
  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    showBubble.value = false
  }, 5000)
}

function toggleMinimize(e: Event) {
  e.stopPropagation()
  isMinimized.value = !isMinimized.value
  showBubble.value = false
}

// --- Drag Logic ---
function startDrag(e: MouseEvent | TouchEvent) {
  if ((e.target as HTMLElement).classList.contains('close-btn')) return
  
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
    // Update position. x is from left, y is from bottom (so y goes opposite to dy)
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
}
// ------------------

// Ensure PIXI is available globally
declare global {
  interface Window {
    PIXI: any;
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

  setTimeout(() => {
    changeQuote()
  }, 1000)
})

async function setupLive2D() {
  if (!live2dCanvas.value) return

  app = new window.PIXI.Application({
    view: live2dCanvas.value,
    transparent: true,
    autoStart: true,
    backgroundAlpha: 0,
    width: 250,
    height: 300,
    resolution: window.devicePixelRatio || 1,
    autoDensity: true
  })

  try {
    model = await window.PIXI.live2d.Live2DModel.from('/miku_qq/初音未来qq.model3.json')
    app.stage.addChild(model)

    resizeModel()
    
    // Internal hit area click
    model.on('pointerdown', () => {
      if (!isDragging) handlePoke()
    })
    
    // Give it a moment to fully load textures and internal dimensions
    setTimeout(resizeModel, 500)

  } catch (err) {
    console.error('Live2D Load Error:', err)
  }
}

function resizeModel() {
  if (!model || !app) return
  
  // Reset scale to correctly calculate base size
  model.scale.set(1)
  
  const containerWidth = 250
  const containerHeight = 300
  
  const scaleX = containerWidth / model.width
  const scaleY = containerHeight / model.height
  const scale = Math.min(scaleX, scaleY) * 0.95 // 95% to leave some padding
  
  model.scale.set(scale)
  
  // Center horizontally and vertically
  model.x = (containerWidth - model.width) / 2
  model.y = (containerHeight - model.height) / 2 + 10 // Shift down slightly
}

onBeforeUnmount(() => {
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
    <transition name="bubble-fade">
      <div v-if="showBubble && !isMinimized" class="speech-bubble glass-ui">
        {{ currentQuote }}
      </div>
    </transition>
    
    <div 
      class="mascot-avatar" 
      :class="{ 'is-bouncing': isBouncing }" 
      @mouseenter="onMouseEnter"
      @click="handlePoke"
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
  align-items: center; /* Align center relative to the container */
  gap: 15px;
  /* Remove transition to allow smooth dragging */
}
.mascot-container.is-minimized {
  transform: scale(0.7);
  opacity: 0.7;
}

.speech-bubble {
  padding: 12px 18px;
  border-radius: 16px;
  border-bottom-left-radius: 4px; /* Change to bottom-left since it's on the left side */
  max-width: 200px;
  font-size: 0.9rem;
  font-weight: 600;
  line-height: 1.4;
  transform-origin: bottom left;
  text-align: center;
}

.glass-ui {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
  color: #fff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

.mascot-avatar {
  width: 250px;
  height: 300px;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  position: relative;
  transition: transform 0.3s ease-out;
}
.mascot-avatar:active {
  cursor: grabbing;
}

.mascot-avatar:hover {
  transform: translateY(-5px);
}

.live2d-canvas {
  width: 100%;
  height: 100%;
  /* Pass events so dragging on canvas works */
  pointer-events: auto;
}

/* Bounce Animation for Clicks */
@keyframes mascot-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}
.mascot-avatar.is-bouncing {
  animation: mascot-bounce 0.3s ease-out;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(0,0,0,0.3);
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

.mascot-avatar:hover .close-btn, .mascot-container.is-minimized .close-btn {
  opacity: 1;
}

.bubble-fade-enter-active, .bubble-fade-leave-active {
  transition: all 0.3s ease;
}
.bubble-fade-enter-from, .bubble-fade-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(10px);
}

/* Mobile Adjustments */
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