<script setup lang="ts">
import { ref, computed } from 'vue'
import BackToBlogButton from '../../components/BackToBlogButton.vue'

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
    <div class="back-btn"><BackToBlogButton /></div>

    <div class="header">
      <h1 class="title">🎲 猜数字</h1>
      <p class="subtitle">脑力挑战 · 越少次数越厉害</p>
    </div>

    <!-- 空闲/选难度 -->
    <div v-if="gameState === 'idle'" class="idle-card glass-surface">
      <p class="idle-hint">选择难度开始游戏</p>
      <div class="diff-btns">
        <button
          v-for="(cfg, key) in diffMap"
          :key="key"
          class="diff-btn"
          :class="{ active: difficulty === key }"
          @click="difficulty = (key as Difficulty)"
        >{{ cfg.label }}</button>
      </div>
      <button class="start-btn" @click="startGame">开始游戏 🚀</button>
    </div>

    <!-- 游戏中 -->
    <div v-else class="game-area">
      <!-- 状态栏 -->
      <div class="status-bar glass-surface">
        <div class="stat"><span class="stat-v">{{ diffMap[difficulty].range }}</span><span class="stat-l">范围</span></div>
        <div class="stat highlight"><span class="stat-v">{{ triesLeft }}</span><span class="stat-l">剩余次数</span></div>
        <div class="stat"><span class="stat-v">{{ bestScore[difficulty] ?? '—' }}</span><span class="stat-l">最佳</span></div>
      </div>

      <!-- 结果提示 -->
      <div v-if="gameState === 'win'" class="result-banner win">
        🎉 恭喜！用了 <strong>{{ tries }}</strong> 次猜中了 <strong>{{ secret }}</strong>！
      </div>
      <div v-else-if="gameState === 'lose'" class="result-banner lose">
        😢 很遗憾，答案是 <strong>{{ secret }}</strong>，再来一次！
      </div>

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
        <button class="guess-btn" @click="guess">猜！</button>
      </div>

      <!-- 历史记录 -->
      <div class="history" v-if="history.length">
        <div v-for="h in history" :key="h.idx" class="h-row" :class="{ first: h.idx === tries && gameState !== 'playing' }">
          <span class="h-idx">#{{ h.idx }}</span>
          <span class="h-num">{{ h.num }}</span>
          <span class="h-hint">{{ h.hint }}</span>
        </div>
      </div>

      <!-- 重来 -->
      <div class="bottom-btns">
        <button class="restart-btn" @click="startGame">🔄 再玩一局</button>
        <button class="restart-btn" @click="gameState = 'idle'">🎮 换难度</button>
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
}
.back-btn { margin-bottom: 12px; }
.header { text-align: center; margin-bottom: 24px; }
.title { font-size: clamp(1.6rem, 6vw, 2rem); color: var(--primary-color, #66d9ff); margin-bottom: 6px; }
.subtitle { font-size: 0.85rem; color: var(--text-color); opacity: 0.7; }

/* 空闲卡 */
.idle-card { padding: 28px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.08); text-align: center; }
.idle-hint { font-size: 1rem; color: var(--text-color); margin-bottom: 20px; }
.diff-btns { display: flex; flex-direction: column; gap: 10px; margin-bottom: 24px; }
@media (min-width: 400px) { .diff-btns { flex-direction: row; justify-content: center; } }
.diff-btn {
  padding: 10px 18px; border-radius: 10px;
  border: 1.5px solid rgba(255,255,255,0.25);
  background: rgba(255,255,255,0.07); color: var(--text-color); cursor: pointer; transition: all 0.2s; font-size: 0.9rem;
}
.diff-btn.active { border-color: var(--primary-color, #66d9ff); background: rgba(102,217,255,0.15); color: var(--primary-color, #66d9ff); font-weight: 700; }
.start-btn { padding: 12px 40px; border-radius: 12px; background: var(--primary-color, #66d9ff); color: #000; border: none; font-weight: 800; font-size: 1rem; cursor: pointer; transition: all 0.2s; }
.start-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(102,217,255,0.35); }

/* 状态栏 */
.status-bar { display: flex; justify-content: space-around; padding: 16px; border-radius: 16px; border: 1px solid rgba(255,255,255,0.15); background: rgba(255,255,255,0.07); margin-bottom: 16px; }
.stat { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-v { font-size: 1.5rem; font-weight: 800; color: var(--text-color); }
.stat.highlight .stat-v { color: var(--primary-color, #66d9ff); }
.stat-l { font-size: 0.72rem; color: var(--text-color); opacity: 0.6; }

/* 结果 */
.result-banner { padding: 14px; border-radius: 12px; text-align: center; font-size: 0.95rem; margin-bottom: 16px; }
.result-banner.win { background: rgba(16,185,129,0.15); border: 1px solid rgba(16,185,129,0.3); color: #10b981; }
.result-banner.lose { background: rgba(239,68,68,0.12); border: 1px solid rgba(239,68,68,0.25); color: #ef4444; }

/* 输入 */
.input-area { display: flex; gap: 10px; margin-bottom: 20px; }
.guess-input {
  flex: 1; padding: 14px 16px; border-radius: 12px;
  border: 1.5px solid rgba(255,255,255,0.2);
  background: rgba(255,255,255,0.08); color: var(--text-color);
  font-size: 1.1rem; outline: none; transition: border-color 0.2s;
  -moz-appearance: textfield;
}
.guess-input::-webkit-outer-spin-button,
.guess-input::-webkit-inner-spin-button { -webkit-appearance: none; }
.guess-input:focus { border-color: var(--primary-color, #66d9ff); }
.guess-btn { padding: 14px 22px; border-radius: 12px; background: var(--primary-color, #66d9ff); color: #000; border: none; font-weight: 800; font-size: 1rem; cursor: pointer; transition: all 0.2s; white-space: nowrap; }

/* 历史 */
.history { display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
.h-row { display: flex; align-items: center; gap: 10px; padding: 10px 14px; border-radius: 10px; background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.1); }
.h-row.first { border-color: #10b981; background: rgba(16,185,129,0.1); }
.h-idx { width: 28px; font-size: 0.75rem; color: var(--text-color); opacity: 0.5; }
.h-num { width: 50px; font-size: 1.1rem; font-weight: 700; color: var(--text-color); }
.h-hint { flex: 1; font-size: 0.88rem; color: var(--text-color); opacity: 0.85; }

.bottom-btns { display: flex; gap: 10px; }
.restart-btn { flex: 1; padding: 12px; border-radius: 12px; border: 1.5px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.07); color: var(--text-color); font-size: 0.9rem; cursor: pointer; transition: all 0.2s; }
.restart-btn:hover { border-color: var(--primary-color, #66d9ff); background: rgba(102,217,255,0.1); }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
