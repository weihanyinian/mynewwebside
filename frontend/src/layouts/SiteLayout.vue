<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../stores/user'
import { goToSiteHome } from '../utils/siteHome'
import GlassBreadcrumb from '../components/GlassBreadcrumb.vue'
import SiteGlassFooter from '../components/site/SiteGlassFooter.vue'
import SiteBackToTop from '../components/site/SiteBackToTop.vue'
import GlobalSearch from '../components/GlobalSearch.vue'
import SiteTopNav from '../components/site/SiteTopNav.vue'
import NewsletterSignup from '../components/NewsletterSignup.vue'
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

/** 博客/文章等页需要更宽主栏，避免卡片栅格被 1080px 挤乱 */
const isWideMain = computed(() => {
  const p = route.path
  return (
    p.startsWith('/blog') ||
    p.startsWith('/article') ||
    p === '/categories' ||
    p === '/tags' ||
    p.startsWith('/tools') ||
    p === '/albums' ||
    p === '/stats' ||
    p === '/music'
  )
})

/** 工具栏等页：整站壳子必须压过固定定位看板娘 (z-index:999)，否则卡片链接悬停有 href、点击却被挡住 */
const liftAboveMascot = computed(() => {
  const p = route.path
  return (
    p.startsWith('/tools') ||
    p === '/albums' ||
    p === '/stats' ||
    p === '/music'
  )
})

function logout() {
  userStore.logout()
  router.push('/login')
}

function isRoutePrefix(path: string) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

const mobileTabs = computed(() => [
  { key: 'home', label: locale.value === 'zh' ? '首页' : 'Home', active: route.path === '/', go: () => router.push('/') },
  { key: 'blog', label: t('nav.blog'), active: isRoutePrefix('/blog') || isRoutePrefix('/article'), go: () => router.push('/blog') },
  { key: 'tools', label: t('nav.tools'), active: isRoutePrefix('/tools'), go: () => router.push('/tools') },
  { key: 'msg', label: t('nav.message'), active: route.path === '/message', go: () => router.push('/message') },
  { key: 'me', label: isLoggedIn.value ? t('nav.logout') : t('nav.login'), active: route.path === '/login', go: () => (isLoggedIn.value ? logout() : router.push('/login')) },
])

const searchRef = ref<InstanceType<typeof import('../components/GlobalSearch.vue').default> | null>(null)

function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    searchRef.value?.open()
  }
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
  const id = import.meta.env.VITE_CLARITY_ID as string | undefined
  if (id) {
    const w = window as unknown as Record<string, unknown>
    const c = (w.clarity = w.clarity || function () { (c as { q: unknown[] }).q.push(arguments) }) as { q: unknown[] }
    c.q = c.q || []
    const s = document.createElement('script')
    s.async = true; s.src = 'https://www.clarity.ms/tag/' + id
    document.head.appendChild(s)
  }
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})

</script>

<template>
  <GlobalSearch ref="searchRef" />
  <div class="site-root" :class="{ 'site-root--above-mascot': liftAboveMascot }">
    <SiteTopNav />

    <main class="site-main" :class="{ 'site-main--wide': isWideMain }">
      <GlassBreadcrumb />
      <slot />
    </main>

    <nav class="mobile-tabbar" aria-label="mobile navigation">
      <button
        v-for="tab in mobileTabs"
        :key="tab.key"
        type="button"
        class="mobile-tabbar__item"
        :class="{ 'mobile-tabbar__item--active': tab.active }"
        @click="tab.go()"
      >
        {{ tab.label }}
      </button>
    </nav>

    <div class="newsletter-wrap"><NewsletterSignup /></div>
    <SiteGlassFooter />
    <SiteBackToTop />
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
  backdrop-filter: blur(20px) saturate(1.12);
  -webkit-backdrop-filter: blur(20px) saturate(1.12);
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
  display: inline-flex;
  align-items: center;
  gap: 6px;
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

.brand-logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  min-width: 0;
}

.brand-logo__text {
  font-size: clamp(1rem, 2.2vw, 1.35rem);
  font-weight: 800;
  letter-spacing: -0.5px;
  white-space: nowrap;
  flex-shrink: 0;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
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

.site-nav-links__core,
.site-nav-links__actions {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.site-nav-links__actions {
  margin-left: 6px;
  padding-left: 8px;
  border-left: 1px solid rgba(148, 163, 184, 0.3);
}

.site-main {
  flex: 1;
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 16px;
}

.site-main--wide {
  max-width: min(1320px, 100%);
  padding-left: 20px;
  padding-right: 20px;
}

.mobile-tabbar {
  display: none;
}

@media (max-width: 900px) {
  .site-main {
    padding-bottom: calc(74px + env(safe-area-inset-bottom));
  }

  .nav-social,
  .site-nav-links {
    display: none !important;
  }

  .mobile-tabbar {
    position: fixed;
    left: 10px;
    right: 10px;
    bottom: calc(8px + env(safe-area-inset-bottom));
    z-index: 1400;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 8px;
    padding: 8px;
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.42);
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    box-shadow: 0 10px 30px rgba(15, 23, 42, 0.18);
  }

  .mobile-tabbar__item {
    min-height: 44px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.5);
    color: var(--text-color, #0f172a);
    font-size: 0.78rem;
    font-weight: 700;
    cursor: pointer;
  }

  .mobile-tabbar__item--active {
    background: linear-gradient(135deg, var(--primary-color, #4a90e2), var(--secondary-color, #8b7fd8));
    color: #fff;
    border-color: rgba(255, 255, 255, 0.62);
  }

  :root[data-theme='dark'] .mobile-tabbar {
    background: rgba(15, 23, 42, 0.76);
    border-color: rgba(255, 255, 255, 0.14);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.48);
  }

  :root[data-theme='dark'] .mobile-tabbar__item {
    background: rgba(30, 41, 59, 0.72);
    border-color: rgba(148, 163, 184, 0.24);
    color: rgba(226, 232, 240, 0.96);
  }
}
</style>

