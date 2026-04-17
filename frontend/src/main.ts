import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import './styles/site-ui.css'
import './styles/oj-theme.css'
import App from './App.vue'
import { router } from './router'
import { i18n } from './i18n'
import { useThemeStore } from './stores/theme'
import { useUserStore } from './stores/user'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
// 【主题】首屏前同步 localStorage → DOM，避免路由切换闪回日间
useThemeStore().initTheme()
useUserStore().hydrateFromStorage()
app.use(router).use(ElementPlus).use(i18n).mount('#app')
