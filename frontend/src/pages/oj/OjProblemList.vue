<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchOjProblems, type ProblemListItem } from '../../api/oj'

const router = useRouter()
const loading = ref(true)
const err = ref('')
const items = ref<ProblemListItem[]>([])
const q = ref('')
const difficulty = ref('')

const difficultyOptions = computed(() => {
  const s = new Set<string>()
  for (const it of items.value) s.add(it.difficulty)
  return Array.from(s).sort()
})

const filtered = computed(() => {
  const qq = q.value.trim().toLowerCase()
  const df = difficulty.value
  return items.value.filter((p) => {
    if (df && p.difficulty !== df) return false
    if (!qq) return true
    return p.title.toLowerCase().includes(qq)
  })
})

onMounted(async () => {
  loading.value = true
  err.value = ''
  try {
    items.value = await fetchOjProblems()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
})

function goDetail(id: string) {
  router.push(`/tools/oj/p/${id}`)
}

function badgeMode(m: string) {
  if (m === 'LEETCODE') return '力扣'
  return 'ACM'
}
</script>

<template>
  <div class="oj-list">
    <header class="glass-card hero">
      <h1 class="title">在线判题 · OJ</h1>
      <p class="subtitle">C / C++ / Java / Python · Judge0 CE · ACM & 力扣样例模式</p>
    </header>

    <div class="glass-card toolbar">
      <input v-model="q" class="input" type="search" placeholder="搜索标题…" />
      <select v-model="difficulty" class="select">
        <option value="">全部难度</option>
        <option v-for="d in difficultyOptions" :key="d" :value="d">{{ d }}</option>
      </select>
      <button type="button" class="site-pill site-pill--active" @click="router.push('/tools/oj/submissions')">我的提交</button>
    </div>

    <p v-if="loading" class="hint">加载中…</p>
    <p v-else-if="err" class="err">{{ err }}</p>

    <ul v-else class="grid">
      <li v-for="p in filtered" :key="p.id" class="glass-card item" @click="goDetail(p.id)">
        <div class="item-top">
          <span class="diff">{{ p.difficulty }}</span>
          <span class="mode">{{ badgeMode(p.judgeMode) }}</span>
        </div>
        <h2 class="item-title">{{ p.title }}</h2>
        <p class="item-id">{{ p.id }}</p>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
  border-radius: 16px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 30, 60, 0.35);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 36px rgba(102, 217, 255, 0.28);
}

.hero {
  padding: 1.25rem 1.5rem;
  margin-bottom: 1rem;
}

.title {
  margin: 0 0 0.35rem;
  font-size: 1.75rem;
  font-weight: 800;
}

.subtitle {
  margin: 0;
  opacity: 0.92;
  font-size: 0.95rem;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  margin-bottom: 1rem;
}

.input,
.select {
  flex: 1 1 200px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  background: rgba(0, 40, 80, 0.15);
  color: #fff;
  padding: 0.55rem 0.75rem;
  outline: none;
}

.input::placeholder {
  color: rgba(255, 255, 255, 0.55);
}

.hint,
.err {
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 30, 60, 0.35);
}

.err {
  color: #ffd0d0;
}

.grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.item {
  padding: 1rem 1.15rem;
  cursor: pointer;
}

.item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.8rem;
}

.diff {
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  background: rgba(102, 217, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.45);
}

.mode {
  opacity: 0.9;
}

.item-title {
  margin: 0 0 0.35rem;
  font-size: 1.1rem;
}

.item-id {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.75;
}
</style>
