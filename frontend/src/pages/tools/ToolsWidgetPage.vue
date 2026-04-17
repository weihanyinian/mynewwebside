<script setup lang="ts">
import { computed, ref, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import ToolBackBar from '../../components/tools/ToolBackBar.vue'

const { t } = useI18n()
const route = useRoute()

type ToolId = 'reaction' | 'cps' | 'pomodoro' | 'password' | 'base64'
const toolId = computed(() => route.meta.toolId as ToolId)

// ========== Reaction ==========
const reactStatus = ref<'idle' | 'waiting' | 'click' | 'early' | 'result'>('idle')
const reactTime = ref(0)
let reactStart = 0
let reactTimeout: ReturnType<typeof setTimeout> | null = null

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
    reactTime.value = Math.max(0, Math.round(performance.now() - reactStart))
    reactStatus.value = 'result'
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
  if (reactTime.value < 150) return '⚡ 神级反应！'
  if (reactTime.value < 220) return '🚀 极速达人！'
  if (reactTime.value < 300) return '🌟 凡人巅峰！'
  return '🐢 还需要练习哦~'
})

// ========== CPS ==========
const cpsMode = ref(5)
const cpsStatus = ref<'idle' | 'running' | 'result'>('idle')
const cpsTimeLeft = ref(5)
const cpsClicks = ref(0)
const cpsScore = ref(0)
const cpsHighScore = ref(0)
let cpsTimer: ReturnType<typeof setInterval> | null = null

function resetCps() {
  cpsStatus.value = 'idle'
  cpsTimeLeft.value = cpsMode.value
  cpsClicks.value = 0
  if (cpsTimer) clearInterval(cpsTimer)
}

function setCpsMode(m: number) {
  if (cpsStatus.value === 'running') return
  cpsMode.value = m
  resetCps()
}

function handleCpsClick() {
  if (cpsStatus.value === 'idle') {
    cpsStatus.value = 'running'
    cpsClicks.value++
    cpsTimeLeft.value = cpsMode.value
    cpsTimer = setInterval(() => {
      cpsTimeLeft.value--
      if (cpsTimeLeft.value <= 0) endCps()
    }, 1000)
  } else if (cpsStatus.value === 'running') {
    cpsClicks.value++
  }
}

function endCps() {
  if (cpsTimer) clearInterval(cpsTimer)
  cpsStatus.value = 'result'
  cpsScore.value = +(cpsClicks.value / cpsMode.value).toFixed(2)
  if (cpsScore.value > cpsHighScore.value) cpsHighScore.value = cpsScore.value
}

const cpsEvaluation = computed(() => {
  if (cpsStatus.value !== 'result') return ''
  if (cpsScore.value >= 12) return '⚡ 键盘破坏者！'
  if (cpsScore.value >= 8) return '🔥 手速惊人！'
  if (cpsScore.value >= 5) return '✨ 稳扎稳打！'
  return '🍃 慢工出细活~'
})

