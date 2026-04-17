<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/blog'

const router = useRouter()

const username = ref('')
const password = ref('')
const nickname = ref('')
const loading = ref(false)

async function onSubmit() {
  if (!username.value || !password.value || !nickname.value) {
    ElMessage.warning('请填写完整')
    return
  }
  if (password.value.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  loading.value = true
  try {
    await register({
      username: username.value.trim(),
      password: password.value,
      nickname: nickname.value.trim(),
    })
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
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
      <el-form label-position="top" class="auth-form" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="username" autocomplete="username" placeholder="3–64 字符" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="nickname" autocomplete="nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" autocomplete="new-password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <button type="submit" class="site-pill site-pill--active auth-btn" :disabled="loading" @click="onSubmit">
          {{ loading ? '提交中…' : '注册' }}
        </button>
      </el-form>
      <div class="auth-footer">
        <router-link class="auth-link" to="/login">已有账号？登录</router-link>
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
