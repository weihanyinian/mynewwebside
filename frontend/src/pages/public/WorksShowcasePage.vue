<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorksStore } from '../../stores/works'

const router = useRouter()
const worksStore = useWorksStore()

const works = computed(() => worksStore.works)

function openLink(link: string) {
  if (link.startsWith('/')) {
    void router.push(link)
    return
  }
  if (link.startsWith('http://') || link.startsWith('https://')) {
    window.open(link, '_blank', 'noopener,noreferrer')
    return
  }
  void router.push('/')
}
</script>

<template>
  <main class="works-showcase">
    <header class="works-showcase__hero glass-panel">
      <h1>作品全景展示</h1>
      <p>这里集中展示我的项目背景、核心能力点与落地价值，方便你快速了解每个作品的完整信息。</p>
      <button type="button" class="site-pill site-pill--secondary" @click="router.push('/')">返回首页</button>
    </header>

    <section class="works-showcase__grid">
      <article v-for="(work, idx) in works" :key="`${work.title}-${idx}`" class="showcase-card glass-panel">
        <img :src="work.cover" :alt="work.title" class="showcase-card__cover" loading="lazy" decoding="async" />
        <div class="showcase-card__body">
          <span class="showcase-card__tag">{{ work.tag }}</span>
          <h2>{{ work.title }}</h2>
          <p class="showcase-card__desc">{{ work.desc }}</p>
          <p class="showcase-card__detail">{{ work.detail }}</p>
          <button type="button" class="site-pill site-pill--active showcase-card__btn" @click="openLink(work.link)">
            查看该作品
          </button>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.works-showcase {
  max-width: 1120px;
  margin: 0 auto;
  padding: 92px 20px 48px;
  color: var(--text-color, #0f172a);
}

.glass-panel {
  border-radius: 20px;
  border: 1px solid rgba(148, 163, 184, 0.26);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: 0 12px 34px rgba(51, 65, 85, 0.1);
}

.works-showcase__hero {
  text-align: center;
  padding: 1.35rem 1.25rem 1.55rem;
  margin-bottom: 1rem;
}

.works-showcase__hero h1 {
  margin: 0 0 0.7rem;
  font-size: clamp(1.4rem, 2.6vw, 2rem);
  color: color-mix(in srgb, var(--primary-color, #4a90e2) 65%, #0f172a 35%);
}

.works-showcase__hero p {
  margin: 0 auto 1rem;
  max-width: 760px;
  line-height: 1.75;
  font-size: 0.95rem;
  color: color-mix(in srgb, var(--text-color, #0f172a) 80%, var(--primary-color, #4a90e2) 20%);
}

.works-showcase__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.showcase-card {
  overflow: hidden;
}

.showcase-card__cover {
  width: 100%;
  height: 174px;
  object-fit: cover;
  display: block;
}

.showcase-card__body {
  padding: 1rem 1rem 1.1rem;
}

.showcase-card__tag {
  display: inline-flex;
  margin-bottom: 0.55rem;
  border-radius: 999px;
  padding: 0.22rem 0.62rem;
  font-size: 0.75rem;
  font-weight: 700;
  background: color-mix(in srgb, var(--primary-color, #4a90e2) 18%, transparent);
  color: var(--primary-color, #4a90e2);
}

.showcase-card h2 {
  margin: 0 0 0.45rem;
  font-size: 1.15rem;
  line-height: 1.34;
  color: var(--text-color, #0f172a);
}

.showcase-card__desc {
  margin: 0 0 0.45rem;
  line-height: 1.65;
  color: rgba(15, 23, 42, 0.84);
}

.showcase-card__detail {
  margin: 0 0 0.82rem;
  line-height: 1.68;
  font-size: 0.92rem;
  color: rgba(15, 23, 42, 0.74);
}

.showcase-card__btn {
  width: 100%;
}

:root[data-theme='dark'] .works-showcase {
  color: #e2e8f0;
}

:root[data-theme='dark'] .glass-panel {
  border-color: rgba(148, 163, 184, 0.24);
  background: rgba(15, 23, 42, 0.52);
  box-shadow: 0 12px 34px rgba(0, 0, 0, 0.4);
}

:root[data-theme='dark'] .works-showcase__hero h1 {
  color: #e2e8f0;
}

:root[data-theme='dark'] .works-showcase__hero p {
  color: rgba(226, 232, 240, 0.92);
}

:root[data-theme='dark'] .showcase-card__tag {
  background: rgba(167, 139, 250, 0.2);
  color: #ddd6fe;
}

:root[data-theme='dark'] .showcase-card h2 {
  color: #f8fafc;
}

:root[data-theme='dark'] .showcase-card__desc {
  color: rgba(226, 232, 240, 0.9);
}

:root[data-theme='dark'] .showcase-card__detail {
  color: rgba(203, 213, 225, 0.88);
}
</style>
