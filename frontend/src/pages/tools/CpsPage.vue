<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import GlassCard from '../../components/GlassCard.vue'

const clicks = ref(0)
const timeLeft = ref(10)
const running = ref(false)
let timer: ReturnType<typeof setInterval> | null = null
let countdown: ReturnType<typeof setInterval> | null = null

function start() {
  clicks.value = 0
  timeLeft.value = 10
  running.value = true
  countdown = setInterval(() => {
    timeLeft.value--
    if (timeLeft.value <= 0) stop()
  }, 1000)
}

function stop() {
  running.value = false
  if (countdown) clearInterval(countdown)
}

function click() {
  if (running.value) clicks.value++
}

function reset() {
  stop()
  clicks.value = 0
  timeLeft.value = 10
}

onUnmounted(() => { stop() })
</script>

<template>
  <div class="page-container max-w-xl text-center">
    <button @click="$router.push('/tools')" class="glass-button text-sm mb-8">← 返回工具箱</button>
    <h1 class="text-3xl font-bold mb-6 gradient-text">🖱️ CPS 测试</h1>

    <GlassCard>
      <p class="text-[var(--text-secondary)] mb-4 text-sm">10 秒内尽可能多地点击！</p>

      <div
        @click="click"
        :class="{ 'pointer-events-none opacity-50': !running }"
        class="w-40 h-40 mx-auto rounded-full flex items-center justify-center transition-all duration-100 border-2 border-[#62a7ea33] bg-[#62a7ea15] cursor-pointer hover:scale-105 select-none"
      >
        <div class="text-center">
          <div class="text-4xl font-bold gradient-text">{{ clicks }}</div>
          <div class="text-xs text-[var(--text-secondary)]">点击次数</div>
        </div>
      </div>

      <div class="mt-4 text-3xl font-bold" :class="timeLeft <= 3 ? 'text-red-400' : 'text-[var(--text-primary)]'">
        {{ timeLeft }}s
      </div>

      <div class="mt-4">
        <button v-if="!running" @click="start" class="glass-button primary text-sm">开始测试</button>
        <button v-else @click="reset" class="glass-button text-sm">重置</button>
      </div>

      <p v-if="!running && clicks > 0" class="mt-4 text-lg">
        你的 CPS：<span class="font-bold gradient-text">{{ (clicks / 10).toFixed(1) }}</span>
      </p>
    </GlassCard>
  </div>
</template>
