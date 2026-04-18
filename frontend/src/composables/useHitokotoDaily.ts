import { computed, onMounted, ref } from 'vue'
import type { Ref } from 'vue'
import { fetchDailyQuoteFromServer } from '../api/blog'

const ACTIVE_KEY = 'hitokoto_daily_active_v1'
const API_CACHE_KEY = 'hitokoto_daily_api_v1'

export type HitokotoDailyState = {
  ymd: string
  index: number
}

function ymdLocal(d = new Date()): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function hashDayToIndex(ymd: string, modulo: number): number {
  if (modulo <= 0) return 0
  let h = 2166136261
  for (let i = 0; i < ymd.length; i++) {
    h ^= ymd.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return Math.abs(h >>> 0) % modulo
}

function readJson<T>(key: string): T | null {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

function writeJson(key: string, v: unknown) {
  try {
    localStorage.setItem(key, JSON.stringify(v))
  } catch {
    /* ignore */
  }
}

export type UseHitokotoDailyOptions = {
  builtin: string[]
  /** 是否请求后端 /api/public/daily-quote */
  enableApi: boolean
}

/**
 * 每日一句：同日固定索引；支持服务端一句缓存；上一句/下一句；与内置列表合并导航。
 */
export function useHitokotoDaily(opts: UseHitokotoDailyOptions) {
  const builtin = opts.builtin
  const serverLine: Ref<string | null> = ref(null)
  const userOverrode = ref(false)

  const list = computed(() => {
    const s = serverLine.value?.trim()
    if (s) return [s, ...builtin]
    return [...builtin]
  })

  const index = ref(0)
  const ready = ref(false)

  function loadActive(ymd: string): HitokotoDailyState | null {
    const st = readJson<HitokotoDailyState>(ACTIVE_KEY)
    if (!st || st.ymd !== ymd) return null
    if (typeof st.index !== 'number' || st.index < 0) return null
    return st
  }

  function saveActive(ymd: string, i: number) {
    writeJson(ACTIVE_KEY, { ymd, index: i } satisfies HitokotoDailyState)
  }

  function loadApiCache(ymd: string): string | null {
    const c = readJson<{ ymd: string; text: string }>(API_CACHE_KEY)
    if (c && c.ymd === ymd && typeof c.text === 'string' && c.text.trim()) return c.text.trim()
    return null
  }

  function saveApiCache(ymd: string, text: string) {
    writeJson(API_CACHE_KEY, { ymd, text })
  }

  function clampIndex(i: number, len: number) {
    if (len <= 0) return 0
    return ((i % len) + len) % len
  }

  function bootstrapIndexForNewDay(ymd: string, len: number, hasServer: boolean) {
    if (hasServer) return 0
    return hashDayToIndex(ymd, len)
  }

  async function tryFetchServer(ymd: string) {
    if (!opts.enableApi) return
    try {
      const text = await fetchDailyQuoteFromServer()
      if (!text) return
      const prevLen = list.value.length
      serverLine.value = text
      saveApiCache(ymd, text)
      const newLen = list.value.length
      if (newLen > prevLen) {
        if (!userOverrode.value) {
          index.value = 0
        } else {
          index.value = clampIndex(index.value + 1, newLen)
        }
        saveActive(ymdLocal(), index.value)
      }
    } catch {
      /* ignore */
    }
  }

  onMounted(async () => {
    const ymd = ymdLocal()
    const cachedApi = loadApiCache(ymd)
    if (cachedApi) serverLine.value = cachedApi

    const L = list.value.length
    const hasServer = !!serverLine.value
    let st = loadActive(ymd)
    if (!st) {
      const bi = bootstrapIndexForNewDay(ymd, L, hasServer)
      st = { ymd, index: clampIndex(bi, L) }
      saveActive(ymd, st.index)
    } else {
      st = { ymd, index: clampIndex(st.index, L) }
      saveActive(ymd, st.index)
    }
    index.value = st.index
    ready.value = true

    if (!cachedApi && opts.enableApi) {
      await tryFetchServer(ymd)
    }
  })

  const currentText = computed(() => {
    const arr = list.value
    if (!arr.length) return ''
    return arr[clampIndex(index.value, arr.length)] ?? ''
  })

  function goNext() {
    userOverrode.value = true
    const ymd = ymdLocal()
    const len = list.value.length
    if (!len) return
    index.value = clampIndex(index.value + 1, len)
    saveActive(ymd, index.value)
  }

  function goPrev() {
    userOverrode.value = true
    const ymd = ymdLocal()
    const len = list.value.length
    if (!len) return
    index.value = clampIndex(index.value - 1, len)
    saveActive(ymd, index.value)
  }

  function advanceLoop() {
    goNext()
  }

  return {
    currentText,
    index,
    list,
    ready,
    goNext,
    goPrev,
    advanceLoop,
    serverLine,
  }
}
