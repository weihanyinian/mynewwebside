<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import BackToHomeButton from '../../components/BackToHomeButton.vue'

// ─── 恋爱计时 ──────────────────────────────────────────────────────────────
// 在一起时间：2025年6月18日 23:00:00
const START_DATE = new Date('2025-06-18T23:00:00')

interface TimeElapsed {
  days: number
  hours: number
  minutes: number
  seconds: number
  totalDays: number
}

const elapsed = ref<TimeElapsed>({ days: 0, hours: 0, minutes: 0, seconds: 0, totalDays: 0 })
let timerRef: ReturnType<typeof setInterval> | null = null

function calcElapsed() {
  const now = new Date()
  const diff = now.getTime() - START_DATE.getTime()
  if (diff < 0) return
  const totalSeconds = Math.floor(diff / 1000)
  const totalDays = Math.floor(diff / (1000 * 60 * 60 * 24))
  elapsed.value = {
    days: totalDays,
    hours: Math.floor((totalSeconds % 86400) / 3600),
    minutes: Math.floor((totalSeconds % 3600) / 60),
    seconds: totalSeconds % 60,
    totalDays,
  }
}

// ─── 回忆数据（静态示例，可后续接后端）─────────────────────────────────────
interface Memory {
  id: number
  date: string
  title: string
  desc: string
  emoji: string
  tags: string[]
  color: string
}

const memories = ref<Memory[]>([
  {
    id: 1,
    date: '2025-06-18',
    title: '我们在一起了 💑',
    desc: '那个特别的夜晚，23:00，我们确认了彼此的心意。从这一刻起，世界都不一样了。',
    emoji: '💕',
    tags: ['纪念日', '开始'],
    color: '#f43f5e',
  },
  {
    id: 2,
    date: '2025-07-07',
    title: '七夕快乐 🥢',
    desc: '第一个在一起的七夕节，一起看星星，一起许愿，希望每一年都能这样度过。',
    emoji: '⭐',
    tags: ['节日', '浪漫'],
    color: '#8b5cf6',
  },
  {
    id: 3,
    date: '2025-08-15',
    title: '第一次约会 🎬',
    desc: '一起去看了期待很久的电影，散场后漫步在夜晚的街道，笑声不断。',
    emoji: '🎬',
    tags: ['约会', '电影'],
    color: '#3b82f6',
  },
  {
    id: 4,
    date: '2025-09-30',
    title: '第一次旅行 🗺️',
    desc: '一起出发去远方，看了从未见过的风景，留下了很多珍贵的照片和回忆。',
    emoji: '✈️',
    tags: ['旅行', '冒险'],
    color: '#10b981',
  },
  {
    id: 5,
    date: '2025-12-24',
    title: '圣诞平安夜 🎄',
    desc: '互换了小礼物，一起在温暖的灯光下享用晚餐，窗外飘着小雪。这一刻很美。',
    emoji: '🎁',
    tags: ['节日', '礼物'],
    color: '#f59e0b',
  },
  {
    id: 6,
    date: '2026-01-01',
    title: '新年快乐 🎊',
    desc: '跨年倒计时，一起迎接2026年。握着你的手，许下了美好的愿望。',
    emoji: '🎆',
    tags: ['新年', '跨年'],
    color: '#ec4899',
  },
])

// 按日期倒序
const sortedMemories = computed(() =>
  [...memories.value].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
)

// ─── 弹出框 ────────────────────────────────────────────────────────────────
const activeMemory = ref<Memory | null>(null)
function openMemory(m: Memory) { activeMemory.value = m }
function closeMemory() { activeMemory.value = null }

// ─── 生命周期 ──────────────────────────────────────────────────────────────
onMounted(() => {
  calcElapsed()
  timerRef = setInterval(calcElapsed, 1000)
})
onUnmounted(() => { if (timerRef) clearInterval(timerRef) })

function pad(n: number) { return String(n).padStart(2, '0') }

