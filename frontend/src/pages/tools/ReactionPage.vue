<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import GlassCard from '../../components/GlassCard.vue'

type Phase = 'waiting' | 'ready' | 'clicked' | 'toofast'
const phase = ref<Phase>('waiting')
const result = ref<number | null>(null)
let timeout: ReturnType<typeof setTimeout> | null = null
let startTime = 0

function start() {
  phase.value = 'waiting'
  result.value = null
  const delay = 1000 + Math.random() * 3000
  timeout = setTimeout(() => {
    phase.value = 'ready'
    startTime = Date.now()
  }, delay)
}

function click() {
  if (phase.value === 'waiting') {
    phase.value = 'toofast'
    if (timeout) clearTimeout(timeout)
    setTimeout(() => start(), 1500)
  } else if (phase.value === 'ready') {
    result.value = Date.now() - startTime
    phase.value = 'clicked'
  }
}

function reset() {
  if (timeout) clearTimeout(timeout)
  start()
}

onUnmounted(() => { if (timeout) clearTimeout(timeout) })
start()
</script>

<template>
  <div class="page-container max-w-xl text-center">
    <button @click="$router.push('/tools')" class="glass-button text-sm mb-8">← 返回工具箱</button>
    <h1 class="text-3xl font-bold mb-6 gradient-text">⏱️ 反应力测试</h1>

    <GlassCard>
      <p class="text-[var(--text-secondary)] mb-6 text-sm">看到屏幕变绿时立即点击！</p>

      <div
        @click="click"
        class="w-48 h-48 mx-auto rounded-full flex items-center justify-center cursor-pointer transition-all duration-150 border-2"
        :class="{
          'bg-red-500/20 border-red-500/30': phase === 'waiting',
          'bg-green-500/20 border-green-500/50 scale-110': phase === 'ready',
          'bg-green-500/30 border-green-500/60': phase === 'clicked',
          'bg-yellow-500/20 border-yellow-500/30': phase === 'toofast'
        }"
      >
        <div class="text-center">
          <div v-if="phase === 'waiting'" class="text-4xl">🔴</div>
          <div v-else-if="phase === 'ready'" class="text-4xl">🟢</div>
          <div v-else-if="phase === 'clicked'" class="text-4xl">✅</div>
          <div v-else class="text-4xl">⚠️</div>
          <p class="text-xs mt-2 text-[var(--text-secondary)]">
            {{ phase === 'waiting' ? '等待变绿...' : phase === 'ready' ? '快点击！' : phase === 'clicked' ? '' : '太早了！' }}
          </p>
        </div>
      </div>

      <div v-if="result !== null" class="mt-6">
        <p class="text-2xl font-bold gradient-text">{{ result }} ms</p>
      </div>

      <button @click="reset" class="glass-button text-sm mt-4">再来一次</button>
    </GlassCard>
  </div>
</template>
