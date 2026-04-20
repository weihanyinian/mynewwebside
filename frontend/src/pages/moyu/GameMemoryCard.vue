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
    <!-- 动态光球 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>

    <div class="back-btn"><BackToBlogButton /></div>
    <div class="header">
      <div class="header-icon">🃏</div>
      <h1 class="title">记忆翻牌</h1>
      <p class="subtitle">找到所有匹配对 · 记忆力挑战</p>
    </div>

    <!-- 空闲 -->
    <div v-if="gameState === 'idle'" class="idle-card glass-surface">
      <p class="idle-hint">选择难度开始挑战</p>
      <div class="diff-row">
        <button
          v-for="(cfg, key) in diffMap"
          :key="key"
          class="diff-btn"
          :class="{ active: difficulty === key }"
          @click="difficulty = (key as Difficulty)"
        >
          <span class="diff-emoji">{{ key === 'easy' ? '🌱' : key === 'normal' ? '🌿' : '🔥' }}</span>
          {{ cfg.label }}
        </button>
      </div>
      <button class="start-btn" @click="startGame">
        <span>🚀</span> 开始挑战
      </button>
    </div>

    <!-- 游戏 -->
    <div v-else class="game-area">
      <!-- 状态栏 -->
      <div class="stats glass-surface">
        <div class="stat">
          <div class="stat-icon">⏱️</div>
          <span class="v">{{ formatTime(elapsed) }}</span>
          <span class="l">用时</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat">
          <div class="stat-icon">🔄</div>
          <span class="v">{{ moves }}</span>
          <span class="l">翻牌</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat">
          <div class="stat-icon">✨</div>
          <span class="v matched-count">{{ matchedCnt }}/{{ diffMap[difficulty].pairs }}</span>
          <span class="l">已配</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat">
          <div class="stat-icon">🏆</div>
          <span class="v">{{ bestScore[difficulty] ? formatTime(bestScore[difficulty]!) : '—' }}</span>
          <span class="l">最佳</span>
        </div>
      </div>

      <!-- 进度条 -->
      <div class="match-progress">
        <div class="match-bar" :style="{ width: (matchedCnt / diffMap[difficulty].pairs * 100) + '%' }"></div>
      </div>

      <!-- 胜利 -->
      <transition name="win-pop">
        <div v-if="gameState === 'win'" class="win-banner">
          <div class="win-icon">🎉</div>
          <div class="win-text">
            <strong>全部匹配成功！</strong>
            <span>用时 {{ formatTime(elapsed) }}，翻了 {{ moves }} 次</span>
          </div>
        </div>
      </transition>

      <!-- 牌格 -->
      <div class="board" :class="{ 'board--shuffling': gameState === 'playing' && matchedCnt === 0 }" :style="{ '--cols': cols }">
        <div
          v-for="(card, idx) in cards"
          :key="card.id"
          class="card"
          :class="{
            flipped: card.flipped || card.matched,
            matched: card.matched
          }"
          :style="{ '--idx': idx }"
          @click="flip(card.id)"
        >
          <div class="card-inner">
            <div class="card-back">
              <span class="card-back-icon">?</span>
            </div>
            <div class="card-front">
              <span class="card-emoji">{{ card.emoji }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="bottom-btns">
        <button class="restart-btn" @click="startGame">
          <span>🔄</span> 重来
        </button>
        <button class="restart-btn" @click="gameState = 'idle'">
          <span>🎮</span> 换难度
        </button>
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
  width: 350px; height: 350px;
  background: #f472b6;
  top: 10%; left: -80px;
  animation-duration: 18s;
}
.orb-2 {
  width: 280px; height: 280px;
  background: #66d9ff;
  bottom: 10%; right: -60px;
  animation-duration: 22s;
  animation-delay: -9s;
}

.back-btn { margin-bottom: 12px; position: relative; z-index: 1; }
.header {
  text-align: center;
  margin-bottom: 24px;
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
  font-size: clamp(1.6rem, 5vw, 2.2rem);
  background: linear-gradient(135deg, #f472b6, #66d9ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
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
.diff-row {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 28px;
}
.diff-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 14px;
  border: 2px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.05);
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 0.9rem;
}
.diff-emoji { font-size: 1.1rem; }
.diff-btn.active {
  border-color: #f472b6;
  background: linear-gradient(135deg, rgba(244,114,182,0.15), rgba(102,217,255,0.1));
  color: #f472b6;
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(244,114,182,0.2);
}
.diff-btn:hover:not(.active) {
  border-color: rgba(244,114,182,0.4);
  background: rgba(244,114,182,0.08);
  transform: translateY(-2px);
}
.start-btn {
  padding: 14px 40px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f472b6, #66d9ff);
  color: #fff;
  border: none;
  font-weight: 800;
  font-size: 1.05rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 6px 24px rgba(244,114,182,0.3);
}
.start-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 32px rgba(244,114,182,0.45);
}
.start-btn:active { transform: translateY(0) scale(0.97); }

