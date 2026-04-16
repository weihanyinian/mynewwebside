<script setup lang="ts">
/**
 * 博客列表页
 * - 主导航仅使用 layouts/SiteLayout.vue，本页只提供左侧「二级导航」与业务区，避免顶栏重叠
 * - 玻璃态参数与首页规范一致，颜色对比度随 SiteLayout 注入的 data-theme / CSS 变量变化
 */
import { Search } from '@element-plus/icons-vue'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getPublicArticles, getTags, type ArticleListItem, type Category, type Tag } from '../../api/blog'
import { goToSiteHome } from '../../utils/siteHome'

// ---------- 可调整参数（设计 token，改这里即可全局微调）----------
const STYLE = {
  glassBg: 'rgba(255, 255, 255, 0.2)',
  glassBorder: 'rgba(255, 255, 255, 0.5)',
  glassShadow: '0 8px 32px rgba(102, 217, 255, 0.2)',
  glassBlur: '10px',
  radius: '16px',
  transition: '0.3s ease',
  /** 桌面端为左下角 Live2D 预留的左侧安全间距（与看板娘宽度、默认 left 偏移大致匹配） */
  mascotGutterLg: 'clamp(100px, 14vw, 240px)',
  sidebarWidth: '260px',
  gridMaxWidth: '1080px',
  skeletonCount: 6,
} as const

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()

const keyword = ref<string>((route.query.keyword as string) || '')
const categoryId = ref<number | undefined>(route.query.categoryId ? Number(route.query.categoryId) : undefined)
const tagId = ref<number | undefined>(route.query.tagId ? Number(route.query.tagId) : undefined)
const page = ref(route.query.page ? Number(route.query.page) - 1 : 0)
const size = ref(9)
const loading = ref(false)
const total = ref(0)
const showBackTop = ref(false)
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const items = ref<ArticleListItem[]>([])
const visibleCards = ref<number[]>([])

const hasGuardianBadge = ref(localStorage.getItem('guardianBadgeUnlocked') === 'true')
const clueCount = ref(Number(localStorage.getItem('blogClueCount') || '0'))

/**
 * 左侧二级导航（与顶栏区分：不含 OJ，顶栏已有独立入口；返回博客由面包屑承担）
 */
const sideNavItems = computed(() => [
  { label: locale.value === 'zh' ? '首页' : 'Home', path: '/', active: route.path === '/' },
  { label: locale.value === 'zh' ? '博客列表' : 'Blog', path: '/blog', active: route.path === '/blog' },
  { label: locale.value === 'zh' ? '分类' : 'Categories', path: '/categories', active: route.path === '/categories' },
  { label: locale.value === 'zh' ? '标签' : 'Tags', path: '/tags', active: route.path === '/tags' },
  { label: t('nav.message'), path: '/message', active: route.path === '/message' },
])

const categoryTabs = computed(() => {
  const tabs = [{ id: 0, name: locale.value === 'zh' ? '全部' : 'All' }]
  return tabs.concat(categories.value.map(c => ({ id: c.id, name: c.name })))
})

function articleReadMinutes(item: ArticleListItem) {
  const totalWords = (item.summary || '').length + (item.title || '').length
  return Math.max(1, Math.round(totalWords / 130))
}

function syncToRoute() {
  const query: Record<string, string> = {}
  if (keyword.value.trim()) query.keyword = keyword.value.trim()
  if (categoryId.value) query.categoryId = String(categoryId.value)
  if (tagId.value) query.tagId = String(tagId.value)
  if (page.value > 0) query.page = String(page.value + 1)
  router.push({ path: '/blog', query })
}

function selectCategory(id: number) {
  categoryId.value = id === 0 ? undefined : id
  page.value = 0
  syncToRoute()
}

function selectTag(id: number) {
  tagId.value = id
  page.value = 0
  syncToRoute()
}

function clearTagFilter() {
  tagId.value = undefined
  page.value = 0
  syncToRoute()
}

function goArticleDetail(articleId: number) {
  router.push(`/article/${articleId}`)
}

function goPath(path: string) {
  if (path === '/') {
    goToSiteHome(router)
  } else {
    router.push(path)
  }
}

function onCardVisible(index: number) {
  if (!visibleCards.value.includes(index)) visibleCards.value.push(index)
}

function triggerCardEnterAnimation() {
  visibleCards.value = []
  requestAnimationFrame(() => {
    document.querySelectorAll('[data-animate-card]').forEach((_, index) => {
      window.setTimeout(() => onCardVisible(index), 60 * index)
    })
  })
}

