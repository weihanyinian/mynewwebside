<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useThemeStore } from '../../stores/theme'
import { getPublicArticle, getPublicArticles, type ArticleDetail, type ArticleListItem } from '../../api/blog'
import MarkdownView from '../../components/MarkdownView.vue'
import SiteBackToTop from '../../components/site/SiteBackToTop.vue'
import { useSeoMeta } from '../../composables/useSeoMeta'
import { toggleArticleLike, getArticleLikeStatus } from '../../api/blog'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const themeStore = useThemeStore()

const loading = ref(false)
const article = ref<ArticleDetail | null>(null)

type TocItem = {
  id: string
  level: number
  text: string
}

const toc = ref<TocItem[]>([])
const activeTocId = ref<string>('')
const tocOpen = ref(false)
const showBackTop = ref(false)
const readProgress = ref(0)
const relatedPosts = ref<ArticleListItem[]>([])
const likeCount = ref(0)
const liked = ref(false)

async function toggleLike() {
  if (!article.value) return
  try {
    const r = await toggleArticleLike(article.value.id)
    likeCount.value = r.count
    liked.value = r.liked
  } catch { /* ignore — we show login prompt via ElMessage */ }
}
const isDarkMode = computed(() => themeStore.isDarkMode)

const navItems = computed(() => [
  { name: t('nav.blog'), path: '/blog' },
  { name: t('nav.works'), path: '/#works' },
  { name: t('nav.message'), path: '/message' }
])

const readTime = computed(() => {
  const md = article.value?.contentMd || ''
  const cjk = (md.match(/[\u4e00-\u9fa5]/g) || []).length
  const latinWords = (md.replace(/[\u4e00-\u9fa5]/g, ' ').match(/\b\w+\b/g) || []).length
  const words = cjk + latinWords
  const minutes = Math.max(1, Math.round(words / 220))
  return `${minutes} min`
})

const publishedAtText = computed(() => {
  const dt = article.value?.publishedAt
  if (!dt) return ''
  return new Date(dt).toLocaleString()
})

function isNavActive(path: string) {
  const p = route.path
  if (path === '/blog') return p.startsWith('/blog') || p.startsWith('/article')
  return p === path
}

function go(path: string) {
  router.push(path)
}

function scrollToHeading(id: string) {
  const el = document.getElementById(id)
  if (!el) return
  el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  tocOpen.value = false
}

function backToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

let observer: IntersectionObserver | null = null

async function setupTocObserver() {
  if (observer) observer.disconnect()
  await nextTick()
  const targets = toc.value.map(i => document.getElementById(i.id)).filter(Boolean) as HTMLElement[]
  if (!targets.length) return

  observer = new IntersectionObserver(
    entries => {
      for (const e of entries) {
        if (!e.isIntersecting) continue
        activeTocId.value = (e.target as HTMLElement).id
      }
    },
    { rootMargin: '-15% 0px -75% 0px', threshold: 0 }
  )

  for (const el of targets) observer.observe(el)
}

function onScroll() {
  showBackTop.value = window.scrollY > 700
  const scrollH = document.documentElement.scrollHeight - window.innerHeight
  readProgress.value = scrollH > 0 ? Math.min(100, (window.scrollY / scrollH) * 100) : 0
}

function onToc(items: TocItem[]) {
  toc.value = items
}

async function loadArticle() {
  loading.value = true
  toc.value = []
  activeTocId.value = ''
  relatedPosts.value = []
  try {
    const id = Number(route.params.id)
    article.value = await getPublicArticle(id)
    if (article.value) {
      useSeoMeta({
        title: `${article.value.title} — 维寒一念的小站`,
        description: article.value.summary || article.value.title,
        url: `https://mywebside.vercel.app/article/${article.value.id}`,
        image: article.value.coverUrl || 'https://mywebside.vercel.app/avatar.png',
        type: 'article',
      })
    }
    getArticleLikeStatus(id).then(r => { likeCount.value = r.count; liked.value = r.liked }).catch(() => {})
    if (article.value?.tags?.length) {
      const tagId = article.value.tags[0].id
      const res = await getPublicArticles({ tagId, size: 3 })
      relatedPosts.value = res.items.filter((p) => p.id !== article.value!.id).slice(0, 3)
    }
  } catch {
    article.value = null
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadArticle()
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
  if (observer) observer.disconnect()
})

