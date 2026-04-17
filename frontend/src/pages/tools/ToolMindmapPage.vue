<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { exportToBlob, exportToSvg, serializeAsJSON } from '@excalidraw/excalidraw'
import { useThemeStore } from '../../stores/theme'
import ToolBackBar from '../../components/tools/ToolBackBar.vue'
import ExcalidrawHost from '../../components/tools/ExcalidrawHost.vue'
import type { ExcalidrawApi } from '../../excalidraw/MindExcalidrawApp'
import * as mindApi from '../../api/mindmap'

const { t } = useI18n()
const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const themeMode = computed(() => (isDarkMode.value ? 'dark' : 'light'))

const list = ref<mindApi.MindMapListItem[]>([])
const currentId = ref<number | null>(null)
const currentTitle = ref('')
const sceneJson = ref<string | null>(null)
const hostKey = ref(0)
const apiRef = ref<ExcalidrawApi | null>(null)
const loading = ref(false)
const status = ref('')

const initialDataObj = computed((): Record<string, unknown> | null => {
  if (!sceneJson.value) return null
  try {
    return JSON.parse(sceneJson.value) as Record<string, unknown>
  } catch {
    return null
  }
})

function downloadBlob(blob: Blob, name: string) {
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = name
  a.click()
  URL.revokeObjectURL(a.href)
}

async function refreshList() {
  list.value = await mindApi.fetchMindMapList()
}

async function selectMap(id: number) {
  loading.value = true
  status.value = ''
  try {
    const d = await mindApi.fetchMindMap(id)
    currentId.value = d.id
    currentTitle.value = d.title
    sceneJson.value = d.data
    hostKey.value++
    apiRef.value = null
  } finally {
    loading.value = false
  }
}

async function newMap() {
  loading.value = true
  try {
    const d = await mindApi.createBlankMindMap()
    await refreshList()
    await selectMap(d.id)
    status.value = t('mindmapEditor.created')
  } catch (e) {
    status.value = String(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await refreshList()
    if (list.value.length > 0) {
      await selectMap(list.value[0].id)
    } else {
      await newMap()
    }
  } catch (e) {
    status.value = String(e)
  }
})

/** 切换深/浅色时先把当前画布序列化进 sceneJson 再 remount，避免未保存内容被旧快照覆盖 */
watch(isDarkMode, () => {
  const api = apiRef.value
  if (api) {
    try {
      sceneJson.value = serializeAsJSON(api.getSceneElements(), api.getAppState(), api.getFiles(), 'database')
    } catch {
      /* ignore */
    }
  }
  hostKey.value++
})

function onExcalidrawReady(api: ExcalidrawApi) {
  apiRef.value = api
}

async function saveCloud() {
  const api = apiRef.value
  if (!api || currentId.value == null) return
  try {
    const elements = api.getSceneElements()
    const appState = api.getAppState()
    const files = api.getFiles()
    const json = serializeAsJSON(elements, appState, files, 'database')
    await mindApi.patchMindMap(currentId.value, { title: currentTitle.value.trim() || t('mindmapEditor.untitled'), data: json })
    await refreshList()
    status.value = t('mindmapEditor.savedCloud')
  } catch (e) {
    status.value = String(e)
  }
}

async function applyRename() {
  if (currentId.value == null) return
  try {
    await mindApi.patchMindMap(currentId.value, {
      title: currentTitle.value.trim() || t('mindmapEditor.untitled'),
    })
    await refreshList()
    status.value = t('mindmapEditor.renamed')
  } catch (e) {
    status.value = String(e)
  }
}

async function removeMap() {
  if (currentId.value == null) return
  if (!window.confirm(t('mindmapEditor.confirmDelete'))) return
  try {
    await mindApi.deleteMindMap(currentId.value)
    currentId.value = null
    sceneJson.value = null
    await refreshList()
    if (list.value.length > 0) {
      await selectMap(list.value[0].id)
    } else {
      await newMap()
    }
  } catch (e) {
    status.value = String(e)
  }
}

async function exportPng() {
  const api = apiRef.value
  if (!api) return
  try {
    const blob = await exportToBlob({
      elements: api.getSceneElements(),
      appState: api.getAppState(),
      files: api.getFiles(),
      mimeType: 'image/png',
      exportPadding: 12,
    })
    if (blob) downloadBlob(blob, `${currentTitle.value || 'mindmap'}.png`)
  } catch (e) {
    status.value = t('mindmapEditor.exportFail', { msg: String(e) })
  }
}

async function exportSvgFile() {
  const api = apiRef.value
  if (!api) return
  try {
    const svg = await exportToSvg({
      elements: api.getSceneElements(),
      appState: api.getAppState(),
      files: api.getFiles(),
      exportPadding: 12,
    })
    const html = svg.outerHTML
    downloadBlob(new Blob([html], { type: 'image/svg+xml;charset=utf-8' }), `${currentTitle.value || 'mindmap'}.svg`)
  } catch (e) {
    status.value = t('mindmapEditor.exportFail', { msg: String(e) })
  }
}

function exportJsonFile() {
  const api = apiRef.value
  if (!api) return
  try {
    const json = serializeAsJSON(api.getSceneElements(), api.getAppState(), api.getFiles(), 'database')
    downloadBlob(new Blob([json], { type: 'application/json' }), `${currentTitle.value || 'mindmap'}.json`)
  } catch (e) {
    status.value = t('mindmapEditor.exportFail', { msg: String(e) })
  }
}
</script>

