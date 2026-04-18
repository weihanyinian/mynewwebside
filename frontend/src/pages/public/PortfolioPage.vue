<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { RouterLink } from 'vue-router'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { goToSiteHome } from '../../utils/siteHome'
import { useI18n } from 'vue-i18n'
import HitokotoCard from '../../components/HitokotoCard.vue'
import SiteGlassFooter from '../../components/site/SiteGlassFooter.vue'
import { useThemeStore } from '../../stores/theme'
import { useUserStore } from '../../stores/user'
import { getPublicArticles, type ArticleListItem } from '../../api/blog'
import { fetchWallMessages, submitWallMessage, type WallMessagePublic } from '../../api/wall'

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

const works = ref([
  { title: '大语言模型微调与部署', desc: '基于 GLM4 与 LoRA 技术的大模型微调实战项目，探索垂直领域大语言模型应用。', link: '#works', tag: 'LLM / GLM4' },
  { title: 'Transformer 机器翻译', desc: '基于底层 Transformer 架构从零构建的机器翻译模型，深入理解 Attention 机制。', link: '#works', tag: 'Deep Learning' },
  { title: 'MyWebSide Blog', desc: '个人专属数字花园，基于 Spring Boot 3 与 Vue 3 构建的全栈展示平台。', link: '#blog', tag: 'Full Stack' },
  { title: '在线判题 OJ', desc: '内置算法题库与 Judge0 沙箱，支持 C/C++/Java/Python，ACM 与力扣风格评测。', link: '/tools/oj', tag: 'OJ / Sandbox' },
])

// Smooth scroll for nav anchors
function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

