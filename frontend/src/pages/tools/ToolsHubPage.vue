<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToolsStore } from '../../stores/tools'
import ToolModuleIcon from '../../components/tools/ToolModuleIcon.vue'

const { t } = useI18n()
const router = useRouter()
const toolsStore = useToolsStore()
toolsStore.hydrate()

const cards = computed(() =>
  toolsStore.tools.map((card) => ({
    id: card.id,
    icon: card.icon,
    titleKey: card.titleKey,
    descKey: card.descKey,
    to: card.path,
  })),
)

function openTool(id: string, path: string) {
  toolsStore.markUsed(id)
  void router.push(path)
}

function toggleFavorite(id: string) {
  toolsStore.toggleFavorite(id)
}
</script>

<template>
  <div class="tools-hub">
    <header class="tools-hub__header">
      <h1 class="tools-hub__title">{{ t('toolsHub.title') }}</h1>
      <p class="tools-hub__sub">{{ t('toolsHub.subtitle') }}</p>
      <p v-if="toolsStore.recentTools.length" class="tools-hub__recent">
        最近使用：{{ toolsStore.recentTools.map((v) => t(v.titleKey)).join(' / ') }}
      </p>
    </header>

    <div class="tools-hub__grid">
      <article
        v-for="c in cards"
        :key="c.id"
        class="site-module-card home-tool-card glass-tool-card"
        :data-icon="c.icon"
        role="button"
        tabindex="0"
        @click="openTool(c.id, c.to)"
        @keydown.enter.prevent="openTool(c.id, c.to)"
        @keydown.space.prevent="openTool(c.id, c.to)"
      >
        <button
          type="button"
          class="tool-favorite"
          :class="{ 'tool-favorite--active': toolsStore.favorites.includes(c.id) }"
          :title="toolsStore.favorites.includes(c.id) ? '取消收藏' : '收藏工具'"
          @click.stop="toggleFavorite(c.id)"
        >
          ★
        </button>
        <div class="home-tool-card__head">
          <span class="home-tool-ico" aria-hidden="true">
            <ToolModuleIcon :icon="c.icon" />
          </span>
          <h3>{{ t(c.titleKey) }}</h3>
        </div>
        <p class="home-tool-card__desc">{{ t(c.descKey) }}</p>
        <button
          type="button"
          class="home-tool-fab"
          :aria-label="t('home.sectionToolsOpen')"
          :title="t('home.sectionToolsOpen')"
          @click.stop="openTool(c.id, c.to)"
        >
          <svg class="home-tool-fab__ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <path
              fill="currentColor"
              d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8-8-8z"
            />
          </svg>
        </button>
      </article>
    </div>
  </div>
</template>

<style scoped>
.tools-hub {
  position: relative;
  max-width: 1100px;
  margin: 0 auto;
  padding: 1rem 0 2.5rem;
}

.tools-hub__header {
  text-align: center;
  margin-bottom: 2rem;
}

.tools-hub__title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 800;
  margin: 0 0 0.5rem;
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:root[data-theme='dark'] .tools-hub__title {
  background: linear-gradient(135deg, #a18cd1 0%, #66d9ff 100%);
  -webkit-background-clip: text;
  background-clip: text;
}

.tools-hub__sub {
  margin: 0;
  color: var(--blog-on-glass-muted, rgba(26, 58, 82, 0.75));
  font-size: 0.95rem;
  font-weight: 600;
}

.tools-hub__recent {
  margin: 0.7rem auto 0;
  font-size: 0.82rem;
  opacity: 0.9;
  color: var(--blog-on-glass-muted, rgba(26, 58, 82, 0.75));
}

.tools-hub__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(252px, 1fr));
  gap: 22px 24px;
  align-items: stretch;
}

/* 与首页 ToolsSection 同一套卡片：收藏星 + 圆角模块图标 + 右下角箭头 FAB */
.glass-tool-card {
  border: 2px solid rgba(167, 139, 250, 0.2);
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: var(--glass-shadow, 0 8px 32px rgba(74, 144, 226, 0.15));
}

:root[data-theme='dark'] .glass-tool-card {
  background: rgba(15, 23, 42, 0.35);
  border-color: rgba(167, 139, 250, 0.15);
}

.home-tool-card {
  padding: 24px 24px 22px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 220px;
  cursor: pointer;
  outline: none;
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.28s ease, border-color 0.28s ease;
  position: relative;
  overflow: hidden;
}

.tool-favorite {
  position: absolute;
  top: 10px;
  right: 10px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.28);
  color: rgba(100, 116, 139, 0.95);
  border-radius: 10px;
  width: 30px;
  height: 30px;
  line-height: 1;
  cursor: pointer;
  transition: transform 0.2s ease, color 0.2s ease;
  z-index: 2;
}

.tool-favorite--active {
  color: #f59e0b;
  transform: scale(1.04);
}

