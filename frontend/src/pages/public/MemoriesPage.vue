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

// 格式化日期
function fmtDate(d: string) {
  const dt = new Date(d)
  return `${dt.getFullYear()} 年 ${dt.getMonth() + 1} 月 ${dt.getDate()} 日`
}
</script>

<template>
  <div class="memories-page">
    <div class="memories-back"><BackToHomeButton /></div>

    <!-- ─── 标题 ─── -->
    <div class="page-header">
      <div class="heart-icon">💝</div>
      <h1 class="page-title">我们的回忆</h1>
      <p class="page-subtitle">记录每一个珍贵的瞬间</p>
    </div>

    <!-- ─── 恋爱计时器 ─── -->
    <div class="timer-card glass-surface">
      <div class="timer-label">
        <span class="timer-heart">💑</span>
        <span>在一起已经</span>
      </div>
      <div class="timer-display">
        <div class="time-unit">
          <span class="time-value">{{ elapsed.days }}</span>
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
          <span class="time-value">{{ pad(elapsed.seconds) }}</span>
          <span class="time-label">秒</span>
        </div>
      </div>
      <div class="timer-start-info">从 2025 年 6 月 18 日 23:00 开始 ✨</div>
    </div>

    <!-- ─── 回忆时间线 ─── -->
    <div class="section-title">📖 我们的故事</div>
    <div class="timeline">
      <div
        v-for="(m, idx) in sortedMemories"
        :key="m.id"
        class="timeline-item"
        :class="{ 'timeline-item--right': idx % 2 === 1 }"
        @click="openMemory(m)"
      >
        <div class="tl-connector">
          <div class="tl-dot" :style="{ background: m.color }">{{ m.emoji }}</div>
        </div>
        <div class="memory-card glass-surface">
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
      <div v-if="activeMemory" class="modal-overlay" @click.self="closeMemory">
        <div class="modal-card glass-surface">
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
}

.memories-back { margin-bottom: 14px; }

/* 标题 */
.page-header { text-align: center; margin-bottom: 32px; }
.heart-icon { font-size: 3rem; animation: heartbeat 1.4s ease-in-out infinite; }
.page-title {
  font-size: clamp(1.8rem, 5vw, 2.4rem);
  background: linear-gradient(135deg, #f43f5e, #ec4899, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 8px 0 6px;
}
.page-subtitle { color: var(--text-color); opacity: 0.7; font-size: 0.9rem; }

/* 计时器 */
.timer-card {
  padding: 28px 24px;
  border-radius: 24px;
  border: 1px solid rgba(244, 63, 94, 0.3);
  background: linear-gradient(135deg, rgba(244,63,94,0.08), rgba(168,139,250,0.08));
  text-align: center;
  margin-bottom: 36px;
  box-shadow: 0 8px 32px rgba(244,63,94,0.12);
}
.timer-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 1rem;
  color: var(--text-color);
  opacity: 0.8;
  margin-bottom: 18px;
}
.timer-heart { font-size: 1.3rem; animation: heartbeat 1.4s ease-in-out infinite; }

.timer-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-wrap: wrap;
}
.time-unit { display: flex; flex-direction: column; align-items: center; gap: 4px; min-width: 56px; }
.time-value {
  font-size: clamp(2rem, 6vw, 3rem);
  font-weight: 900;
  background: linear-gradient(135deg, #f43f5e, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.time-label { font-size: 0.72rem; color: var(--text-color); opacity: 0.55; }
.time-sep { font-size: 2rem; font-weight: 900; color: #f43f5e; opacity: 0.6; align-self: flex-start; padding-top: 4px; }
.timer-start-info { margin-top: 16px; font-size: 0.8rem; color: var(--text-color); opacity: 0.55; }

/* 区块标题 */
.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-color);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 时间线 */
.timeline {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 桌面端左右交替 */
@media (min-width: 700px) {
  .timeline { padding: 0 40px; }
  .timeline::before {
    content: '';
    position: absolute;
    left: 50%;
    top: 0; bottom: 0;
    width: 2px;
    background: linear-gradient(180deg, rgba(244,63,94,0.4), rgba(168,139,250,0.2));
    transform: translateX(-50%);
  }
  .timeline-item {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    width: 50%;
    position: relative;
  }
  .timeline-item--right {
    align-self: flex-end;
    flex-direction: row-reverse;
  }
  .tl-connector {
    position: absolute;
    right: -32px;
  }
  .timeline-item--right .tl-connector {
    right: auto;
    left: -32px;
  }
}

.timeline-item { cursor: pointer; }
.tl-dot {
  width: 40px; height: 40px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.2rem;
  box-shadow: 0 4px 14px rgba(0,0,0,0.2);
  flex-shrink: 0;
  transition: transform 0.2s;
}
.timeline-item:hover .tl-dot { transform: scale(1.15); }

.memory-card {
  flex: 1;
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.15);
  background: rgba(255,255,255,0.07);
  transition: transform 0.25s, box-shadow 0.25s;
}
.memory-card:hover { transform: translateY(-3px); box-shadow: 0 12px 32px rgba(244,63,94,0.12); }

.memory-card-top { margin-bottom: 8px; }
.memory-date { font-size: 0.75rem; color: var(--text-color); opacity: 0.55; }
.memory-title { font-size: 1rem; font-weight: 700; color: var(--text-color); margin: 0 0 8px; }
.memory-desc { font-size: 0.85rem; color: var(--text-color); opacity: 0.8; line-height: 1.6; margin: 0 0 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.tag-row { display: flex; flex-wrap: wrap; gap: 6px; }
.mem-tag { padding: 3px 10px; border-radius: 20px; font-size: 0.75rem; border: 1px solid; font-weight: 500; }

/* 弹出框 */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.65);
  backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.2s ease;
}
.modal-card {
  max-width: 440px;
  width: 100%;
  padding: 32px 28px;
  border-radius: 24px;
  border: 1px solid rgba(244,63,94,0.25);
  background: rgba(15,10,25,0.92);
  position: relative;
  text-align: center;
  animation: scaleIn 0.25s ease;
}
.modal-close {
  position: absolute; top: 14px; right: 16px;
  background: none; border: none; color: var(--text-color); font-size: 1rem; cursor: pointer; opacity: 0.6;
}
.modal-close:hover { opacity: 1; }
.modal-emoji { font-size: 3.5rem; margin-bottom: 10px; }
.modal-date { font-size: 0.8rem; color: var(--text-color); opacity: 0.5; margin-bottom: 8px; }
.modal-title { font-size: 1.3rem; font-weight: 800; color: var(--text-color); margin-bottom: 14px; }
.modal-desc { font-size: 0.92rem; color: var(--text-color); opacity: 0.85; line-height: 1.7; margin-bottom: 18px; text-align: left; }

/* 动画 */
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes scaleIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }
@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  14% { transform: scale(1.2); }
  28% { transform: scale(1); }
  42% { transform: scale(1.15); }
  56% { transform: scale(1); }
}
</style>