function onScroll() {
  showBackTop.value = window.scrollY > 480
}

function backToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function collectClue() {
  clueCount.value += 1
  localStorage.setItem('blogClueCount', String(clueCount.value))
  if (clueCount.value >= 3) {
    hasGuardianBadge.value = true
    localStorage.setItem('guardianBadgeUnlocked', 'true')
  }
}

function handlePageChange(p: number) {
  page.value = p - 1
  syncToRoute()
}

async function load() {
  loading.value = true
  try {
    const res = await getPublicArticles({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      tagId: tagId.value,
      page: page.value,
      size: size.value,
    })
    items.value = res.items
    total.value = res.total
    triggerCardEnterAnimation()
  } finally {
    loading.value = false
  }
}

function syncStateFromRoute() {
  keyword.value = (route.query.keyword as string) || ''
  categoryId.value = route.query.categoryId ? Number(route.query.categoryId) : undefined
  tagId.value = route.query.tagId ? Number(route.query.tagId) : undefined
  page.value = route.query.page ? Math.max(0, Number(route.query.page) - 1) : 0
}

watch(
  () => route.fullPath,
  () => {
    syncStateFromRoute()
    load()
  },
  { immediate: true },
)

onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  const [c, tg] = await Promise.all([getCategories(true), getTags(true)])
  categories.value = c
  tags.value = tg
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <section class="blog-home">
    <!-- 顶栏面包屑已承担「本站 / 博客」路径；此处不再重复「返回博客 / 本站导航」条 -->

    <div
      class="blog-layout"
      :style="{
        '--blog-sidebar-width': STYLE.sidebarWidth,
        '--blog-mascot-gutter': STYLE.mascotGutterLg,
        '--blog-grid-max': STYLE.gridMaxWidth,
      }"
    >
      <aside class="blog-sidebar glass-surface">
        <div class="blog-sidebar__inner">
          <div class="blog-profile">
            <img
              src="../../assets/images/about-miku.jpg"
              alt=""
              class="blog-avatar"
              width="72"
              height="72"
              @click="collectClue"
            />
            <div class="blog-profile__text">
              <h2 class="blog-profile__name">{{ t('home.name') }}</h2>
              <p class="blog-profile__bio">{{ locale === 'zh' ? '保持热爱，奔赴山海' : 'Stay curious.' }}</p>
            </div>
          </div>

          <nav class="blog-side-nav" aria-label="secondary">
            <button
              v-for="item in sideNavItems"
              :key="item.path"
              type="button"
              class="site-pill site-pill--block"
              :class="{
                'site-pill--active': item.active && item.path !== '/moyu',
                'site-pill--pink': item.active && item.path === '/moyu',
              }"
              @click="goPath(item.path)"
            >
              {{ item.label }}
            </button>
          </nav>

          <div class="blog-clue glass-surface glass-surface--inset">
            <p class="blog-clue__title">{{ locale === 'zh' ? '14酱解密碎片' : '14-chan clues' }}</p>
            <p class="blog-clue__stat">{{ locale === 'zh' ? '已收集' : 'Collected' }}：{{ clueCount }} / 3</p>
            <p v-if="hasGuardianBadge" class="blog-badge">{{ locale === 'zh' ? '14酱守护者' : 'Guardian' }}</p>
            <p class="blog-clue__tip">{{ locale === 'zh' ? '点头像可收集碎片（彩蛋）' : 'Tap avatar for a clue.' }}</p>
          </div>
        </div>
      </aside>

      <main class="blog-main">
        <!-- 搜索与筛选：单列堆叠 → 宽屏横向，避免溢出 -->
        <!-- site-el-round-16：Element Plus 输入/下拉圆角与全站 16px 对齐 -->
        <section class="blog-toolbar glass-surface site-el-round-16">
          <div class="blog-toolbar__grid">
            <el-input
              v-model="keyword"
              clearable
              size="large"
              :placeholder="locale === 'zh' ? '搜索标题 / 标签 / 内容…' : 'Search…'"
              @keyup.enter="syncToRoute"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="tagId"
              clearable
              size="large"
              :placeholder="locale === 'zh' ? '按标签筛选' : 'Filter by tag'"
              @change="syncToRoute"
            >
              <el-option v-for="tItem in tags" :key="tItem.id" :label="tItem.name" :value="tItem.id" />
            </el-select>
            <button type="button" class="site-pill" @click="syncToRoute">
              {{ locale === 'zh' ? '搜索' : 'Search' }}
            </button>
          </div>

          <div class="blog-toolbar__chips">
            <button
              v-for="tab in categoryTabs"
              :key="tab.id"
              type="button"
              class="site-pill site-pill--chip"
              :class="{ 'site-pill--active': (categoryId ?? 0) === tab.id }"
              @click="selectCategory(tab.id)"
            >
              {{ tab.name }}
            </button>
            <button v-if="tagId" type="button" class="site-pill site-pill--chip" @click="clearTagFilter">
              {{ locale === 'zh' ? '清除标签' : 'Clear tag' }}
            </button>
          </div>
        </section>

        <header class="blog-heading">
          <h1 class="blog-heading__title">{{ locale === 'zh' ? '最新博客' : 'Latest Posts' }}</h1>
          <div class="blog-heading__line" />
        </header>

        <!-- 骨架屏 -->
        <div v-if="loading" class="blog-grid blog-grid--center" aria-busy="true">
          <div v-for="n in STYLE.skeletonCount" :key="'sk-' + n" class="blog-skeleton glass-surface">
            <div class="blog-skeleton__cover" />
            <div class="blog-skeleton__body">
              <div class="blog-skeleton__line blog-skeleton__line--short" />
              <div class="blog-skeleton__line" />
              <div class="blog-skeleton__line" />
              <div class="blog-skeleton__meta" />
            </div>
          </div>
        </div>

        <!-- 文章卡片 -->
        <div v-else class="blog-grid blog-grid--center">
          <article
            v-for="(article, index) in items"
            :key="article.id"
            data-animate-card
            class="blog-card glass-surface"
            :class="{ 'blog-card--visible': visibleCards.includes(index) }"
            @click="goArticleDetail(article.id)"
          >
            <div class="blog-card__cover">
              <img
                v-if="article.coverUrl"
                :src="article.coverUrl"
                :alt="article.title"
                class="blog-card__img"
              />
              <div v-else class="blog-card__placeholder">✦</div>
            </div>
            <div class="blog-card__body">
              <div class="blog-card__tags">
                <button
                  v-for="tg in article.tags"
                  :key="tg.id"
                  type="button"
                  class="site-pill site-pill--chip site-pill--chip-sm"
                  @click.stop="selectTag(tg.id)"
                >
                  # {{ tg.name }}
                </button>
              </div>
              <h3 class="blog-card__title">{{ article.title }}</h3>
              <p class="blog-card__summary">
                {{ article.summary || (locale === 'zh' ? '这篇文章正在等待你探索。' : 'No summary yet.') }}
              </p>
              <div class="blog-card__meta">
                <span>{{ article.publishedAt ? new Date(article.publishedAt).toLocaleDateString() : '—' }}</span>
                <span>👁 {{ article.views }}</span>
                <span>⏱ {{ articleReadMinutes(article) }} min</span>
              </div>
            </div>
          </article>
        </div>

        <!-- 空状态：单独一行居中 -->
        <div v-if="!loading && items.length === 0" class="blog-empty-wrap">
          <div class="blog-empty glass-surface">
            <div class="blog-empty__icon">🍃</div>
            <p class="blog-empty__text">
              {{ locale === 'zh' ? '暂无匹配文章，换个关键词试试吧。' : 'No posts match. Try other keywords.' }}
            </p>
          </div>
        </div>

        <div v-if="!loading && total > 0" class="blog-pager">
          <el-pagination
            background
            layout="prev, pager, next"
            :current-page="page + 1"
            :page-size="size"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </main>
    </div>

    <transition
      enter-active-class="blog-fade-enter-active"
      leave-active-class="blog-fade-leave-active"
      enter-from-class="blog-fade-enter-from"
      leave-to-class="blog-fade-leave-to"
    >
      <button v-if="showBackTop" type="button" class="site-pill site-pill--active blog-back-top" @click="backToTop">
        ↑ {{ locale === 'zh' ? '顶部' : 'Top' }}
      </button>
    </transition>
  </section>
