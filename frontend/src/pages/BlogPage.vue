<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()
const articles = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await fetch('/api/articles?page=0&size=20')
    const data = await res.json()
    if (data.code === 200) {
      articles.value = data.data?.content || []
    }
  } catch (e) {
    // Backend not available
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-8 gradient-text">📝 博客</h1>

    <div v-if="loading" class="text-center py-12">
      <p class="text-[var(--text-secondary)]">加载中...</p>
    </div>

    <div v-else-if="articles.length === 0" class="glass-card p-12 text-center">
      <p class="text-5xl mb-4">📝</p>
      <p class="text-[var(--text-secondary)]">还没有文章，博主正在努力写作中...</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <GlassCard v-for="article in articles" :key="article.id" class="cursor-pointer" @click="router.push(`/blog/${article.id}`)">
        <div v-if="article.coverImage" class="mb-3 h-40 rounded-lg overflow-hidden bg-gradient-to-br from-[#62a7ea33] to-[#a58eea33]">
          <img :src="article.coverImage" :alt="article.title" class="w-full h-full object-cover" />
        </div>
        <div class="flex items-center gap-2 text-xs text-[var(--text-secondary)] mb-2">
          <span>{{ article.category?.name || '未分类' }}</span>
          <span>·</span>
          <span>{{ new Date(article.createdAt).toLocaleDateString('zh-CN') }}</span>
          <span>·</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>
        <h3 class="font-bold text-lg mb-1">{{ article.title }}</h3>
        <p class="text-sm text-[var(--text-secondary)] line-clamp-2">{{ article.summary }}</p>
      </GlassCard>
    </div>
  </div>
</template>
