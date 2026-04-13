<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreateCategory,
  adminDeleteCategory,
  adminUpdateCategory,
  getCategories,
  type Category,
} from '../../api/blog'

const loading = ref(false)
const items = ref<Category[]>([])
const newName = ref('')

async function load() {
  loading.value = true
  try {
    items.value = await getCategories(false)
  } finally {
    loading.value = false
  }
}

async function onCreate() {
  if (!newName.value.trim()) return
  try {
    await adminCreateCategory(newName.value.trim())
    newName.value = ''
    ElMessage.success('已创建')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  }
}

async function onEdit(row: Category) {
  const { value } = await ElMessageBox.prompt('请输入新分类名', '编辑分类', {
    inputValue: row.name,
    confirmButtonText: '保存',
    cancelButtonText: '取消',
  })
  await adminUpdateCategory(row.id, value)
  ElMessage.success('已保存')
  await load()
}

async function onDelete(row: Category) {
  await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
  await adminDeleteCategory(row.id)
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
          <div class="head__title">分类管理</div>
          <div class="head__actions">
            <el-input v-model="newName" placeholder="新分类名" style="width: 220px" />
            <el-button type="primary" @click="onCreate">新增</el-button>
          </div>
        </div>
      </template>

      <el-table :data="items">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="name" label="名称" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="onEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.admin-page {
  max-width: 1000px;
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
.head__actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>

