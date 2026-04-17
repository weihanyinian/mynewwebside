<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const LS_BEST = 'weihan_tool_reaction_best_ms'
const LS_HISTORY = 'weihan_tool_reaction_history'

type HistoryItem = { at: number; ms: number }

const reactStatus = ref<'idle' | 'waiting' | 'click' | 'early' | 'result'>('idle')
const reactTime = ref(0)
const bestMs = ref<number | null>(null)
const history = ref<HistoryItem[]>([])

let reactStart = 0
let reactTimeout: ReturnType<typeof setTimeout> | null = null

function loadStorage() {
  try {
    const b = localStorage.getItem(LS_BEST)
    if (b) bestMs.value = Math.max(0, parseInt(b, 10) || 0)
    const h = localStorage.getItem(LS_HISTORY)
    if (h) {
      const parsed = JSON.parse(h) as HistoryItem[]
      if (Array.isArray(parsed)) history.value = parsed.slice(-30)
    }
  } catch {
    /* ignore */
  }
}

function persist() {
  try {
    if (bestMs.value != null) localStorage.setItem(LS_BEST, String(bestMs.value))
    localStorage.setItem(LS_HISTORY, JSON.stringify(history.value.slice(-30)))
  } catch {
    /* ignore */
  }
}

onMounted(loadStorage)

onUnmounted(() => {
  if (reactTimeout) clearTimeout(reactTimeout)
})

function handleReactionClick() {
  if (reactStatus.value === 'idle' || reactStatus.value === 'result' || reactStatus.value === 'early') {
    reactStatus.value = 'waiting'
    const delay = Math.random() * 3000 + 2000
    reactTimeout = setTimeout(() => {
      reactStatus.value = 'click'
      reactStart = performance.now()
    }, delay)
  } else if (reactStatus.value === 'waiting') {
    if (reactTimeout) clearTimeout(reactTimeout)
    reactStatus.value = 'early'
  } else if (reactStatus.value === 'click') {
    const ms = Math.max(0, Math.round(performance.now() - reactStart))
    reactTime.value = ms
    reactStatus.value = 'result'
    history.value = [...history.value, { at: Date.now(), ms }].slice(-30)
    if (bestMs.value == null || ms < bestMs.value) bestMs.value = ms
    persist()
  }
}

const reactionMessage = computed(() => {
  switch (reactStatus.value) {
    case 'idle':
      return t('tools.ready')
    case 'waiting':
      return t('tools.waiting')
    case 'click':
      return t('tools.clickNow')
    case 'early':
      return t('tools.tooEarly')
    case 'result':
      return t('tools.reactTime', { time: reactTime.value })
    default:
      return ''
  }
})

const reactionEvaluation = computed(() => {
  if (reactStatus.value !== 'result') return ''
  if (reactTime.value < 150) return t('tools.reactRank1')
  if (reactTime.value < 220) return t('tools.reactRank2')
  if (reactTime.value < 300) return t('tools.reactRank3')
  return t('tools.reactRank4')
})

function resetScores() {
  bestMs.value = null
  history.value = []
  try {
    localStorage.removeItem(LS_BEST)
    localStorage.removeItem(LS_HISTORY)
  } catch {
    /* ignore */
  }
}

const historyDisplay = computed(() => [...history.value].reverse().slice(0, 8))
</script>

<template>
  <div class="tool-inner">
    <h2 class="tool-title">⚡ {{ t('tools.reaction') }}</h2>
    <div class="tool-meta">
      <span v-if="bestMs != null" class="meta-pill">{{ t('tools.reactionBest', { ms: bestMs }) }}</span>
      <button type="button" class="meta-link" @click="resetScores">{{ t('tools.reactionResetData') }}</button>
    </div>
    <div class="react-area" :class="`bg-${reactStatus}`" @click="handleReactionClick">
      <div class="react-msg">{{ reactionMessage }}</div>
    </div>
    <div v-if="reactStatus === 'result'" class="eval-text bounce-in">{{ reactionEvaluation }}</div>

    <section v-if="historyDisplay.length" class="history-block">
      <h3 class="history-title">{{ t('tools.reactionHistory') }}</h3>
      <ul class="history-list">
        <li v-for="(h, i) in historyDisplay" :key="h.at + '-' + i" class="history-row">
          <span class="history-ms">{{ h.ms }} ms</span>
          <span class="history-time">{{ new Date(h.at).toLocaleString() }}</span>
        </li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.tool-inner {
  min-height: 120px;
}

.tool-title {
  font-size: 1.35rem;
  font-weight: 800;
  margin: 0 0 0.75rem;
  text-align: center;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

:root[data-theme='dark'] .tool-title {
  background: linear-gradient(to right, #a18cd1, #66d9ff);
  -webkit-background-clip: text;
}

.tool-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0.65rem;
  margin-bottom: 1rem;
}

.meta-pill {
  font-size: 0.82rem;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(80, 227, 194, 0.2);
  border: 1px solid rgba(80, 227, 194, 0.45);
}

.meta-link {
  border: none;
  background: none;
  cursor: pointer;
  font: inherit;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--primary-color, #4a90e2);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.react-area {
  width: 100%;
  height: 200px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.35);
  transition: background 0.1s;
  white-space: pre-wrap;
  user-select: none;
  touch-action: manipulation;
}

.bg-idle {
  background: rgba(74, 144, 226, 0.75);
}
.bg-waiting {
  background: rgba(252, 162, 229, 0.75);
}
.bg-click {
  background: rgba(80, 227, 194, 0.88);
}
.bg-early {
  background: rgba(243, 156, 18, 0.8);
}
.bg-result {
  background: rgba(102, 217, 255, 0.45);
}

.eval-text {
  margin-top: 1rem;
  font-size: 1.1rem;
  font-weight: 800;
  color: #c76bb8;
  text-align: center;
}

:root[data-theme='dark'] .eval-text {
  color: #fca2e5;
}

.bounce-in {
  animation: bounceIn 0.5s;
}

@keyframes bounceIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  60% {
    transform: scale(1.05);
    opacity: 1;
  }
  100% {
    transform: scale(1);
  }
}

.history-block {
  margin-top: 1.35rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(74, 144, 226, 0.2);
}

.history-title {
  margin: 0 0 0.5rem;
  font-size: 0.88rem;
  font-weight: 800;
  opacity: 0.9;
}

.history-list {
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: 0.8rem;
}

.history-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed rgba(74, 144, 226, 0.15);
}

.history-ms {
  font-weight: 800;
  color: #4a90e2;
}

:root[data-theme='dark'] .history-ms {
  color: #66d9ff;
}

.history-time {
  opacity: 0.72;
  font-variant-numeric: tabular-nums;
}
</style>
