<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { exportToBlob, exportToSvg, serializeAsJSON } from '@excalidraw/excalidraw'
import { useThemeStore } from '../../stores/theme'
import { useUserStore } from '../../stores/user'
import BackToHomeButton from '../../components/BackToHomeButton.vue'
import ExcalidrawHost from '../../components/tools/ExcalidrawHost.vue'
import type { ExcalidrawApi } from '../../excalidraw/MindExcalidrawApp'
import * as mindApi from '../../api/mindmap'

const LS_LOCAL = 'weihan_local_mindmaps_v1'

type LocalMindMap = {
  id: string
  title: string
  data: string
  updatedAt: string
}

type MapRow = { idStr: string; title: string; updatedAt: string }

/** 与后端 MindMapService.EMPTY_SCENE 一致 */
const EMPTY_SCENE_JSON =
  '{"type":"excalidraw","version":2,"source":"mywebside","elements":[],"appState":{"theme":"light","viewBackgroundColor":"#ffffff"},"files":{}}'

const { t } = useI18n()
const themeStore = useThemeStore()
const userStore = useUserStore()
const { isDarkMode } = storeToRefs(themeStore)

const themeMode = computed(() => (isDarkMode.value ? 'dark' : 'light'))
const isCloud = computed(() => userStore.isLoggedIn)

const mapRows = ref<MapRow[]>([])
const currentMapId = ref<string | null>(null)
const currentTitle = ref('')
const sceneJson = ref<string | null>(null)
const hostKey = ref(0)
const apiRef = ref<ExcalidrawApi | null>(null)
const loading = ref(false)
const status = ref('')
const ready = ref(false)

function readLocalMaps(): LocalMindMap[] {
  try {
    const raw = localStorage.getItem(LS_LOCAL)
    if (!raw) return []
    const o = JSON.parse(raw) as { maps?: LocalMindMap[] }
    return Array.isArray(o.maps) ? o.maps : []
  } catch {
    return []
  }
}

function writeLocalMaps(maps: LocalMindMap[]) {
  localStorage.setItem(LS_LOCAL, JSON.stringify({ maps }))
}

function formatRowTime(iso: string): string {
  if (!iso) return ''
  return iso.replace('T', ' ').slice(0, 16)
}

