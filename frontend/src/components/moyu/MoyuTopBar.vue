<script setup lang="ts">
/**
 * 摸鱼游戏统一顶栏 — 所有游戏共用。
 * 新增功能（如主题切换、分享、排行榜入口）只需改此组件，全部游戏同步生效。
 */
import MoyuBackToHubButton from './MoyuBackToHubButton.vue'

defineProps<{
  title: string
  /** 右侧统计/最高分文案 */
  highText?: string
}>()
</script>

<template>
  <header class="moyu-topbar">
    <div class="moyu-topbar__row">
      <MoyuBackToHubButton class="moyu-topbar__back" compact />
      <h1 class="moyu-topbar__title">{{ title }}</h1>
    </div>
    <div v-if="highText" class="moyu-topbar__hi">{{ highText }}</div>
    <slot name="extra" />
  </header>
</template>

<style scoped>
.moyu-topbar {
  margin-bottom: 12px;
}

.moyu-topbar__row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.moyu-topbar__back {
  flex-shrink: 0;
  touch-action: manipulation;
}

.moyu-topbar__title {
  flex: 1;
  margin: 0;
  font-size: clamp(1.05rem, 3vw, 1.25rem);
  font-weight: 800;
  color: var(--text-color);
  min-width: 0;
  text-align: right;
  line-height: 1.2;
}

.moyu-topbar__hi {
  margin-top: 10px;
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--text-color);
  opacity: 0.9;
  line-height: 1.4;
  word-break: break-word;
  text-align: center;
}
</style>
