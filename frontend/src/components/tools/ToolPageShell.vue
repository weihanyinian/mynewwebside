<script setup lang="ts">
import ToolBackBar from './ToolBackBar.vue'
</script>

<template>
  <div class="tools-widget-page">
    <ToolBackBar />
    <div class="glass-tool-sheet">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.tools-widget-page {
  max-width: min(640px, 100%);
  margin: 0 auto;
  padding-bottom: 2rem;
}

.glass-tool-sheet {
  background: var(--glass-bg, rgba(255, 255, 255, 0.4));
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.55));
  box-shadow: var(--glass-shadow, 0 8px 32px rgba(74, 144, 226, 0.15));
  border-radius: 24px;
  padding: 1.75rem 1.5rem 2rem;
  color: var(--text-color, #2c3e50);
}

:root[data-theme='dark'] .glass-tool-sheet {
  background: rgba(16, 18, 27, 0.72);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.45);
  color: #eaf8ff;
}
</style>
