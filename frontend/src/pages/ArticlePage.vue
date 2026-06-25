<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import 'highlight.js/styles/github-dark.css'
import hljs from 'highlight.js'

const route = useRoute()
const article = ref<any>(null)
const loading = ref(true)

marked.setOptions({
  highlight(code: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  }
})

onMounted(async () => {
  try {
    const res = await fetch(`/api/articles/${route.params.id}`)
    const data = await res.json()
    if (data.code === 200) {
      article.value = data.data
    }
  } catch (e) {
    // Backend not available
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
      </div>

      <!-- Content -->
      <div class="glass-card p-8 mb-8 markdown-content" v-html="marked.parse(article.content || '')" />

      <!-- Navigation -->
      <div class="flex justify-between">
        <button @click="$router.push('/blog')" class="glass-button text-sm">
          ← 返回博客
        </button>
      </div>
    </div>
  </div>
</template>
