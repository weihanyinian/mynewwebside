<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SiteLayout from './layouts/SiteLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'

const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))
</script>

<template>
  <AdminLayout v-if="isAdmin">
    <router-view />
  </AdminLayout>
  <SiteLayout v-else>
    <router-view />
  </SiteLayout>
</template>
