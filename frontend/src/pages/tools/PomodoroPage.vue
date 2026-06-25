<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import GlassCard from '../../components/GlassCard.vue'

const minutes = ref(25)
const seconds = ref(0)
const running = ref(false)
const isBreak = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

const totalSeconds = () => minutes.value * 60 + seconds.value

function start() {
  if (totalSeconds() <= 0) return
  running.value = true
  timer = setInterval(() => {
    seconds.value--
    if (seconds.value < 0) {
      minutes.value--
      seconds.value = 59
    }
    if (totalSeconds() <= 0) {
      stop()
      isBreak.value = !isBreak.value
      if (isBreak.value) { minutes.value = 5; seconds.value = 0 }
      else { minutes.value = 25; seconds.value = 0 }
    }
  }, 1000)
}

function stop() {
  running.value = false
  if (timer) { clearInterval(timer); timer = null }
}

function reset() {
  stop()
  isBreak.value = false
  minutes.value = 25
  seconds.value = 0
}

onUnmounted(() => stop())

function fmt(n: number) { return n.toString().padStart(2, '0') }
</script>

<template>
  <div class="page-container max-w-xl text-center">
    <button @click="$router.push('/tools')" class="glass-button text-sm mb-8">← 返回工具箱</button>
    <h1 class="text-3xl font-bold mb-6 gradient-text">🍅 番茄钟</h1>

    <GlassCard>
      <p class="text-[var(--text-secondary)] mb-4 text-sm">
        {{ isBreak ? '☕ 休息时间' : '🍅 专注时间' }}
      </p>

      <div class="text-6xl font-mono font-bold mb-6" :class="running ? 'gradient-text' : 'text-[var(--text-primary)]'">
        {{ fmt(minutes) }}:{{ fmt(seconds) }}
      </div>

      <div class="flex justify-center gap-3 mb-4">
        <button @click="start" :disabled="running" class="glass-button primary text-sm" :class="{ 'opacity-50': running }">
          ▶ 开始
        </button>
        <button @click="stop" :disabled="!running" class="glass-button text-sm" :class="{ 'opacity-50': !running }">
          ⏸ 暂停
        </button>
        <button @click="reset" class="glass-button text-sm">↺ 重置</button>
      </div>

      <div class="flex justify-center gap-2">
        <button v-for="m in [15, 25, 45]" :key="m" @click="stop(); minutes = m; seconds = 0; isBreak = false"
          class="glass-button !px-3 !py-1 text-xs">
          {{ m }}分钟
        </button>
      </div>
    </GlassCard>
  </div>
</template>
