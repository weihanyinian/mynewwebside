<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getToken } from '../utils/token'

const router = useRouter()

const isLoggedIn = computed(() => !!getToken())

function goHome(hash: string) {
  router.push({ path: '/', hash: hash })
}
</script>

<template>
  <div class="site-root">
    <nav class="glass-nav">
      <div class="nav-inner">
        <div class="logo" @click="router.push('/')">维寒一念的小站</div>
        <div class="links">
          <a @click="goHome('#about')">关于</a>
          <a @click="goHome('#skills')">技能</a>
          <a @click="goHome('#works')">作品</a>
          <a @click="goHome('#contact')">联系</a>
          <a @click="router.push('/message')">留言墙</a>
          <a @click="router.push('/blog')" class="active">前往博客</a>
          <a v-if="isLoggedIn" @click="router.push('/admin')">管理后台</a>
          <a v-else @click="router.push('/admin/login')">管理后台</a>
        </div>
      </div>
    </nav>

    <main class="site-main">
      <slot />
    </main>

    <footer class="site-footer">
      <div class="site-footer__inner">
        <p>© 2026 维寒一念 | MyWebSide</p>
        <div class="footer-links">
          <a href="https://github.com/weihanyinian" target="_blank" title="GitHub">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z" />
            </svg>
          </a>
          <a href="mailto:1012308753@qq.com" title="Email">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z" />
            </svg>
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.site-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.glass-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
}

.nav-inner {
  max-width: 1080px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  cursor: pointer;
}

.links {
  display: flex;
  gap: 24px;
  align-items: center;
}

.links a {
  cursor: pointer;
  font-weight: 600;
  color: var(--text-color);
  transition: color 0.3s;
  text-decoration: none;
}

.links a:hover, .links a.active {
  color: var(--primary-color);
}

.site-main {
  flex: 1;
  max-width: 1080px;
  width: 100%;
  margin: 0 auto;
  padding: 40px 16px;
}

.site-footer {
  border-top: 1px solid var(--glass-border);
  background: var(--glass-bg);
  backdrop-filter: blur(8px);
  color: var(--text-color);
}

.site-footer__inner {
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.site-footer__inner p {
  margin: 0;
  font-weight: 500;
}

.footer-links {
  display: flex;
  gap: 16px;
}

.footer-links a {
  color: var(--text-color);
  transition: all 0.3s;
  opacity: 0.7;
}

.footer-links a:hover {
  color: var(--primary-color);
  opacity: 1;
  transform: translateY(-2px);
}

@media (max-width: 780px) {
  .links a:not(.active) {
    display: none;
  }
}
</style>

