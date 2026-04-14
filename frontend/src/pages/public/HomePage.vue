<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getPublicArticles, getTags, type ArticleListItem, type Category, type Tag } from '../../api/blog'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const keyword = ref<string>((route.query.keyword as string) || '')
const categoryId = ref<number | undefined>(
  route.query.categoryId ? Number(route.query.categoryId) : undefined,
)
const tagId = ref<number | undefined>(route.query.tagId ? Number(route.query.tagId) : undefined)

const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])

const items = ref<ArticleListItem[]>([])
const total = ref(0)
const page = ref(0)
const size = ref(10)
const loading = ref(false)

function syncToRoute() {
  const q: Record<string, any> = {}
  if (keyword.value) q.keyword = keyword.value
  if (categoryId.value) q.categoryId = String(categoryId.value)
  if (tagId.value) q.tagId = String(tagId.value)
  router.push({ path: '/blog', query: q })
}

async function load() {
  loading.value = true
  try {
    const res = await getPublicArticles({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      tagId: tagId.value,
      page: page.value,
      size: size.value,
    })
    items.value = res.items
    total.value = res.total
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query,
  () => {
    keyword.value = (route.query.keyword as string) || ''
    categoryId.value = route.query.categoryId ? Number(route.query.categoryId) : undefined
    tagId.value = route.query.tagId ? Number(route.query.tagId) : undefined
    page.value = 0
    load()
  },
)

onMounted(async () => {
  categories.value = await getCategories(true)
  tags.value = await getTags(true)
  await load()
})
</script>

<template>
  <div class="home">
    <div class="card home-filters">
      <div class="home-filters__row">
        <el-input v-model="keyword" placeholder="搜索文章（标题/内容）..." clearable @keyup.enter="syncToRoute">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="categoryId" clearable placeholder="选择分类" @change="syncToRoute">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="tagId" clearable placeholder="选择标签" @change="syncToRoute">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <button class="filter-btn" @click="syncToRoute">搜索</button>
      </div>
    </div>

    <div class="home-list" v-loading="loading">
      <div v-for="a in items" :key="a.id" class="card post" @click="router.push(`/article/${a.id}`)">
        <div class="post__cover">
          <!-- Placeholder cover using gradient since we don't have actual covers -->
          <div class="cover-placeholder"></div>
        </div>
        <div class="post__body">
          <div class="post__title">{{ a.title }}</div>
          <div class="post__summary muted">{{ a.summary }}</div>
          <div class="post__meta muted">
            <span v-if="a.category" class="meta-item category-meta">分类：{{ a.category.name }}</span>
            <span v-if="a.publishedAt" class="meta-item">📅 {{ new Date(a.publishedAt).toLocaleString() }}</span>
            <span class="meta-item">👁️ {{ a.views }} 次围观</span>
          </div>
          <div class="post__tags">
            <span v-for="t in a.tags" :key="t.id" class="anime-tag" @click.stop="router.push({ path: '/blog', query: { tagId: t.id } })">
              #{{ t.name }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="!loading && items.length === 0" class="empty-state">
        <div class="empty-illustration">
          <!-- Anime style empty state SVG -->
          <svg viewBox="0 0 200 200" width="160" height="160" xmlns="http://www.w3.org/2000/svg">
            <path fill="rgba(74, 144, 226, 0.2)" d="M42.7,-73.4C56.6,-66.1,70.1,-55.8,78.8,-42.1C87.5,-28.4,91.3,-14.2,90.4,-0.5C89.5,13.2,83.8,26.4,75.1,38.1C66.4,49.8,54.7,60.1,41.4,66.6C28.1,73.1,14,75.9,-0.6,76.9C-15.2,78,-30.4,77.3,-43.3,70.5C-56.2,63.7,-66.8,50.8,-73.8,36.5C-80.8,22.2,-84.2,6.5,-81.4,-8.2C-78.6,-22.9,-69.6,-36.6,-57.6,-45.5C-45.6,-54.4,-30.6,-58.5,-17.1,-63.4C-3.6,-68.3,8.8,-74.3,21.5,-76C34.2,-77.7,46.9,-75.1,42.7,-73.4Z" transform="translate(100 100)" />
            <text x="100" y="100" font-size="40" text-anchor="middle" dominant-baseline="central" fill="rgba(74, 144, 226, 0.5)">🍃</text>
          </svg>
        </div>
        <p class="empty-text">这里还没有任何文章...</p>
      </div>

      <div class="home-pager" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="page + 1"
          :page-size="size"
          :total="total"
          @current-change="(p: number) => { page = p - 1; load() }"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.home-filters {
  padding: 20px;
  margin-bottom: 24px;
}

.home-filters__row {
  display: grid;
  grid-template-columns: 1fr 180px 180px auto;
  gap: 16px;
}

/* Override Element Plus input styles for glassmorphism */
:deep(.el-input__wrapper), :deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px;
}
:deep(.el-input__wrapper.is-focus), :deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.filter-btn {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 24px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.filter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
}

.home-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.post {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  padding: 0;
}

.post:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 32px rgba(74, 144, 226, 0.2);
}

.post__cover {
  height: 160px;
  width: 100%;
  overflow: hidden;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, rgba(74, 144, 226, 0.2), rgba(80, 227, 194, 0.2));
  position: relative;
}
.cover-placeholder::after {
  content: "✧";
  font-size: 2rem;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: rgba(255,255,255,0.8);
}

.post__body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.post__title {
  font-weight: 800;
  font-size: 1.2rem;
  line-height: 1.4;
  color: var(--text-color);
  margin-bottom: 8px;
  transition: color 0.3s;
}

.post:hover .post__title {
  color: var(--primary-color);
}

.post__summary {
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 16px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 0.85rem;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.category-meta {
  color: var(--primary-color);
  font-weight: 600;
}

.post__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.anime-tag {
  font-size: 0.75rem;
  padding: 4px 10px;
  background: rgba(74, 144, 226, 0.1);
  color: var(--primary-color);
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s;
}
.anime-tag:hover {
  background: var(--primary-color);
  color: white;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.empty-text {
  margin-top: 16px;
  color: rgba(44, 62, 80, 0.6);
  font-weight: 500;
  font-size: 1.1rem;
}

.home-pager {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  padding: 30px 0;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--primary-color);
}

@media (max-width: 780px) {
  .home-filters__row {
    grid-template-columns: 1fr;
  }
  .home-list {
    grid-template-columns: 1fr;
  }
}
</style>

