<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { EditorView, keymap, lineNumbers, placeholder } from '@codemirror/view'
import { EditorState, Compartment } from '@codemirror/state'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { markdown } from '@codemirror/lang-markdown'
import { oneDark } from '@codemirror/theme-one-dark'
import { Transformer } from 'markmap-lib'
import { plugins as markmapPlugins } from 'markmap-lib/plugins'
import { Markmap, loadJS, loadCSS } from 'markmap-view'
import * as markmapView from 'markmap-view'
import { useThemeStore } from '../../stores/theme'

const { t } = useI18n()
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const STORAGE_KEY = 'mywebside-markmap-md'

const DEFAULT_MD = `# 维寒一念 · 思维导图

## 项目
### MyWebSide
- Spring Boot 3
- Vue 3

## 学习路线
- 算法与数据结构
  - 排序 \`sort\`
  - 图论

## 公式
- 欧拉公式 $e^{i\\pi}+1=0$

## 图片
- ![](https://picsum.photos/seed/markmap/240/120)

> 左侧编辑 Markdown，右侧实时预览；支持拖拽节点、滚轮缩放、点击折叠。
`

const editorHost = ref<HTMLDivElement | null>(null)
const svgRef = ref<SVGSVGElement | null>(null)
const statusText = ref('')

const mdSource = ref('')
const themeComp = new Compartment()

const transformer = new Transformer(markmapPlugins)
let mm: Markmap | null = null
let editorView: EditorView | null = null

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function loadStoredMd(): string {
  try {
    const s = localStorage.getItem(STORAGE_KEY)
    if (s) return s
  } catch {
    /* ignore */
  }
  return DEFAULT_MD
}

function persistMd(text: string) {
  try {
    localStorage.setItem(STORAGE_KEY, text)
  } catch {
    /* ignore */
  }
}

async function renderMarkmap(md: string) {
  if (!mm) return
  try {
    const { root, features } = transformer.transform(md)
    const { scripts, styles } = transformer.getUsedAssets(features)
    if (styles?.length) await loadCSS(styles)
    if (scripts?.length) {
      await loadJS(scripts, { getMarkmap: () => markmapView })
    }
    await mm.setData(root)
    await mm.fit()
    statusText.value = ''
  } catch (e) {
    console.error(e)
    statusText.value = String((e as Error).message || e)
  }
}

function scheduleRender(md: string) {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    void renderMarkmap(md)
  }, 280)
}

