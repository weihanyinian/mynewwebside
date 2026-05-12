<script setup lang="ts">
defineProps<{
  titlePrefix: string
  name: string
  exploreLabel: string
  readBlogLabel: string
  subtitle: string
  statsBadge?: string
  parallaxY: number
}>()

const emit = defineEmits<{
  explore: []
  readBlog: []
}>()
</script>

<template>
  <section class="hero" id="hero">
    <div class="hero-content hero-glass-card" :style="{ transform: `translate3d(0, ${parallaxY}px, 0)` }">
      <h1 class="hero-title hero-title--animate" :data-shadow="`${titlePrefix} ${name}`">
        {{ titlePrefix }} <span class="hero-name-float">{{ name }}</span>
      </h1>
      <p class="hero-subtitle">{{ subtitle }}</p>
      <p v-if="statsBadge" class="hero-stats">{{ statsBadge }}</p>
      <div class="hero-actions hero-actions--cta">
        <button type="button" class="site-pill site-pill--lg site-pill--active" @click="emit('explore')">{{ exploreLabel }}</button>
        <button type="button" class="site-pill site-pill--lg site-pill--secondary" @click="emit('readBlog')">{{ readBlogLabel }}</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 80px 20px 0;
  font-family:
    system-ui,
    -apple-system,
    'Segoe UI',
    'PingFang SC',
    'Microsoft YaHei',
    sans-serif;
}
.hero-content {
  will-change: transform;
  transition: transform 0.15s ease-out;
}
.hero-glass-card {
  position: relative;
  padding: clamp(28px, 4.2vw, 46px) clamp(22px, 4vw, 56px);
  border-radius: 28px;
  background: var(--hero-glass-bg);
  border: 1px solid var(--hero-glass-border);
  /* 使用固定 px，避免个别浏览器对 filter() 内 var() 解析异常导致毛玻璃消失 */
  backdrop-filter: blur(22px) saturate(1.12);
  -webkit-backdrop-filter: blur(22px) saturate(1.12);
  box-shadow: var(--hero-glass-shadow);
}
.hero-title {
  position: relative;
  font-size: clamp(1.75rem, 4.8vw, 2.85rem);
  letter-spacing: 0.01em;
  line-height: 1.2;
  margin-bottom: 1rem;
  font-weight: 700;
  color: var(--hero-title-color);
  text-shadow: var(--hero-title-shadow);
}
.hero-title::before,
.hero-title::after {
  display: none;
}
.hero-title--animate {
  animation: hero-title-in 0.95s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  opacity: 0;
}
@media (prefers-reduced-motion: reduce) {
  .hero-title--animate {
    animation: none;
    opacity: 1;
  }
}
@keyframes hero-title-in {
  from { opacity: 0; transform: translateY(28px); }
  to { opacity: 1; transform: translateY(0); }
}
.hero-name-float {
  display: inline-block;
  color: var(--hero-name-color);
  font-weight: 700;
  -webkit-text-fill-color: currentColor;
  background: none;
  background-clip: unset;
  -webkit-background-clip: unset;
  text-shadow: var(--hero-name-shadow);
  animation: hero-name-float 4.8s ease-in-out infinite;
  animation-delay: 0.4s;
  -webkit-text-stroke: 0;
  filter: none;
  transition: color 0.3s ease;
}
.hero-title:hover .hero-name-float {
  animation: hero-name-float 4.8s ease-in-out infinite;
  color: var(--hero-name-hover-color);
}
.hero-subtitle {
  margin: 0 auto 1.2rem;
  font-size: 0.98rem;
  font-weight: 500;
  letter-spacing: 0.02em;
  line-height: 1.65;
  color: var(--hero-subtitle-color);
  text-shadow: none;
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}
.hero-stats {
  margin: -0.2rem auto 1rem;
  width: fit-content;
  padding: 0.34rem 0.7rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--hero-stats-color);
  border: 1px solid var(--hero-stats-border);
  background: var(--hero-stats-bg);
}
.hero-actions--cta .site-pill {
  min-height: 46px;
  font-size: 0.9rem;
}
@keyframes hero-name-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-7px); }
}
</style>
