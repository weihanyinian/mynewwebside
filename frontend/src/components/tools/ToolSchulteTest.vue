<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

type DifficultyId = 'easy' | 'beginner' | 'standard' | 'advanced' | 'pro'
type GradeLevel = 'excellent' | 'good' | 'normal' | 'improve'
type GameState = 'idle' | 'running' | 'done'

type DifficultyConfig = {
  id: DifficultyId
  label: string
  size: number
}

type HistoryItem = {
  difficultyId: DifficultyId
  difficultyLabel: string
  size: number
  elapsedMs: number
  errors: number
  at: number
}

type BestByDifficulty = Partial<Record<DifficultyId, number>>

const DIFFICULTIES: DifficultyConfig[] = [
  { id: 'easy', label: '入门 3×3', size: 3 },
  { id: 'beginner', label: '初级 4×4', size: 4 },
  { id: 'standard', label: '标准 5×5', size: 5 },
  { id: 'advanced', label: '进阶 6×6', size: 6 },
  { id: 'pro', label: '专业 7×7', size: 7 },
]

const DEFAULT_DIFFICULTY_ID: DifficultyId = 'standard'
const BASE_THRESHOLDS_MS = [10_000, 15_000, 20_000]
const ERROR_PENALTY_MS = 800
const HISTORY_LIMIT = 10
const ANIM_MS = 380

const LS_BEST = 'weihan_tool_schulte_best_by_difficulty'
const LS_HISTORY = 'weihan_tool_schulte_history'

const selectedDifficultyId = ref<DifficultyId>(DEFAULT_DIFFICULTY_ID)
const gameState = ref<GameState>('idle')
const board = ref<number[]>([])
const nextNumber = ref(1)
const errors = ref(0)
const elapsedMs = ref(0)
const startedAt = ref<number | null>(null)
const finishedAt = ref<number | null>(null)
const showResultModal = ref(false)
const resultSnapshotMs = ref(0)
const resultSnapshotErrors = ref(0)
const historyOpen = ref(false)
const bestByDifficulty = ref<BestByDifficulty>({})
const history = ref<HistoryItem[]>([])
const correctFlashIndex = ref<number | null>(null)
const wrongFlashIndex = ref<number | null>(null)

let rafId = 0
let correctTimer: ReturnType<typeof setTimeout> | null = null
let wrongTimer: ReturnType<typeof setTimeout> | null = null

const currentDifficulty = computed(
  () => DIFFICULTIES.find((d) => d.id === selectedDifficultyId.value) ?? DIFFICULTIES[2],
)

const totalCells = computed(() => currentDifficulty.value.size * currentDifficulty.value.size)
const boardSizeCss = computed(() => String(currentDifficulty.value.size))
const currentBestMs = computed(() => bestByDifficulty.value[selectedDifficultyId.value] ?? null)

const displayElapsedMs = computed(() =>
  gameState.value === 'done' ? resultSnapshotMs.value : elapsedMs.value,
)
const displayErrors = computed(() =>
  gameState.value === 'done' ? resultSnapshotErrors.value : errors.value,
)

const currentThresholds = computed(() => {
  const scale = totalCells.value / 25
  return BASE_THRESHOLDS_MS.map((ms) => Math.round(ms * scale))
})

const gradeLevel = computed<GradeLevel>(() => {
  const adjusted = displayElapsedMs.value + displayErrors.value * ERROR_PENALTY_MS
  const [excellent, good, normal] = currentThresholds.value
  if (adjusted <= excellent) return 'excellent'
  if (adjusted <= good) return 'good'
  if (adjusted <= normal) return 'normal'
  return 'improve'
})

const gradeText = computed(() => {
  if (gradeLevel.value === 'excellent') return t('tools.schulteGradeExcellent')
  if (gradeLevel.value === 'good') return t('tools.schulteGradeGood')
  if (gradeLevel.value === 'normal') return t('tools.schulteGradeNormal')
  return t('tools.schulteGradeImprove')
})

const gradeHint = computed(() => {
  const sec = (displayElapsedMs.value / 1000).toFixed(3)
  if (gradeLevel.value === 'excellent') {
    return t('tools.schulteHintExcellent', { sec, errors: displayErrors.value })
  }
  if (gradeLevel.value === 'good') {
    return t('tools.schulteHintGood', { sec, errors: displayErrors.value })
  }
  if (gradeLevel.value === 'normal') {
    return t('tools.schulteHintNormal', { sec, errors: displayErrors.value })
  }
  return t('tools.schulteHintImprove', { sec, errors: displayErrors.value })
})

