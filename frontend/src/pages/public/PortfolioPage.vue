<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import HitokotoCard from '../../components/HitokotoCard.vue'
import SiteBackgroundVideos from '../../components/site/SiteBackgroundVideos.vue'
import { useThemeStore } from '../../stores/theme'
import { useWorksStore } from '../../stores/works'
import { useVisitStore } from '../../stores/visit'
import MessageWallSection from './sections/MessageWallSection.vue'
import ToolsSection from './sections/ToolsSection.vue'
import HomeHero from '../../components/home/HomeHero.vue'
import HomeWorksSection from '../../components/home/HomeWorksSection.vue'
import { useHeroMotion } from '../../hooks/useHeroMotion'
import { useSectionObserver } from '../../hooks/useSectionObserver'
import { HOME_SECTION_IDS, type HomeWorkItem } from '../../types/home'
import { HOME_BG_LIGHT_SRC, HOME_BG_DARK_SRC } from '../../config/homeBackgroundVideos'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

/** 【主题】与 SiteLayout 共用 Pinia，首页仅负责局部 dark-theme 类 */
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const worksStore = useWorksStore()
const visitStore = useVisitStore()

const githubRepo =
  import.meta.env.VITE_PUBLIC_GITHUB_REPO || 'https://github.com/weihanyinian/website'

/** 日间浅色 / 夜间深色，片名见 `config/homeBackgroundVideos.ts` */
const homeBgLightSrc = HOME_BG_LIGHT_SRC
const homeBgDarkSrc = HOME_BG_DARK_SRC

const works = computed(() => worksStore.filteredWorks)
const workCategories = computed(() => worksStore.categories)
const selectedWorkCategory = computed(() => worksStore.selectedCategory)
const visitBadge = computed(
  () => `访问 ${visitStore.homeVisits} 次 · 今日 ${visitStore.todayVisits} 次`,
)

/** 当前高亮的 section id（由滚动监听驱动，点击时也同步更新） */
const { isHashActive, scrollToSection } = useSectionObserver(
  computed(() => route.fullPath),
  computed(() => route.hash),
  HOME_SECTION_IDS,
)
const { heroParallaxY, pointerX, pointerY } = useHeroMotion()

/** 全屏背景 MP4 体积大：首帧后再挂，避免与首屏 CSS/字体竞争带宽 */
const bgVideoReady = ref(false)

function scrollTo(id: string) {
  scrollToSection(id, 92)
}

const mobileTabs = computed(() => [
  { key: 'about', label: t('nav.about'), active: isHashActive('#about'), go: () => scrollTo('about') },
  { key: 'works', label: t('nav.works'), active: isHashActive('#works'), go: () => scrollTo('works') },
  { key: 'blog', label: t('nav.blog'), active: isHashActive('#blog'), go: () => scrollTo('blog') },
  { key: 'tools', label: t('nav.tools'), active: isHashActive('#tools'), go: () => scrollTo('tools') },
  { key: 'msg', label: t('nav.message'), active: isHashActive('#message'), go: () => scrollTo('message') },
])

