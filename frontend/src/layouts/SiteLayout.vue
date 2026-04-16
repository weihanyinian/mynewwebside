<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getToken } from '../utils/token'
import { goToSiteHome } from '../utils/siteHome'
import GlassBreadcrumb from '../components/GlassBreadcrumb.vue'
import { useThemeStore } from '../stores/theme'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

/** 【主题】全站共享 Pinia，与首页 Portfolio 同一套日间/夜间状态 */
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const isLoggedIn = computed(() => !!getToken())

/** 博客/文章等页需要更宽主栏，避免卡片栅格被 1080px 挤乱 */
const isWideMain = computed(() => {
  const p = route.path
  return (
    p.startsWith('/blog') ||
    p.startsWith('/article') ||
    p === '/categories' ||
    p === '/tags' ||
    p.startsWith('/oj')
  )
})

function goHome(hash: string) {
  router.push({ path: '/', hash: hash })
}

/** Portfolio 锚点区高亮：仅当当前在首页且 hash 一致 */
function isSectionActive(hash: string) {
  return route.path === '/' && route.hash === hash
}

/** 顶栏 Logo：全局回主页入口（清除 hash，子页状态随卸载而结束） */
function onSiteLogoClick() {
  goToSiteHome(router)
}

function toggleLocale() {
  locale.value = locale.value === 'zh' ? 'en' : 'zh'
}

</script>

<template>
  <div class="site-root">
    <nav class="glass-nav">
      <div class="nav-inner">
        <div
          class="logo"
          role="link"
          tabindex="0"
          :title="locale === 'zh' ? '返回主页' : 'Home'"
          @click="onSiteLogoClick"
          @keydown.enter.prevent="onSiteLogoClick"
        >
          {{ t('nav.logo') }}
        </div>
        <div class="links">
          <!-- 【全站统一】顶栏导航：site-pill 玻璃态 + 路由/锚点高亮 -->
          <a
            href="#"
            class="site-pill site-pill--nav site-top-anchor"
            :class="{ 'site-pill--active': isSectionActive('#about') }"
            @click.prevent="goHome('#about')"
          >{{ t('nav.about') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-top-anchor"
            :class="{ 'site-pill--active': isSectionActive('#skills') }"
            @click.prevent="goHome('#skills')"
          >{{ t('nav.skills') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-top-anchor"
            :class="{ 'site-pill--active': isSectionActive('#works') }"
            @click.prevent="goHome('#works')"
          >{{ t('nav.works') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-top-anchor"
            :class="{ 'site-pill--active': isSectionActive('#contact') }"
            @click.prevent="goHome('#contact')"
          >{{ t('nav.contact') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav"
            :class="{ 'site-pill--active': route.path === '/message' }"
            @click.prevent="router.push('/message')"
          >{{ t('nav.message') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav moyu-link"
            :class="{ 'site-pill--pink': route.path === '/moyu' }"
            @click.prevent="router.push('/moyu')"
          >{{ t('nav.moyu') }}</a>
          <a href="#" class="site-pill site-pill--nav lang-toggle" :title="t('home.langToggle')" @click.prevent="toggleLocale">
            {{ locale === 'zh' ? 'EN' : '中' }}
          </a>
          <a
            href="#"
            class="site-pill site-pill--nav oj-link"
            :class="{ 'site-pill--active': route.path.startsWith('/oj') }"
            @click.prevent="router.push('/oj')"
          >{{ t('nav.oj') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav"
            :class="{ 'site-pill--active': route.path.startsWith('/blog') || route.path.startsWith('/article') }"
            @click.prevent="router.push('/blog')"
          >{{ t('nav.blog') }}</a>
          <a
            v-if="isLoggedIn"
            href="#"
            class="site-pill site-pill--nav"
            :class="{ 'site-pill--active': route.path.startsWith('/admin') && route.path !== '/admin/login' }"
            @click.prevent="router.push('/admin')"
          >{{ t('nav.admin') }}</a>
          <a
            v-else
            href="#"
            class="site-pill site-pill--nav"
            :class="{ 'site-pill--active': route.path === '/admin/login' }"
            @click.prevent="router.push('/admin/login')"
          >{{ t('nav.admin') }}</a>
          <a href="#" class="site-pill site-pill--nav theme-toggle" :title="t('home.themeToggle')" @click.prevent="themeStore.toggleTheme">
            {{ !isDarkMode ? '🌙' : '☀️' }}
          </a>
        </div>
      </div>
    </nav>

    <main class="site-main" :class="{ 'site-main--wide': isWideMain }">
      <GlassBreadcrumb />
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
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
}

.links a.site-pill {
  text-decoration: none;
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

/* 窄屏：Portfolio 区块锚点始终收起（与原顶栏一致）；其余项仅保留高亮 + 语言/主题/摸鱼/OJ */
@media (max-width: 780px) {
  .links > a.site-top-anchor {
    display: none;
  }
  .links > a.site-pill:not(.site-pill--active):not(.lang-toggle):not(.moyu-link):not(.oj-link):not(.theme-toggle):not(.site-pill--pink) {
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

