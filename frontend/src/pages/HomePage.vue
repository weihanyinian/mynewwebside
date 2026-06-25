<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '../components/GlassCard.vue'

const router = useRouter()

// ─── Typewriter ───
const displayedText = ref('')
const fullText = '写点代码，听点歌，摸点鱼 ✨'
onMounted(() => {
  let i = 0
  const timer = setInterval(() => {
    if (i < fullText.length) { displayedText.value += fullText[i]; i++ }
    else { clearInterval(timer) }
  }, 100)
})

// ─── Active section tracking ───
const activeSection = ref('home')
const sections = ['home', 'about', 'blog', 'guestbook', 'games']

function onScroll() {
  const viewH = window.innerHeight
  const scrollY = window.scrollY + viewH / 3
  for (const id of sections) {
    const el = document.getElementById(`section-${id}`)
    if (!el) continue
    const top = el.offsetTop; const bottom = top + el.offsetHeight
    if (scrollY >= top && scrollY < bottom) { activeSection.value = id; break }
  }
}
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// ─── Guestbook ───
interface Message { id: number; nickname: string; content: string; createdAt: string }
const messages = ref<Message[]>([])
const nickname = ref('')
const content = ref('')
const submitting = ref(false)

async function loadMsgs() {
  try {
    const res = await fetch('/api/guestbook')
    const data = await res.json()
    if (data.code === 200) messages.value = (data.data || []).slice(0, 4)
  } catch (e) { /* */ }
}
async function submitMsg() {
  if (!nickname.value.trim() || !content.value.trim()) return
  submitting.value = true
  try {
    const res = await fetch('/api/guestbook', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nickname: nickname.value, content: content.value })
    })
    if ((await res.json()).code === 200) { nickname.value = ''; content.value = ''; await loadMsgs() }
  } catch (e) { /* */ }
  finally { submitting.value = false }
}
onMounted(loadMsgs)

// ─── Games ───
const games = [
  { id: 'snake', icon: '🐍', name: '贪吃蛇', desc: '经典街机', color: '#62ea8a' },
  { id: 'flappy', icon: '🐦', name: '像素鸟', desc: '别撞管子', color: '#ea6262' },
  { id: '2048', icon: '🔢', name: '2048', desc: '合并数字', color: '#eacd62' },
  { id: 'puzzle15', icon: '🧩', name: '数字华容道', desc: '滑动拼图', color: '#a562ea' },
]
</script>

