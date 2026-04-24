<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToolsStore } from '../../../stores/tools'

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
            <!-- moyu: 手柄 -->
            <svg v-if="card.icon === 'moyu'" viewBox="0 0 24 24" class="home-tool-ico__svg home-tool-ico__svg--moyu">
              <path
                fill="currentColor"
                d="M6.7 5h10.6c1.4 0 2.6 1 2.9 2.3l1.1 5.2c.2.9-.4 1.8-1.3 2l-.4.1h-.2c-.6 0-1.1-.4-1.3-.9l-.2-.6c-.2-.6-.8-1-1.4-1H9.8c-.6 0-1.2.4-1.4 1l-.2.6c-.2.5-.7.9-1.3.9H4.2c-1 0-1.8-.8-1.8-1.8 0-.2 0-.3.1-.4l1.1-5.2C4.1 6 5.3 5 6.7 5zm2.7 3.5c-.8 0-1.5.7-1.5 1.5S8.6 11 9.4 11s1.5-.7 1.5-1.5-.7-1.5-1.5-1.5zm5 0c-.8 0-1.5.7-1.5 1.5s.7 1.5 1.5 1.5 1.5-.7 1.5-1.5-.7-1.5-1.5-1.5z"
              />
            </svg>
            <!-- reaction: 闪电 -->
            <svg v-else-if="card.icon === 'reaction'" viewBox="0 0 24 24" class="home-tool-ico__svg">
              <path
                fill="currentColor"
                d="M11 21h-1l1-7H6.5c-.58 0-.57-.32-.38-.66.19-.34.05-.08.07-.12C8.48 10.94 10.42 7.54 13 3h1l-1 7h3.5c.49 0 .56.33.47.51l-.07.15C12.96 17.55 11 21 11 21z"
              />
            </svg>
            <!-- cps: 竖向鼠标 -->
            <svg v-else-if="card.icon === 'cps'" viewBox="0 0 24 24" class="home-tool-ico__svg home-tool-ico__svg--cps">
              <path
                fill="currentColor"
                d="M12 3.5c-3.05 0-5.5 2.45-5.5 5.5v5.4c0 3.35 2.72 6.1 6.1 6.1h.8c3.38 0 6.1-2.75 6.1-6.1V9c0-3.05-2.45-5.5-5.5-5.5zm0 2c1.93 0 3.5 1.57 3.5 3.5v.35h-7V9c0-1.93 1.57-3.5 3.5-3.5zm-1.25 5.25h2.5c.14 0 .25.11.25.25v2.25c0 1.1-.9 2-2 2s-2-.9-2-2v-2.25c0-.14.11-.25.25-.25z"
              />
            </svg>
            <!-- pomodoro: 番茄 + 绿蒂 -->
            <svg v-else-if="card.icon === 'pomodoro'" viewBox="0 0 24 24" class="home-tool-ico__svg home-tool-ico__svg--tomato">
              <path
                fill="#15803d"
                d="M12 2.2c-.85 0-1.55.55-1.85 1.3-.12.28-.18.58-.18.9 0 .35.08.68.22.98.35-.15.73-.23 1.13-.23h1.36c.4 0 .78.08 1.13.23.14-.3.22-.63.22-.98 0-.32-.06-.62-.18-.9-.3-.75-1-1.3-1.85-1.3z"
              />
              <ellipse cx="12" cy="14.2" rx="7.2" ry="6.9" fill="#dc2626" />
              <path fill="rgba(255,255,255,0.38)" d="M9.2 11.5a4.2 4.8 0 015.6 6.35 6.9 6.9 0 01-6.95-5.9c.35-.55.78-1 1.35-1.45z" />
            </svg>
            <!-- schulte: 2×2 数字格 -->
            <svg v-else-if="card.icon === 'schulte'" viewBox="0 0 24 24" class="home-tool-ico__svg home-tool-ico__svg--schulte">
              <rect x="3" y="3" width="18" height="18" rx="4.5" fill="none" stroke="currentColor" stroke-width="1.35" />
              <line x1="12" y1="3.65" x2="12" y2="20.35" stroke="currentColor" stroke-width="1.1" />
              <line x1="3.65" y1="12" x2="20.35" y2="12" stroke="currentColor" stroke-width="1.1" />
              <text x="7.35" y="10.4" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">1</text>
              <text x="16.65" y="10.4" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">2</text>
              <text x="7.35" y="18.6" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">3</text>
              <text x="16.65" y="18.6" text-anchor="middle" font-size="6.2" font-weight="800" fill="currentColor" font-family="system-ui, -apple-system, sans-serif">4</text>
            </svg>
            <!-- mbti: 大脑轮廓 -->
            <svg v-else-if="card.icon === 'mbti'" viewBox="0 0 24 24" class="home-tool-ico__svg home-tool-ico__svg--brain">
              <path
                fill="currentColor"
                d="M12 4.2c-.9 0-1.7.35-2.35.9-.55-.35-1.2-.55-1.9-.55-1.45 0-2.65 1-2.95 2.35-.85.2-1.55.85-1.75 1.7-.25 1.15.35 2.25 1.35 2.8-.05.25-.05.5 0 .75.15.85.85 1.5 1.7 1.65.1.9.65 1.65 1.45 2 .35 1.65 1.75 2.9 3.45 2.9s3.1-1.25 3.45-2.9c.8-.35 1.35-1.1 1.45-2 .85-.15 1.55-.8 1.7-1.65.05-.25.05-.5 0-.75 1-.55 1.6-1.65 1.35-2.8-.2-.85-.9-1.5-1.75-1.7-.3-1.35-1.5-2.35-2.95-2.35-.7 0-1.35.2-1.9.55-.65-.55-1.45-.9-2.35-.9zm-3.5 3.35c.35 0 .65.08.95.22-.32.45-.52.98-.52 1.58 0 .55.18 1.05.48 1.48-.28.12-.58.18-.9.18-1 0-1.8-.8-1.8-1.8s.8-1.8 1.8-1.8zm7 0c1 0 1.8.8 1.8 1.8s-.8 1.8-1.8 1.8c-.32 0-.62-.06-.9-.18.3-.43.48-.93.48-1.48 0-.6-.2-1.13-.52-1.58.3-.14.6-.22.95-.22zM12 9.6c.85 0 1.55.7 1.55 1.55S12.85 12.7 12 12.7s-1.55-.7-1.55-1.55S11.15 9.6 12 9.6z"
              />
            </svg>
            <!-- oj: 键盘 -->
            <svg v-else-if="card.icon === 'oj'" viewBox="0 0 24 24" class="home-tool-ico__svg home-tool-ico__svg--keyboard">
              <path
                fill="currentColor"
                d="M4 7c-.55 0-1 .45-1 1v8c0 .55.45 1 1 1h16c.55 0 1-.45 1-1V8c0-.55-.45-1-1-1H4zm1.2 2h1.6v1.4H5.2V9zm2.6 0H10v1.4H7.8V9zm2.8 0h2.4v1.4h-2.4V9zm2.8 0H16v1.4h-2.4V9zm2.8 0h1.6v1.4H16V9zM5.2 12.2h2.4v1.4H5.2v-1.4zm3.4 0H13v1.4H8.6v-1.4zm3.6 0h2.4v1.4h-2.4v-1.4zm3.4 0h3.4v1.4H15.6v-1.4zM5.2 15.4h4.6v1.4H5.2v-1.4zm5.6 0H19v1.4h-8.2v-1.4z"
              />
            </svg>
            <!-- lock -->
            <svg v-else-if="card.icon === 'lock'" viewBox="0 0 24 24" class="home-tool-ico__svg">
              <path
                fill="currentColor"
                d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
              />
            </svg>
            <!-- base64 -->
            <svg v-else-if="card.icon === 'b64'" viewBox="0 0 24 24" class="home-tool-ico__svg">
              <path
                fill="currentColor"
                d="M4 4h7v2H6v12h5v2H4V4zm9 4h7v2h-5v8h5v2h-7V8zm-1 4h2v4h-2v-4z"
              />
            </svg>
            <!-- code 兜底 -->
            <svg v-else viewBox="0 0 24 24" class="home-tool-ico__svg">
              <path
                fill="currentColor"
                d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"
              />
            </svg>
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