watch(
  () => toc.value,
  () => setupTocObserver(),
  { deep: true }
)

watch(
  () => route.params.id,
  () => loadArticle()
)
</script>

<template>
  <div
    class="article-page min-h-screen font-sans selection:bg-purple-500/30 selection:text-white relative overflow-x-hidden"
    :class="isDarkMode ? 'article-page--dark text-slate-200' : 'article-page--light text-slate-800'"
  >
    <div class="fixed top-0 left-0 w-full h-[3px] z-[9999] pointer-events-none">
      <div class="h-full bg-gradient-to-r from-blue-400 via-purple-500 to-pink-400 transition-[width] duration-150 ease-out" :style="{ width: readProgress + '%' }"></div>
    </div>

    <div class="fixed top-[-10%] left-[-10%] w-[40vw] h-[40vw] rounded-full bg-blue-500/10 blur-[120px] pointer-events-none"></div>
    <div class="fixed bottom-[-10%] right-[20%] w-[50vw] h-[50vw] rounded-full bg-purple-600/10 blur-[150px] pointer-events-none"></div>

    <div class="max-w-[1400px] mx-auto min-h-screen flex flex-col lg:flex-row relative z-10">
      <aside class="w-full lg:w-[320px] lg:h-screen lg:sticky lg:top-0 p-6 lg:p-10 flex flex-col justify-between border-b lg:border-b-0 lg:border-r article-sidebar z-40">
        <div class="flex flex-col items-center lg:items-start text-center lg:text-left mt-4 lg:mt-10">
          <div class="relative group cursor-pointer mb-6" @click="go('/blog')">
            <div class="absolute inset-0 bg-gradient-to-tr from-[#8be6ff] to-[#a78bfa] rounded-full blur-md opacity-40 group-hover:opacity-70 transition-opacity duration-500"></div>
            <img src="/avatar.png" alt="维寒一念" class="w-28 h-28 rounded-full object-cover border-2 border-white/10 relative z-10 shadow-xl transition-transform duration-500 group-hover:scale-105" />
          </div>

          <h1 class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-[#8be6ff] to-[#a78bfa] mb-2 tracking-wide">维寒一念</h1>
          <p class="text-sm mb-10 font-medium tracking-wide article-sidebar-motto">保持热爱，奔赴山海</p>

          <!-- 【全站统一】侧栏导航：玻璃 pill + 青蓝/粉高亮（摸鱼） -->
          <nav class="flex lg:flex-col gap-3 w-full overflow-x-auto lg:overflow-visible pb-4 lg:pb-0 scrollbar-hide">
            <a
              v-for="item in navItems"
              :key="item.name"
              href="#"
              class="site-pill site-pill--block site-pill--on-dark text-sm whitespace-nowrap flex-shrink-0"
              :class="{ 'site-pill--active': isNavActive(item.path) }"
              @click.prevent="go(item.path)"
            >
              {{ item.name }}
            </a>
          </nav>
        </div>

        <div class="hidden lg:flex gap-5 mt-auto pb-4 px-2">
          <a href="https://github.com/weihanyinian" target="_blank" rel="noopener noreferrer" class="text-slate-400 hover:text-[#8be6ff] transition-colors duration-300 transform hover:-translate-y-1">
            <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path fill-rule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clip-rule="evenodd"></path></svg>
          </a>
          <a href="mailto:1012308753@qq.com" class="text-slate-400 hover:text-[#8be6ff] transition-colors duration-300 transform hover:-translate-y-1">
            <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2zm-8 7L4 8v2l8 5 8-5V8l-8 5z"/></svg>
          </a>
        </div>
      </aside>

      <main class="flex-1 p-6 lg:p-12 xl:p-16 min-h-screen relative pb-44">
        <div class="flex items-center justify-between gap-4 mb-8 flex-wrap">
          <div class="flex items-center gap-2 flex-wrap">
            <button type="button" class="site-pill site-pill--on-dark site-pill--active inline-flex items-center gap-2" @click="go('/blog')">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path></svg>
              返回博客
            </button>
          </div>

          <button v-if="toc.length" type="button" class="site-pill site-pill--on-dark xl:hidden inline-flex items-center gap-2" @click="tocOpen = true">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h10"></path></svg>
            目录
          </button>
        </div>

        <div v-if="loading" class="article-shell article-shell--hero rounded-3xl p-8">
          <div class="h-7 w-2/3 rounded-xl animate-pulse article-skeleton"></div>
          <div class="mt-6 flex flex-wrap gap-3">
            <div class="h-4 w-32 rounded-lg animate-pulse article-skeleton"></div>
            <div class="h-4 w-24 rounded-lg animate-pulse article-skeleton"></div>
            <div class="h-4 w-28 rounded-lg animate-pulse article-skeleton"></div>
          </div>
          <div class="mt-8 h-56 rounded-2xl animate-pulse article-skeleton"></div>
          <div class="mt-8 space-y-4">
            <div class="h-4 w-full rounded-lg animate-pulse article-skeleton"></div>
            <div class="h-4 w-11/12 rounded-lg animate-pulse article-skeleton"></div>
            <div class="h-4 w-10/12 rounded-lg animate-pulse article-skeleton"></div>
          </div>
        </div>

        <div v-else-if="article" class="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_280px] gap-10">
          <article>
            <header class="article-shell article-shell--hero rounded-3xl overflow-hidden">
              <div class="p-8 lg:p-10">
                <h1 class="text-3xl lg:text-4xl font-extrabold tracking-tight leading-snug article-title">{{ article.title }}</h1>

                <div class="mt-5 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm font-medium article-meta">
                  <span v-if="publishedAtText" class="inline-flex items-center gap-2">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                    {{ publishedAtText }}
                  </span>
                  <span class="w-1 h-1 rounded-full bg-slate-600"></span>
                  <span class="inline-flex items-center gap-2">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    {{ readTime }}
                  </span>
                  <span class="w-1 h-1 rounded-full bg-slate-600"></span>
                  <span class="inline-flex items-center gap-2">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 13m0-3v6m0-6H5a2 2 0 00-2 2v2a2 2 0 002 2h10"></path></svg>
                    {{ article.views }} 次阅读
                  </span>
                  <span class="w-1 h-1 rounded-full bg-slate-600"></span>
                  <button class="like-btn inline-flex items-center gap-1" :class="{ 'liked': liked }" @click="toggleLike">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                    <span class="text-sm">{{ likeCount || '' }}</span>
                  </button>
                </div>

                <div v-if="article.tags?.length" class="mt-6 flex flex-wrap gap-2">
                  <button
                    v-for="t in article.tags"
                    :key="t.id"
                    type="button"
                    class="site-pill site-pill--chip site-pill--chip-sm site-pill--on-dark"
                    @click="router.push({ path: '/blog', query: { tagId: t.id } })"
                  >
                    #{{ t.name }}
                  </button>
                </div>
              </div>

              <div v-if="article.coverUrl" class="relative h-64 lg:h-80 w-full overflow-hidden bg-slate-900">
                <img :src="article.coverUrl" alt="cover" class="w-full h-full object-cover" />
                <div class="absolute inset-0 bg-gradient-to-t from-[#111318] via-transparent to-transparent opacity-90"></div>
              </div>
            </header>

            <section class="article-shell article-shell--content mt-10 rounded-3xl p-8 lg:p-10">
              <MarkdownView :content="article.contentMd" @toc="onToc" />
            </section>

            <section v-if="relatedPosts.length" class="mt-10">
              <h3 class="text-lg font-bold mb-4">相关推荐</h3>
              <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                <div v-for="rp in relatedPosts" :key="rp.id" class="article-shell article-shell--content rounded-2xl p-5 cursor-pointer hover:scale-[1.02] transition-transform" @click="router.push(`/article/${rp.id}`)">
                  <h4 class="font-semibold text-sm line-clamp-2 mb-2">{{ rp.title }}</h4>
                  <p class="text-xs text-slate-500 line-clamp-2 mb-3">{{ rp.summary }}</p>
                  <div class="flex items-center gap-2 text-xs text-slate-500">
                    <span>{{ rp.views }} 阅读</span>
                    <span v-if="rp.category">· {{ rp.category.name }}</span>
                  </div>
                </div>
              </div>
            </section>
          </article>

          <aside class="hidden xl:block">
            <div class="sticky top-10">
              <div class="article-shell article-shell--toc rounded-3xl p-6">
                <div class="flex items-center justify-between">
                  <div class="text-sm font-semibold text-white">目录</div>
                  <div class="text-xs text-slate-500">{{ toc.length }}</div>
                </div>
                <div class="mt-4 space-y-1 max-h-[70vh] overflow-auto pr-1 scrollbar-hide">
                  <button
                    v-for="item in toc"
                    :key="item.id"
                    type="button"
                    class="site-pill site-pill--toc site-pill--on-dark"
                    :class="{ 'site-pill--active': activeTocId === item.id }"
                    :style="{ paddingLeft: `${Math.max(0, (item.level - 1) * 12)}px` }"
                    @click="scrollToHeading(item.id)"
                  >
                    {{ item.text }}
                  </button>
                </div>
              </div>
            </div>
          </aside>
        </div>

        <div v-else class="article-shell article-shell--hero rounded-3xl p-10 text-center article-notfound">
          文章可能已被删除或不存在
        </div>
      </main>
    </div>

    <SiteBackToTop />

    <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition duration-150 ease-in" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="tocOpen" class="fixed inset-0 z-[60] xl:hidden">
        <div class="absolute inset-0 bg-black/60" @click="tocOpen = false"></div>
        <div class="absolute right-0 top-0 h-full w-[86vw] max-w-[360px] article-shell--toc-bg border-l border-white/10 backdrop-blur-2xl p-5">
          <div class="flex items-center justify-between">
            <div class="text-sm font-semibold text-white">目录</div>
            <button type="button" class="site-pill site-pill--icon site-pill--on-dark" aria-label="关闭目录" @click="tocOpen = false">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
          </div>
          <div class="mt-4 space-y-1 overflow-auto h-[calc(100%-40px)] pr-1 scrollbar-hide">
            <button
              v-for="item in toc"
              :key="item.id"
              type="button"
              class="site-pill site-pill--toc site-pill--on-dark"
              :class="{ 'site-pill--active': activeTocId === item.id }"
              :style="{ paddingLeft: `${Math.max(0, (item.level - 1) * 12)}px` }"
              @click="scrollToHeading(item.id)"
            >
              {{ item.text }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
/* --- Theme backgrounds --- */
.article-page--dark {
  background: linear-gradient(135deg, #1a1c25 0%, #111318 100%);
}

.article-page--light {
  background: linear-gradient(135deg, #f2f7ff 0%, #e8f2ff 45%, #edf5ff 100%);
}

/* --- Pills: light-mode override --- */
.article-page--light :deep(.site-pill--on-dark) {
  color: #1f2937;
  background: rgba(255, 255, 255, 0.82);
  border-color: rgba(148, 163, 184, 0.48);
}

.article-page--light :deep(.site-pill--on-dark.site-pill--active) {
  color: #fff;
  border-color: transparent;
}

/* --- Article title: responsive color --- */
.article-title {
  color: #fff;
}
.article-page--light .article-title {
  color: #0f172a;
}

/* --- Article meta: responsive color --- */
.article-meta {
  color: #94a3b8;
}
.article-page--light .article-meta {
  color: #64748b;
}

.like-btn {
  background: none; border: none; cursor: pointer; color: #94a3b8; transition: color 0.2s; padding: 0;
}
.like-btn:hover { color: #f472b6; }
.like-btn.liked { color: #f472b6; }
.like-btn.liked svg { fill: #f472b6; }

/* --- Article sidebar: responsive bg --- */
.article-sidebar {
  border-color: rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(24px);
}
.article-page--light .article-sidebar {
  border-color: rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.6);
}

.article-sidebar-motto {
  color: #94a3b8;
}
.article-page--light .article-sidebar-motto {
  color: #475569;
}

/* --- Skeleton: responsive bg --- */
.article-skeleton {
  background: rgba(255, 255, 255, 0.1);
}
.article-page--light .article-skeleton {
  background: rgba(148, 163, 184, 0.25);
}

/* --- Not found text --- */
.article-notfound {
  color: #94a3b8;
}
.article-page--light .article-notfound {
  color: #64748b;
}

/* --- Article shell: glass card base --- */
.article-shell {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(16px);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.16);
}

.article-page--light .article-shell {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.article-shell--hero {
  border-radius: 28px;
}

/* --- Mobile TOC panel --- */
.article-page--light .article-shell--toc-bg {
  background: rgba(255, 255, 255, 0.92);
}
.article-page--dark .article-shell--toc-bg {
  background: rgba(17, 19, 24, 0.8);
}

.article-shell--content {
  line-height: 1.85;
}

.article-shell--toc {
  border-radius: 22px;
}

.article-page--light .article-shell {
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.1);
}
</style>

