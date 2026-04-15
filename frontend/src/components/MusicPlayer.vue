<script setup lang="ts">
import { ref, onMounted } from 'vue'
import playlistData from '../assets/playlist.json'

const playlist = playlistData

const audioRef = ref<HTMLAudioElement | null>(null)
const isPlaying = ref(false)
const currentIndex = ref(0)
const isExpanded = ref(false)
const volume = ref(0.5)

function togglePlay() {
  if (!audioRef.value) return
  if (isPlaying.value) {
    audioRef.value.pause()
  } else {
    audioRef.value.play().catch(err => {
      console.log('Autoplay prevented', err)
    })
  }
  isPlaying.value = !isPlaying.value
}

function playNext() {
  currentIndex.value = (currentIndex.value + 1) % playlist.length
  if (isPlaying.value && audioRef.value) {
    setTimeout(() => {
      audioRef.value?.play()
    }, 50)
  }
}

function handleEnded() {
  playNext()
}

function updateVolume() {
  if (audioRef.value) {
    audioRef.value.volume = volume.value
  }
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

onMounted(() => {
  if (audioRef.value) {
    audioRef.value.volume = volume.value
  }
})
</script>

<template>
  <div class="music-player-wrapper" :class="{ 'is-expanded': isExpanded }">
    <div class="music-trigger glass-ui" @click="toggleExpand">
      <span class="music-icon" :class="{ 'spin-anim': isPlaying }">🎵</span>
    </div>
    
    <div class="music-panel glass-ui">
      <div class="panel-header">
        <span class="song-title">{{ playlist[currentIndex].title }}</span>
        <button class="close-btn" @click="toggleExpand">✕</button>
      </div>
      
      <div class="controls">
        <button class="control-btn" @click="togglePlay">
          {{ isPlaying ? '⏸' : '▶' }}
        </button>
        <button class="control-btn" @click="playNext">⏭</button>
      </div>
      
      <div class="volume-control">
        <span class="vol-icon">🔊</span>
        <input type="range" v-model="volume" min="0" max="1" step="0.01" @input="updateVolume" class="vol-slider" />
      </div>
    </div>

    <audio 
      ref="audioRef" 
      :src="playlist[currentIndex].url" 
      @ended="handleEnded"
      @play="isPlaying = true"
      @pause="isPlaying = false"
    ></audio>
  </div>
</template>

<style scoped>
.music-player-wrapper {
  position: fixed;
  left: 20px;
  bottom: 20px;
  z-index: 998;
  display: flex;
  align-items: flex-end;
  gap: 15px;
  transition: all 0.3s ease;
}

.glass-ui {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 15px rgba(102, 217, 255, 0.2);
  border-radius: 16px;
  color: #fff;
}

.music-trigger {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.3) 0%, rgba(252, 162, 229, 0.3) 100%);
  transition: all 0.3s;
}
.music-trigger:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 217, 255, 0.4);
}

.music-icon {
  font-size: 1.5rem;
  display: inline-block;
}
.spin-anim {
  animation: spin 3s linear infinite;
}
@keyframes spin {
  100% { transform: rotate(360deg); }
}

.music-panel {
  width: 220px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: absolute;
  left: 60px;
  bottom: 0;
  opacity: 0;
  pointer-events: none;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.is-expanded .music-panel {
  opacity: 1;
  pointer-events: auto;
  transform: translateX(0);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.song-title {
  font-size: 0.9rem;
  font-weight: bold;
  color: #66d9ff;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}
.close-btn {
  background: none;
  border: none;
  color: rgba(255,255,255,0.6);
  cursor: pointer;
}
.close-btn:hover {
  color: #fca2e5;
}

.controls {
  display: flex;
  justify-content: center;
  gap: 15px;
}
.control-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}
.control-btn:hover {
  background: rgba(102, 217, 255, 0.4);
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
}
.vol-icon {
  font-size: 0.8rem;
}
.vol-slider {
  flex: 1;
  accent-color: #fca2e5;
}

@media (max-width: 480px) {
  .music-player-wrapper {
    left: 10px;
    bottom: 10px;
  }
  .music-trigger {
    width: 40px;
    height: 40px;
  }
  .music-icon {
    font-size: 1.2rem;
  }
  .music-panel {
    left: 50px;
    width: 200px;
  }
}
</style>
