<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

function back() {
  router.push('/tools')
}
</script>

<template>
  <div class="tool-back-bar">
    <button type="button" class="site-pill site-pill--nav tool-back-bar__btn" @click="back">
      ← {{ t('toolsHub.back') }}
    </button>
  </div>
</template>

<style scoped>
.tool-back-bar {
  margin-bottom: 1rem;
}
.tool-back-bar__btn {
  cursor: pointer;
  border: none;
  font: inherit;
}
</style>
