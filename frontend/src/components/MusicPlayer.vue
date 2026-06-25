<script setup lang="ts">
import { ref } from 'vue'

const expanded = ref(false)
</script>

<template>
  <!-- Music player: top-left floating -->
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
    <div v-else class="glass-card p-4 w-72">
      <div class="flex items-center justify-between mb-3">
        <span class="text-sm font-bold gradient-text">🎵 音乐播放器</span>
        <button @click="expanded = false" class="glass-button !p-1 !rounded-full text-xs !px-2">
          ✕
        </button>
      </div>
      <p class="text-xs text-[var(--text-secondary)]">
        网易云音乐接入中...<br />
        部署后可播放歌单音乐
      </p>
    </div>
  </div>
</template>
