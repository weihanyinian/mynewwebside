<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const icpNo = computed(() => import.meta.env.VITE_PUBLIC_ICP_NO || '桂ICP备2025060137号-2')
const icpUrl = computed(() => import.meta.env.VITE_PUBLIC_ICP_URL || 'https://beian.miit.gov.cn/')
const psb = computed(() => import.meta.env.VITE_PUBLIC_PSB_NO || '')
const psbUrl = computed(() => import.meta.env.VITE_PUBLIC_PSB_URL || '#')

const loveStart = new Date(2025, 5, 18, 23, 0, 0)
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | null = null

const loveDuration = computed(() => {
  const diffMs = Math.max(0, now.value - loveStart.getTime())
  const totalSec = Math.floor(diffMs / 1000)
  const days = Math.floor(totalSec / 86400)
  const hours = Math.floor((totalSec % 86400) / 3600)
  const minutes = Math.floor((totalSec % 3600) / 60)
  const seconds = totalSec % 60
  return `${days} 天 ${hours} 小时 ${minutes} 分 ${seconds} 秒`
})

onMounted(() => {
  timer = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <footer class="site-rich-footer">
    <div class="site-rich-footer__friends">
      <span class="friends-label">{{ t('footer.friendsTitle') }}</span>
      <a href="https://github.com/weihanyinian" target="_blank" rel="noopener noreferrer" class="friends-link">{{
        t('footer.friendGithub')
      }}</a>
      <span class="friends-sep">·</span>
      <router-link to="/blog" class="friends-link">{{ t('footer.friendBlog') }}</router-link>
    </div>
    <div class="site-rich-footer__filings">
      <span class="filing-item">
        <span class="filing-label">{{ t('footer.icpLabel') }}</span>
        <a :href="icpUrl" target="_blank" rel="noopener noreferrer" class="filing-link">{{ icpNo }}</a>
      </span>
      <template v-if="psb">
        <span class="filing-sep">|</span>
        <span class="filing-item">
          <span class="filing-label">{{ t('footer.psbLabel') }}</span>
          <a :href="psbUrl" target="_blank" rel="noopener noreferrer" class="filing-link">{{ psb }}</a>
        </span>
      </template>
      <span class="filing-sep">|</span>
      <span class="filing-muted">{{ t('footer.siteDomain') }}</span>
    </div>

    <div class="site-rich-footer__copy">
      <span class="footer-glow-dot" aria-hidden="true" />
      <p>© 2026 {{ t('footer.copyName') }}</p>
    </div>
    <div class="site-rich-footer__love-timer">
      <p class="love-timer__title">恋爱计时器（从 2025-06-18 23:00 开始）</p>
      <p class="love-timer__value">我们已经在一起 {{ loveDuration }} ❤️</p>
    </div>
  </footer>
</template>

<style scoped>
.site-rich-footer {
  margin-top: auto;
  padding: 24px 16px 32px;
  border-top: 1px solid var(--footer-border, rgba(255, 255, 255, 0.22));
  background: var(--footer-bg, rgba(255, 255, 255, 0.22));
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  color: var(--text-color);
}

:root[data-theme='dark'] .site-rich-footer {
  --footer-bg: rgba(12, 14, 24, 0.55);
  --footer-border: rgba(255, 255, 255, 0.1);
}

.site-rich-footer__friends {
  max-width: 920px;
  margin: 0 auto 12px;
  text-align: center;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-color);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0.35rem 0.5rem;
}

.friends-label {
  opacity: 0.55;
  font-weight: 600;
  margin-right: 0.25rem;
}

.friends-link {
  color: var(--primary-color);
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.friends-link:hover {
  opacity: 0.85;
  text-decoration: underline;
}

.friends-sep {
  opacity: 0.35;
  user-select: none;
}

.site-rich-footer__filings {
  max-width: 920px;
  margin: 0 auto 14px;
  text-align: center;
  font-size: 0.78rem;
  color: var(--text-color);
  opacity: 0.72;
}

.filing-link {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s ease;
}

.filing-link:hover {
  color: var(--primary-color);
}

.filing-sep {
  margin: 0 0.5rem;
  opacity: 0.45;
}

.filing-muted {
  opacity: 0.65;
}

.filing-item {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
  justify-content: center;
}

.filing-label {
  opacity: 0.55;
  font-size: 0.72rem;
}

.site-rich-footer__copy {
  text-align: center;
  position: relative;
}

.site-rich-footer__copy p {
  margin: 0.35rem 0 0;
  font-size: 0.85rem;
  font-weight: 600;
  opacity: 0.88;
}

.site-rich-footer__love-timer {
  margin: 14px auto 0;
  max-width: 920px;
  text-align: center;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: rgba(255, 255, 255, 0.14);
  padding: 10px 12px;
  box-shadow: 0 8px 24px rgba(74, 144, 226, 0.12);
}

.love-timer__title {
  margin: 0;
  font-size: 0.78rem;
  font-weight: 700;
  color: color-mix(in srgb, var(--primary-color, #4a90e2) 62%, var(--text-color, #0f172a) 38%);
}

.love-timer__value {
  margin: 0.35rem 0 0;
  font-size: 0.92rem;
  font-weight: 700;
  letter-spacing: 0.01em;
  color: color-mix(in srgb, var(--text-color, #0f172a) 86%, var(--secondary-color, #8b7fd8) 14%);
}

:root[data-theme='dark'] .site-rich-footer__love-timer {
  border-color: rgba(148, 163, 184, 0.32);
  background: rgba(15, 23, 42, 0.55);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.42);
}

:root[data-theme='dark'] .love-timer__title {
  color: #c4b5fd;
}

:root[data-theme='dark'] .love-timer__value {
  color: rgba(241, 245, 249, 0.95);
}

.footer-glow-dot {
  display: block;
  width: 10px;
  height: 10px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: radial-gradient(circle, #7dd3fc 0%, #4a90e2 45%, transparent 70%);
  box-shadow: 0 0 18px rgba(102, 217, 255, 0.55);
}
</style>
