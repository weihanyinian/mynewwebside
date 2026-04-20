<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

const toolCards = computed(() => [
  {
    path: '/tools/reaction',
    title: t('tools.reaction'),
    desc: t('toolsHub.cardReactionDesc'),
    icon: 'reaction',
  },
  {
    path: '/tools/cps',
    title: t('tools.cps'),
    desc: t('toolsHub.cardCpsDesc'),
    icon: 'cps',
  },
  {
    path: '/tools/pomodoro',
    title: t('tools.pomodoro'),
    desc: t('toolsHub.cardPomodoroDesc'),
    icon: 'pomodoro',
  },
  {
    path: '/tools/password',
    title: t('tools.password'),
    desc: t('toolsHub.cardPasswordDesc'),
    icon: 'lock',
  },
  {
    path: '/tools/base64',
    title: t('tools.base64'),
    desc: t('toolsHub.cardBase64Desc'),
    icon: 'b64',
  },
  {
    path: '/tools/oj',
    title: t('toolsHub.cardOjTitle'),
    desc: t('toolsHub.cardOjDesc'),
    icon: 'code',
  },
])

function openTool(path: string) {
  void router.push(path)
}
</script>

<template>
  <section class="section" id="tools">
    <h2>{{ t('toolsHub.title') }}</h2>
    <p class="section-sub">{{ t('home.sectionToolsLead') }}</p>
    <div class="home-tools-grid">
      <article
        v-for="card in toolCards"
        :key="card.path"
        class="site-module-card home-tool-card"
        role="button"
        tabindex="0"
        :aria-label="card.title"
        @click="openTool(card.path)"
        @keydown.enter.prevent="openTool(card.path)"
        @keydown.space.prevent="openTool(card.path)"
      >
        <div class="home-tool-card__head">
          <span class="home-tool-ico" aria-hidden="true">
            <!-- reaction: 闪电 -->
            <svg v-if="card.icon === 'reaction'" viewBox="0 0 24 24" class="home-tool-ico__svg">
              <path
                fill="currentColor"
                d="M11 21h-1l1-7H6.5c-.58 0-.57-.32-.38-.66.19-.34.05-.08.07-.12C8.48 10.94 10.42 7.54 13 3h1l-1 7h3.5c.49 0 .56.33.47.51l-.07.15C12.96 17.55 11 21 11 21z"
              />
            </svg>
            <!-- cps: 光标点击 -->
            <svg v-else-if="card.icon === 'cps'" viewBox="0 0 24 24" class="home-tool-ico__svg">
              <path
                fill="currentColor"
                d="M9 11.24V7.5C9 5.57 10.57 4 12.5 4S16 5.57 16 7.5v3.74c1.21.81 2 2.18 2 3.76 0 2.49-2.01 4.5-4.5 4.5s-4.5-2.01-4.5-4.5c0-1.58.79-2.95 2-3.76zm2.5-1.24h2V7.5c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v2.5zM12 20c1.93 0 3.5-1.57 3.5-3.5S13.93 13 12 13s-3.5 1.57-3.5 3.5S10.07 20 12 20z"
              />
            </svg>
            <!-- pomodoro: 计时 / 番茄钟意象 -->
            <svg v-else-if="card.icon === 'pomodoro'" viewBox="0 0 24 24" class="home-tool-ico__svg">
              <path
                fill="currentColor"
                d="M15 1H9v2h6V1zm3.03 7.39l1.42-1.42c-.43-.51-.9-.99-1.41-1.41L16.62 6c-1.54-1.26-3.49-2-5.62-2-4.97 0-9 4.03-9 9s4.02 9 9 9 9-4.03 9-9c0-2.12-.74-4.07-1.97-5.61zM12 20c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7zm1-13h-2v6l5.25 3.15.75-1.23-4-2.42V7z"
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
            <!-- code / OJ -->
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
          @click.stop="openTool(card.path)"
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
.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  color: var(--text-muted, rgba(15, 23, 42, 0.68));
  line-height: 1.72;
  letter-spacing: 0.02em;
  font-weight: 400;
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
  padding: 22px 22px 20px;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 218px;
  cursor: pointer;
  outline: none;
}
.home-tool-card:focus-visible {
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary-color) 40%, transparent);
}
.home-tool-card__head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.home-tool-ico {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, color-mix(in srgb, var(--primary-color) 22%, transparent), transparent);
  color: var(--primary-color);
  flex-shrink: 0;
}
.home-tool-ico__svg {
  width: 26px;
  height: 26px;
}
.home-tool-card h3 {
  margin: 2px 0 0;
  font-size: 1.06rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.3;
  color: var(--text-color, #0f172a);
}
.home-tool-card__desc {
  margin: 0;
  flex: 1;
  font-size: 0.875rem;
  font-weight: 400;
  color: var(--text-muted);
  line-height: 1.65;
  letter-spacing: 0.02em;
}
.home-tool-fab {
  align-self: flex-end;
  width: 50px;
  height: 50px;
  margin-top: auto;
  padding: 0;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(145deg, var(--primary-color, #4a90e2), var(--secondary-color, #8b7fd8));
  color: #fff;
  box-shadow:
    0 8px 22px color-mix(in srgb, var(--primary-color, #4a90e2) 38%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
  transition:
    transform 0.28s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.28s ease,
    filter 0.28s ease;
  -webkit-tap-highlight-color: transparent;
}
.home-tool-fab:hover {
  transform: scale(1.08);
  box-shadow:
    0 14px 32px color-mix(in srgb, var(--primary-color, #4a90e2) 45%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.28);
  filter: brightness(1.03);
}
.home-tool-fab:active {
  transform: scale(0.96);
}
.home-tool-fab__ico {
  width: 22px;
  height: 22px;
  margin-left: 2px;
}
</style>
