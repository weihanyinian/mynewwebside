<script setup lang="ts">
import { useRouter } from 'vue-router'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()

const tools = [
  { id: 'reaction', name: '⏱️ 反应力测试', desc: '看到绿色就点，测测你的反应速度' },
  { id: 'cps', name: '🖱️ CPS 测试', desc: 'Click Per Second，测手速' },
  { id: 'pomodoro', name: '🍅 番茄钟', desc: '专注 25 分钟，休息 5 分钟' },
  { id: 'schulte', name: '🧠 舒尔特方格', desc: '专注力训练经典工具' },
  { id: 'mbti', name: '🧬 MBTI 测试', desc: '简易版性格测试' },
]
</script>

<template>
  <div class="page-container">
    <h1 class="text-3xl font-bold mb-3 gradient-text">🛠️ 在线工具箱</h1>
    <p class="text-[var(--text-secondary)] mb-8">一些实用小工具，收好不谢~</p>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <GlassCard v-for="tool in tools" :key="tool.id" class="cursor-pointer" @click="router.push(`/tools/${tool.id}`)">
        <div class="text-3xl mb-2">{{ tool.name.slice(0, 2) }}</div>
        <h3 class="font-bold mb-1">{{ tool.name.slice(3) }}</h3>
        <p class="text-sm text-[var(--text-secondary)]">{{ tool.desc }}</p>
      </GlassCard>
    </div>
  </div>
</template>
