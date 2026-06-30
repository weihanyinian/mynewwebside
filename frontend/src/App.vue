<script setup lang="ts">
import SiteTopNav from './components/site/SiteTopNav.vue'
import SiteFooter from './components/site/SiteFooter.vue'
import SiteBackground from './components/site/SiteBackground.vue'
import MusicPlayer from './components/MusicPlayer.vue'
import AiChatWidget from './components/AiChatWidget.vue'
</script>

<template>
  <div class="relative min-h-screen">
    <!-- Video Background -->
    <SiteBackground />

    <!-- Content Layer -->
    <div class="relative z-10 flex flex-col min-h-screen">
      <!-- Navigation -->
      <SiteTopNav />

      <!-- Music Player (top-left floating) -->
      <MusicPlayer />

      <!-- AI Chat (top-right floating) -->
      <AiChatWidget />

      <!-- Main Content -->
      <main class="flex-1">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <!-- Footer -->
      <SiteFooter />
    </div>
  </div>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
