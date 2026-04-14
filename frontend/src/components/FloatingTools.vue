<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'

const isMenuOpen = ref(false)
const activeTool = ref<'none' | 'reaction' | 'cps' | 'pomodoro' | 'password' | 'base64'>('none')

function toggleMenu() {
  isMenuOpen.value = !isMenuOpen.value
}

function openTool(tool: 'reaction' | 'cps' | 'pomodoro' | 'password' | 'base64') {
  activeTool.value = tool
  isMenuOpen.value = false
}

function closeTool() {
  activeTool.value = 'none'
  resetReaction()
  resetCps()
  // Note: Pomodoro keeps running in the background deliberately.
}

// ==========================================
// 1. Reaction Test
// ==========================================
const reactStatus = ref<'idle' | 'waiting' | 'click' | 'early' | 'result'>('idle')
const reactTime = ref(0)
let reactStart = 0
let reactTimeout: ReturnType<typeof setTimeout> | null = null

function resetReaction() {
  reactStatus.value = 'idle'
  if (reactTimeout) clearTimeout(reactTimeout)
}

function handleReactionClick() {
  if (reactStatus.value === 'idle' || reactStatus.value === 'result' || reactStatus.value === 'early') {
    // Start waiting
    reactStatus.value = 'waiting'
    const delay = Math.random() * 3000 + 2000 // 2-5 seconds
    reactTimeout = setTimeout(() => {
      reactStatus.value = 'click'
      reactStart = performance.now()
    }, delay)
  } else if (reactStatus.value === 'waiting') {
    // Clicked too early
    if (reactTimeout) clearTimeout(reactTimeout)
    reactStatus.value = 'early'
  } else if (reactStatus.value === 'click') {
    // Clicked correctly
    reactTime.value = Math.max(0, Math.round(performance.now() - reactStart))
    reactStatus.value = 'result'
  }
}

const reactionMessage = computed(() => {
  switch (reactStatus.value) {
    case 'idle': return '准备开始\n(点击区域)'
    case 'waiting': return '等待颜色变绿...'
    case 'click': return '点击！'
    case 'early': return '太早了！\n点击重试'
    case 'result': return `反应时间: ${reactTime.value} ms\n(点击重试)`
    default: return ''
  }
})

const reactionEvaluation = computed(() => {
  if (reactStatus.value !== 'result') return ''
  if (reactTime.value < 150) return '⚡ 神级反应！'
  if (reactTime.value < 220) return '🚀 极速达人！'
  if (reactTime.value < 300) return '🌟 凡人巅峰！'
  return '🐢 还需要练习哦~'
})

// ==========================================
// 2. CPS Test
// ==========================================
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
      if (cpsTimeLeft.value <= 0) {
        endCps()
      }
    }, 1000)
  } else if (cpsStatus.value === 'running') {
    cpsClicks.value++
  }
}

function endCps() {
  if (cpsTimer) clearInterval(cpsTimer)
  cpsStatus.value = 'result'
  cpsScore.value = +(cpsClicks.value / cpsMode.value).toFixed(2)
  if (cpsScore.value > cpsHighScore.value) {
    cpsHighScore.value = cpsScore.value
  }
}

const cpsEvaluation = computed(() => {
  if (cpsStatus.value !== 'result') return ''
  if (cpsScore.value >= 12) return '⚡ 键盘破坏者！'
  if (cpsScore.value >= 8) return '🔥 手速惊人！'
  if (cpsScore.value >= 5) return '✨ 稳扎稳打！'
  return '🍃 慢工出细活~'
})

// ==========================================
// 3. Pomodoro Timer
// ==========================================
const pomoWork = ref(25)
const pomoRest = ref(5)
const pomoMode = ref<'work' | 'rest'>('work')
const pomoTimeLeft = ref(25 * 60)
const pomoRunning = ref(false)
const pomoCount = ref(0)
let pomoTimer: ReturnType<typeof setInterval> | null = null

