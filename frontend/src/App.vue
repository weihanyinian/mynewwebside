<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, shallowRef } from 'vue'
import { useRoute } from 'vue-router'
import SiteLayout from './layouts/SiteLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'

const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))

/** 音乐播放器与头像浮标非首屏关键路径：空闲后再挂载，减轻首包解析与首帧工作 */
const showDeferredChrome = shallowRef(false)
onMounted(() => {
  const run = () => {
    showDeferredChrome.value = true
  }
  if (typeof requestIdleCallback === 'function') {
    requestIdleCallback(run, { timeout: 1800 })
  } else {
    setTimeout(run, 300)
  }
})

const AvatarBadge = defineAsyncComponent(() => import('./components/AvatarBadge.vue'))
const MusicPlayer = defineAsyncComponent(() => import('./components/MusicPlayer.vue'))
</script>

<template>
  <div>
    <router-view v-slot="{ Component }">
      <transition name="page-fade-slide" mode="out-in">
        <AdminLayout v-if="isAdmin && Component" :key="'a-' + route.fullPath">
          <component :is="Component" />
        </AdminLayout>
        <SiteLayout v-else-if="Component" :key="'s-' + route.fullPath">
          <component :is="Component" />
        </SiteLayout>
      </transition>
    </router-view>
    <template v-if="!isAdmin && showDeferredChrome">
      <AvatarBadge />
      <MusicPlayer />
    </template>
  </div>
</template>

<style>
/* 【页面过渡】略缩短时长，减少路由切换「拖沓感」 */
.page-fade-slide-enter-active,
.page-fade-slide-leave-active {
  transition:
    opacity 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1);
}
.page-fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}
.page-fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
