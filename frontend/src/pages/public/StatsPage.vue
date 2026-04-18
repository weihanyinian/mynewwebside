<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/user'
import { getToken } from '../../utils/token'

const { t } = useI18n()
const userStore = useUserStore()
const { isAdmin } = storeToRefs(userStore)

onMounted(async () => {
  if (getToken()) {
    try {
      await userStore.fetchMe()
    } catch {
      /* ignore */
    }
  }
})

const shareUrl = computed(() => (import.meta.env.VITE_UMAMI_SHARE_URL as string) || '')
const dashUrl = computed(() => (import.meta.env.VITE_UMAMI_DASHBOARD_URL as string) || '')
</script>

<template>
  <div class="stats-page card">
    <h1 class="page-title">{{ t('pages.statsTitle') }}</h1>
    <p class="muted">{{ t('pages.statsHint') }}</p>
    <div v-if="shareUrl" class="frame-wrap">
      <iframe :src="shareUrl" class="umami-frame" title="Umami" />
    </div>
    <p v-else class="muted box">{{ t('pages.statsHint') }}</p>
    <div v-if="isAdmin && dashUrl" class="actions">
      <a :href="dashUrl" target="_blank" rel="noopener noreferrer" class="site-pill site-pill--active">
        {{ t('pages.statsAdmin') }}
      </a>
    </div>
    <p v-else-if="shareUrl" class="muted small">
      <a :href="shareUrl" target="_blank" rel="noopener noreferrer">{{ t('pages.statsPublic') }}</a>
    </p>
  </div>
</template>

<style scoped>
.stats-page {
  padding: 24px;
}
.frame-wrap {
  margin-top: 20px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--glass-border);
  min-height: 420px;
}
.umami-frame {
  width: 100%;
  height: 560px;
  border: none;
  background: transparent;
}
.box {
  margin-top: 16px;
  padding: 20px;
  border-radius: 14px;
  border: 1px dashed var(--glass-border);
}
.actions {
  margin-top: 16px;
}
.small {
  margin-top: 12px;
  font-size: 0.85rem;
}
</style>
