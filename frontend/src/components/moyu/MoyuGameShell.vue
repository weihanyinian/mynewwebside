<script setup lang="ts">
/**
 * 摸鱼游戏统一外壳：安全区、毛玻璃、返回、标题、顶栏统计、加载态
 */
import { storeToRefs } from 'pinia'
import { useThemeStore } from '../../stores/theme'
import MoyuTopBar from './MoyuTopBar.vue'

defineProps<{
  title: string
  highText?: string
  loading?: boolean
}>()

const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)
</script>

<template>
  <div class="moyu-game-page" :class="{ 'moyu-game-page--dark': isDarkMode }">
    <div class="moyu-game-shell glass-surface">
      <MoyuTopBar :title="title" :high-text="highText" />

      <div v-if="loading" class="moyu-game-shell__loading" aria-busy="true">
        <span class="moyu-game-shell__loading-dot" />
        <span>加载中…</span>
      </div>

      <div class="moyu-game-shell__body">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.moyu-game-page {
  box-sizing: border-box;
  width: 100%;
  max-width: 100vw;
  overflow-x: hidden;
  min-height: 100dvh;
  padding: max(10px, env(safe-area-inset-top)) max(12px, env(safe-area-inset-right))
    max(16px, env(safe-area-inset-bottom)) max(12px, env(safe-area-inset-left));
  padding-bottom: max(16px, calc(env(safe-area-inset-bottom) + 8px));
}

.moyu-game-shell {
  max-width: min(960px, 100%);
  margin: 0 auto;
  border-radius: 16px;
  padding: 14px;
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

.moyu-game-shell__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 8px 0 12px;
  font-weight: 700;
  color: var(--text-color);
  opacity: 0.88;
}

.moyu-game-shell__loading-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a90e2, #50e3c2);
  animation: moyuSpin 0.9s ease-in-out infinite alternate;
}

@keyframes moyuSpin {
  from {
    transform: scale(0.65);
    opacity: 0.5;
  }
  to {
    transform: scale(1.1);
    opacity: 1;
  }
}

.moyu-game-shell__body {
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

</style>
