<script setup lang="ts">
import { useRouter } from 'vue-router'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()

const games = [
  { id: 'snake', name: '🐍 贪吃蛇', desc: '经典街机，看看你能吃多长', tech: 'Canvas', done: true },
  { id: '2048', name: '🔢 2048', desc: '合并数字，冲击 2048', tech: 'Vue', done: true },
  { id: 'schulte', name: '🔢 舒尔特方格', desc: '按顺序找数字，测注意力', tech: 'Vue', done: true },
  { id: 'puzzle15', name: '🧩 数字华容道', desc: '滑动拼图，挑战最强大脑', tech: 'Vue', done: false },
  { id: 'minesweeper', name: '💣 扫雷', desc: '经典 Windows 扫雷', tech: 'Vue', done: false },
  { id: 'reaction', name: '🎯 反应力测试', desc: '测测你的反应速度', tech: 'Vue', done: false },
]
</script>

<template>
  <div class="page-container">
    <button @click="router.push('/')" class="glass-button text-sm mb-6">← 返回首页</button>
    <h1 class="text-3xl font-bold mb-3 gradient-text">🎮 摸鱼小游戏</h1>
    <p class="text-[var(--text-secondary)] mb-8 leading-relaxed">
      学累了？来摸会儿鱼 🐟<br />
      友情提醒：适度游戏益脑，沉迷游戏伤身。
    </p>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <GlassCard v-for="game in games" :key="game.id" class="cursor-pointer relative" @click="router.push(`/games/${game.id}`)">
        <div class="text-4xl mb-3">{{ game.name.slice(0, 2) }}</div>
        <h3 class="font-bold mb-1">{{ game.name.slice(3) }}</h3>
        <p class="text-sm text-[var(--text-secondary)] mb-2">{{ game.desc }}</p>
        <div class="flex items-center gap-2">
          <span class="text-xs px-2 py-0.5 rounded-full bg-[var(--glass-bg)] text-[var(--text-muted)]">{{ game.tech }}</span>
          <span v-if="game.done" class="text-xs px-2 py-0.5 rounded-full bg-green-500/20 text-green-400 border border-green-500/30">可玩</span>
          <span v-else class="text-xs px-2 py-0.5 rounded-full bg-yellow-500/10 text-yellow-400/80 border border-yellow-500/20">待开发</span>
        </div>
      </GlassCard>
    </div>
  </div>
</template>
