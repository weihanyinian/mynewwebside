<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDeleteArticle, adminPageArticles, type AdminArticleListItem } from '../../api/blog'

const router = useRouter()

const keyword = ref('')
const status = ref<string | undefined>(undefined)
const page = ref(0)
const size = ref(10)
const total = ref(0)
const items = ref<AdminArticleListItem[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await adminPageArticles({
      keyword: keyword.value || undefined,
      status: status.value || undefined,
      page: page.value,
      size: size.value,
    })
    items.value = res.items
    total.value = res.total
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function onDelete(id: number) {
  await ElMessageBox.confirm('确认删除该文章？', '提示', { type: 'warning' })
  await adminDeleteArticle(id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <el-card>
      <template #header>
        <div class="head">
          <div class="head__title">文章管理</div>
          <el-button type="primary" @click="router.push('/admin/editor')">发布文章</el-button>
        </div>
      </template>

      <div class="filters">
        <el-input v-model="keyword" placeholder="关键字" clearable @keyup.enter="() => { page = 0; load() }" />
        <el-select v-model="status" clearable placeholder="状态" style="width: 160px">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
        </el-select>
        <el-button @click="() => { page = 0; load() }">查询</el-button>
      </div>

      <el-empty
        v-if="!loading && items.length === 0"
        description="暂无文章"
      >
        <el-button type="primary" @click="router.push('/admin/editor')">去撰写第一篇文章</el-button>
        <p class="empty-tip">写好标题、摘要与 Markdown 正文后，将状态设为「已发布」即可在博客首页展示。</p>
      </el-empty>

      <el-table v-else :data="items" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="title" label="标题" min-width="260" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" effect="plain">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="160">
          <template #default="{ row }">
            <span class="muted">{{ row.category?.name || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览" width="110" />
        <el-table-column label="更新时间" width="200">
          <template #default="{ row }">
            <span class="muted">{{ new Date(row.updatedAt).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="router.push(`/admin/editor/${row.id}`)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pager">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="page + 1"
          :page-size="size"
          :total="total"
          @current-change="(p: number) => { page = p - 1; load() }"
        />
      </div>
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
  justify-content: space-between;
  align-items: center;
}
.head__title {
  font-weight: 900;
}
.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.empty-tip {
  margin: 12px 0 0;
  max-width: 400px;
  color: rgba(226, 232, 240, 0.72);
  font-size: 0.88rem;
  line-height: 1.55;
}
</style>

