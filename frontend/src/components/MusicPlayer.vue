<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { musicApi } from '../api/music'

const expanded = ref(false)
const mode = ref<'search' | 'playlist'>('search')
const keyword = ref('')
const results = ref<any[]>([])
const playlists = ref<any[]>([])
const currentPlaylist = ref<any[]>([])
const playlistName = ref('')
const currentSong = ref<any>(null)
const lyrics = ref<{ time: number; text: string }[]>([])
const playing = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(80)
let audioEl: HTMLAudioElement | null = null
let audioUrl = ref('')

const progressPercent = computed(() => {
  if (!duration.value) return 0
  return (currentTime.value / duration.value) * 100
})

async function search() {
  if (!keyword.value.trim()) return
  try {
    const res = await musicApi.search(keyword.value)
    const songs = res.data?.data?.result?.songs || []
    results.value = (songs || []).slice(0, 20)
  } catch (e) {
    console.error('Search failed:', e)
  }
}

async function loadPlaylists() {
  try {
    const res = await musicApi.getTopPlaylists(10)
    playlists.value = res.data?.data?.playlists || []
  } catch (e) {
    console.error('Load playlists failed:', e)
  }
}

async function openPlaylist(id: number, name: string) {
  playlistName.value = name
  try {
    const res = await musicApi.getPlaylistDetail(id)
    const tracks = res.data?.data?.playlist?.tracks || []
    currentPlaylist.value = tracks
    mode.value = 'playlist'
  } catch (e) {
    console.error('Load playlist failed:', e)
  }
}

async function playSong(song: any) {
  try {
    const res = await musicApi.getSongUrl(song.id)
    const url = res.data?.data?.data?.[0]?.url
    if (!url) { alert('该歌曲无播放源'); return }
    audioUrl.value = url
    currentSong.value = song

    // Load lyrics
    try {
      const lyricRes = await musicApi.getLyric(song.id)
      const lrc = lyricRes.data?.data?.lrc?.lyric || ''
      lyrics.value = parseLyric(lrc)
    } catch (e) { lyrics.value = [] }

    // Create audio element
    if (audioEl) { audioEl.pause(); audioEl.src = '' }
    audioEl = new Audio(url)
    audioEl.volume = volume.value / 100
    audioEl.play()
    playing.value = true
    duration.value = 0

    audioEl.ontimeupdate = () => { currentTime.value = audioEl?.currentTime || 0 }
    audioEl.onloadedmetadata = () => { duration.value = audioEl?.duration || 0 }
    audioEl.onended = () => { playing.value = false }
    audioEl.onerror = () => { alert('播放失败') }
  } catch (e) {
    console.error('Play failed:', e)
  }
}

function togglePlay() {
  if (!audioEl) return
  if (playing.value) { audioEl.pause() } else { audioEl.play() }
  playing.value = !playing.value
}

function seek(e: MouseEvent) {
  if (!audioEl || !duration.value) return
  const bar = (e.currentTarget as HTMLElement)
  const rect = bar.getBoundingClientRect()
  const pct = (e.clientX - rect.left) / rect.width
  audioEl.currentTime = pct * duration.value
}

function setVolume(e: MouseEvent) {
  const bar = (e.currentTarget as HTMLElement)
  const rect = bar.getBoundingClientRect()
  volume.value = Math.floor(((e.clientX - rect.left) / rect.width) * 100)
  if (audioEl) audioEl.volume = volume.value / 100
}

function parseLyric(lrc: string): { time: number; text: string }[] {
  const lines = lrc.split('\n')
  const result: { time: number; text: string }[] = []
  for (const line of lines) {
    const match = line.match(/\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/)
    if (match) {
      const min = parseInt(match[1])
      const sec = parseInt(match[2])
      const ms = parseInt(match[3].padEnd(3, '0'))
      result.push({ time: min * 60 + sec + ms / 1000, text: match[4].trim() })
    }
  }
  return result
}

