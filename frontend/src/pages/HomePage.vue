<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()
const displayedText = ref('')
const fullText = '写点代码，听点歌，摸点鱼 ✨'
const recentArticles = ref<any[]>([])

// Typewriter effect
onMounted(() => {
  let i = 0
  const timer = setInterval(() => {
    if (i < fullText.length) {
      displayedText.value += fullText[i]
      i++
    } else {
      clearInterval(timer)
    }
  }, 100)
})

const gamePreviews = [
  { id: 'snake', name: '🐍 贪吃蛇', desc: '经典街机' },
  { id: 'flappy', name: '🐦 像素鸟', desc: '别撞管子' },
  { id: '2048', name: '🔢 2048', desc: '合并数字' },
  { id: 'puzzle15', name: '🧩 数字华容道', desc: '滑动拼图' },
]
</script>

<template>
  <div class="page-container">
    <!-- Hero -->
    <section class="text-center py-20 animate-fade-in-up">
      <h1 class="text-5xl md:text-6xl font-bold mb-4 gradient-text">
        维寒一念的小站
      </h1>
      <p class="text-xl md:text-2xl mb-6 text-[var(--text-secondary)] min-h-[2em]">
        {{ displayedText }}<span class="animate-pulse">|</span>
      </p>
      <p class="text-[var(--text-secondary)] mb-8 max-w-lg mx-auto leading-relaxed">
        一个热爱技术与二次元的开发者。<br />
        在这里记录思考、分享创造、偶尔摸鱼。
      </p>
      <div class="flex justify-center gap-4">
        <button @click="router.push('/blog')" class="glass-button primary">
          📝 逛逛博客
        </button>
        <button @click="router.push('/guestbook')" class="glass-button">
          💬 留个言吧
        </button>
      </div>
    </section>

    <!-- About Brief -->
    <section class="mb-16 animate-fade-in-up">
      <GlassCard title="👤 关于博主">
        <div class="text-[var(--text-secondary)] leading-relaxed space-y-2">
          <p>你好，我是<strong class="gradient-text">维寒一念</strong>。</p>
          <p>一个写 Java 和 Vue 的全栈开发者，偶尔也折腾 Python 和 AI。</p>
          <p>喜欢初音未来，喜欢研究有趣的技术，喜欢把想法变成代码。</p>
          <p class="italic opacity-70">这个网站是我的数字花园 —— 种着代码，养着灵感，开着脑洞。</p>
        </div>
      </GlassCard>
    </section>

    <!-- Recent Articles -->
    <section class="mb-16 animate-fade-in-up">
      <h2 class="section-title">📝 最近更新</h2>
      <div v-if="recentArticles.length === 0" class="glass-card p-8 text-center">
        <p class="text-[var(--text-secondary)]">还没有文章，敬请期待~</p>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <GlassCard v-for="article in recentArticles" :key="article.id">
          <span class="text-xs text-[var(--text-secondary)]">{{ article.date }}</span>
          <h3 class="font-bold mt-1 mb-2">{{ article.title }}</h3>
          <p class="text-sm text-[var(--text-secondary)]">{{ article.summary }}</p>
        </GlassCard>
      </div>
      <div class="text-center mt-6">
        <button @click="router.push('/blog')" class="glass-button text-sm">
          查看全部 →
        </button>
      </div>
    </section>

    <!-- Games Previews -->
    <section class="mb-16 animate-fade-in-up">
      <h2 class="section-title">🎮 摸鱼时间</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <GlassCard v-for="game in gamePreviews" :key="game.id" class="text-center cursor-pointer" @click="router.push('/games')">
          <div class="text-3xl mb-2">{{ game.name.slice(0, 2) }}</div>
          <div class="font-bold text-sm">{{ game.name.slice(3) }}</div>
          <div class="text-xs text-[var(--text-secondary)] mt-1">{{ game.desc }}</div>
        </GlassCard>
      </div>
      <div class="text-center mt-6">
        <button @click="router.push('/games')" class="glass-button text-sm">
          更多游戏 →
        </button>
      </div>
    </section>
  </div>
</template>
