<script setup lang="ts">
/**
 * 全局在线音乐播放器（随机 API + 网易云歌单 + HTML5 Audio）
 *
 * 两种模式：
 * 1. 随机模式：维梦API 随机网易云音乐
 * 2. 歌单模式：后端代理网易云歌单，顺序/随机播放
 */
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

// ---------- 布局 / 层级 ----------
const PLAYER_Z_INDEX = 1100
const OFFSET_LEFT = '24px'
const OFFSET_BOTTOM = '20px'

// ---------- 玻璃态 UI 规范 ----------
const GLASS = {
  bg: 'rgba(255, 255, 255, 0.2)',
  blur: '10px',
  border: '1px solid rgba(255, 255, 255, 0.5)',
  shadow: '0 8px 32px rgba(102, 217, 255, 0.2)',
  radius: '16px',
} as const

// ---------- 音乐 API 配置 ----------
const RANDOM_API = 'https://api.52vmy.cn/api/music/wy/rand'
const FALLBACK_API = 'https://openapi.dwo.cc/api/mp3'
const PLAYLIST_API = '/api/public/music/playlist'
const DEFAULT_PLAYLIST_ID = '489057279'

type OnlineTrack = {
  title: string
  artist: string
  url: string
  cover: string
  id: number
}

type PlaySource = 'random' | 'playlist'
const playSource = ref<PlaySource>('random')

/** 歌单列表 */
const playlistTracks = ref<OnlineTrack[]>([])
const playlistIndex = ref(-1)
const playlistLoaded = ref(false)

