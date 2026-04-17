<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const LS_KEY = 'weihan_tool_cps_best_by_mode'

const MODES = [1, 5, 10, 30] as const

const cpsMode = ref<number>(5)
const cpsStatus = ref<'idle' | 'running' | 'result'>('idle')
const cpsTimeLeft = ref(5)
const cpsClicks = ref(0)
const cpsAvg = ref(0)
const cpsPeak = ref(0)
const bestByMode = ref<Record<string, number>>({})

const clickTimes = ref<number[]>([])
let cpsTimer: ReturnType<typeof setInterval> | null = null
let runStartedAt = 0

function loadBest() {
  try {
    const raw = localStorage.getItem(LS_KEY)
    if (raw) bestByMode.value = JSON.parse(raw) as Record<string, number>
  } catch {
    bestByMode.value = {}
  }
}

function saveBest() {
  try {
    localStorage.setItem(LS_KEY, JSON.stringify(bestByMode.value))
  } catch {
    /* ignore */
  }
}

onMounted(loadBest)

onUnmounted(() => {
  if (cpsTimer) clearInterval(cpsTimer)
})

const currentBest = computed(() => bestByMode.value[String(cpsMode.value)] ?? 0)

function resetCps() {
  cpsStatus.value = 'idle'
  cpsTimeLeft.value = cpsMode.value
  cpsClicks.value = 0
  clickTimes.value = []
  if (cpsTimer) clearInterval(cpsTimer)
}

function setCpsMode(m: number) {
  if (cpsStatus.value === 'running') return
  cpsMode.value = m
  resetCps()
}

function peakFromClicks(times: number[], durationSec: number): number {
  if (times.length === 0 || durationSec <= 0) return 0
  const start = times[0]
  const end = start + durationSec * 1000
  let peak = 0
  for (let wStart = start; wStart < end; wStart += 1000) {
    const wEnd = wStart + 1000
    const n = times.filter((t) => t >= wStart && t < wEnd).length
    if (n > peak) peak = n
  }
  return peak
}

function handleCpsClick() {
  if (cpsStatus.value === 'idle') {
    cpsStatus.value = 'running'
    runStartedAt = performance.now()
    cpsClicks.value = 1
    clickTimes.value = [runStartedAt]
    cpsTimeLeft.value = cpsMode.value
    cpsTimer = setInterval(() => {
      cpsTimeLeft.value--
      if (cpsTimeLeft.value <= 0) endCps()
    }, 1000)
  } else if (cpsStatus.value === 'running') {
    cpsClicks.value++
    clickTimes.value.push(performance.now())
  } else if (cpsStatus.value === 'result') {
    resetCps()
  }
}

function endCps() {
  if (cpsTimer) clearInterval(cpsTimer)
  cpsStatus.value = 'result'
  const dur = cpsMode.value
  cpsAvg.value = +(cpsClicks.value / dur).toFixed(2)
  const elapsedMs = performance.now() - runStartedAt
  const effSec = Math.min(dur, Math.max(0.001, elapsedMs / 1000))
  cpsPeak.value = peakFromClicks(clickTimes.value, effSec)
  const key = String(cpsMode.value)
  const prev = bestByMode.value[key] ?? 0
  if (cpsAvg.value > prev) {
    bestByMode.value = { ...bestByMode.value, [key]: cpsAvg.value }
    saveBest()
  }
}

const cpsEvaluation = computed(() => {
  if (cpsStatus.value !== 'result') return ''
  if (cpsAvg.value >= 12) return '⚡ 键盘破坏者！'
  if (cpsAvg.value >= 8) return '🔥 手速惊人！'
  if (cpsAvg.value >= 5) return '✨ 稳扎稳打！'
  return '🍃 慢工出细活~'
})
</script>

