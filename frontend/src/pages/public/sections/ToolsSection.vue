<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToolsStore } from '../../../stores/tools'
import ToolModuleIcon from '../../../components/tools/ToolModuleIcon.vue'

const router = useRouter()
const { t } = useI18n()
const toolsStore = useToolsStore()
toolsStore.hydrate()

const toolCards = computed(() =>
  toolsStore.tools.map((card) => ({
    ...card,
    title: t(card.titleKey),
    desc: t(card.descKey),
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
  <section class="section" id="tools">
    <h2 class="section-title-pill">{{ t('toolsHub.title') }}</h2>
    <p class="section-sub section-lead-pill">{{ t('home.sectionToolsLead') }}</p>
    <div class="home-tools-grid">
      <article
        v-for="card in toolCards"
        :key="card.path"
        class="site-module-card home-tool-card"
        :data-icon="card.icon"
        role="button"
        tabindex="0"
        :aria-label="card.title"
        @click="openTool(card.id, card.path)"
        @keydown.enter.prevent="openTool(card.id, card.path)"
        @keydown.space.prevent="openTool(card.id, card.path)"
      >
        <button
          type="button"
          class="tool-favorite"
          :class="{ 'tool-favorite--active': toolsStore.favorites.includes(card.id) }"
          :title="toolsStore.favorites.includes(card.id) ? '取消收藏' : '收藏工具'"
          @click.stop="toggleFavorite(card.id)"
        >
          ★
        </button>
        <div class="home-tool-card__head">
          <span class="home-tool-ico" aria-hidden="true">
            <ToolModuleIcon :icon="card.icon" />
          </span>
          <h3>{{ card.title }}</h3>
        </div>
        <p class="home-tool-card__desc">{{ card.desc }}</p>
        <button
          type="button"
          class="home-tool-fab"
          :aria-label="t('home.sectionToolsOpen')"
          :title="t('home.sectionToolsOpen')"
          @click.stop="openTool(card.id, card.path)"
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
  </section>
</template>

<style scoped>
.section-title-pill {
  width: fit-content;
  max-width: min(92vw, 560px);
  margin: 0 auto 0.95rem;
  padding: 0.48rem 1.05rem;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--primary-color, #4a90e2) 34%, rgba(255, 255, 255, 0.7));
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.36), rgba(255, 255, 255, 0.16));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: color-mix(in srgb, var(--primary-color, #4a90e2) 62%, #1e293b 38%);
  -webkit-text-fill-color: currentColor;
  background-clip: border-box;
  -webkit-background-clip: border-box;
  text-shadow: 0 1px 10px rgba(74, 144, 226, 0.16);
  box-shadow: 0 8px 26px rgba(74, 144, 226, 0.1);
}
.section-lead-pill {
  width: fit-content;
  max-width: min(92vw, 720px);
  margin: 0 auto 1.5rem;
  padding: 0.45rem 0.9rem;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0.13));
  box-shadow: 0 6px 18px rgba(59, 130, 246, 0.08);
  color: color-mix(in srgb, var(--text-color, #0f172a) 80%, var(--primary-color, #4a90e2) 20%);
  -webkit-text-fill-color: currentColor;
  background-clip: border-box;
  -webkit-background-clip: border-box;
}
:root[data-theme='dark'] .section-title-pill {
  border-color: rgba(167, 139, 250, 0.42);
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.5), rgba(15, 23, 42, 0.42));
  color: #f1f5f9;
  text-shadow: 0 1px 14px rgba(167, 139, 250, 0.22);
}
:root[data-theme='dark'] .section-lead-pill {
  border-color: rgba(148, 163, 184, 0.28);
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.45), rgba(15, 23, 42, 0.35));
  color: rgba(226, 232, 240, 0.94);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.24);
}
.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  color: var(--text-muted, rgba(15, 23, 42, 0.68));
  line-height: 1.72;
  letter-spacing: 0.02em;
  font-size: 0.98rem;
  font-weight: 450;
}
.home-tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(252px, 1fr));
  gap: 22px 24px;
  max-width: 1100px;
  margin: 0 auto;
  align-items: stretch;
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
  border: 2px solid rgba(167, 139, 250, 0.2);
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
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
}
.tool-favorite--active {
  color: #f59e0b;
  transform: scale(1.04);
}

/* 卡片顶部装饰线 */
.home-tool-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 20px;
  right: 20px;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--tool-color, #a78bfa), transparent);
  border-radius: 0 0 3px 3px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.home-tool-card:hover::before {
  opacity: 1;
}

