<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import GlassCard from '../components/GlassCard.vue'

const route = useRoute()
const router = useRouter()
const gameId = route.params.id as string

const games: Record<string, { name: string; desc: string; status: string }> = {
  snake: { name: '🐍 贪吃蛇', desc: '经典街机游戏，用方向键控制蛇的移动，吃到食物变长，撞到墙壁或自己则游戏结束。', status: 'coming' },
  flappy: { name: '🐦 像素鸟', desc: '点击屏幕让小鸟飞起来，穿过管道间隙，碰到管道或地面则游戏结束。', status: 'coming' },
  '2048': { name: '🔢 2048', desc: '滑动合并相同数字，目标是凑出 2048。看似简单，实则考验策略。', status: 'coming' },
  puzzle15: { name: '🧩 数字华容道', desc: '滑动方块使数字按顺序排列，经典的智力游戏。', status: 'coming' },
  minesweeper: { name: '💣 扫雷', desc: '根据数字提示找出所有地雷，经典 Windows 游戏。', status: 'coming' },
  reaction: { name: '🎯 反应力测试', desc: '看到屏幕变绿时立即点击，测试你的反应速度。', status: 'coming' },
}

const game = games[gameId] || { name: '未知游戏', desc: '', status: 'unknown' }
</script>

<template>
  <div class="page-container max-w-2xl text-center">
    <button @click="router.push('/games')" class="glass-button text-sm mb-8">← 返回游戏大厅</button>

    <GlassCard v-if="game">
      <div class="text-6xl mb-4">{{ game.name.slice(0, 2) }}</div>
      <h1 class="text-2xl font-bold mb-4 gradient-text">{{ game.name.slice(3) }}</h1>
      <p class="text-[var(--text-secondary)] mb-6">{{ game.desc }}</p>

      <div v-if="game.status === 'coming'" class="glass-card !bg-[#a58eea1a] !border-[#a58eea33] p-4 inline-block">
        <p class="text-sm">🚧 游戏开发中，敬请期待~</p>
      </div>
    </GlassCard>
  </div>
</template>
