<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchMyOjSubmissions, type OjSubmissionRow } from '../../api/oj'

const router = useRouter()
const loading = ref(true)
const err = ref('')
const page = ref(0)
const size = ref(20)
const total = ref(0)
const items = ref<OjSubmissionRow[]>([])

async function load() {
  loading.value = true
  err.value = ''
  try {
    const data = await fetchMyOjSubmissions({ page: page.value, size: size.value })
    items.value = data.items
    total.value = data.total
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function prev() {
  if (page.value <= 0) return
  page.value--
  void load()
}

function next() {
  if ((page.value + 1) * size.value >= total.value) return
  page.value++
  void load()
}

onMounted(() => void load())
</script>

<template>
  <div class="oj-subs">
    <div class="toolbar">
      <button type="button" class="site-pill" @click="router.push('/tools/oj')">← 题目列表</button>
      <h2 class="title">我的提交记录</h2>
    </div>
    <p v-if="err" class="err">{{ err }}</p>
    <div v-else-if="loading" class="hint">加载中…</div>
    <div v-else class="table-wrap glass-surface">
      <table class="tbl">
        <thead>
          <tr>
            <th>ID</th>
            <th>题目</th>
            <th>语言</th>
            <th>提交</th>
            <th>结果</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in items" :key="r.id">
            <td>{{ r.id }}</td>
            <td>
              <a href="#" class="link" @click.prevent="router.push(`/tools/oj/p/${r.problemId}`)">{{ r.problemId }}</a>
            </td>
            <td>{{ r.language }}</td>
            <td>{{ r.submitted ? '是' : '运行' }}</td>
            <td>{{ r.verdict }}</td>
            <td class="muted">{{ r.createdAt }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="!items.length" class="empty">暂无记录</div>
      <div class="pager">
        <button type="button" class="site-pill" :disabled="page <= 0" @click="prev">上一页</button>
        <span class="meta">{{ page + 1 }} / {{ Math.max(1, Math.ceil(total / size)) }}（共 {{ total }}）</span>
        <button type="button" class="site-pill" :disabled="(page + 1) * size >= total" @click="next">下一页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.oj-subs {
  padding-bottom: 2rem;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.title {
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-color);
}
.err {
  color: #f87171;
}
.hint {
  color: var(--text-color);
  opacity: 0.8;
}
.table-wrap {
  border-radius: 16px;
  padding: 16px;
  overflow: auto;
}
.tbl {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  color: var(--text-color);
}
.tbl th,
.tbl td {
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  padding: 10px 8px;
  text-align: left;
}
.link {
  color: var(--primary-color);
  text-decoration: none;
}
.muted {
  opacity: 0.75;
  font-size: 0.82rem;
}
.empty {
  text-align: center;
  padding: 24px;
  color: var(--text-color);
  opacity: 0.7;
}
.pager {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.meta {
  color: var(--text-color);
  opacity: 0.85;
  font-size: 0.9rem;
}
</style>
