<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ref, computed, defineAsyncComponent } from 'vue'
import GlassCard from '../components/GlassCard.vue'

const route = useRoute()
const router = useRouter()
const gameId = computed(() => route.params.id as string)

const gameMap: Record<string, any> = {
  snake: defineAsyncComponent(() => import('./games/SnakeGame.vue')),
  '2048': defineAsyncComponent(() => import('./games/Game2048.vue')),
  schulte: defineAsyncComponent(() => import('./games/SchulteGame.vue')),
  puzzle15: defineAsyncComponent(() => import('./games/Puzzle15Game.vue')),
  minesweeper: defineAsyncComponent(() => import('./games/MinesweeperGame.vue')),
  reaction: defineAsyncComponent(() => import('./games/ReactionGame.vue')),
}

const places: Record<string, { name: string; desc: string }> = {
  // 三个新游戏已实现，下方保留为空表
}
</script>

<template>
  <div class="page-container max-w-3xl mx-auto">
    <button @click="router.push('/')" class="glass-button text-sm mb-6">← 返回首页</button>

    <Suspense>
      <component :is="gameMap[gameId]" v-if="gameMap[gameId]" :key="gameId" />
      <template #fallback>
        <GlassCard class="heavy !p-12 text-center">
          <div class="text-5xl mb-3 animate-pulse">⏳</div>
          <p class="text-[var(--text-secondary)]">游戏加载中...</p>
        </GlassCard>
      </template>
    </Suspense>

    <GlassCard v-if="!gameMap[gameId]" class="heavy !p-12 text-center">
      <p class="text-4xl mb-4">🤷</p>
      <p class="text-[var(--text-secondary)]">未找到该游戏</p>
    </GlassCard>
  </div>
</template>
