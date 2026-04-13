<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { clearToken } from '../utils/token'

const router = useRouter()
const route = useRoute()

function logout() {
  clearToken()
  router.push('/admin/login')
}
</script>

<template>
  <el-container class="admin-root">
    <el-aside class="admin-aside" width="220px">
      <div class="admin-brand" @click="router.push('/admin/articles')">Admin</div>
      <el-menu
        :default-active="route.path"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/articles">文章</el-menu-item>
        <el-menu-item index="/admin/editor">发布</el-menu-item>
        <el-menu-item index="/admin/categories">分类</el-menu-item>
        <el-menu-item index="/admin/tags">标签</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="admin-header__inner">
          <div class="admin-header__title">后台管理</div>
          <div class="admin-header__actions">
            <el-button type="primary" plain @click="router.push('/')">查看前台</el-button>
            <el-button @click="logout">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="admin-main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-root {
  min-height: 100vh;
}

.admin-aside {
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.9);
}

.admin-brand {
  padding: 16px 16px 10px;
  font-weight: 800;
  cursor: pointer;
}

.admin-header {
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.admin-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.admin-header__title {
  font-weight: 700;
}

.admin-main {
  background: #f6f7fb;
}
</style>

