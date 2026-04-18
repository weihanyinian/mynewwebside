<script setup lang="ts">
/**
 * 全局在线音乐播放器（HTML5 Audio）
 *
 * 【使用方法】在 App.vue 中已挂载则全站生效（非 admin 布局）。
 *
 * 【歌单配置】
 * 1. 推荐：在 public/music/ 下放音频，并维护 public/music/playlist.json（见该目录 README）。
 * 2. 若该 JSON 为空或加载失败，将自动回退到 src/assets/playlist.json。
 */
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import fallbackPlaylist from '../assets/playlist.json'

// ---------- 布局 / 层级（低于 14 酱 Live2D 的 z-index: 999）----------
/** 高于工具页 site-root(1000)，避免主布局抬升后点不到播放器 */
const PLAYER_Z_INDEX = 1100
/** 与侧栏宽度 CSS 变量对齐，避免挡住播放器触发钮 */
const OFFSET_LEFT = 'calc(var(--site-sidebar-width, 0px) + 24px)'
const OFFSET_BOTTOM = '20px'

// ---------- 玻璃态 UI 规范（与站点统一）----------
const GLASS = {
  bg: 'rgba(255, 255, 255, 0.2)',
  blur: '10px',
  border: '1px solid rgba(255, 255, 255, 0.5)',
  shadow: '0 8px 32px rgba(102, 217, 255, 0.2)',
  radius: '16px',
} as const

type MusicTrack = {
  title: string
  url: string
}

const PLAYLIST_PUBLIC_URL = '/music/playlist.json'

