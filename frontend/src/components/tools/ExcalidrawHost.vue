<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { createRoot, type Root } from 'react-dom/client'
import { createElement } from 'react'
import { MindExcalidrawApp, type ExcalidrawApi } from '../../excalidraw/MindExcalidrawApp'

const props = defineProps<{
  themeMode: 'light' | 'dark'
  initialData: Record<string, unknown> | null
}>()

const emit = defineEmits<{
  ready: [api: ExcalidrawApi]
}>()

const container = ref<HTMLDivElement | null>(null)
let reactRoot: Root | null = null

function mount() {
  if (!container.value) return
  reactRoot?.unmount()
  reactRoot = createRoot(container.value)
  reactRoot.render(
    createElement(MindExcalidrawApp, {
      theme: props.themeMode,
      initialData: props.initialData,
      onReady: (api: ExcalidrawApi) => emit('ready', api),
    }),
  )
}

onMounted(() => {
  mount()
})

watch(
  () => [props.themeMode, props.initialData] as const,
  () => mount(),
)

onUnmounted(() => {
  reactRoot?.unmount()
  reactRoot = null
})
</script>

<template>
  <div ref="container" class="excalidraw-host" />
</template>

<style scoped>
.excalidraw-host {
  width: 100%;
  height: min(72vh, 820px);
  min-height: 480px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.45));
  transition:
    border-color 0.35s ease,
    box-shadow 0.35s ease;
  will-change: transform;
  touch-action: manipulation;
}

.excalidraw-host:hover {
  box-shadow: 0 8px 28px rgba(102, 217, 255, 0.12);
}
</style>
