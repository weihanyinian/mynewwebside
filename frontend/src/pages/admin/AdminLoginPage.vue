<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/blog'
import { setToken } from '../../utils/token'

const router = useRouter()
const route = useRoute()

const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)

async function onSubmit() {
  loading.value = true
  try {
    const token = await login(username.value, password.value)
    setToken(token)
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/admin/articles'
    router.replace(redirect)
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <div class="card login-card">
      <div class="login-title">管理员登录</div>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 60px);
}
.login-card {
  width: 420px;
  padding: 18px;
}
.login-title {
  font-size: 20px;
  font-weight: 900;
  margin-bottom: 12px;
}
</style>

