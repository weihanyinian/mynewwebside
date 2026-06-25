import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(true) // Default dark mode

  // Load from localStorage
  const saved = localStorage.getItem('theme')
  if (saved === 'light') {
    isDark.value = false
  }

  function applyTheme() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  }

  function toggle() {
    isDark.value = !isDark.value
    applyTheme()
  }

  // Apply on init
  applyTheme()

  return { isDark, toggle }
})