/* 各工具图标独立颜色（与首页工具栏示意一致） */
.home-tool-card[data-icon="moyu"] .home-tool-ico {
  --tool-color: #a855f7;
  --tool-color-2: #7c3aed;
  background: linear-gradient(145deg, rgba(168, 85, 247, 0.32), rgba(124, 58, 237, 0.14));
  color: #6d28d9;
}
.home-tool-card[data-icon="reaction"] .home-tool-ico {
  --tool-color: #fbbf24;
  --tool-color-2: #f97316;
  background: linear-gradient(145deg, rgba(251, 191, 36, 0.28), rgba(249, 115, 22, 0.16));
  color: #ea580c;
}
.home-tool-card[data-icon="cps"] .home-tool-ico {
  --tool-color: #fbcfe8;
  --tool-color-2: #f9a8d4;
  background: linear-gradient(145deg, rgba(253, 230, 232, 0.75), rgba(249, 168, 212, 0.35));
  color: #db2777;
}
.home-tool-card[data-icon="pomodoro"] .home-tool-ico {
  --tool-color: #fca5a5;
  --tool-color-2: #f87171;
  background: linear-gradient(145deg, rgba(254, 202, 202, 0.45), rgba(248, 113, 113, 0.22));
  color: #dc2626;
}
.home-tool-card[data-icon="schulte"] .home-tool-ico {
  --tool-color: #3b82f6;
  --tool-color-2: #2563eb;
  background: linear-gradient(145deg, rgba(59, 130, 246, 0.32), rgba(37, 99, 235, 0.14));
  color: #fff;
}
.home-tool-card[data-icon="lock"] .home-tool-ico {
  --tool-color: #a78bfa;
  --tool-color-2: #8b5cf6;
  background: linear-gradient(145deg, rgba(167, 139, 250, 0.28), rgba(139, 92, 246, 0.14));
  color: #8b5cf6;
}
.home-tool-card[data-icon="b64"] .home-tool-ico {
  --tool-color: #34d399;
  --tool-color-2: #22c55e;
  background: linear-gradient(145deg, rgba(52, 211, 153, 0.28), rgba(34, 197, 94, 0.14));
  color: #10b981;
}
.home-tool-card[data-icon="mbti"] .home-tool-ico {
  --tool-color: #f472b6;
  --tool-color-2: #ec4899;
  background: linear-gradient(145deg, rgba(244, 114, 182, 0.34), rgba(236, 72, 153, 0.16));
  color: #db2777;
}
.home-tool-card[data-icon="oj"] .home-tool-ico {
  --tool-color: #94a3b8;
  --tool-color-2: #c4b5fd;
  background: linear-gradient(145deg, rgba(148, 163, 184, 0.38), rgba(167, 139, 250, 0.18));
  color: #64748b;
}
.home-tool-card[data-icon="code"] .home-tool-ico,
.home-tool-card:not([data-icon]) .home-tool-ico {
  --tool-color: #66d9ff;
  --tool-color-2: #60a5fa;
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
.home-tool-ico__svg {
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
