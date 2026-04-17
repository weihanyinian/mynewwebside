<script setup lang="ts">
import { useRouter } from 'vue-router'
import BackToHomeButton from '../../components/BackToHomeButton.vue'

const router = useRouter()

const games = [
  { path: '/moyu/2048', name: '2048', icon: '🔢', desc: '合并数字，挑战 2048' },
  { path: '/moyu/tetris', name: '俄罗斯方块', icon: '🧱', desc: '经典下落消除' },
  { path: '/moyu/snake', name: '贪吃蛇', icon: '🐍', desc: '吃豆变长，别撞墙' },
  { path: '/moyu/minesweeper', name: '扫雷', icon: '💣', desc: '9×9 雷区，逻辑推理' },
  { path: '/moyu/trex', name: '恐龙跑酷', icon: '🦖', desc: '跳跃躲避障碍' },
  { path: '/moyu/flappy', name: 'Flappy Bird', icon: '🐦', desc: '穿过管道' },
  { path: '/moyu/breakout', name: '打砖块', icon: '🏏', desc: '弹球清屏' },
  { path: '/moyu/gomoku', name: '五子棋', icon: '⚫', desc: '对战简易 AI' },
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
      <h1 class="title">摸鱼中心</h1>
      <p class="subtitle">8 款内置小游戏，纯前端运行，自动跟随系统/站点主题。</p>
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
  padding: 20px;
  padding-bottom: 5rem;
  animation: fadeIn 0.5s ease-out;
}

.moyu-back {
  margin-bottom: 12px;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 2.25rem;
  color: var(--primary-color, #66d9ff);
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(102, 217, 255, 0.3);
}

.subtitle {
  color: var(--text-color);
  opacity: 0.88;
  font-size: 1rem;
  max-width: 520px;
  margin: 0 auto;
  line-height: 1.5;
}

.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.game-card {
  display: flex;
  align-items: center;
  padding: 18px;
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
}

.game-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 36px rgba(102, 217, 255, 0.22);
  border-color: var(--primary-color, #66d9ff);
}

.game-icon {
  font-size: 2.25rem;
  margin-right: 14px;
  background: rgba(255, 255, 255, 0.12);
  width: 56px;
  height: 56px;
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
  font-size: 1.05rem;
  margin: 0 0 4px 0;
  color: var(--text-color);
  font-weight: 800;
}

.game-desc {
  font-size: 0.85rem;
  margin: 0;
  color: var(--text-color);
  opacity: 0.75;
  line-height: 1.35;
}

.play-chip {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.78rem;
  opacity: 0.92;
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
