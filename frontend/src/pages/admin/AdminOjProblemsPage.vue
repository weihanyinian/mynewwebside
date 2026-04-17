<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminListOjProblems } from '../../api/admin'
import type { ProblemListItem } from '../../api/oj'

const router = useRouter()
const loading = ref(true)
const rows = ref<ProblemListItem[]>([])

async function load() {
  loading.value = true
  try {
    rows.value = await adminListOjProblems()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => void load())
</script>

<template>
  <div>
    <h2 class="page-title">OJ 题目管理</h2>
    <p class="hint">题目数据存于数据库；完整增删改可通过后台 API 或直接向表 oj_problem 维护。</p>
    <p v-if="loading">加载中…</p>
    <el-table v-else :data="rows" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="difficulty" label="难度" width="100" />
      <el-table-column prop="judgeMode" label="模式" width="100" />
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button type="primary" link @click="router.push(`/tools/oj/p/${row.id}`)">前台查看</el-button>
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
.hint {
  color: #94a3b8;
  font-size: 0.9rem;
  margin-bottom: 16px;
}
</style>