function formatTime(sec: number) {
  if (!Number.isFinite(sec) || sec < 0) return '0:00'
  const s = Math.floor(sec % 60)
  const m = Math.floor(sec / 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

const playlist = ref<MusicTrack[]>([])
const audioRef = ref<HTMLAudioElement | null>(null)
const isExpanded = ref(false)
const isPlaying = ref(false)
/** 拖动进度条时不与时间更新抢写 */
const isSeeking = ref(false)

const currentIndex = ref(0)
const volume = ref(0.65)
const currentTime = ref(0)
const duration = ref(0)

/** 顺序 / 单曲循环 / 随机 */
type PlayMode = 'sequence' | 'loop_one' | 'shuffle'
const playMode = ref<PlayMode>('sequence')
const FAV_KEY = 'weihan_mp_favorites_v1'
const favorites = ref<string[]>([])

const currentTrack = computed(() => playlist.value[currentIndex.value] ?? null)
const isFavorite = computed(
  () => !!(currentTrack.value && favorites.value.includes(currentTrack.value.url)),
)
const modeLabel = computed(() =>
  playMode.value === 'loop_one' ? '🔂' : playMode.value === 'shuffle' ? '🔀' : '🔁',
)

/** 供 range 渐变填充 */
const progressFillStr = computed(() =>
  duration.value > 0 ? `${(currentTime.value / duration.value) * 100}%` : '0%',
)
const volumeFillStr = computed(() => `${volume.value * 100}%`)

/** 加载 public/music/playlist.json，失败或空则使用 assets 内建列表 */
async function loadPlaylist() {
  try {
    const res = await fetch(`${PLAYLIST_PUBLIC_URL}?t=${Date.now()}`, { cache: 'no-store' })
    if (res.ok) {
      const data = (await res.json()) as unknown
      if (Array.isArray(data) && data.length > 0) {
        playlist.value = data as MusicTrack[]
        currentIndex.value = 0
        return
      }
    }
  } catch {
    /* 使用回退 */
  }
  playlist.value = fallbackPlaylist as MusicTrack[]
  currentIndex.value = 0
}

function clampIndex(i: number) {
  const n = playlist.value.length
  if (n === 0) return 0
  return ((i % n) + n) % n
}

async function playFromCurrentIndex() {
  await nextTick()
  const el = audioRef.value
  if (!el || !currentTrack.value) return
  el.currentTime = 0
  try {
    await el.play()
  } catch {
    isPlaying.value = false
  }
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

function collapse() {
  isExpanded.value = false
}

async function togglePlay() {
  const el = audioRef.value
  if (!el || !currentTrack.value) return
  if (isPlaying.value) {
    el.pause()
    return
  }
  try {
    await el.play()
  } catch {
    isPlaying.value = false
  }
}

function loadFavorites() {
  try {
    const raw = localStorage.getItem(FAV_KEY)
    const o = raw ? (JSON.parse(raw) as unknown) : []
    favorites.value = Array.isArray(o) ? (o as string[]) : []
  } catch {
    favorites.value = []
  }
}

function persistFavorites() {
  localStorage.setItem(FAV_KEY, JSON.stringify(favorites.value))
}

function toggleFavorite() {
  const url = currentTrack.value?.url
  if (!url) return
  const i = favorites.value.indexOf(url)
  if (i >= 0) favorites.value.splice(i, 1)
  else favorites.value.push(url)
  persistFavorites()
}

function cyclePlayMode() {
  const order: PlayMode[] = ['sequence', 'loop_one', 'shuffle']
  playMode.value = order[(order.indexOf(playMode.value) + 1) % order.length]
}

function playRandomDifferent() {
  const n = playlist.value.length
  if (n <= 1) return
  let j = currentIndex.value
  let guard = 0
  while (j === currentIndex.value && guard++ < 8) {
    j = Math.floor(Math.random() * n)
  }
  currentIndex.value = j
}

function playPrev() {
  if (!playlist.value.length) return
  if (playMode.value === 'shuffle') {
    playRandomDifferent()
    void playFromCurrentIndex()
    return
  }
  currentIndex.value = clampIndex(currentIndex.value - 1)
  void playFromCurrentIndex()
}

function playNext() {
  if (!playlist.value.length) return
  if (playMode.value === 'shuffle') {
    playRandomDifferent()
    void playFromCurrentIndex()
    return
  }
  currentIndex.value = clampIndex(currentIndex.value + 1)
  void playFromCurrentIndex()
}

function onEnded() {
  if (playMode.value === 'loop_one') {
    void playFromCurrentIndex()
    return
  }
  if (playMode.value === 'shuffle') {
    playRandomDifferent()
    void playFromCurrentIndex()
    return
  }
  playNext()
}

function onTimeUpdate() {
  const el = audioRef.value
  if (!el || isSeeking.value) return
  currentTime.value = el.currentTime
}

function onLoadedMetadata() {
  const el = audioRef.value
  if (!el) return
  duration.value = el.duration || 0
}

function onPlay() {
  isPlaying.value = true
}

function onPause() {
  isPlaying.value = false
}

function applyVolume() {
  const el = audioRef.value
  if (el) el.volume = volume.value
}

function onVolumeInput(e: Event) {
  volume.value = Number((e.target as HTMLInputElement).value)
  applyVolume()
}

function onSeekStart() {
  isSeeking.value = true
}

function onSeekInput(e: Event) {
  const el = audioRef.value
  if (!el) return
  const v = Number((e.target as HTMLInputElement).value)
  el.currentTime = v
  currentTime.value = v
}

function onSeekEnd() {
  isSeeking.value = false
}

let rafId = 0
function tick() {
  /* 部分浏览器 timeupdate 偏稀疏，用 raf 辅助刷新显示（拖动时跳过） */
  if (!isSeeking.value && audioRef.value && isPlaying.value) {
    currentTime.value = audioRef.value.currentTime
  }
  rafId = requestAnimationFrame(tick)
}

watch(currentIndex, () => {
  duration.value = 0
  currentTime.value = 0
})

watch(
  playlist,
  (list) => {
    if (!list.length) return
    currentIndex.value = clampIndex(currentIndex.value)
  },
  { deep: true },
)

onMounted(async () => {
  loadFavorites()
  await loadPlaylist()
  applyVolume()
  rafId = requestAnimationFrame(tick)
})

onUnmounted(() => {
  cancelAnimationFrame(rafId)
})
</script>

<template>
  <div
    class="mp-root"
    :style="{
      zIndex: PLAYER_Z_INDEX,
      left: OFFSET_LEFT,
      bottom: OFFSET_BOTTOM,
    }"
  >
    <!-- 最小化：圆形触发钮（青蓝 + 粉紫渐变，与参考图一致） -->
    <button
      type="button"
      class="mp-trigger glass-surface"
      :class="{ 'mp-trigger--pulse': isPlaying }"
      aria-label="展开音乐播放器"
      @click="toggleExpand"
    >
      <span class="mp-trigger__icon">🎵</span>
    </button>

    <!-- 展开面板 -->
    <transition name="mp-panel">
      <div v-show="isExpanded" class="mp-panel glass-surface">
        <header class="mp-panel__head">
          <span class="mp-panel__title" :title="currentTrack?.title ?? ''">
            {{ currentTrack?.title ?? '暂无曲目' }}
          </span>
          <button type="button" class="mp-panel__close" aria-label="收起" @click="collapse">
            ✕
          </button>
        </header>

        <div class="mp-extra">
          <button type="button" class="mp-mini" title="播放模式" @click="cyclePlayMode">{{ modeLabel }}</button>
          <button type="button" class="mp-mini" title="收藏当前" @click="toggleFavorite">
            {{ isFavorite ? '❤' : '♡' }}
          </button>
        </div>

        <div class="mp-controls">
          <button type="button" class="mp-round" aria-label="上一首" @click="playPrev">⏮</button>
          <button type="button" class="mp-round mp-round--lg" aria-label="播放或暂停" @click="togglePlay">
            {{ isPlaying ? '⏸' : '▶' }}
          </button>
          <button type="button" class="mp-round" aria-label="下一首" @click="playNext">⏭</button>
        </div>

        <div class="mp-row">
          <span class="mp-time">{{ formatTime(currentTime) }}</span>
          <input
            type="range"
            class="mp-range mp-range--progress"
            :min="0"
            :max="duration > 0 ? duration : 0"
            :step="0.1"
            :value="currentTime"
            @pointerdown="onSeekStart"
            @pointerup="onSeekEnd"
            @pointerleave="onSeekEnd"
            @input="onSeekInput"
          />
          <span class="mp-time">{{ formatTime(duration) }}</span>
        </div>

        <div class="mp-row mp-row--vol">
          <span class="mp-vol-ico" aria-hidden="true">🔊</span>
          <input
            type="range"
            class="mp-range mp-range--vol"
            min="0"
            max="1"
            step="0.01"
            :value="volume"
            @input="onVolumeInput"
          />
        </div>
      </div>
    </transition>

    <audio
      ref="audioRef"
      :src="currentTrack?.url"
      preload="metadata"
      @ended="onEnded"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      @play="onPlay"
      @pause="onPause"
    />
  </div>
</template>

<style scoped>
.mp-root {
  position: fixed;
  display: flex;
  flex-direction: row;
  align-items: flex-end;
  gap: 12px;
  pointer-events: none;
}

.mp-root > * {
  pointer-events: auto;
}

.glass-surface {
  background: v-bind('GLASS.bg');
  backdrop-filter: blur(v-bind('GLASS.blur'));
  -webkit-backdrop-filter: blur(v-bind('GLASS.blur'));
  border: v-bind('GLASS.border');
  box-shadow: v-bind('GLASS.shadow');
  border-radius: v-bind('GLASS.radius');
}

.mp-trigger {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: v-bind('GLASS.border');
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.45) 0%, rgba(252, 162, 229, 0.42) 100%);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: v-bind('GLASS.shadow');
}

