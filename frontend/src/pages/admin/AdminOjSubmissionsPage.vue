<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminListOjSubmissions } from '../../api/admin'
import type { OjSubmissionRow } from '../../api/oj'

const router = useRouter()
const loading = ref(true)
const page = ref(0)
const size = ref(20)
const total = ref(0)
const items = ref<OjSubmissionRow[]>([])

async function load() {
  loading.value = true
  try {
    const data = await adminListOjSubmissions({ page: page.value, size: size.value })
    items.value = data.items
    total.value = data.total
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function prev() {
  if (page.value <= 0) return
  page.value--
  void load()
}

function next() {
  if ((page.value + 1) * size.value >= total.value) return
  page.value++
  void load()
}

onMounted(() => void load())
</script>

<template>
  <div>
    <h2 class="page-title">OJ 提交记录</h2>
    <p v-if="loading">加载中…</p>
    <el-table v-else :data="items" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column label="题目" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="router.push(`/tools/oj/p/${row.problemId}`)">{{ row.problemId }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="language" label="语言" width="100" />
      <el-table-column prop="submitted" label="提交" width="80">
        <template #default="{ row }">{{ row.submitted ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="verdict" label="结果" width="100" />
      <el-table-column prop="message" label="说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" min-width="180" />
    </el-table>
    <div class="pager">
      <el-button :disabled="page <= 0" @click="prev">上一页</el-button>
      <span class="meta">{{ page + 1 }} / {{ Math.max(1, Math.ceil(total / size)) }}（共 {{ total }}）</span>
      <el-button :disabled="(page + 1) * size >= total" @click="next">下一页</el-button>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  margin-top: 0;
  color: #e2e8f0;
}
.pager {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.meta {
  color: #94a3b8;
  font-size: 0.9rem;
}
</style>