.home-tool-card:focus-visible {
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--tool-color, var(--primary-color)) 45%, transparent);
  border-color: color-mix(in srgb, var(--tool-color, var(--primary-color)) 60%, transparent);
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
.home-tool-card:hover h3 {
  text-shadow: 0 0 12px color-mix(in srgb, var(--tool-color, var(--primary-color)) 35%, transparent);
}
.home-tool-card__head {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

/* 各工具图标独立颜色；--tool-color 写在卡片上以便右下角 FAB 继承 */
.home-tool-card[data-icon="moyu"] {
  --tool-color: #a855f7;
  --tool-color-2: #7c3aed;
}
.home-tool-card[data-icon="moyu"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(168, 85, 247, 0.32), rgba(124, 58, 237, 0.14));
  color: #6d28d9;
}
.home-tool-card[data-icon="reaction"] {
  --tool-color: #fbbf24;
  --tool-color-2: #f97316;
}
.home-tool-card[data-icon="reaction"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(251, 191, 36, 0.28), rgba(249, 115, 22, 0.16));
  color: #ea580c;
}
.home-tool-card[data-icon="cps"] {
  --tool-color: #fbcfe8;
  --tool-color-2: #f9a8d4;
}
.home-tool-card[data-icon="cps"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(253, 230, 232, 0.75), rgba(249, 168, 212, 0.35));
  color: #db2777;
}
.home-tool-card[data-icon="pomodoro"] {
  --tool-color: #fca5a5;
  --tool-color-2: #f87171;
}
.home-tool-card[data-icon="pomodoro"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(254, 202, 202, 0.45), rgba(248, 113, 113, 0.22));
  color: #dc2626;
}
.home-tool-card[data-icon="schulte"] {
  --tool-color: #3b82f6;
  --tool-color-2: #2563eb;
}
.home-tool-card[data-icon="schulte"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(59, 130, 246, 0.32), rgba(37, 99, 235, 0.14));
  color: #fff;
}
.home-tool-card[data-icon="lock"] {
  --tool-color: #a78bfa;
  --tool-color-2: #8b5cf6;
}
.home-tool-card[data-icon="lock"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(167, 139, 250, 0.28), rgba(139, 92, 246, 0.14));
  color: #8b5cf6;
}
.home-tool-card[data-icon="b64"] {
  --tool-color: #34d399;
  --tool-color-2: #22c55e;
}
.home-tool-card[data-icon="b64"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(52, 211, 153, 0.28), rgba(34, 197, 94, 0.14));
  color: #10b981;
}
.home-tool-card[data-icon="mbti"] {
  --tool-color: #f472b6;
  --tool-color-2: #ec4899;
}
.home-tool-card[data-icon="mbti"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(244, 114, 182, 0.34), rgba(236, 72, 153, 0.16));
  color: #db2777;
}
.home-tool-card[data-icon="oj"] {
  --tool-color: #94a3b8;
  --tool-color-2: #c4b5fd;
}
.home-tool-card[data-icon="oj"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(148, 163, 184, 0.38), rgba(167, 139, 250, 0.18));
  color: #64748b;
}
.home-tool-card[data-icon="stock"] {
  --tool-color: #f472b6;
  --tool-color-2: #ec4899;
}
.home-tool-card[data-icon="stock"] .home-tool-ico {
  background: linear-gradient(145deg, rgba(244, 114, 182, 0.34), rgba(236, 72, 153, 0.16));
  color: #db2777;
}
.home-tool-card[data-icon="code"],
.home-tool-card:not([data-icon]) {
  --tool-color: #66d9ff;
  --tool-color-2: #60a5fa;
}
.home-tool-card[data-icon="code"] .home-tool-ico,
.home-tool-card:not([data-icon]) .home-tool-ico {
  background: linear-gradient(145deg, rgba(102, 217, 255, 0.3), rgba(96, 165, 250, 0.14));
  color: #0ea5e9;
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
.home-tool-ico :deep(.tool-module-icon__svg) {
  width: 28px;
  height: 28px;
  filter: drop-shadow(0 1px 6px color-mix(in srgb, var(--tool-color, var(--primary-color)) 36%, transparent));
}
.home-tool-card h3 {
  margin: 4px 0 0;
  font-size: 1.16rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.3;
  color: var(--text-color, #0f172a);
  transition: color 0.26s ease, text-shadow 0.26s ease;
}
.home-tool-card__desc {
  margin: 0;
  flex: 1;
  font-size: 0.95rem;
  font-weight: 400;
  color: var(--text-muted);
  line-height: 1.7;
  letter-spacing: 0.01em;
  transition: color 0.24s ease;
}
.home-tool-card:hover .home-tool-card__desc {
  color: color-mix(in srgb, var(--text-muted) 65%, #000 35%);
}
.dark-theme .home-tool-card:hover .home-tool-card__desc {
  color: color-mix(in srgb, var(--text-muted) 65%, #fff 35%);
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
  background: linear-gradient(145deg, var(--tool-color, var(--primary-color)), color-mix(in srgb, var(--tool-color, var(--primary-color)) 70%, var(--secondary-color) 30%));
  color: #fff;
  box-shadow:
    0 8px 24px color-mix(in srgb, var(--tool-color, var(--primary-color)) 40%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition:
    transform 0.28s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.28s ease,
    filter 0.28s ease;
  -webkit-tap-highlight-color: transparent;
}
.home-tool-fab:hover {
  transform: scale(1.1) translateX(2px);
  box-shadow:
    0 12px 36px color-mix(in srgb, var(--tool-color, var(--primary-color)) 50%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.35);
  filter: brightness(1.05);
}
.home-tool-card:hover .home-tool-fab {
  transform: translateX(2px) scale(1.08);
}
.home-tool-fab:active {
  transform: scale(0.95);
}
.home-tool-fab__ico {
  width: 24px;
  height: 24px;
  margin-left: 2px;
}

/* Dark Theme 支持 */
.dark-theme .home-tool-card {
  background: rgba(15, 23, 42, 0.35);
  border-color: rgba(167, 139, 250, 0.15);
}

.dark-theme .home-tool-card:hover {
  background: rgba(30, 27, 58, 0.45);
  border-color: color-mix(in srgb, var(--tool-color, var(--primary-color)) 40%, rgba(255, 255, 255, 0.2));
}

.dark-theme .home-tool-ico {
  box-shadow: 0 4px 16px color-mix(in srgb, var(--tool-color, var(--primary-color)) 30%, transparent);
}

.dark-theme .home-tool-card h3 {
  color: #f0f4f8;
}

.dark-theme .home-tool-card .home-tool-card__desc {
  color: rgba(203, 213, 225, 0.85);
}
</style>