// 生成粒子样式
function particleStyle(i: number) {
  const size = 12 + (i % 4) * 4
  const left = 5 + (i * 7.3) % 90
  const delay = (i * 1.7) % 6
  const dur = 8 + (i % 4) * 3
  const opacity = 0.1 + (i % 3) * 0.08
  return {
    fontSize: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${dur}s`,
    opacity,
  }
}

// 格式化日期
function fmtDate(d: string) {
  const dt = new Date(d)
  return `${dt.getFullYear()} 年 ${dt.getMonth() + 1} 月 ${dt.getDate()} 日`
}
</script>

<template>
  <div class="memories-page">
    <!-- 浮动爱心粒子 -->
    <div class="particles">
      <span v-for="i in 12" :key="i" class="particle" :style="particleStyle(i)">💕</span>
    </div>

    <div class="memories-back"><BackToHomeButton /></div>

    <!-- ─── 标题 ─── -->
    <div class="page-header">
      <div class="heart-icon">
        <span class="heart-main">💝</span>
        <span class="heart-ring"></span>
      </div>
      <h1 class="page-title">我们的回忆</h1>
      <p class="page-subtitle">记录每一个珍贵的瞬间</p>
    </div>

    <!-- ─── 恋爱计时器 ─── -->
    <div class="timer-card glass-surface">
      <div class="timer-glow"></div>
      <div class="timer-label">
        <span class="timer-heart">💑</span>
        <span>在一起已经</span>
      </div>
      <div class="timer-display">
        <div class="time-unit">
          <span class="time-value days">{{ elapsed.days }}</span>
          <span class="time-label">天</span>
        </div>
        <span class="time-sep">:</span>
        <div class="time-unit">
          <span class="time-value">{{ pad(elapsed.hours) }}</span>
          <span class="time-label">时</span>
        </div>
        <span class="time-sep">:</span>
        <div class="time-unit">
          <span class="time-value">{{ pad(elapsed.minutes) }}</span>
          <span class="time-label">分</span>
        </div>
        <span class="time-sep">:</span>
        <div class="time-unit">
          <span class="time-value seconds">{{ pad(elapsed.seconds) }}</span>
          <span class="time-label">秒</span>
        </div>
      </div>
      <div class="timer-start-info">
        <span class="sparkle">✨</span>
        从 2025 年 6 月 18 日 23:00 开始
      </div>
    </div>

    <!-- ─── 回忆时间线 ─── -->
    <div class="section-title">
      <span class="section-icon">📖</span>
      我们的故事
    </div>
    <div class="timeline">
      <!-- 时间线中心线 -->
      <div class="timeline-line"></div>
      <div
        v-for="(m, idx) in sortedMemories"
        :key="m.id"
        class="timeline-item"
        :class="{ 'timeline-item--right': idx % 2 === 1 }"
        :style="{ '--item-color': m.color, '--idx': idx }"
        @click="openMemory(m)"
      >
        <div class="tl-connector">
          <div class="tl-dot" :style="{ background: m.color }">
            <span class="tl-emoji">{{ m.emoji }}</span>
          </div>
          <div class="tl-connector-line"></div>
        </div>
        <div class="memory-card glass-surface">
          <div class="memory-card-accent" :style="{ background: m.color }"></div>
          <div class="memory-card-top">
            <span class="memory-date">{{ fmtDate(m.date) }}</span>
          </div>
          <h3 class="memory-title">{{ m.title }}</h3>
          <p class="memory-desc">{{ m.desc }}</p>
          <div class="tag-row">
            <span
              v-for="t in m.tags"
              :key="t"
              class="mem-tag"
              :style="{ background: m.color + '22', borderColor: m.color + '55', color: m.color }"
            >{{ t }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ─── 弹出详情 ─── -->
    <Teleport to="body">
      <transition name="modal-fade">
        <div v-if="activeMemory" class="modal-overlay" @click.self="closeMemory">
          <div class="modal-card glass-surface" :style="{ '--mc': activeMemory.color }">
            <div class="modal-glow" :style="{ background: activeMemory.color }"></div>
            <button class="modal-close" @click="closeMemory">✕</button>
            <div class="modal-emoji">{{ activeMemory.emoji }}</div>
            <div class="modal-date">{{ fmtDate(activeMemory.date) }}</div>
            <h2 class="modal-title">{{ activeMemory.title }}</h2>
            <p class="modal-desc">{{ activeMemory.desc }}</p>
            <div class="tag-row">
              <span
                v-for="t in activeMemory.tags"
                :key="t"
                class="mem-tag"
                :style="{ background: activeMemory.color + '22', borderColor: activeMemory.color + '55', color: activeMemory.color }"
              >{{ t }}</span>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<style scoped>
.memories-page {
  box-sizing: border-box;
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  padding: max(16px, env(safe-area-inset-top)) 16px max(48px, calc(env(safe-area-inset-bottom) + 20px));
  animation: fadeIn 0.5s ease-out;
  position: relative;
  overflow: hidden;
}

/* 浮动爱心 */
.particles {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}
.particle {
  position: absolute;
  bottom: -30px;
  animation: floatUp linear infinite;
  opacity: 0.12;
}
@keyframes floatUp {
  0% { transform: translateY(0) rotate(0deg) scale(1); opacity: 0; }
  10% { opacity: 0.15; }
  90% { opacity: 0.1; }
  100% { transform: translateY(-110vh) rotate(360deg) scale(0.6); opacity: 0; }
}

.memories-back { margin-bottom: 14px; position: relative; z-index: 1; }

/* 标题 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}
.heart-icon {
  font-size: 3.5rem;
  display: inline-block;
  position: relative;
  animation: heartbeat 1.4s ease-in-out infinite;
}
.heart-main { display: block; }
.heart-ring {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  border: 2px solid rgba(244,63,94,0.3);
  animation: ringPulse 1.4s ease-in-out infinite;
}
.page-title {
  font-size: clamp(1.8rem, 5vw, 2.6rem);
  background: linear-gradient(135deg, #f43f5e, #ec4899, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 8px 0 6px;
  font-weight: 800;
  letter-spacing: -0.5px;
}
.page-subtitle {
  color: var(--text-color);
  opacity: 0.65;
  font-size: 0.9rem;
  letter-spacing: 0.3px;
}

/* 计时器 */
.timer-card {
  padding: 32px 24px;
  border-radius: 28px;
  border: 1px solid rgba(244,63,94,0.3);
  background: linear-gradient(135deg, rgba(244,63,94,0.1), rgba(168,139,250,0.08));
  text-align: center;
  margin-bottom: 36px;
  position: relative;
  overflow: hidden;
  animation: fadeUp 0.6s ease-out;
}
.timer-glow {
  position: absolute;
  inset: -40px;
  background: radial-gradient(circle at 50% 0%, rgba(244,63,94,0.15) 0%, transparent 70%);
  pointer-events: none;
  animation: glowPulse 3s ease-in-out infinite;
}
.timer-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 1rem;
  color: var(--text-color);
  opacity: 0.85;
  margin-bottom: 20px;
  position: relative;
}
.timer-heart {
  font-size: 1.5rem;
  animation: heartbeat 1.4s ease-in-out infinite;
  display: inline-block;
}

.timer-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  position: relative;
}
.time-unit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 60px;
  animation: fadeUp 0.5s ease-out both;
}
.time-unit:nth-child(1) { animation-delay: 0.1s; }
.time-unit:nth-child(3) { animation-delay: 0.2s; }
.time-unit:nth-child(5) { animation-delay: 0.3s; }
.time-unit:nth-child(7) { animation-delay: 0.4s; }

.time-value {
  font-size: clamp(2rem, 6vw, 3.2rem);
  font-weight: 900;
  background: linear-gradient(135deg, #f43f5e, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  position: relative;
}
.time-value.seconds::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0; right: 0;
  height: 2px;
  background: #f43f5e;
  border-radius: 1px;
  animation: secondsBlink 1s ease-in-out infinite;
}
.time-value.days {
  background: linear-gradient(135deg, #f43f5e, #ec4899);
  -webkit-background-clip: text;
  background-clip: text;
}

.time-label { font-size: 0.72rem; color: var(--text-color); opacity: 0.5; font-weight: 500; }
.time-sep {
  font-size: 2rem;
  font-weight: 900;
  color: #f43f5e;
  opacity: 0.5;
  align-self: flex-start;
  padding-top: 4px;
  animation: secondsBlink 1s ease-in-out infinite;
}
.timer-start-info {
  margin-top: 18px;
  font-size: 0.8rem;
  color: var(--text-color);
  opacity: 0.5;
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.sparkle {
  animation: sparkleRotate 3s linear infinite;
  display: inline-block;
}

/* 区块标题 */
.section-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 1;
}
.section-icon { font-size: 1.3rem; }

/* 时间线 */
.timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-left: 16px;
}
.timeline-line {
  position: absolute;
  left: 6px;
  top: 16px;
  bottom: 16px;
  width: 2px;
  background: linear-gradient(180deg,
    rgba(244,63,94,0.5) 0%,
    rgba(236,72,153,0.3) 40%,
    rgba(167,139,250,0.2) 100%
  );
  border-radius: 1px;
}
.timeline-line::before {
  content: '';
  position: absolute;
  top: 0;
  left: -2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #f43f5e;
  box-shadow: 0 0 8px #f43f5e;
  animation: dotPulse 2s ease-in-out infinite;
}

/* 桌面端左右交替 */
@media (min-width: 700px) {
  .timeline { padding: 0 40px; }
  .timeline-line { left: 50%; transform: translateX(-50%); }

  .timeline-item {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    width: 50%;
    position: relative;
    animation: fadeUp 0.5s ease-out both;
    animation-delay: calc(var(--idx) * 0.08s);
  }
  .timeline-item--right {
    align-self: flex-end;
    flex-direction: row-reverse;
  }
  .tl-connector {
    position: absolute;
    right: -34px;
  }
  .timeline-item--right .tl-connector {
    right: auto;
    left: -34px;
  }
}

.timeline-item { cursor: pointer; position: relative; z-index: 1; }
.tl-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  padding-top: 4px;
}
.tl-dot {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  box-shadow: 0 4px 16px rgba(0,0,0,0.25);
  flex-shrink: 0;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s;
  border: 2px solid rgba(255,255,255,0.15);
  position: relative;
  z-index: 2;
}
.tl-emoji { font-size: 1.3rem; line-height: 1; }
.tl-connector-line {
  width: 2px;
  flex: 1;
  min-height: 24px;
  background: linear-gradient(180deg, var(--item-color, #f43f5e) 0%, transparent 100%);
  opacity: 0.4;
}
.timeline-item:hover .tl-dot {
  transform: scale(1.2);
  box-shadow: 0 6px 24px rgba(244,63,94,0.3);
}

.memory-card {
  flex: 1;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(255,255,255,0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  animation: fadeUp 0.5s ease-out both;
  animation-delay: calc(var(--idx) * 0.08s + 0.1s);
}
.memory-card-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0.7;
  border-radius: 18px 18px 0 0;
}
.memory-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 16px 40px rgba(244,63,94,0.15);
  border-color: rgba(244,63,94,0.25);
}

.memory-card-top { margin-bottom: 8px; }
.memory-date { font-size: 0.75rem; color: var(--text-color); opacity: 0.5; }
.memory-title {
  font-size: 1.05rem;
  font-weight: 800;
  color: var(--text-color);
  margin: 0 0 8px;
}
.memory-desc {
  font-size: 0.85rem;
  color: var(--text-color);
  opacity: 0.8;
  line-height: 1.65;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tag-row { display: flex; flex-wrap: wrap; gap: 6px; }
.mem-tag {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  border: 1px solid;
  font-weight: 500;
}

/* 弹出框 */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal-card {
  max-width: 440px;
  width: 100%;
  padding: 36px 28px;
  border-radius: 28px;
  border: 1px solid rgba(244,63,94,0.25);
  background: rgba(10,8,20,0.95);
  position: relative;
  text-align: center;
  overflow: hidden;
}
.modal-glow {
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.15;
  pointer-events: none;
}
.modal-close {
  position: absolute; top: 14px; right: 16px;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.1);
  color: var(--text-color);
  font-size: 1rem;
  width: 32px; height: 32px;
  border-radius: 50%;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.modal-close:hover { opacity: 1; background: rgba(255,255,255,0.15); }
.modal-emoji {
  font-size: 4rem;
  margin-bottom: 12px;
  animation: emojiPop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.modal-date { font-size: 0.8rem; color: var(--text-color); opacity: 0.45; margin-bottom: 8px; }
.modal-title {
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--text-color);
  margin-bottom: 16px;
}
.modal-desc {
  font-size: 0.92rem;
  color: var(--text-color);
  opacity: 0.85;
  line-height: 1.75;
  margin-bottom: 20px;
  text-align: left;
}

/* 动画 */
.modal-fade-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.modal-fade-enter-from { opacity: 0; transform: scale(0.9) translateY(20px); }

@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  14% { transform: scale(1.25); }
  28% { transform: scale(1); }
  42% { transform: scale(1.18); }
  56% { transform: scale(1); }
}
@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.3); opacity: 0; }
}
@keyframes glowPulse {
  0%, 100% { opacity: 0.8; }
  50% { opacity: 1.4; }
}
@keyframes secondsBlink {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
@keyframes sparkleRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
@keyframes dotPulse {
  0%, 100% { box-shadow: 0 0 8px #f43f5e; }
  50% { box-shadow: 0 0 16px #f43f5e, 0 0 24px rgba(244,63,94,0.5); }
}
@keyframes emojiPop { from { transform: scale(0) rotate(-15deg); } to { transform: scale(1) rotate(0); } }
</style>