</template>

<style scoped>
/* 由 SiteLayout 写入 --blog-on-glass / --blog-on-glass-muted，保证浅深色对比 */
.blog-home {
  --on-glass: var(--blog-on-glass, #1a3a52);
  --on-glass-muted: var(--blog-on-glass-muted, rgba(26, 58, 82, 0.75));
  font-family: system-ui, -apple-system, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding-bottom: 4rem;
}

.blog-layout {
  display: grid;
  grid-template-columns: var(--blog-sidebar-width) minmax(0, 1fr);
  gap: 1.25rem;
  align-items: start;
}

@media (min-width: 1024px) {
  .blog-home {
    /* 整体右移，为左下角 Live2D 留出可视与拖拽区域 */
    padding-left: var(--blog-mascot-gutter);
  }
}

@media (max-width: 1023px) {
  .blog-layout {
    grid-template-columns: 1fr;
  }
}

.glass-surface {
  background: v-bind('STYLE.glassBg');
  border: 1px solid v-bind('STYLE.glassBorder');
  backdrop-filter: blur(v-bind('STYLE.glassBlur'));
  -webkit-backdrop-filter: blur(v-bind('STYLE.glassBlur'));
  box-shadow: v-bind('STYLE.glassShadow');
  border-radius: v-bind('STYLE.radius');
  transition: transform v-bind('STYLE.transition'), box-shadow v-bind('STYLE.transition');
}

.glass-surface--inset {
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35), v-bind('STYLE.glassShadow');
}