// ========== Pomodoro ==========
const pomoWork = ref(25)
const pomoRest = ref(5)
const pomoMode = ref<'work' | 'rest'>('work')
const pomoTimeLeft = ref(25 * 60)
const pomoRunning = ref(false)
const pomoCount = ref(0)
let pomoTimer: ReturnType<typeof setInterval> | null = null

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
  playBeep()
  if (pomoMode.value === 'work') {
    pomoCount.value++
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

// ========== Password ==========
const pwdLength = ref(12)
const pwdUpper = ref(true)
const pwdLower = ref(true)
const pwdNum = ref(true)
const pwdSym = ref(true)
const pwdResult = ref('')

function generatePwd() {
  const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const lower = 'abcdefghijklmnopqrstuvwxyz'
  const num = '0123456789'
  const sym = '!@#$%^&*()_+~`|}{[]:;?><,./-='
  let charset = ''
  if (pwdUpper.value) charset += upper
  if (pwdLower.value) charset += lower
  if (pwdNum.value) charset += num
  if (pwdSym.value) charset += sym
  if (!charset) {
    pwdResult.value = '请至少选择一种字符'
    return
  }
  let res = ''
  for (let i = 0; i < pwdLength.value; i++) {
    res += charset[Math.floor(Math.random() * charset.length)]
  }
  pwdResult.value = res
}

function copyPwd() {
  if (pwdResult.value && pwdResult.value !== '请至少选择一种字符') {
    void navigator.clipboard.writeText(pwdResult.value)
  }
}

// ========== Base64 ==========
const base64Input = ref('')
const base64Output = ref('')
const base64Mode = ref<'encode' | 'decode'>('encode')

function processBase64() {
  try {
    if (!base64Input.value) {
      base64Output.value = ''
      return
    }
    if (base64Mode.value === 'encode') {
      base64Output.value = btoa(
        encodeURIComponent(base64Input.value).replace(/%([0-9A-F]{2})/g, (_, p1) =>
          String.fromCharCode(Number(`0x${p1}`)),
        ),
      )
    } else {
      base64Output.value = decodeURIComponent(
        atob(base64Input.value)
          .split('')
          .map((c) => `%${(`00${c.charCodeAt(0).toString(16)}`).slice(-2)}`)
          .join(''),
      )
    }
  } catch {
    base64Output.value = '解析失败，请输入有效内容'
  }
}

onUnmounted(() => {
  if (reactTimeout) clearTimeout(reactTimeout)
  if (cpsTimer) clearInterval(cpsTimer)
  if (pomoTimer) clearInterval(pomoTimer)
})
</script>

<template>
  <div class="tools-widget-page">
    <ToolBackBar />

    <div class="tool-sheet glass-tool-sheet">
      <!-- Reaction -->
      <template v-if="toolId === 'reaction'">
        <h2 class="tool-title">⚡ {{ t('tools.reaction') }}</h2>
        <div class="react-area" :class="`bg-${reactStatus}`" @click="handleReactionClick">
          <div class="react-msg">{{ reactionMessage }}</div>
        </div>
        <div v-if="reactStatus === 'result'" class="eval-text bounce-in">{{ reactionEvaluation }}</div>
      </template>

      <!-- CPS -->
      <template v-else-if="toolId === 'cps'">
        <h2 class="tool-title">🖱️ {{ t('tools.cps') }}</h2>
        <div class="cps-modes">
          <span class="cps-mode-btn" :class="{ active: cpsMode === 1 }" @click="setCpsMode(1)">1s</span>
          <span class="cps-mode-btn" :class="{ active: cpsMode === 3 }" @click="setCpsMode(3)">3s</span>
          <span class="cps-mode-btn" :class="{ active: cpsMode === 5 }" @click="setCpsMode(5)">5s</span>
        </div>
        <div class="cps-stats">
          <div class="stat">
            <span class="stat-label">时间</span>
            <span class="stat-val">{{ cpsTimeLeft }}s</span>
          </div>
          <div class="stat">
            <span class="stat-label">点击数</span>
            <span class="stat-val">{{ cpsClicks }}</span>
          </div>
        </div>
        <div class="cps-area" @click="handleCpsClick">
          <div v-if="cpsStatus === 'idle'" class="cps-msg">{{ t('tools.clickNow') }}</div>
          <div v-else-if="cpsStatus === 'running'" class="cps-msg cps-count">{{ cpsClicks }}</div>
          <div v-else class="cps-msg">
            <div>最高: {{ cpsHighScore }} CPS</div>
            <div class="eval-text">{{ cpsEvaluation }}</div>
            <div class="cps-retry">(点击重试)</div>
          </div>
        </div>
      </template>

      <!-- Pomodoro -->
      <template v-else-if="toolId === 'pomodoro'">
        <h2 class="tool-title">🍅 {{ t('tools.pomodoro') }}</h2>
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
          <div class="pomo-count">{{ t('tools.todayFocus', { count: pomoCount }) }}</div>
        </div>
        <div class="pomo-controls">
          <button type="button" class="pomo-btn play-btn" @click="togglePomo">
            {{ pomoRunning ? '⏸ ' + t('tools.pause') : '▶ ' + t('tools.play') }}
          </button>
          <button type="button" class="pomo-btn reset-btn" @click="resetPomo">🔄 {{ t('tools.reset') }}</button>
        </div>
      </template>

      <!-- Password -->
      <template v-else-if="toolId === 'password'">
        <h2 class="tool-title">🔑 {{ t('tools.password') }}</h2>
        <div class="pwd-result-box" @click="copyPwd">
          {{ pwdResult || t('tools.generatePwd') }}
          <span v-if="pwdResult" class="pwd-copy-hint">点击复制</span>
        </div>
        <div class="pwd-settings">
          <div class="pwd-row">
            <label>{{ t('tools.pwdLength') }} ({{ pwdLength }})</label>
            <input v-model.number="pwdLength" type="range" min="4" max="32" class="pwd-slider" />
          </div>
          <div class="pwd-options">
            <label class="cyber-checkbox"><input v-model="pwdUpper" type="checkbox" /> <span>{{ t('tools.pwdUpper') }}</span></label>
            <label class="cyber-checkbox"><input v-model="pwdLower" type="checkbox" /> <span>{{ t('tools.pwdLower') }}</span></label>
            <label class="cyber-checkbox"><input v-model="pwdNum" type="checkbox" /> <span>{{ t('tools.pwdNum') }}</span></label>
            <label class="cyber-checkbox"><input v-model="pwdSym" type="checkbox" /> <span>{{ t('tools.pwdSym') }}</span></label>
          </div>
        </div>
        <button type="button" class="pwd-gen-btn" @click="generatePwd">{{ t('tools.generatePwd') }}</button>
      </template>

      <!-- Base64 -->
      <template v-else-if="toolId === 'base64'">
        <h2 class="tool-title">🔤 {{ t('tools.base64') }}</h2>
        <div class="b64-modes">
          <span class="b64-mode-btn" :class="{ active: base64Mode === 'encode' }" @click="base64Mode = 'encode'; processBase64()">{{ t('tools.encode') }}</span>
          <span class="b64-mode-btn" :class="{ active: base64Mode === 'decode' }" @click="base64Mode = 'decode'; processBase64()">{{ t('tools.decode') }}</span>
        </div>
        <textarea v-model="base64Input" class="cyber-textarea b64-input" :placeholder="t('tools.inputB64')" @input="processBase64" />
        <div class="b64-arrow">↓</div>
        <textarea v-model="base64Output" class="cyber-textarea b64-output" readonly :placeholder="t('tools.outputB64')" />
      </template>
    </div>
  </div>
</template>

<style scoped>
.tools-widget-page {
  max-width: 480px;
  margin: 0 auto;
  padding-bottom: 2rem;
}

.glass-tool-sheet {
  background: var(--glass-bg, rgba(255, 255, 255, 0.4));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.55));
  box-shadow: var(--glass-shadow, 0 8px 32px rgba(74, 144, 226, 0.15));
  border-radius: 24px;
  padding: 1.75rem 1.5rem 2rem;
  color: var(--text-color, #2c3e50);
}