const initialDataObj = computed((): Record<string, unknown> | null => {
  const raw = sceneJson.value
  if (!raw || !raw.trim()) return null
  try {
    const o = JSON.parse(raw) as Record<string, unknown>
    if (o && typeof o === 'object' && 'elements' in o) return o
  } catch {
    /* fallthrough */
  }
  try {
    return JSON.parse(EMPTY_SCENE_JSON) as Record<string, unknown>
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
  userStore.hydrateFromStorage()
  if (userStore.isLoggedIn) {
    const items = await mindApi.fetchMindMapList()
    mapRows.value = items.map((i) => ({
      idStr: String(i.id),
      title: i.title,
      updatedAt: i.updatedAt ?? '',
    }))
  } else {
    mapRows.value = readLocalMaps().map((m) => ({
      idStr: m.id,
      title: m.title,
      updatedAt: m.updatedAt,
    }))
  }
}

async function selectMap(idStr: string) {
  loading.value = true
  status.value = ''
  try {
    if (userStore.isLoggedIn) {
      const d = await mindApi.fetchMindMap(Number(idStr))
      currentMapId.value = idStr
      currentTitle.value = d.title
      const payload = d.data
      sceneJson.value = payload && String(payload).trim() ? String(payload) : EMPTY_SCENE_JSON
    } else {
      const m = readLocalMaps().find((x) => x.id === idStr)
      if (!m) {
        status.value = t('mindmapEditor.loadError')
        return
      }
      currentMapId.value = idStr
      currentTitle.value = m.title
      const payload = m.data
      sceneJson.value = payload && String(payload).trim() ? String(payload) : EMPTY_SCENE_JSON
    }
    hostKey.value++
    apiRef.value = null
  } finally {
    loading.value = false
  }
}

async function newMap() {
  loading.value = true
  try {
    if (userStore.isLoggedIn) {
      const d = await mindApi.createBlankMindMap()
      await refreshList()
      await selectMap(String(d.id))
      status.value = t('mindmapEditor.created')
    } else {
      const id = `local_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
      const maps = readLocalMaps()
      maps.unshift({
        id,
        title: t('mindmapEditor.untitled'),
        data: EMPTY_SCENE_JSON,
        updatedAt: new Date().toISOString(),
      })
      writeLocalMaps(maps)
      await refreshList()
      await selectMap(id)
      status.value = t('mindmapEditor.createdLocal')
    }
  } catch (e) {
    status.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  userStore.hydrateFromStorage()
  try {
    await refreshList()
    if (mapRows.value.length > 0) {
      await selectMap(mapRows.value[0].idStr)
    } else {
      await newMap()
    }
  } catch (e) {
    status.value = e instanceof Error ? e.message : String(e)
  } finally {
    ready.value = true
  }
})

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

function reloadPage() {
  window.location.reload()
}

async function saveCurrent() {
  const api = apiRef.value
  if (!api || currentMapId.value == null) return
  try {
    const elements = api.getSceneElements()
    const appState = api.getAppState()
    const files = api.getFiles()
    const json = serializeAsJSON(elements, appState, files, 'database')
    const title = currentTitle.value.trim() || t('mindmapEditor.untitled')
    if (userStore.isLoggedIn) {
      await mindApi.patchMindMap(Number(currentMapId.value), { title, data: json })
      await refreshList()
      status.value = t('mindmapEditor.savedCloud')
    } else {
      const maps = readLocalMaps()
      const i = maps.findIndex((x) => x.id === currentMapId.value)
      if (i >= 0) {
        maps[i] = { ...maps[i]!, title, data: json, updatedAt: new Date().toISOString() }
        writeLocalMaps(maps)
        await refreshList()
        status.value = t('mindmapEditor.savedLocal')
      }
    }
  } catch (e) {
    status.value = String(e)
  }
}

async function applyRename() {
  if (currentMapId.value == null) return
  try {
    const title = currentTitle.value.trim() || t('mindmapEditor.untitled')
    if (userStore.isLoggedIn) {
      await mindApi.patchMindMap(Number(currentMapId.value), { title })
      await refreshList()
      status.value = t('mindmapEditor.renamed')
    } else {
      const maps = readLocalMaps()
      const i = maps.findIndex((x) => x.id === currentMapId.value)
      if (i >= 0) {
        maps[i] = { ...maps[i]!, title, updatedAt: new Date().toISOString() }
        writeLocalMaps(maps)
        await refreshList()
        status.value = t('mindmapEditor.renamed')
      }
    }
  } catch (e) {
    status.value = String(e)
  }
}

async function removeMap() {
  if (currentMapId.value == null) return
  if (!window.confirm(t('mindmapEditor.confirmDelete'))) return
  try {
    if (userStore.isLoggedIn) {
      await mindApi.deleteMindMap(Number(currentMapId.value))
    } else {
      const maps = readLocalMaps().filter((x) => x.id !== currentMapId.value)
      writeLocalMaps(maps)
    }
    currentMapId.value = null
    sceneJson.value = null
    await refreshList()
    if (mapRows.value.length > 0) {
      await selectMap(mapRows.value[0].idStr)
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

const saveLabel = computed(() => (isCloud.value ? t('mindmapEditor.saveCloud') : t('mindmapEditor.saveLocal')))
</script>

<template>
  <div class="mindmap-page">
    <div class="mindmap-back">
      <BackToHomeButton />
    </div>

    <div class="mindmap-layout glass-layout">
      <aside class="mindmap-sidebar">
        <div class="sidebar-head">
          <h2 class="sidebar-title">{{ t('mindmapEditor.sidebarTitle') }}</h2>
          <button type="button" class="site-pill site-pill--nav" :disabled="loading" @click="newMap">
            {{ t('mindmapEditor.new') }}
          </button>
        </div>
        <ul class="map-list">
          <li v-for="m in mapRows" :key="m.idStr">
            <button
              type="button"
              class="map-item"
              :class="{ active: m.idStr === currentMapId }"
              @click="selectMap(m.idStr)"
            >
              <span class="map-item__title">{{ m.title }}</span>
              <span class="map-item__time">{{ formatRowTime(m.updatedAt) }}</span>
            </button>
          </li>
        </ul>
      </aside>

      <section class="mindmap-main">
        <div v-if="!ready" class="loading-mask">{{ t('mindmapEditor.loading') }}</div>
        <template v-else>
          <p v-if="!isCloud" class="local-hint">{{ t('mindmapEditor.localModeHint') }}</p>
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
            <button type="button" class="site-pill site-pill--nav site-pill--active" :disabled="loading" @click="saveCurrent">
              {{ saveLabel }}
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
          <div v-else class="loading-mask mindmap-error">
            <p>{{ status || t('mindmapEditor.loadError') }}</p>
            <button type="button" class="site-pill site-pill--nav" @click="reloadPage">
              {{ t('mindmapEditor.retry') }}
            </button>
          </div>
        </template>
      </section>
    </div>

    <p class="mindmap-footnote">{{ t('mindmapEditor.excalidrawHint') }}</p>
  </div>
</template>

<style scoped>
.mindmap-page {
  max-width: 1480px;
  margin: 0 auto;
  padding-bottom: 2rem;
}

.mindmap-back {
  margin-bottom: 1rem;
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

.local-hint {
  margin: 0 0 0.65rem;
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
  line-height: 1.45;
  background: rgba(102, 217, 255, 0.12);
  border: 1px solid rgba(102, 217, 255, 0.35);
  color: var(--blog-on-glass, #1a3a52);
}

:root[data-theme='dark'] .local-hint {
  color: #eaf8ff;
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

.mindmap-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.mindmap-error p {
  margin: 0;
  max-width: 420px;
  line-height: 1.5;
}

.mindmap-footnote {
  margin-top: 1rem;
  font-size: 0.78rem;
  opacity: 0.72;
  font-weight: 600;
}
</style>
