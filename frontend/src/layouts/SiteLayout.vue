<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const keyword = ref<string>((route.query.keyword as string) || '')

function onSearch() {
  router.push({ path: '/blog', query: keyword.value ? { keyword: keyword.value } : {} })
}
</script>

<template>
  <div class="site-root">
    <header class="site-header">
      <div class="site-header__inner">
        <div class="brand" @click="router.push('/')">MyWebSide</div>
        <nav class="nav">
          <a class="nav__link" @click="router.push('/blog')">文章</a>
          <a class="nav__link" @click="router.push('/categories')">分类</a>
          <a class="nav__link" @click="router.push('/tags')">标签</a>
          <a class="nav__link" @click="router.push('/admin/login')">管理</a>
        </nav>
        <div class="search">
          <el-input v-model="keyword" placeholder="搜索文章..." clearable @keyup.enter="onSearch" />
        </div>
      </div>
    </header>

    <main class="site-main">
      <slot />
    </main>

    <footer class="site-footer">
      <div class="site-footer__inner">© {{ new Date().getFullYear() }} MyWebSide</div>
    </footer>
  </div>
</template>

<style scoped>
.site-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 10;
  backdrop-filter: blur(14px);
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.site-header__inner {
  max-width: 1080px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: auto 1fr 280px;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
}

.brand {
  font-weight: 800;
  letter-spacing: 0.2px;
  cursor: pointer;
  user-select: none;
}

.nav {
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: center;
}

.nav__link {
  color: rgba(0, 0, 0, 0.72);
  text-decoration: none;
  font-weight: 600;
  cursor: pointer;
}

.nav__link:hover {
  color: rgba(0, 0, 0, 0.92);
}

.site-main {
  flex: 1;
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  padding: 22px 16px 44px;
}

.site-footer {
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  color: rgba(0, 0, 0, 0.58);
}

.site-footer__inner {
  max-width: 1080px;
  margin: 0 auto;
  padding: 18px 16px;
}

@media (max-width: 780px) {
  .site-header__inner {
    grid-template-columns: 1fr;
  }
  .nav {
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  .search {
    width: 100%;
  }
}
</style>

