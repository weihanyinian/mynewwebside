<script setup lang="ts">
import { useRouter } from 'vue-router'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4">
    <GlassCard class="text-center !p-10 max-w-md w-full">
      <div class="text-8xl mb-6">🔮</div>
      <h1 class="text-4xl font-bold gradient-text mb-4">404</h1>
      <p class="text-[var(--text-secondary)] text-lg mb-2">这个页面去二次元了</p>
      <p class="text-[var(--text-muted)] text-sm mb-8">请检查链接是否正确，或者返回首页</p>
      <div class="flex justify-center gap-3">
        <button @click="router.back()" class="glass-button text-sm">← 返回上页</button>
        <button @click="router.push('/')" class="glass-button primary text-sm">🏠 返回首页</button>
      </div>
    </GlassCard>
  </div>
</template>
