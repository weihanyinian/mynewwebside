<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/user'
import { goToSiteHome } from '../utils/siteHome'
import GlassBreadcrumb from '../components/GlassBreadcrumb.vue'
import SiteGlassFooter from '../components/site/SiteGlassFooter.vue'
import { useThemeStore } from '../stores/theme'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

/** 【主题】全站共享 Pinia，与首页 Portfolio 同一套日间/夜间状态 */
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdminUser = computed(() => userStore.isAdmin)

function logout() {
  userStore.logout()
  router.push('/login')
}

/** 博客/文章等页需要更宽主栏，避免卡片栅格被 1080px 挤乱 */
const isWideMain = computed(() => {
  const p = route.path
  return (
    p.startsWith('/blog') ||
    p.startsWith('/article') ||
    p === '/categories' ||
    p === '/tags' ||
    p.startsWith('/tools') ||
    p === '/friends' ||
    p === '/albums' ||
    p === '/snippets' ||
    p === '/search' ||
    p === '/archives' ||
    p === '/stats'
  )
})

/** 工具栏等页：整站壳子必须压过固定定位看板娘 (z-index:999)，否则卡片链接悬停有 href、点击却被挡住 */
const liftAboveMascot = computed(() => {
  const p = route.path
  return (
    p.startsWith('/tools') ||
    p === '/friends' ||
    p === '/albums' ||
    p === '/snippets' ||
    p === '/search' ||
    p === '/archives' ||
    p === '/stats'
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
  <div class="site-root" :class="{ 'site-root--above-mascot': liftAboveMascot }">
    <nav class="glass-nav site-nav-unified">
      <div class="nav-inner">
        <div class="nav-brand-row">
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
          <div class="nav-social" role="navigation" :aria-label="t('sidebar.social')">
            <a
              class="nav-social-link"
              href="https://github.com/weihanyinian"
              target="_blank"
              rel="noopener noreferrer"
            >GitHub</a>
            <a class="nav-social-link" href="mailto:1012308753@qq.com">Email</a>
          </div>
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
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/friends') }"
            @click.prevent="router.push('/friends')"
          >{{ t('breadcrumb.friends') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/albums') }"
            @click.prevent="router.push('/albums')"
          >{{ t('breadcrumb.albums') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/archives') }"
            @click.prevent="router.push('/archives')"
          >{{ t('breadcrumb.archives') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path === '/search' }"
            @click.prevent="router.push('/search')"
          >{{ t('breadcrumb.search') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/snippets') }"
            @click.prevent="router.push('/snippets')"
          >{{ t('breadcrumb.snippets') }}</a>
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
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/tools') }"
            @click.prevent="router.push('/tools')"
          >{{ t('nav.tools') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/blog') || route.path.startsWith('/article') }"
            @click.prevent="router.push('/blog')"
          >{{ t('nav.blog') }}</a>
          <a
            v-if="isAdminUser"
            href="#"
            class="site-pill site-pill--nav site-nav-auth"
            :class="{ 'site-pill--active': route.path.startsWith('/admin') }"
            @click.prevent="router.push('/admin')"
          >{{ t('nav.admin') }}</a>
          <a
            v-if="!isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-nav-auth"
            :class="{ 'site-pill--active': route.path === '/login' }"
            @click.prevent="router.push('/login')"
          >{{ t('nav.login') }}</a>
          <a
            v-if="!isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-nav-auth"
            :class="{ 'site-pill--active': route.path === '/register' }"
            @click.prevent="router.push('/register')"
          >{{ t('nav.register') }}</a>
          <a
            v-if="isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-nav-auth"
            @click.prevent="logout"
          >{{ t('nav.logout') }}</a>
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

    <SiteGlassFooter />
  </div>
</template>

<style scoped>
.site-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.site-root--above-mascot {
  position: relative;
  z-index: 1000;
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

/* 与全局 .site-nav-unified 顶边线叠加一层内高光，不替换整段 box-shadow */
.glass-nav.site-nav-unified {
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.1),
    var(--glass-shadow);
}

:root[data-theme='dark'] .glass-nav.site-nav-unified {
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.06),
    var(--glass-shadow);
}

.nav-inner {
  max-width: min(1400px, 100%);
  margin: 0 auto;
  padding: 12px 16px;
  display: flex;
  flex-wrap: nowrap;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  box-sizing: border-box;
}

.nav-brand-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  min-width: 0;
}

.nav-social {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.nav-social-link {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: 999px;
  text-decoration: none;
  color: var(--text-color, #2c3e50);
  background: rgba(255, 255, 255, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: opacity 0.2s, transform 0.2s;
  white-space: nowrap;
}

:root[data-theme='dark'] .nav-social-link {
  color: #e2e8f0;
  background: rgba(18, 20, 32, 0.55);
  border-color: rgba(255, 255, 255, 0.12);
}

.nav-social-link:hover {
  opacity: 0.92;
  transform: translateY(-1px);
}

.logo {
  font-size: clamp(1rem, 2.2vw, 1.35rem);
  font-weight: 800;
  letter-spacing: -0.5px;
  white-space: nowrap;
  flex-shrink: 0;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  cursor: pointer;
}

.links {
  display: flex;
  flex-wrap: nowrap;
  gap: 5px;
  align-items: center;
  justify-content: flex-end;
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
}

.links::-webkit-scrollbar {
  height: 3px;
}

.links::-webkit-scrollbar-thumb {
  background: rgba(102, 217, 255, 0.35);
  border-radius: 3px;
}

.links a.site-pill {
  text-decoration: none;
  flex-shrink: 0;
}

.site-root .links .site-pill--nav {
  padding: 6px 10px;
  font-size: 0.78rem;
}

.site-root .links .theme-toggle {
  font-size: 1rem;
  padding: 6px 10px;
}

.site-main {
  flex: 1;
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 16px;
}

/* 窄屏：Portfolio 区块锚点始终收起（与原顶栏一致）；其余项仅保留高亮 + 语言/主题/摸鱼/OJ */
@media (max-width: 780px) {
  .nav-social {
    display: none;
  }
  .links > a.site-top-anchor {
    display: none;
  }
  .links > a.site-pill:not(.site-pill--active):not(.lang-toggle):not(.moyu-link):not(.oj-link):not(.theme-toggle):not(.site-pill--pink):not(.site-nav-auth):not(.site-pill--keep-mobile) {
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