/* ---------- 侧栏 ---------- */
/**
 * 【核心修改2】侧栏容器：16px 圆角 + 强化玻璃态（与全站青蓝毛玻璃统一），
 * 不改动 grid 布局与 sticky 行为，避免影响主栏与左下角看板娘区域。
 */
.blog-sidebar {
  position: sticky;
  top: 5.75rem;
  padding: 1.25rem 1.125rem;
  max-height: calc(100vh - 6.5rem);
  overflow: auto;
  background: rgba(255, 255, 255, 0.22) !important;
  border: 1px solid rgba(255, 255, 255, 0.55) !important;
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.22), inset 0 1px 0 rgba(255, 255, 255, 0.35);
}

:root[data-theme='dark'] .blog-sidebar {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.42) !important;
  box-shadow: 0 8px 28px rgba(0, 30, 60, 0.35), inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

.blog-sidebar__inner {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.blog-profile {
  display: flex;
  align-items: center;
  gap: 0.875rem;
}

.blog-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid v-bind('STYLE.glassBorder');
  cursor: pointer;
  transition: transform v-bind('STYLE.transition');
}

.blog-avatar:hover {
  transform: scale(1.04);
}

.blog-profile__name {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 800;
  color: var(--on-glass);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.12);
}

.blog-profile__bio {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  line-height: 1.45;
  color: var(--on-glass-muted);
}

.blog-side-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

/* 侧栏导航视觉由全局 styles/site-ui.css 的 .site-pill* 统一提供 */

.blog-clue {
  padding: 0.75rem 1rem;
  font-size: 0.8125rem;
}

.blog-clue__title {
  margin: 0 0 0.35rem;
  font-weight: 700;
  color: var(--on-glass);
}

.blog-clue__stat {
  margin: 0;
  color: var(--on-glass-muted);
}

.blog-clue__tip {
  margin: 0.5rem 0 0;
  font-size: 0.75rem;
  color: var(--on-glass-muted);
}

.blog-badge {
  display: inline-flex;
  margin: 0.5rem 0 0;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #4a90e2, #50e3c2);
}

/* 移动端：侧栏改为栅格内联展示（与桌面同为「侧栏 + 主栏」顺序），无需抽屉/关闭钮 */
@media (max-width: 1023px) {
  .blog-sidebar {
    position: relative;
    top: auto;
    width: 100%;
    max-height: none;
    margin-bottom: 0.5rem;
  }
}

/* ---------- 主内容 ---------- */
.blog-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 2rem;
}

.blog-toolbar {
  width: 100%;
  max-width: var(--blog-grid-max);
  padding: 1rem 1.125rem;
  margin-bottom: 1.25rem;
}

.blog-toolbar__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.75rem;
}

@media (min-width: 768px) {
  .blog-toolbar__grid {
    grid-template-columns: minmax(0, 1.4fr) minmax(160px, 0.9fr) auto;
    align-items: center;
  }
}

.blog-toolbar__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 1rem;
}

.blog-heading {
  width: 100%;
  max-width: var(--blog-grid-max);
  text-align: center;
  margin-bottom: 1.25rem;
}

