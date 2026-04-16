<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getToken } from '../utils/token'

type ThemeMode = 'light' | 'dark'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

const isLoggedIn = computed(() => !!getToken())

/** 博客/文章等页需要更宽主栏，避免卡片栅格被 1080px 挤乱 */
const isWideMain = computed(() => {
  const p = route.path
  return p.startsWith('/blog') || p.startsWith('/article') || p === '/categories' || p === '/tags'
})

const colorTheme = ref<ThemeMode>('light')

function goHome(hash: string) {
  router.push({ path: '/', hash: hash })
}

function toggleLocale() {
  locale.value = locale.value === 'zh' ? 'en' : 'zh'
}

/** 全站主题：同步 CSS 变量，供博客玻璃态与正文对比度使用 */
function applyTheme(mode: ThemeMode) {
  const root = document.documentElement
  if (mode === 'dark') {
    root.style.setProperty('--bg-gradient', 'linear-gradient(135deg, rgba(20, 38, 51, 0.96) 0%, rgba(18, 28, 40, 0.96) 100%)')
    root.style.setProperty('--text-color', '#eaf8ff')
    root.style.setProperty('--glass-bg', 'rgba(255, 255, 255, 0.12)')
    root.style.setProperty('--glass-border', 'rgba(255, 255, 255, 0.45)')
    root.style.setProperty('--glass-shadow', '0 8px 32px rgba(102, 217, 255, 0.18)')
    root.style.setProperty('--blog-on-glass', '#f0fbff')
    root.style.setProperty('--blog-on-glass-muted', 'rgba(240, 251, 255, 0.82)')
  } else {
    root.style.setProperty('--bg-gradient', 'linear-gradient(135deg, rgba(230, 238, 245, 0.85) 0%, rgba(200, 218, 235, 0.92) 100%)')
    root.style.setProperty('--text-color', '#2c3e50')
    root.style.setProperty('--glass-bg', 'rgba(255, 255, 255, 0.4)')
    root.style.setProperty('--glass-border', 'rgba(255, 255, 255, 0.6)')
    root.style.setProperty('--glass-shadow', '0 8px 32px 0 rgba(74, 144, 226, 0.15)')
    root.style.setProperty('--blog-on-glass', '#1a3a52')
    root.style.setProperty('--blog-on-glass-muted', 'rgba(26, 58, 82, 0.75)')
  }
  root.dataset.theme = mode
}

function toggleTheme() {
  colorTheme.value = colorTheme.value === 'light' ? 'dark' : 'light'
  localStorage.setItem('site-theme', colorTheme.value)
  applyTheme(colorTheme.value)
}

onMounted(() => {
  const saved = localStorage.getItem('site-theme') as ThemeMode | null
  colorTheme.value = saved === 'dark' || saved === 'light' ? saved : 'light'
  applyTheme(colorTheme.value)
})
</script>

<template>
  <div class="site-root">
    <nav class="glass-nav">
      <div class="nav-inner">
        <div class="logo" @click="router.push('/')">{{ t('nav.logo') }}</div>
        <div class="links">
          <a @click="goHome('#about')">{{ t('nav.about') }}</a>
          <a @click="goHome('#skills')">{{ t('nav.skills') }}</a>
          <a @click="goHome('#works')">{{ t('nav.works') }}</a>
          <a @click="goHome('#contact')">{{ t('nav.contact') }}</a>
          <a @click="router.push('/message')">{{ t('nav.message') }}</a>
          <a @click="router.push('/moyu')" :class="{ active: router.currentRoute.value.path === '/moyu', 'moyu-link': true }">{{ t('nav.moyu') }}</a>
          <a @click="toggleLocale" class="lang-toggle" :title="t('home.langToggle')">
            {{ locale === 'zh' ? 'EN' : '中' }}
          </a>
          <a
            @click="router.push('/blog')"
            :class="{ active: route.path.startsWith('/blog') || route.path.startsWith('/article') }"
          >{{ t('nav.blog') }}</a>
          <a v-if="isLoggedIn" @click="router.push('/admin')">{{ t('nav.admin') }}</a>
          <a v-else @click="router.push('/admin/login')">{{ t('nav.admin') }}</a>
          <a class="theme-toggle" :title="t('home.themeToggle')" @click="toggleTheme">
            {{ colorTheme === 'light' ? '🌙' : '☀️' }}
          </a>
        </div>
      </div>
    </nav>

    <main class="site-main" :class="{ 'site-main--wide': isWideMain }">
      <slot />
    </main>

    <footer class="site-footer">
      <div class="site-footer__inner">
        <p>© 2026 {{ t('home.footer') }}</p>
        <div class="footer-links">
          <a href="https://github.com/weihanyinian" target="_blank" title="GitHub">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
            </svg>
          </a>
          <a href="mailto:1012308753@qq.com" title="Email">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z" />
            </svg>
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.site-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.glass-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
}

.nav-inner {
  max-width: 1080px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  cursor: pointer;
}

.links {
  display: flex;
  gap: 24px;
  align-items: center;
}

.links a {
  cursor: pointer;
  font-weight: 600;
  color: var(--text-color);
  transition: color 0.3s;
  text-decoration: none;
}

.links a:hover, .links a.active {
  color: var(--primary-color);
}

.site-main {
  flex: 1;
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 16px;
}

.site-footer {
  border-top: 1px solid var(--glass-border);
  background: var(--glass-bg);
  backdrop-filter: blur(8px);
  color: var(--text-color);
}

.site-footer__inner {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.site-footer__inner p {
  margin: 0;
  font-weight: 500;
}

.footer-links {
  display: flex;
  gap: 16px;
}

.footer-links a {
  color: var(--text-color);
  transition: all 0.3s;
  opacity: 0.7;
}

.footer-links a:hover {
  color: var(--primary-color);
  opacity: 1;
  transform: translateY(-2px);
}

@media (max-width: 780px) {
  .links a:not(.active):not(.lang-toggle):not(.moyu-link):not(.theme-toggle) {
    display: none;
  }
}

.site-main--wide {
  max-width: min(1320px, 100%);
  padding-left: 20px;
  padding-right: 20px;
}

.theme-toggle {
  font-size: 1.1rem;
  opacity: 0.9;
}
</style>

