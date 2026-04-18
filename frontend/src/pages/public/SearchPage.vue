<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { getPublicArticles, type ArticleListItem } from '../../api/blog'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const q = ref(String(route.query.q ?? ''))
const loading = ref(false)
const items = ref<ArticleListItem[]>([])
const total = ref(0)

async function run() {
  const kw = q.value.trim()
  router.replace({ query: kw ? { q: kw } : {} })
  loading.value = true
  try {
    const page = await getPublicArticles({ keyword: kw || undefined, page: 0, size: 20 })
    items.value = page.items
    total.value = page.total
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void run()
})

watch(
  () => route.query.q,
  () => {
    q.value = String(route.query.q ?? '')
    void run()
  },
)
</script>

<template>
  <div class="search-page card">
    <h1 class="page-title">{{ t('pages.searchTitle') }}</h1>
    <form class="search-bar" @submit.prevent="run">
      <input v-model="q" type="search" class="search-input" :placeholder="t('pages.searchPlaceholder')" />
      <button type="submit" class="site-pill site-pill--active">{{ t('pages.searchBtn') }}</button>
    </form>
    <p v-if="loading" class="muted">{{ t('pages.loading') }}</p>
    <p v-else-if="total > 0" class="muted small">{{ t('pages.searchTotal', { n: total }) }}</p>
    <ul class="results">
      <li v-for="a in items" :key="a.id" class="result-card">
        <RouterLink :to="`/article/${a.id}`" class="result-title">{{ a.title }}</RouterLink>
        <p class="result-sum">{{ a.summary }}</p>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.search-page {
  padding: 28px 24px;
}
.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}
.search-input {
  flex: 1;
  min-width: 200px;
  padding: 12px 16px;
  border-radius: 16px;
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  color: var(--text-color);
  font-size: 0.95rem;
}
.small {
  font-size: 0.85rem;
}
.results {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.result-card {
  padding: 16px 18px;
  border-radius: 16px;
  border: 1px solid var(--glass-border);
  background: rgba(255, 255, 255, 0.08);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
:root[data-theme='dark'] .result-card {
  background: rgba(255, 255, 255, 0.04);
}
.result-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(102, 217, 255, 0.12);
}
.result-title {
  font-weight: 700;
  text-decoration: none;
  color: var(--text-color);
}
.result-title:hover {
  color: var(--primary-color);
}
.result-sum {
  margin: 8px 0 0;
  font-size: 0.88rem;
  opacity: 0.78;
  line-height: 1.5;
}
</style>
