<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

type AlbumItem = { src: string; w: number; h: number; title?: string }
type AlbumManifest = { title?: string; items: AlbumItem[] }
const manifest = ref<AlbumManifest | null>(null)

async function load() {
  try {
    const res = await fetch('/albums/manifest.json')
    if (res.ok) manifest.value = await res.json()
    else manifest.value = null
  } catch {
    manifest.value = null
  }
}

onMounted(() => void load())
</script>

<template>
  <section class="section" id="albums">
    <h2>{{ manifest?.title || t('pages.albumsTitle') }}</h2>
    <div v-if="manifest?.items?.length" class="home-album-grid">
      <figure v-for="(it, i) in manifest.items" :key="i" class="home-album-item glass-card">
        <img :src="it.src" :alt="it.title || ''" loading="lazy" />
        <figcaption v-if="it.title">{{ it.title }}</figcaption>
      </figure>
    </div>
    <p v-else class="muted center">{{ t('home.homeAlbumFallback') }}</p>
  </section>
</template>

<style scoped>
.center { text-align: center; }
.home-album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
}
.home-album-item {
  margin: 0;
  padding: 0;
  overflow: hidden;
  border-radius: 16px;
}
.home-album-item img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  display: block;
}
.home-album-item figcaption {
  padding: 10px 12px;
  font-size: 0.85rem;
}
</style>
