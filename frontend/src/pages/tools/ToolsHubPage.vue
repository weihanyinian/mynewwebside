<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

type Card = { id: string; icon: string; titleKey: string; descKey: string; to: string; accent?: boolean }

const cards = computed((): Card[] => [
  { id: 'oj', icon: '⌨️', titleKey: 'toolsHub.cardOjTitle', descKey: 'toolsHub.cardOjDesc', to: '/tools/oj' },
  { id: 'reaction', icon: '⚡', titleKey: 'tools.reaction', descKey: 'toolsHub.cardReactionDesc', to: '/tools/reaction' },
  { id: 'cps', icon: '🖱️', titleKey: 'tools.cps', descKey: 'toolsHub.cardCpsDesc', to: '/tools/cps' },
  { id: 'pomodoro', icon: '🍅', titleKey: 'tools.pomodoro', descKey: 'toolsHub.cardPomodoroDesc', to: '/tools/pomodoro' },
  { id: 'password', icon: '🔑', titleKey: 'tools.password', descKey: 'toolsHub.cardPasswordDesc', to: '/tools/password' },
  { id: 'base64', icon: '🔤', titleKey: 'tools.base64', descKey: 'toolsHub.cardBase64Desc', to: '/tools/base64' },
  { id: 'mbti', icon: '🧠', titleKey: 'toolsHub.cardMbtiTitle', descKey: 'toolsHub.cardMbtiDesc', to: '/tools/mbti' },
])
</script>

<template>
  <div class="tools-hub">
    <header class="tools-hub__header">
      <h1 class="tools-hub__title">{{ t('toolsHub.title') }}</h1>
      <p class="tools-hub__sub">{{ t('toolsHub.subtitle') }}</p>
    </header>

    <div class="tools-hub__grid">
      <!-- RouterLink 与 router.push(to) 等价，无子元素拦截冒泡 -->
      <RouterLink
        v-for="c in cards"
        :key="c.id"
        :to="c.to"
        class="tool-card glass-tool-card"
        :class="{ 'tool-card--accent': c.accent }"
      >
        <span class="tool-card__icon" aria-hidden="true">{{ c.icon }}</span>
        <span class="tool-card__name">{{ t(c.titleKey) }}</span>
        <span class="tool-card__desc">{{ t(c.descKey) }}</span>
      </RouterLink>
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

.tools-hub__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1.25rem;
}

.tool-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-align: left;
  padding: 1.35rem 1.25rem;
  border-radius: 20px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  box-sizing: border-box;
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.55));
  background: var(--glass-bg, rgba(255, 255, 255, 0.35));
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  box-shadow: var(--glass-shadow, 0 8px 32px rgba(74, 144, 226, 0.15));
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.2s ease;
  color: var(--text-color, #2c3e50);
  font: inherit;
}

.tool-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 14px 40px rgba(102, 217, 255, 0.28);
  border-color: rgba(102, 217, 255, 0.65);
}

.tool-card--accent {
  border-color: rgba(252, 162, 229, 0.45);
}

.tool-card--accent:hover {
  box-shadow: 0 14px 40px rgba(252, 162, 229, 0.22);
}

.tool-card__icon {
  font-size: 2rem;
  line-height: 1;
  margin-bottom: 0.65rem;
}

.tool-card__name {
  font-size: 1.05rem;
  font-weight: 800;
  margin-bottom: 0.35rem;
  color: var(--blog-on-glass, #1a3a52);
}

:root[data-theme='dark'] .tool-card__name {
  color: #f0fbff;
}

.tool-card__desc {
  font-size: 0.82rem;
  font-weight: 600;
  line-height: 1.45;
  color: var(--blog-on-glass-muted, rgba(26, 58, 82, 0.72));
}

:root[data-theme='dark'] .tool-card__desc {
  color: rgba(240, 251, 255, 0.78);
}
</style>
