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
  padding: 30px;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin: 40px auto;
  max-width: 600px;
  position: relative;
  overflow: hidden;
}

.glass-ui {
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.1) 0%, rgba(252, 162, 229, 0.1) 100%);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.15);
  color: #fff;
  transition: all 0.3s ease;
}

.hitokoto-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(252, 162, 229, 0.2);
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
  font-size: 1.2rem;
  font-weight: 600;
  line-height: 1.6;
  letter-spacing: 1px;
  color: #e0f7fa;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin: 0;
}

.typed {
  word-break: break-word;
}

.caret {
  display: inline-block;
  width: 2px;
  height: 1.15em;
  margin-left: 3px;
  background: #4da3ff;
  border-radius: 1px;
  flex-shrink: 0;
  align-self: flex-end;
  margin-bottom: 0.15em;
  box-shadow: 0 0 6px rgba(77, 163, 255, 0.85);
  animation: caret-blink var(--tw-cursor-blink, 500ms) steps(1, end) infinite;
}

@keyframes caret-blink {
  50% {
    opacity: 0;
  }
}

.quote-mark {
  position: absolute;
  font-size: 4rem;
  font-family: Georgia, serif;
  color: rgba(252, 162, 229, 0.3);
  line-height: 1;
}
.quote-mark.left {
  top: -20px;
  left: -10px;
}
.quote-mark.right {
  bottom: -40px;
  right: -10px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.ctrl-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.ctrl-btn:hover {
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.4), rgba(252, 162, 229, 0.4));
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 12px rgba(102, 217, 255, 0.3);
  transform: translateY(-1px);
}

.ctrl-btn:active {
  transform: translateY(0);
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
    font-size: 3rem;
  }
}
</style>
