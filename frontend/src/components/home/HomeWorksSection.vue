<script setup lang="ts">
import type { HomeWorkItem } from '../../types/home'

defineProps<{
  title: string
  detailLabel: string
  works: HomeWorkItem[]
  categories: string[]
  selectedCategory: string
}>()

const emit = defineEmits<{
  open: [work: HomeWorkItem, event?: Event]
  changeCategory: [category: string]
}>()
</script>

<template>
  <section class="section" id="works">
    <div class="works-head">
      <h2 class="section-title-pill">{{ title }}</h2>
      <RouterLink class="works-entry site-pill site-pill--secondary" to="/works-showcase">
        进入作品详情页
      </RouterLink>
    </div>
    <div class="works-filters">
      <button
        v-for="category in categories"
        :key="category"
        type="button"
        class="works-filter-chip"
        :class="{ 'works-filter-chip--active': selectedCategory === category }"
        @click="emit('changeCategory', category)"
      >
        {{ category === 'all' ? '全部' : category }}
      </button>
    </div>
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
.works-head {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 0.25rem;
}

.section-title-pill {
  width: fit-content;
  max-width: min(92vw, 560px);
  margin: 0 auto 1.15rem;
  padding: 0.48rem 1.05rem;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--primary-color, #4a90e2) 34%, rgba(255, 255, 255, 0.7));
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.36), rgba(255, 255, 255, 0.16));
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: color-mix(in srgb, var(--primary-color, #4a90e2) 62%, #1e293b 38%);
  -webkit-text-fill-color: currentColor;
  background-clip: border-box;
  -webkit-background-clip: border-box;
  text-shadow: 0 1px 10px rgba(74, 144, 226, 0.16);
  box-shadow: 0 8px 26px rgba(74, 144, 226, 0.1);
}
.works-entry {
  min-height: 40px;
  border-radius: 12px;
  font-size: 0.86rem;
  font-weight: 700;
  padding: 0.45rem 0.95rem;
  text-decoration: none;
}
:root[data-theme='dark'] .section-title-pill {
  border-color: rgba(167, 139, 250, 0.42);
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.5), rgba(15, 23, 42, 0.42));
  color: #ddd6fe;
  text-shadow: 0 1px 14px rgba(167, 139, 250, 0.22);
}

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
.works-filters {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin: -6px 0 20px;
}
.works-filter-chip {
  border: 1px solid color-mix(in srgb, var(--primary-color, #5b9bd8) 30%, rgba(255, 255, 255, 0.65));
  background: rgba(255, 255, 255, 0.28);
  color: var(--text-color, #0f172a);
  border-radius: 999px;
  font-size: 0.8rem;
  padding: 0.35rem 0.78rem;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}
.works-filter-chip:hover {
  transform: translateY(-1px);
  border-color: var(--primary-color, #5b9bd8);
}
.works-filter-chip--active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(120deg, var(--primary-color, #5b9bd8), var(--secondary-color, #9b8fd4));
  box-shadow: 0 8px 20px rgba(91, 155, 216, 0.28);
}
.work-card {
  padding: 0;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  outline: none;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: rgba(255, 255, 255, 0.24);
  box-shadow: 0 12px 34px rgba(51, 65, 85, 0.08);
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.32), rgba(255, 255, 255, 0.2));
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

:root[data-theme='dark'] .works-filter-chip {
  border-color: rgba(196, 181, 253, 0.35);
  background: rgba(30, 41, 59, 0.52);
  color: rgba(241, 245, 249, 0.92);
}

:root[data-theme='dark'] .works-filter-chip:hover {
  border-color: rgba(167, 139, 250, 0.75);
}

:root[data-theme='dark'] .work-tag {
  background: rgba(167, 139, 250, 0.2);
  color: #ddd6fe;
}

:root[data-theme='dark'] .work-card {
  border-color: rgba(148, 163, 184, 0.28);
  background: rgba(15, 23, 42, 0.5);
  box-shadow: 0 12px 34px rgba(0, 0, 0, 0.38);
}

:root[data-theme='dark'] .work-card__body {
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.52), rgba(15, 23, 42, 0.42));
}

:root[data-theme='dark'] .work-card h3 {
  color: #f8fafc;
  text-shadow: 0 1px 10px rgba(167, 139, 250, 0.22);
}

:root[data-theme='dark'] .work-card__excerpt {
  color: rgba(226, 232, 240, 0.9);
}

:root[data-theme='dark'] .work-link {
  color: #93c5fd;
}
</style>
