import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
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
})
