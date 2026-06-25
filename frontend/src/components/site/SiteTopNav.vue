<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '../../stores/theme'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const isScrolled = ref(false)

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
  if (route.path !== '/') {
    return route.path === item.path
  }
  // On homepage, use section-based active state for homepage items
  if (item.section) {
    return activeSection.value === item.section
  }
  return route.path === item.path
}

function navClick(item: typeof navItems[0]) {
  if (item.section && route.path === '/') {
    const el = document.getElementById(`section-${item.section}`)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
      return
    }
  }
  router.push(item.path)
}
</script>

<template>
  <nav
    class="glass-nav fixed top-0 left-0 right-0 z-50 transition-all duration-300"
    :class="isScrolled ? 'py-2' : 'py-3'"
  >
    <div class="max-w-6xl mx-auto px-4 md:px-6 flex items-center justify-between">
      <!-- Logo -->
      <a
        href="#section-home"
        @click.prevent="router.push('/')"
        class="flex items-center gap-2 no-underline group"
      >
        <div class="w-7 h-7 rounded-full bg-gradient-to-br from-[#62a7ea] to-[#a58eea] flex items-center justify-center text-xs font-bold text-white">
          寒
        </div>
        <span class="text-lg font-bold gradient-text hidden sm:inline">维寒一念的小站</span>
      </a>

      <!-- Nav Links -->
      <div class="hidden md:flex items-center gap-0.5">
        <button
          v-for="item in navItems"
          :key="item.id"
          @click="navClick(item)"
          class="relative px-4 py-2 rounded-xl text-sm font-medium transition-all duration-300 border outline-none cursor-pointer"
          :class="isActive(item)
            ? 'text-white bg-[#62a7ea30] border-[#62a7ea] shadow-[0_0_14px_rgba(98,167,234,0.4)] scale-105'
            : 'border-[var(--glass-border)] text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--glass-bg)] hover:border-[#62a7ea50]'"
        >
          {{ item.label }}
        </button>
      </div>

      <!-- Theme Toggle -->
      <div class="flex items-center gap-3">
        <button
          @click="themeStore.toggle()"
          class="glass-button !p-2 !rounded-full text-lg"
          :title="themeStore.isDark ? '切换日间模式' : '切换夜间模式'"
        >
          {{ themeStore.isDark ? '☀️' : '🌙' }}
        </button>

        <!-- Mobile hamburger -->
        <button class="md:hidden glass-button !p-2 !rounded-full">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
      </div>
    </div>
  </nav>
</template>
