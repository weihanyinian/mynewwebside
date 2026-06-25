<script setup lang="ts">
import { useThemeStore } from '../../stores/theme'
import { computed } from 'vue'

const themeStore = useThemeStore()

// Day: dark.mp4 (初音), Night: light.mp4 (Redial)
const videoSrc = computed(() =>
  themeStore.isDark ? '/videos/light.mp4' : '/videos/dark.mp4'
)
</script>

<template>
  <div class="fixed inset-0 w-full h-full z-0 overflow-hidden">
    <!-- Fallback gradient background -->
    <div
      class="absolute inset-0 transition-all duration-700"
      :class="themeStore.isDark
        ? 'bg-gradient-to-br from-[#0b1022] via-[#1a1040] to-[#0b1022]'
        : 'bg-gradient-to-br from-[#e0e8f4] via-[#d8e2f2] to-[#e4dcf4]'"
    />

    <!-- Video Background -->
    <video
      :key="videoSrc"
      autoplay
      loop
      muted
      playsinline
      class="absolute inset-0 w-full h-full object-cover transition-opacity duration-700"
    >
      <source :src="videoSrc" type="video/mp4" />
    </video>

    <!-- Glass blur overlay (blurs video underneath) -->
    <div
      class="absolute inset-0 transition-all duration-700"
      style="backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);"
    />

    <!-- Dimming overlay: lighter in day, darker at night -->
    <div
      class="absolute inset-0 transition-all duration-700"
      :class="themeStore.isDark ? 'bg-black/45' : 'bg-black/20'"
    />

    <!-- Gradient fringe -->
    <div
      class="absolute inset-0 transition-colors duration-700"
      :class="themeStore.isDark
        ? 'bg-gradient-to-b from-black/20 via-transparent to-black/35'
        : 'bg-gradient-to-b from-black/15 via-transparent to-black/25'"
    />
  </div>
</template>