/* 状态栏 */
.stats {
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
.v {
  font-size: clamp(1rem, 3vw, 1.3rem);
  font-weight: 800;
  color: #f472b6;
  font-variant-numeric: tabular-nums;
}
.matched-count { color: #10b981; }
.l { font-size: 0.65rem; color: var(--text-color); opacity: 0.5; font-weight: 500; }
.stat-divider {
  width: 1px;
  height: 32px;
  background: rgba(255,255,255,0.1);
}

/* 进度条 */
.match-progress {
  height: 4px;
  background: rgba(255,255,255,0.1);
  border-radius: 2px;
  margin-bottom: 14px;
  overflow: hidden;
}
.match-bar {
  height: 100%;
  background: linear-gradient(90deg, #f472b6, #10b981);
  border-radius: 2px;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}
.match-bar::after {
  content: '';
  position: absolute;
  right: 0;
  top: -2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
}

/* 胜利 */
.win-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(16,185,129,0.15), rgba(16,185,129,0.05));
  border: 1px solid rgba(16,185,129,0.3);
  color: #10b981;
  text-align: left;
  margin-bottom: 14px;
  box-shadow: 0 4px 20px rgba(16,185,129,0.15);
}
.win-icon { font-size: 2rem; animation: winBounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1); }
.win-text { display: flex; flex-direction: column; gap: 2px; }
.win-text strong { font-size: 1rem; }
.win-text span { font-size: 0.82rem; opacity: 0.8; }
.win-pop-enter-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.win-pop-enter-from { opacity: 0; transform: scale(0.8) translateY(-10px); }

/* 牌格 */
.board {
  display: grid;
  grid-template-columns: repeat(var(--cols), 1fr);
  gap: 8px;
  margin-bottom: 18px;
}
.board--shuffling .card { animation: cardShuffle 0.5s ease-out both; }
.board--shuffling .card:nth-child(2n) { animation-delay: 0.05s; }
.board--shuffling .card:nth-child(3n) { animation-delay: 0.1s; }
@media (max-width: 400px) { .board { gap: 5px; } }

.card {
  aspect-ratio: 1;
  cursor: pointer;
  perspective: 700px;
  animation: cardSlideIn 0.4s ease-out both;
  animation-delay: calc(var(--idx, 0) * 0.02s);
}
.card-inner {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
}
.card.flipped .card-inner { transform: rotateY(180deg); }
.card.matched .card-inner { transform: rotateY(180deg); }

.card-back, .card-front {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  border: 2px solid rgba(255,255,255,0.15);
  transition: all 0.3s ease;
}
.card-back {
  background: linear-gradient(135deg, rgba(244,114,182,0.2), rgba(102,217,255,0.15));
  border-color: rgba(244,114,182,0.3);
}
.card-back-icon {
  font-size: clamp(1.2rem, 4vw, 1.8rem);
  font-weight: 900;
  color: #f472b6;
  opacity: 0.6;
  transition: opacity 0.2s;
}
.card:hover .card-back { border-color: rgba(244,114,182,0.5); box-shadow: 0 4px 16px rgba(244,114,182,0.15); }
.card:hover .card-back-icon { opacity: 0.9; }

.card-front {
  background: rgba(255,255,255,0.08);
  transform: rotateY(180deg);
  border-color: rgba(102,217,255,0.25);
}
.card-emoji {
  font-size: clamp(1.4rem, 5vw, 2rem);
  animation: emojiPop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.card.matched .card-front {
  background: linear-gradient(135deg, rgba(16,185,129,0.2), rgba(16,185,129,0.08));
  border-color: rgba(16,185,129,0.5);
  box-shadow: 0 0 20px rgba(16,185,129,0.2);
}
.card.pop-matched .card-inner { animation: matchPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1); }

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
  border-color: rgba(244,114,182,0.5);
  background: rgba(244,114,182,0.08);
  transform: translateY(-2px);
}
.restart-btn:active { transform: translateY(0) scale(0.97); }

/* 动画 */
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
@keyframes cardSlideIn { from { opacity: 0; transform: scale(0.8) translateY(10px); } to { opacity: 1; transform: scale(1) translateY(0); } }
@keyframes cardShuffle { 0% { transform: translateX(-20px) rotate(-5deg); opacity: 0; } 100% { transform: translateX(0) rotate(0); opacity: 1; } }
@keyframes emojiPop { from { transform: scale(0); } to { transform: scale(1); } }
@keyframes matchPop { 0% { transform: rotateY(180deg) scale(1); } 50% { transform: rotateY(180deg) scale(1.15); } 100% { transform: rotateY(180deg) scale(1); } }
@keyframes winBounce { from { transform: scale(0) rotate(-20deg); } to { transform: scale(1) rotate(0); } }
@keyframes iconFloat { 0%, 100% { transform: translateY(0) rotate(0); } 50% { transform: translateY(-6px) rotate(3deg); } }
@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(25px, -30px); }
  66% { transform: translate(-15px, 15px); }
}
</style>
