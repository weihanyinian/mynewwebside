/**
 * 全站主题（Pinia）
 * - 首次访问：跟随系统 prefers-color-scheme（手机/浏览器暗黑设置）
 * - 用户点击顶栏切换：写入 theme-user，永久覆盖系统直至清除（与旧版 theme 键兼容迁移）
 * - 跟随系统时监听 matchMedia('change')，全页同步
 */
import { defineStore } from 'pinia'

const STORAGE_THEME = 'theme'
const STORAGE_LEGACY = 'site-theme'
/** 存在且为 light|dark 表示用户手动选择，优先级高于系统 */
const STORAGE_THEME_USER = 'theme-user'

let mediaQuery: MediaQueryList | null = null
let mediaHandler: (() => void) | null = null

export function syncDocumentTheme(isDark: boolean) {
  const root = document.documentElement
  const mode = isDark ? 'dark' : 'light'

  if (isDark) {
    root.style.setProperty(
      '--bg-gradient',
      'linear-gradient(145deg, rgba(22, 28, 42, 0.97) 0%, rgba(32, 26, 48, 0.94) 42%, rgba(18, 36, 52, 0.96) 100%)',
    )
    root.style.setProperty('--text-color', '#eaf8ff')
    root.style.setProperty('--glass-bg', 'rgba(255, 255, 255, 0.12)')
    root.style.setProperty('--glass-border', 'rgba(255, 255, 255, 0.45)')
    root.style.setProperty('--glass-shadow', '0 8px 32px rgba(102, 217, 255, 0.18)')
    root.style.setProperty('--blog-on-glass', '#f0fbff')
    root.style.setProperty('--blog-on-glass-muted', 'rgba(240, 251, 255, 0.82)')
  } else {
    root.style.setProperty(
      '--bg-gradient',
      'linear-gradient(135deg, rgba(230, 238, 245, 0.85) 0%, rgba(200, 218, 235, 0.92) 100%)',
    )
    root.style.setProperty('--text-color', '#2c3e50')
    root.style.setProperty('--glass-bg', 'rgba(255, 255, 255, 0.4)')
    root.style.setProperty('--glass-border', 'rgba(255, 255, 255, 0.6)')
    root.style.setProperty('--glass-shadow', '0 8px 32px 0 rgba(74, 144, 226, 0.15)')
    root.style.setProperty('--blog-on-glass', '#1a3a52')
    root.style.setProperty('--blog-on-glass-muted', 'rgba(26, 58, 82, 0.75)')
  }

  root.dataset.theme = mode
  root.classList.toggle('dark-mode', isDark)
  document.body.classList.toggle('dark-mode', isDark)
}

function readSystemDark(): boolean {
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDarkMode: false,
    /** true：跟随系统；false：使用用户手动选择的 isDarkMode */
    followSystem: true,
  }),
  actions: {
    /** 绑定/解绑系统主题监听 */
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

    /**
     * 应用启动时调用。
     * - 无 theme-user：若存在旧 theme 键则视为历史手动选择并迁移；否则跟随系统。
     */
    initTheme() {
      let user = localStorage.getItem(STORAGE_THEME_USER) as 'light' | 'dark' | null

      if (user !== 'light' && user !== 'dark') {
        const legacy = localStorage.getItem(STORAGE_THEME) ?? localStorage.getItem(STORAGE_LEGACY)
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
      localStorage.setItem(STORAGE_LEGACY, this.isDarkMode ? 'dark' : 'light')

      this._attachSystemListener()
    },

    /** 用户手动切换：不再跟随系统，持久化 */
    toggleTheme() {
      this.followSystem = false
      this.isDarkMode = !this.isDarkMode
      const token = this.isDarkMode ? 'dark' : 'light'
      localStorage.setItem(STORAGE_THEME_USER, token)
      localStorage.setItem(STORAGE_THEME, token)
      localStorage.setItem(STORAGE_LEGACY, token)
      syncDocumentTheme(this.isDarkMode)
      this._attachSystemListener()
    },
  },
})
