<script setup lang="ts">
import ToolBackBar from '../../components/tools/ToolBackBar.vue'
import MarkmapEditor from '../../components/tools/MarkmapEditor.vue'
</script>

<template>
  <div class="tool-markmap-page">
    <ToolBackBar />
    <MarkmapEditor />
  </div>
</template>

<style scoped>
.tool-markmap-page {
  max-width: 1400px;
  margin: 0 auto;
  padding-bottom: 2rem;
}
</style>
