<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicArticle, type ArticleDetail } from '../../api/blog'
import MarkdownView from '../../components/MarkdownView.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref<ArticleDetail | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    article.value = await getPublicArticle(id)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <div v-if="article" class="card article">
      <div class="article__head">
        <div class="article__title">{{ article.title }}</div>
        <div class="article__meta muted">
          <span v-if="article.category">分类：{{ article.category.name }}</span>
          <span v-if="article.publishedAt"> · {{ new Date(article.publishedAt).toLocaleString() }}</span>
          <span> · 浏览 {{ article.views }}</span>
        </div>
        <div class="article__tags">
          <el-tag
            v-for="t in article.tags"
            :key="t.id"
            size="small"
            effect="plain"
            @click="router.push({ path: '/blog', query: { tagId: t.id } })"
          >
            {{ t.name }}
          </el-tag>
        </div>
      </div>

      <div v-if="article.coverUrl" class="article__cover">
        <img :src="article.coverUrl" alt="cover" />
      </div>

      <div class="article__body">
        <MarkdownView :content="article.contentMd" />
      </div>
    </div>

    <el-empty v-else-if="!loading" description="文章不存在" />
  </div>
</template>

<style scoped>
.article {
  padding: 18px 18px 22px;
}

.article__title {
  font-size: 28px;
  font-weight: 900;
  line-height: 1.2;
}

.article__meta {
  margin-top: 10px;
  font-size: 13px;
}

.article__tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.article__cover {
  margin-top: 16px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.article__cover img {
  width: 100%;
  display: block;
}

.article__body {
  margin-top: 18px;
  line-height: 1.75;
  font-size: 15px;
}

.article__body :deep(h1),
.article__body :deep(h2),
.article__body :deep(h3) {
  margin: 20px 0 10px;
  line-height: 1.25;
}

.article__body :deep(p) {
  margin: 10px 0;
}

.article__body :deep(a) {
  color: #2563eb;
}

.article__body :deep(blockquote) {
  margin: 12px 0;
  padding: 10px 12px;
  border-left: 4px solid rgba(0, 0, 0, 0.15);
  background: rgba(0, 0, 0, 0.03);
  border-radius: 10px;
}
</style>