.mp-trigger:hover {
  transform: scale(1.06);
  box-shadow: 0 10px 28px rgba(102, 217, 255, 0.35);
}

.mp-trigger--pulse .mp-trigger__icon {
  animation: mp-spin 4s linear infinite;
}

@keyframes mp-spin {
  to {
    transform: rotate(360deg);
  }
}

.mp-trigger__icon {
  font-size: 1.45rem;
  line-height: 1;
  display: block;
}

.mp-panel {
  width: min(300px, calc(100vw - 100px));
  padding: 14px 16px 16px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.mp-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}

.mp-panel__title {
  font-size: 0.88rem;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  line-height: 1.35;
  flex: 1;
  min-width: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mp-panel__close {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.mp-panel__close:hover {
  background: rgba(252, 162, 229, 0.35);
  color: #fff;
}

.mp-extra {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 10px;
}
.mp-mini {
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border-radius: 10px;
  padding: 4px 10px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.mp-mini:hover {
  transform: scale(1.05);
}

.mp-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 14px;
}

.mp-round {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 0.95rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.25s ease, background 0.25s ease, box-shadow 0.25s ease;
}

.mp-round:hover {
  transform: scale(1.08);
  background: rgba(102, 217, 255, 0.35);
  box-shadow: 0 6px 18px rgba(102, 217, 255, 0.25);
}

.mp-round--lg {
  width: 48px;
  height: 48px;
  font-size: 1.1rem;
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  border-color: rgba(255, 255, 255, 0.55);
}

.mp-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.mp-row--vol {
  margin-bottom: 0;
}

.mp-time {
  font-size: 0.7rem;
  font-weight: 600;
  opacity: 0.9;
  width: 2.25rem;
  flex-shrink: 0;
  text-align: center;
}

.mp-vol-ico {
  font-size: 0.85rem;
  width: 1.5rem;
  flex-shrink: 0;
  text-align: center;
}

/* 进度 / 音量：轨道深色，已选段青蓝渐变（用 accent-color + background-size 模拟） */
.mp-range {
  flex: 1;
  height: 6px;
  border-radius: 6px;
  appearance: none;
  -webkit-appearance: none;
  background: linear-gradient(
    to right,
    #4a90e2 0%,
    #50e3c2 var(--fill, 0%),
    rgba(30, 40, 50, 0.55) var(--fill, 0%),
    rgba(30, 40, 50, 0.55) 100%
  );
  cursor: pointer;
}

.mp-range--progress {
  --fill: v-bind(progressFillStr);
}

.mp-range--vol {
  --fill: v-bind(volumeFillStr);
}

.mp-range::-webkit-slider-thumb {
  appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(145deg, #fca2e5, #66d9ff);
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(102, 217, 255, 0.45);
  cursor: grab;
  transition: transform 0.2s ease;
}

.mp-range::-webkit-slider-thumb:hover {
  transform: scale(1.12);
}

.mp-range::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(145deg, #fca2e5, #66d9ff);
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(102, 217, 255, 0.45);
}

.mp-panel-enter-active,
.mp-panel-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.mp-panel-enter-from,
.mp-panel-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

@media (max-width: 480px) {
  .mp-root {
    left: 12px !important;
    bottom: 16px !important;
    flex-direction: column-reverse;
    align-items: flex-start;
  }

  .mp-panel {
    width: calc(100vw - 24px);
    max-width: 320px;
  }
}
</style>