const pomoDisplay = computed(() => {
  const m = Math.floor(pomoTimeLeft.value / 60).toString().padStart(2, '0')
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
      if (pomoTimeLeft.value <= 0) {
        handlePomoEnd()
      }
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
    const AudioContext = window.AudioContext || (window as any).webkitAudioContext
    const ctx = new AudioContext()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'sine'
    osc.frequency.setValueAtTime(880, ctx.currentTime)
    gain.gain.setValueAtTime(0.1, ctx.currentTime) // Soft volume
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start()
    osc.stop(ctx.currentTime + 0.5)
  } catch(e) {
    console.error("Audio beep not supported", e)
  }
}

// ==========================================
// 4. Password Generator
// ==========================================
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
    navigator.clipboard.writeText(pwdResult.value)
  }
}

// ==========================================
// 5. Base64
// ==========================================
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
      // Encode with UTF-8 support
      base64Output.value = btoa(encodeURIComponent(base64Input.value).replace(/%([0-9A-F]{2})/g,
        (_, p1) => String.fromCharCode(Number('0x' + p1))
      ))
    } else {
      // Decode with UTF-8 support
      base64Output.value = decodeURIComponent(atob(base64Input.value).split('').map(c => 
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      ).join(''))
    }
  } catch (e) {
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
  <div class="floating-widget">
    <!-- Floating Trigger -->
    <div class="widget-btn glass-ui" @click="toggleMenu" :class="{ 'is-active': isMenuOpen }">
      <span class="icon">🎮</span>
    </div>
    
    <!-- Floating Menu -->
    <transition name="fade-slide">
      <div v-if="isMenuOpen" class="widget-menu glass-ui">
        <div class="menu-item" @click="openTool('reaction')">
          <span class="item-icon">⚡</span> 反应力测试
        </div>
        <div class="menu-item" @click="openTool('cps')">
          <span class="item-icon">🖱️</span> CPS 测试
        </div>
        <div class="menu-item" @click="openTool('pomodoro')">
          <span class="item-icon">🍅</span> 番茄钟
        </div>
        <div class="menu-item" @click="openTool('password')">
          <span class="item-icon">🔑</span> 随机密码
        </div>
        <div class="menu-item" @click="openTool('base64')">
          <span class="item-icon">🔤</span> Base64
        </div>
      </div>
    </transition>

    <!-- Modals (Teleported to avoid clipping) -->
    <teleport to="body">
      <transition name="modal-fade">
        <div v-if="activeTool !== 'none'" class="tool-overlay" @click.self="closeTool">
          
          <!-- ================= 1. Reaction Test ================= -->
          <div v-if="activeTool === 'reaction'" class="tool-modal glass-ui-modal">
            <button class="close-btn" @click="closeTool">✕</button>
            <h2 class="modal-title">⚡ 反应力测试</h2>
            
            <div 
              class="react-area" 
              :class="`bg-${reactStatus}`"
              @click="handleReactionClick"
            >
              <div class="react-msg">{{ reactionMessage }}</div>
            </div>
            
            <div v-if="reactStatus === 'result'" class="eval-text bounce-in">
              {{ reactionEvaluation }}
            </div>
          </div>

          <!-- ================= 2. CPS Test ================= -->
          <div v-if="activeTool === 'cps'" class="tool-modal glass-ui-modal">
            <button class="close-btn" @click="closeTool">✕</button>
            <h2 class="modal-title">🖱️ CPS 点击测试</h2>
            
            <div class="cps-modes">
              <span class="cps-mode-btn" :class="{ active: cpsMode === 1 }" @click="setCpsMode(1)">1秒</span>
              <span class="cps-mode-btn" :class="{ active: cpsMode === 3 }" @click="setCpsMode(3)">3秒</span>
              <span class="cps-mode-btn" :class="{ active: cpsMode === 5 }" @click="setCpsMode(5)">5秒</span>
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

            <div 
              class="cps-area" 
              @click="handleCpsClick"
            >
              <div v-if="cpsStatus === 'idle'" class="cps-msg">点击此区域开始！</div>
              <div v-else-if="cpsStatus === 'running'" class="cps-msg cps-count">{{ cpsClicks }}</div>
              <div v-else class="cps-msg">
                <div>最高: {{ cpsHighScore }} CPS</div>
                <div class="eval-text">{{ cpsEvaluation }}</div>
                <div style="font-size: 0.8rem; margin-top:10px;">(点击重试)</div>
              </div>
            </div>
          </div>

          <!-- ================= 3. Pomodoro ================= -->
          <div v-if="activeTool === 'pomodoro'" class="tool-modal glass-ui-modal">
            <button class="close-btn" @click="closeTool">✕</button>
            <h2 class="modal-title">🍅 番茄专注钟</h2>

            <div class="pomo-settings">
              <div class="pomo-input-group">
                <label>专注 (分)</label>
                <input type="number" v-model="pomoWork" @change="applyPomoSettings" min="1" max="120" :disabled="pomoRunning" />
              </div>
              <div class="pomo-input-group">
                <label>休息 (分)</label>
                <input type="number" v-model="pomoRest" @change="applyPomoSettings" min="1" max="60" :disabled="pomoRunning" />
              </div>
            </div>

            <div class="pomo-display">
              <div class="pomo-mode-label">{{ pomoMode === 'work' ? '🔥 专注中' : '☕ 休息中' }}</div>
              <div class="pomo-time">{{ pomoDisplay }}</div>
              <div class="pomo-count">今日专注: {{ pomoCount }} 次</div>
            </div>

            <div class="pomo-controls">
              <button class="pomo-btn play-btn" @click="togglePomo">
                {{ pomoRunning ? '⏸ 暂停' : '▶ 开始' }}
              </button>
              <button class="pomo-btn reset-btn" @click="resetPomo">
                🔄 重置
              </button>
            </div>
          </div>

          <!-- ================= 4. Password Generator ================= -->
          <div v-if="activeTool === 'password'" class="tool-modal glass-ui-modal">
            <button class="close-btn" @click="closeTool">✕</button>
            <h2 class="modal-title">🔑 密码生成器</h2>
            
            <div class="pwd-result-box" @click="copyPwd">
              {{ pwdResult || '点击下方生成' }}
              <span v-if="pwdResult" class="pwd-copy-hint">点击复制</span>
            </div>

            <div class="pwd-settings">
              <div class="pwd-row">
                <label>长度 ({{ pwdLength }})</label>
                <input type="range" v-model="pwdLength" min="4" max="32" class="pwd-slider" />
              </div>
              <div class="pwd-options">
                <label class="cyber-checkbox"><input type="checkbox" v-model="pwdUpper" /> <span>大写字母</span></label>
                <label class="cyber-checkbox"><input type="checkbox" v-model="pwdLower" /> <span>小写字母</span></label>
                <label class="cyber-checkbox"><input type="checkbox" v-model="pwdNum" /> <span>数字</span></label>
                <label class="cyber-checkbox"><input type="checkbox" v-model="pwdSym" /> <span>特殊符号</span></label>
              </div>
            </div>

            <button class="cyber-btn-outline pwd-gen-btn" @click="generatePwd">生成密码</button>
          </div>

          <!-- ================= 5. Base64 Encode/Decode ================= -->
          <div v-if="activeTool === 'base64'" class="tool-modal glass-ui-modal">
            <button class="close-btn" @click="closeTool">✕</button>
            <h2 class="modal-title">🔤 Base64 工具</h2>

            <div class="b64-modes">
              <span class="b64-mode-btn" :class="{ active: base64Mode === 'encode' }" @click="base64Mode = 'encode'; processBase64()">加密</span>
              <span class="b64-mode-btn" :class="{ active: base64Mode === 'decode' }" @click="base64Mode = 'decode'; processBase64()">解密</span>
            </div>

            <textarea class="cyber-textarea b64-input" v-model="base64Input" @input="processBase64" placeholder="输入待处理内容..."></textarea>
            <div class="b64-arrow">↓</div>
            <textarea class="cyber-textarea b64-output" v-model="base64Output" readonly placeholder="输出结果..."></textarea>
          </div>

        </div>
      </transition>
    </teleport>
  </div>
</template>

<style scoped>
/* ================= Base Styles & Theme Colors ================= */
.floating-widget {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1000;
}

/* Glass UI Mixin */
.glass-ui {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 15px rgba(102, 217, 255, 0.2);
  border-radius: 16px;
  color: #fff;
  transition: all 0.3s ease;
}

/* Widget Trigger Button */
.widget-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2rem;
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.3) 0%, rgba(252, 162, 229, 0.3) 100%);
}
.widget-btn:hover, .widget-btn.is-active {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(252, 162, 229, 0.4);
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.5) 0%, rgba(252, 162, 229, 0.5) 100%);
}

