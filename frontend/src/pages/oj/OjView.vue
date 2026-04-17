<script setup lang="ts">
/**
 * OJ 模块根布局：为底部 Live2D / 音乐播放器预留安全区，主内容略收进以避免遮挡左下区域。
 * 浅色模式背景与卡片对比度见 styles/oj-theme.css（html[data-theme='light'] .oj-view）。
 */
import ToolBackBar from '../../components/tools/ToolBackBar.vue'
</script>

<template>
  <div class="oj-view">
    <ToolBackBar />
    <router-view />
  </div>
</template>

<style scoped>
.oj-view {
  width: 100%;
  min-height: calc(100vh - 180px);
  padding-bottom: 7rem;
  padding-left: clamp(0.5rem, 2vw, 1.25rem);
  box-sizing: border-box;
}
</style>
