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

function changeQuote(quoteStr?: string) {
  const next = quoteStr || quotes[Math.floor(Math.random() * quotes.length)]
  currentQuote.value = next
  showBubble.value = true
  resetHideTimer()
}

function poke() {
  if (isMinimized.value) {
    isMinimized.value = false
    showBubble.value = true
    resetHideTimer()
  } else {
    // Random quote
    changeQuote()
    // Play bounce animation
    isBouncing.value = true
    setTimeout(() => {
      isBouncing.value = false
    }, 300)
    
    // Attempt to trigger random expression if supported
    if (model && model.internalModel && model.internalModel.motionManager) {
      try {
        model.internalModel.motionManager.expressionManager?.setRandomExpression()
      } catch (e) {}
    }
  }
}

function onMouseEnter() {
  if (isMinimized.value || showBubble.value) return
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

// Ensure PIXI is available globally
declare global {
  interface Window {
    PIXI: any;
  }
}

onMounted(async () => {
  await nextTick()
  
  // Wait for PIXI and Live2D scripts to be ready
  let checkCount = 0
  const initLive2D = async () => {
    if (window.PIXI && window.PIXI.live2d) {
      setupLive2D()
    } else {
      checkCount++
      if (checkCount < 20) { // Wait up to 10 seconds
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

  // Create PIXI Application
  app = new window.PIXI.Application({
    view: live2dCanvas.value,
    transparent: true,
    autoStart: true,
    backgroundAlpha: 0,
    resizeTo: live2dCanvas.value.parentElement
  })

  try {
    // Load the model from the public folder
    model = await window.PIXI.live2d.Live2DModel.from('/mikumiku/mikumiku.model3.json')
    app.stage.addChild(model)

    // Setup initial scale and position
    resizeModel()

    // Handle window resize
    window.addEventListener('resize', resizeModel)
    
    // Enable dragging/interaction if supported by the plugin
    model.on('pointerdown', poke)

    // Wait a brief moment to ensure model dimensions are loaded before resizing
    setTimeout(resizeModel, 100)

  } catch (err) {
    console.error('Live2D Load Error:', err)
  }
}

function resizeModel() {
  if (!model || !app) return
  const containerWidth = app.renderer.width
  const containerHeight = app.renderer.height
  
  const baseWidth = model.internalModel ? model.internalModel.width : (model.width / model.scale.x)
  const baseHeight = model.internalModel ? model.internalModel.height : (model.height / model.scale.y)
  
  // Calculate scale to fit the container width/height appropriately
  const scaleX = containerWidth / baseWidth
  const scaleY = containerHeight / baseHeight
  
  // We want the model to fit inside the avatar circle mostly, so we scale it down slightly
  const scale = Math.min(scaleX, scaleY) * 1.5 
  model.scale.set(scale)
  
  // Center horizontally, align towards bottom
  model.x = (containerWidth - baseWidth * scale) / 2
  model.y = (containerHeight - baseHeight * scale) * 0.8
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeModel)
  if (app) {
    app.destroy(false, { children: true })
  }
})
</script>

<template>
  <div class="mascot-container" :class="{ 'is-minimized': isMinimized }">
    <transition name="bubble-fade">
      <div v-if="showBubble && !isMinimized" class="speech-bubble glass-ui">
        {{ currentQuote }}
      </div>
    </transition>
    
    <div class="mascot-avatar" :class="{ 'is-bouncing': isBouncing }" @click="poke" @mouseenter="onMouseEnter">
      <div class="close-btn" @click.stop="toggleMinimize">
        {{ isMinimized ? '➕' : '➖' }}
      </div>
      <canvas ref="live2dCanvas" class="live2d-canvas"></canvas>
    </div>
  </div>
</template>

<style scoped>
.mascot-container {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 15px;
  transition: all 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}
.mascot-container.is-minimized {
  bottom: 10px;
  right: 10px;
  transform: scale(0.7);
  opacity: 0.7;
}

.speech-bubble {
  padding: 12px 18px;
  border-radius: 16px;
  border-bottom-right-radius: 4px;
  max-width: 200px;
  font-size: 0.9rem;
  font-weight: 600;
  line-height: 1.4;
  transform-origin: bottom right;
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
  /* Transparent background to show the full model seamlessly */
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  transition: transform 0.3s ease-out;
}

.mascot-avatar:hover {
  transform: translateY(-5px);
}

.live2d-canvas {
  width: 100%;
  height: 100%;
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
    opacity: 1; /* Always visible on mobile */
    width: 24px;
    height: 24px;
    font-size: 0.8rem;
    top: 0;
    right: 0;
  }
}
</style>
