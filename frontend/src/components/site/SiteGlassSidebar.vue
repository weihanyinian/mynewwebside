<script setup lang="ts">
/**
 * 左侧玻璃态导航：z-index 995，低于看板娘(999)与播放器(1100)
 */
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '../../stores/theme'
import { goToSiteHome } from '../../utils/siteHome'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const mobileOpen = ref(false)
const isDesktop = ref(typeof window !== 'undefined' && window.innerWidth > 900)

function mq() {
  isDesktop.value = window.innerWidth > 900
  if (isDesktop.value) mobileOpen.value = false
}

onMounted(() => {
  mq()
  window.addEventListener('resize', mq, { passive: true })
  document.documentElement.classList.add('site-has-sidebar')
})

onUnmounted(() => {
  window.removeEventListener('resize', mq)
  document.documentElement.classList.remove('site-has-sidebar')
})

watch(mobileOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : ''
})

function toggleLocale() {
  locale.value = locale.value === 'zh' ? 'en' : 'zh'
}

function closeMobile() {
  if (!isDesktop.value) mobileOpen.value = false
}

function goHomeAbout() {
  closeMobile()
  router.push({ path: '/', hash: '#about' })
}

function navTo(path: string) {
  closeMobile()
  router.push(path)
}

const items = computed(() => [
  { key: 'home', label: t('sidebar.home'), sub: 'Home', icon: 'home', action: () => { closeMobile(); goToSiteHome(router) } },
  { key: 'about', label: t('sidebar.about'), sub: 'About', icon: 'user', action: goHomeAbout },
  { key: 'friends', label: t('sidebar.friends'), sub: 'Friends', icon: 'heart', action: () => navTo('/friends') },
  { key: 'albums', label: t('sidebar.albums'), sub: 'Album', icon: 'album', action: () => navTo('/albums') },
  { key: 'archives', label: t('sidebar.archives'), sub: 'Archives', icon: 'archive', action: () => navTo('/archives') },
  { key: 'search', label: t('sidebar.search'), sub: 'Search', icon: 'search', action: () => navTo('/search') },
  { key: 'snippets', label: t('sidebar.snippets'), sub: 'Snippets', icon: 'code', action: () => navTo('/snippets') },
])

const socialLinks = [
  { href: 'https://github.com/weihanyinian', label: 'GitHub', icon: 'gh' },
  { href: 'mailto:1012308753@qq.com', label: 'Email', icon: 'mail' },
]

function isActive(key: string) {
  const p = route.path
  if (key === 'home') return p === '/'
  if (key === 'about') return p === '/' && route.hash === '#about'
  if (key === 'friends') return p.startsWith('/friends')
  if (key === 'albums') return p.startsWith('/albums')
  if (key === 'archives') return p.startsWith('/archives')
  if (key === 'search') return p.startsWith('/search')
  if (key === 'snippets') return p.startsWith('/snippets')
  return false
}
</script>

<template>
  <!-- 移动端遮罩 -->
  <div
    v-show="mobileOpen && !isDesktop"
    class="site-side-scrim"
    aria-hidden="true"
    @click="mobileOpen = false"
  />

  <aside class="site-glass-sidebar" :class="{ 'site-glass-sidebar--open': mobileOpen || isDesktop }">
    <div class="site-side-inner">
      <header class="site-side-head">
        <button
          v-if="!isDesktop"
          type="button"
          class="site-side-burger"
          :aria-label="t('sidebar.menu')"
          @click="mobileOpen = !mobileOpen"
        >
          <span />
          <span />
          <span />
        </button>
        <button type="button" class="site-side-logo" @click="goToSiteHome(router); closeMobile()">
          <span class="site-side-logo__mark">✦</span>
          <span class="site-side-logo__text">{{ t('nav.logo') }}</span>
        </button>
        <button
          type="button"
          class="site-side-icon-btn"
          :title="t('home.themeToggle')"
          @click="themeStore.toggleTheme"
        >
          {{ !isDarkMode ? '🌙' : '☀️' }}
        </button>
      </header>

      <div class="site-side-social glass-pill-row" role="navigation" :aria-label="t('sidebar.social')">
        <a
          v-for="s in socialLinks"
          :key="s.href"
          :href="s.href"
          target="_blank"
          rel="noopener noreferrer"
          class="site-social-ico"
          :title="s.label"
        >
          <svg v-if="s.icon === 'gh'" viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
          </svg>
          <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true">
            <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z" />
          </svg>
        </a>
      </div>

      <nav class="site-side-nav" :aria-label="t('sidebar.nav')">
        <button
          v-for="it in items"
          :key="it.key"
          type="button"
          class="site-nav-item"
          :class="{ 'site-nav-item--active': isActive(it.key) }"
          @click="it.action"
        >
          <span class="site-nav-item__ico" aria-hidden="true">
            <template v-if="it.icon === 'home'">⌂</template>
            <template v-else-if="it.icon === 'user'">◉</template>
            <template v-else-if="it.icon === 'heart'">♥</template>
            <template v-else-if="it.icon === 'album'">▣</template>
            <template v-else-if="it.icon === 'archive'">▤</template>
            <template v-else-if="it.icon === 'code'">⌘</template>
            <template v-else>⌕</template>
          </span>
          <span class="site-nav-item__text">
            <span class="site-nav-item__zh">{{ it.label }}</span>
            <span class="site-nav-item__en">{{ it.sub }}</span>
          </span>
        </button>
      </nav>

      <footer class="site-side-foot">
        <button type="button" class="site-foot-pill" @click="toggleLocale">
          <span>{{ locale === 'zh' ? '中 / EN' : 'EN / 中' }}</span>
        </button>
        <p class="site-side-hint">{{ t('sidebar.hint') }}</p>
      </footer>
    </div>
  </aside>

  <!-- 移动端顶栏触发条：仅小屏 -->
  <button
    v-if="!isDesktop"
    type="button"
    class="site-mobile-fab-nav"
    :aria-label="t('sidebar.openMenu')"
    @click="mobileOpen = true"
  >
    ☰
  </button>
