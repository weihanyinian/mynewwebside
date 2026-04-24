<script setup lang="ts">
/**
 * 纯静态游戏嵌入：/public/games/<gameId>/index.html（无构建依赖）
 * 与 iframe 内 localStorage 键一致，并通过 postMessage 同步顶栏文案与结束弹窗
 */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import MoyuGameShell from './MoyuGameShell.vue'
import MoyuBackToHubButton from './MoyuBackToHubButton.vue'
import { useThemeStore } from '../../stores/theme'
import { getMoyuHighScore } from '../../utils/moyuScore'

const props = defineProps<{
  gameId: string
  title: string
}>()

const themeStore = useThemeStore()
const { isDarkMode } = storeToRefs(themeStore)

const iframeKey = ref(0)
const loading = ref(true)
const statsLine = ref(`最高分 ${getMoyuHighScore(props.gameId)}`)
const showGameOver = ref(false)

const src = computed(() => {
  const t = isDarkMode.value ? 'dark' : 'light'
  return `/games/${props.gameId}/index.html?id=${encodeURIComponent(props.gameId)}&theme=${t}&k=${iframeKey.value}`
})

function onIframeLoad() {
  loading.value = false
}

function onMessage(ev: MessageEvent) {
  const d = ev.data
  if (!d || !d.__moyu) return
  if (d.type === 'stats' && typeof d.text === 'string') {
    statsLine.value = d.text
  }
  if (d.type === 'gameover') {
    showGameOver.value = true
  }
  if (d.type === 'playing') {
    showGameOver.value = false
  }
}

function restart() {
  showGameOver.value = false
  iframeKey.value += 1
  loading.value = true
}

onMounted(() => window.addEventListener('message', onMessage))
onUnmounted(() => window.removeEventListener('message', onMessage))

</script>

<template>
  <MoyuGameShell :title="title" :high-text="statsLine" :loading="loading">
    <div class="embed">
      <iframe
        :key="iframeKey"
        class="embed__iframe"
        :src="src"
        :title="title"
        @load="onIframeLoad"
      />
      <div v-if="showGameOver" class="embed__overlay" role="dialog" aria-modal="true">
        <div class="embed__modal moyu-like-glass">
          <p class="embed__modal-title">游戏结束</p>
          <div class="embed__modal-actions">
            <button type="button" class="embed__btn embed__btn--primary" @click="restart">再来一局</button>
            <MoyuBackToHubButton block />
          </div>
        </div>
      </div>
    </div>
  </MoyuGameShell>
</template>

<style scoped>
.embed {
  position: relative;
  width: 100%;
  max-width: 100%;
  min-height: min(560px, calc(100dvh - 200px));
  border-radius: 16px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.embed__iframe {
  width: 100%;
  max-width: 100%;
  min-height: min(520px, calc(100dvh - 210px));
  border: none;
  display: block;
}

.embed__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.42);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  padding: 16px;
  z-index: 5;
}

.embed__modal {
  max-width: 320px;
  width: 100%;
  padding: 20px;
  border-radius: 16px;
  text-align: center;
}

.moyu-like-glass {
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.45);
  box-shadow: 0 12px 40px rgba(102, 217, 255, 0.2);
}

:global(.dark-mode) .moyu-like-glass {
  background: rgba(15, 23, 42, 0.85);
  border-color: rgba(255, 255, 255, 0.2);
}

.embed__modal-title {
  margin: 0 0 16px;
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--text-color);
}

.embed__modal-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.embed__btn {
  min-height: 48px;
  border-radius: 14px;
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.45));
  background: var(--glass-bg, rgba(255, 255, 255, 0.25));
  color: var(--text-color);
  font-weight: 700;
  cursor: pointer;
  font-size: 0.95rem;
}

.embed__btn--primary {
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.35);
}
</style>
