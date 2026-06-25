<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { blogApi, Article } from '../api/blog'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()
const articles = ref<Article[]>([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(0)

async function loadArticles(p = 0) {
  loading.value = true
  page.value = p
  try {
    const res = await blogApi.getPublished(p, 12)
    if (res.data.code === 200) {
      articles.value = res.data.data.content || []
      totalPages.value = res.data.data.totalPages
    }
  } catch (e) {
    articles.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => loadArticles())
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-2 gradient-text">📝 博客</h1>
    <p class="text-sm text-[var(--text-muted)] mb-8">技术笔记 · 生活随笔 · 个人思考</p>

    <div v-if="loading" class="text-center py-12">
      <p class="text-[var(--text-secondary)]">加载中...</p>
    </div>

    <div v-else-if="articles.length === 0" class="glass-card p-12 text-center">
      <p class="text-5xl mb-4">📝</p>
      <p class="text-[var(--text-secondary)]">还没有文章，博主正在努力写作中...</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <GlassCard v-for="article in articles" :key="article.id" class="cursor-pointer" @click="router.push(`/blog/${article.id}`)">
        <!-- Cover -->
        <div class="mb-3 h-36 rounded-lg overflow-hidden bg-gradient-to-br from-[#62a7ea33] to-[#a58eea33] flex items-center justify-center text-4xl">
          <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" />
          <span v-else>{{ article.category?.slug === 'tech' ? '💻' : article.category?.slug === 'life' ? '🌟' : '📝' }}</span>
        </div>
        <!-- Meta -->
        <div class="flex items-center gap-2 text-xs text-[var(--text-muted)] mb-2">
          <span class="glass-button !py-0.5 !px-2 !text-[10px] !rounded-full">{{ article.category?.name || '未分类' }}</span>
          <span>{{ new Date(article.createdAt).toLocaleDateString('zh-CN') }}</span>
        </div>
        <!-- Title -->
        <h3 class="font-bold text-base mb-1 text-[var(--text-primary)]">{{ article.title }}</h3>
        <p class="text-sm text-[var(--text-muted)] line-clamp-2 leading-relaxed">{{ article.summary }}</p>
        <!-- Tags -->
        <div v-if="article.tags?.length" class="flex gap-1.5 mt-3">
          <span v-for="tag in article.tags.slice(0, 4)" :key="tag.id" class="text-[10px] text-[var(--text-muted)]">#{{ tag.name }}</span>
        </div>
      </GlassCard>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex justify-center gap-3 mt-10">
      <button :disabled="page === 0" @click="loadArticles(page - 1)" class="glass-button text-sm" :class="{ 'opacity-40': page === 0 }">← 上一页</button>
      <span class="text-sm text-[var(--text-muted)] self-center">{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="loadArticles(page + 1)" class="glass-button text-sm" :class="{ 'opacity-40': page >= totalPages - 1 }">下一页 →</button>
    </div>
  </div>
</template>
