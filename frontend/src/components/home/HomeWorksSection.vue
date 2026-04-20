<script setup lang="ts">
import type { HomeWorkItem } from '../../types/home'

defineProps<{
  title: string
  detailLabel: string
  works: HomeWorkItem[]
}>()

const emit = defineEmits<{
  open: [work: HomeWorkItem, event?: Event]
}>()
</script>

<template>
  <section class="section" id="works">
    <h2>{{ title }}</h2>
    <div class="works-grid">
      <article
        v-for="(work, index) in works"
        :key="index"
        class="glass-card work-card site-module-card"
        role="link"
        tabindex="0"
        :aria-label="work.title"
        @click="emit('open', work, $event)"
        @keydown.enter.prevent="emit('open', work, $event)"
        @keydown.space.prevent="emit('open', work, $event)"
      >
        <div class="work-card__media">
          <img :src="work.cover" :alt="''" loading="lazy" decoding="async" class="work-card__img" />
          <div class="work-card__overlay" :aria-hidden="true">
            <span class="work-card__overlay-tag">{{ work.tag }}</span>
            <p class="work-card__overlay-title">{{ work.title }}</p>
            <p class="work-card__detail">{{ work.desc }}</p>
          </div>
        </div>
        <div class="work-card__body">
          <span class="work-tag">{{ work.tag }}</span>
          <h3>{{ work.title }}</h3>
          <p class="work-card__excerpt">{{ work.desc }}</p>
          <span class="work-link">{{ detailLabel }} →</span>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.section {
  max-width: 1080px;
  margin: 0 auto;
  padding: 80px 20px;
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.55s ease, transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
}
.section.section--visible {
  opacity: 1;
  transform: translateY(0);
}
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-top: 8px;
}
.work-card {
  padding: 0;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  outline: none;
}
.work-card__media {
  position: relative;
  height: 180px;
  overflow: hidden;
}
.work-card__media::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: repeating-linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0 1px, rgba(255, 255, 255, 0) 1px 4px);
  opacity: 0;
  transition: opacity 0.35s ease;
}
.work-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.45s cubic-bezier(0.22, 1, 0.36, 1);
}
.work-card__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 6px;
  padding: 16px;
  background: linear-gradient(to top, rgba(15, 23, 42, 0.88), transparent 52%);
  opacity: 0;
  transition: opacity 0.35s ease;
}
.work-card__overlay-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.32);
  color: #fff;
  font-size: 0.74rem;
  font-weight: 700;
}
.work-card__overlay-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 800;
  color: #fff;
}
.work-card__detail {
  margin: 0;
  font-size: 0.86rem;
  line-height: 1.55;
  color: rgba(248, 250, 252, 0.92);
}
.work-card:hover .work-card__overlay,
.work-card:focus-visible .work-card__overlay {
  opacity: 1;
}
.work-card:hover .work-card__img,
.work-card:focus-visible .work-card__img {
  transform: scale(1.08);
  filter: contrast(1.08) saturate(1.08) hue-rotate(-4deg);
}
.work-card:hover .work-card__media::after,
.work-card:focus-visible .work-card__media::after {
  opacity: 0.85;
}
.work-card__body {
  padding: 22px 24px 24px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.work-tag {
  font-size: 0.8rem;
  letter-spacing: 1px;
  background: color-mix(in srgb, var(--primary-color, #4a90e2) 18%, transparent);
  color: var(--primary-color, #4a90e2);
  padding: 4px 10px;
  border-radius: 12px;
  align-self: flex-start;
  margin-bottom: 12px;
  font-weight: 700;
}
.work-card h3 {
  font-size: 1.26rem;
  margin: 0 0 8px;
  font-weight: 800;
}
.work-card__excerpt {
  color: rgba(15, 23, 42, 0.76);
  margin: 0 0 16px;
  flex-grow: 1;
  line-height: 1.62;
  font-size: 0.92rem;
}
.work-link {
  font-weight: 700;
  font-size: 0.92rem;
  color: var(--primary-color, #4a90e2);
}
</style>
