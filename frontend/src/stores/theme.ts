/**
 * 全站主题（Pinia）：日间/夜间跨页面持久化。
 * - localStorage 主键 `theme`（'light' | 'dark'），并同步旧键 `site-theme` 以兼容历史数据
 * - 同步设置 document.documentElement 的 data-theme、CSS 变量，以及 html/body 的 .dark-mode 类
 */
import { defineStore } from 'pinia'

const STORAGE_THEME = 'theme'
const STORAGE_LEGACY = 'site-theme'

/** 将主题写入 DOM（供博客玻璃态、OJ 浅色覆盖 data-theme=light 等逻辑使用） */
export function syncDocumentTheme(isDark: boolean) {
  const root = document.documentElement
  const mode = isDark ? 'dark' : 'light'

  if (isDark) {
    root.style.setProperty(
      '--bg-gradient',
      'linear-gradient(135deg, rgba(20, 38, 51, 0.96) 0%, rgba(18, 28, 40, 0.96) 100%)',
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

export const useThemeStore = defineStore('theme', {
  state: () => ({
    /** false = 日间，true = 夜间 */
    isDarkMode: false,
  }),
  actions: {
    /** 应用启动时调用：读 localStorage 并同步 DOM */
    initTheme() {
      const raw = localStorage.getItem(STORAGE_THEME) ?? localStorage.getItem(STORAGE_LEGACY)
      this.isDarkMode = raw === 'dark'
      syncDocumentTheme(this.isDarkMode)
    },

    /** 切换主题并持久化 */
    toggleTheme() {
      this.isDarkMode = !this.isDarkMode
      const token = this.isDarkMode ? 'dark' : 'light'
      localStorage.setItem(STORAGE_THEME, token)
      localStorage.setItem(STORAGE_LEGACY, token)
      syncDocumentTheme(this.isDarkMode)
    },
  },
})
