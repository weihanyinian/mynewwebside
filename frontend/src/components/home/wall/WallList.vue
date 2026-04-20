<script setup lang="ts">
import { escapeHtml } from '../../../utils/escapeHtml'
import type { WallMessageItem } from '../../../types/wall'

defineProps<{
  loading: boolean
  items: WallMessageItem[]
  anonymousLabel: string
  replyFromAdminLabel: string
  emptyTitle: string
  emptyHint: string
  loadingLabel: string
  loadError?: boolean
  loadErrorLabel?: string
}>()

function formatTime(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}
</script>

<template>
  <p v-if="loading" class="muted center">{{ loadingLabel }}</p>
  <p v-else-if="loadError" class="wall-error">{{ loadErrorLabel }}</p>
  <template v-else>
    <div v-if="items.length" class="home-wall-grid">
      <div v-for="m in items" :key="m.id" class="site-module-card home-wall-card">
        <div class="home-wall-meta">{{ m.nickname || anonymousLabel }} · {{ formatTime(m.createdAt) }}</div>
        <p class="home-wall-body" v-html="escapeHtml(m.content)"></p>
        <p v-if="m.adminReply" class="home-wall-reply">{{ replyFromAdminLabel }}：{{ m.adminReply }}</p>
      </div>
    </div>
    <div v-else class="wall-empty-block">
      <p class="wall-empty-title"><span aria-hidden="true">💬</span> {{ emptyTitle }}</p>
      <p class="muted center wall-empty-hint">{{ emptyHint }}</p>
    </div>
  </template>
</template>

<style scoped>
.center { text-align: center; }
.home-wall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  max-width: 1100px;
  margin: 0 auto;
}
.home-wall-card {
  padding: 20px;
  border-radius: 20px;
  border: 1.5px solid rgba(167, 139, 250, 0.25);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}
.home-wall-card::before {
  content: '';
  position: absolute;
  top: 12px;
  left: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #66d9ff, #a78bfa);
  box-shadow: 0 0 8px rgba(102, 217, 255, 0.5);
}
.home-wall-card:hover {
  transform: translateY(-4px);
  border-color: rgba(167, 139, 250, 0.5);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.15), 0 0 24px color-mix(in srgb, rgba(167, 139, 250, 0.3) 50%, transparent);
}
.home-wall-meta {
  font-size: 0.86rem;
  font-weight: 600;
  color: rgba(102, 217, 255, 0.9);
  margin-bottom: 12px;
  margin-left: 16px;
  letter-spacing: 0.02em;
}
.home-wall-body {
  margin: 0;
  margin-left: 16px;
  line-height: 1.65;
  font-size: 0.99rem;
  color: var(--text-color, #0f172a);
  word-break: break-word;
}
.home-wall-reply {
  margin: 12px 0 0;
  margin-left: 16px;
  font-size: 0.9rem;
  color: rgba(167, 139, 250, 0.9);
  background: rgba(167, 139, 250, 0.1);
  border-left: 3px solid rgba(167, 139, 250, 0.6);
  padding: 10px 12px;
  border-radius: 0 12px 12px 0;
}
.wall-empty-block {
  text-align: center;
  margin: 8px 0 16px;
}
.wall-empty-title {
  text-align: center;
  font-weight: 900;
  font-size: 1.2rem;
  color: color-mix(in srgb, var(--text-color) 90%, #000 10%);
  margin: 0 0 10px;
  text-shadow: 0 1px 10px rgba(255, 255, 255, 0.32);
  letter-spacing: 0.01em;
}
.wall-empty-hint {
  margin-top: 8px;
}
.wall-error {
  padding: 12px 16px;
  border-radius: 16px;
  color: #b91c1c;
  background: rgba(254, 226, 226, 0.85);
  border: 1px solid rgba(248, 113, 113, 0.5);
  margin-bottom: 16px;
}
</style>