function onWorkClick(link: string) {
  if (link.startsWith('/works/')) {
    router.push(link)
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

function onWorkCardActivate(work: HomeWorkItem, e?: Event) {
  if (e instanceof KeyboardEvent && e.key !== 'Enter' && e.key !== ' ') return
  if (work.id) {
    router.push(`/works/${work.id}`)
    return
  }
  onWorkClick(work.link)
}

onMounted(() => {
  const enableBg = () => {
    bgVideoReady.value = true
  }
  if (typeof requestIdleCallback === 'function') {
    requestIdleCallback(enableBg, { timeout: 1600 })
  } else {
    setTimeout(enableBg, 500)
  }

  visitStore.initHomeVisit()
  void worksStore.fetchWorksFromBackend()
  if (route.hash) {
    const id = route.hash.replace(/^#/, '')
    scrollToSection(id, 92)
    void nextTick(() => {
      const target = document.getElementById(id)
      if (!target) return
      const top = window.scrollY + target.getBoundingClientRect().top - 92
      window.scrollTo({ top: Math.max(top, 0), behavior: 'auto' })
    })
  }
})

</script>

<template>
  <div
    class="portfolio-container"
    :class="{ 'dark-theme': isDarkMode }"
    :style="{ '--pointer-x': `${pointerX}%`, '--pointer-y': `${pointerY}%` }"
  >
    <!-- 背景 MP4：Shadow 内挂载；片名见 script 中 HOME_BG_* -->
    <SiteBackgroundVideos
      v-if="bgVideoReady"
      :is-dark="isDarkMode"
      :light-src="homeBgLightSrc"
      :dark-src="homeBgDarkSrc"
    />
    <div class="portfolio-bg-scrim" aria-hidden="true" />
    <div class="portfolio-bg-noise" aria-hidden="true" />
    <div class="portfolio-bg-scanline" aria-hidden="true" />
    <div class="bg-neon-orb bg-neon-orb--a" aria-hidden="true" />
    <div class="bg-neon-orb bg-neon-orb--b" aria-hidden="true" />
    <div class="bg-neon-orb bg-neon-orb--c" aria-hidden="true" />

    <!-- Hero Section -->
    <HomeHero
      :title-prefix="t('home.hello')"
      :name="t('home.name')"
      :explore-label="t('home.explore')"
      :read-blog-label="t('home.readBlog')"
      :subtitle="'全栈开发 | 二次元爱好者 | 分享与记录'"
      :stats-badge="visitBadge"
      :parallax-y="heroParallaxY"
      @explore="router.push('/works-showcase')"
      @read-blog="router.push('/blog')"
    />

    <!-- Hitokoto Quote -->
    <HitokotoCard />

    <!-- About Section -->
    <section class="section" id="about">
      <div class="glass-card about-card">
        <h2>{{ t('home.aboutTitle') }}</h2>
        <div class="about-content">
          <picture class="about-img-wrap">
            <source srcset="/avatar.webp" type="image/webp" />
            <img src="/avatar.png" alt="维寒一念" class="about-img" loading="lazy" decoding="async" />
          </picture>
          <div class="about-text">
            <p>{{ t('home.aboutText1') }}</p>
            <br />
            <p>{{ t('home.aboutText2') }}</p>
          </div>
        </div>
      </div>
    </section>

    <HomeWorksSection
      :title="t('home.worksTitle')"
      :detail-label="t('home.worksDetail')"
      :works="works"
      :categories="workCategories"
      :selected-category="selectedWorkCategory"
      @open="onWorkCardActivate"
      @change-category="worksStore.setCategory"
    />

    <!-- Blog & open source -->
    <section class="section" id="blog">
      <div class="glass-card home-hub-card">
        <h2 class="section-title-pill">{{ t('home.sectionBlogTitle') }}</h2>
        <p class="home-hub-lead section-lead-pill">{{ t('home.sectionBlogLead') }}</p>
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
        <picture class="contact-img-wrap">
          <source srcset="../../assets/images/contact-miku.webp" type="image/webp" />
          <img src="../../assets/images/contact-miku.jpg" alt="contact miku" class="contact-img" loading="lazy" decoding="async" />
        </picture>
        <div class="contact-links">
          <a href="https://github.com/weihanyinian" target="_blank" rel="noopener noreferrer">GitHub</a>
          <a href="mailto:1012308753@qq.com">Email</a>
        </div>
      </div>
    </section>

    <!-- 留言墙（拆分为独立组件） -->
    <MessageWallSection />

    <!-- 工具栏入口（拆分为独立组件） -->
    <ToolsSection />

    <nav class="mobile-tabbar" aria-label="home mobile sections">
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
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    'PingFang SC',
    'Microsoft YaHei',
    sans-serif;
  overflow-x: hidden;
  transition: background 0.5s ease, color 0.5s ease;
  position: relative;
  /* 与 body 背景分层：避免内部 z-index:-1/-2 与整页主内容叠乱导致「像全空白」 */
  isolation: isolate;
  z-index: 0;
}

@media (max-width: 900px) {
  .portfolio-container {
    padding-bottom: calc(76px + env(safe-area-inset-bottom));
  }
}

.portfolio-bg-scrim {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
  transition: background 0.45s ease;
}

.portfolio-bg-noise {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
  opacity: 0.03;
  background:
    radial-gradient(circle at 20% 25%, rgba(255, 255, 255, 0.18) 0 1px, transparent 1px) 0 0 / 4px 4px,
    radial-gradient(circle at 80% 60%, rgba(91, 155, 216, 0.12) 0 1px, transparent 1px) 0 0 / 6px 6px;
  mix-blend-mode: soft-light;
}

.portfolio-bg-scanline {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  pointer-events: none;
  opacity: 0.018;
  background: repeating-linear-gradient(180deg, rgba(255, 255, 255, 0.08) 0 1px, transparent 1px 6px);
  mix-blend-mode: soft-light;
}

.bg-neon-orb {
  position: fixed;
  pointer-events: none;
  z-index: -1;
  border-radius: 50%;
  opacity: 0.03;
  filter: blur(34px);
  transition: transform 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}
.bg-neon-orb--a {
  width: min(40vw, 460px);
  height: min(40vw, 460px);
  left: -8vw;
  top: 6vh;
  background: radial-gradient(circle, rgba(110, 231, 255, 0.42) 0%, rgba(110, 231, 255, 0) 70%);
  transform: translate3d(calc((var(--pointer-x, 50%) - 50%) * 0.12), calc((var(--pointer-y, 50%) - 50%) * 0.16), 0);
}
.bg-neon-orb--b {
  width: min(34vw, 420px);
  height: min(34vw, 420px);
  right: -6vw;
  top: 18vh;
  background: radial-gradient(circle, rgba(171, 139, 255, 0.4) 0%, rgba(171, 139, 255, 0) 72%);
  transform: translate3d(calc((50% - var(--pointer-x, 50%)) * 0.11), calc((var(--pointer-y, 50%) - 50%) * 0.14), 0);
}
.bg-neon-orb--c {
  width: min(30vw, 360px);
  height: min(30vw, 360px);
  right: 12vw;
  bottom: 2vh;
  background: radial-gradient(circle, rgba(244, 167, 194, 0.36) 0%, rgba(244, 167, 194, 0) 70%);
  transform: translate3d(calc((var(--pointer-x, 50%) - 50%) * 0.1), calc((50% - var(--pointer-y, 50%)) * 0.14), 0);
}
.dark-theme .bg-neon-orb {
  opacity: 0.035;
}
.dark-theme .portfolio-bg-scanline {
  opacity: 0.028;
}

.dark-theme .portfolio-bg-noise {
  opacity: 0.04;
  background:
    radial-gradient(circle at 25% 20%, rgba(255, 255, 255, 0.16) 0 1px, transparent 1px) 0 0 / 5px 5px,
    radial-gradient(circle at 75% 70%, rgba(196, 181, 253, 0.14) 0 1px, transparent 1px) 0 0 / 7px 7px;
}
@keyframes noiseDrift {
  0% { transform: translate3d(0, 0, 0); }
  50% { transform: translate3d(1px, -1px, 0); }
  100% { transform: translate3d(0, 0, 0); }
}

/* 日间叠层：轻模糊让底下视频有「磨砂」感，与顶栏/Hero 一致 */
.portfolio-container:not(.dark-theme) .portfolio-bg-scrim {
  background: linear-gradient(
    165deg,
    rgba(255, 255, 255, 0.14) 0%,
    rgba(186, 230, 253, 0.1) 42%,
    rgba(233, 213, 255, 0.09) 100%
  );
  backdrop-filter: blur(14px) saturate(1.08);
  -webkit-backdrop-filter: blur(14px) saturate(1.08);
}

/* 夜间：明显压暗 + 轻模糊，与日间一眼区分 */
.portfolio-container.dark-theme .portfolio-bg-scrim {
  background: linear-gradient(
    165deg,
    rgba(2, 6, 23, 0.55) 0%,
    rgba(15, 23, 42, 0.42) 48%,
    rgba(30, 27, 75, 0.48) 100%
  );
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

/* Dark Theme */
.portfolio-container.dark-theme {
  background: linear-gradient(135deg, rgba(2, 6, 23, 0.55) 0%, rgba(15, 23, 42, 0.42) 100%);
  color: #cbd5e1;
}

/* Typography：清晰易读，少用装饰性字距 */
h1, h2, h3 {
  font-weight: 700;
  margin-bottom: 1rem;
  letter-spacing: 0.01em;
  line-height: 1.28;
}
h2 {
  font-size: clamp(1.45rem, 2.2vw, 1.85rem);
  text-align: center;
  margin-bottom: 2rem;
  font-weight: 700;
  color: #1e3a8a;
  -webkit-text-fill-color: currentColor;
  background: none;
  background-clip: unset;
  -webkit-background-clip: unset;
  text-shadow: none;
}
.dark-theme h2 {
  color: #ddd6fe;
  -webkit-text-fill-color: currentColor;
  background: none;
  background-clip: unset;
  -webkit-background-clip: unset;
}

/* 内容玻璃：略提高不透明度 + 模糊，正文更易辨认 */
.glass-card {
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(14px) saturate(1.12);
  -webkit-backdrop-filter: blur(14px) saturate(1.12);
  border: 1px solid rgba(255, 255, 255, 0.55);
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08);
  transition: transform 0.32s ease, box-shadow 0.32s ease, background 0.3s ease;
}
.dark-theme .glass-card {
  background: rgba(15, 23, 42, 0.52);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.32);
}

/* Anchor scroll offset */
#hero, #about, #works, #blog, #contact, #message, #tools {
  scroll-margin-top: 5.5rem;
}

