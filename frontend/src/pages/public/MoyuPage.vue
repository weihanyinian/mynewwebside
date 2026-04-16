<script setup lang="ts">
import { ref } from 'vue'
import BackToHomeButton from '../../components/BackToHomeButton.vue'

const games = [
  { id: 1, name: '2048 数字合并', icon: '🔢', desc: '经典数字合并游戏，挑战2048', url: 'https://play2048.co/' },
  { id: 2, name: '经典俄罗斯方块', icon: '🧱', desc: '童年回忆，下落消除', url: 'https://chvin.github.io/react-tetris/?lan=zh' },
  { id: 3, name: '像素恐龙跑酷', icon: '🦖', desc: '断网小恐龙，无限奔跑', url: 'https://trex-runner.com/' },
  { id: 4, name: '视觉差贪吃蛇', icon: '🐍', desc: '经典贪吃蛇，考验反应', url: 'https://playsnake.org/' },
  { id: 5, name: '国际象棋', icon: '♟️', desc: '脑力对决，在线下棋', url: 'https://lichess.org/' },
  { id: 6, name: '五子棋', icon: '⚫', desc: '黑白交锋，连五子为胜', url: 'https://papergames.io/zh/gomoku' },
  { id: 7, name: '黑白棋 (奥赛罗)', icon: '⚪', desc: '经典翻转棋盘游戏', url: 'https://papergames.io/zh/reversi' },
  { id: 8, name: '数独', icon: '📝', desc: '逻辑填数字，锻炼大脑', url: 'https://sudoku.com/zh' },
  { id: 9, name: '纸牌接龙', icon: '🃏', desc: 'Windows经典扑克接龙', url: 'https://www.solitr.com/' },
  { id: 10, name: '盖塔楼', icon: '🏢', desc: '考验时机的建筑堆叠', url: 'https://towerbuild.vercel.app/' },
  { id: 11, name: '记忆翻牌配对', icon: '🎴', desc: '考验记忆力的配对游戏', url: 'https://www.crazygames.com/game/card-match' },
  { id: 12, name: '经典扫雷', icon: '💣', desc: '步步为营，小心地雷', url: 'https://minesweeperonline.com/' },
  { id: 13, name: '吃豆人', icon: 'ᗧ', desc: '躲避幽灵，吃掉所有豆子', url: 'https://freepacman.org/' },
  { id: 14, name: '井字棋', icon: '❌', desc: '简单快速的画圈打叉', url: 'https://playtictactoe.org/' },
  { id: 15, name: '像素飞鸟 (Flappy)', icon: '🐦', desc: '点击屏幕，穿过水管', url: 'https://flappybird.io/' },
  { id: 16, name: '打砖块', icon: '🏏', desc: '经典弹球打砖块', url: 'https://elgoog.im/breakout/' },
  { id: 17, name: '涂鸦跳跃', icon: '🦘', desc: '不断向上跳跃的经典', url: 'https://doodlejump.io/' },
  { id: 18, name: '台球 8Ball', icon: '🎱', desc: '在线台球对战', url: 'https://8ballpool.com/zh' },
  { id: 19, name: '蜘蛛纸牌', icon: '🕷️', desc: '更具挑战的扑克接龙', url: 'https://www.spider-solitaire-game.com/' },
  { id: 20, name: '连连看', icon: '🧩', desc: '经典图形消除连连看', url: 'https://www.crazygames.com/game/onet-connect-classic' },
  { id: 21, name: '3D魔方', icon: '🧊', desc: '在线旋转还原魔方', url: 'https://ruwix.com/online-rubiks-cube-solver-program/' },
  { id: 22, name: '太空入侵者', icon: '👾', desc: '经典复古街机射击', url: 'https://elgoog.im/space-invaders/' }
]

const activeGame = ref<{ id: number, name: string, url: string } | null>(null)

function openGame(game: any) {
  activeGame.value = game
}

function closeGame() {
  activeGame.value = null
}

function openInNewTab() {
  if (activeGame.value) {
    window.open(activeGame.value.url, '_blank')
  }
}
</script>

<template>
  <div class="moyu-container">
    <div class="moyu-back">
      <BackToHomeButton />
    </div>
    <div class="header">
      <h1 class="title">摸鱼中心 🎮</h1>
      <p class="subtitle">工作学习累了？来这里放松一下吧！精选 20+ 款经典小游戏。</p>
    </div>

    <div class="games-grid">
      <div 
        v-for="game in games" 
        :key="game.id" 
        class="game-card glass-ui"
        @click="openGame(game)"
      >
        <div class="game-icon">{{ game.icon }}</div>
        <div class="game-info">
          <h3 class="game-name">{{ game.name }}</h3>
          <p class="game-desc">{{ game.desc }}</p>
        </div>
        <div class="play-btn">▶ 开始</div>
      </div>
    </div>

    <!-- Game Modal -->
    <transition name="fade">
      <div v-if="activeGame" class="game-modal">
        <div class="modal-overlay" @click="closeGame"></div>
        <div class="modal-content glass-ui">
          <div class="modal-header">
            <h2>{{ activeGame.name }}</h2>
            <div class="modal-actions">
              <button class="action-btn new-tab-btn" @click="openInNewTab" title="在新标签页打开（如果白屏请点击这里）">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M19 19H5V5h7V3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z"/>
                </svg>
                在新标签页打开
              </button>
              <button class="action-btn close-btn" @click="closeGame">✕ 关闭</button>
            </div>
          </div>
          <div class="iframe-container">
            <iframe 
              :src="activeGame.url" 
              frameborder="0" 
              allow="autoplay; fullscreen" 
              allowfullscreen
            ></iframe>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.moyu-container {
  padding: 20px;
  animation: fadeIn 0.5s ease-out;
}

.moyu-back {
  margin-bottom: 12px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 2.5rem;
  color: var(--primary-color);
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(102, 217, 255, 0.3);
}

.subtitle {
  color: #fff;
  opacity: 0.8;
  font-size: 1.1rem;
}

.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.game-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.game-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(102, 217, 255, 0.3);
  border-color: var(--primary-color);
}

.game-card:hover .play-btn {
  opacity: 1;
  transform: translateX(0);
}

.game-icon {
  font-size: 2.5rem;
  margin-right: 15px;
  background: rgba(255, 255, 255, 0.1);
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.game-info {
  flex: 1;
}

.game-name {
  font-size: 1.1rem;
  margin: 0 0 5px 0;
  color: var(--primary-color);
}

.game-desc {
  font-size: 0.85rem;
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
}

.play-btn {
  position: absolute;
  right: 20px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: #fff;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
  opacity: 0;
  transform: translateX(20px);
  transition: all 0.3s ease;
}

/* Modal Styles */
.game-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
}

.modal-content {
  position: relative;
  width: 90vw;
  height: 90vh;
  max-width: 1400px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.2rem;
  color: var(--primary-color);
}

.modal-actions {
  display: flex;
  gap: 15px;
}

.action-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 6px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.close-btn:hover {
  background: rgba(252, 162, 229, 0.3);
  border-color: var(--secondary-color);
}

.new-tab-btn:hover {
  background: rgba(102, 217, 255, 0.3);
  border-color: var(--primary-color);
}

.iframe-container {
  flex: 1;
  background: #000;
  position: relative;
}

.iframe-container iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .modal-content,
.fade-leave-active .modal-content {
  transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.fade-enter-from .modal-content,
.fade-leave-to .modal-content {
  transform: scale(0.9);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .modal-content {
    width: 100vw;
    height: 100vh;
    border-radius: 0;
  }
  .action-btn span {
    display: none;
  }
}
</style>
