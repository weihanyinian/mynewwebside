<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '../../api/blog'
import { useUserStore } from '../../stores/user'
import { getSafeInternalPath } from '../../utils/safeRedirect'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 64, message: '用户名长度 2～64 字符', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  loading.value = true
  try {
    const data = await login(form.username.trim(), form.password)
    userStore.setSession(data)
    ElMessage.success('登录成功')
    const next = getSafeInternalPath(route.query.redirect)
    await router.replace(next || '/')
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
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="auth-form site-el-round-16"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            show-password
            placeholder="密码"
          />
        </el-form-item>
        <button
          type="submit"
          class="site-pill site-pill--active auth-btn"
          :disabled="loading"
          :class="{ 'auth-btn--loading': loading }"
          @click="onSubmit"
        >
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </el-form>
      <div class="auth-footer">
        <router-link class="auth-link site-link-animated" to="/register">没有账号？注册</router-link>
        <router-link class="auth-link site-link-animated" to="/">← 返回主页</router-link>
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
  min-height: 48px;
}
.auth-btn--loading {
  pointer-events: none;
  opacity: 0.88;
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
  padding: 6px 0;
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
:deep(.el-input__wrapper) {
  transition:
    box-shadow 0.28s ease,
    border-color 0.28s ease !important;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 217, 255, 0.45), 0 4px 18px rgba(74, 144, 226, 0.2) !important;
}
:root[data-theme='dark'] :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 217, 255, 0.35), 0 4px 20px rgba(0, 40, 80, 0.35) !important;
}
</style>
