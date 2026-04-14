<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { clearToken } from '../utils/token'
import { Document, Edit, Folder, CollectionTag } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

function logout() {
  clearToken()
  router.push('/admin/login')
}
</script>

<template>
  <el-container class="admin-root">
    <el-aside class="admin-aside" width="240px">
      <div class="admin-brand" @click="router.push('/')">
        <span class="brand-text">MyWebSide</span>
        <span class="brand-badge">ADMIN</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="admin-menu"
        background-color="transparent"
        text-color="#e2e8f0"
        active-text-color="#50e3c2"
      >
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/editor">
          <el-icon><Edit /></el-icon>
          <span>文章撰写</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><CollectionTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="admin-content-wrapper">
      <el-header class="admin-header">
        <div class="admin-header__inner">
          <div class="admin-header__title">
            <!-- Icon or Breadcrumb could go here -->
            <span>控制面板</span>
          </div>
          <div class="admin-header__actions">
            <el-button class="cyber-btn-outline" @click="router.push('/blog')">返回前台</el-button>
            <el-button class="cyber-btn-danger" @click="logout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="admin-main">
        <div class="admin-main-inner">
          <slot />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-root {
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(26, 26, 46, 1) 0%, rgba(42, 27, 61, 1) 100%);
  color: #e2e8f0;
}

.admin-aside {
  background: rgba(16, 18, 27, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid rgba(74, 144, 226, 0.2);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 24px rgba(0,0,0,0.5);
  z-index: 10;
}

.admin-brand {
  padding: 24px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px dashed rgba(74, 144, 226, 0.3);
}

.brand-text {
  font-size: 1.2rem;
  font-weight: 900;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-badge {
  font-size: 0.7rem;
  padding: 2px 6px;
  background: rgba(80, 227, 194, 0.2);
  color: #50e3c2;
  border-radius: 4px;
  font-weight: bold;
}

.admin-menu {
  border-right: none;
  flex: 1;
  padding-top: 10px;
}

.admin-menu :deep(.el-menu-item) {
  margin: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.admin-menu :deep(.el-menu-item:hover) {
  background: rgba(74, 144, 226, 0.1) !important;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(74, 144, 226, 0.2) 0%, transparent 100%) !important;
  border-left: 3px solid #50e3c2;
}

.admin-content-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: rgba(26, 26, 46, 0.6);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(74, 144, 226, 0.2);
  padding: 0 24px;
  height: 64px;
}

.admin-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.admin-header__title {
  font-weight: 700;
  font-size: 1.1rem;
  color: #a18cd1;
  letter-spacing: 1px;
}

.admin-header__actions {
  display: flex;
  gap: 12px;
}

.cyber-btn-outline {
  background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
  border-radius: 6px;
}
.cyber-btn-outline:hover {
  background: rgba(74, 144, 226, 0.1);
  color: #50e3c2;
  border-color: #50e3c2;
}

.cyber-btn-danger {
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid #e74c3c;
  color: #e74c3c;
  border-radius: 6px;
}
.cyber-btn-danger:hover {
  background: #e74c3c;
  color: white;
}

.admin-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.admin-main-inner {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  min-height: calc(100% - 40px);
  padding: 24px;
  box-shadow: inset 0 0 20px rgba(0,0,0,0.2);
}

/* Global scrollbar for admin */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}
::-webkit-scrollbar-thumb {
  background: rgba(74, 144, 226, 0.5);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(74, 144, 226, 0.8);
}
</style>

