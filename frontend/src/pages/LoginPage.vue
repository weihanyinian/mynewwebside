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
    if (auth.isAdmin) {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-12">
    <GlassCard class="w-full max-w-md !p-8">
      <h1 class="text-2xl font-bold text-center mb-1 gradient-text">登录</h1>
      <p class="text-sm text-center text-[var(--text-muted)] mb-8">登录后可访问管理后台</p>

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

        <p v-if="error" class="text-red-400 text-sm bg-red-500/10 rounded-lg p-3 border border-red-500/20">{{ error }}</p>

        <button type="submit" :disabled="loading" class="glass-button primary w-full py-3 font-medium text-base">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <p class="text-xs text-center mt-6 text-[var(--text-muted)]">
        <a href="/" class="underline hover:text-[var(--text-primary)]">← 返回首页</a>
      </p>
    </GlassCard>
  </div>
</template>
