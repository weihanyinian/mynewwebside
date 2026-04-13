<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getPublicArticles, getTags, type ArticleListItem, type Category, type Tag } from '../../api/blog'

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
  router.push({ path: '/', query: q })
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
        <el-input v-model="keyword" placeholder="关键字（标题/摘要/正文）" clearable @keyup.enter="syncToRoute" />
        <el-select v-model="categoryId" clearable placeholder="分类" @change="syncToRoute">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="tagId" clearable placeholder="标签" @change="syncToRoute">
          <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-button type="primary" @click="syncToRoute">筛选</el-button>
      </div>
    </div>

    <div class="home-list" v-loading="loading">
      <div v-for="a in items" :key="a.id" class="card post" @click="router.push(`/article/${a.id}`)">
        <div class="post__body">
          <div class="post__title">{{ a.title }}</div>
          <div class="post__summary muted">{{ a.summary }}</div>
          <div class="post__meta muted">
            <span v-if="a.category">分类：{{ a.category.name }}</span>
            <span v-if="a.publishedAt"> · {{ new Date(a.publishedAt).toLocaleString() }}</span>
            <span> · 浏览 {{ a.views }}</span>
          </div>
          <div class="post__tags">
            <el-tag v-for="t in a.tags" :key="t.id" size="small" effect="plain" @click.stop="router.push({ path: '/', query: { tagId: t.id } })">
              {{ t.name }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && items.length === 0" description="暂无文章" />

      <div class="home-pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
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
.home-filters {
  padding: 14px;
}

.home-filters__row {
  display: grid;
  grid-template-columns: 1fr 160px 160px auto;
  gap: 12px;
}

.home-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.post {
  padding: 16px;
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.1s ease;
}

.post:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(0, 0, 0, 0.06);
}

.post__title {
  font-weight: 800;
  font-size: 18px;
  line-height: 1.25;
}

.post__summary {
  margin-top: 8px;
  line-height: 1.6;
}

.post__meta {
  margin-top: 10px;
  font-size: 13px;
}

.post__tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.home-pager {
  display: flex;
  justify-content: center;
  padding: 10px 0 0;
}

@media (max-width: 780px) {
  .home-filters__row {
    grid-template-columns: 1fr;
  }
}
</style>