function formatTime(t: number) {
  const m = Math.floor(t / 60)
  const s = Math.floor(t % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

watch(expanded, (v) => {
  if (v && !playlists.value.length) loadPlaylists()
})
</script>

<template>
  <div class="fixed top-20 left-4 z-40">
    <!-- Collapsed bubble -->
    <button
      v-if="!expanded"
      @click="expanded = true"
      class="glass-button !p-3 !rounded-full text-xl animate-float"
      title="音乐播放器"
    >
      🎵
    </button>

    <!-- Expanded panel -->
    <div v-else class="glass-card p-4 w-80 max-h-[80vh] overflow-y-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-3">
        <span class="text-sm font-bold gradient-text">🎵 网易云音乐</span>
        <button @click="expanded = false" class="glass-button !p-1 !rounded-full text-xs !px-2">✕</button>
      </div>

      <!-- Now Playing -->
      <div v-if="currentSong" class="mb-3 p-2 rounded-lg bg-[var(--card-bg)]">
        <div class="text-xs font-medium truncate">{{ currentSong.name }}</div>
        <div class="text-[10px] text-[var(--text-muted)] truncate">
          {{ currentSong.ar?.map((a: any) => a.name).join('/') || currentSong.artists?.map((a: any) => a.name).join('/') }}
        </div>
        <!-- Progress -->
        <div @click="seek" class="h-1.5 bg-white/10 rounded-full mt-2 cursor-pointer relative">
          <div class="h-full bg-gradient-to-r from-[var(--primary)] to-[#a58eea] rounded-full" :style="{ width: progressPercent + '%' }"></div>
        </div>
        <div class="flex justify-between text-[10px] text-[var(--text-muted)] mt-0.5">
          <span>{{ formatTime(currentTime) }}</span>
          <span>{{ formatTime(duration) }}</span>
        </div>
        <!-- Controls -->
        <div class="flex items-center justify-center gap-3 mt-2">
          <button @click="togglePlay" class="glass-button !p-1.5 !rounded-full text-sm">
            {{ playing ? '⏸' : '▶️' }}
          </button>
          <!-- Volume -->
          <div @click="setVolume" class="flex-1 h-1 bg-white/10 rounded-full cursor-pointer max-w-20">
            <div class="h-full bg-white/30 rounded-full" :style="{ width: volume + '%' }"></div>
          </div>
          <span class="text-[10px] text-[var(--text-muted)]">🔊{{ volume }}</span>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex gap-2 mb-3">
        <button @click="mode = 'search'" :class="['text-xs glass-button !py-1 !px-3', mode === 'search' ? 'primary' : '']">🔍 搜索</button>
        <button @click="mode = 'playlist'; loadPlaylists()" :class="['text-xs glass-button !py-1 !px-3', mode === 'playlist' ? 'primary' : '']">📋 歌单</button>
      </div>

      <!-- Search -->
      <div v-if="mode === 'search'">
        <div class="flex gap-2 mb-3">
          <input v-model="keyword" @keyup.enter="search" class="glass-input flex-1 text-xs" placeholder="搜索歌曲..." />
          <button @click="search" class="glass-button primary text-xs !py-1 !px-3">搜索</button>
        </div>
        <div v-if="results.length" class="space-y-1 max-h-60 overflow-y-auto">
          <div
            v-for="s in results" :key="s.id"
            @click="playSong(s)"
            class="text-xs p-2 rounded cursor-pointer hover:bg-white/5 text-[var(--text-primary)] truncate"
          >
            {{ s.name }} - {{ s.ar?.map((a: any) => a.name).join('/') || s.artists?.map((a: any) => a.name).join('/') }}
          </div>
        </div>
      </div>

      <!-- Playlists -->
      <div v-if="mode === 'playlist'">
        <div class="space-y-1 max-h-60 overflow-y-auto">
          <div
            v-for="p in playlists" :key="p.id"
            @click="openPlaylist(p.id, p.name)"
            class="flex items-center gap-2 text-xs p-2 rounded cursor-pointer hover:bg-white/5"
          >
            <img v-if="p.coverImgUrl" :src="p.coverImgUrl" class="w-8 h-8 rounded object-cover" />
            <div class="flex-1 min-w-0">
              <div class="text-[var(--text-primary)] truncate">{{ p.name }}</div>
              <div class="text-[10px] text-[var(--text-muted)]">{{ p.trackCount }}首 · {{ (p.playCount / 10000).toFixed(0) }}万播放</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