:root[data-theme='dark'] .glass-tool-sheet {
  background: rgba(16, 18, 27, 0.72);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.45);
  color: #eaf8ff;
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

.cps-modes {
  display: flex;
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
  cursor: pointer;
  transition: all 0.2s;
}

:root[data-theme='dark'] .cps-mode-btn {
  background: rgba(255, 255, 255, 0.08);
}

.cps-mode-btn.active {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
}

.cps-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 1rem;
  background: rgba(74, 144, 226, 0.08);
  border-radius: 12px;
  padding: 10px;
}

:root[data-theme='dark'] .cps-stats {
  background: rgba(0, 0, 0, 0.25);
}

.stat {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 0.8rem;
  opacity: 0.75;
}

.stat-val {
  font-size: 1.35rem;
  font-weight: 800;
  color: #4a90e2;
}

:root[data-theme='dark'] .stat-val {
  color: #66d9ff;
}

.cps-area {
  width: 100%;
  height: 180px;
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
}

.cps-count {
  font-size: 2.75rem;
  color: #c76bb8;
}

:root[data-theme='dark'] .cps-count {
  color: #fca2e5;
}

.cps-retry {
  font-size: 0.8rem;
  margin-top: 10px;
  opacity: 0.85;
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

.pwd-result-box {
  background: rgba(74, 144, 226, 0.08);
  border: 1px solid rgba(74, 144, 226, 0.35);
  padding: 14px;
  border-radius: 12px;
  font-family: ui-monospace, monospace;
  font-size: 1.05rem;
  margin-bottom: 1.25rem;
  cursor: pointer;
  position: relative;
  word-break: break-all;
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.pwd-copy-hint {
  position: absolute;
  right: 10px;
  bottom: -18px;
  font-size: 0.75rem;
  color: #50e3c2;
  opacity: 0;
  transition: opacity 0.2s;
}

.pwd-result-box:hover .pwd-copy-hint {
  opacity: 1;
}

.pwd-settings {
  text-align: left;
  margin-bottom: 1.25rem;
}

.pwd-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.pwd-slider {
  width: 58%;
}

.pwd-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.cyber-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.88rem;
}

.pwd-gen-btn {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4a90e2, #50e3c2);
  color: #fff;
  font-weight: 700;
  border: none;
  cursor: pointer;
}

.b64-modes {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.b64-mode-btn {
  padding: 6px 18px;
  border-radius: 20px;
  background: rgba(74, 144, 226, 0.12);
  cursor: pointer;
  font-weight: 600;
}

.b64-mode-btn.active {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
}

.cyber-textarea {
  width: 100%;
  height: 120px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(74, 144, 226, 0.35);
  border-radius: 12px;
  padding: 10px;
  color: inherit;
  font-family: ui-monospace, monospace;
  resize: vertical;
  box-sizing: border-box;
}

:root[data-theme='dark'] .cyber-textarea {
  background: rgba(0, 0, 0, 0.35);
  border-color: rgba(102, 217, 255, 0.3);
}

.cyber-textarea:focus {
  outline: none;
  border-color: #50e3c2;
}

.b64-arrow {
  text-align: center;
  margin: 8px 0;
  font-size: 1.25rem;
  color: #4a90e2;
}
</style>