.center { text-align: center; }

.home-hub-card {
  padding: 36px 28px;
  border-radius: 24px;
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
}
.section-title-pill {
  width: fit-content;
  max-width: min(92vw, 560px);
  margin: 0 auto 0.95rem;
  padding: 0.48rem 1.05rem;
  border-radius: 14px;
  border: 1px solid rgba(37, 99, 235, 0.22);
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #1e3a8a;
  -webkit-text-fill-color: currentColor;
  background-clip: border-box;
  -webkit-background-clip: border-box;
  text-shadow: none;
  box-shadow: 0 8px 26px rgba(15, 23, 42, 0.08);
}
.dark-theme .section-title-pill {
  border-color: rgba(196, 181, 253, 0.35);
  background: rgba(15, 23, 42, 0.45);
  color: #e9d5ff;
  -webkit-text-fill-color: currentColor;
}
.section-lead-pill {
  width: fit-content;
  max-width: min(92vw, 700px);
  margin: 0 auto 1.25rem;
  padding: 0.5rem 0.95rem;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0.13));
  box-shadow: 0 6px 18px rgba(59, 130, 246, 0.08);
  color: color-mix(in srgb, var(--text-color, #0f172a) 80%, var(--primary-color, #4a90e2) 20%);
  -webkit-text-fill-color: currentColor;
  background-clip: border-box;
  -webkit-background-clip: border-box;
}
.dark-theme .section-title-pill {
  border-color: rgba(167, 139, 250, 0.42);
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.5), rgba(15, 23, 42, 0.42));
  color: #f1f5f9;
  -webkit-text-fill-color: currentColor;
  background-clip: border-box;
  -webkit-background-clip: border-box;
  text-shadow: 0 1px 14px rgba(167, 139, 250, 0.22);
}
.dark-theme .section-lead-pill {
  border-color: rgba(148, 163, 184, 0.28);
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.45), rgba(15, 23, 42, 0.35));
  color: rgba(226, 232, 240, 0.94);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.24);
}
.home-hub-lead {
  margin: 0 0 1.25rem;
  font-size: 0.94rem;
  line-height: 1.82;
  letter-spacing: 0.015em;
  font-weight: 430;
  opacity: 0.92;
  color: rgba(15, 23, 42, 0.9);
}
.dark-theme .home-hub-lead {
  color: rgba(241, 245, 249, 0.98);
  text-shadow: 0 1px 8px rgba(15, 23, 42, 0.55);
}
.home-hub-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}
.mobile-tabbar {
  display: none;
}

