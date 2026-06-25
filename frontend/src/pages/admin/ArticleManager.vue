<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { blogApi, Article } from '../../api/blog'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()
const articles = ref<Article[]>([])
const loading = ref(true)
const editing = ref<Partial<Article> | null>(null)
const isNew = ref(false)

const form = ref({
  title: '',
  summary: '',
  content: '',
  status: 'DRAFT' as 'DRAFT' | 'PUBLISHED',
  coverImage: '',
  categoryId: null as number | null
})

onMounted(() => loadArticles())

async function loadArticles() {
  try {
    const res = await blogApi.getAll()
    articles.value = res.data.data.content
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function startNew() {
  isNew.value = true
  editing.value = {}
  form.value = { title: '', summary: '', content: '', status: 'DRAFT', coverImage: '', categoryId: null }
}

function startEdit(article: Article) {
  isNew.value = false
  editing.value = article
  form.value = {
    title: article.title,
    summary: article.summary || '',
    content: article.content,
    status: article.status,
    coverImage: article.coverImage || '',
    categoryId: article.category?.id || null
  }
}

async function save() {
  if (!form.value.title || !form.value.content) return
  try {
    const payload: any = { ...form.value }
    if (form.value.categoryId) {
      payload.category = { id: form.value.categoryId }
    }
    if (isNew.value) {
      await blogApi.create(payload)
    } else if (editing.value?.id) {
      await blogApi.update(editing.value.id, payload)
    }
    editing.value = null
    await loadArticles()
  } catch (e: any) {
    alert(e.response?.data?.message || '保存失败')
  }
}

async function deleteArticle(id: number) {
  if (!confirm('确认删除？')) return
  try {
    await blogApi.delete(id)
    await loadArticles()
  } catch (e) {
    alert('删除失败')
  }
}
</script>

<template>
  <div class="min-h-screen px-4 py-10 max-w-6xl mx-auto">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold gradient-text">📝 文章管理</h1>
      <div class="flex gap-3">
        <button @click="router.push('/admin')" class="glass-button text-sm">← 后台</button>
        <button @click="startNew" class="glass-button primary text-sm">+ 新建文章</button>
      </div>
    </div>

    <!-- Editor -->
    <GlassCard v-if="editing" class="!p-6 mb-8">
      <h2 class="text-lg font-bold mb-4">{{ isNew ? '新建文章' : '编辑文章' }}</h2>
      <div class="space-y-4">
        <input v-model="form.title" class="glass-input w-full" placeholder="文章标题" />
        <input v-model="form.summary" class="glass-input w-full" placeholder="摘要（可选）" />
        <textarea v-model="form.content" class="glass-input w-full min-h-[300px]" placeholder="Markdown 内容"></textarea>
        <div class="flex gap-4 items-center">
          <select v-model="form.status" class="glass-input">
            <option value="DRAFT">草稿</option>
            <option value="PUBLISHED">发布</option>
          </select>
          <div class="flex gap-2 ml-auto">
            <button @click="editing = null" class="glass-button text-sm">取消</button>
            <button @click="save" class="glass-button primary text-sm">保存</button>
          </div>
        </div>
      </div>
    </GlassCard>

    <!-- Article List -->
    <div v-if="loading" class="text-center py-10 text-[var(--text-muted)]">加载中...</div>
    <div v-else class="space-y-4">
      <GlassCard v-for="a in articles" :key="a.id" class="!p-5 flex justify-between items-center">
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-3 mb-1">
            <span class="font-medium truncate text-[var(--text-primary)]">{{ a.title }}</span>
            <span :class="['text-xs px-2 py-0.5 rounded', a.status === 'PUBLISHED' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400']">
              {{ a.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </span>
          </div>
          <div class="text-xs text-[var(--text-muted)]">
            {{ a.createdAt?.slice(0, 10) }} · {{ a.viewCount }} 阅读
            <span v-if="a.category"> · {{ a.category.name }}</span>
          </div>
        </div>
        <div class="flex gap-2 ml-4 shrink-0">
          <button @click="startEdit(a)" class="glass-button text-sm">编辑</button>
          <button @click="deleteArticle(a.id)" class="glass-button text-sm !border-red-500/30 !text-red-400">删除</button>
        </div>
      </GlassCard>
    </div>
  </div>
</template>
