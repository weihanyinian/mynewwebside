<script setup lang="ts">
defineProps<{
  title?: string
  hover?: boolean
}>()
</script>

<template>
  <div class="glass-card p-6" :class="{ 'hover:scale-[1.02]': hover !== false }">
    <h3 v-if="title" class="text-lg font-bold mb-3 gradient-text">{{ title }}</h3>
    <slot />
  </div>
</template>