/* Widget Menu */
.widget-menu {
  position: absolute;
  top: 50px;
  left: 0;
  display: flex;
  flex-direction: column;
  padding: 8px 0;
  width: 150px;
  overflow: hidden;
}
.menu-item {
  padding: 12px 16px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}
.menu-item:hover {
  background: rgba(102, 217, 255, 0.3);
}
.item-icon {
  font-size: 1.2rem;
}

/* Transitions */
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from, .fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.3s;
}
.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}

/* ================= Modal Overlay ================= */
.tool-overlay {
  position: fixed;
  inset: 0;
  background: rgba(10, 15, 30, 0.4);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.glass-ui-modal {
  background: linear-gradient(135deg, rgba(30, 40, 60, 0.6) 0%, rgba(50, 30, 50, 0.6) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(102, 217, 255, 0.3);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3), inset 0 0 20px rgba(252, 162, 229, 0.1);
  border-radius: 24px;
  color: #fff;
  width: 100%;
  max-width: 400px;
  padding: 30px 20px;
  position: relative;
  text-align: center;
  animation: zoomIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes zoomIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background: transparent;
  border: none;
  color: rgba(255,255,255,0.6);
  font-size: 1.2rem;
  cursor: pointer;
  transition: color 0.2s;
}
.close-btn:hover {
  color: #fca2e5;
}

.modal-title {
  font-size: 1.4rem;
  font-weight: 800;
  margin-bottom: 24px;
  background: linear-gradient(to right, #66d9ff, #fca2e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* ================= Reaction Test ================= */
.react-area {
  width: 100%;
  height: 200px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 1.2rem;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  transition: background 0.1s;
  white-space: pre-wrap;
  user-select: none;
  touch-action: manipulation;
}

.bg-idle { background: rgba(102, 217, 255, 0.6); box-shadow: 0 0 15px rgba(102, 217, 255, 0.3); }
.bg-waiting { background: rgba(252, 162, 229, 0.6); box-shadow: 0 0 15px rgba(252, 162, 229, 0.3); }
.bg-click { background: rgba(80, 227, 194, 0.8); box-shadow: 0 0 20px rgba(80, 227, 194, 0.5); }
.bg-early { background: rgba(243, 156, 18, 0.7); }
.bg-result { background: rgba(102, 217, 255, 0.4); }

.eval-text {
  margin-top: 16px;
  font-size: 1.2rem;
  font-weight: 800;
  color: #fca2e5;
}
.bounce-in {
  animation: bounceIn 0.5s;
}
@keyframes bounceIn {
  0% { transform: scale(0.8); opacity: 0; }
  60% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); }
}

/* ================= CPS Test ================= */
.cps-modes {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}
.cps-mode-btn {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  background: rgba(255,255,255,0.1);
  cursor: pointer;
  transition: all 0.2s;
}
.cps-mode-btn.active {
  background: linear-gradient(135deg, #66d9ff, #fca2e5);
  box-shadow: 0 2px 10px rgba(102, 217, 255, 0.4);
}

.cps-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
  background: rgba(0,0,0,0.2);
  border-radius: 12px;
  padding: 10px;
}
.stat {
  display: flex;
  flex-direction: column;
}
.stat-label {
  font-size: 0.8rem;
  color: rgba(255,255,255,0.6);
}
.stat-val {
  font-size: 1.4rem;
  font-weight: 800;
  color: #66d9ff;
}

.cps-area {
  width: 100%;
  height: 180px;
  background: rgba(255,255,255,0.05);
  border: 2px dashed rgba(252, 162, 229, 0.5);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  touch-action: manipulation;
  transition: all 0.1s;
}
.cps-area:active {
  transform: scale(0.96);
  background: rgba(252, 162, 229, 0.2);
}
.cps-msg {
  font-size: 1.2rem;
  font-weight: 700;
  color: #fff;
  pointer-events: none;
}
.cps-count {
  font-size: 3rem;
  color: #fca2e5;
  text-shadow: 0 0 10px rgba(252, 162, 229, 0.5);
}

/* ================= Pomodoro ================= */
.pomo-settings {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}
.pomo-input-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.pomo-input-group label {
  font-size: 0.8rem;
  color: rgba(255,255,255,0.6);
}
.pomo-input-group input {
  width: 60px;
  padding: 4px;
  border-radius: 8px;
  border: 1px solid rgba(102, 217, 255, 0.3);
  background: rgba(0,0,0,0.3);
  color: #fff;
  text-align: center;
  font-weight: 600;
}

.pomo-display {
  margin-bottom: 30px;
}
.pomo-mode-label {
  font-size: 1rem;
  font-weight: 600;
  color: #fca2e5;
  margin-bottom: 8px;
}
.pomo-time {
  font-size: 4rem;
  font-weight: 900;
  letter-spacing: 2px;
  color: #66d9ff;
  text-shadow: 0 0 15px rgba(102, 217, 255, 0.4);
  font-family: monospace;
}
.pomo-count {
  font-size: 0.9rem;
  color: rgba(255,255,255,0.5);
  margin-top: 8px;
}

.pomo-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
}
.pomo-btn {
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 1rem;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}
.play-btn {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
  box-shadow: 0 4px 10px rgba(102, 217, 255, 0.3);
}
.play-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(102, 217, 255, 0.5);
}
.reset-btn {
  background: rgba(255,255,255,0.1);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.3);
}
.reset-btn:hover {
  background: rgba(255,255,255,0.2);
}

