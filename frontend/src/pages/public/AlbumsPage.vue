<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import PhotoSwipeLightbox from 'photoswipe/lightbox'
import 'photoswipe/style.css'

type Manifest = {
  title?: string
  items: { src: string; w: number; h: number; title?: string }[]
}

const { t } = useI18n()
const loading = ref(true)
const err = ref('')
const manifest = ref<Manifest | null>(null)
let lightbox: PhotoSwipeLightbox | null = null

onMounted(async () => {
  loading.value = true
  try {
    const res = await fetch(`/albums/manifest.json?t=${Date.now()}`, { cache: 'no-store' })
    if (!res.ok) throw new Error(String(res.status))
    manifest.value = (await res.json()) as Manifest
  } catch (e) {
    err.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
  await nextTick()
  if (!manifest.value?.items?.length) return
  lightbox = new PhotoSwipeLightbox({
    gallery: '#album-pswp',
    children: 'a',
    pswpModule: () => import('photoswipe'),
  })
  lightbox.init()
})

onUnmounted(() => {
  lightbox?.destroy()
  lightbox = null
})
</script>

<template>
  <div class="albums-page">
    <h1 class="page-title">{{ manifest?.title || t('pages.albumsTitle') }}</h1>
    <p v-if="loading" class="muted">{{ t('pages.loading') }}</p>
    <p v-else-if="err" class="muted">{{ t('pages.loadError') }}: {{ err }}</p>
    <p v-else-if="!manifest?.items?.length" class="muted">{{ t('pages.albumsEmpty') }}</p>
    <div v-else id="album-pswp" class="album-grid">
      <a
        v-for="(it, i) in manifest.items"
        :key="i"
        :href="it.src"
        :data-pswp-width="it.w"
        :data-pswp-height="it.h"
        target="_blank"
        class="album-thumb card"
      >
        <img :src="it.src" :alt="it.title || `img-${i}`" loading="lazy" />
        <span v-if="it.title" class="album-cap">{{ it.title }}</span>
      </a>
    </div>
  </div>
</template>

<style scoped>
.albums-page {
  padding: 8px 0 32px;
}
.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 18px;
  margin-top: 20px;
}
.album-thumb {
  position: relative;
  display: block;
  padding: 0;
  overflow: hidden;
  border-radius: 16px;
  text-decoration: none;
  color: inherit;
  transition:
    transform 0.28s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.28s ease;
}
.album-thumb:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 44px rgba(102, 217, 255, 0.22);
}
.album-thumb img {
  width: 100%;
  aspect-ratio: 4/3;
  object-fit: cover;
  display: block;
}
.album-cap {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 12px;
  font-size: 0.78rem;
  font-weight: 700;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.65));
  color: #fff;
}
</style>
