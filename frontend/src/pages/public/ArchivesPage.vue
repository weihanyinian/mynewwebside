<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { RouterLink } from 'vue-router'
import { getPublicArticles, type ArticleListItem } from '../../api/blog'

const { t } = useI18n()
const loading = ref(true)
const err = ref('')
const items = ref<ArticleListItem[]>([])

const byYear = computed(() => {
  const m = new Map<number, ArticleListItem[]>()
  for (const a of items.value) {
    const y = a.publishedAt ? new Date(a.publishedAt).getFullYear() : 0
    if (!m.has(y)) m.set(y, [])
    m.get(y)!.push(a)
  }
  return [...m.entries()].sort((a, b) => b[0] - a[0])
})

onMounted(async () => {
  loading.value = true
  err.value = ''
  try {
    const page = await getPublicArticles({ page: 0, size: 500 })
    items.value = page.items
  } catch (e) {
    err.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="archives-page card">
    <h1 class="page-title">{{ t('pages.archivesTitle') }}</h1>
    <p v-if="loading" class="muted">{{ t('pages.loading') }}</p>
    <p v-else-if="err" class="text-red-400">{{ err }}</p>
    <p v-else-if="!items.length" class="muted">{{ t('pages.archivesEmpty') }}</p>
    <div v-else class="year-blocks">
      <section v-for="[year, posts] in byYear" :key="year" class="year-block">
        <h2 class="year-title">{{ year || '—' }}</h2>
        <ul class="post-list">
          <li v-for="p in posts" :key="p.id">
            <RouterLink :to="`/article/${p.id}`" class="post-link">{{ p.title }}</RouterLink>
            <span class="post-date">{{ p.publishedAt?.replace('T', ' ').slice(0, 10) }}</span>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>

<style scoped>
.archives-page {
  padding: 28px 24px;
}
.year-blocks {
  display: flex;
  flex-direction: column;
  gap: 28px;
}
.year-title {
  font-size: 1.35rem;
  margin: 0 0 12px;
  color: var(--primary-color);
}
.post-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.post-list li {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--glass-border);
}
:root[data-theme='dark'] .post-list li {
  background: rgba(255, 255, 255, 0.04);
}
.post-link {
  text-decoration: none;
  font-weight: 600;
  color: var(--text-color);
  transition: color 0.2s ease;
}
.post-link:hover {
  color: var(--primary-color);
}
.post-date {
  font-size: 0.78rem;
  opacity: 0.65;
  flex-shrink: 0;
}
</style>
