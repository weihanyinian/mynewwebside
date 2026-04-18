<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import * as monaco from 'monaco-editor'
import EditorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import TsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/user'
import * as snippetApi from '../../api/snippets'

const { t } = useI18n()
const userStore = useUserStore()
const { isLoggedIn } = storeToRefs(userStore)

const LANGS = ['plaintext', 'javascript', 'typescript', 'python', 'java', 'cpp', 'html', 'json'] as const

const _g = globalThis as typeof globalThis & {
  MonacoEnvironment?: { getWorker: (module: string, label: string) => Worker }
}
_g.MonacoEnvironment = {
  getWorker(_module: string, label: string) {
    if (label === 'typescript' || label === 'javascript') return new TsWorker()
    return new EditorWorker()
  },
}

const el = ref<HTMLElement | null>(null)
let editor: monaco.editor.IStandaloneCodeEditor | null = null

const list = ref<snippetApi.CodeSnippet[]>([])
const currentId = ref<number | null>(null)
const title = ref('')
const language = ref<string>('typescript')
const loading = ref(false)
const status = ref('')

async function refreshList() {
  if (!isLoggedIn.value) {
    list.value = []
    return
  }
  loading.value = true
  try {
    list.value = await snippetApi.listSnippets()
  } catch (e) {
    status.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

function applyToEditor(content: string) {
  if (editor) editor.setValue(content)
}

async function selectSnippet(id: number) {
  if (!isLoggedIn.value) return
  loading.value = true
  try {
    const s = await snippetApi.getSnippet(id)
    currentId.value = s.id
    title.value = s.title
    language.value = (LANGS as readonly string[]).includes(s.language) ? s.language : 'plaintext'
    await nextTick()
    monaco.editor.setModelLanguage(editor!.getModel()!, language.value)
    applyToEditor(s.content)
  } catch (e) {
    status.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

async function saveSnippet() {
  if (!isLoggedIn.value || !editor) return
  const content = editor.getValue()
  loading.value = true
  status.value = ''
  try {
    const body = { title: title.value.trim() || t('pages.snippetsTitlePh'), language: language.value, content }
    if (currentId.value == null) {
      const s = await snippetApi.createSnippet(body)
      currentId.value = s.id
    } else {
      await snippetApi.updateSnippet(currentId.value, body)
    }
    await refreshList()
    status.value = 'OK'
  } catch (e) {
    status.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

async function newSnippet() {
  currentId.value = null
  title.value = ''
  language.value = 'typescript'
  await nextTick()
  if (editor) {
    monaco.editor.setModelLanguage(editor.getModel()!, 'typescript')
    applyToEditor('// ' + t('pages.snippetsTitle') + '\n')
  }
}

async function removeSnippet() {
  if (!isLoggedIn.value || currentId.value == null) return
  if (!confirm('Delete?')) return
  await snippetApi.deleteSnippet(currentId.value)
  currentId.value = null
  await refreshList()
  await newSnippet()
}

onMounted(async () => {
  userStore.hydrateFromStorage()
  await nextTick()
  if (!el.value) return
  editor = monaco.editor.create(el.value, {
    value: '// Monaco Editor\n',
    language: language.value,
    theme: document.documentElement.dataset.theme === 'dark' ? 'vs-dark' : 'vs',
    automaticLayout: true,
    fontSize: 14,
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
  })
  await refreshList()
})

watch(isLoggedIn, () => {
  void refreshList()
})

watch(language, (lang) => {
  if (editor?.getModel()) monaco.editor.setModelLanguage(editor.getModel()!, lang)
})

onUnmounted(() => {
  editor?.dispose()
  editor = null
})
</script>

<template>
  <div class="snippets-page">
    <h1 class="page-title">{{ t('pages.snippetsTitle') }}</h1>
    <p v-if="!isLoggedIn" class="muted banner">{{ t('pages.snippetsLogin') }}</p>
    <div class="snippets-layout">
      <aside v-if="isLoggedIn" class="snippet-list card">
        <button type="button" class="site-pill site-pill--active block-btn" @click="newSnippet">
          {{ t('pages.snippetsNew') }}
        </button>
        <p v-if="loading" class="muted small-p">{{ t('pages.loading') }}</p>
        <ul>
          <li v-for="s in list" :key="s.id">
            <button type="button" class="list-btn" :class="{ active: s.id === currentId }" @click="selectSnippet(s.id)">
              {{ s.title }}
            </button>
          </li>
        </ul>
      </aside>
      <div class="editor-panel card">
        <div class="editor-toolbar">
          <input v-model="title" type="text" class="title-in" :placeholder="t('pages.snippetsTitlePh')" />
          <select v-model="language" class="lang-select">
            <option v-for="l in LANGS" :key="l" :value="l">{{ l }}</option>
          </select>
          <button v-if="isLoggedIn" type="button" class="site-pill site-pill--active" @click="saveSnippet">
            {{ t('pages.snippetsSave') }}
          </button>
          <button v-if="isLoggedIn && currentId != null" type="button" class="site-pill danger" @click="removeSnippet">
            {{ t('pages.snippetsDelete') }}
          </button>
          <span v-if="status" class="status">{{ status }}</span>
        </div>
        <div ref="el" class="monaco-wrap" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.snippets-page {
  padding: 8px 0 24px;
}
.banner {
  margin-bottom: 16px;
}
.snippets-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 18px;
  align-items: start;
}
@media (max-width: 900px) {
  .snippets-layout {
    grid-template-columns: 1fr;
  }
}
.snippet-list {
  padding: 14px;
  max-height: 70vh;
  overflow: auto;
}
.block-btn {
  width: 100%;
  margin-bottom: 12px;
}
.small-p {
  font-size: 0.8rem;
}
.snippet-list ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.list-btn {
  width: 100%;
  text-align: left;
  padding: 8px 10px;
  margin-bottom: 6px;
  border-radius: 10px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.06);
  color: inherit;
  cursor: pointer;
  font-size: 0.82rem;
}
.list-btn:hover {
  border-color: rgba(102, 217, 255, 0.35);
}
.list-btn.active {
  border-color: rgba(80, 227, 194, 0.55);
  background: rgba(74, 144, 226, 0.12);
}
.editor-panel {
  padding: 0;
  overflow: hidden;
}
.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 12px 14px;
  border-bottom: 1px solid var(--glass-border);
  background: rgba(255, 255, 255, 0.04);
}
.title-in {
  flex: 1;
  min-width: 140px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  color: var(--text-color);
}
.lang-select {
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid var(--glass-border);
  background: var(--glass-bg);
  color: var(--text-color);
}
.danger {
  background: linear-gradient(135deg, #f87171, #fb7185) !important;
  border-color: transparent !important;
  color: #fff !important;
}
.monaco-wrap {
  height: min(68vh, 640px);
  width: 100%;
}
.status {
  font-size: 0.78rem;
  opacity: 0.75;
}
</style>
