<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/blog'
import { setToken } from '../../utils/token'

const router = useRouter()
const route = useRoute()

const username = ref('admin')
const password = ref('123456')
const loading = ref(false)

async function onSubmit() {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    // 模拟登录拦截
    if (username.value === 'admin' && password.value === '123456') {
      // Mocking token
      setToken('mock-token-admin')
      ElMessage.success('欢迎回来！')
      const redirect = (route.query.redirect as string) || '/admin/articles'
      router.replace(redirect)
    } else {
      // 尝试真实接口
      const token = await login(username.value, password.value)
      setToken(token)
      ElMessage.success('登录成功')
      const redirect = (route.query.redirect as string) || '/admin/articles'
      router.replace(redirect)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败，账号或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-background"></div>
    <div class="card login-card">
      <div class="login-header">
        <h2 class="login-title">管理后台</h2>
        <p class="login-subtitle">Admin Portal</p>
      </div>
      <el-form label-position="top" @submit.prevent="onSubmit" class="login-form">
        <el-form-item label="用户名 (Username)">
          <el-input v-model="username" autocomplete="username" placeholder="admin" class="cyber-input" />
        </el-form-item>
        <el-form-item label="密码 (Password)">
          <el-input v-model="password" type="password" autocomplete="current-password" show-password placeholder="123456" class="cyber-input" />
        </el-form-item>
        <button class="btn-login" :disabled="loading" @click="onSubmit">
          {{ loading ? '验证中...' : '登 录' }}
        </button>
      </el-form>
      <div class="login-footer">
        <a @click="router.push('/')">← 返回主页</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.9) 0%, rgba(42, 27, 61, 0.95) 100%);
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle at 50% 50%, rgba(74, 144, 226, 0.1) 0%, transparent 60%);
  z-index: 0;
}

.login-card {
  width: 400px;
  padding: 40px;
  background: rgba(16, 18, 27, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(74, 144, 226, 0.3);
  box-shadow: 0 0 40px rgba(74, 144, 226, 0.2), inset 0 0 20px rgba(74, 144, 226, 0.1);
  border-radius: 20px;
  z-index: 1;
  color: #e2e8f0;
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 900;
  margin: 0 0 8px;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}

.login-subtitle {
  font-size: 14px;
  color: #a18cd1;
  text-transform: uppercase;
  letter-spacing: 4px;
  margin: 0;
}

.login-form :deep(.el-form-item__label) {
  color: #e2e8f0;
  font-weight: 600;
  padding-bottom: 4px;
}

.cyber-input :deep(.el-input__wrapper) {
  background: rgba(0, 0, 0, 0.3);
  box-shadow: 0 0 0 1px rgba(74, 144, 226, 0.3) inset;
  border-radius: 8px;
}

.cyber-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #50e3c2 inset, 0 0 10px rgba(80, 227, 194, 0.3);
}

.cyber-input :deep(.el-input__inner) {
  color: #e2e8f0;
  height: 40px;
}

.btn-login {
  width: 100%;
  margin-top: 24px;
  padding: 14px;
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(74, 144, 226, 0.4);
}

.btn-login:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(80, 227, 194, 0.5);
  filter: brightness(1.1);
}

.btn-login:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-footer {
  margin-top: 24px;
  text-align: center;
}

.login-footer a {
  color: #a18cd1;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s;
}

.login-footer a:hover {
  color: #fbc2eb;
}
</style>