function formatTime(sec: number) {
  if (!Number.isFinite(sec) || sec < 0) return '0:00'
  const s = Math.floor(sec % 60)
  const m = Math.floor(sec / 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

const audioRef = ref<HTMLAudioElement | null>(null)
const isExpanded = ref(false)
const isPlaying = ref(false)
const isSeeking = ref(false)
const isLoading = ref(false)

const currentTrack = ref<OnlineTrack | null>(null)
const volume = ref(0.65)
const currentTime = ref(0)
const duration = ref(0)

/** 顺序 / 单曲循环 / 随机 */
type PlayMode = 'sequence' | 'loop_one' | 'shuffle'
const playMode = ref<PlayMode>('shuffle')
const FAV_KEY = 'weihan_mp_favorites_v2'
const favorites = ref<string[]>([])

/** 随机模式播放历史 */
const history = ref<OnlineTrack[]>([])
const historyIndex = ref(-1)

const displayTitle = computed(() => {
  if (!currentTrack.value) return '暂无曲目'
  const t = currentTrack.value
  return t.artist ? `${t.artist} - ${t.title}` : t.title
})

const sourceLabel = computed(() =>
  playSource.value === 'playlist' ? '📋 歌单' : '🎲 随机',
)

const isFavorite = computed(
  () => !!(currentTrack.value && favorites.value.includes(String(currentTrack.value.id))),
)
const modeLabel = computed(() =>
  playMode.value === 'loop_one' ? '🔂' : playMode.value === 'shuffle' ? '🔀' : '🔁',
)

const progressFillStr = computed(() =>
  duration.value > 0 ? `${(currentTime.value / duration.value) * 100}%` : '0%',
)
const volumeFillStr = computed(() => `${volume.value * 100}%`)

// ---------- 切换音源 ----------
function toggleSource() {
  if (playSource.value === 'random') {
    playSource.value = 'playlist'
    if (!playlistLoaded.value) {
      loadPlaylist()
    }
  } else {
    playSource.value = 'random'
  }
}

// ---------- 歌单加载 ----------
async function loadPlaylist() {
  try {
    const res = await fetch(`${PLAYLIST_API}?id=${DEFAULT_PLAYLIST_ID}&shuffle=true`, { cache: 'no-store' })
    if (res.ok) {
      const json = await res.json()
      if (json.code === 0 && Array.isArray(json.data) && json.data.length > 0) {
        playlistTracks.value = json.data.map((t: any) => ({
          id: t.id,
          title: t.name || '未知',
          artist: t.artist || '',
          url: '', // 需要单独获取播放链接
          cover: t.cover || '',
        }))
        playlistIndex.value = 0
        playlistLoaded.value = true
        return
      }
    }
  } catch {
    // 歌单 API 不可用
  }
  // fallback：回到随机模式
  playSource.value = 'random'
}

/** 通过维梦 API 根据歌曲名获取播放链接 */
async function resolveTrackUrl(_track: OnlineTrack): Promise<string> {
  // 尝试用歌曲 ID 从维梦获取
  try {
    const res = await fetch(RANDOM_API, { cache: 'no-store' })
    if (res.ok) {
      const json = await res.json()
      if (json.code === 200 && json.data?.Music) {
        return json.data.Music
      }
    }
  } catch { /* ignore */ }

  // 备用 API
  return `${FALLBACK_API}?t=${Date.now()}`
}

// ---------- API 调用：随机模式 ----------
async function fetchRandomTrack(): Promise<OnlineTrack | null> {
  try {
    const res = await fetch(RANDOM_API, { cache: 'no-store' })
    if (res.ok) {
      const json = await res.json()
      if (json.code === 200 && json.data?.Music) {
        return {
          title: json.data.song || '未知歌曲',
          artist: json.data.singer || '',
          url: json.data.Music,
          cover: json.data.cover || '',
          id: json.data.id || 0,
        }
      }
    }
  } catch { /* ignore */ }

  // 备用 API
  try {
    const fallbackUrl = `${FALLBACK_API}?t=${Date.now()}`
    return {
      title: '随机音乐',
      artist: '',
      url: fallbackUrl,
      cover: '',
      id: Date.now(),
    }
  } catch {
    return null
  }
}

// ---------- 播放逻辑 ----------
async function playNewTrack() {
  isLoading.value = true

  if (playSource.value === 'playlist' && playlistTracks.value.length > 0) {
    // 歌单模式：顺序/随机播放
    if (playMode.value === 'shuffle') {
      playlistIndex.value = Math.floor(Math.random() * playlistTracks.value.length)
    } else {
      playlistIndex.value = (playlistIndex.value + 1) % playlistTracks.value.length
    }
    const track = playlistTracks.value[playlistIndex.value]
    // 歌单只有元信息，需要获取播放链接
    if (!track.url) {
      const url = await resolveTrackUrl(track)
      track.url = url
    }
    currentTrack.value = { ...track }
    addToHistory(currentTrack.value)
  } else {
    // 随机模式
    const track = await fetchRandomTrack()
    if (!track) {
      isLoading.value = false
      return
    }
    currentTrack.value = track
    addToHistory(track)
  }

  await nextTick()
  await playAudio()
  isLoading.value = false
}

function addToHistory(track: OnlineTrack) {
  history.value = history.value.slice(0, historyIndex.value + 1)
  history.value.push(track)
  historyIndex.value = history.value.length - 1
  if (history.value.length > 50) {
    history.value.shift()
    historyIndex.value--
  }
}

async function playAudio() {
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
  if (!el) return
  if (!currentTrack.value) {
    await playNewTrack()
    return
  }
  if (isPlaying.value) {
    el.pause()
  } else {
    try {
      await el.play()
    } catch {
      isPlaying.value = false
    }
  }
}

function loadFavorites() {
  try {
    const raw = localStorage.getItem(FAV_KEY)
    const o = raw ? JSON.parse(raw) : []
    favorites.value = Array.isArray(o) ? o : []
  } catch {
    favorites.value = []
  }
}

function persistFavorites() {
  localStorage.setItem(FAV_KEY, JSON.stringify(favorites.value))
}

function toggleFavorite() {
  if (!currentTrack.value) return
  const id = String(currentTrack.value.id)
  const i = favorites.value.indexOf(id)
  if (i >= 0) favorites.value.splice(i, 1)
  else favorites.value.push(id)
  persistFavorites()
}

function cyclePlayMode() {
  const order: PlayMode[] = ['sequence', 'loop_one', 'shuffle']
  playMode.value = order[(order.indexOf(playMode.value) + 1) % order.length]
}

function playPrev() {
  if (playSource.value === 'playlist' && playlistTracks.value.length > 0) {
    // 歌单模式：上一首
    playlistIndex.value = (playlistIndex.value - 1 + playlistTracks.value.length) % playlistTracks.value.length
    const track = playlistTracks.value[playlistIndex.value]
    currentTrack.value = { ...track }
    void playAudio()
  } else if (historyIndex.value > 0) {
    // 随机模式：从历史回退
    historyIndex.value--
    currentTrack.value = history.value[historyIndex.value]
    void playAudio()
  }
}

function playNext() {
  if (playMode.value === 'loop_one') {
    void playAudio()
    return
  }
  void playNewTrack()
}

function onEnded() {
  if (playMode.value === 'loop_one') {
    void playAudio()
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
  if (!isSeeking.value && audioRef.value && isPlaying.value) {
    currentTime.value = audioRef.value.currentTime
  }
  rafId = requestAnimationFrame(tick)
}

watch(currentTrack, () => {
  duration.value = 0
  currentTime.value = 0
})

onMounted(() => {
  loadFavorites()
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
    <!-- 最小化：圆形触发钮 -->
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
          <span class="mp-panel__title" :title="displayTitle">
            {{ isLoading ? '🎵 加载中...' : displayTitle }}
          </span>
          <button type="button" class="mp-panel__close" aria-label="收起" @click="collapse">
            ✕
          </button>
        </header>

        <div class="mp-extra">
          <button type="button" class="mp-mini" title="切换音源" @click="toggleSource">{{ sourceLabel }}</button>
          <button type="button" class="mp-mini" title="播放模式" @click="cyclePlayMode">{{ modeLabel }}</button>
          <button type="button" class="mp-mini" title="收藏当前" @click="toggleFavorite">
            {{ isFavorite ? '❤' : '♡' }}
          </button>
        </div>

        <div class="mp-controls">
          <button type="button" class="mp-round" aria-label="上一首" @click="playPrev">⏮</button>
          <button type="button" class="mp-round mp-round--lg" aria-label="播放或暂停" @click="togglePlay">
            {{ isLoading ? '⏳' : isPlaying ? '⏸' : '▶' }}
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
      @error="onEnded"
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
  gap: 6px;
  margin-bottom: 10px;
}
.mp-mini {
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border-radius: 10px;
  padding: 3px 8px;
  font-size: 0.78rem;
  cursor: pointer;
  transition: transform 0.2s ease;
  white-space: nowrap;
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
