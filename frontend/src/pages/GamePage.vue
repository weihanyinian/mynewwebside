<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { defineAsyncComponent } from 'vue'
import GlassCard from '../components/GlassCard.vue'

const route = useRoute()
const router = useRouter()
const gameId = route.params.id as string

const gameMap: Record<string, any> = {
  snake: defineAsyncComponent(() => import('./games/SnakeGame.vue')),
  '2048': defineAsyncComponent(() => import('./games/Game2048.vue')),
}

const places: Record<string, { name: string; desc: string }> = {
  flappy: { name: '🐦 像素鸟', desc: '点击屏幕让小鸟飞起来，穿过管道间隙。' },
  puzzle15: { name: '🧩 数字华容道', desc: '滑动方块使数字按顺序排列。' },
  minesweeper: { name: '💣 扫雷', desc: '根据数字提示找出所有地雷。' },
  reaction: { name: '🎯 反应力测试', desc: '测测你的反应速度。（也在工具箱中）' },
}

const GameComponent = gameMap[gameId]
const placeholder = places[gameId]
</script>

<template>
  <!-- Full game component -->
  <GameComponent v-if="GameComponent" />

  <!-- Placeholder for unimplemented games -->
  <div v-else class="page-container max-w-2xl text-center">
    <button @click="router.push('/games')" class="glass-button text-sm mb-8">← 返回游戏大厅</button>
    <GlassCard v-if="placeholder">
      <div class="text-6xl mb-4">{{ placeholder.name.slice(0, 2) }}</div>
      <h1 class="text-2xl font-bold mb-4 gradient-text">{{ placeholder.name.slice(3) }}</h1>
      <p class="text-[var(--text-muted)] mb-6">{{ placeholder.desc }}</p>
      <div class="glass-card !bg-[#a58eea1a] !border-[#a58eea33] p-4 inline-block">
        <p class="text-sm">🚧 游戏开发中，敬请期待~</p>
      </div>
    </GlassCard>
    <GlassCard v-else>
      <p class="text-4xl mb-4">🤷</p>
      <p class="text-[var(--text-secondary)]">未找到该游戏</p>
    </GlassCard>
  </div>
</template>