/* ================= Password Generator ================= */
.pwd-result-box {
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(102, 217, 255, 0.4);
  padding: 15px;
  border-radius: 12px;
  font-family: monospace;
  font-size: 1.2rem;
  margin-bottom: 20px;
  cursor: pointer;
  position: relative;
  word-break: break-all;
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.pwd-result-box:hover {
  background: rgba(102, 217, 255, 0.1);
}
.pwd-copy-hint {
  position: absolute;
  right: 10px;
  bottom: -20px;
  font-size: 0.8rem;
  color: #50e3c2;
  opacity: 0;
  transition: opacity 0.2s;
}
.pwd-result-box:hover .pwd-copy-hint {
  opacity: 1;
}

.pwd-settings {
  text-align: left;
  margin-bottom: 20px;
}
.pwd-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.pwd-slider {
  width: 60%;
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
  font-size: 0.9rem;
}
.cyber-checkbox input {
  accent-color: #fca2e5;
  width: 16px;
  height: 16px;
}

.pwd-gen-btn {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #66d9ff, #fca2e5);
  color: #fff;
  font-weight: bold;
  border: none;
  cursor: pointer;
  transition: transform 0.2s;
}
.pwd-gen-btn:hover {
  transform: translateY(-2px);
}

/* ================= Base64 ================= */
.b64-modes {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 15px;
}
.b64-mode-btn {
  padding: 6px 20px;
  border-radius: 20px;
  background: rgba(255,255,255,0.1);
  cursor: pointer;
  transition: all 0.2s;
}
.b64-mode-btn.active {
  background: linear-gradient(135deg, #66d9ff, #fca2e5);
  box-shadow: 0 2px 10px rgba(102, 217, 255, 0.4);
}
.cyber-textarea {
  width: 100%;
  height: 100px;
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(102, 217, 255, 0.3);
  border-radius: 12px;
  padding: 10px;
  color: #fff;
  font-family: monospace;
  resize: none;
}
.cyber-textarea:focus {
  outline: none;
  border-color: #fca2e5;
}
.b64-arrow {
  margin: 10px 0;
  font-size: 1.5rem;
  color: #66d9ff;
}

/* ================= Mobile Adjustments ================= */
@media (max-width: 480px) {
  .widget-btn {
    width: 36px;
    height: 36px;
    font-size: 1.1rem;
  }
  .glass-ui-modal {
    padding: 20px 15px;
  }
  .pomo-time {
    font-size: 3.5rem;
  }
}
</style>
