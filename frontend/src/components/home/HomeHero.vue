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
  background: rgba(255, 255, 255, 0.26);
  border: 1px solid rgba(255, 255, 255, 0.58);
  backdrop-filter: blur(16px) saturate(1.1);
  -webkit-backdrop-filter: blur(16px) saturate(1.1);
  box-shadow:
    0 16px 38px rgba(15, 23, 42, 0.1),
    0 0 22px color-mix(in srgb, var(--primary-color) 14%, transparent);
}
:global(.dark-theme) .hero-glass-card {
  background: rgba(15, 23, 42, 0.52);
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.42),
    0 0 22px color-mix(in srgb, #a78bfa 18%, transparent);
}
.hero-title {
  position: relative;
  font-size: clamp(1.75rem, 4.8vw, 2.85rem);
  letter-spacing: 0.01em;
  line-height: 1.2;
  margin-bottom: 1rem;
  font-weight: 700;
  color: #0f172a;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.75);
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
:global(.dark-theme) .hero-title {
  color: #f8fafc;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}
.hero-name-float {
  display: inline-block;
  color: #1d4ed8;
  font-weight: 800;
  -webkit-text-fill-color: currentColor;
  background: none;
  background-clip: unset;
  -webkit-background-clip: unset;
  text-shadow: none;
  animation: hero-name-float 4.8s ease-in-out infinite;
  animation-delay: 0.4s;
  -webkit-text-stroke: 0;
  filter: none;
  transition: color 0.3s ease;
}
:global(.dark-theme) .hero-name-float {
  color: #d8b4fe;
  -webkit-text-fill-color: currentColor;
  background: none;
  -webkit-text-stroke: 0;
}
.hero-title:hover .hero-name-float {
  animation: hero-name-float 4.8s ease-in-out infinite;
  color: #1e40af;
}
:global(.dark-theme) .hero-title:hover .hero-name-float {
  color: #e9d5ff;
}
.hero-subtitle {
  margin: 0 auto 1.2rem;
  font-size: 0.95rem;
  letter-spacing: 0.03em;
  line-height: 1.6;
  color: rgba(15, 23, 42, 0.78);
}
:global(.dark-theme) .hero-subtitle {
  color: rgba(226, 232, 240, 0.88);
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
  color: color-mix(in srgb, var(--text-color, #0f172a) 78%, #fff 22%);
  border: 1px solid color-mix(in srgb, var(--primary-color, #5b9bd8) 28%, rgba(255, 255, 255, 0.55));
  background: color-mix(in srgb, var(--primary-color, #5b9bd8) 10%, rgba(255, 255, 255, 0.42));
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
