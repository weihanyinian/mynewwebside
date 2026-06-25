<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { blogApi, Article } from '../api/blog'
import GlassCard from '../components/GlassCard.vue'
import MarkdownRenderer from '../components/MarkdownRenderer.vue'

const route = useRoute()
const article = ref<Article | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await blogApi.getById(Number(route.params.id))
    if (res.data.code === 200) {
      article.value = res.data.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container max-w-3xl">
    <div v-if="loading" class="text-center py-12 text-[var(--text-secondary)]">加载中...</div>

    <div v-else-if="!article" class="glass-card p-12 text-center">
      <p class="text-4xl mb-4">😢</p>
      <p class="text-[var(--text-secondary)]">文章不存在或已被删除</p>
    </div>

    <div v-else class="animate-fade-in-up">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold mb-3 gradient-text">{{ article.title }}</h1>
        <div class="flex items-center gap-3 text-sm text-[var(--text-secondary)]">
          <span>{{ article.category?.name || '未分类' }}</span>
          <span>·</span>
          <span>{{ new Date(article.createdAt).toLocaleDateString('zh-CN') }}</span>
          <span>·</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>
        <div v-if="article.tags?.length" class="flex gap-2 mt-3">
          <span v-for="tag in article.tags" :key="tag.id" class="text-xs glass-button !py-0.5 !px-2 !rounded-full">
            {{ tag.name }}
          </span>
        </div>
      </div>

      <!-- Content -->
      <GlassCard class="!p-6 md:!p-8 mb-8">
        <MarkdownRenderer :content="article.content || ''" />
      </GlassCard>

      <!-- Navigation -->
      <div class="flex justify-between">
        <button @click="$router.push('/blog')" class="glass-button text-sm">← 返回博客</button>
      </div>
    </div>
  </div>
</template>
