<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { goToSiteHome } from '../../utils/siteHome'
import { useI18n } from 'vue-i18n'
import HitokotoCard from '../../components/HitokotoCard.vue'
import { useThemeStore } from '../../stores/theme'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

/** 【主题】与 SiteLayout 共用 Pinia，首页仅负责视频/局部 dark-theme 类 */
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdminUser = computed(() => userStore.isAdmin)

function logout() {
  userStore.logout()
  router.push('/login')
}

function toggleLocale() {
  locale.value = locale.value === 'zh' ? 'en' : 'zh'
}

const works = ref([
  { title: '大语言模型微调与部署', desc: '基于 GLM4 与 LoRA 技术的大模型微调实战项目，探索垂直领域大语言模型应用。', link: '#', tag: 'LLM / GLM4' },
  { title: 'Transformer 机器翻译', desc: '基于底层 Transformer 架构从零构建的机器翻译模型，深入理解 Attention 机制。', link: '#', tag: 'Deep Learning' },
  { title: 'MyWebSide Blog', desc: '个人专属数字花园，基于 Spring Boot 3 与 Vue 3 构建的全栈展示平台。', link: '/blog', tag: 'Full Stack' },
  { title: '在线判题 OJ', desc: '内置算法题库与 Judge0 沙箱，支持 C/C++/Java/Python，ACM 与力扣风格评测。', link: '/tools/oj', tag: 'OJ / Sandbox' },
])

// Smooth scroll for nav anchors
function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

/** 首页顶栏标题：已在主页则滚回顶部，否则进入 `/` */
function onSiteLogoClick() {
  if (route.path === '/') {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    goToSiteHome(router)
  }
}

/** Hero 轻微视差：随滚动上移，增强层次 */
const heroParallaxY = ref(0)
function onHeroParallax() {
  heroParallaxY.value = Math.min(window.scrollY * 0.14, 88)
}
onMounted(() => window.addEventListener('scroll', onHeroParallax, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onHeroParallax))
</script>

<template>
  <div class="portfolio-container" :class="{ 'dark-theme': isDarkMode }">
    <!-- Video Backgrounds -->
    <video class="bg-video light-video" autoplay loop muted playsinline src="/videos/light.mp4"></video>
    <video class="bg-video dark-video" autoplay loop muted playsinline src="/videos/dark.mp4"></video>

    <!-- Navbar -->
    <nav class="glass-nav site-nav-unified">
      <div class="nav-inner">
        <div class="nav-left">
          <div
            class="logo"
            role="link"
            tabindex="0"
            :title="locale === 'zh' ? '返回顶部 / 主页' : 'Home / top'"
            @click="onSiteLogoClick"
            @keydown.enter.prevent="onSiteLogoClick"
          >
            {{ t('nav.logo') }}
          </div>
        </div>
        <div class="links">
          <!-- 【全站统一】首页顶栏与 site-ui .site-pill 一致；窄屏保留项见 site-pill--keep-mobile -->
          <a href="#" class="site-pill site-pill--nav" @click.prevent="scrollTo('about')">{{ t('nav.about') }}</a>
          <a href="#" class="site-pill site-pill--nav" @click.prevent="scrollTo('works')">{{ t('nav.works') }}</a>
          <a href="#" class="site-pill site-pill--nav" @click.prevent="scrollTo('contact')">{{ t('nav.contact') }}</a>
          <a href="#" class="site-pill site-pill--nav" @click.prevent="router.push('/message')">{{ t('nav.message') }}</a>
          <a
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path.startsWith('/tools') }"
            @click.prevent="router.push('/tools')"
          >{{ t('nav.tools') }}</a>
          <a href="#" class="site-pill site-pill--nav lang-toggle site-pill--keep-mobile" :title="t('home.langToggle')" @click.prevent="toggleLocale">
            {{ locale === 'zh' ? 'EN' : '中' }}
          </a>
          <a href="#" class="site-pill site-pill--nav theme-toggle site-pill--keep-mobile" :title="t('home.themeToggle')" @click.prevent="themeStore.toggleTheme">
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
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path === '/login' }"
            @click.prevent="router.push('/login')"
          >{{ t('nav.login') }}</a>
          <a
            v-if="!isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            :class="{ 'site-pill--active': route.path === '/register' }"
            @click.prevent="router.push('/register')"
          >{{ t('nav.register') }}</a>
          <a
            v-if="isLoggedIn"
            href="#"
            class="site-pill site-pill--nav site-pill--keep-mobile"
            @click.prevent="logout"
          >{{ t('nav.logout') }}</a>
          <a href="#" class="site-pill site-pill--nav site-pill--active site-pill--keep-mobile" @click.prevent="router.push('/blog')">{{ t('nav.blog') }}</a>
          <a href="#" class="site-pill site-pill--nav site-pill--pink site-pill--keep-mobile" @click.prevent="router.push('/moyu')">{{ t('nav.moyu') }}</a>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero" id="hero">
      <div class="hero-content" :style="{ transform: `translate3d(0, ${heroParallaxY}px, 0)` }">
        <h1 class="hero-title hero-title--animate">{{ t('home.hello') }} <span class="hero-name-float">{{ t('home.name') }}</span></h1>
        <div class="hero-actions hero-actions--cta">
          <button type="button" class="site-pill site-pill--lg site-pill--active" @click="scrollTo('works')">{{ t('home.explore') }}</button>
          <button type="button" class="site-pill site-pill--lg" @click="router.push('/blog')">{{ t('home.readBlog') }}</button>
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
          <img src="../../assets/images/about-miku.jpg" alt="about miku" class="about-img" />
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
        <div v-for="(work, index) in works" :key="index" class="glass-card work-card">
          <span class="work-tag">{{ work.tag }}</span>
          <h3>{{ work.title }}</h3>
          <p>{{ work.desc }}</p>
          <a v-if="work.link.startsWith('/')" @click="router.push(work.link)" class="work-link">{{ t('home.worksDetail') }} &rarr;</a>
          <a v-else :href="work.link" class="work-link">{{ t('home.worksDetail') }} &rarr;</a>
        </div>
      </div>
    </section>

    <!-- Contact Section -->
    <section class="section" id="contact">
      <div class="glass-card contact-card">
        <h2>{{ t('home.contactTitle') }}</h2>
        <p>{{ t('home.contactText') }}</p>
        <img src="../../assets/images/contact-miku.jpg" alt="contact miku" class="contact-img" />
        <div class="contact-links">
          <a href="https://github.com" target="_blank">GitHub</a>
          <a href="mailto:hello@example.com">Email</a>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <p>© {{ new Date().getFullYear() }} {{ t('home.footer') }}</p>
    </footer>
  </div>
