<script setup lang="ts">
defineProps<{
  titlePrefix: string
  name: string
  exploreLabel: string
  readBlogLabel: string
  subtitle: string
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
}
.hero-content {
  will-change: transform;
  transition: transform 0.15s ease-out;
}
.hero-glass-card {
  position: relative;
  padding: clamp(28px, 4.2vw, 46px) clamp(22px, 4vw, 56px);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid color-mix(in srgb, #7dd3fc 40%, rgba(255, 255, 255, 0.5));
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow:
    0 16px 38px rgba(15, 23, 42, 0.1),
    0 0 22px color-mix(in srgb, var(--primary-color) 18%, transparent);
}
:global(.dark-theme) .hero-glass-card {
  background: rgba(15, 23, 42, 0.22);
  border-color: color-mix(in srgb, #93c5fd 45%, rgba(255, 255, 255, 0.22));
}
.hero-title {
  position: relative;
  font-size: clamp(1.86rem, 5.2vw, 3.22rem);
  letter-spacing: -0.03em;
  line-height: 1.12;
  margin-bottom: 0.5rem;
  font-weight: 800;
  color: #0f172a;
}
.hero-title::before,
.hero-title::after {
  content: attr(data-shadow);
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: -1;
  opacity: 0.55;
}
.hero-title::before {
  transform: translate3d(-2px, -2px, 0);
  color: rgba(110, 231, 255, 0.6);
}
.hero-title::after {
  transform: translate3d(2px, 2px, 0);
  color: rgba(167, 139, 250, 0.55);
}
.hero-title--animate {
  animation: hero-title-in 0.95s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  opacity: 0;
}
.hero-name-float {
  display: inline-block;
  background: linear-gradient(to right, var(--primary-color, #4a90e2), var(--secondary-color, #8b7fd8));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: hero-name-float 4.8s ease-in-out infinite;
  -webkit-text-stroke: 1px rgba(255, 255, 255, 0.48);
}
.hero-title:hover .hero-name-float {
  animation:
    hero-name-float 4.8s ease-in-out infinite,
    hero-name-flow 1.8s linear infinite;
}
.hero-subtitle {
  margin: 0 auto 1.2rem;
  font-size: 0.92rem;
  letter-spacing: 0.08em;
  color: color-mix(in srgb, var(--text-muted, #64748b) 90%, #fff 10%);
}
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}
.hero-actions--cta .site-pill {
  min-height: 46px;
  font-size: 0.9rem;
}
@keyframes hero-title-in {
  from { opacity: 0; transform: translateY(28px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes hero-name-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-7px); }
}
@keyframes hero-name-flow {
  0% { filter: hue-rotate(0deg); }
  100% { filter: hue-rotate(45deg); }
}
</style>
