<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()

type Phase = 'idle' | 'waiting' | 'ready' | 'result' | 'tooSoon'
const phase = ref<Phase>('idle')
const reactionTime = ref(0)
const trials = ref<number[]>([])
const trialCount = ref(5)
const bestTime = ref<number | null>(null)
const currentTrial = ref(0)
let startTime = 0
let waitTimer: ReturnType<typeof setTimeout> | null = null

const avgTime = computed(() => {
  if (trials.value.length === 0) return 0
  const sum = trials.value.reduce((a, b) => a + b, 0)
  return Math.round(sum / trials.value.length)
})

const lastThreeAvg = computed(() => {
  const last = trials.value.slice(-3)
  if (last.length === 0) return 0
  return Math.round(last.reduce((a, b) => a + b, 0) / last.length)
})

function getRating(ms: number) {
  if (ms < 180) return { label: '⚡ 电竞级', color: 'text-purple-400', desc: '超越 99% 玩家' }
  if (ms < 220) return { label: '🔥 神级', color: 'text-red-400', desc: '超越 95% 玩家' }
  if (ms < 280) return { label: '✨ 优秀', color: 'text-orange-400', desc: '超越 80% 玩家' }
  if (ms < 350) return { label: '👍 良好', color: 'text-yellow-400', desc: '高于平均' }
  if (ms < 450) return { label: '😐 一般', color: 'text-blue-400', desc: '正常水平' }
  return { label: '🐌 偏慢', color: 'text-gray-400', desc: '再来一次？' }
}

function startTrial() {
  if (currentTrial.value >= trialCount.value) {
    // Reset for next round
    trials.value = []
    currentTrial.value = 0
  }
  phase.value = 'waiting'
  // Random wait 1500-4000ms
  const wait = 1500 + Math.random() * 2500
  waitTimer = setTimeout(() => {
    phase.value = 'ready'
    startTime = performance.now()
  }, wait)
}

function handleClick() {
  if (phase.value === 'idle' || phase.value === 'result' || phase.value === 'tooSoon') {
    return
  }
  if (phase.value === 'waiting') {
    // Clicked too soon
    if (waitTimer) clearTimeout(waitTimer)
    phase.value = 'tooSoon'
    return
  }
  if (phase.value === 'ready') {
    const ms = Math.round(performance.now() - startTime)
    reactionTime.value = ms
    trials.value.push(ms)
    currentTrial.value++
    if (ms < (bestTime.value ?? Infinity)) {
      bestTime.value = ms
      try { localStorage.setItem('reaction-best', String(ms)) } catch {}
    }
    phase.value = 'result'
  }
}

function nextTrial() {
  if (currentTrial.value >= trialCount.value) {
    phase.value = 'idle'
    return
  }
  startTrial()
}

const phaseInfo = computed(() => {
  switch (phase.value) {
    case 'idle': return { bg: 'from-[#62a7ea] to-[#a58eea]', text: '🎯 点击开始' }
    case 'waiting': return { bg: 'from-red-500 to-rose-600', text: '🔴 等待变绿...' }
    case 'ready': return { bg: 'from-green-400 to-emerald-500', text: '👆 立即点击！' }
    case 'result': return { bg: 'from-yellow-400 to-orange-500', text: `${reactionTime.value} ms` }
    case 'tooSoon': return { bg: 'from-gray-500 to-gray-700', text: '⚠️ 太早了！重来' }
  }
})

onMounted(() => {
  try {
    const saved = localStorage.getItem('reaction-best')
    if (saved) bestTime.value = parseInt(saved)
  } catch {}
})

onUnmounted(() => {
  if (waitTimer) clearTimeout(waitTimer)
})
</script>

