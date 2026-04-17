<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/blog'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

async function onSubmit() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await login(username.value, password.value)
    userStore.setSession(data)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || ''
    if (redirect) {
      router.replace(redirect)
    } else if (data.admin) {
      router.replace('/admin/articles')
    } else {
      router.replace('/')
    }
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card glass-surface">
      <h1 class="auth-title">登录</h1>
      <p class="auth-sub">使用网站账号访问 OJ 与更多功能</p>
      <el-form label-position="top" class="auth-form" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="username" autocomplete="username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" autocomplete="current-password" show-password placeholder="密码" />
        </el-form-item>
        <button type="submit" class="site-pill site-pill--active auth-btn" :disabled="loading" @click="onSubmit">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </el-form>
      <div class="auth-footer">
        <router-link class="auth-link" to="/register">没有账号？注册</router-link>
        <router-link class="auth-link" to="/">← 返回主页</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - 160px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}
.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 28px;
  border-radius: 16px;
}
.auth-title {
  margin: 0 0 8px;
  font-size: 1.5rem;
  color: var(--text-color);
}
.auth-sub {
  margin: 0 0 20px;
  color: var(--text-color);
  opacity: 0.8;
  font-size: 0.95rem;
}
.auth-form :deep(.el-form-item__label) {
  color: var(--text-color);
}
.auth-btn {
  width: 100%;
  margin-top: 8px;
  border: none;
  cursor: pointer;
  padding: 12px;
  font-weight: 700;
}
.auth-footer {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: center;
}
.auth-link {
  color: var(--primary-color);
  text-decoration: none;
  font-size: 0.9rem;
}
.auth-link:hover {
  text-decoration: underline;
}
</style>
