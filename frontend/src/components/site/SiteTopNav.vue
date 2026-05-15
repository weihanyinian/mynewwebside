<script setup lang="ts">
/**
 * 全局统一顶部导航栏组件（模块化）
 * 包含 Logo、社会链接、核心导航、主题/语言/登录、音乐入口
 * 所有页面共享此组件，修改一处全站生效
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/user'
import { useThemeStore } from '../../stores/theme'
import { goToSiteHome } from '../../utils/siteHome'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

const userStore = useUserStore()
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdminUser = computed(() => userStore.isAdmin)

function logout() {
  userStore.logout()
  router.push('/login')
}

function goHome(hash: string) {
  router.push({ path: '/', hash: hash })
}

function isSectionActive(hash: string) {
  return route.path === '/' && route.hash === hash
}

function isRoutePrefix(path: string) {
  return route.path === path || route.path.startsWith(`${path}/`)
}

function onSiteLogoClick() {
  goToSiteHome(router)
}

function toggleLocale() {
  locale.value = locale.value === 'zh' ? 'en' : 'zh'
}

// 音乐入口：跳转音乐中心或 toggle 全局播放器（当前直接跳 /music）
function goMusic() {
  router.push('/music')
}
</script>

<template>
  <nav class="glass-nav site-nav-unified">
    <div class="nav-inner">
      <div class="nav-brand-row">
        <div
          class="logo brand-logo"
          role="link"
          tabindex="0"
          :title="locale === 'zh' ? '返回主页' : 'Home'"
          @click="onSiteLogoClick"
          @keydown.enter.prevent="onSiteLogoClick"
        >
          <span class="brand-logo__text">{{ t('nav.logo') }}</span>
        </div>
        <div class="nav-social" role="navigation" :aria-label="t('sidebar.social')">
          <a class="nav-social-link" href="https://github.com/weihanyinian" target="_blank" rel="noopener noreferrer">
            <svg class="nav-social-ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
              <path fill="currentColor" d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.207 11.385.6.113.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.087.745.084.729.084.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.304 3.495.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.98-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.565 21.796 24 17.302 24 12c0-6.63-5.373-12-12-12Z"/>
            </svg>
            GitHub
          </a>
          <a class="nav-social-link" href="mailto:1012308753@qq.com">
            <svg class="nav-social-ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
              <path fill="currentColor" d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2Zm0 4-8 4.99L4 8V6l8 5 8-5v2Z"/>
            </svg>
            Email
          </a>
        </div>
      </div>

      <div class="links site-nav-links">
        <div class="site-nav-links__core">
          <a href="#" class="site-pill site-pill--nav site-top-anchor" :class="{ 'site-pill--active': isSectionActive('#about') }" @click.prevent="goHome('#about')">{{ t('nav.about') }}</a>
          <a href="#" class="site-pill site-pill--nav site-top-anchor" :class="{ 'site-pill--active': isSectionActive('#works') }" @click.prevent="goHome('#works')">{{ t('nav.works') }}</a>
          <a href="#" class="site-pill site-pill--nav site-top-anchor" :class="{ 'site-pill--active': isSectionActive('#contact') }" @click.prevent="goHome('#contact')">{{ t('nav.contact') }}</a>
          <a href="#" class="site-pill site-pill--nav" :class="{ 'site-pill--active': route.path === '/message' }" @click.prevent="router.push('/message')">{{ t('nav.message') }}</a>
          <a href="#" class="site-pill site-pill--nav site-pill--keep-mobile" :class="{ 'site-pill--active': isRoutePrefix('/tools') }" @click.prevent="router.push('/tools')">{{ t('nav.tools') }}</a>
          <a href="#" class="site-pill site-pill--nav site-pill--keep-mobile" :class="{ 'site-pill--active': isRoutePrefix('/blog') || isRoutePrefix('/article') }" @click.prevent="router.push('/blog')">{{ t('nav.blog') }}</a>
          <a href="#" class="site-pill site-pill--nav site-pill--keep-mobile" :class="{ 'site-pill--active': isRoutePrefix('/albums') }" @click.prevent="router.push('/albums')">{{ t('breadcrumb.albums') }}</a>
          <!-- 音乐模块入口：解决主页顶栏无音乐的问题 -->
          <a href="#" class="site-pill site-pill--nav site-pill--keep-mobile" :class="{ 'site-pill--active': route.path === '/music' }" @click.prevent="goMusic">
            🎵 {{ t('nav.music') }}
          </a>
        </div>

        <div class="site-nav-links__actions">
          <a href="#" class="site-pill site-pill--nav site-pill--ghost lang-toggle" :title="t('home.langToggle')" @click.prevent="toggleLocale">
            {{ locale === 'zh' ? 'EN' : '中' }}
          </a>
          <a href="#" class="nav-social-link nav-theme-icon" :title="t('home.themeToggle')" @click.prevent="themeStore.toggleTheme">{{ !isDarkMode ? '夜' : '昼' }}</a>

          <a v-if="isAdminUser" href="#" class="site-pill site-pill--nav site-nav-auth" :class="{ 'site-pill--active': route.path.startsWith('/admin') }" @click.prevent="router.push('/admin')">{{ t('nav.admin') }}</a>
          <a v-if="!isLoggedIn" href="#" class="site-pill site-pill--nav site-nav-auth" :class="{ 'site-pill--active': route.path === '/login' }" @click.prevent="router.push('/login')">{{ t('nav.login') }}</a>
          <a v-if="!isLoggedIn" href="#" class="site-pill site-pill--nav site-nav-auth" :class="{ 'site-pill--active': route.path === '/register' }" @click.prevent="router.push('/register')">{{ t('nav.register') }}</a>
          <a v-if="isLoggedIn" href="#" class="site-pill site-pill--nav site-nav-auth" @click.prevent="logout">{{ t('nav.logout') }}</a>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
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
</style>