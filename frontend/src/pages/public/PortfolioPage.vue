<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { goToSiteHome } from '../../utils/siteHome'
import HitokotoCard from '../../components/HitokotoCard.vue'
import SiteGlassFooter from '../../components/site/SiteGlassFooter.vue'
import SiteBackToTop from '../../components/site/SiteBackToTop.vue'
import { useThemeStore } from '../../stores/theme'
import { useUserStore } from '../../stores/user'
import MessageWallSection from './sections/MessageWallSection.vue'
import AlbumsSection from './sections/AlbumsSection.vue'
import ToolsSection from './sections/ToolsSection.vue'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

/** 【主题】与 SiteLayout 共用 Pinia，首页仅负责视频/局部 dark-theme 类 */
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdminUser = computed(() => userStore.isAdmin)

const githubRepo =
  import.meta.env.VITE_PUBLIC_GITHUB_REPO || 'https://github.com/weihanyinian/mynewwebside'

function logout() {
  userStore.logout()
  router.push('/login')
}

function toggleLocale() {
  locale.value = locale.value === 'zh' ? 'en' : 'zh'
}

function closeNavMobile() {
  mobileNavOpen.value = false
}

type WorkItem = {
  title: string
  desc: string
  detail: string
  tag: string
  link: string
  cover: string
}

const works = ref<WorkItem[]>([
  {
    title: '大语言模型微调与部署',
    desc: '基于 GLM4 与 LoRA 技术的大模型微调实战项目，探索垂直领域大语言模型应用。',
    detail: '覆盖数据构建、训练与推理部署流程，可作为 LLM 工程化落地的实践参考。',
    link: '#works',
    tag: 'LLM / GLM4',
    cover: '/avatar.png',
  },
  {
    title: 'Transformer 机器翻译',
    desc: '基于底层 Transformer 架构从零构建的机器翻译模型，深入理解 Attention 机制。',
    detail: '从编码器—解码器到多头注意力，适合巩固序列建模与并行训练要点。',
    link: '#works',
    tag: 'Deep Learning',
    cover: '/avatar.png',
  },
  {
    title: 'MyWebSide Blog',
    desc: '个人专属数字花园，基于 Spring Boot 3 与 Vue 3 构建的全栈展示平台。',
    detail: '博客、留言墙、工具栏与 OJ 一体化；代码开源，欢迎交流与共建。',
    link: 'https://github.com/weihanyinian/mynewwebside',
    tag: 'Full Stack',
    cover: '/avatar.png',
  },
  {
    title: '在线判题 OJ',
    desc: '内置算法题库与 Judge0 沙箱，支持 C/C++/Java/Python，ACM 与力扣风格评测。',
    detail: '登录后可提交代码、查看评测状态与历史提交记录。',
    link: '/tools/oj',
    tag: 'OJ / Sandbox',
    cover: '/avatar.png',
  },
])

const mobileNavOpen = ref(false)
const portfolioMoreEl = ref<HTMLDetailsElement | null>(null)

function closePortfolioMore() {
  if (portfolioMoreEl.value) portfolioMoreEl.value.open = false
}

watch(
  () => route.fullPath,
  () => {
    mobileNavOpen.value = false
    closePortfolioMore()
  },
)

function isHashActive(fragment: string) {
  return route.path === '/' && route.hash === fragment
}

function scrollTo(id: string) {
  void router.replace({ path: '/', hash: '#' + id })
  void nextTick(() => {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
  })
  mobileNavOpen.value = false
}

function onWorkClick(link: string) {
  if (link.startsWith('#') && link.length > 1) {
    scrollTo(link.slice(1))
    return
  }
  if (link.startsWith('/')) {
    router.push(link)
    return
  }
  if (link.startsWith('http://') || link.startsWith('https://')) {
    window.open(link, '_blank', 'noopener,noreferrer')
  }
}

function onWorkCardActivate(work: WorkItem, e?: Event) {
  if (e instanceof KeyboardEvent && e.key !== 'Enter' && e.key !== ' ') return
  onWorkClick(work.link)
}

function onSiteLogoClick() {
  if (route.path === '/') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    goToSiteHome(router)
  }
}

/** Hero 轻微视差 */
const heroParallaxY = ref(0)
function onHeroParallax() {
  heroParallaxY.value = Math.min(window.scrollY * 0.14, 88)
}