.home-tool-card__head {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.home-tool-ico :deep(.tool-module-icon__svg) {
  width: 28px;
  height: 28px;
  filter: drop-shadow(0 1px 6px color-mix(in srgb, var(--tool-color, var(--primary-color)) 36%, transparent));
}

.home-tool-card[data-icon='moyu'] {
  --tool-color: #a855f7;
  --tool-color-2: #7c3aed;
}
.home-tool-card[data-icon='moyu'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(168, 85, 247, 0.32), rgba(124, 58, 237, 0.14));
  color: #6d28d9;
}
.home-tool-card[data-icon='reaction'] {
  --tool-color: #fbbf24;
  --tool-color-2: #f97316;
}
.home-tool-card[data-icon='reaction'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(251, 191, 36, 0.28), rgba(249, 115, 22, 0.16));
  color: #ea580c;
}
.home-tool-card[data-icon='cps'] {
  --tool-color: #fbcfe8;
  --tool-color-2: #f9a8d4;
}
.home-tool-card[data-icon='cps'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(253, 230, 232, 0.75), rgba(249, 168, 212, 0.35));
  color: #db2777;
}
.home-tool-card[data-icon='pomodoro'] {
  --tool-color: #fca5a5;
  --tool-color-2: #f87171;
}
.home-tool-card[data-icon='pomodoro'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(254, 202, 202, 0.45), rgba(248, 113, 113, 0.22));
  color: #dc2626;
}
.home-tool-card[data-icon='schulte'] {
  --tool-color: #3b82f6;
  --tool-color-2: #2563eb;
}
.home-tool-card[data-icon='schulte'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(59, 130, 246, 0.32), rgba(37, 99, 235, 0.14));
  color: #fff;
}
.home-tool-card[data-icon='mbti'] {
  --tool-color: #f472b6;
  --tool-color-2: #ec4899;
}
.home-tool-card[data-icon='mbti'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(244, 114, 182, 0.34), rgba(236, 72, 153, 0.16));
  color: #db2777;
}
.home-tool-card[data-icon='oj'] {
  --tool-color: #94a3b8;
  --tool-color-2: #c4b5fd;
}
.home-tool-card[data-icon='oj'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(148, 163, 184, 0.38), rgba(167, 139, 250, 0.18));
  color: #64748b;
}
.home-tool-card[data-icon='stock'] {
  --tool-color: #f472b6;
  --tool-color-2: #ec4899;
}
.home-tool-card[data-icon='stock'] .home-tool-ico {
  background: linear-gradient(145deg, rgba(244, 114, 182, 0.34), rgba(236, 72, 153, 0.16));
  color: #db2777;
}

.home-tool-ico {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.26s cubic-bezier(0.22, 1, 0.36, 1), color 0.26s ease, background 0.26s ease;
  box-shadow: 0 4px 12px color-mix(in srgb, var(--tool-color, var(--primary-color)) 25%, transparent);
  border: 1px solid color-mix(in srgb, var(--tool-color-2, var(--tool-color, var(--primary-color))) 34%, rgba(255, 255, 255, 0.5));
}

.home-tool-card h3 {
  margin: 4px 0 0;
  font-size: 1.16rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.3;
  color: var(--text-color, #0f172a);
}

:root[data-theme='dark'] .home-tool-card h3 {
  color: #f0f4f8;
}

.home-tool-card__desc {
  margin: 0;
  flex: 1;
  font-size: 0.95rem;
  font-weight: 400;
  color: var(--text-muted);
  line-height: 1.7;
}

:root[data-theme='dark'] .home-tool-card__desc {
  color: rgba(203, 213, 225, 0.85);
}

.home-tool-fab {
  align-self: flex-end;
  width: 52px;
  height: 52px;
  margin-top: auto;
  padding: 0;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(
    145deg,
    var(--tool-color, var(--primary-color)),
    color-mix(in srgb, var(--tool-color, var(--primary-color)) 70%, var(--secondary-color) 30%)
  );
  color: #fff;
  box-shadow:
    0 8px 24px color-mix(in srgb, var(--tool-color, var(--primary-color)) 40%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.28s ease, filter 0.28s ease;
  -webkit-tap-highlight-color: transparent;
}

.home-tool-fab__ico {
  width: 24px;
  height: 24px;
  margin-left: 2px;
}

.home-tool-card:hover {
  transform: translateY(-8px);
  box-shadow:
    0 20px 48px rgba(15, 23, 42, 0.16),
    0 0 32px color-mix(in srgb, var(--tool-color, var(--primary-color)) 25%, transparent);
  border-color: color-mix(in srgb, var(--tool-color, var(--primary-color)) 50%, rgba(255, 255, 255, 0.5));
}

.home-tool-card:hover .home-tool-ico {
  transform: translateY(-2px) scale(1.1);
}

.home-tool-card:hover .home-tool-fab {
  transform: translateX(2px) scale(1.08);
}

.home-tool-fab:hover {
  transform: scale(1.1) translateX(2px);
  filter: brightness(1.05);
}

:root[data-theme='dark'] .home-tool-card:hover {
  background: rgba(30, 27, 58, 0.45);
  border-color: color-mix(in srgb, var(--tool-color, var(--primary-color)) 40%, rgba(255, 255, 255, 0.2));
}
</style>