</template>

<style scoped>
/* 
  Base Theme & Gradient Background (Light/Frieren Day Theme)
*/
.portfolio-container {
  min-height: 100vh;
  /* Light and airy with a touch of magic */
  background: linear-gradient(135deg, rgba(230, 238, 245, 0.2) 0%, rgba(200, 218, 235, 0.3) 100%);
  color: #2c3e50;
  font-family: system-ui, -apple-system, sans-serif;
  overflow-x: hidden;
  transition: background 0.5s ease, color 0.5s ease;
  position: relative;
}

.bg-video {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  object-fit: cover;
  z-index: -1;
  transition: opacity 0.5s ease, transform 0.6s ease-out;
  will-change: transform, opacity;
}

.light-video {
  opacity: 1;
}
.dark-theme .light-video {
  opacity: 0;
}

.dark-video {
  opacity: 0;
}
.dark-theme .dark-video {
  opacity: 1;
}

/* 
  Dark Theme (Frieren Night Theme)
*/
.portfolio-container.dark-theme {
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.4) 0%, rgba(42, 27, 61, 0.5) 100%);
  color: #e2e8f0;
}

/* Typography */
h1, h2, h3 {
  font-weight: 700;
  margin-bottom: 1rem;
}
h2 {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 2rem;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.dark-theme h2 {
  background: linear-gradient(to right, #a18cd1, #fbc2eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* Glassmorphism Mixin */
.glass-nav, .glass-card {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px 0 rgba(74, 144, 226, 0.15);
  transition: all 0.3s ease;
}
.dark-theme .glass-nav, .dark-theme .glass-card {
  background: rgba(16, 18, 27, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.5);
}

/* Navigation（顶边线：全局 .site-nav-unified；内高光与原有阴影合并） */
.glass-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
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
.logo {
  font-size: clamp(1rem, 2.2vw, 1.35rem);
  font-weight: 800;
  letter-spacing: -0.5px;
  white-space: nowrap;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  cursor: pointer;
}
.dark-theme .logo {
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
/* 顶栏 pill 略收紧，便于桌面宽度下单行排布 */
.portfolio-container .links .site-pill--nav {
  padding: 6px 10px;
  font-size: 0.78rem;
}
.portfolio-container .links .theme-toggle {
  font-size: 1rem;
  padding: 6px 10px;
}
.theme-toggle {
  font-size: 1.1rem;
  user-select: none;
}

/* 锚点滚动时避开固定顶栏 */
#hero,
#about,
#works,
#contact {
  scroll-margin-top: 5.5rem;
}

/* Hero Section */
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 20px;
  padding-top: 80px; /* Offset for nav */
}
.hero-content {
  will-change: transform;
  transition: transform 0.15s ease-out;
}
.hero-title {
  font-size: 4rem;
  letter-spacing: -1.5px;
  margin-bottom: 1rem;
  color: #1a2a3a;
  text-shadow: 0 2px 4px rgba(255,255,255,0.8);
}
.hero-title--animate {
  animation: hero-title-in 0.95s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  opacity: 0;
}
@keyframes hero-title-in {
  from {
    opacity: 0;
    transform: translateY(28px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.dark-theme .hero-title {
  color: #f0f4f8;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
}
.hero-name-float {
  display: inline-block;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
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
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-7px);
  }
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
  box-shadow:
    0 0 22px rgba(102, 217, 255, 0.45),
    0 10px 28px rgba(74, 144, 226, 0.28);
}
.hero-actions--cta .site-pill--active:hover:not(:disabled) {
  box-shadow:
    0 0 26px rgba(80, 227, 194, 0.5),
    0 10px 32px rgba(74, 144, 226, 0.35);
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
  font-size: 1.1rem;
  line-height: 1.8;
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
.about-text {
  flex: 1;
}
.about-card p {
  font-weight: 500;
  color: #2c3e50;
}
.dark-theme .about-card p {
  color: #e2e8f0;
}

/* Cards Hover Interaction */
.glass-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 12px 40px 0 rgba(74, 144, 226, 0.25);
}
.dark-theme .glass-card:hover {
  background: rgba(30, 32, 45, 0.7);
  box-shadow: 0 12px 40px 0 rgba(0, 0, 0, 0.6);
}

/* Works Grid */
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}
.work-card {
  padding: 30px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
}
.work-tag {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  background: rgba(74, 144, 226, 0.15);
  color: #4a90e2;
  padding: 4px 10px;
  border-radius: 12px;
  align-self: flex-start;
  margin-bottom: 16px;
  font-weight: 700;
}
.dark-theme .work-tag {
  background: rgba(251, 194, 235, 0.15);
  color: #fbc2eb;
}
.work-card h3 {
  font-size: 1.4rem;
  color: #1a2a3a;
}
.dark-theme .work-card h3 {
  color: #f0f4f8;
}
.work-card p {
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 24px;
  flex-grow: 1;
}
.dark-theme .work-card p {
  color: #cbd5e1;
}
.work-link {
  font-weight: 700;
  color: #4a90e2;
  text-decoration: none;
  cursor: pointer;
  transition: opacity 0.3s;
}
.dark-theme .work-link {
  color: #fbc2eb;
}
.work-link:hover {
  opacity: 0.8;
}

/* Contact */
.contact-card {
  padding: 60px 40px;
  border-radius: 24px;
  text-align: center;
}
.contact-card p {
  margin-bottom: 30px;
  font-size: 1.1rem;
  font-weight: 500;
  color: #2c3e50;
}
.dark-theme .contact-card p {
  color: #e2e8f0;
}
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
.dark-theme .contact-img {
  box-shadow: 0 4px 15px rgba(0,0,0,0.4);
}
.contact-links {
  display: flex;
  justify-content: center;
  gap: 20px;
}
.contact-links a {
  display: inline-block;
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.8);
  color: #4a90e2;
  border: 1px solid rgba(74, 144, 226, 0.3);
  transition: all 0.3s ease;
}
.dark-theme .contact-links a {
  background: rgba(0, 0, 0, 0.3);
  color: #fbc2eb;
  border-color: rgba(251, 194, 235, 0.3);
}
.contact-links a:hover {
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
}
.dark-theme .contact-links a:hover {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  color: white;
  border-color: transparent;
}

/* Footer */
.footer {
  text-align: center;
  padding: 40px 20px;
  font-weight: 500;
  color: #2c3e50;
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
}
.dark-theme .footer {
  color: #cbd5e1;
  background: rgba(0, 0, 0, 0.3);
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  .links a.site-pill:not(.site-pill--keep-mobile) {
    display: none;
  }
  .works-grid {
    grid-template-columns: 1fr;
  }
  .hero-actions {
    flex-direction: column;
  }
  .about-content {
    flex-direction: column;
    text-align: center;
  }
  .about-img {
    width: 100%;
    aspect-ratio: 4/3;
  }
  .contact-img {
    height: auto;
    aspect-ratio: 16/9;
  }
}
</style>
