<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  adminCreateArticle,
  adminGetArticle,
  adminUpdateArticle,
  getCategories,
  getTags,
  type ArticleUpsert,
  type Category,
  type Tag,
} from '../../api/blog'

const route = useRoute()
const router = useRouter()

const id = computed(() => (route.params.id ? Number(route.params.id) : 0))
const isEdit = computed(() => id.value > 0)

const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const loading = ref(false)

const form = reactive<ArticleUpsert>({
  title: '',
  summary: '',
  contentMd: '',
  coverUrl: '',
  status: 'DRAFT',
  categoryId: null,
  tagIds: [],
})

async function loadMeta() {
  categories.value = await getCategories(false)
  tags.value = await getTags(false)
}

async function loadArticle() {
  if (!isEdit.value) return
  const a = await adminGetArticle(id.value)
  form.title = a.title
  form.summary = a.summary
  form.contentMd = a.contentMd
  form.coverUrl = a.coverUrl || ''
  form.status = a.status || 'DRAFT'
  form.categoryId = a.category?.id ?? null
  form.tagIds = a.tags?.map((t) => t.id) || []
}

async function onSave() {
  if (!form.title.trim() || !form.summary.trim() || !form.contentMd.trim()) {
    ElMessage.warning('请填写标题 / 摘要 / 正文')
    return
  }
  loading.value = true
  try {
    if (isEdit.value) {
      await adminUpdateArticle(id.value, form)
      ElMessage.success('已保存')
    } else {
      const res = await adminCreateArticle(form)
      ElMessage.success('已发布')
      router.replace(`/admin/editor/${res.id}`)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadMeta()
  await loadArticle()
})
</script>

<template>
  <div class="admin-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="head">
          <div class="head__title">{{ isEdit ? '编辑文章' : '发布文章' }}</div>
          <div class="head__actions">
            <el-button @click="router.push('/admin/articles')">返回列表</el-button>
            <el-button type="primary" @click="onSave">保存</el-button>
          </div>
        </div>
      </template>

      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" maxlength="400" show-word-limit />
        </el-form-item>

        <div class="row">
          <el-form-item label="状态" class="row__item">
            <el-select v-model="form.status">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已发布" value="PUBLISHED" />
            </el-select>
          </el-form-item>

          <el-form-item label="分类" class="row__item">
            <el-select v-model="form.categoryId" clearable placeholder="可选">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="标签" class="row__item">
            <el-select v-model="form.tagIds" multiple filterable clearable placeholder="可选">
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="封面图 URL（可选）">
          <el-input v-model="form.coverUrl" maxlength="500" />
        </el-form-item>

        <el-form-item label="正文（Markdown）">
          <el-input v-model="form.contentMd" type="textarea" :rows="18" placeholder="# Hello" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.head__title {
  font-weight: 900;
}
.row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.row__item {
  margin-bottom: 0;
}
@media (max-width: 780px) {
  .row {
    grid-template-columns: 1fr;
  }
}
</style>
