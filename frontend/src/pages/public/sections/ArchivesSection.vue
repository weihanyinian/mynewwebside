<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPublicArticles, type ArticleListItem } from '../../../api/blog'

const { t } = useI18n()

const loading = ref(false)
const items = ref<ArticleListItem[]>([])
const sectionEl = ref<HTMLElement | null>(null)
const fetchStarted = ref(false)
let observer: IntersectionObserver | null = null

const byYear = computed(() => {
  const m = new Map<number, ArticleListItem[]>()
  for (const a of items.value) {
    const y = a.publishedAt ? new Date(a.publishedAt).getFullYear() : 0
    if (!m.has(y)) m.set(y, [])
    m.get(y)!.push(a)
  }
  return [...m.entries()].sort((a, b) => b[0] - a[0])
})

async function loadWhenVisible() {
  if (fetchStarted.value) return
  fetchStarted.value = true
  loading.value = true
  items.value = []
  try {
    let page = 0
    const size = 60
    while (true) {
      const res = await getPublicArticles({ page, size })
      items.value.push(...res.items)
      if (res.items.length < size || items.value.length >= res.total) break
      page += 1
    }
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  observer = new IntersectionObserver(
    (entries) => {
      if (entries.some((e) => e.isIntersecting)) {
        void loadWhenVisible()
      }
    },
    { root: null, rootMargin: '140px 0px', threshold: 0 },
  )
  void nextTick(() => {
    if (sectionEl.value) observer?.observe(sectionEl.value)
  })
})

onUnmounted(() => {
  observer?.disconnect()
  observer = null
})
</script>

<template>
  <section ref="sectionEl" class="section" id="archives">
    <h2>{{ t('pages.archivesTitle') }}</h2>
    <p v-if="loading" class="muted center">{{ t('pages.loading') }}</p>
    <p v-else-if="!items.length" class="muted center">{{ t('pages.archivesEmpty') }}</p>
    <div v-else class="home-arch-blocks">
      <div v-for="[year, posts] in byYear" :key="year" class="glass-card home-arch-year">
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
</template>

<style scoped>
.center { text-align: center; }
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
</style>
