<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchPortfolioWorkDetail, type PortfolioWorkDetail } from '../../api/portfolioApi'
import MarkdownView from '../../components/MarkdownView.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const item = ref<PortfolioWorkDetail | null>(null)

const workId = computed(() => Number(route.params.id))

async function load() {
  const id = workId.value
  if (!Number.isFinite(id) || id <= 0) {
    error.value = '作品 ID 不合法'
    item.value = null
    return
  }
  loading.value = true
  error.value = ''
  try {
    item.value = await fetchPortfolioWorkDetail(id)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '加载作品详情失败'
    item.value = null
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<template>
  <main class="works-detail">
    <section class="works-detail__hero panel-card">
      <button type="button" class="site-pill site-pill--secondary" @click="router.push('/works-showcase')">
        返回作品全景页
      </button>
      <h1 class="works-detail__title">{{ item?.title || '作品详情' }}</h1>
      <p class="works-detail__subtitle">{{ item?.desc || '查看完整项目说明与技术细节。' }}</p>
    </section>

    <section v-if="loading" class="works-detail__state panel-card">加载中...</section>
    <section v-else-if="error" class="works-detail__state panel-card works-detail__state--error">{{ error }}</section>
    <section v-else-if="item" class="works-detail__content">
      <article class="works-detail__main content-card">
        <img :src="item.cover" :alt="item.title" class="works-detail__cover" loading="lazy" decoding="async" />
        <div class="works-detail__summary">
          <span class="site-pill site-pill--chip">{{ item.tag }}</span>
          <p class="works-detail__lead">{{ item.detail }}</p>
        </div>
        <div class="works-detail__markdown">
          <MarkdownView :content="item.contentMd" />
        </div>
      </article>

      <aside class="works-detail__side panel-card">
        <h2>项目信息</h2>
        <p v-if="item.techStack" class="works-detail__meta"><strong>技术栈：</strong>{{ item.techStack }}</p>
        <p class="works-detail__meta"><strong>主链接：</strong><a :href="item.link" target="_blank" rel="noopener noreferrer">{{ item.link }}</a></p>
        <p v-if="item.demoUrl" class="works-detail__meta"><strong>Demo：</strong><a :href="item.demoUrl" target="_blank" rel="noopener noreferrer">{{ item.demoUrl }}</a></p>
        <p v-if="item.repoUrl" class="works-detail__meta"><strong>仓库：</strong><a :href="item.repoUrl" target="_blank" rel="noopener noreferrer">{{ item.repoUrl }}</a></p>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.works-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 88px 20px 44px;
}

.works-detail__hero {
  padding: 22px 24px;
  margin-bottom: 18px;
}

.works-detail__title {
  margin: 12px 0 8px;
  font-size: clamp(1.5rem, 3vw, 2.1rem);
}

.works-detail__subtitle {
  margin: 0;
  color: var(--text-muted);
}

.works-detail__state {
  padding: 24px;
}

.works-detail__state--error {
  color: #dc2626;
}

.works-detail__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}

.works-detail__main {
  padding: 0;
  overflow: hidden;
}

.works-detail__cover {
  width: 100%;
  max-height: 380px;
  object-fit: cover;
  display: block;
}

.works-detail__summary {
  padding: 18px 18px 12px;
}

.works-detail__lead {
  margin: 12px 0 0;
  line-height: 1.72;
}

.works-detail__markdown {
  padding: 0 18px 18px;
}

.works-detail__side {
  padding: 16px;
  height: fit-content;
  position: sticky;
  top: 92px;
}

.works-detail__side h2 {
  margin: 0 0 12px;
  font-size: 1rem;
}

.works-detail__meta {
  margin: 0 0 10px;
  font-size: 0.9rem;
  line-height: 1.58;
  word-break: break-word;
}

.works-detail__meta a {
  color: var(--primary-color);
  text-decoration: none;
}

@media (max-width: 980px) {
  .works-detail__content {
    grid-template-columns: 1fr;
  }

  .works-detail__side {
    position: static;
  }
}
</style>
