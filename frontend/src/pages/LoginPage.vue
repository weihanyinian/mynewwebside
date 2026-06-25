<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!username.value || !password.value) {
    error.value = '请填写用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await auth.login(username.value, password.value)
    router.push('/admin')
  } catch (e: any) {
    error.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4">
    <GlassCard class="w-full max-w-md !p-8">
      <h1 class="text-2xl font-bold text-center mb-2 gradient-text">管理员登录</h1>
      <p class="text-sm text-center text-[var(--text-muted)] mb-8">维寒一念的小站 · 后台管理</p>

      <form @submit.prevent="handleLogin" class="space-y-5">
        <div>
          <label class="block text-sm font-medium mb-1.5 text-[var(--text-primary)]">用户名</label>
          <input
            v-model="username"
            type="text"
            class="glass-input w-full"
            placeholder="请输入用户名"
            autocomplete="username"
          />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1.5 text-[var(--text-primary)]">密码</label>
          <input
            v-model="password"
            type="password"
            class="glass-input w-full"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </div>

        <p v-if="error" class="text-red-400 text-sm">{{ error }}</p>

        <button type="submit" :disabled="loading" class="glass-button primary w-full py-3 font-medium text-base">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <p class="text-xs text-center mt-6 text-[var(--text-muted)]">
        <a href="/" class="underline hover:text-[var(--text-primary)]">← 返回首页</a>
      </p>
    </GlassCard>
  </div>
</template>
