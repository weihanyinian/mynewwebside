<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useThemeStore } from '../../stores/theme'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const themeStore = useThemeStore()
const isScrolled = ref(false)
const mobileOpen = ref(false)

function onScroll() {
  isScrolled.value = window.scrollY > 80
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const navItems = [
  { id: 'home', path: '/', label: '🏠 首页', section: 'home' },
  { id: 'about', path: '/', label: '👤 关于', section: 'about' },
  { id: 'blog', path: '/', label: '📝 博客', section: 'blog' },
  { id: 'guestbook', path: '/', label: '💬 留言', section: 'guestbook' },
  { id: 'games', path: '/', label: '🎮 游戏', section: 'games' },
]

// Track which section is visible on homepage
const activeSection = ref('home')
function onHomeScroll() {
  if (route.path !== '/') return
  const viewH = window.innerHeight
  const scrollY = window.scrollY + viewH / 3
  const sections = ['home', 'about', 'blog', 'guestbook', 'games']
  for (const id of sections) {
    const el = document.getElementById(`section-${id}`)
    if (!el) continue
    const top = el.offsetTop
    const bottom = top + el.offsetHeight
    if (scrollY >= top && scrollY < bottom) {
      activeSection.value = id
      break
    }
  }
}

onMounted(() => {
  window.addEventListener('scroll', onHomeScroll, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', onHomeScroll)
})

function isActive(item: typeof navItems[0]) {
  if (route.path !== '/') return route.path === item.path
  if (item.section) return activeSection.value === item.section
  return route.path === item.path
}

function navClick(item: typeof navItems[0]) {
  mobileOpen.value = false
  if (item.section && route.path === '/') {
    const el = document.getElementById(`section-${item.section}`)
    if (el) { el.scrollIntoView({ behavior: 'smooth' }); return }
  }
  router.push(item.path)
}
</script>

<template>
  <nav class="glass-nav transition-all duration-300 py-3">
    <div class="max-w-6xl mx-auto px-4 md:px-6 flex items-center justify-between">
      <!-- Logo -->
      <a href="#section-home" @click.prevent="router.push('/')" class="flex items-center gap-2 no-underline group">
        <div class="w-8 h-8 rounded-full bg-gradient-to-br from-[#62a7ea] to-[#a58eea] flex items-center justify-center text-sm font-bold text-white">寒</div>
        <span class="text-xl font-bold gradient-text hidden sm:inline">维寒一念的小站</span>
      </a>

      <!-- Desktop Nav -->
      <div class="hidden md:flex items-center gap-0.5">
        <button v-for="item in navItems" :key="item.id" @click="navClick(item)"
          class="relative px-4 py-2.5 rounded-xl text-base font-medium transition-all duration-300 border outline-none cursor-pointer"
          :class="isActive(item)
            ? 'text-white bg-[#62a7ea30] border-[#62a7ea] shadow-[0_0_14px_rgba(98,167,234,0.4)] scale-105'
            : 'border-[var(--glass-border)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--glass-bg)] hover:border-[#62a7ea50]'">
          {{ item.label }}
        </button>
      </div>

      <!-- Right: login + theme + hamburger -->
      <div class="flex items-center gap-2">
        <!-- Login / User button (desktop) -->
        <button v-if="auth.isAdmin" @click="router.push('/admin')"
          class="hidden md:inline-flex glass-button text-sm !py-2 !px-3 font-medium">
          ⚙️ {{ auth.username }}
        </button>
        <button v-else-if="auth.isLoggedIn" @click="auth.logout(); router.push('/')"
          class="hidden md:inline-flex glass-button text-sm !py-2 !px-3 font-medium">
          👤 {{ auth.username }}
        </button>
        <button v-else @click="router.push('/login')"
          class="hidden md:inline-flex glass-button text-sm !py-2 !px-3 font-medium">
          🔑 登录
        </button>

        <button @click="themeStore.toggle()" class="glass-button !p-2 !rounded-full text-lg"
          :title="themeStore.isDark ? '切换日间模式' : '切换夜间模式'">
          {{ themeStore.isDark ? '☀️' : '🌙' }}
        </button>

        <!-- Mobile hamburger -->
        <button @click="mobileOpen = !mobileOpen" class="md:hidden glass-button !p-2 !rounded-full">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path v-if="!mobileOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Mobile Drawer -->
    <transition name="slide">
      <div v-if="mobileOpen" class="md:hidden absolute top-full left-0 right-0 glass-card mx-4 mt-2 !p-4 !rounded-2xl">
        <div class="flex flex-col gap-1">
          <button v-for="item in navItems" :key="item.id" @click="navClick(item)"
            class="w-full text-left px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200"
            :class="isActive(item)
              ? 'text-white bg-[#62a7ea20] border border-[#62a7ea50]'
              : 'text-[var(--text-secondary)] hover:bg-white/5'">
            {{ item.label }}
          </button>
          <hr class="my-2 border-[var(--border-color)]" />
          <button @click="mobileOpen = false; router.push('/tools')" class="w-full text-left px-4 py-3 rounded-xl text-sm text-[var(--text-secondary)] hover:bg-white/5">🛠 工具箱</button>
          <button @click="mobileOpen = false; router.push('/about')" class="w-full text-left px-4 py-3 rounded-xl text-sm text-[var(--text-secondary)] hover:bg-white/5">ℹ️ 关于我</button>
          <button v-if="auth.isAdmin" @click="mobileOpen = false; router.push('/admin')" class="w-full text-left px-4 py-3 rounded-xl text-sm text-[var(--text-secondary)] hover:bg-white/5">⚙️ 管理后台 ({{ auth.username }})</button>
          <button v-else-if="auth.isLoggedIn" @click="auth.logout(); mobileOpen = false; router.push('/')" class="w-full text-left px-4 py-3 rounded-xl text-sm text-[var(--text-secondary)] hover:bg-white/5">👤 {{ auth.username }} · 退出</button>
          <button v-else @click="mobileOpen = false; router.push('/login')" class="w-full text-left px-4 py-3 rounded-xl text-sm text-[var(--text-secondary)] hover:bg-white/5">🔑 登录</button>
        </div>
      </div>
    </transition>
  </nav>
</template>

<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
