<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicArticles, type ArticleListItem } from '../api/blog'

const router = useRouter()
const open = ref(false)
const keyword = ref('')
const results = ref<ArticleListItem[]>([])
const loading = ref(false)
const selectedIndex = ref(0)
const inputRef = ref<HTMLInputElement | null>(null)
let timer: ReturnType<typeof setTimeout> | null = null

function onInput() {
  if (timer) clearTimeout(timer)
  selectedIndex.value = 0
  const kw = keyword.value.trim()
  if (!kw) { results.value = []; return }
  timer = setTimeout(async () => {
    loading.value = true
    try {
      const r = await getPublicArticles({ keyword: kw, size: 8 })
      results.value = r.items
    } catch { results.value = [] }
    finally { loading.value = false }
  }, 250)
}

function go(item: ArticleListItem) {
  close()
  router.push(`/article/${item.id}`)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') { e.preventDefault(); selectedIndex.value = Math.min(selectedIndex.value + 1, results.value.length - 1) }
  if (e.key === 'ArrowUp') { e.preventDefault(); selectedIndex.value = Math.max(selectedIndex.value - 1, 0) }
  if (e.key === 'Enter' && results.value[selectedIndex.value]) {
    go(results.value[selectedIndex.value])
  }
  if (e.key === 'Escape') close()
}

function close() {
  open.value = false
  keyword.value = ''
  results.value = []
}

function onOpen() {
  open.value = true
  nextTick(() => inputRef.value?.focus())
}

watch(open, (v) => {
  if (!v) { keyword.value = ''; results.value = [] }
})

defineExpose({ open: onOpen, close })
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="search-overlay" @click.self="close">
      <div class="search-dialog">
        <div class="search-input-wrap">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          <input ref="inputRef" v-model="keyword" type="text" class="search-input" placeholder="搜索文章..." @input="onInput" @keydown="onKeydown" />
          <kbd class="search-kbd">ESC</kbd>
        </div>
        <div v-if="loading" class="search-hint">搜索中...</div>
        <div v-else-if="results.length" class="search-results">
          <button v-for="(item, i) in results" :key="item.id" class="search-item" :class="{ selected: i === selectedIndex }" @click="go(item)">
            <span class="search-item-title">{{ item.title }}</span>
            <span class="search-item-meta">{{ item.category?.name || '' }} · {{ item.publishedAt?.split('T')[0] || '' }}</span>
          </button>
        </div>
        <div v-else-if="keyword.trim()" class="search-hint">无匹配结果</div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.search-overlay {
  position: fixed; inset: 0; z-index: 9999; background: rgba(0,0,0,0.55);
  display: flex; align-items: flex-start; justify-content: center; padding-top: 18vh;
  backdrop-filter: blur(4px); -webkit-backdrop-filter: blur(4px);
}
.search-dialog {
  width: min(560px, 92vw); background: var(--surface-elevated);
  border-radius: 16px; border: 1px solid var(--glass-border);
  box-shadow: 0 20px 60px rgba(0,0,0,0.3); overflow: hidden;
}
.search-input-wrap {
  display: flex; align-items: center; gap: 10px; padding: 14px 16px; border-bottom: 1px solid var(--glass-border);
}
.search-icon { flex-shrink: 0; color: var(--text-muted); }
.search-input {
  flex: 1; border: none; background: none; outline: none; font-size: 1rem; color: var(--text-color);
}
.search-input::placeholder { color: var(--text-muted); }
.search-kbd {
  padding: 3px 8px; border-radius: 5px; background: var(--surface-2); color: var(--text-muted);
  font-size: 0.7rem; font-family: inherit; border: 1px solid var(--glass-border);
}
.search-hint { padding: 20px; text-align: center; color: var(--text-muted); font-size: 0.85rem; }
.search-results { max-height: 360px; overflow-y: auto; }
.search-item {
  display: flex; flex-direction: column; gap: 2px; padding: 12px 16px; width: 100%;
  border: none; background: none; text-align: left; cursor: pointer; transition: background 0.1s;
  border-bottom: 1px solid var(--glass-border);
}
.search-item:hover, .search-item.selected { background: var(--surface-3); }
.search-item-title { font-size: 0.9rem; color: var(--text-color); font-weight: 600; }
.search-item-meta { font-size: 0.75rem; color: var(--text-muted); }
</style>
