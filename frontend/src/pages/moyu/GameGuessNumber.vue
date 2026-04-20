<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// ─── 游戏状态 ────────────────────────────────────────────────────────────────
type Difficulty = 'easy' | 'normal' | 'hard'

const diffMap: Record<Difficulty, { range: number; maxTries: number; label: string }> = {
  easy:   { range: 50,  maxTries: 10, label: '简单 (1-50)' },
  normal: { range: 100, maxTries: 8,  label: '普通 (1-100)' },
  hard:   { range: 200, maxTries: 6,  label: '困难 (1-200)' },
}

const difficulty = ref<Difficulty>('normal')
const secret     = ref(0)
const inputVal   = ref('')
const history    = ref<{ num: number; hint: string; idx: number }[]>([])
const gameState  = ref<'idle' | 'playing' | 'win' | 'lose'>('idle')
const tries      = ref(0)
const bestScore  = ref<Record<Difficulty, number | null>>({ easy: null, normal: null, hard: null })

const maxTries = computed(() => diffMap[difficulty.value].maxTries)
const triesLeft = computed(() => maxTries.value - tries.value)

function startGame() {
  const { range } = diffMap[difficulty.value]
  secret.value = Math.floor(Math.random() * range) + 1
  history.value = []
  tries.value = 0
  inputVal.value = ''
  gameState.value = 'playing'
}

function guess() {
  const num = parseInt(inputVal.value)
  const { range } = diffMap[difficulty.value]
  if (isNaN(num) || num < 1 || num > range) return

  tries.value++
  let hint: string
  const diff = Math.abs(num - secret.value)

  if (num === secret.value) {
    hint = '🎯 正确！'
    gameState.value = 'win'
    const prev = bestScore.value[difficulty.value]
    if (prev === null || tries.value < prev) bestScore.value[difficulty.value] = tries.value
  } else if (tries.value >= maxTries.value) {
    hint = num < secret.value ? '⬆️ 偏小' : '⬇️ 偏大'
    gameState.value = 'lose'
  } else {
    if (diff <= 3) hint = num < secret.value ? '🔥 非常接近！偏小' : '🔥 非常接近！偏大'
    else if (diff <= 10) hint = num < secret.value ? '🌡️ 有点热，偏小' : '🌡️ 有点热，偏大'
    else hint = num < secret.value ? '❄️ 偏小' : '❄️ 偏大'
  }

  history.value.unshift({ num, hint, idx: tries.value })
  inputVal.value = ''
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Enter') guess()
}
</script>

<template>
  <div class="guess-page">
    <!-- 动态光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>

    <div class="back-btn">
      <button type="button" class="site-pill site-pill--active" @click="router.push('/moyu')">
        <span aria-hidden="true">←</span>
        <span>摸鱼中心</span>
      </button>
    </div>

    <div class="header">
      <div class="header-icon">🎲</div>
      <h1 class="title">猜数字</h1>
      <p class="subtitle">脑力挑战 · 越少次数越厉害</p>
    </div>

    <!-- 空闲/选难度 -->
    <div v-if="gameState === 'idle'" class="idle-card glass-surface">
      <p class="idle-hint">选择难度开始挑战</p>
      <div class="diff-btns">
        <button
          v-for="(cfg, key) in diffMap"
          :key="key"
          class="diff-btn"
          :class="{ active: difficulty === key }"
          @click="difficulty = (key as Difficulty)"
        >
          <span class="diff-icon">{{ key === 'easy' ? '🌱' : key === 'normal' ? '⚡' : '🔥' }}</span>
          {{ cfg.label }}
        </button>
      </div>
      <button class="start-btn" @click="startGame">
        <span>🚀</span> 开始挑战
      </button>
    </div>

    <!-- 游戏中 -->
    <div v-else class="game-area">
      <!-- 状态栏 -->
      <div class="status-bar glass-surface">
        <div class="stat">
          <span class="stat-icon">📏</span>
          <span class="stat-v">{{ diffMap[difficulty].range }}</span>
          <span class="stat-l">范围</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat" :class="{ 'stat--alert': triesLeft <= 2 }">
          <span class="stat-icon">🎯</span>
          <span class="stat-v">{{ triesLeft }}</span>
          <span class="stat-l">剩余</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat">
          <span class="stat-icon">🏆</span>
          <span class="stat-v">{{ bestScore[difficulty] ?? '—' }}</span>
          <span class="stat-l">最佳</span>
        </div>
      </div>

      <!-- 剩余次数进度条 -->
      <div class="tries-progress">
        <div
          class="tries-bar"
          :style="{ width: (triesLeft / maxTries * 100) + '%', '--tries-color': triesLeft <= 2 ? '#ef4444' : triesLeft <= 4 ? '#f59e0b' : '#10b981' }"
        ></div>
      </div>

      <!-- 结果提示 -->
      <transition name="result-pop">
        <div v-if="gameState === 'win'" class="result-banner win">
          <span class="result-icon">🎉</span>
          <div class="result-text">
            <strong>恭喜猜中！</strong>
            <span>用了 <strong>{{ tries }}</strong> 次找到了 <strong>{{ secret }}</strong></span>
          </div>
        </div>
        <div v-else-if="gameState === 'lose'" class="result-banner lose">
          <span class="result-icon">😢</span>
          <div class="result-text">
            <strong>很遗憾，正确答案是 <span class="answer">{{ secret }}</span></strong>
            <span>再试一次吧！</span>
          </div>
        </div>
      </transition>

      <!-- 输入区 -->
      <div v-if="gameState === 'playing'" class="input-area">
        <input
          v-model="inputVal"
          type="number"
          class="guess-input"
          :min="1"
          :max="diffMap[difficulty].range"
          :placeholder="`输入 1 ~ ${diffMap[difficulty].range}`"
          @keydown="onKey"
          autofocus
        />
        <button class="guess-btn" @click="guess">
          <span>🔍</span> 猜
        </button>
      </div>

      <!-- 历史记录 -->
      <div class="history" v-if="history.length">
        <div
          v-for="(h, i) in history"
          :key="h.idx"
          class="h-row"
          :class="{ 'h-row--win': h.hint.includes('正确'), 'h-row--close': h.hint.includes('热'), 'h-row--far': h.hint.includes('❄') }"
          :style="{ '--delay': i * 0.05 + 's' }"
        >
          <span class="h-idx">#{{ h.idx }}</span>
          <span class="h-num">{{ h.num }}</span>
          <span class="h-hint">{{ h.hint }}</span>
        </div>
      </div>

      <!-- 重来 -->
      <div class="bottom-btns">
        <button class="restart-btn" @click="startGame">
          <span>🔄</span> 再玩一局
        </button>
        <button class="restart-btn" @click="gameState = 'idle'">
          <span>🎮</span> 换难度
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.guess-page {
  box-sizing: border-box;
  max-width: 480px;
  margin: 0 auto;
  padding: max(16px, env(safe-area-inset-top)) 16px max(32px, calc(env(safe-area-inset-bottom) + 12px));
  animation: fadeIn 0.4s ease-out;
  position: relative;
  overflow: hidden;
}

