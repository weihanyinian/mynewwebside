<script setup lang="ts">
/**
 * 摸鱼内置游戏统一外壳：返回按钮、标题、最高分槽位、青蓝玻璃态、随全局主题对比度
 */
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useThemeStore } from '../../stores/theme'

defineProps<{
  title: string
  /** 右侧展示的最高分文案，无则不占位 */
  highText?: string
}>()

const router = useRouter()
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

function back() {
  router.push('/moyu')
}
</script>

<template>
  <div class="moyu-game-page" :class="{ 'moyu-game-page--dark': isDarkMode }">
    <div class="moyu-game-shell glass-surface">
      <header class="moyu-game-shell__bar">
        <button type="button" class="site-pill" @click="back">← 返回摸鱼中心</button>
        <h1 class="moyu-game-shell__title">{{ title }}</h1>
        <div v-if="highText" class="moyu-game-shell__hi">{{ highText }}</div>
        <div v-else class="moyu-game-shell__spacer" />
      </header>
      <div class="moyu-game-shell__body">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.moyu-game-page {
  padding: 16px;
  padding-bottom: 6rem;
  min-height: calc(100vh - 120px);
  box-sizing: border-box;
}

.moyu-game-shell {
  max-width: 960px;
  margin: 0 auto;
  border-radius: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.18);
}

.moyu-game-page--dark .moyu-game-shell {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.35);
}

.moyu-game-shell__bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.moyu-game-shell__title {
  flex: 1;
  margin: 0;
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-color);
  min-width: 0;
}

.moyu-game-shell__hi {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-color);
  opacity: 0.9;
}

.moyu-game-shell__spacer {
  width: 1px;
  height: 1px;
}

.moyu-game-shell__body {
  border-radius: 16px;
  overflow: auto;
}
</style>
