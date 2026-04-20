<script setup lang="ts">
import { ref } from 'vue'
import MoyuGameShell from '../../components/moyu/MoyuGameShell.vue'

const gameUrl = 'https://snakeball.jiejoe.com'
const loading = ref(true)

function onLoad() {
  loading.value = false
}

function openInNewTab() {
  window.open(gameUrl, '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <MoyuGameShell title="Snakeball" high-text="外部项目接入 · 分数以原项目为准" :loading="loading">
    <div class="snakeball-wrap">
      <iframe class="snakeball-frame" :src="gameUrl" title="Snakeball" @load="onLoad" />
      <div class="snakeball-actions">
        <button type="button" class="snakeball-open" @click="openInNewTab">新窗口打开 Snakeball</button>
      </div>
    </div>
  </MoyuGameShell>
</template>

<style scoped>
.snakeball-wrap {
  width: 100%;
}

.snakeball-frame {
  width: 100%;
  min-height: min(720px, calc(100dvh - 190px));
  border: 1px solid color-mix(in srgb, var(--primary-color, #66d9ff) 36%, rgba(255, 255, 255, 0.5));
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.12);
}

.snakeball-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.snakeball-open {
  min-height: 40px;
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--primary-color, #66d9ff) 38%, rgba(255, 255, 255, 0.45));
  background: rgba(255, 255, 255, 0.2);
  color: var(--text-color);
  font-size: 0.88rem;
  font-weight: 700;
  padding: 0 14px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.snakeball-open:hover {
  transform: translateY(-2px);
  border-color: var(--primary-color, #66d9ff);
  box-shadow: 0 8px 24px rgba(102, 217, 255, 0.24);
}

:global(.dark-mode) .snakeball-frame {
  background: rgba(15, 23, 42, 0.45);
}

:global(.dark-mode) .snakeball-open {
  background: rgba(15, 23, 42, 0.45);
}
</style>
