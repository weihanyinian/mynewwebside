<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminDeleteUser, adminListUsers, type AdminUserRow } from '../../api/admin'

const loading = ref(true)
const rows = ref<AdminUserRow[]>([])

async function load() {
  loading.value = true
  try {
    rows.value = await adminListUsers()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function onDelete(r: AdminUserRow) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${r.username}」？`, '确认', { type: 'warning' })
    await adminDeleteUser(r.id)
    ElMessage.success('已删除')
    await load()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(() => void load())
</script>

<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <p v-if="loading">加载中…</p>
    <el-table v-else :data="rows" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="createdAt" label="注册时间" min-width="200" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-if="row.username.toLowerCase() !== 'admin'"
            type="danger"
            link
            @click="onDelete(row)"
          >
            删除
          </el-button>
          <span v-else class="muted">内置管理员</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page-title {
  margin-top: 0;
  color: #e2e8f0;
}
.muted {
  color: #94a3b8;
  font-size: 0.85rem;
}
</style>
