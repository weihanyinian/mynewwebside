import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import './styles/design-tokens.css'
import './styles/site-ui.css'
import './styles/oj-theme.css'
import App from './App.vue'
import { router } from './router'
import { i18n } from './i18n'
import { useThemeStore } from './stores/theme'
import { useUserStore } from './stores/user'
import { initUmami } from './utils/umami'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
// 【主题】首屏前同步 localStorage → DOM，避免路由切换闪回日间
useThemeStore().initTheme()
useUserStore().hydrateFromStorage()
if (typeof requestIdleCallback === 'function') {
  requestIdleCallback(() => initUmami(), { timeout: 4000 })
} else {
  setTimeout(() => initUmami(), 1)
}
app.use(router).use(i18n).mount('#app')