function onWorkClick(link: string) {
  if (link.startsWith('#') && link.length > 1) {
    scrollTo(link.slice(1))
    return
  }
  if (link.startsWith('/')) {
    router.push(link)
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

/** ---------- 主页内嵌：留言 / 归档 / 作品集 ---------- */
const wallLoading = ref(true)
const wallMessages = ref<WallMessagePublic[]>([])
const wallAuthor = ref('')
const wallContent = ref('')
const wallSubmitting = ref(false)

async function loadWall() {
  wallLoading.value = true
  try {
    wallMessages.value = await fetchWallMessages()
  } catch {
    wallMessages.value = []
  } finally {
    wallLoading.value = false
  }
}

async function submitWall() {
  if (!wallContent.value.trim()) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  wallSubmitting.value = true
  try {
    await submitWallMessage(wallAuthor.value, wallContent.value)
    wallContent.value = ''
    wallAuthor.value = ''
    ElMessage.success(t('messageWall.pendingReview'))
    await loadWall()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : 'Error')
  } finally {
    wallSubmitting.value = false
  }
}

function formatWallTime(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

const archLoading = ref(true)
const archItems = ref<ArticleListItem[]>([])

const archByYear = computed(() => {
  const m = new Map<number, ArticleListItem[]>()
  for (const a of archItems.value) {
    const y = a.publishedAt ? new Date(a.publishedAt).getFullYear() : 0
    if (!m.has(y)) m.set(y, [])
    m.get(y)!.push(a)
  }
  return [...m.entries()].sort((a, b) => b[0] - a[0])
})

async function loadArchives() {
  archLoading.value = true
  try {
    const page = await getPublicArticles({ page: 0, size: 400 })
    archItems.value = page.items
  } catch {
    archItems.value = []
  } finally {
    archLoading.value = false
  }
}

type AlbumItem = { src: string; w: number; h: number; title?: string }
type AlbumManifest = { title?: string; items: AlbumItem[] }
const albumManifest = ref<AlbumManifest | null>(null)

async function loadAlbumManifest() {
  try {
    const res = await fetch('/albums/manifest.json')
    if (res.ok) albumManifest.value = await res.json()
    else albumManifest.value = null
  } catch {
    albumManifest.value = null
  }
}

const homeToolCards = computed(() => [
  { path: '/tools', title: t('nav.tools'), desc: t('toolsHub.subtitle') },
  { path: '/tools/reaction', title: t('tools.reaction'), desc: t('toolsHub.cardReactionDesc') },
  { path: '/tools/cps', title: t('tools.cps'), desc: t('toolsHub.cardCpsDesc') },
  { path: '/tools/pomodoro', title: t('tools.pomodoro'), desc: t('toolsHub.cardPomodoroDesc') },
  { path: '/tools/password', title: t('tools.password'), desc: t('toolsHub.cardPasswordDesc') },
  { path: '/tools/base64', title: t('tools.base64'), desc: t('toolsHub.cardBase64Desc') },
  { path: '/tools/oj', title: t('toolsHub.cardOjTitle'), desc: t('toolsHub.cardOjDesc') },
])

onMounted(() => {
  window.addEventListener('scroll', onHeroParallax, { passive: true })
  void loadWall()
  void loadArchives()
  void loadAlbumManifest()
})

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
          <!-- 首页顶栏：仅页内锚点滚动，不跳到子路由（登录等与摸鱼、后台除外） -->
          <a href="#about" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('about')">{{ t('nav.about') }}</a>
          <a href="#works" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('works')">{{ t('nav.works') }}</a>
          <a href="#blog" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('blog')">{{ t('nav.blog') }}</a>
          <a href="#contact" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('contact')">{{ t('nav.contact') }}</a>
          <a href="#message" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('message')">{{ t('nav.message') }}</a>
          <a href="#albums" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('albums')">{{ t('breadcrumb.albums') }}</a>
          <a href="#archives" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('archives')">{{ t('breadcrumb.archives') }}</a>
          <a href="#snippets" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('snippets')">{{ t('breadcrumb.snippets') }}</a>
          <a href="#tools" class="site-pill site-pill--nav site-pill--keep-mobile" @click.prevent="scrollTo('tools')">{{ t('nav.tools') }}</a>
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
          <button type="button" class="site-pill site-pill--lg" @click="scrollTo('blog')">{{ t('home.readBlog') }}</button>
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
          <a
            v-if="work.link.startsWith('/') || work.link.startsWith('#')"
            href="#"
            class="work-link"
            @click.prevent="onWorkClick(work.link)"
          >{{ t('home.worksDetail') }} &rarr;</a>
          <a v-else :href="work.link" class="work-link" target="_blank" rel="noopener noreferrer">{{ t('home.worksDetail') }} &rarr;</a>
        </div>
      </div>
    </section>

    <!-- Blog & open source（原「前往博客」改为下滑到此） -->
    <section class="section" id="blog">
      <div class="glass-card home-hub-card">
        <h2>{{ t('home.sectionBlogTitle') }}</h2>
        <p class="home-hub-lead">{{ t('home.sectionBlogLead') }}</p>
        <div class="home-hub-actions">
          <a :href="githubRepo" class="site-pill site-pill--lg" target="_blank" rel="noopener noreferrer">{{ t('home.sectionBlogRepo') }}</a>
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
        <img src="../../assets/images/contact-miku.jpg" alt="contact miku" class="contact-img" />
        <div class="contact-links">
          <a href="https://github.com/weihanyinian" target="_blank" rel="noopener noreferrer">GitHub</a>
          <a href="mailto:1012308753@qq.com">Email</a>
        </div>
        <div class="contact-account glass-inset">
          <template v-if="isLoggedIn">
            <button type="button" class="site-pill" @click="logout">{{ t('nav.logout') }}</button>
          </template>
          <template v-else>
            <button type="button" class="site-pill" @click="router.push('/login')">{{ t('nav.login') }}</button>
            <button type="button" class="site-pill site-pill--active" @click="router.push('/register')">{{ t('nav.register') }}</button>
          </template>
        </div>
      </div>
    </section>

    <!-- 留言墙（嵌入主页） -->
    <section class="section" id="message">
      <h2>{{ t('messageWall.title') }}</h2>
      <p class="section-sub">{{ t('messageWall.subtitle') }}</p>
      <div class="glass-card home-message-form site-el-round-16">
        <el-input v-model="wallAuthor" :placeholder="t('messageWall.nickname')" class="home-field" />
        <el-input v-model="wallContent" type="textarea" :rows="3" :placeholder="t('messageWall.placeholder')" class="home-field" />
        <button type="button" class="site-pill site-pill--pink" :disabled="wallSubmitting" @click="submitWall">
          {{ t('messageWall.submit') }}
        </button>
      </div>
      <p v-if="wallLoading" class="muted center">{{ t('pages.loading') }}</p>
      <div v-else class="home-wall-grid">
        <div v-for="m in wallMessages" :key="m.id" class="glass-card home-wall-card">
          <div class="home-wall-meta">{{ m.nickname || t('messageWall.anonymous') }} · {{ formatWallTime(m.createdAt) }}</div>
          <p class="home-wall-body">{{ m.content }}</p>
          <p v-if="m.adminReply" class="home-wall-reply">{{ t('messageWall.replyFromAdmin') }}：{{ m.adminReply }}</p>
        </div>
        <p v-if="!wallMessages.length" class="muted center">{{ t('messageWall.emptyTitle') }}</p>
      </div>
    </section>

    <!-- 作品集缩略 -->
    <section class="section" id="albums">
      <h2>{{ albumManifest?.title || t('pages.albumsTitle') }}</h2>
      <div v-if="albumManifest?.items?.length" class="home-album-grid">
        <figure v-for="(it, i) in albumManifest.items" :key="i" class="home-album-item glass-card">
          <img :src="it.src" :alt="it.title || ''" loading="lazy" />
          <figcaption v-if="it.title">{{ it.title }}</figcaption>
        </figure>
      </div>
      <p v-else class="muted center">{{ t('home.homeAlbumFallback') }}</p>
    </section>

    <!-- 归档摘要 -->
    <section class="section" id="archives">
      <h2>{{ t('pages.archivesTitle') }}</h2>
      <p v-if="archLoading" class="muted center">{{ t('pages.loading') }}</p>
      <p v-else-if="!archItems.length" class="muted center">{{ t('pages.archivesEmpty') }}</p>
      <div v-else class="home-arch-blocks">
        <div v-for="[year, posts] in archByYear" :key="year" class="glass-card home-arch-year">
          <h3 class="home-arch-year-title">{{ year || '—' }} <span class="count">({{ posts.length }})</span></h3>
          <ul class="home-arch-list">
            <li v-for="p in posts.slice(0, 8)" :key="p.id">
              <RouterLink :to="`/article/${p.id}`" class="arch-link">{{ p.title }}</RouterLink>
              <span class="arch-date">{{ p.publishedAt?.replace('T', ' ').slice(0, 10) }}</span>
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- 代码片段 -->
    <section class="section" id="snippets">
      <div class="glass-card home-hub-card">
        <h2>{{ t('pages.snippetsTitle') }}</h2>
        <p class="home-hub-lead">{{ t('home.sectionSnippetsLead') }}</p>
        <button type="button" class="site-pill site-pill--active" @click="router.push('/snippets')">{{ t('home.sectionSnippetsOpen') }}</button>
      </div>
    </section>

    <!-- 工具栏入口 -->
    <section class="section" id="tools">
      <h2>{{ t('toolsHub.title') }}</h2>
      <p class="section-sub">{{ t('home.sectionToolsLead') }}</p>
      <div class="home-tools-grid">
        <div v-for="card in homeToolCards" :key="card.path" class="glass-card home-tool-card">
          <h3>{{ card.title }}</h3>
          <p>{{ card.desc }}</p>
          <button type="button" class="site-pill site-pill--active" @click="router.push(card.path)">{{ t('home.sectionToolsOpen') }}</button>
        </div>
      </div>
    </section>

    <SiteGlassFooter />
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

/* Navigation（顶边线：全局 .site-nav-unified；内高光与原有阴影合并）
   z-index 高于看板娘(999)与全局音乐播放器(mp-root 1100)，避免顶栏右侧链接被吞点击 */
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
  color: #2c3e50;
  background: rgba(255, 255, 255, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: opacity 0.2s, transform 0.2s;
  white-space: nowrap;
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
#blog,
#contact,
#message,
#albums,
#archives,
#snippets,
#tools {
  scroll-margin-top: 5.5rem;
}

.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  opacity: 0.85;
  line-height: 1.6;
}

.center {
  text-align: center;
}

.home-hub-card {
  padding: 36px 28px;
  border-radius: 24px;
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
}

.home-hub-lead {
  margin: 0 0 1.25rem;
  line-height: 1.65;
  opacity: 0.9;
}

.home-hub-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.contact-account {
  margin-top: 24px;
  padding: 16px;
  border-radius: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.glass-inset {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.dark-theme .glass-inset {
  background: rgba(0, 0, 0, 0.2);
}

.home-message-form {
  max-width: 560px;
  margin: 0 auto 28px;
  padding: 24px;
}

.home-message-form .home-field {
  margin-bottom: 12px;
}

.home-wall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
}

.home-wall-card {
  padding: 16px;
  border-radius: 16px;
}

.home-wall-meta {
  font-size: 0.78rem;
  opacity: 0.65;
  margin-bottom: 8px;
}

.home-wall-body {
  margin: 0;
  line-height: 1.55;
}

.home-wall-reply {
  margin: 10px 0 0;
  font-size: 0.85rem;
  opacity: 0.8;
  border-left: 3px solid rgba(102, 217, 255, 0.5);
  padding-left: 10px;
}

.home-album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
}

.home-album-item {
  margin: 0;
  padding: 0;
  overflow: hidden;
  border-radius: 16px;
}

.home-album-item img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}

.home-album-item figcaption {
  padding: 10px 12px;
  font-size: 0.85rem;
}

.home-arch-blocks {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.home-arch-year {
  padding: 20px 22px;
  border-radius: 18px;
}

.home-arch-year-title {
  margin: 0 0 12px;
  font-size: 1.2rem;
  color: #4a90e2;
}

.dark-theme .home-arch-year-title {
  color: #7dd3fc;
}

.home-arch-year-title .count {
  font-size: 0.9rem;
  opacity: 0.7;
}

.home-arch-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.home-arch-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: baseline;
  font-size: 0.92rem;
}

.arch-link {
  color: inherit;
  text-decoration: none;
  font-weight: 600;
}

.arch-link:hover {
  text-decoration: underline;
}

.arch-date {
  font-size: 0.78rem;
  opacity: 0.55;
  flex-shrink: 0;
}

.home-tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
}

.home-tool-card {
  padding: 20px;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.home-tool-card h3 {
  margin: 0;
  font-size: 1.05rem;
}

.home-tool-card p {
  margin: 0;
  flex: 1;
  font-size: 0.88rem;
  opacity: 0.85;
  line-height: 1.5;
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

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  .nav-social {
    display: none;
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
