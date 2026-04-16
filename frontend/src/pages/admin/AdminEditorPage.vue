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
import MarkdownView from '../../components/MarkdownView.vue'

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

        <el-form-item label="正文（Markdown · 左侧编辑 / 右侧实时预览）">
          <div class="md-split">
            <el-input
              v-model="form.contentMd"
              type="textarea"
              :rows="22"
              placeholder="# 标题&#10;&#10;支持 **粗体**、`代码`、列表与代码高亮（与前台 MarkdownView 一致）。"
              class="md-split__editor"
            />
            <div class="md-split__preview">
              <p class="md-split__preview-title">预览</p>
              <div class="md-split__preview-body">
                <MarkdownView :content="form.contentMd" />
              </div>
            </div>
          </div>
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

/* 管理后台 Markdown 分栏：青蓝玻璃态与前台正文风格对齐 */
.md-split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  width: 100%;
}
@media (max-width: 1100px) {
  .md-split {
    grid-template-columns: 1fr;
  }
}
.md-split__editor :deep(textarea) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.55;
}
.md-split__preview {
  border-radius: 12px;
  border: 1px solid rgba(74, 144, 226, 0.35);
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(12px);
  overflow: hidden;
  min-height: 420px;
  display: flex;
  flex-direction: column;
}
.md-split__preview-title {
  margin: 0;
  padding: 10px 14px;
  font-size: 0.8rem;
  font-weight: 800;
  color: #50e3c2;
  border-bottom: 1px solid rgba(74, 144, 226, 0.25);
}
.md-split__preview-body {
  padding: 12px 14px 16px;
  overflow: auto;
  flex: 1;
  max-height: min(70vh, 640px);
}
</style>
