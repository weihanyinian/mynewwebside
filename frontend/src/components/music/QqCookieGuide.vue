<script setup lang="ts">
/**
 * QQ 音乐 Cookie 绑定说明：分步、面向新手；优先推荐「网络 → 请求标头 → cookie」复制法。
 */
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

withDefaults(
  defineProps<{
    /** 播放器内窄面板用更小字号 */
    compact?: boolean
    /** 未登录本站时提示先登录 */
    requireSiteUser?: boolean
  }>(),
  { compact: false, requireSiteUser: false },
)

const { t } = useI18n()

const methodBKeys = [
  'pages.musicQqCookieB1',
  'pages.musicQqCookieB2',
  'pages.musicQqCookieB3',
  'pages.musicQqCookieB4',
  'pages.musicQqCookieB5',
  'pages.musicQqCookieB6',
  'pages.musicQqCookieB7',
] as const

const methodAKeys = ['pages.musicQqCookieA1', 'pages.musicQqCookieA2', 'pages.musicQqCookieA3'] as const

const stepsB = computed(() => methodBKeys.map((k) => t(k)))
const stepsA = computed(() => methodAKeys.map((k) => t(k)))
</script>

<template>
  <div class="qq-guide" :class="{ 'qq-guide--compact': compact }">
    <p v-if="requireSiteUser" class="qq-guide__warn">{{ t('pages.musicQqCookieLeadNeedSite') }}</p>
    <p class="qq-guide__lead">{{ t('pages.musicQqCookieLead') }}</p>
    <p class="qq-guide__link">
      <a href="https://y.qq.com" target="_blank" rel="noopener noreferrer">{{ t('pages.musicQqCookieOpenYqq') }}</a>
    </p>
    <details class="qq-guide__details">
      <summary class="qq-guide__summary">{{ t('pages.musicQqCookieDetailsSummary') }}</summary>
      <div class="qq-guide__body">
        <h4 class="qq-guide__h">{{ t('pages.musicQqCookieMethodBTitle') }}</h4>
        <ol class="qq-guide__ol">
          <li v-for="(line, i) in stepsB" :key="'b' + i" class="qq-guide__li">{{ line }}</li>
        </ol>
        <h4 class="qq-guide__h">{{ t('pages.musicQqCookieMethodATitle') }}</h4>
        <ol class="qq-guide__ol">
          <li v-for="(line, i) in stepsA" :key="'a' + i" class="qq-guide__li">{{ line }}</li>
        </ol>
        <p class="qq-guide__note">{{ t('pages.musicQqCookieNote') }}</p>
      </div>
    </details>
  </div>
</template>

<style scoped>
.qq-guide {
  font-size: 0.88rem;
  line-height: 1.55;
  color: inherit;
}

.qq-guide--compact {
  font-size: 0.72rem;
  line-height: 1.45;
}

.qq-guide__warn {
  margin: 0 0 0.4rem;
  padding: 0.35rem 0.45rem;
  border-radius: 8px;
  background: rgba(192, 57, 43, 0.12);
  font-weight: 700;
  font-size: 0.92em;
}

.qq-guide__lead {
  margin: 0 0 0.35rem;
  opacity: 0.95;
}

.qq-guide__link {
  margin: 0 0 0.5rem;
  font-size: 0.92em;
}

.qq-guide__link a {
  color: #4a90e2;
  font-weight: 600;
}

.qq-guide__details {
  margin-top: 0.35rem;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.qq-guide--compact .qq-guide__body {
  max-height: min(48vh, 280px);
  overflow-y: auto;
}

.qq-guide__summary {
  cursor: pointer;
  padding: 0.45rem 0.55rem;
  font-weight: 700;
  list-style: none;
  user-select: none;
}

.qq-guide__summary::-webkit-details-marker {
  display: none;
}

.qq-guide__summary::before {
  content: '▸ ';
  opacity: 0.7;
}

details[open] > .qq-guide__summary::before {
  content: '▾ ';
}

.qq-guide__body {
  padding: 0 0.55rem 0.6rem;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.qq-guide__h {
  margin: 0.5rem 0 0.25rem;
  font-size: 0.95em;
  font-weight: 800;
}

.qq-guide__ol {
  margin: 0 0 0.35rem;
  padding-left: 1.2rem;
}

.qq-guide__li {
  margin-bottom: 0.35rem;
}

.qq-guide__note {
  margin: 0.6rem 0 0;
  font-size: 0.88em;
  opacity: 0.85;
}

:root[data-theme='dark'] .qq-guide__details {
  border-color: rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.06);
}

:root[data-theme='dark'] .qq-guide__body {
  border-top-color: rgba(255, 255, 255, 0.08);
}

:root[data-theme='dark'] .qq-guide__link a {
  color: #7eb8ff;
}
</style>
