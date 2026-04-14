<script setup lang="ts">
import { ref, onMounted } from 'vue'

const quotes = [
  '今天也要加油哦！(≧◡≦)',
  'Bug 什么的，退散退散！(ノ*゜▽゜*)',
  '要不要休息一下，喝杯茶？☕',
  '代码虽然冰冷，但创造出的世界很温暖呢~✨',
  '初音未来的歌，总是能治愈人心呢🎵',
  '我一直在这里陪着你哦！(๑•̀ㅂ•́)و✧'
]

const currentQuote = ref(quotes[0])
const isMinimized = ref(false)
const showBubble = ref(true)
let hideTimer: ReturnType<typeof setTimeout> | null = null

function poke() {
  if (isMinimized.value) {
    isMinimized.value = false
    showBubble.value = true
    resetHideTimer()
  } else {
    changeQuote()
  }
}

function changeQuote() {
  const next = quotes[Math.floor(Math.random() * quotes.length)]
  currentQuote.value = next
  showBubble.value = true
  resetHideTimer()
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

onMounted(() => {
  setTimeout(() => {
    changeQuote()
  }, 1000)
})
</script>

<template>
  <div class="mascot-container" :class="{ 'is-minimized': isMinimized }">
    <transition name="bubble-fade">
      <div v-if="showBubble && !isMinimized" class="speech-bubble glass-ui">
        {{ currentQuote }}
      </div>
    </transition>
    
    <div class="mascot-avatar" @click="poke">
      <div class="close-btn" @click="toggleMinimize">
        {{ isMinimized ? '➕' : '➖' }}
      </div>
      <div class="face">{{ isMinimized ? '(-_-)' : '(≧◡≦)' }}</div>
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
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.6), rgba(252, 162, 229, 0.6));
  backdrop-filter: blur(8px);
  border: 2px solid rgba(255,255,255,0.6);
  box-shadow: 0 10px 20px rgba(0,0,0,0.2), inset 0 0 15px rgba(255,255,255,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}
.mascot-avatar:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 25px rgba(252, 162, 229, 0.4), inset 0 0 20px rgba(255,255,255,0.6);
}
.face {
  font-size: 1.4rem;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.close-btn {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0,0,0,0.4);
  color: #fff;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}
.mascot-avatar:hover .close-btn {
  opacity: 1;
}

.bubble-fade-enter-active, .bubble-fade-leave-active {
  transition: all 0.3s ease;
}
.bubble-fade-enter-from, .bubble-fade-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(10px);
}

@media (max-width: 768px) {
  .mascot-avatar {
    width: 60px;
    height: 60px;
  }
  .face {
    font-size: 1rem;
  }
  .speech-bubble {
    max-width: 150px;
    font-size: 0.8rem;
  }
}
</style>