const canStart = computed(() => gameState.value !== 'running')
const canReset = computed(() => gameState.value !== 'idle')

function formatMs(ms: number) {
  return `${(ms / 1000).toFixed(3)}s`
}

function shuffledNumbers(max: number) {
  const arr = Array.from({ length: max }, (_, i) => i + 1)
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

function resetCore() {
  gameState.value = 'idle'
  nextNumber.value = 1
  errors.value = 0
  elapsedMs.value = 0
  startedAt.value = null
  finishedAt.value = null
  correctFlashIndex.value = null
  wrongFlashIndex.value = null
  showResultModal.value = false
  resultSnapshotMs.value = 0
  resultSnapshotErrors.value = 0
}

function buildMaskedBoard() {
  board.value = Array.from({ length: totalCells.value }, (_, i) => i + 1)
}

function startTest() {
  if (!canStart.value) return
  resetCore()
  board.value = shuffledNumbers(totalCells.value)
  gameState.value = 'running'
}

function resetCurrentRound() {
  resetCore()
  buildMaskedBoard()
}

function closeResultModal() {
  showResultModal.value = false
}

function changeDifficulty(id: DifficultyId) {
  if (gameState.value === 'running') return
  selectedDifficultyId.value = id
  resetCurrentRound()
}

function saveStorage() {
  try {
    localStorage.setItem(LS_BEST, JSON.stringify(bestByDifficulty.value))
    localStorage.setItem(LS_HISTORY, JSON.stringify(history.value.slice(0, HISTORY_LIMIT)))
  } catch {
    // ignore
  }
}

function loadStorage() {
  try {
    const bestRaw = localStorage.getItem(LS_BEST)
    if (bestRaw) bestByDifficulty.value = JSON.parse(bestRaw) as BestByDifficulty
    const historyRaw = localStorage.getItem(LS_HISTORY)
    if (historyRaw) {
      const parsed = JSON.parse(historyRaw) as HistoryItem[]
      if (Array.isArray(parsed)) history.value = parsed.slice(0, HISTORY_LIMIT)
    }
  } catch {
    bestByDifficulty.value = {}
    history.value = []
  }
}

function clearHistory() {
  history.value = []
  saveStorage()
}

function pushHistory(elapsed: number, errs: number) {
  const row: HistoryItem = {
    difficultyId: currentDifficulty.value.id,
    difficultyLabel: currentDifficulty.value.label,
    size: currentDifficulty.value.size,
    elapsedMs: elapsed,
    errors: errs,
    at: Date.now(),
  }
  history.value = [row, ...history.value].slice(0, HISTORY_LIMIT)
}

function updateBest(elapsed: number) {
  const id = currentDifficulty.value.id
  const oldBest = bestByDifficulty.value[id]
  if (oldBest == null || elapsed < oldBest) {
    bestByDifficulty.value = {
      ...bestByDifficulty.value,
      [id]: elapsed,
    }
  }
}

function tick() {
  if (gameState.value === 'running' && startedAt.value != null) {
    elapsedMs.value = Math.max(0, Math.round(performance.now() - startedAt.value))
  }
  rafId = requestAnimationFrame(tick)
}

function markCorrect(index: number) {
  correctFlashIndex.value = index
  if (correctTimer) clearTimeout(correctTimer)
  correctTimer = setTimeout(() => {
    correctFlashIndex.value = null
  }, ANIM_MS)
}

function markWrong(index: number) {
  wrongFlashIndex.value = index
  if (wrongTimer) clearTimeout(wrongTimer)
  wrongTimer = setTimeout(() => {
    wrongFlashIndex.value = null
  }, ANIM_MS)
}

function finishRound() {
  const finalElapsed =
    startedAt.value == null ? 0 : Math.max(0, Math.round(performance.now() - startedAt.value))
  elapsedMs.value = finalElapsed
  finishedAt.value = performance.now()
  gameState.value = 'done'
  resultSnapshotMs.value = finalElapsed
  resultSnapshotErrors.value = errors.value
  updateBest(finalElapsed)
  pushHistory(finalElapsed, errors.value)
  saveStorage()
  showResultModal.value = true
}

function onCellPress(index: number) {
  if (gameState.value !== 'running') return
  const val = board.value[index]
  if (val !== nextNumber.value) {
    errors.value += 1
    markWrong(index)
    return
  }
  if (nextNumber.value === 1 && startedAt.value == null) {
    startedAt.value = performance.now()
  }
  markCorrect(index)
  nextNumber.value += 1
  if (val === totalCells.value) {
    finishRound()
  }
}

function retryFromModal() {
  closeResultModal()
  startTest()
}

function switchDifficultyFromModal() {
  closeResultModal()
  resetCurrentRound()
}

function isDoneCell(val: number) {
  return val < nextNumber.value
}

onMounted(() => {
  loadStorage()
  resetCurrentRound()
  rafId = requestAnimationFrame(tick)
})

onUnmounted(() => {
  cancelAnimationFrame(rafId)
  if (correctTimer) clearTimeout(correctTimer)
  if (wrongTimer) clearTimeout(wrongTimer)
})
</script>

<template>
  <div class="tool-inner">
    <header class="head">
      <h2 class="tool-title">🔢 {{ t('tools.schulteTitle') }}</h2>
      <p class="tool-lead">{{ t('tools.schulteLead') }}</p>
    </header>

    <section class="difficulty-row" aria-label="难度选择">
      <button
        v-for="d in DIFFICULTIES"
        :key="d.id"
        type="button"
        class="difficulty-btn"
        :class="{ active: selectedDifficultyId === d.id }"
        :disabled="gameState === 'running'"
        @click="changeDifficulty(d.id)"
      >
        {{ d.label }}
      </button>
    </section>

    <section class="stats-card" aria-live="polite">
      <div class="stat-item">
        <span class="label">{{ t('tools.schulteStatTime') }}</span>
        <strong class="value mono">{{ formatMs(displayElapsedMs) }}</strong>
      </div>
      <div class="stat-item">
        <span class="label">{{ t('tools.schulteStatErrors') }}</span>
        <strong class="value">{{ displayErrors }}</strong>
      </div>
      <div class="stat-item">
        <span class="label">{{ t('tools.schulteStatBest') }}</span>
        <strong class="value mono">{{ currentBestMs == null ? '--' : formatMs(currentBestMs) }}</strong>
      </div>
    </section>

    <section class="board-wrap">
      <div
        class="board-grid"
        :style="{ '--board-size': boardSizeCss }"
        role="grid"
        :aria-label="`${currentDifficulty.label} 舒尔特方格`"
      >
        <button
          v-for="(val, index) in board"
          :key="`${val}-${index}`"
          type="button"
          class="cell"
          role="gridcell"
          :disabled="gameState !== 'running'"
          :class="{
            'is-correct': correctFlashIndex === index,
            'is-wrong': wrongFlashIndex === index,
            'is-done': isDoneCell(val),
          }"
          @click="onCellPress(index)"
          @keydown.enter.prevent="onCellPress(index)"
          @keydown.space.prevent="onCellPress(index)"
        >
          <span v-if="gameState === 'running' || gameState === 'done'">{{ val }}</span>
          <span v-else>•</span>
        </button>
      </div>
      <p v-if="gameState === 'running'" class="next-tip">
        {{ t('tools.schulteNextTip', { n: nextNumber }) }}
      </p>
      <p v-else-if="gameState === 'idle'" class="next-tip">
        {{ t('tools.schulteIdleTip') }}
      </p>
      <p v-else class="next-tip">
        {{ t('tools.schulteDoneTip') }}
      </p>
    </section>

    <section class="actions">
      <button type="button" class="action-btn action-btn--primary" :disabled="!canStart" @click="startTest">
        {{ t('tools.schulteStart') }}
      </button>
      <button type="button" class="action-btn action-btn--ghost" :disabled="!canReset" @click="resetCurrentRound">
        {{ t('tools.schulteReset') }}
      </button>
    </section>

    <section class="history-panel">
      <button type="button" class="history-toggle" @click="historyOpen = !historyOpen">
        {{ historyOpen ? t('tools.schulteHideHistory') : t('tools.schulteShowHistory') }}
      </button>
      <div v-if="historyOpen" class="history-body">
        <div class="history-actions">
          <span class="history-count">{{ t('tools.schulteHistoryCount', { n: history.length }) }}</span>
          <button type="button" class="clear-btn" :disabled="history.length === 0" @click="clearHistory">
            {{ t('tools.schulteClearHistory') }}
          </button>
        </div>
        <ul v-if="history.length" class="history-list">
          <li v-for="(item, idx) in history" :key="item.at + '-' + idx" class="history-row">
            <span>{{ item.difficultyLabel }}</span>
            <span class="mono">{{ formatMs(item.elapsedMs) }}</span>
            <span>{{ t('tools.schulteErrorShort', { n: item.errors }) }}</span>
            <span class="history-time">{{ new Date(item.at).toLocaleString() }}</span>
          </li>
        </ul>
        <p v-else class="history-empty">{{ t('tools.schulteNoHistory') }}</p>
      </div>
    </section>

    <div v-if="showResultModal" class="result-mask" role="dialog" aria-modal="true">
      <div class="result-modal">
        <h3 class="result-title">{{ t('tools.schulteResultTitle') }}</h3>
        <p class="result-line">{{ t('tools.schulteResultDifficulty', { d: currentDifficulty.label }) }}</p>
        <p class="result-line">{{ t('tools.schulteResultTime', { time: formatMs(resultSnapshotMs) }) }}</p>
        <p class="result-line">{{ t('tools.schulteResultErrors', { n: resultSnapshotErrors }) }}</p>
        <p class="result-grade">{{ gradeText }}</p>
        <p class="result-hint">{{ gradeHint }}</p>
        <div class="result-actions">
          <button type="button" class="action-btn action-btn--primary" @click="retryFromModal">
            {{ t('tools.schulteAgain') }}
          </button>
          <button type="button" class="action-btn action-btn--ghost" @click="switchDifficultyFromModal">
            {{ t('tools.schulteChangeDifficulty') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tool-inner {
  min-height: 120px;
}

.head {
  text-align: center;
  margin-bottom: 1rem;
}

.tool-title {
  font-size: 1.35rem;
  font-weight: 800;
  margin: 0 0 0.55rem;
  text-align: center;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

:root[data-theme='dark'] .tool-title {
  background: linear-gradient(to right, #a18cd1, #66d9ff);
  -webkit-background-clip: text;
  background-clip: text;
}

.tool-lead {
  margin: 0 auto;
  max-width: 620px;
  font-size: 0.86rem;
  font-weight: 600;
  line-height: 1.55;
  opacity: 0.85;
}

.difficulty-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 8px;
  margin-bottom: 0.9rem;
}

.difficulty-btn {
  min-height: 44px;
  border-radius: 14px;
  border: 1px solid rgba(74, 144, 226, 0.3);
  background: rgba(74, 144, 226, 0.1);
  color: inherit;
  font: inherit;
  font-size: 0.83rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.difficulty-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(74, 144, 226, 0.16);
}

.difficulty-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.difficulty-btn.active {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.35);
}

.difficulty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

:root[data-theme='dark'] .difficulty-btn {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.16);
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  background: rgba(74, 144, 226, 0.08);
  border: 1px solid rgba(74, 144, 226, 0.18);
  border-radius: 14px;
  padding: 0.75rem;
}

:root[data-theme='dark'] .stats-card {
  background: rgba(0, 0, 0, 0.22);
  border-color: rgba(255, 255, 255, 0.12);
}

.stat-item {
  text-align: center;
}

.label {
  display: block;
  font-size: 0.72rem;
  opacity: 0.75;
  margin-bottom: 2px;
}

.value {
  font-size: 1rem;
  color: #4a90e2;
}

.mono {
  font-variant-numeric: tabular-nums;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
}

:root[data-theme='dark'] .value {
  color: #66d9ff;
}

.board-wrap {
  margin-top: 0.9rem;
}

.board-grid {
  --board-size: 5;
  display: grid;
  grid-template-columns: repeat(var(--board-size), minmax(44px, 1fr));
  gap: 8px;
}

.cell {
  aspect-ratio: 1 / 1;
  min-height: 44px;
  border-radius: 12px;
  border: 1px solid rgba(74, 144, 226, 0.25);
  background: rgba(255, 255, 255, 0.58);
  color: #1f3245;
  font: inherit;
  font-size: clamp(0.9rem, 2.8vw, 1.08rem);
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  touch-action: manipulation;
}

:root[data-theme='dark'] .cell {
  background: rgba(255, 255, 255, 0.06);
  color: #eaf8ff;
  border-color: rgba(255, 255, 255, 0.16);
}

.cell:disabled {
  cursor: default;
  opacity: 0.75;
}

.cell.is-done {
  color: rgba(31, 50, 69, 0.5);
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.28), rgba(80, 227, 194, 0.28));
}