<template>
  <div class="tool-mindmap">
    <ToolBackBar />

    <div class="mindmap-layout glass-layout">
      <aside class="mindmap-sidebar">
        <div class="sidebar-head">
          <h2 class="sidebar-title">{{ t('mindmapEditor.sidebarTitle') }}</h2>
          <button type="button" class="site-pill site-pill--nav" :disabled="loading" @click="newMap">
            {{ t('mindmapEditor.new') }}
          </button>
        </div>
        <ul class="map-list">
          <li v-for="m in list" :key="m.id">
            <button
              type="button"
              class="map-item"
              :class="{ active: m.id === currentId }"
              @click="selectMap(m.id)"
            >
              <span class="map-item__title">{{ m.title }}</span>
              <span class="map-item__time">{{ m.updatedAt?.replace('T', ' ').slice(0, 16) }}</span>
            </button>
          </li>
        </ul>
      </aside>

      <section class="mindmap-main">
        <div class="mindmap-toolbar glass-bar">
          <input
            v-model="currentTitle"
            type="text"
            class="title-input"
            :placeholder="t('mindmapEditor.titlePlaceholder')"
            @keydown.enter.prevent="applyRename"
          />
          <button type="button" class="site-pill site-pill--nav" :disabled="loading" @click="applyRename">
            {{ t('mindmapEditor.rename') }}
          </button>
          <button type="button" class="site-pill site-pill--nav site-pill--active" :disabled="loading" @click="saveCloud">
            {{ t('mindmapEditor.saveCloud') }}
          </button>
          <button type="button" class="site-pill site-pill--nav" @click="exportPng">{{ t('mindmapEditor.exportPng') }}</button>
          <button type="button" class="site-pill site-pill--nav" @click="exportSvgFile">{{ t('mindmapEditor.exportSvg') }}</button>
          <button type="button" class="site-pill site-pill--nav" @click="exportJsonFile">{{ t('mindmapEditor.exportJson') }}</button>
          <button type="button" class="site-pill site-pill--nav danger" :disabled="loading" @click="removeMap">
            {{ t('mindmapEditor.delete') }}
          </button>
          <span v-if="status" class="toolbar-status">{{ status }}</span>
        </div>

        <div v-if="loading" class="loading-mask">{{ t('mindmapEditor.loading') }}</div>

        <ExcalidrawHost
          v-else-if="initialDataObj"
          :key="hostKey"
          :theme-mode="themeMode"
          :initial-data="initialDataObj"
          @ready="onExcalidrawReady"
        />
      </section>
    </div>

    <p class="mindmap-footnote">{{ t('mindmapEditor.excalidrawHint') }}</p>
  </div>
</template>

<style scoped>
.tool-mindmap {
  max-width: 1480px;
  margin: 0 auto;
  padding-bottom: 2rem;
}

.glass-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 1rem;
  align-items: start;
}

@media (max-width: 960px) {
  .glass-layout {
    grid-template-columns: 1fr;
  }
}

.mindmap-sidebar {
  background: var(--glass-bg, rgba(255, 255, 255, 0.35));
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.5));
  border-radius: 18px;
  padding: 1rem;
  backdrop-filter: blur(12px);
  max-height: 80vh;
  overflow: auto;
}

.sidebar-head {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.sidebar-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 800;
  color: var(--blog-on-glass, #1a3a52);
}

.map-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.map-item {
  width: 100%;
  text-align: left;
  padding: 0.6rem 0.5rem;
  margin-bottom: 6px;
  border-radius: 12px;
  border: 1px solid transparent;
  background: rgba(74, 144, 226, 0.08);
  cursor: pointer;
  font: inherit;
  color: inherit;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.map-item.active {
  border-color: rgba(102, 217, 255, 0.65);
  background: rgba(102, 217, 255, 0.18);
}

.map-item__title {
  font-weight: 700;
  font-size: 0.88rem;
}

.map-item__time {
  font-size: 0.72rem;
  opacity: 0.7;
}

.mindmap-main {
  position: relative;
  min-height: 520px;
}

.mindmap-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.45rem;
  padding: 0.55rem 0.65rem;
  margin-bottom: 0.75rem;
  border-radius: 16px;
  background: var(--glass-bg, rgba(255, 255, 255, 0.35));
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.5));
}

.mindmap-toolbar .site-pill {
  cursor: pointer;
  border: none;
  font: inherit;
}

.mindmap-toolbar .danger {
  color: #c0392b;
}

.title-input {
  flex: 1;
  min-width: 140px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(74, 144, 226, 0.35);
  background: rgba(255, 255, 255, 0.85);
  color: #1a2a3a;
  font-weight: 600;
}

:root[data-theme='dark'] .title-input {
  background: rgba(0, 0, 0, 0.35);
  color: #eaf8ff;
  border-color: rgba(102, 217, 255, 0.35);
}

.toolbar-status {
  font-size: 0.8rem;
  font-weight: 600;
  color: #50e3c2;
  margin-left: auto;
}

.loading-mask {
  padding: 3rem;
  text-align: center;
  font-weight: 700;
}

.mindmap-footnote {
  margin-top: 1rem;
  font-size: 0.78rem;
  opacity: 0.72;
  font-weight: 600;
}
</style>
