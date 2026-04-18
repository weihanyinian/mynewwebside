<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SiteLayout from './layouts/SiteLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'
import Mascot from './components/Mascot.vue'
import MusicPlayer from './components/MusicPlayer.vue'
import SiteGlassSidebar from './components/site/SiteGlassSidebar.vue'

const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))
const isPortfolio = computed(() => route.path === '/')
</script>

<template>
  <SiteGlassSidebar v-if="!isAdmin" />
  <router-view v-slot="{ Component }">
    <transition name="page-fade-slide" mode="out-in">
      <AdminLayout v-if="isAdmin && Component" :key="'a-' + route.fullPath">
        <component :is="Component" />
      </AdminLayout>
      <component :is="Component" v-else-if="isPortfolio && Component" :key="'p-' + route.fullPath" />
      <SiteLayout v-else-if="Component" :key="'s-' + route.fullPath">
        <component :is="Component" />
      </SiteLayout>
    </transition>
  </router-view>
  <template v-if="!isAdmin">
    <Mascot />
    <MusicPlayer />
  </template>
</template>

<style>
/* 【页面过渡】路由切换：淡入淡出 + 轻微位移 */
.page-fade-slide-enter-active,
.page-fade-slide-leave-active {
  transition:
    opacity 0.34s ease,
    transform 0.34s ease;
}
.page-fade-slide-enter-from {
  opacity: 0;
  transform: translateY(14px);
}
.page-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
