<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '../../stores/theme'

const router = useRouter()
const themeStore = useThemeStore()
const isScrolled = ref(false)

function onScroll() {
  isScrolled.value = window.scrollY > 50
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const navItems = [
  { path: '/', label: '🏠 首页' },
  { path: '/blog', label: '📝 博客' },
  { path: '/tools', label: '🛠️ 工具箱' },
  { path: '/guestbook', label: '💬 留言板' },
  { path: '/games', label: '🎮 小游戏' },
  { path: '/about', label: '👤 关于我' },
]
</script>

<template>
  <nav
    class="glass-nav fixed top-0 left-0 right-0 z-50 transition-all duration-300"
    :class="isScrolled ? 'py-2' : 'py-3'"
  >
    <div class="max-w-6xl mx-auto px-6 flex items-center justify-between">
      <!-- Logo -->
      <router-link to="/" class="text-xl font-bold gradient-text no-underline">
        维寒一念的小站
      </router-link>

      <!-- Nav Links -->
      <div class="hidden md:flex items-center gap-1">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="px-3 py-2 rounded-lg text-sm transition-all duration-200 no-underline"
          :class="$route.path === item.path
            ? 'text-white bg-[#62a7ea33]'
            : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--glass-bg)]'"
        >
          {{ item.label }}
        </router-link>
      </div>

      <!-- Theme Toggle + Mobile Menu -->
      <div class="flex items-center gap-3">
        <button
          @click="themeStore.toggle()"
          class="glass-button !p-2 !rounded-full text-lg"
          :title="themeStore.isDark ? '切换日间模式' : '切换夜间模式'"
        >
          {{ themeStore.isDark ? '☀️' : '🌙' }}
        </button>

        <!-- Mobile: hamburger -->
        <button class="md:hidden glass-button !p-2 !rounded-full">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
      </div>
    </div>
  </nav>
</template>