function buildEditorExtensions() {
  return [
    history(),
    markdown(),
    lineNumbers(),
    placeholder(t('markmap.placeholder')),
    keymap.of([...defaultKeymap, ...historyKeymap]),
    themeComp.of(isDarkMode.value ? oneDark : []),
    EditorView.theme({
      '&': { fontSize: '13px', minHeight: '100%' },
      '.cm-scroller': { fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace' },
    }),
    EditorView.updateListener.of((u) => {
      if (!u.docChanged) return
      const text = u.state.doc.toString()
      mdSource.value = text
      persistMd(text)
      scheduleRender(text)
    }),
  ]
}

function initEditor(initial: string) {
  if (!editorHost.value) return
  mdSource.value = initial
  const state = EditorState.create({
    doc: initial,
    extensions: buildEditorExtensions(),
  })
  editorView = new EditorView({ state, parent: editorHost.value })
}

function initMarkmap() {
  const el = svgRef.value
  if (!el) return
  mm = Markmap.create(el, {
    autoFit: true,
    duration: 400,
    zoom: true,
    pan: true,
  })
}

function applyEditorTheme() {
  if (!editorView) return
  editorView.dispatch({
    effects: themeComp.reconfigure(isDarkMode.value ? oneDark : []),
  })
}

watch(isDarkMode, () => {
  applyEditorTheme()
})

function manualSave() {
  const text = editorView?.state.doc.toString() ?? mdSource.value
  persistMd(text)
  statusText.value = t('markmap.savedHint')
  setTimeout(() => {
    if (statusText.value === t('markmap.savedHint')) statusText.value = ''
  }, 2000)
}

function clearDoc() {
  if (!editorView) return
  editorView.dispatch({
    changes: { from: 0, to: editorView.state.doc.length, insert: '# \n' },
  })
}

function resetDoc() {
  if (!editorView) return
  editorView.dispatch({
    changes: { from: 0, to: editorView.state.doc.length, insert: DEFAULT_MD },
  })
}

function downloadBlob(filename: string, blob: Blob) {
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = filename
  a.click()
  URL.revokeObjectURL(a.href)
}

function exportSvg() {
  const svgEl = mm?.svg.node() as SVGSVGElement | undefined
  if (!svgEl) return
  const xml = new XMLSerializer().serializeToString(svgEl)
  downloadBlob('mindmap.svg', new Blob([xml], { type: 'image/svg+xml;charset=utf-8' }))
}

async function exportPng() {
  const svgEl = mm?.svg.node() as SVGSVGElement | undefined
  if (!svgEl) return
  const scale = 2
  const rect = svgEl.getBoundingClientRect()
  const w = Math.max(1, Math.ceil(rect.width * scale))
  const h = Math.max(1, Math.ceil(rect.height * scale))
  const xml = new XMLSerializer().serializeToString(svgEl)
  const url = URL.createObjectURL(new Blob([xml], { type: 'image/svg+xml;charset=utf-8' }))
  const img = new Image()
  try {
    await new Promise<void>((resolve, reject) => {
      img.onload = () => resolve()
      img.onerror = () => reject(new Error('svg image load'))
      img.src = url
    })
    const canvas = document.createElement('canvas')
    canvas.width = w
    canvas.height = h
    const ctx = canvas.getContext('2d')
    if (!ctx) return
    ctx.fillStyle = isDarkMode.value ? '#1a1a2e' : '#f4f8fc'
    ctx.fillRect(0, 0, w, h)
    ctx.drawImage(img, 0, 0, w, h)
    URL.revokeObjectURL(url)
    canvas.toBlob((blob) => {
      if (blob) downloadBlob('mindmap.png', blob)
    }, 'image/png')
  } catch {
    URL.revokeObjectURL(url)
    statusText.value = t('markmap.exportPngFail')
  }
}

function escapeHtml(s: string) {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

function exportHtml() {
  const svgEl = mm?.svg.node() as SVGSVGElement | undefined
  if (!svgEl) return
  const xml = new XMLSerializer().serializeToString(svgEl)
  const md = editorView?.state.doc.toString() ?? mdSource.value
  const title = 'Mindmap — 维寒一念的小站'
  const body = `${xml}\n<!-- 原始 Markdown（可粘贴回编辑器） -->\n<pre id="md-backup" style="display:none">${escapeHtml(md)}</pre>`
  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>${escapeHtml(title)}</title>
<style>
  html,body{margin:0;height:100%;background:${isDarkMode.value ? '#1a1a2e' : '#e8f0f8'};}
  svg{width:100%;height:100vh;display:block;}
</style>
</head>
<body>
${body}
</body>
</html>`
  downloadBlob('mindmap.html', new Blob([html], { type: 'text/html;charset=utf-8' }))
}

const canExport = computed(() => !!mm)

onMounted(() => {
  const initial = loadStoredMd()
  initMarkmap()
  initEditor(initial)
  void renderMarkmap(initial)
})

onBeforeUnmount(() => {
  if (debounceTimer) clearTimeout(debounceTimer)
  editorView?.destroy()
  mm?.destroy()
  mm = null
  editorView = null
})
</script>

<template>
  <div class="markmap-editor" :class="{ 'markmap-editor--dark': isDarkMode }">
    <div class="markmap-toolbar glass-bar">
      <button type="button" class="site-pill site-pill--nav" @click="manualSave">{{ t('markmap.save') }}</button>
      <button type="button" class="site-pill site-pill--nav" :disabled="!canExport" @click="exportSvg">{{ t('markmap.exportSvg') }}</button>
      <button type="button" class="site-pill site-pill--nav" :disabled="!canExport" @click="exportPng">{{ t('markmap.exportPng') }}</button>
      <button type="button" class="site-pill site-pill--nav" :disabled="!canExport" @click="exportHtml">{{ t('markmap.exportHtml') }}</button>
      <button type="button" class="site-pill site-pill--nav" @click="clearDoc">{{ t('markmap.clear') }}</button>
      <button type="button" class="site-pill site-pill--nav" @click="resetDoc">{{ t('markmap.reset') }}</button>
      <span v-if="statusText" class="markmap-status">{{ statusText }}</span>
    </div>

    <div class="markmap-split">
      <div class="markmap-pane markmap-pane--editor">
        <div ref="editorHost" class="markmap-cm-host" />
      </div>
      <div class="markmap-pane markmap-pane--preview">
        <svg ref="svgRef" class="markmap-svg" />
      </div>
    </div>

    <p class="markmap-hint">{{ t('markmap.cdnHint') }}</p>
  </div>
</template>

<style scoped>
.markmap-editor {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-height: min(78vh, 900px);
}

.markmap-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 0.75rem;
  border-radius: 16px;
}

.glass-bar {
  background: var(--glass-bg, rgba(255, 255, 255, 0.4));
  backdrop-filter: blur(12px);
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.55));
  box-shadow: var(--glass-shadow, 0 8px 32px rgba(74, 144, 226, 0.12));
}

.markmap-toolbar .site-pill {
  cursor: pointer;
  border: none;
  font: inherit;
}

.markmap-toolbar button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.markmap-status {
  font-size: 0.82rem;
  font-weight: 600;
  color: #50e3c2;
  margin-left: auto;
}

.markmap-split {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(280px, 1fr) minmax(280px, 1.1fr);
  gap: 12px;
  min-height: 560px;
}

@media (max-width: 900px) {
  .markmap-split {
    grid-template-columns: 1fr;
    min-height: auto;
  }
}

.markmap-pane {
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.5));
  background: rgba(255, 255, 255, 0.55);
}

:root[data-theme='dark'] .markmap-pane {
  background: rgba(16, 18, 27, 0.65);
  border-color: rgba(255, 255, 255, 0.12);
}

.markmap-pane--editor {
  display: flex;
  flex-direction: column;
}

.markmap-cm-host {
  flex: 1;
  min-height: 320px;
  text-align: left;
}

.markmap-pane--preview {
  position: relative;
  min-height: 320px;
}

.markmap-svg {
  width: 100%;
  height: 100%;
  min-height: 320px;
  display: block;
  background: transparent;
}

.markmap-hint {
  margin: 0;
  font-size: 0.78rem;
  opacity: 0.72;
  font-weight: 600;
}
</style>
