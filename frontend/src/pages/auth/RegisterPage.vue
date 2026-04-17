<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '../../api/blog'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名 3～64 字符', trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 64, message: '昵称 1～64 字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

async function onSubmit() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) return
  loading.value = true
  try {
    await register({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim(),
    })
    ElMessage.success('注册成功，请登录')
    await router.replace('/login')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card glass-surface">
      <h1 class="auth-title">注册</h1>
      <p class="auth-sub">注册后可使用在线 OJ 与提交记录</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="auth-form site-el-round-16"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" placeholder="3～64 字符" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" autocomplete="nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            autocomplete="new-password"
            show-password
            placeholder="至少 6 位"
          />
        </el-form-item>
        <button
          type="submit"
          class="site-pill site-pill--active auth-btn"
          :disabled="loading"
          :class="{ 'auth-btn--loading': loading }"
          @click="onSubmit"
        >
          {{ loading ? '提交中…' : '注册' }}
        </button>
      </el-form>
      <div class="auth-footer">
        <router-link class="auth-link site-link-animated" to="/login">已有账号？登录</router-link>
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
