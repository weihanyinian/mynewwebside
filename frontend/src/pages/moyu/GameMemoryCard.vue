<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import BackToBlogButton from '../../components/BackToBlogButton.vue'

// ─── 牌组配置 ────────────────────────────────────────────────────────────────
const EMOJIS_ALL = ['🌸','🎸','🦊','🐉','🍭','🎯','🌊','🦋','🍓','🎃','🌈','⭐','🎹','🎨','🚀','🦄','🍀','🎵']

type Difficulty = 'easy' | 'normal' | 'hard'
const diffMap: Record<Difficulty, { pairs: number; label: string; cols: number }> = {
  easy:   { pairs: 6,  label: '简单 6对', cols: 4 },
  normal: { pairs: 10, label: '普通 10对', cols: 5 },
  hard:   { pairs: 16, label: '困难 16对', cols: 6 },
}

interface Card { id: number; emoji: string; flipped: boolean; matched: boolean }

// ─── 状态 ─────────────────────────────────────────────────────────────────────
const difficulty = ref<Difficulty>('normal')
const cards      = ref<Card[]>([])
const selected   = ref<number[]>([])
const moves      = ref(0)
const matchedCnt = ref(0)
const gameState  = ref<'idle' | 'playing' | 'win'>('idle')
const elapsed    = ref(0)
const bestScore  = ref<Record<Difficulty, number | null>>({ easy: null, normal: null, hard: null })
let timer: ReturnType<typeof setInterval> | null = null
let lockBoard = false

function shuffle<T>(arr: T[]): T[] {
  const a = [...arr]; for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random()*(i+1));[a[i],a[j]]=[a[j],a[i]] } return a
}

function startGame() {
  if (timer) clearInterval(timer)
  const { pairs } = diffMap[difficulty.value]
  const emojis = shuffle(EMOJIS_ALL).slice(0, pairs)
  const deck = shuffle([...emojis, ...emojis]).map((e, i) => ({ id: i, emoji: e, flipped: false, matched: false }))
  cards.value = deck
  selected.value = []
  moves.value = 0
  matchedCnt.value = 0
  elapsed.value = 0
  gameState.value = 'playing'
  lockBoard = false
  timer = setInterval(() => elapsed.value++, 1000)
}

function flip(id: number) {
  if (lockBoard || gameState.value !== 'playing') return
  const card = cards.value[id]
  if (card.flipped || card.matched || selected.value.includes(id)) return

  card.flipped = true
  selected.value.push(id)

  if (selected.value.length === 2) {
    moves.value++
    lockBoard = true
    const [a, b] = selected.value
    const ca = cards.value[a], cb = cards.value[b]
    if (ca.emoji === cb.emoji) {
      ca.matched = cb.matched = true
      matchedCnt.value++
      selected.value = []
      lockBoard = false
      if (matchedCnt.value === diffMap[difficulty.value].pairs) {
        gameState.value = 'win'
        clearInterval(timer!)
        const prev = bestScore.value[difficulty.value]
        if (prev === null || elapsed.value < prev) bestScore.value[difficulty.value] = elapsed.value
      }
    } else {
      setTimeout(() => {
        ca.flipped = cb.flipped = false
        selected.value = []
        lockBoard = false
      }, 900)
    }
  }
}