.blog-heading__title {
  margin: 0;
  font-size: clamp(1.75rem, 4vw, 2.25rem);
  font-weight: 900;
  line-height: 1.2;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.blog-heading__line {
  width: 5rem;
  height: 0.3rem;
  margin: 0.6rem auto 0;
  border-radius: 999px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
}

.blog-grid {
  width: 100%;
  max-width: var(--blog-grid-max);
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.blog-grid--center {
  justify-items: stretch;
}

@media (min-width: 640px) {
  .blog-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 1100px) {
  .blog-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

/* 卡片 */
.blog-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  opacity: 0;
  transform: translateY(12px);
  transition:
    opacity v-bind('STYLE.transition'),
    transform v-bind('STYLE.transition'),
    box-shadow v-bind('STYLE.transition');
}

.blog-card--visible {
  opacity: 1;
  transform: translateY(0);
}

.blog-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 14px 36px rgba(102, 217, 255, 0.28);
}

.blog-card--visible:hover {
  transform: translateY(-4px) scale(1.01);
}

.blog-card__cover {
  height: 168px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.15);
}

.blog-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
}

.blog-card:hover .blog-card__img {
  transform: scale(1.05);
}

.blog-card__placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: var(--on-glass-muted);
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.35), rgba(80, 227, 194, 0.3));
}

.blog-card__body {
  padding: 1rem 1.125rem 1.125rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
}

.blog-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}

.blog-card__title {
  margin: 0;
  font-size: 1.0625rem;
  font-weight: 800;
  line-height: 1.35;
  color: var(--on-glass);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.blog-card__summary {
  margin: 0;
  flex: 1;
  font-size: 0.875rem;
  line-height: 1.6;
  color: var(--on-glass-muted);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--on-glass-muted);
}

/* 骨架 */
.blog-skeleton__cover {
  height: 168px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.08) 25%, rgba(255, 255, 255, 0.22) 40%, rgba(255, 255, 255, 0.08) 55%);
  background-size: 200% 100%;
  animation: blog-shimmer 1.2s ease-in-out infinite;
}

.blog-skeleton__body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.blog-skeleton__line {
  height: 0.65rem;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.08) 25%, rgba(255, 255, 255, 0.2) 40%, rgba(255, 255, 255, 0.08) 55%);
  background-size: 200% 100%;
  animation: blog-shimmer 1.2s ease-in-out infinite;
}

.blog-skeleton__line--short {
  width: 45%;
}

.blog-skeleton__meta {
  height: 0.65rem;
  width: 70%;
  margin-top: 0.25rem;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.08) 25%, rgba(255, 255, 255, 0.18) 40%, rgba(255, 255, 255, 0.08) 55%);
  background-size: 200% 100%;
  animation: blog-shimmer 1.2s ease-in-out infinite;
}

@keyframes blog-shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 空状态 */
.blog-empty-wrap {
  width: 100%;
  max-width: var(--blog-grid-max);
  display: flex;
  justify-content: center;
  padding: 2rem 0 1rem;
}

.blog-empty {
  width: 100%;
  max-width: 420px;
  padding: 2rem 1.5rem;
  text-align: center;
}

.blog-empty__icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.blog-empty__text {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.6;
  color: var(--on-glass);
}

/* Element Plus 与主题统一 */
:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  border-radius: v-bind('STYLE.radius') !important;
  background: rgba(255, 255, 255, 0.22) !important;
  border: 1px solid v-bind('STYLE.glassBorder') !important;
  box-shadow: none !important;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

:deep(.el-input__inner) {
  color: var(--on-glass) !important;
  text-shadow: none;
}

:deep(.el-select__placeholder) {
  color: var(--on-glass-muted) !important;
}

:deep(.el-select .el-input__suffix .el-icon) {
  color: var(--on-glass-muted);
}

:deep(.el-pagination.is-background .el-pager li),
:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next) {
  border-radius: v-bind('STYLE.radius');
  border: 1px solid v-bind('STYLE.glassBorder');
  background: rgba(255, 255, 255, 0.18);
  color: var(--on-glass);
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.9), rgba(80, 227, 194, 0.92));
  color: #fff;
}

.blog-pager {
  width: 100%;
  max-width: var(--blog-grid-max);
  display: flex;
  justify-content: center;
  margin-top: 1.75rem;
  padding-bottom: 0.5rem;
}

.blog-back-top {
  position: fixed;
  right: 1.25rem;
  bottom: 1.25rem;
  z-index: 50;
  font-weight: 800;
  font-size: 0.8125rem;
  cursor: pointer;
  box-shadow: v-bind('STYLE.glassShadow');
}

.blog-fade-enter-active,
.blog-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.blog-fade-enter-from,
.blog-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@media (min-width: 1024px) {
  .blog-back-top {
    right: 1.5rem;
    bottom: 1.5rem;
  }
}
</style>