onMounted(() => {
  window.addEventListener('scroll', onHeroParallax, { passive: true })
  if (route.hash) {
    const id = route.hash.replace(/^#/, '')
    void nextTick(() => document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' }))
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', onHeroParallax)
})
</script>

<template>
  <div class="portfolio-container" :class="{ 'dark-theme': isDarkMode }">
    <!-- Video Backgrounds -->
    <video class="bg-video light-video" autoplay loop muted playsinline src="/videos/light.mp4"></video>
    <video class="bg-video dark-video" autoplay loop muted playsinline src="/videos/dark.mp4"></video>
    <div class="portfolio-bg-scrim" aria-hidden="true" />

    <!-- Navbar -->
    <nav class="glass-nav site-nav-unified">
      <div class="nav-inner">
        <div class="nav-left">
          <div
            class="logo brand-logo"
            role="link"
            tabindex="0"
            :title="locale === 'zh' ? '返回顶部 / 主页' : 'Home / top'"
            @click="onSiteLogoClick"
            @keydown.enter.prevent="onSiteLogoClick"
          >
            <span class="brand-logo__text">{{ t('nav.logo') }}</span>
          </div>
          <div class="nav-social" role="navigation" :aria-label="t('sidebar.social')">
            <a
              class="nav-social-link"
              href="https://github.com/weihanyinian"
              target="_blank"
              rel="noopener noreferrer"
            >
              <svg class="nav-social-ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                <path
                  fill="currentColor"
                  d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.207 11.385.6.113.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.087.745.084.729.084.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.304 3.495.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.98-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.565 21.796 24 17.302 24 12c0-6.63-5.373-12-12-12Z"
                />
              </svg>
              GitHub
            </a>
            <a class="nav-social-link" href="mailto:1012308753@qq.com">
              <svg class="nav-social-ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                <path
                  fill="currentColor"
                  d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2Zm0 4-8 4.99L4 8V6l8 5 8-5v2Z"
                />
              </svg>
              Email
            </a>
          </div>
        </div>
        <button
          type="button"
          class="nav-burger"
          :aria-expanded="mobileNavOpen"
          aria-controls="portfolio-nav-links"
          :aria-label="locale === 'zh' ? '打开导航菜单' : 'Open menu'"
          @click="mobileNavOpen = !mobileNavOpen"
        >
          <span></span><span></span><span></span>
        </button>
        <div
          id="portfolio-nav-links"
          class="links site-nav-links"
          :class="{ 'is-open': mobileNavOpen }"
        >
          <a
            href="#about"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': isHashActive('#about') }"
            @click.prevent="scrollTo('about')"
          >{{ t('nav.about') }}</a>
          <a
            href="#works"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': isHashActive('#works') }"
            @click.prevent="scrollTo('works')"
          >{{ t('nav.works') }}</a>
          <a
            href="#blog"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': isHashActive('#blog') }"
            @click.prevent="scrollTo('blog')"
          >{{ t('nav.blog') }}</a>
          <a
            href="#contact"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': isHashActive('#contact') }"
            @click.prevent="scrollTo('contact')"
          >{{ t('nav.contact') }}</a>
          <a
            href="#message"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': isHashActive('#message') }"
            @click.prevent="scrollTo('message')"
          >{{ t('nav.message') }}</a>
          <a
            href="#albums"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': isHashActive('#albums') }"
            @click.prevent="scrollTo('albums')"
          >{{ t('breadcrumb.albums') }}</a>
          <a
            href="#tools"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': isHashActive('#tools') }"
            @click.prevent="scrollTo('tools')"
          >{{ t('nav.tools') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav lang-toggle site-pill--keep-mobile"
            :title="t('home.langToggle')"
            @click.prevent="toggleLocale(); closeNavMobile()"
          >
            {{ locale === 'zh' ? 'EN' : '中' }}
          </a>
          <a
            href="#"
            class="nav-social-link nav-theme-icon"
            :title="t('home.themeToggle')"
            @click.prevent="themeStore.toggleTheme(); closeNavMobile()"
          >
            {{ !isDarkMode ? '🌙' : '☀️' }}
          </a>
          <a
            v-if="isAdminUser"
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/admin') }"
            @click.prevent="router.push('/admin')"
          >{{ t('nav.admin') }}</a>
          <a
            v-if="!isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-nav-auth site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path === '/login' }"
            @click.prevent="router.push('/login')"
          >{{ t('nav.login') }}</a>
          <a
            v-if="!isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-nav-auth site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path === '/register' }"
            @click.prevent="router.push('/register')"
          >{{ t('nav.register') }}</a>
          <a
            v-if="isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-nav-auth site-pill--keep-mobile"
            @click.prevent="logout"
          >{{ t('nav.logout') }}</a>
          <details ref="portfolioMoreEl" class="nav-more-wrap">
            <summary class="site-pill site-pill--nav nav-more-trigger site-pill--keep-mobile">{{ t('nav.more') }}</summary>
            <div class="nav-more-panel" @click="closePortfolioMore">
              <a
                href="#"
                :class="{ 'site-pill--active': route.path.startsWith('/moyu') }"
                @click.prevent="router.push('/moyu')"
              >{{ t('nav.moyu') }}</a>
              <a
                href="#"
                :class="{ 'site-pill--active': route.path === '/tools/mbti' }"
                @click.prevent="router.push('/tools/mbti')"
              >{{ t('nav.mbti') }}</a>
              <a
                v-if="isLoggedIn"
                href="#"
                :class="{ 'site-pill--active': route.path === '/memories' }"
                @click.prevent="router.push('/memories')"
              >{{ t('nav.memories') }}</a>
            </div>
          </details>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero" id="hero">
      <div class="hero-content" :style="{ transform: `translate3d(0, ${heroParallaxY}px, 0)` }">
        <h1 class="hero-title hero-title--animate">{{ t('home.hello') }} <span class="hero-name-float">{{ t('home.name') }}</span></h1>
        <div class="hero-actions hero-actions--cta">
          <button type="button" class="site-pill site-pill--lg site-pill--active" @click="scrollTo('works')">{{ t('home.explore') }}</button>
          <button type="button" class="site-pill site-pill--lg site-pill--secondary" @click="scrollTo('blog')">{{ t('home.readBlog') }}</button>
        </div>
      </div>
    </section>

    <!-- Hitokoto Quote -->
    <HitokotoCard />

    <!-- About Section -->
    <section class="section" id="about">
      <div class="glass-card about-card">
        <h2>{{ t('home.aboutTitle') }}</h2>
        <div class="about-content">
          <img src="/avatar.png" alt="维寒一念" class="about-img" loading="lazy" decoding="async" />
          <div class="about-text">
            <p>{{ t('home.aboutText1') }}</p>
            <br />
            <p>{{ t('home.aboutText2') }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Works Section -->
    <section class="section" id="works">
      <h2>{{ t('home.worksTitle') }}</h2>
      <div class="works-grid">
        <article
          v-for="(work, index) in works"
          :key="index"
          class="glass-card work-card site-module-card"
          role="link"
          tabindex="0"
          :aria-label="work.title"
          @click="onWorkCardActivate(work, $event)"
          @keydown.enter.prevent="onWorkCardActivate(work, $event)"
          @keydown.space.prevent="onWorkCardActivate(work, $event)"
        >
          <div class="work-card__media">
            <img :src="work.cover" :alt="''" loading="lazy" decoding="async" class="work-card__img" />
            <div class="work-card__overlay" :aria-hidden="true">
              <p class="work-card__overlay-title">{{ work.title }}</p>
              <p class="work-card__detail">{{ work.desc }}</p>
            </div>
          </div>
          <div class="work-card__body">
            <span class="work-tag">{{ work.tag }}</span>
            <h3>{{ work.title }}</h3>
            <p class="work-card__excerpt">{{ work.desc }}</p>
            <span class="work-link">{{ t('home.worksDetail') }} →</span>
          </div>
        </article>
      </div>
    </section>

    <!-- Blog & open source -->
    <section class="section" id="blog">
      <div class="glass-card home-hub-card">
        <h2>{{ t('home.sectionBlogTitle') }}</h2>
        <p class="home-hub-lead">{{ t('home.sectionBlogLead') }}</p>
        <div class="home-hub-actions">
          <a :href="githubRepo" class="site-pill site-pill--lg site-pill--secondary" target="_blank" rel="noopener noreferrer">{{ t('home.sectionBlogRepo') }}</a>
          <button type="button" class="site-pill site-pill--lg site-pill--active" @click="router.push('/blog')">
            {{ t('home.sectionBlogRead') }}
          </button>
        </div>
      </div>
    </section>

    <!-- Contact Section -->
    <section class="section" id="contact">
      <div class="glass-card contact-card">
        <h2>{{ t('home.contactTitle') }}</h2>
        <p>{{ t('home.contactText') }}</p>
        <img src="../../assets/images/contact-miku.jpg" alt="contact miku" class="contact-img" loading="lazy" decoding="async" />
        <div class="contact-links">
          <a href="https://github.com/weihanyinian" target="_blank" rel="noopener noreferrer">GitHub</a>
          <a href="mailto:1012308753@qq.com">Email</a>
        </div>
      </div>
    </section>

    <!-- 留言墙（拆分为独立组件） -->
    <MessageWallSection />

    <!-- 相册（拆分为独立组件） -->
    <AlbumsSection />

    <!-- 工具栏入口（拆分为独立组件） -->
    <ToolsSection />

    <SiteGlassFooter />
    <SiteBackToTop />
  </div>
</template>

<style scoped>
/* 
  Base Theme & Gradient Background (Light/Frieren Day Theme)
*/
.portfolio-container {
  min-height: 100vh;
  /* 底层几乎透明，主视觉交给背景视频 */
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.02) 0%, rgba(230, 242, 255, 0.04) 100%);
  color: #0f172a;
  font-family: system-ui, -apple-system, sans-serif;
  overflow-x: hidden;
  transition: background 0.5s ease, color 0.5s ease;
  position: relative;
}

.portfolio-bg-scrim {
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  transition: background 0.45s ease;
}

/* 日间：仅 5%–10% 量级浅色渐变叠在视频上，不糊化画面，保留动态感 */
.portfolio-container:not(.dark-theme) .portfolio-bg-scrim {
  background: linear-gradient(
    165deg,
    rgba(255, 255, 255, 0.09) 0%,
    rgba(232, 244, 255, 0.05) 42%,
    rgba(255, 255, 255, 0.08) 100%
  );
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
}

/* 夜间：对应轻暗角 + 极轻模糊，通透不闷 */
.portfolio-container.dark-theme .portfolio-bg-scrim {
  background: linear-gradient(
    165deg,
    rgba(15, 23, 42, 0.12) 0%,
    rgba(30, 27, 58, 0.08) 50%,
    rgba(15, 23, 42, 0.14) 100%
  );
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.bg-video {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  object-fit: cover;
  z-index: -2;
  transition: opacity 0.5s ease, transform 0.6s ease-out;
  will-change: transform, opacity;
}

.light-video { opacity: 1; }
.dark-theme .light-video { opacity: 0; }
.dark-video { opacity: 0; }
.dark-theme .dark-video { opacity: 1; }

/* Dark Theme */
.portfolio-container.dark-theme {
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.12) 0%, rgba(30, 27, 50, 0.1) 100%);
  color: #e2e8f0;
}

/* Typography：标题加重、辅文轻量化、阅读节奏 */
h1, h2, h3 {
  font-weight: 800;
  margin-bottom: 1rem;
  letter-spacing: -0.02em;
  line-height: 1.2;
}
h2 {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 2rem;
  font-weight: 800;
  background: linear-gradient(to right, var(--primary-color, #4a90e2), var(--secondary-color, #8b7fd8));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.dark-theme h2 {
  background: linear-gradient(to right, #a18cd1, #fbc2eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 顶栏：略强于内容区，保证导航可读 */
.glass-nav {
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.38);
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.08);
  transition: transform 0.32s ease, box-shadow 0.32s ease, background 0.3s ease;
}
.dark-theme .glass-nav {
  background: rgba(15, 23, 42, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.35);
}

/* 内容玻璃卡片：≈10% 不透明 + blur(5px)，悬浮于视频之上 */
.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.42);
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.07);
  transition: transform 0.32s ease, box-shadow 0.32s ease, background 0.3s ease;
}
.dark-theme .glass-card {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

/* Navigation */
.glass-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1200;
  border-radius: 0 0 16px 16px;
}
.portfolio-container .glass-nav.site-nav-unified {
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.18),
    0 8px 32px 0 rgba(74, 144, 226, 0.15);
}
.portfolio-container.dark-theme .glass-nav.site-nav-unified {
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.08),
    0 8px 32px 0 rgba(0, 0, 0, 0.5);
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
.nav-left {
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
  color: #0f172a;
  background: rgba(255, 255, 255, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: opacity 0.2s, transform 0.2s;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dark-theme .nav-social-link {
  color: #e2e8f0;
  background: rgba(16, 18, 27, 0.65);
  border-color: rgba(255, 255, 255, 0.12);
}
.nav-social-link:hover {
  opacity: 0.9;
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
  background: linear-gradient(to right, var(--primary-color, #4a90e2), var(--secondary-color, #8b7fd8));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.dark-theme .brand-logo__text {
  background: linear-gradient(to right, #a18cd1, #fbc2eb);
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
.links::-webkit-scrollbar { height: 3px; }
.links::-webkit-scrollbar-thumb { background: rgba(102, 217, 255, 0.35); border-radius: 3px; }
.links a.site-pill { text-decoration: none; flex-shrink: 0; }

/* Anchor scroll offset */
#hero, #about, #works, #blog, #contact, #message, #albums, #tools {
  scroll-margin-top: 5.5rem;
}

.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  font-weight: 400;
  opacity: 0.78;
  line-height: 1.72;
  letter-spacing: 0.02em;
  color: rgba(15, 23, 42, 0.82);
}
.dark-theme .section-sub {
  color: rgba(226, 232, 240, 0.82);
}
.center { text-align: center; }

.home-hub-card {
  padding: 36px 28px;
  border-radius: 24px;
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
}
.home-hub-lead {
  margin: 0 0 1.25rem;
  line-height: 1.75;
  letter-spacing: 0.015em;
  font-weight: 400;
  opacity: 0.82;
  color: rgba(15, 23, 42, 0.78);
}
.dark-theme .home-hub-lead {
  color: rgba(226, 232, 240, 0.78);
}
.home-hub-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}
/* Hero Section */
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 20px;
  padding-top: 80px;
}
.hero-content {
  will-change: transform;
  transition: transform 0.15s ease-out;
}
.hero-title {
  font-size: 4rem;
  letter-spacing: -0.04em;
  line-height: 1.08;
  margin-bottom: 1rem;
  font-weight: 800;
  color: #0f172a;
  text-shadow:
    0 1px 0 rgba(255, 255, 255, 0.55),
    0 0 40px rgba(255, 255, 255, 0.25);
}
.hero-title--animate {
  animation: hero-title-in 0.95s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  opacity: 0;
}
@keyframes hero-title-in {
  from { opacity: 0; transform: translateY(28px); }
  to { opacity: 1; transform: translateY(0); }
}
.dark-theme .hero-title {
  color: #f0f4f8;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
}
.hero-name-float {
  display: inline-block;
  background: linear-gradient(to right, var(--primary-color, #4a90e2), var(--secondary-color, #8b7fd8));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  animation: hero-name-float 4.8s ease-in-out infinite;
  animation-delay: 0.4s;
}
.dark-theme .hero-name-float {
  background: linear-gradient(to right, #a18cd1, #fbc2eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
@keyframes hero-name-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-7px); }
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}
.hero-actions--cta .site-pill {
  position: relative;
  overflow: hidden;
  min-height: 48px;
  padding-left: 1.35rem;
  padding-right: 1.35rem;
}
.hero-actions--cta .site-pill:hover:not(:disabled) {
  box-shadow: 0 0 22px rgba(102, 217, 255, 0.45), 0 10px 28px rgba(74, 144, 226, 0.28);
}
.hero-actions--cta .site-pill--active:hover:not(:disabled) {
  box-shadow: 0 0 26px rgba(80, 227, 194, 0.5), 0 10px 32px rgba(74, 144, 226, 0.35);
}

/* General Sections */
.section {
  max-width: 1080px;
  margin: 0 auto;
  padding: 80px 20px;
}

/* About Card */
.about-card {
  padding: 40px;
  border-radius: 24px;
  font-size: 1.05rem;
  line-height: 1.75;
  letter-spacing: 0.01em;
}
.about-content {
  display: flex;
  align-items: center;
  gap: 40px;
  text-align: left;
}
.about-img {
  width: 45%;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  object-fit: cover;
  aspect-ratio: 16/10;
}
.dark-theme .about-img {
  box-shadow: 0 8px 24px rgba(0,0,0,0.5);
}
.about-text { flex: 1; }
.about-card p {
  font-weight: 400;
  color: rgba(15, 23, 42, 0.88);
}
.dark-theme .about-card p {
  color: #e2e8f0;
}

/* Cards Hover：略提亮，不破坏通透 */
.glass-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.14);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.12);
}
.dark-theme .glass-card:hover {
  background: rgba(255, 255, 255, 0.11);
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.35);
}