</template>

<style scoped>
.site-side-scrim {
  position: fixed;
  inset: 0;
  z-index: 994;
  background: rgba(15, 18, 28, 0.45);
  backdrop-filter: blur(4px);
}

.site-glass-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: min(268px, 88vw);
  z-index: 995;
  transform: translateX(-104%);
  transition:
    transform 0.38s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.3s ease;
  pointer-events: none;
}

.site-glass-sidebar--open {
  transform: translateX(0);
  pointer-events: auto;
  box-shadow: 12px 0 40px rgba(0, 0, 0, 0.18);
}

@media (min-width: 901px) {
  .site-glass-sidebar {
    transform: translateX(0);
    pointer-events: auto;
    box-shadow: 8px 0 32px rgba(74, 144, 226, 0.08);
  }
}

.site-side-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 14px 12px 16px;
  box-sizing: border-box;
  background: var(--sidebar-glass-bg, rgba(255, 255, 255, 0.38));
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  border-right: 1px solid var(--sidebar-glass-border, rgba(255, 255, 255, 0.55));
}

:root[data-theme='dark'] .site-side-inner {
  --sidebar-glass-bg: rgba(18, 20, 32, 0.72);
  --sidebar-glass-border: rgba(255, 255, 255, 0.12);
}

.site-side-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.site-side-burger {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid rgba(102, 217, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  cursor: pointer;
  flex-shrink: 0;
}

.site-side-burger span {
  display: block;
  width: 18px;
  height: 2px;
  border-radius: 2px;
  background: currentColor;
  opacity: 0.85;
}

@media (min-width: 901px) {
  .site-side-burger {
    display: none;
  }
}

.site-side-logo {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 14px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
  text-align: left;
  color: inherit;
  transition:
    transform 0.22s ease,
    border-color 0.22s ease,
    box-shadow 0.22s ease;
}

.site-side-logo:hover {
  transform: translateY(-1px);
  border-color: rgba(102, 217, 255, 0.45);
  box-shadow: 0 6px 20px rgba(102, 217, 255, 0.15);
}

.site-side-logo__mark {
  font-size: 1.1rem;
  opacity: 0.9;
}

.site-side-logo__text {
  font-size: 0.82rem;
  font-weight: 800;
  line-height: 1.2;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.site-side-icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid rgba(102, 217, 255, 0.32);
  background: rgba(255, 255, 255, 0.1);
  cursor: pointer;
  font-size: 1.05rem;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.site-side-icon-btn:hover {
  transform: scale(1.06);
}

.glass-pill-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.35);
  margin-bottom: 14px;
}

:root[data-theme='dark'] .glass-pill-row {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.12);
}

.site-social-ico {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: var(--text-color);
  opacity: 0.88;
  transition:
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.2s ease,
    background 0.2s ease;
}

.site-social-ico:hover {
  transform: translateY(-3px) scale(1.06);
  opacity: 1;
  background: rgba(102, 217, 255, 0.18);
}

.site-side-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow-y: auto;
  padding-right: 2px;
}

.site-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 12px;
  border-radius: 14px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.06);
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition:
    transform 0.24s cubic-bezier(0.22, 1, 0.36, 1),
    border-color 0.24s ease,
    box-shadow 0.24s ease,
    background 0.24s ease;
}

:root[data-theme='dark'] .site-nav-item {
  background: rgba(255, 255, 255, 0.04);
}

.site-nav-item:hover {
  transform: translateX(4px);
  border-color: rgba(102, 217, 255, 0.42);
  box-shadow: 0 8px 24px rgba(102, 217, 255, 0.12);
}

.site-nav-item--active {
  border-color: rgba(80, 227, 194, 0.55);
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.2), rgba(80, 227, 194, 0.16));
  box-shadow: 0 6px 22px rgba(74, 144, 226, 0.18);
}

.site-nav-item__ico {
  width: 2rem;
  text-align: center;
  font-size: 1.05rem;
  opacity: 0.92;
}

.site-nav-item__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.site-nav-item__zh {
  font-size: 0.9rem;
  font-weight: 700;
}

.site-nav-item__en {
  font-size: 0.68rem;
  font-weight: 600;
  opacity: 0.62;
  letter-spacing: 0.02em;
}

.site-side-foot {
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

:root[data-theme='dark'] .site-side-foot {
  border-top-color: rgba(255, 255, 255, 0.1);
}

.site-foot-pill {
  width: 100%;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(102, 217, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  font-weight: 700;
  font-size: 0.82rem;
  cursor: pointer;
  color: inherit;
  transition: transform 0.2s ease;
}

.site-foot-pill:hover {
  transform: translateY(-2px);
}

.site-side-hint {
  margin: 10px 0 0;
  font-size: 0.65rem;
  line-height: 1.4;
  opacity: 0.55;
  text-align: center;
}

.site-mobile-fab-nav {
  position: fixed;
  top: 12px;
  left: 12px;
  z-index: 996;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: 1px solid rgba(102, 217, 255, 0.45);
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(12px);
  font-size: 1.25rem;
  cursor: pointer;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.12);
}

:root[data-theme='dark'] .site-mobile-fab-nav {
  background: rgba(22, 24, 36, 0.65);
  color: #e2e8f0;
}

@media (min-width: 901px) {
  .site-mobile-fab-nav {
    display: none;
  }
}
</style>
