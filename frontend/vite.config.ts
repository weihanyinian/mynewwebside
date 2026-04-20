import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  /** 后端未占用 8080 时可在 .env.development.local 中设置 VITE_PROXY_TARGET=http://127.0.0.1:8081 */
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://127.0.0.1:8080'

  return {
  plugins: [vue()],
  /** 本地开发：浏览器请求 /api → 转发到 Spring Boot，避免跨域 */
  server: {
    proxy: {
      '/api': {
        target: proxyTarget,
        changeOrigin: true,
      },
    },
  },
  build: {
    chunkSizeWarningLimit: 800,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/')) {
            // 大依赖独立 chunk，避免全部打进主 bundle
            if (id.includes('monaco-editor')) return 'monaco-editor'
            if (id.includes('element-plus')) return 'element-plus'
            if (id.includes('@codemirror') || id.includes('codemirror')) return 'codemirror'
            if (id.includes('photoswipe')) return 'photoswipe'
            if (id.includes('markdown-it') || id.includes('highlight.js')) return 'markdown'
            // Vue 全家桶核心
            if (
              id.includes('node_modules/vue/') ||
              id.includes('node_modules/@vue/') ||
              id.includes('node_modules/vue-router/') ||
              id.includes('node_modules/pinia/')
            ) return 'vue-core'
            // 其余 node_modules 统一放 vendor
            return 'vendor'
          }
        },
      },
    },
  },
  }
})
