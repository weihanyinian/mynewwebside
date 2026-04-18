import { computed, onUnmounted, ref, unref, watch, type Ref } from 'vue'

export type TypewriterEffectOptions = {
  /** 每个字符间隔（毫秒） */
  charIntervalMs: number | Ref<number>
  /** 光标闪烁周期（毫秒，一次明暗循环） */
  cursorBlinkMs: number | Ref<number>
  /** 打满一句后的停留时间 */
  pauseAfterSentenceMs: number | Ref<number>
  /** 是否循环打下一句 */
  loop: boolean | Ref<boolean>
  /** hover 时是否暂停打字 */
  pauseOnHover: boolean | Ref<boolean>
  /** 打下一句前是否清空当前显示 */
  clearBeforeNext: boolean | Ref<boolean>
  /** localStorage 键（进度按「当前句子的文本指纹」保存） */
  storageKey: string
  /** 由父组件提供：是否 hover */
  isHovering: Ref<boolean>
  /** 用户点击暂停 */
  userPaused: Ref<boolean>
  /** 一句打完并经过停留后，自动进入下一句（由父组件推进索引） */
  onRequestAdvance: () => void
}

function graphemes(s: string): string[] {
  if (typeof Intl !== 'undefined' && 'Segmenter' in Intl) {
    const seg = new Intl.Segmenter('zh', { granularity: 'grapheme' })
    return Array.from(seg.segment(s), (x) => x.segment)
  }
  return Array.from(s)
}

function textFingerprint(s: string): string {
  const g = graphemes(s)
  let h = 2166136261
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return `${g.length}:${h >>> 0}`
}

function readProgress(storageKey: string): { id: string; pos: number } | null {
  try {
    const raw = localStorage.getItem(storageKey)
    if (!raw) return null
    const j = JSON.parse(raw) as { id?: string; pos?: number }
    if (typeof j.id === 'string' && typeof j.pos === 'number') return { id: j.id, pos: j.pos }
  } catch {
    /* ignore */
  }
  return null
}

function writeProgress(storageKey: string, id: string, pos: number) {
  try {
    localStorage.setItem(storageKey, JSON.stringify({ id, pos }))
  } catch {
    /* ignore */
  }
}

/**
 * 逐字打字效果 + 刷新后按 grapheme 位置续播；打满后停留并可循环触发下一句。
 */
export function useTypewriterEffect(targetText: Ref<string>, opts: TypewriterEffectOptions) {
  const displayed = ref('')
  let timer: ReturnType<typeof setTimeout> | null = null
  let disposed = false

  const ms = (k: 'charIntervalMs' | 'pauseAfterSentenceMs' | 'cursorBlinkMs') => unref(opts[k])
  const bool = (k: 'loop' | 'clearBeforeNext') => unref(opts[k])

  function clearTimer() {
    if (timer !== null) {
      clearTimeout(timer)
      timer = null
    }
  }

  function schedule(ms: number, fn: () => void) {
    clearTimer()
    timer = setTimeout(() => {
      timer = null
      if (!disposed) fn()
    }, ms)
  }

  function persistPartial(full: string, pos: number) {
    writeProgress(opts.storageKey, textFingerprint(full), pos)
  }

  function syncDisplayedFromProgress(full: string) {
    const g = graphemes(full)
    const fp = textFingerprint(full)
    const p = readProgress(opts.storageKey)
    if (p && p.id === fp) {
      const pos = Math.min(Math.max(0, p.pos), g.length)
      displayed.value = g.slice(0, pos).join('')
      return pos
    }
    displayed.value = ''
    persistPartial(full, 0)
    return 0
  }

  function tickType() {
    if (disposed) return
    const full = targetText.value
    if (!full) {
      displayed.value = ''
      return
    }
    if (opts.userPaused.value || (unref(opts.pauseOnHover) && opts.isHovering.value)) {
      schedule(40, tickType)
      return
    }
    const g = graphemes(full)
    let pos = graphemes(displayed.value).length
    if (pos >= g.length) {
      persistPartial(full, g.length)
      if (!bool('loop')) return
      schedule(ms('pauseAfterSentenceMs'), () => {
        if (disposed) return
        if (bool('clearBeforeNext')) {
          displayed.value = ''
          persistPartial(full, 0)
        }
        opts.onRequestAdvance()
      })
      return
    }
    const next = g.slice(0, pos + 1).join('')
    displayed.value = next
    persistPartial(full, pos + 1)
    schedule(ms('charIntervalMs'), tickType)
  }

  function startOrResume() {
    clearTimer()
    const full = targetText.value
    if (!full) {
      displayed.value = ''
      return
    }
    const g = graphemes(full)
    let pos = syncDisplayedFromProgress(full)
    if (pos >= g.length) {
      if (!bool('loop')) return
      schedule(ms('pauseAfterSentenceMs'), () => {
        if (disposed) return
        if (bool('clearBeforeNext')) {
          displayed.value = ''
          persistPartial(full, 0)
        }
        opts.onRequestAdvance()
      })
      return
    }
    schedule(ms('charIntervalMs'), tickType)
  }

  watch(
    () => targetText.value,
    () => {
      clearTimer()
      startOrResume()
    },
    { immediate: true },
  )

  onUnmounted(() => {
    disposed = true
    clearTimer()
  })

  const cursorStyle = computed(() => ({
    '--tw-cursor-blink': `${unref(opts.cursorBlinkMs)}ms`,
  }))

  function resetProgressForCurrentText() {
    const full = targetText.value
    if (!full) return
    displayed.value = ''
    persistPartial(full, 0)
    clearTimer()
    schedule(ms('charIntervalMs'), tickType)
  }

  return {
    displayed,
    cursorStyle,
    resetProgressForCurrentText,
    startOrResume,
  }
}