@media (max-width: 900px) {

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
    color: #0f172a;
    font-size: 0.78rem;
    font-weight: 700;
    cursor: pointer;
  }

  .mobile-tabbar__item--active {
    background: linear-gradient(135deg, var(--primary-color, #4a90e2), var(--secondary-color, #8b7fd8));
    color: #fff;
    border-color: rgba(255, 255, 255, 0.62);
  }

  .dark-theme .mobile-tabbar {
    background: rgba(15, 23, 42, 0.76);
    border-color: rgba(255, 255, 255, 0.14);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.48);
  }

  .dark-theme .mobile-tabbar__item {
    background: rgba(30, 41, 59, 0.72);
    border-color: rgba(148, 163, 184, 0.24);
    color: rgba(226, 232, 240, 0.96);
  }
}

/* General Sections */
.section {
  max-width: 1080px;
  margin: 0 auto;
  padding: 80px 20px;
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.55s ease, transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
}
.section.section--visible {
  opacity: 1;
  transform: translateY(0);
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
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  object-fit: cover;
  aspect-ratio: 4/3;
}
.about-img-wrap {
  width: 45%;
  display: block;
}
.dark-theme .about-img {
  box-shadow: 0 8px 24px rgba(0,0,0,0.5);
}
.about-text { flex: 1; }
.about-card p {
  font-weight: 400;
  color: rgba(15, 23, 42, 0.92);
  line-height: 1.75;
}
.dark-theme .about-card p {
  color: #e2e8f0;
}

/* Cards Hover：略提亮，不破坏通透 */
.glass-card:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.16);
  box-shadow: 0 16px 38px rgba(15, 23, 42, 0.14);
  border-color: color-mix(in srgb, var(--primary-color) 38%, rgba(255, 255, 255, 0.6));
}
.dark-theme .glass-card:hover {
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.35);
  border-color: color-mix(in srgb, #a78bfa 48%, rgba(255, 255, 255, 0.22));
}

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
.contact-img-wrap {
  width: 100%;
  max-width: 600px;
  margin: 0 auto 30px auto;
  display: block;
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
  .about-content { flex-direction: column; text-align: center; }
  .about-img-wrap { width: 100%; }
  .about-img { width: 100%; aspect-ratio: 4/3; }
  .contact-img { height: auto; aspect-ratio: 16/9; }
}

</style>