/* Works Grid */
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}
.work-card {
  padding: 0;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  outline: none;
}
.work-card:focus-visible {
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary-color, #4a90e2) 45%, transparent);
}
.work-card__media {
  position: relative;
  height: 180px;
  overflow: hidden;
}
.work-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}
.work-card__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 6px;
  padding: 16px;
  background: linear-gradient(to top, rgba(15, 23, 42, 0.88), transparent 52%);
  opacity: 0;
  transition: opacity 0.35s ease;
}
.work-card__overlay-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.25;
  color: #fff;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.35);
}
.work-card__detail {
  margin: 0;
  font-size: 0.86rem;
  line-height: 1.55;
  font-weight: 400;
  color: rgba(248, 250, 252, 0.92);
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.35);
}
.work-card:hover .work-card__overlay,
.work-card:focus-visible .work-card__overlay {
  opacity: 1;
}
.work-card:hover .work-card__img,
.work-card:focus-visible .work-card__img {
  transform: scale(1.06);
}
.glass-card.work-card:hover,
.glass-card.work-card:focus-visible {
  transform: translateY(-8px) scale(1.01);
  box-shadow:
    0 22px 50px rgba(15, 23, 42, 0.14),
    0 0 0 1px rgba(255, 255, 255, 0.35);
}
.dark-theme .glass-card.work-card:hover,
.dark-theme .glass-card.work-card:focus-visible {
  box-shadow:
    0 22px 50px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1);
}
.work-card__body {
  padding: 22px 24px 24px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.work-tag {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  background: color-mix(in srgb, var(--primary-color, #4a90e2) 18%, transparent);
  color: var(--primary-color, #4a90e2);
  padding: 4px 10px;
  border-radius: 12px;
  align-self: flex-start;
  margin-bottom: 12px;
  font-weight: 700;
}
.dark-theme .work-tag {
  background: rgba(251, 194, 235, 0.15);
  color: #fbc2eb;
}
.work-card h3 {
  font-size: 1.35rem;
  color: #0f172a;
  margin: 0 0 8px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.25;
}
.dark-theme .work-card h3 {
  color: #f0f4f8;
}
.work-card__excerpt {
  color: rgba(15, 23, 42, 0.76);
  font-weight: 400;
  margin: 0 0 16px;
  flex-grow: 1;
  line-height: 1.68;
  letter-spacing: 0.02em;
  font-size: 0.95rem;
}
.dark-theme .work-card__excerpt {
  color: rgba(203, 213, 225, 0.88);
}
.work-link {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--primary-color, #4a90e2);
  margin-top: auto;
}
.dark-theme .work-link { color: #c4b5fd; }

/* Contact */
.contact-card {
  padding: 60px 40px;
  border-radius: 24px;
  text-align: center;
}
.contact-card p { margin-bottom: 30px; font-size: 1.1rem; font-weight: 500; color: #0f172a; }
.dark-theme .contact-card p { color: #e2e8f0; }
.contact-img {
  width: 100%;
  max-width: 600px;
  height: 180px;
  object-fit: cover;
  border-radius: 16px;
  margin: 0 auto 30px auto;
  display: block;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}
.dark-theme .contact-img { box-shadow: 0 4px 15px rgba(0,0,0,0.4); }
.contact-links { display: flex; justify-content: center; gap: 20px; }
.contact-links a {
  display: inline-block;
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.88);
  color: var(--primary-color, #4a90e2);
  border: 1px solid color-mix(in srgb, var(--primary-color, #4a90e2) 35%, transparent);
  transition: all 0.3s ease;
}
.dark-theme .contact-links a {
  background: rgba(0, 0, 0, 0.3);
  color: #fbc2eb;
  border-color: rgba(251, 194, 235, 0.3);
}
.contact-links a:hover {
  background: linear-gradient(135deg, var(--primary-color, #4a90e2) 0%, var(--secondary-color, #8b7fd8) 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
}
.dark-theme .contact-links a:hover {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  color: white;
  border-color: transparent;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title { font-size: 2.5rem; }
  .works-grid { grid-template-columns: 1fr; }
  .hero-actions { flex-direction: column; }
  .about-content { flex-direction: column; text-align: center; }
  .about-img { width: 100%; aspect-ratio: 4/3; }
  .contact-img { height: auto; aspect-ratio: 16/9; }
}

/* 首页顶栏汉堡：浅色背景下保证对比度 */
.portfolio-container:not(.dark-theme) .nav-burger {
  color: #0f172a;
}

.portfolio-container .nav-more-wrap summary.nav-more-trigger {
  list-style: none;
}
.portfolio-container .nav-more-wrap summary::-webkit-details-marker {
  display: none;
}
</style>
