/**
 * 全站主题（Pinia）
 * - 所有颜色 Token 统一定义在 style.css（:root / :root[data-theme='dark']）和 design-tokens.css
 * - 本 Store 只负责：切换 data-theme 属性 + dark-mode class + 持久化 + 监听系统主题变化
 */
import { defineStore } from 'pinia'

const STORAGE_THEME = 'theme'
const STORAGE_THEME_USER = 'theme-user'

let mediaQuery: MediaQueryList | null = null
let mediaHandler: (() => void) | null = null

export function syncDocumentTheme(isDark: boolean) {
  const root = document.documentElement
  root.dataset.theme = isDark ? 'dark' : 'light'
  root.classList.toggle('dark-mode', isDark)
  document.body.classList.toggle('dark-mode', isDark)
}

function readSystemDark(): boolean {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDarkMode: false,
    followSystem: true,
  }),
  actions: {
    _attachSystemListener() {
      if (mediaHandler && mediaQuery) {
        mediaQuery.removeEventListener('change', mediaHandler)
        mediaHandler = null
        mediaQuery = null
      }
      if (!this.followSystem) return

      mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaHandler = () => {
        if (this.followSystem) {
          this.isDarkMode = mediaQuery!.matches
          syncDocumentTheme(this.isDarkMode)
        }
      }
      mediaQuery.addEventListener('change', mediaHandler)
    },

    initTheme() {
      let user = localStorage.getItem(STORAGE_THEME_USER) as 'light' | 'dark' | null

      if (user !== 'light' && user !== 'dark') {
        const legacy = localStorage.getItem('site-theme')
        if (legacy === 'light' || legacy === 'dark') {
          user = legacy
          localStorage.setItem(STORAGE_THEME_USER, user)
        }
      }

      if (user === 'light' || user === 'dark') {
        this.followSystem = false
        this.isDarkMode = user === 'dark'
      } else {
        this.followSystem = true
        this.isDarkMode = readSystemDark()
      }

      syncDocumentTheme(this.isDarkMode)
      localStorage.setItem(STORAGE_THEME, this.isDarkMode ? 'dark' : 'light')
      this._attachSystemListener()
    },

    toggleTheme() {
      this.followSystem = false
      this.isDarkMode = !this.isDarkMode
      const token = this.isDarkMode ? 'dark' : 'light'
      localStorage.setItem(STORAGE_THEME_USER, token)
      localStorage.setItem(STORAGE_THEME, token)
      syncDocumentTheme(this.isDarkMode)
      this._attachSystemListener()
    },
  },
})