<template>
  <!-- ═══════ ❶ Hero ═══════ -->
  <section id="section-home" class="fullscreen-section">
    <GlassCard class="heavy !p-10 md:!p-14 !rounded-2xl max-w-2xl w-full mx-4 text-center animate-fade-in-up">
      <h1 class="text-5xl md:text-7xl font-bold mb-6 gradient-text drop-shadow-lg">
        维寒一念的小站
      </h1>
      <p class="text-xl md:text-2xl mb-6 text-[var(--text-secondary)] min-h-[2em] font-medium">
        {{ displayedText }}<span class="animate-pulse">|</span>
      </p>
      <p class="text-base md:text-lg text-[var(--text-secondary)] mb-10 max-w-xl mx-auto leading-relaxed font-medium">
        一个热爱技术与二次元的开发者。<br />
        在这里记录思考、分享创造、偶尔摸鱼。
      </p>
      <div class="flex flex-col sm:flex-row justify-center gap-4">
        <button @click="router.push('/blog')" class="glass-button primary text-lg px-10 py-3.5 rounded-xl shadow-lg">
          📝 逛逛博客
        </button>
        <button @click="() => document.getElementById('section-guestbook')?.scrollIntoView({behavior:'smooth'})" class="glass-button text-lg px-10 py-3.5 rounded-xl">
          💬 留个言吧
        </button>
      </div>
    </GlassCard>
  </section>

  <!-- ═══════ ❷ About ═══════ -->
  <section id="section-about" class="fullscreen-section">
    <div class="max-w-2xl w-full animate-fade-in-up">
      <GlassCard class="heavy max-w-2xl mx-auto !p-8 md:!p-10">
        <div class="flex flex-col md:flex-row items-center gap-8">
          <div class="shrink-0">
            <div class="w-32 h-32 md:w-40 md:h-40 rounded-full bg-gradient-to-br from-[#62a7ea] via-[#a58eea] to-[#e599f7] flex items-center justify-center text-6xl shadow-xl shadow-[#62a7ea40]">
              🎧
            </div>
          </div>
          <div class="text-left space-y-3">
            <p class="text-2xl md:text-3xl font-bold">
              你好，我是<span class="gradient-text">维寒一念</span> 👋
            </p>
            <p class="text-[var(--text-secondary)] leading-relaxed text-base font-medium">
              一个写 Java 和 Vue 的全栈开发者，偶尔也折腾 Python 和 AI。
              喜欢初音未来，喜欢研究有趣的技术，喜欢把想法变成代码。
            </p>
            <p class="text-sm text-[var(--text-muted)] italic">
              「这个网站是我的数字花园 —— 种着代码，养着灵感，开着脑洞。」
            </p>
            <div class="flex gap-3 pt-2">
              <a href="https://github.com/weihanyinian" target="_blank" class="glass-button !px-4 !py-2 text-sm !rounded-lg font-medium">🔗 GitHub</a>
              <a href="mailto:1012308753@qq.com" class="glass-button !px-4 !py-2 text-sm !rounded-lg font-medium">✉️ 邮箱</a>
            </div>
          </div>
        </div>
      </GlassCard>
    </div>
  </section>

  <!-- ═══════ ❸ Blog ═══════ -->
  <section id="section-blog" class="fullscreen-section">
    <div class="max-w-3xl w-full animate-fade-in-up">
      <GlassCard class="heavy !p-10 md:!p-12 text-center max-w-xl mx-auto">
        <p class="text-5xl md:text-6xl mb-4">📝</p>
        <h2 class="text-3xl font-bold mb-4 gradient-text">博客</h2>
        <p class="text-[var(--text-secondary)] mb-8 leading-relaxed font-medium">
          还没有文章，博主正在努力写作中...<br />
          这里会记录技术笔记、项目心得和生活碎碎念。
        </p>
        <button @click="router.push('/blog')" class="glass-button primary text-base px-8 py-3">
          进入博客 →
        </button>
      </GlassCard>
    </div>
  </section>

  <!-- ═══════ ❹ Guestbook ═══════ -->
  <section id="section-guestbook" class="fullscreen-section">
    <div class="max-w-2xl w-full animate-fade-in-up">
      <h2 class="text-3xl font-bold mb-6 text-center gradient-text">💬 留言板</h2>

      <!-- Form -->
      <GlassCard class="heavy !p-6 mb-4">
        <input v-model="nickname" class="glass-input !text-base !font-medium mb-3" placeholder="你的昵称" />
        <textarea v-model="content" class="glass-input !text-base !font-medium mb-3" rows="2" placeholder="说点什么吧..." />
        <button @click="submitMsg" :disabled="submitting" class="glass-button primary text-sm w-full">
          {{ submitting ? '提交中...' : '💬 发表留言' }}
        </button>
      </GlassCard>

      <!-- Messages list -->
      <div class="space-y-3 max-h-64 overflow-y-auto">
        <GlassCard v-for="msg in messages" :key="msg.id" class="!p-4">
          <div class="flex items-center gap-2 mb-1.5">
            <span class="font-bold text-sm text-[var(--text-primary)]">{{ msg.nickname }}</span>
            <span class="text-xs text-[var(--text-muted)]">{{ new Date(msg.createdAt).toLocaleDateString('zh-CN') }}</span>
          </div>
          <p class="text-sm text-[var(--text-secondary)] font-medium whitespace-pre-wrap">{{ msg.content }}</p>
        </GlassCard>
        <GlassCard v-if="messages.length === 0" class="!p-6 text-center">
          <p class="text-sm text-[var(--text-muted)]">还没有留言，来做第一个留言的人吧~</p>
        </GlassCard>
      </div>

      <div class="text-center mt-4">
        <button @click="router.push('/guestbook')" class="glass-button text-sm">查看全部留言 →</button>
      </div>
    </div>
  </section>

  <!-- ═══════ ❺ Games ═══════ -->
  <section id="section-games" class="fullscreen-section">
    <div class="max-w-3xl w-full animate-fade-in-up">
      <h2 class="text-3xl font-bold mb-8 text-center gradient-text">🎮 摸鱼时间</h2>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-2xl mx-auto">
        <GlassCard
          v-for="g in games" :key="g.id"
          class="text-center cursor-pointer !p-5 hover:!border-[2px]"
          :style="{ '--hover-border': g.color }"
          @click="router.push('/games')"
        >
          <div class="text-4xl mb-2 drop-shadow-md">{{ g.icon }}</div>
          <div class="font-bold text-sm mb-1 text-[var(--text-primary)]">{{ g.name }}</div>
          <div class="text-xs text-[var(--text-muted)]">{{ g.desc }}</div>
        </GlassCard>
      </div>
      <div class="text-center mt-8">
        <button @click="router.push('/games')" class="glass-button text-sm font-medium">更多游戏 →</button>
      </div>
    </div>
  </section>
</template>