:root[data-theme='dark'] .cell.is-done {
  color: rgba(234, 248, 255, 0.55);
}

.cell.is-correct {
  animation: pulseOk 0.35s ease;
  border-color: rgba(80, 227, 194, 0.7);
  box-shadow: 0 8px 18px rgba(80, 227, 194, 0.22);
}

.cell.is-wrong {
  animation: shakeWrong 0.35s ease;
  border-color: rgba(246, 92, 139, 0.8);
  box-shadow: 0 8px 18px rgba(246, 92, 139, 0.2);
}

@keyframes pulseOk {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.94);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes shakeWrong {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-3px);
  }
  50% {
    transform: translateX(3px);
  }
  75% {
    transform: translateX(-2px);
  }
}

.next-tip {
  margin: 0.6rem 0 0;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 700;
  opacity: 0.84;
}

.actions {
  margin-top: 0.95rem;
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.action-btn {
  min-height: 44px;
  padding: 10px 18px;
  border-radius: 18px;
  border: 1px solid transparent;
  font: inherit;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.action-btn:active:not(:disabled) {
  transform: scale(0.97);
}

.action-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.action-btn--primary {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
}

.action-btn--ghost {
  background: rgba(74, 144, 226, 0.12);
  border-color: rgba(74, 144, 226, 0.3);
  color: inherit;
}

:root[data-theme='dark'] .action-btn--ghost {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.18);
}

.history-panel {
  margin-top: 1rem;
  border-top: 1px dashed rgba(74, 144, 226, 0.24);
  padding-top: 0.85rem;
}

.history-toggle {
  width: 100%;
  min-height: 44px;
  border-radius: 12px;
  border: 1px solid rgba(74, 144, 226, 0.24);
  background: rgba(74, 144, 226, 0.08);
  color: inherit;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

:root[data-theme='dark'] .history-toggle {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.12);
}

.history-body {
  margin-top: 0.65rem;
}

.history-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 0.4rem;
}

