<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { adminApi, DashboardStats } from '../../api/admin'
import GlassCard from '../../components/GlassCard.vue'

const router = useRouter()
const auth = useAuthStore()
const stats = ref<DashboardStats | null>(null)
const loading = ref(true)

onMounted(async () => {
  if (!auth.isAdmin) {
    router.push('/admin/login')
    return
  }
  try {
    const res = await adminApi.getDashboard()
    stats.value = res.data.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

function logout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen px-4 py-10 max-w-6xl mx-auto">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold gradient-text">📊 管理仪表盘</h1>
      <div class="flex gap-3">
        <button @click="router.push('/')" class="glass-button text-sm">← 前台</button>
        <button @click="logout" class="glass-button text-sm">退出</button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-20 text-[var(--text-muted)]">加载中...</div>

    <template v-else-if="stats">
      <!-- Stats Cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <GlassCard class="!p-5 text-center">
          <div class="text-3xl font-bold gradient-text">{{ stats.articleCount }}</div>
          <div class="text-sm text-[var(--text-muted)] mt-1">文章数</div>
        </GlassCard>
        <GlassCard class="!p-5 text-center">
          <div class="text-3xl font-bold gradient-text">{{ stats.guestbookCount }}</div>
          <div class="text-sm text-[var(--text-muted)] mt-1">留言数</div>
        </GlassCard>
        <GlassCard class="!p-5 text-center">
          <div class="text-3xl font-bold gradient-text">{{ stats.todayVisits }}</div>
          <div class="text-sm text-[var(--text-muted)] mt-1">今日访问</div>
        </GlassCard>
        <GlassCard class="!p-5 text-center">
          <div class="text-3xl font-bold gradient-text">{{ stats.totalVisits }}</div>
          <div class="text-sm text-[var(--text-muted)] mt-1">总访问量</div>
        </GlassCard>
      </div>

      <!-- Quick Links -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <GlassCard class="!p-5 text-center cursor-pointer hover:!border-[var(--primary)]" @click="router.push('/admin/articles')">
          <div class="text-2xl mb-1">📝</div>
          <div class="text-sm font-medium">文章管理</div>
        </GlassCard>
        <GlassCard class="!p-5 text-center cursor-pointer hover:!border-[var(--primary)]" @click="router.push('/admin/guestbooks')">
          <div class="text-2xl mb-1">💬</div>
          <div class="text-sm font-medium">留言管理</div>
        </GlassCard>
        <GlassCard class="!p-5 text-center cursor-pointer hover:!border-[var(--primary)]" @click="router.push('/admin/visitors')">
          <div class="text-2xl mb-1">👁</div>
          <div class="text-sm font-medium">访问日志</div>
        </GlassCard>
        <GlassCard class="!p-5 text-center cursor-pointer hover:!border-[var(--primary)]" @click="router.push('/')">
          <div class="text-2xl mb-1">🏠</div>
          <div class="text-sm font-medium">返回前台</div>
        </GlassCard>
      </div>

      <!-- Daily Visits Chart (simple bar) -->
      <GlassCard class="!p-6 mb-6">
        <h2 class="text-lg font-bold mb-4">最近7天访问量</h2>
        <div class="flex items-end gap-2 h-32">
          <div
            v-for="d in stats.dailyVisits"
            :key="d.date"
            class="flex-1 flex flex-col items-center"
          >
            <div class="text-xs text-[var(--text-muted)] mb-1">{{ d.count }}</div>
            <div
              class="w-full rounded-t-md"
              :style="{
                height: Math.max(4, (d.count / Math.max(...stats.dailyVisits.map(x => x.count), 1)) * 100) + '%',
                background: 'linear-gradient(180deg, var(--primary) 0%, rgba(98,167,234,0.3) 100%)'
              }"
            ></div>
            <div class="text-[10px] text-[var(--text-muted)] mt-1">{{ d.date.slice(5) }}</div>
          </div>
        </div>
      </GlassCard>

      <!-- Top Pages & Top IPs -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <GlassCard class="!p-5">
          <h2 class="text-lg font-bold mb-3">热门页面 (TOP 10)</h2>
          <div v-for="p in stats.topPaths" :key="p.path" class="flex justify-between text-sm py-1.5 border-b border-[var(--border-color)] last:border-0">
            <span class="text-[var(--text-primary)] truncate max-w-[70%]">{{ p.path }}</span>
            <span class="text-[var(--text-muted)]">{{ p.count }}</span>
          </div>
          <p v-if="!stats.topPaths.length" class="text-sm text-[var(--text-muted)]">暂无数据</p>
        </GlassCard>
        <GlassCard class="!p-5">
          <h2 class="text-lg font-bold mb-3">访问 IP (TOP 10)</h2>
          <div v-for="ip in stats.topIps" :key="ip.ip" class="flex justify-between text-sm py-1.5 border-b border-[var(--border-color)] last:border-0">
            <span class="text-[var(--text-primary)]">{{ ip.ip }}</span>
            <span class="text-[var(--text-muted)]">{{ ip.count }}</span>
          </div>
          <p v-if="!stats.topIps.length" class="text-sm text-[var(--text-muted)]">暂无数据</p>
        </GlassCard>
      </div>
    </template>
  </div>
</template>
