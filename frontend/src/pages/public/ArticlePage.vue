<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicArticle, type ArticleDetail } from '../../api/blog'
import MarkdownView from '../../components/MarkdownView.vue'
import BackToHomeButton from '../../components/BackToHomeButton.vue'

const route = useRoute()
const router = useRouter()

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

const navItems = [
  { name: '首页', path: '/' },
  { name: '博客', path: '/blog' },
  { name: '作品', path: '/#works' },
  { name: '留言墙', path: '/message' },
  { name: '摸鱼', path: '/moyu' }
]

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
}

function onToc(items: TocItem[]) {
  toc.value = items
}

async function loadArticle() {
  loading.value = true
  toc.value = []
  activeTocId.value = ''
  try {
    const id = Number(route.params.id)
    article.value = await getPublicArticle(id)
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
  <div class="min-h-screen bg-gradient-to-br from-[#1a1c25] to-[#111318] text-slate-200 font-sans selection:bg-purple-500/30 selection:text-white relative overflow-x-hidden">
    <div class="fixed top-[-10%] left-[-10%] w-[40vw] h-[40vw] rounded-full bg-blue-500/10 blur-[120px] pointer-events-none"></div>
    <div class="fixed bottom-[-10%] right-[20%] w-[50vw] h-[50vw] rounded-full bg-purple-600/10 blur-[150px] pointer-events-none"></div>

    <div class="max-w-[1400px] mx-auto min-h-screen flex flex-col lg:flex-row relative z-10">
      <aside class="w-full lg:w-[320px] lg:h-screen lg:sticky lg:top-0 p-6 lg:p-10 flex flex-col justify-between border-b lg:border-b-0 lg:border-r border-white/5 bg-white/[0.02] backdrop-blur-xl z-40">
        <div class="flex flex-col items-center lg:items-start text-center lg:text-left mt-4 lg:mt-10">
          <div class="relative group cursor-pointer mb-6" @click="go('/blog')">
            <div class="absolute inset-0 bg-gradient-to-tr from-[#8be6ff] to-[#a78bfa] rounded-full blur-md opacity-40 group-hover:opacity-70 transition-opacity duration-500"></div>
            <img src="../../assets/images/about-miku.jpg" alt="维寒一念" class="w-28 h-28 rounded-full object-cover border-2 border-white/10 relative z-10 shadow-xl transition-transform duration-500 group-hover:scale-105" />
          </div>

          <h1 class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-[#8be6ff] to-[#a78bfa] mb-2 tracking-wide">维寒一念</h1>
          <p class="text-sm text-slate-400 mb-10 font-medium tracking-wide">保持热爱，奔赴山海</p>

          <!-- 【全站统一】侧栏导航：玻璃 pill + 青蓝/粉高亮（摸鱼） -->
          <nav class="flex lg:flex-col gap-3 w-full overflow-x-auto lg:overflow-visible pb-4 lg:pb-0 scrollbar-hide">
            <a
              v-for="item in navItems"
              :key="item.name"
              href="#"
              class="site-pill site-pill--block site-pill--on-dark text-sm whitespace-nowrap flex-shrink-0"
              :class="{
                'site-pill--active': isNavActive(item.path) && item.path !== '/moyu',
                'site-pill--pink': isNavActive(item.path) && item.path === '/moyu',
              }"
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
            <BackToHomeButton variant="on-dark" />
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

        <div v-if="loading" class="bg-white/[0.03] border border-white/10 rounded-3xl backdrop-blur-lg p-8">
          <div class="h-7 w-2/3 bg-white/10 rounded-xl animate-pulse"></div>
          <div class="mt-6 flex flex-wrap gap-3">
            <div class="h-4 w-32 bg-white/10 rounded-lg animate-pulse"></div>
            <div class="h-4 w-24 bg-white/10 rounded-lg animate-pulse"></div>
            <div class="h-4 w-28 bg-white/10 rounded-lg animate-pulse"></div>
          </div>
          <div class="mt-8 h-56 bg-white/10 rounded-2xl animate-pulse"></div>
          <div class="mt-8 space-y-4">
            <div class="h-4 w-full bg-white/10 rounded-lg animate-pulse"></div>
            <div class="h-4 w-11/12 bg-white/10 rounded-lg animate-pulse"></div>
            <div class="h-4 w-10/12 bg-white/10 rounded-lg animate-pulse"></div>
          </div>
        </div>

        <div v-else-if="article" class="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_280px] gap-10">
          <article>
            <header class="bg-white/[0.03] border border-white/10 rounded-3xl overflow-hidden backdrop-blur-lg">
              <div class="p-8 lg:p-10">
                <h1 class="text-3xl lg:text-4xl font-extrabold text-white tracking-tight leading-snug">{{ article.title }}</h1>

                <div class="mt-5 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-slate-400 font-medium">
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

            <section class="mt-10 bg-white/[0.03] border border-white/10 rounded-3xl backdrop-blur-lg p-8 lg:p-10">
              <MarkdownView :content="article.contentMd" @toc="onToc" />
            </section>
          </article>

          <aside class="hidden xl:block">
            <div class="sticky top-10">
              <div class="bg-white/[0.03] border border-white/10 rounded-3xl backdrop-blur-lg p-6">
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

        <div v-else class="bg-white/[0.03] border border-white/10 rounded-3xl backdrop-blur-lg p-10 text-center text-slate-400">
          文章可能已被删除或不存在
        </div>
      </main>
    </div>

    <transition enter-active-class="transition duration-200 ease-out" enter-from-class="opacity-0 translate-y-2" enter-to-class="opacity-100 translate-y-0" leave-active-class="transition duration-150 ease-in" leave-from-class="opacity-100 translate-y-0" leave-to-class="opacity-0 translate-y-2">
      <button
        v-if="showBackTop"
        type="button"
        class="site-pill site-pill--active fixed bottom-8 right-24 md:right-28 z-50 inline-flex items-center gap-2"
        @click="backToTop"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path></svg>
        <span class="text-sm font-semibold">顶部</span>
      </button>
    </transition>

    <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100" leave-active-class="transition duration-150 ease-in" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="tocOpen" class="fixed inset-0 z-[60] xl:hidden">
        <div class="absolute inset-0 bg-black/60" @click="tocOpen = false"></div>
        <div class="absolute right-0 top-0 h-full w-[86vw] max-w-[360px] bg-[#111318]/80 border-l border-white/10 backdrop-blur-2xl p-5">
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

