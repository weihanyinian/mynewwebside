<script setup lang="ts">
import { useThemeStore } from '../../stores/theme'
import { computed } from 'vue'

const themeStore = useThemeStore()

const videoSrc = computed(() =>
  themeStore.isDark ? '/videos/dark.mp4' : '/videos/light.mp4'
)
</script>

<template>
  <div class="fixed inset-0 w-full h-full z-0 overflow-hidden">
    <!-- Video Background -->
    <video
      :key="videoSrc"
      autoplay
      loop
      muted
      playsinline
      class="absolute inset-0 w-full h-full object-cover"
    >
      <source :src="videoSrc" type="video/mp4" />
    </video>

    <!-- Video Overlay -->
    <div class="absolute inset-0 bg-black/40 dark:bg-black/50 transition-colors duration-500" />

    <!-- Gradient Overlay -->
    <div class="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-black/50" />
  </div>
</template>