/* 光球背景 */
.orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.15;
  pointer-events: none;
  z-index: 0;
  animation: orbFloat 20s ease-in-out infinite;
}
.orb-1 {
  width: 320px; height: 320px;
  background: #10b981;
  top: -60px; right: -60px;
  animation-duration: 18s;
}
.orb-2 {
  width: 260px; height: 260px;
  background: #f59e0b;
  bottom: 20%; left: -50px;
  animation-duration: 22s;
  animation-delay: -8s;
}

.back-btn { margin-bottom: 12px; position: relative; z-index: 1; }
.header {
  text-align: center;
  margin-bottom: 28px;
  position: relative;
  z-index: 1;
}
.header-icon {
  font-size: 3rem;
  margin-bottom: 8px;
  display: inline-block;
  animation: iconFloat 3s ease-in-out infinite;
}
.title {
  font-size: clamp(1.6rem, 6vw, 2.2rem);
  background: linear-gradient(135deg, #10b981, #60a5fa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
  font-weight: 800;
}
.subtitle { font-size: 0.85rem; color: var(--text-color); opacity: 0.65; letter-spacing: 0.3px; }

.game-area { position: relative; z-index: 1; }

/* 空闲卡 */
.idle-card {
  padding: 32px 24px;
  border-radius: 24px;
  text-align: center;
  animation: fadeUp 0.5s ease-out;
}
.idle-hint {
  font-size: 1.05rem;
  color: var(--text-color);
  margin-bottom: 22px;
  opacity: 0.85;
}
.diff-btns {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 28px;
}
@media (min-width: 400px) { .diff-btns { flex-direction: row; justify-content: center; } }
.diff-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 14px;
  border: 2px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.05);
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 0.9rem;
}
.diff-icon { font-size: 1.1rem; }
.diff-btn.active {
  border-color: #10b981;
  background: linear-gradient(135deg, rgba(16,185,129,0.15), rgba(96,165,250,0.1));
  color: #10b981;
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(16,185,129,0.2);
}
.diff-btn:hover:not(.active) {
  border-color: rgba(16,185,129,0.4);
  background: rgba(16,185,129,0.08);
  transform: translateY(-2px);
}
.start-btn {
  padding: 14px 40px;
  border-radius: 16px;
  background: linear-gradient(135deg, #10b981, #60a5fa);
  color: #fff;
  border: none;
  font-weight: 800;
  font-size: 1.05rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 6px 24px rgba(16,185,129,0.3);
}
.start-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 32px rgba(16,185,129,0.45);
}
.start-btn:active { transform: translateY(0) scale(0.97); }

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 14px 8px;
  border-radius: 18px;
  margin-bottom: 10px;
  animation: fadeUp 0.4s ease-out;
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.stat-icon { font-size: 0.85rem; margin-bottom: 2px; }
.stat-v {
  font-size: clamp(1.1rem, 3vw, 1.5rem);
  font-weight: 800;
  color: var(--text-color);
  font-variant-numeric: tabular-nums;
}
.stat--alert .stat-v { color: #ef4444; animation: alertPulse 0.8s ease-in-out infinite; }
.stat.l { font-size: 0.68rem; color: var(--text-color); opacity: 0.5; font-weight: 500; }
.stat-divider {
  width: 1px;
  height: 36px;
  background: rgba(255,255,255,0.1);
}

/* 剩余次数进度条 */
.tries-progress {
  height: 4px;
  background: rgba(255,255,255,0.08);
  border-radius: 2px;
  margin-bottom: 14px;
  overflow: hidden;
}
.tries-bar {
  height: 100%;
  border-radius: 2px;
  background: var(--tries-color, #10b981);
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1), background 0.3s;
  position: relative;
}
.tries-bar::after {
  content: '';
  position: absolute;
  right: 0;
  top: -2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--tries-color, #10b981);
  box-shadow: 0 0 8px var(--tries-color, #10b981);
}

/* 结果 */
.result-pop-enter-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.result-pop-enter-from { opacity: 0; transform: translateY(-10px) scale(0.95); }
.result-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-radius: 16px;
  text-align: left;
  margin-bottom: 14px;
  animation: resultPop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.result-icon { font-size: 2rem; flex-shrink: 0; }
.win-banner {
  background: linear-gradient(135deg, rgba(16,185,129,0.15), rgba(16,185,129,0.05));
  border: 1px solid rgba(16,185,129,0.3);
  color: #10b981;
  box-shadow: 0 4px 20px rgba(16,185,129,0.15);
}
.lose-banner {
  background: linear-gradient(135deg, rgba(239,68,68,0.12), rgba(239,68,68,0.04));
  border: 1px solid rgba(239,68,68,0.25);
  color: #ef4444;
  box-shadow: 0 4px 20px rgba(239,68,68,0.1);
}
.result-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.result-text strong { font-size: 0.95rem; }
.result-text span { font-size: 0.82rem; opacity: 0.8; }
.answer { font-size: 1.1rem; font-weight: 800; }

/* 输入 */
.input-area {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.guess-input {
  flex: 1;
  padding: 14px 16px;
  border-radius: 14px;
  border: 2px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.07);
  color: var(--text-color);
  font-size: 1.1rem;
  outline: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  -moz-appearance: textfield;
  font-weight: 600;
}
.guess-input::-webkit-outer-spin-button,
.guess-input::-webkit-inner-spin-button { -webkit-appearance: none; }
.guess-input:focus {
  border-color: rgba(16,185,129,0.6);
  background: rgba(16,185,129,0.06);
  box-shadow: 0 0 0 3px rgba(16,185,129,0.1);
}
.guess-btn {
  padding: 14px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #10b981, #60a5fa);
  color: #fff;
  border: none;
  font-weight: 800;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 4px 16px rgba(16,185,129,0.25);
}
.guess-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(16,185,129,0.4);
}
.guess-btn:active { transform: translateY(0) scale(0.97); }

/* 历史 */
.history { display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
.h-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.08);
  animation: slideInRow 0.3s ease-out both;
  animation-delay: var(--delay, 0s);
  transition: all 0.3s ease;
}
.h-row--win { border-color: rgba(16,185,129,0.4); background: rgba(16,185,129,0.1); }
.h-row--close { border-color: rgba(245,158,11,0.3); background: rgba(245,158,11,0.06); }
.h-row--far { border-color: rgba(59,130,246,0.2); background: rgba(59,130,246,0.04); }
.h-idx {
  width: 28px;
  font-size: 0.72rem;
  color: var(--text-color);
  opacity: 0.4;
  font-weight: 600;
  flex-shrink: 0;
}
.h-num {
  width: 50px;
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--text-color);
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}
.h-row--win .h-num { color: #10b981; }
.h-row--close .h-num { color: #f59e0b; }
.h-hint { flex: 1; font-size: 0.88rem; color: var(--text-color); opacity: 0.85; }

.bottom-btns { display: flex; gap: 10px; position: relative; z-index: 1; }
.restart-btn {
  flex: 1;
  padding: 12px;
  border-radius: 14px;
  border: 1.5px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.06);
  color: var(--text-color);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.restart-btn:hover {
  border-color: rgba(16,185,129,0.5);
  background: rgba(16,185,129,0.08);
  transform: translateY(-2px);
}
.restart-btn:active { transform: translateY(0) scale(0.97); }

/* 动画 */
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
@keyframes slideInRow { from { opacity: 0; transform: translateX(-12px); } to { opacity: 1; transform: translateX(0); } }
@keyframes resultPop { from { opacity: 0; transform: translateY(-8px) scale(0.97); } }
@keyframes alertPulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.6; } }
@keyframes iconFloat { 0%, 100% { transform: translateY(0) rotate(0); } 50% { transform: translateY(-6px) rotate(5deg); } }
@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(20px, -25px); }
  66% { transform: translate(-10px, 15px); }
}
</style>
