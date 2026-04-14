<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SiteLayout from './layouts/SiteLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'
import Mascot from './components/Mascot.vue'
import MusicPlayer from './components/MusicPlayer.vue'

const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))
const isPortfolio = computed(() => route.path === '/')
</script>

<template>
  <AdminLayout v-if="isAdmin">
    <router-view />
  </AdminLayout>
  <template v-else>
    <router-view v-if="isPortfolio" />
    <SiteLayout v-else>
      <router-view />
    </SiteLayout>
    <Mascot />
    <MusicPlayer />
  </template>
</template>
