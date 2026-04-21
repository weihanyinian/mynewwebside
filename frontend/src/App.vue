<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SiteLayout from './layouts/SiteLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'
import AvatarBadge from './components/AvatarBadge.vue'
import MusicPlayer from './components/MusicPlayer.vue'
const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))
const isPortfolio = computed(() => route.path === '/')
</script>

<template>
  <div>
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
      <AvatarBadge />
      <MusicPlayer />
    </template>
  </div>
</template>

<style>
/* 【页面过渡】路由切换：淡入淡出 + 轻微位移 */
.page-fade-slide-enter-active,
.page-fade-slide-leave-active {
  transition:
    opacity 0.42s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.42s cubic-bezier(0.22, 1, 0.36, 1);
}
.page-fade-slide-enter-from {
  opacity: 0;
  transform: translateY(18px);
}
.page-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
