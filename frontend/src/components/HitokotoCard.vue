<script setup lang="ts">
import { computed, nextTick, ref, toRef } from 'vue'
import { useI18n } from 'vue-i18n'
import quotes from '../assets/quotes.json'
import { useHitokotoDaily } from '../composables/useHitokotoDaily'
import { useTypewriterEffect } from '../composables/useTypewriterEffect'

const props = withDefaults(
  defineProps<{
    /** 每个字符间隔（毫秒） */
    charIntervalMs?: number
    /** 光标闪烁周期（毫秒） */
    cursorBlinkMs?: number
    /** 打满一句后的停留时间（毫秒） */
    pauseAfterSentenceMs?: number
    /** 是否循环打下一句 */
    loop?: boolean
    /** 鼠标悬停是否暂停打字 */
    pauseOnHover?: boolean
    /** 打下一句前是否清空当前文案 */
    clearBeforeNext?: boolean
    /** 是否请求 Spring Boot /api/public/daily-quote（可用 VITE_HITOKOTO_USE_API 覆盖） */
    useDailyApi?: boolean
  }>(),
  {
    charIntervalMs: 150,
    cursorBlinkMs: 500,
    pauseAfterSentenceMs: 3000,
    loop: true,
    pauseOnHover: true,
    clearBeforeNext: true,
    useDailyApi: undefined,
  },
)

const { t } = useI18n()

const enableApi =
  props.useDailyApi ??
  (import.meta.env.VITE_HITOKOTO_USE_API === 'true' || import.meta.env.VITE_HITOKOTO_USE_API === '1')

const builtin = quotes as string[]

const isHovering = ref(false)
const userPaused = ref(false)

const {
  currentText,
  ready,
  goNext,
  goPrev,
  advanceLoop,
} = useHitokotoDaily({
  builtin,
  enableApi,
})

const targetText = computed(() => (ready.value ? currentText.value : ''))

const TW_STORAGE_KEY = 'hitokoto_typewriter_progress_v1'

let resetTypingProgress: () => void = () => {}

/** 仅一句文案时循环需强制重开打字机（target 不变则 watch 不触发） */
function advanceLoopWrapped() {
  const t0 = currentText.value
  advanceLoop()
  void nextTick(() => {
    if (currentText.value === t0) resetTypingProgress()
  })
}

const { displayed, cursorStyle, resetProgressForCurrentText } = useTypewriterEffect(targetText, {
  charIntervalMs: toRef(props, 'charIntervalMs'),
  cursorBlinkMs: toRef(props, 'cursorBlinkMs'),
  pauseAfterSentenceMs: toRef(props, 'pauseAfterSentenceMs'),
  loop: toRef(props, 'loop'),
  pauseOnHover: toRef(props, 'pauseOnHover'),
  clearBeforeNext: toRef(props, 'clearBeforeNext'),
  storageKey: TW_STORAGE_KEY,
  isHovering,
  userPaused,
  onRequestAdvance: advanceLoopWrapped,
})
resetTypingProgress = resetProgressForCurrentText

const pauseLabel = computed(() => (userPaused.value ? t('hitokoto.resume') : t('hitokoto.pause')))

function togglePause() {
  userPaused.value = !userPaused.value
}

function onPrev() {
  goPrev()
  resetTypingProgress()
}

function onNext() {
  goNext()
  resetTypingProgress()
}
</script>

<template>
  <div
    class="hitokoto-card glass-ui"
    @mouseenter="isHovering = true"
    @mouseleave="isHovering = false"
  >
    <div class="quote-content">
      <span class="quote-mark left">“</span>
      <p class="text typewriter-line">
        <span class="typed">{{ displayed }}</span>
        <span class="caret" :style="cursorStyle" aria-hidden="true" />
      </p>
      <span class="quote-mark right">”</span>
    </div>
    <div class="actions">
      <button type="button" class="ctrl-btn" @click="onPrev">
        {{ t('hitokoto.prev') }}
      </button>
      <button type="button" class="ctrl-btn" @click="togglePause">
        {{ pauseLabel }}
      </button>
      <button type="button" class="ctrl-btn" @click="onNext">
        {{ t('hitokoto.next') }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.hitokoto-card {
  padding: 28px 26px;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  margin: 40px auto;
  max-width: 600px;
  position: relative;
  overflow: hidden;
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    'PingFang SC',
    'Microsoft YaHei',
    sans-serif;
}

.glass-ui {
  background: rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(18px) saturate(1.15);
  -webkit-backdrop-filter: blur(18px) saturate(1.15);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 10px 36px rgba(15, 23, 42, 0.12);
  color: #111827;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

:global(html[data-theme='dark']) .hitokoto-card.glass-ui {
  background: rgba(15, 23, 42, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
  color: #f1f5f9;
}

.hitokoto-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 44px rgba(15, 23, 42, 0.14);
}

:global(html[data-theme='dark']) .hitokoto-card:hover {
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.45);
}

.quote-content {
  position: relative;
  text-align: center;
  transition: opacity 0.3s;
  padding: 0 30px;
  min-height: 3.2rem;
}

.typewriter-line {
  display: inline-flex;
  align-items: flex-end;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0;
}

.text {
  font-size: 1.05rem;
  font-weight: 500;
  line-height: 1.65;
  letter-spacing: 0.02em;
  color: #111827;
  text-shadow: none;
  margin: 0;
}

:global(html[data-theme='dark']) .hitokoto-card .text {
  color: #f1f5f9;
}

.caret {
  display: inline-block;
  width: 2px;
  height: 1.1em;
  margin-left: 2px;
  background: #2563eb;
  border-radius: 1px;
  flex-shrink: 0;
  align-self: flex-end;
  margin-bottom: 0.12em;
  box-shadow: none;
  animation: caret-blink var(--tw-cursor-blink, 500ms) steps(1, end) infinite;
}

:global(html[data-theme='dark']) .hitokoto-card .caret {
  background: #93c5fd;
}

.quote-mark {
  position: absolute;
  font-size: 2.75rem;
  font-family: system-ui, sans-serif;
  font-weight: 300;
  color: rgba(15, 23, 42, 0.12);
  line-height: 1;
}

:global(html[data-theme='dark']) .hitokoto-card .quote-mark {
  color: rgba(255, 255, 255, 0.12);
}

.typed {
  word-break: break-word;
}

@keyframes caret-blink {
  50% {
    opacity: 0;
  }
}

.quote-mark.left {
  top: -16px;
  left: -8px;
}
.quote-mark.right {
  bottom: -28px;
  right: -8px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.ctrl-btn {
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(15, 23, 42, 0.12);
  color: #0f172a;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.2s;
}

:global(html[data-theme='dark']) .hitokoto-card .ctrl-btn {
  background: rgba(30, 41, 59, 0.75);
  border-color: rgba(255, 255, 255, 0.16);
  color: #e2e8f0;
}

.ctrl-btn:hover {
  background: rgba(255, 255, 255, 0.98);
  border-color: rgba(37, 99, 235, 0.35);
}

:global(html[data-theme='dark']) .hitokoto-card .ctrl-btn:hover {
  background: rgba(51, 65, 85, 0.9);
  border-color: rgba(196, 181, 253, 0.35);
}

.ctrl-btn:active {
  transform: translateY(1px);
}

@media (max-width: 768px) {
  .hitokoto-card {
    margin: 20px;
    padding: 20px;
  }
  .text {
    font-size: 1rem;
  }
  .quote-mark {
    font-size: 2.25rem;
  }
}
</style>