.history-count {
  font-size: 0.76rem;
  opacity: 0.78;
}

.clear-btn {
  border: none;
  background: none;
  color: #4a90e2;
  font: inherit;
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  text-decoration: underline;
}

.clear-btn:disabled {
  opacity: 0.42;
  cursor: not-allowed;
}

:root[data-theme='dark'] .clear-btn {
  color: #66d9ff;
}

.history-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.history-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 4px 8px;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(74, 144, 226, 0.16);
  font-size: 0.78rem;
}

.history-time {
  grid-column: 1 / -1;
  opacity: 0.68;
}

.history-empty {
  margin: 0;
  text-align: center;
  font-size: 0.8rem;
  opacity: 0.7;
}

.result-mask {
  position: fixed;
  inset: 0;
  background: rgba(9, 12, 18, 0.48);
  display: grid;
  place-items: center;
  z-index: 1500;
  padding: 14px;
}

.result-modal {
  width: min(460px, 100%);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 42px rgba(0, 0, 0, 0.2);
  padding: 1rem 1rem 1.1rem;
  animation: modalIn 0.24s ease;
}

:root[data-theme='dark'] .result-modal {
  background: rgba(20, 26, 38, 0.95);
  border-color: rgba(255, 255, 255, 0.14);
}

@keyframes modalIn {
  from {
    transform: translateY(8px) scale(0.98);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.result-title {
  margin: 0 0 0.5rem;
  text-align: center;
  font-size: 1.1rem;
}

.result-line {
  margin: 0.18rem 0;
  font-size: 0.86rem;
}

.result-grade {
  margin: 0.7rem 0 0.3rem;
  text-align: center;
  font-size: 1.15rem;
  font-weight: 900;
  color: #4a90e2;
}

:root[data-theme='dark'] .result-grade {
  color: #66d9ff;
}

.result-hint {
  margin: 0 0 0.8rem;
  text-align: center;
  font-size: 0.82rem;
  line-height: 1.55;
  opacity: 0.88;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 560px) {
  .difficulty-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stats-card {
    grid-template-columns: 1fr;
  }
}
</style>
