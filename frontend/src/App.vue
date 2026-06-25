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

    <!-- Content Layer: translateZ(0) prevents GPU compositing glitches during screenshots -->
    <div class="relative z-10 flex flex-col min-h-screen" style="transform: translateZ(0); -webkit-transform: translateZ(0);"> 
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
