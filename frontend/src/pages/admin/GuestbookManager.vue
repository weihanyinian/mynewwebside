<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { guestbookApi, GuestbookEntry } from '../../api/guestbook'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()
const entries = ref<GuestbookEntry[]>([])
const loading = ref(true)

onMounted(() => loadEntries())

async function loadEntries() {
  try {
    const res = await guestbookApi.getAll()
    entries.value = res.data.data.content
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function deleteEntry(id: number) {
  if (!confirm('确认删除这条留言？')) return
  try {
    await guestbookApi.delete(id)
    await loadEntries()
  } catch (e) {
    alert('删除失败')
  }
}
</script>

<template>
  <div class="min-h-screen px-4 py-10 max-w-4xl mx-auto">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold gradient-text">💬 留言管理</h1>
      <button @click="router.push('/admin')" class="glass-button text-sm">← 后台</button>
    </div>

    <div v-if="loading" class="text-center py-10 text-[var(--text-muted)]">加载中...</div>
    <div v-else class="space-y-4">
      <GlassCard v-for="e in entries" :key="e.id" class="!p-5 flex justify-between items-start">
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 mb-1">
            <span class="font-medium text-[var(--text-primary)]">{{ e.nickname }}</span>
            <span v-if="e.email" class="text-xs text-[var(--text-muted)]">{{ e.email }}</span>
            <span class="text-xs text-[var(--text-muted)] ml-auto">{{ e.createdAt?.slice(0, 16).replace('T', ' ') }}</span>
          </div>
          <p class="text-sm text-[var(--text-secondary)]">{{ e.content }}</p>
        </div>
        <button @click="deleteEntry(e.id)" class="glass-button text-sm !border-red-500/30 !text-red-400 ml-4 shrink-0">删除</button>
      </GlassCard>
      <p v-if="!entries.length" class="text-center py-10 text-[var(--text-muted)]">暂无留言</p>
    </div>
  </div>
</template>
