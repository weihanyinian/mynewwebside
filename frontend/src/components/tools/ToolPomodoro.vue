<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const LS_COUNT = 'weihan_tool_pomo_completed'
const LS_SOUND = 'weihan_tool_pomo_sound'

const pomoWork = ref(25)
const pomoRest = ref(5)
const pomoMode = ref<'work' | 'rest'>('work')
const pomoTimeLeft = ref(25 * 60)
const pomoRunning = ref(false)
const pomoCount = ref(0)
const soundEnabled = ref(true)

let pomoTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  try {
    const c = localStorage.getItem(LS_COUNT)
    if (c) pomoCount.value = Math.max(0, parseInt(c, 10) || 0)
    const s = localStorage.getItem(LS_SOUND)
    if (s === '0') soundEnabled.value = false
  } catch {
    /* ignore */
  }
})

onUnmounted(() => {
  if (pomoTimer) clearInterval(pomoTimer)
})

function persistCount() {
  try {
    localStorage.setItem(LS_COUNT, String(pomoCount.value))
  } catch {
    /* ignore */
  }
}

function persistSound() {
  try {
    localStorage.setItem(LS_SOUND, soundEnabled.value ? '1' : '0')
  } catch {
    /* ignore */
  }
}

const pomoDisplay = computed(() => {
  const m = Math.floor(pomoTimeLeft.value / 60)
    .toString()
    .padStart(2, '0')
  const s = (pomoTimeLeft.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

function applyPomoSettings() {
  if (pomoRunning.value) return
  pomoTimeLeft.value = pomoMode.value === 'work' ? pomoWork.value * 60 : pomoRest.value * 60
}

function togglePomo() {
  if (pomoRunning.value) {
    if (pomoTimer) clearInterval(pomoTimer)
    pomoRunning.value = false
  } else {
    pomoRunning.value = true
    pomoTimer = setInterval(() => {
      pomoTimeLeft.value--
      if (pomoTimeLeft.value <= 0) handlePomoEnd()
    }, 1000)
  }
}

function handlePomoEnd() {
  if (pomoTimer) clearInterval(pomoTimer)
  pomoRunning.value = false
  if (soundEnabled.value) playBeep()
  if (pomoMode.value === 'work') {
    pomoCount.value++
    persistCount()
    pomoMode.value = 'rest'
    pomoTimeLeft.value = pomoRest.value * 60
  } else {
    pomoMode.value = 'work'
    pomoTimeLeft.value = pomoWork.value * 60
  }
}

function resetPomo() {
  if (pomoTimer) clearInterval(pomoTimer)
  pomoRunning.value = false
  pomoMode.value = 'work'
  pomoTimeLeft.value = pomoWork.value * 60
}

function toggleSound() {
  soundEnabled.value = !soundEnabled.value
  persistSound()
}

function playBeep() {
  try {
    const AC = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext
    const ctx = new AC()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'sine'
    osc.frequency.setValueAtTime(880, ctx.currentTime)
    gain.gain.setValueAtTime(0.1, ctx.currentTime)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start()
    osc.stop(ctx.currentTime + 0.5)
  } catch {
    /* ignore */
  }
}

function testSound() {
  if (soundEnabled.value) playBeep()
}
</script>

<template>
  <div class="tool-inner">
    <h2 class="tool-title">🍅 {{ t('tools.pomodoro') }}</h2>
    <div class="sound-row">
      <button type="button" class="sound-toggle site-pill site-pill--nav" @click="toggleSound">
        {{ soundEnabled ? t('tools.pomoSoundOn') : t('tools.pomoSoundOff') }}
      </button>
      <button type="button" class="sound-test site-pill site-pill--nav" :disabled="!soundEnabled" @click="testSound">
        {{ t('tools.pomoTestBeep') }}
      </button>
    </div>
    <p class="sound-hint">{{ t('tools.pomoSoundHint') }}</p>
    <div class="pomo-settings">
      <div class="pomo-input-group">
        <label>{{ t('tools.work') }}</label>
        <input v-model.number="pomoWork" type="number" min="1" max="120" :disabled="pomoRunning" @change="applyPomoSettings" />
      </div>
      <div class="pomo-input-group">
        <label>{{ t('tools.rest') }}</label>
        <input v-model.number="pomoRest" type="number" min="1" max="60" :disabled="pomoRunning" @change="applyPomoSettings" />
      </div>
    </div>
    <div class="pomo-display">
      <div class="pomo-mode-label">{{ pomoMode === 'work' ? '🔥 ' + t('tools.working') : '☕ ' + t('tools.resting') }}</div>
      <div class="pomo-time">{{ pomoDisplay }}</div>
      <div class="pomo-count">{{ t('tools.pomoTotalDone', { count: pomoCount }) }}</div>
    </div>
    <div class="pomo-controls">
      <button type="button" class="pomo-btn play-btn" @click="togglePomo">
        {{ pomoRunning ? '⏸ ' + t('tools.pause') : '▶ ' + t('tools.play') }}
      </button>
      <button type="button" class="pomo-btn reset-btn" @click="resetPomo">🔄 {{ t('tools.reset') }}</button>
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

.sound-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 6px;
}

.sound-toggle,
.sound-test {
  cursor: pointer;
  border: none;
  font: inherit;
  font-size: 0.82rem;
}

.sound-test:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.sound-hint {
  margin: 0 0 1rem;
  text-align: center;
  font-size: 0.78rem;
  opacity: 0.72;
  font-weight: 600;
}

.pomo-settings {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 1.25rem;
}

.pomo-input-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.pomo-input-group label {
  font-size: 0.8rem;
  opacity: 0.75;
}

.pomo-input-group input {
  width: 64px;
  padding: 6px;
  border-radius: 8px;
  border: 1px solid rgba(74, 144, 226, 0.35);
  background: rgba(255, 255, 255, 0.85);
  color: #1a2a3a;
  text-align: center;
  font-weight: 600;
}

:root[data-theme='dark'] .pomo-input-group input {
  background: rgba(0, 0, 0, 0.35);
  color: #eaf8ff;
  border-color: rgba(102, 217, 255, 0.35);
}

.pomo-display {
  text-align: center;
  margin-bottom: 1.5rem;
}

.pomo-mode-label {
  font-size: 1rem;
  font-weight: 600;
  color: #c76bb8;
  margin-bottom: 8px;
}

.pomo-time {
  font-size: 3.25rem;
  font-weight: 900;
  letter-spacing: 2px;
  color: #4a90e2;
  font-family: ui-monospace, monospace;
}

:root[data-theme='dark'] .pomo-time {
  color: #66d9ff;
}

.pomo-count {
  font-size: 0.9rem;
  opacity: 0.7;
  margin-top: 8px;
}

.pomo-controls {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pomo-btn {
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 0.95rem;
  font-weight: 700;
  border: none;
  cursor: pointer;
}

.play-btn {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
}

.reset-btn {
  background: rgba(74, 144, 226, 0.12);
  color: inherit;
  border: 1px solid rgba(74, 144, 226, 0.35);
}
</style>