<template>
  <div class="tool-inner">
    <h2 class="tool-title">🖱️ {{ t('tools.cps') }}</h2>
    <div class="cps-modes">
      <button
        v-for="m in MODES"
        :key="m"
        type="button"
        class="cps-mode-btn"
        :class="{ active: cpsMode === m }"
        :disabled="cpsStatus === 'running'"
        @click="setCpsMode(m)"
      >
        {{ m }}{{ t('tools.cpsSec') }}
      </button>
    </div>
    <div class="cps-stats">
      <div class="stat">
        <span class="stat-label">{{ t('tools.cpsTimeLeft') }}</span>
        <span class="stat-val">{{ cpsTimeLeft }}s</span>
      </div>
      <div class="stat">
        <span class="stat-label">{{ t('tools.cpsClicks') }}</span>
        <span class="stat-val">{{ cpsClicks }}</span>
      </div>
      <div class="stat">
        <span class="stat-label">{{ t('tools.cpsBestMode') }}</span>
        <span class="stat-val">{{ currentBest.toFixed(2) }}</span>
      </div>
    </div>
    <div class="cps-area" @click="handleCpsClick">
      <div v-if="cpsStatus === 'idle'" class="cps-msg">{{ t('tools.clickNow') }}</div>
      <div v-else-if="cpsStatus === 'running'" class="cps-msg cps-count">{{ cpsClicks }}</div>
      <div v-else class="cps-msg result-stack">
        <div>{{ t('tools.cpsAvg', { v: cpsAvg.toFixed(2) }) }}</div>
        <div>{{ t('tools.cpsPeak', { v: cpsPeak }) }}</div>
        <div class="eval-text">{{ cpsEvaluation }}</div>
        <div class="cps-retry">{{ t('tools.cpsRetry') }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tool-inner {
  min-height: 120px;
}

.tool-title {
  font-size: 1.35rem;
  font-weight: 800;
  margin: 0 0 1.25rem;
  text-align: center;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

:root[data-theme='dark'] .tool-title {
  background: linear-gradient(to right, #a18cd1, #66d9ff);
  -webkit-background-clip: text;
}

.cps-modes {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-bottom: 1rem;
}

.cps-mode-btn {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  background: rgba(74, 144, 226, 0.15);
  border: 1px solid transparent;
  cursor: pointer;
  font: inherit;
  color: inherit;
  transition: all 0.2s;
}

.cps-mode-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

:root[data-theme='dark'] .cps-mode-btn {
  background: rgba(255, 255, 255, 0.08);
}

.cps-mode-btn.active {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.25);
}

.cps-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 1rem;
  background: rgba(74, 144, 226, 0.08);
  border-radius: 12px;
  padding: 10px;
  gap: 8px;
  flex-wrap: wrap;
}

:root[data-theme='dark'] .cps-stats {
  background: rgba(0, 0, 0, 0.25);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 72px;
}

.stat-label {
  font-size: 0.72rem;
  opacity: 0.75;
  text-align: center;
}

.stat-val {
  font-size: 1.2rem;
  font-weight: 800;
  color: #4a90e2;
}

:root[data-theme='dark'] .stat-val {
  color: #66d9ff;
}

.cps-area {
  width: 100%;
  min-height: 180px;
  background: rgba(74, 144, 226, 0.06);
  border: 2px dashed rgba(252, 162, 229, 0.45);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  touch-action: manipulation;
}

.cps-area:active {
  transform: scale(0.98);
}

.cps-msg {
  font-size: 1.15rem;
  font-weight: 700;
  text-align: center;
  pointer-events: none;
  padding: 12px;
}

.result-stack {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cps-count {
  font-size: 2.75rem;
  color: #c76bb8;
}

:root[data-theme='dark'] .cps-count {
  color: #fca2e5;
}

.eval-text {
  font-size: 1rem;
  font-weight: 800;
  color: #c76bb8;
}

:root[data-theme='dark'] .eval-text {
  color: #fca2e5;
}

.cps-retry {
  font-size: 0.8rem;
  margin-top: 6px;
  opacity: 0.85;
}
</style>
