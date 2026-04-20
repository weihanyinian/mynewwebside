<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackToHomeButton from '../../components/BackToHomeButton.vue'

const router = useRouter()

const games = [
  { path: '/moyu/2048', name: '2048', icon: '🔢', desc: '合并数字，挑战 2048（支持撤销）' },
  { path: '/moyu/tetris', name: '俄罗斯方块', icon: '🧱', desc: '经典下落，触摸键 + 滑动' },
  { path: '/moyu/snake', name: '贪吃蛇', icon: '🐍', desc: '滑动 / 虚拟方向键，可调速度' },
  { path: '/moyu/minesweeper', name: '扫雷', icon: '💣', desc: '多难度 · 长按插旗 · 计时' },
  { path: '/moyu/trex', name: '恐龙跑酷', icon: '🦖', desc: '点击跳跃，节奏更适合手机' },
  { path: '/moyu/flappy', name: 'Flappy Bird', icon: '🐦', desc: '点按飞行，深色高对比' },
  { path: '/moyu/breakout', name: '打砖块', icon: '🏏', desc: '滑动挡板 + 左右键' },
  { path: '/moyu/gomoku', name: '五子棋', icon: '⚫', desc: '大屏棋盘 · 触摸落子' },
  { path: '/moyu/guess', name: '猜数字', icon: '🎲', desc: '三档难度 · 冷热提示 · 最少次数挑战' },
  { path: '/moyu/memory-card', name: '记忆翻牌', icon: '🃏', desc: '翻牌配对 · 记忆力挑战 · 计时排名' },
] as const

function go(path: string) {
  router.push(path)
}
</script>

<template>
  <div class="moyu-container">
    <div class="moyu-back">
      <BackToHomeButton />
    </div>
    <div class="header">
      <h1 class="title">维寒一念 · 摸鱼中心</h1>
      <p class="subtitle">
        10 款经典小游戏，纯静态页面嵌入、无 npm 依赖；最高分本地保存；随站点主题切换。
      </p>
    </div>

    <div class="games-grid">
      <button
        v-for="g in games"
        :key="g.path"
        type="button"
        class="game-card glass-surface"
        @click="go(g.path)"
      >
        <div class="game-icon" aria-hidden="true">{{ g.icon }}</div>
        <div class="game-info">
          <h3 class="game-name">{{ g.name }}</h3>
          <p class="game-desc">{{ g.desc }}</p>
        </div>
        <span class="play-chip site-pill site-pill--chip site-pill--active">开始</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.moyu-container {
  box-sizing: border-box;
  width: 100%;
  max-width: 100vw;
  overflow-x: hidden;
  padding: max(16px, env(safe-area-inset-top)) 16px max(28px, calc(env(safe-area-inset-bottom) + 12px)) 16px;
  animation: fadeIn 0.5s ease-out;
}

.moyu-back {
  margin-bottom: 12px;
}

.header {
  text-align: center;
  margin-bottom: clamp(20px, 4vw, 32px);
}

.title {
  font-size: clamp(1.5rem, 5vw, 2.25rem);
  color: var(--primary-color, #66d9ff);
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(102, 217, 255, 0.3);
}

.subtitle {
  color: var(--text-color);
  opacity: 0.88;
  font-size: 0.92rem;
  max-width: 520px;
  margin: 0 auto;
  line-height: 1.55;
}

.games-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

@media (min-width: 520px) {
  .games-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }
}

@media (min-width: 960px) {
  .games-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

.game-card {
  display: flex;
  align-items: center;
  padding: 16px;
  min-height: 52px;
  border-radius: 16px;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
  position: relative;
  overflow: hidden;
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 8px 28px rgba(102, 217, 255, 0.12);
  color: inherit;
  font: inherit;
  touch-action: manipulation;
}

.game-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 36px rgba(102, 217, 255, 0.22);
  border-color: var(--primary-color, #66d9ff);
}

.game-icon {
  font-size: 2rem;
  margin-right: 12px;
  background: rgba(255, 255, 255, 0.12);
  width: 52px;
  height: 52px;
  min-width: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  flex-shrink: 0;
}

.game-info {
  flex: 1;
  min-width: 0;
}

.game-name {
  font-size: 1.02rem;
  margin: 0 0 4px 0;
  color: var(--text-color);
  font-weight: 800;
}

.game-desc {
  font-size: 0.8rem;
  margin: 0;
  color: var(--text-color);
  opacity: 0.78;
  line-height: 1.4;
}

.play-chip {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.75rem;
  opacity: 0.95;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