const formatTime = (s: number) => `${String(Math.floor(s/60)).padStart(2,'0')}:${String(s%60).padStart(2,'0')}`
const cols = computed(() => diffMap[difficulty.value].cols)

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<template>
  <div class="memory-page">
    <div class="back-btn"><BackToBlogButton /></div>
    <div class="header">
      <h1 class="title">🃏 记忆翻牌</h1>
      <p class="subtitle">找到所有匹配对 · 记忆力挑战</p>
    </div>

    <!-- 空闲 -->
    <div v-if="gameState === 'idle'" class="idle-card glass-surface">
      <p class="idle-hint">选择难度</p>
      <div class="diff-row">
        <button v-for="(cfg, key) in diffMap" :key="key"
          class="diff-btn" :class="{ active: difficulty === key }"
          @click="difficulty = (key as Difficulty)">{{ cfg.label }}</button>
      </div>
      <button class="start-btn" @click="startGame">开始游戏 🎴</button>
    </div>

    <!-- 游戏 -->
    <div v-else>
      <!-- 状态栏 -->
      <div class="stats glass-surface">
        <div class="stat"><span class="v">{{ formatTime(elapsed) }}</span><span class="l">用时</span></div>
        <div class="stat"><span class="v">{{ moves }}</span><span class="l">翻牌次数</span></div>
        <div class="stat"><span class="v">{{ matchedCnt }}/{{ diffMap[difficulty].pairs }}</span><span class="l">已匹配</span></div>
        <div class="stat"><span class="v">{{ bestScore[difficulty] ? formatTime(bestScore[difficulty]!) : '—' }}</span><span class="l">最佳</span></div>
      </div>

      <!-- 胜利 -->
      <div v-if="gameState === 'win'" class="win-banner">
        🎉 全部匹配！用时 <strong>{{ formatTime(elapsed) }}</strong>，翻了 <strong>{{ moves }}</strong> 次！
      </div>

      <!-- 牌格 -->
      <div class="board" :style="{ '--cols': cols }">
        <div
          v-for="card in cards"
          :key="card.id"
          class="card"
          :class="{ flipped: card.flipped || card.matched, matched: card.matched }"
          @click="flip(card.id)"
        >
          <div class="card-inner">
            <div class="card-back">❓</div>
            <div class="card-front">{{ card.emoji }}</div>
          </div>
        </div>
      </div>

      <div class="bottom-btns">
        <button class="restart-btn" @click="startGame">🔄 重来</button>
        <button class="restart-btn" @click="gameState = 'idle'">🎮 换难度</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.memory-page {
  box-sizing: border-box;
  max-width: 600px;
  margin: 0 auto;
  padding: max(16px, env(safe-area-inset-top)) 14px max(32px, calc(env(safe-area-inset-bottom) + 12px));
  animation: fadeIn 0.4s ease-out;
}
.back-btn { margin-bottom: 12px; }
.header { text-align: center; margin-bottom: 20px; }
.title { font-size: clamp(1.5rem, 5vw, 2rem); color: var(--primary-color, #66d9ff); margin-bottom: 6px; }
.subtitle { font-size: 0.85rem; color: var(--text-color); opacity: 0.7; }

/* 空闲 */
.idle-card { padding: 28px; border-radius: 20px; border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.08); text-align: center; }
.idle-hint { font-size: 1rem; color: var(--text-color); margin-bottom: 18px; }
.diff-row { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; margin-bottom: 22px; }
.diff-btn { padding: 10px 18px; border-radius: 10px; border: 1.5px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.07); color: var(--text-color); cursor: pointer; transition: all 0.2s; font-size: 0.88rem; }
.diff-btn.active { border-color: var(--primary-color, #66d9ff); background: rgba(102,217,255,0.15); color: var(--primary-color, #66d9ff); font-weight: 700; }
.start-btn { padding: 12px 36px; border-radius: 12px; background: var(--primary-color, #66d9ff); color: #000; border: none; font-weight: 800; font-size: 1rem; cursor: pointer; transition: all 0.2s; }
.start-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(102,217,255,0.3); }

/* 状态栏 */
.stats { display: flex; justify-content: space-around; padding: 12px; border-radius: 14px; border: 1px solid rgba(255,255,255,0.15); background: rgba(255,255,255,0.06); margin-bottom: 14px; }
.stat { display: flex; flex-direction: column; align-items: center; gap: 3px; }
.v { font-size: 1.2rem; font-weight: 800; color: var(--primary-color, #66d9ff); }
.l { font-size: 0.68rem; color: var(--text-color); opacity: 0.55; }

/* 胜利 */
.win-banner { padding: 14px; border-radius: 12px; background: rgba(16,185,129,0.14); border: 1px solid rgba(16,185,129,0.3); color: #10b981; text-align: center; font-size: 0.95rem; margin-bottom: 14px; }

/* 牌格 */
.board {
  display: grid;
  grid-template-columns: repeat(var(--cols), 1fr);
  gap: 8px;
  margin-bottom: 18px;
}
@media (max-width: 400px) { .board { gap: 5px; } }

.card {
  aspect-ratio: 1;
  cursor: pointer;
  perspective: 600px;
}
.card-inner {
  width: 100%; height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.4s ease;
  border-radius: 10px;
}
.card.flipped .card-inner { transform: rotateY(180deg); }

.card-back, .card-front {
  position: absolute; inset: 0;
  backface-visibility: hidden;
  display: flex; align-items: center; justify-content: center;
  border-radius: 10px;
  font-size: clamp(1.2rem, 4vw, 1.8rem);
  border: 1.5px solid rgba(255,255,255,0.2);
}
.card-back {
  background: rgba(102,217,255,0.15);
  border-color: rgba(102,217,255,0.3);
  font-size: 1.4rem;
}
.card-front { background: rgba(255,255,255,0.1); transform: rotateY(180deg); }
.card.matched .card-front { background: rgba(16,185,129,0.2); border-color: #10b981; }

.bottom-btns { display: flex; gap: 10px; }
.restart-btn { flex: 1; padding: 12px; border-radius: 12px; border: 1.5px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.07); color: var(--text-color); font-size: 0.9rem; cursor: pointer; transition: all 0.2s; }
.restart-btn:hover { border-color: var(--primary-color, #66d9ff); }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>