<template>
  <div class="page-container max-w-lg mx-auto">
    <button @click="router.push('/')" class="glass-button text-sm mb-6">← 返回首页</button>
    <GlassCard class="heavy !p-6">
      <h1 class="text-2xl font-bold mb-1 gradient-text text-center">🎯 反应力测试</h1>
      <p class="text-sm text-[var(--text-muted)] mb-4 text-center">
        屏幕变绿时立即点击 | 共 {{ trialCount }} 轮
      </p>

      <!-- Click area -->
      <button
        @click="phase === 'idle' || phase === 'result' || phase === 'tooSoon' ? (phase === 'tooSoon' ? startTrial() : startTrial()) : handleClick()"
        class="w-full rounded-2xl mb-4 transition-all duration-200 select-none cursor-pointer flex flex-col items-center justify-center text-white font-bold shadow-lg active:scale-[0.99] bg-gradient-to-br"
        :class="phaseInfo.bg"
        style="height: 280px"
      >
        <div class="text-3xl md:text-5xl mb-2">{{ phaseInfo.text }}</div>
        <div v-if="phase === 'result'" class="text-sm opacity-90">
          {{ getRating(reactionTime).label }} - {{ getRating(reactionTime).desc }}
        </div>
        <div v-else-if="phase === 'idle'" class="text-sm opacity-90 mt-1 px-4">
          看到绿色立即点击，越快越好
        </div>
        <div v-else-if="phase === 'waiting'" class="text-sm opacity-90 mt-1">
          不要点击！等变绿
        </div>
        <div v-else-if="phase === 'tooSoon'" class="text-sm opacity-90 mt-1">
          看到绿色再点，再来一次
        </div>
      </button>

      <!-- Trial stats -->
      <div v-if="trials.length > 0" class="grid grid-cols-3 gap-2 mb-4 text-center">
        <div class="glass-card !p-2">
          <div class="text-xs text-[var(--text-muted)]">本轮次数</div>
          <div class="text-lg font-bold gradient-text">{{ currentTrial }} / {{ trialCount }}</div>
        </div>
        <div class="glass-card !p-2">
          <div class="text-xs text-[var(--text-muted)]">平均</div>
          <div class="text-lg font-bold gradient-text">{{ avgTime }} ms</div>
        </div>
        <div class="glass-card !p-2">
          <div class="text-xs text-[var(--text-muted)]">最佳</div>
          <div class="text-lg font-bold text-green-400">{{ bestTime ?? '-' }} ms</div>
        </div>
      </div>

      <!-- Trial history -->
      <div v-if="trials.length > 0" class="mb-4">
        <div class="text-xs text-[var(--text-muted)] mb-2">📊 历次成绩</div>
        <div class="flex gap-1.5 flex-wrap">
          <div v-for="(t, i) in trials" :key="i"
            class="px-2.5 py-1 rounded text-sm font-mono font-bold"
            :class="t < 250 ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30' :
                    t < 350 ? 'bg-green-500/20 text-green-300 border border-green-500/30' :
                    t < 450 ? 'bg-yellow-500/20 text-yellow-300 border border-yellow-500/30' :
                    'bg-gray-500/20 text-gray-300 border border-gray-500/30'">
            {{ t }}ms
          </div>
        </div>
        <div v-if="trials.length >= 3" class="text-xs text-[var(--text-muted)] mt-2">
          最近 3 次平均：<span class="text-white font-bold">{{ lastThreeAvg }} ms</span>
        </div>
      </div>

      <!-- Action buttons -->
      <div v-if="phase === 'result' || phase === 'tooSoon'" class="flex gap-2">
        <button v-if="phase === 'tooSoon'" @click="startTrial"
          class="glass-button primary flex-1 text-sm">
          🔄 重新开始本轮
        </button>
        <button v-else-if="currentTrial < trialCount" @click="nextTrial"
          class="glass-button primary flex-1 text-sm">
          ▶️ 下一轮 ({{ currentTrial }}/{{ trialCount }})
        </button>
        <button v-else @click="startTrial"
          class="glass-button primary flex-1 text-sm">
          🎉 完成！再来一组
        </button>
      </div>
      <div v-else-if="phase === 'idle'" class="text-center">
        <button @click="startTrial" class="glass-button primary text-base px-8">
          🚀 开始测试
        </button>
      </div>
    </GlassCard>
  </div>
</template>
