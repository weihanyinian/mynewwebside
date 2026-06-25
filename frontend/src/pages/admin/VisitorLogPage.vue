<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi, VisitorLog } from '../../api/admin'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()
const logs = ref<VisitorLog[]>([])
const loading = ref(true)
const page = ref(0)
const totalPages = ref(0)

onMounted(() => loadLogs())

async function loadLogs() {
  loading.value = true
  try {
    const res = await adminApi.getVisitors(page.value)
    logs.value = res.data.data.content
    totalPages.value = res.data.data.totalPages
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function changePage(delta: number) {
  page.value += delta
  loadLogs()
}
</script>

<template>
  <div class="min-h-screen px-4 py-10 max-w-6xl mx-auto">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold gradient-text">👁 访问日志</h1>
      <button @click="router.push('/admin')" class="glass-button text-sm">← 后台</button>
    </div>

    <div v-if="loading" class="text-center py-10 text-[var(--text-muted)]">加载中...</div>
    <div v-else>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-[var(--border-color)] text-left">
              <th class="p-3 text-[var(--text-muted)] font-medium">时间</th>
              <th class="p-3 text-[var(--text-muted)] font-medium">IP</th>
              <th class="p-3 text-[var(--text-muted)] font-medium">路径</th>
              <th class="p-3 text-[var(--text-muted)] font-medium">方法</th>
              <th class="p-3 text-[var(--text-muted)] font-medium">来源</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in logs" :key="log.id" class="border-b border-[var(--border-color)] hover:bg-[var(--card-bg)]">
              <td class="p-3 text-[var(--text-muted)] whitespace-nowrap">{{ log.visitTime?.slice(0, 19).replace('T', ' ') }}</td>
              <td class="p-3 font-mono text-[var(--text-primary)]">{{ log.ip }}</td>
              <td class="p-3 text-[var(--text-primary)] truncate max-w-[300px]">{{ log.path }}</td>
              <td class="p-3">
                <span :class="['text-xs px-1.5 py-0.5 rounded', log.method === 'GET' ? 'bg-blue-500/20 text-blue-400' : 'bg-orange-500/20 text-orange-400']">
                  {{ log.method }}
                </span>
              </td>
              <td class="p-3 text-[var(--text-muted)] truncate max-w-[200px]">{{ log.referer || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="!logs.length" class="text-center py-10 text-[var(--text-muted)]">暂无访问记录</div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center gap-3 mt-6">
        <button :disabled="page === 0" @click="changePage(-1)" class="glass-button text-sm" :class="{ 'opacity-50': page === 0 }">上一页</button>
        <span class="text-sm text-[var(--text-muted)] self-center">{{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="changePage(1)" class="glass-button text-sm" :class="{ 'opacity-50': page >= totalPages - 1 }">下一页</button>
      </div>
    </div>
  </div>
</template>
