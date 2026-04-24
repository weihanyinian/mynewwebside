<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreatePortfolioWork,
  adminDeletePortfolioWork,
  adminListPortfolioWorks,
  adminUpdatePortfolioWork,
  type PortfolioWorkAdmin,
  type PortfolioWorkUpsertPayload,
} from '../../api/portfolioApi'

const loading = ref(false)
const items = ref<PortfolioWorkAdmin[]>([])
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive<PortfolioWorkUpsertPayload>({
  title: '',
  desc: '',
  detail: '',
  tag: '',
  link: '',
  cover: '/avatar.webp',
  enabled: true,
  sortOrder: 0,
})

function resetForm() {
  form.title = ''
  form.desc = ''
  form.detail = ''
  form.tag = ''
  form.link = ''
  form.cover = '/avatar.webp'
  form.enabled = true
  form.sortOrder = 0
}

function fillForm(row: PortfolioWorkAdmin) {
  form.title = row.title
  form.desc = row.desc
  form.detail = row.detail
  form.tag = row.tag
  form.link = row.link
  form.cover = row.cover
  form.enabled = row.enabled
  form.sortOrder = row.sortOrder
}

async function load() {
  loading.value = true
  try {
    items.value = await adminListPortfolioWorks()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载作品失败')
  } finally {
    loading.value = false
  }
}

function onCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function onEdit(row: PortfolioWorkAdmin) {
  editingId.value = row.id
  fillForm(row)
  dialogVisible.value = true
}

async function onSave() {
  if (!form.title.trim() || !form.desc.trim() || !form.detail.trim() || !form.tag.trim() || !form.link.trim()) {
    ElMessage.warning('请先完整填写标题、简介、详情、标签与链接')
    return
  }
  saving.value = true
  try {
    const payload: PortfolioWorkUpsertPayload = {
      ...form,
      title: form.title.trim(),
      desc: form.desc.trim(),
      detail: form.detail.trim(),
      tag: form.tag.trim(),
      link: form.link.trim(),
      cover: form.cover.trim() || '/avatar.webp',
    }
    if (editingId.value) {
      await adminUpdatePortfolioWork(editingId.value, payload)
      ElMessage.success('作品已更新')
    } else {
      await adminCreatePortfolioWork(payload)
      ElMessage.success('作品已创建')
    }
    dialogVisible.value = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function onDelete(row: PortfolioWorkAdmin) {
  await ElMessageBox.confirm(`确认删除作品「${row.title}」？`, '提示', { type: 'warning' })
  await adminDeletePortfolioWork(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="head">
          <div class="head__title">作品管理</div>
          <el-button type="primary" @click="onCreate">新增作品</el-button>
        </div>
      </template>

      <el-empty v-if="!loading && items.length === 0" description="暂无作品">
        <p class="empty-tip">从这里维护首页与作品展示页的项目卡片，新增后前台会自动读取。</p>
      </el-empty>

      <el-table v-else :data="items">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="tag" label="标签" width="160" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" effect="plain">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.updatedAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="onEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑作品' : '新增作品'"
      width="760px"
      destroy-on-close
    >
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="160" show-word-limit />
        </el-form-item>
        <el-form-item label="简介（卡片摘要）">
          <el-input v-model="form.desc" type="textarea" :rows="2" maxlength="600" show-word-limit />
        </el-form-item>
        <el-form-item label="详情（展示页详细描述）">
          <el-input v-model="form.detail" type="textarea" :rows="3" />
        </el-form-item>
        <div class="row">
          <el-form-item label="标签" class="row__item">
            <el-input v-model="form.tag" maxlength="100" />
          </el-form-item>
          <el-form-item label="排序" class="row__item">
            <el-input-number v-model="form.sortOrder" :min="-9999" :max="9999" />
          </el-form-item>
          <el-form-item label="启用" class="row__item">
            <el-switch v-model="form.enabled" />
          </el-form-item>
        </div>
        <el-form-item label="链接（站内路径 / 或 https 外链）">
          <el-input v-model="form.link" maxlength="700" />
        </el-form-item>
        <el-form-item label="封面 URL">
          <el-input v-model="form.cover" maxlength="700" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
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
  gap: 12px;
}
.head__title {
  font-weight: 900;
}
.empty-tip {
  margin-top: 8px;
  max-width: 420px;
  color: rgba(226, 232, 240, 0.72);
  font-size: 0.88rem;
  line-height: 1.55;
}
.row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.row__item {
  margin-bottom: 0;
}
@media (max-width: 860px) {
  .row {
    grid-template-columns: 1fr;
  }
}
</style>
