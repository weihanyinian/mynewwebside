<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const isDark = ref(false)

import bgDayFallback from '../../assets/images/bg-day.jpg'
import bgNightFallback from '../../assets/images/bg-night.jpg'
import aboutForestFallback from '../../assets/images/about-forest.jpg'

const imageUrls = import.meta.glob('/src/assets/images/*.{png,jpg,jpeg,gif,webp,avif,svg}', {
  eager: true,
  import: 'default'
}) as Record<string, string>

function pickByPrefix(prefix: string): string[] {
  return Object.entries(imageUrls)
    .filter(([k]) => (k.split('/').pop() || '').startsWith(prefix))
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([, url]) => url)
}

const dayImages = computed(() => pickByPrefix('317'))
const nightImages = computed(() => pickByPrefix('315'))

const bgDay = computed(() => dayImages.value[0] || bgDayFallback)
const bgNight = computed(() => nightImages.value[0] || bgNightFallback)
const aboutImg = computed(() => dayImages.value[1] || aboutForestFallback)
const contactImg = computed(() => nightImages.value[1] || nightImages.value[0] || bgNightFallback)

const containerStyle = computed(() => ({
  '--bg-day': `url(${bgDay.value})`,
  '--bg-night': `url(${bgNight.value})`
}))

const works = ref([
  { title: 'MyWebSide Blog', desc: '基于 Spring Boot 3 + Vue 3 的全栈博客', link: '/blog', tag: 'Full Stack' },
  { title: 'Awesome Tool', desc: '一个能极大提升效率的命令行工具', link: '#', tag: 'CLI' },
  { title: 'Data Dashboard', desc: '基于 ECharts 封装的开源数据可视化面板', link: '#', tag: 'Frontend' }
])

const skills = ref([
  'Vue 3', 'TypeScript', 'Spring Boot', 'MySQL', 'Java', 'CSS / Sass', 'Docker', 'Git'
])

// Smooth scroll for nav anchors
function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

function toggleTheme() {
  isDark.value = !isDark.value
}
</script>

<template>
  <div class="portfolio-container" :class="{ 'dark-theme': isDark }" :style="containerStyle">
    <!-- Navbar -->
    <nav class="glass-nav">
      <div class="nav-inner">
        <div class="logo">MyPortfolio</div>
        <div class="links">
          <a @click="scrollTo('about')">关于</a>
          <a @click="scrollTo('skills')">技能</a>
          <a @click="scrollTo('works')">作品</a>
          <a @click="scrollTo('contact')">联系</a>
          <a @click="toggleTheme" class="theme-toggle" :title="isDark ? '切换到白昼' : '切换到星夜'">
            {{ isDark ? '🌙' : '☀️' }}
          </a>
          <a @click="router.push('/blog')" class="blog-btn">前往博客</a>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero" id="hero">
      <div class="hero-content">
        <h1 class="hero-title">你好，我是 <span>Developer</span></h1>
        <div class="hero-actions">
          <button class="btn-primary" @click="scrollTo('works')">探索作品</button>
          <button class="btn-outline" @click="router.push('/blog')">阅读博客</button>
        </div>
      </div>
    </section>

    <!-- About Section -->
    <section class="section" id="about">
      <div class="glass-card about-card">
        <h2>个人简介</h2>
        <div class="about-content">
          <img :src="aboutImg" alt="about" class="about-img" />
          <div class="about-text">
            <p>
              我是一名前端与后端兼修的开发者，熟练掌握 Vue 3 生态以及 Spring Boot 后端技术栈。
              喜欢将优雅的界面设计与健壮的后台逻辑相结合。
            </p>
            <br />
            <p>
              就像在漫长旅途中不断收集新魔法一样，我也在不断学习和分享开源项目，希望通过代码让世界变得更美好一点。
              如果生命足够漫长，我希望能一直敲着代码，直到魔法时代的尽头。
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Skills Section -->
    <section class="section" id="skills">
      <h2>魔法技能 (Skills)</h2>
      <div class="skills-grid">
        <div v-for="(skill, index) in skills" :key="index" class="glass-card skill-card">
          {{ skill }}
        </div>
      </div>
    </section>

    <!-- Works Section -->
    <section class="section" id="works">
      <h2>作品展示</h2>
      <div class="works-grid">
        <div v-for="(work, index) in works" :key="index" class="glass-card work-card">
          <span class="work-tag">{{ work.tag }}</span>
          <h3>{{ work.title }}</h3>
          <p>{{ work.desc }}</p>
          <a v-if="work.link.startsWith('/')" @click="router.push(work.link)" class="work-link">查看详情 &rarr;</a>
          <a v-else :href="work.link" class="work-link">查看详情 &rarr;</a>
        </div>
      </div>
    </section>

    <!-- Contact Section -->
    <section class="section" id="contact">
      <div class="glass-card contact-card">
        <h2>联系我</h2>
        <p>期待与你交流技术，探讨合作可能。旅途还在继续，一起创造新的魔法吧。</p>

        <img :src="contactImg" alt="contact" class="contact-img" />

        <div class="contact-links">
          <a href="https://github.com" target="_blank">GitHub</a>
          <a href="mailto:hello@example.com">Email</a>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <p>© {{ new Date().getFullYear() }} MyPortfolio. Built with Vue 3 & Vite.</p>
    </footer>
  </div>
</template>

<style scoped>
/* 
  Base Theme & Gradient Background (Light/Frieren Day Theme)
*/
.portfolio-container {
  min-height: 100vh;
  /* Light and airy with a touch of magic */
  background: linear-gradient(135deg, rgba(230, 238, 245, 0.8) 0%, rgba(200, 218, 235, 0.9) 100%);
  background-image: var(--bg-day), linear-gradient(135deg, rgba(230, 238, 245, 0.5) 0%, rgba(200, 218, 235, 0.7) 100%);
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  background-blend-mode: overlay;
  color: #2c3e50;
  font-family: system-ui, -apple-system, sans-serif;
  overflow-x: hidden;
  transition: background 0.5s ease, color 0.5s ease;
}

/* 
  Dark Theme (Frieren Night Theme)
*/
.portfolio-container.dark-theme {
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.9) 0%, rgba(42, 27, 61, 0.95) 100%);
  background-image: var(--bg-night), linear-gradient(135deg, rgba(26, 26, 46, 0.7) 0%, rgba(42, 27, 61, 0.8) 100%);
  color: #e2e8f0;
}

/* Typography */
h1, h2, h3 {
  font-weight: 700;
  margin-bottom: 1rem;
}
h2 {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 2rem;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.dark-theme h2 {
  background: linear-gradient(to right, #a18cd1, #fbc2eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* Glassmorphism Mixin */
.glass-nav, .glass-card {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px 0 rgba(74, 144, 226, 0.15);
  transition: all 0.3s ease;
}
.dark-theme .glass-nav, .dark-theme .glass-card {
  background: rgba(16, 18, 27, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.5);
}

/* Navigation */
.glass-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  border-radius: 0 0 16px 16px;
  border-top: none;
}
.nav-inner {
  max-width: 1080px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.logo {
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.dark-theme .logo {
  background: linear-gradient(to right, #a18cd1, #fbc2eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.links {
  display: flex;
  gap: 24px;
  align-items: center;
}
.links a {
  cursor: pointer;
  font-weight: 600;
  color: #2c3e50;
  transition: color 0.3s;
}
.links a:hover {
  color: #4a90e2;
}
.dark-theme .links a {
  color: #e2e8f0;
}
.dark-theme .links a:hover {
  color: #fbc2eb;
}
.theme-toggle {
  font-size: 1.3rem;
  user-select: none;
}
.theme-toggle:hover {
  transform: scale(1.1);
}

.blog-btn {
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  color: white !important;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
}
.dark-theme .blog-btn {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}
.blog-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* Hero Section */
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 0 20px;
  padding-top: 80px; /* Offset for nav */
}
.hero-title {
  font-size: 4rem;
  letter-spacing: -1.5px;
  margin-bottom: 1rem;
  color: #1a2a3a;
  text-shadow: 0 2px 4px rgba(255,255,255,0.8);
}
.dark-theme .hero-title {
  color: #f0f4f8;
  text-shadow: 0 2px 4px rgba(0,0,0,0.8);
}
.hero-title span {
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: none;
}
.dark-theme .hero-title span {
  background: linear-gradient(to right, #a18cd1, #fbc2eb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}
.btn-primary, .btn-outline {
  padding: 12px 28px;
  border-radius: 30px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}
.btn-primary {
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(74, 144, 226, 0.3);
}
.dark-theme .btn-primary {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  box-shadow: 0 4px 15px rgba(161, 140, 209, 0.3);
}
.btn-primary:hover {
  transform: translateY(-2px);
  filter: brightness(1.05);
}
.btn-outline {
  background: rgba(255, 255, 255, 0.8);
  border: 2px solid #4a90e2;
  color: #4a90e2;
  backdrop-filter: blur(4px);
}
.dark-theme .btn-outline {
  background: rgba(0, 0, 0, 0.3);
  border-color: #fbc2eb;
  color: #fbc2eb;
}
.btn-outline:hover {
  background: rgba(74, 144, 226, 0.1);
}
.dark-theme .btn-outline:hover {
  background: rgba(251, 194, 235, 0.1);
}

/* General Sections */
.section {
  max-width: 1080px;
  margin: 0 auto;
  padding: 80px 20px;
}

/* About Card */
.about-card {
  padding: 40px;
  border-radius: 24px;
  font-size: 1.1rem;
  line-height: 1.8;
}
.about-content {
  display: flex;
  align-items: center;
  gap: 40px;
  text-align: left;
}
.about-img {
  width: 45%;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  object-fit: cover;
  aspect-ratio: 16/10;
}
.dark-theme .about-img {
  box-shadow: 0 8px 24px rgba(0,0,0,0.5);
}
.about-text {
  flex: 1;
}
.about-card p {
  font-weight: 500;
  color: #2c3e50;
}
.dark-theme .about-card p {
  color: #e2e8f0;
}

/* Cards Hover Interaction */
.glass-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 12px 40px 0 rgba(74, 144, 226, 0.25);
}
.dark-theme .glass-card:hover {
  background: rgba(30, 32, 45, 0.7);
  box-shadow: 0 12px 40px 0 rgba(0, 0, 0, 0.6);
}

/* Skills Grid */
.skills-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}
.skill-card {
  padding: 12px 24px;
  border-radius: 30px;
  font-weight: 600;
  color: #4a90e2;
  cursor: default;
}
.dark-theme .skill-card {
  color: #fbc2eb;
}

/* Works Grid */
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}
.work-card {
  padding: 30px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
}
.work-tag {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  background: rgba(74, 144, 226, 0.15);
  color: #4a90e2;
  padding: 4px 10px;
  border-radius: 12px;
  align-self: flex-start;
  margin-bottom: 16px;
  font-weight: 700;
}
.dark-theme .work-tag {
  background: rgba(251, 194, 235, 0.15);
  color: #fbc2eb;
}
.work-card h3 {
  font-size: 1.4rem;
  color: #1a2a3a;
}
.dark-theme .work-card h3 {
  color: #f0f4f8;
}
.work-card p {
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 24px;
  flex-grow: 1;
}
.dark-theme .work-card p {
  color: #cbd5e1;
}
.work-link {
  font-weight: 700;
  color: #4a90e2;
  text-decoration: none;
  cursor: pointer;
  transition: opacity 0.3s;
}
.dark-theme .work-link {
  color: #fbc2eb;
}
.work-link:hover {
  opacity: 0.8;
}

/* Contact */
.contact-card {
  padding: 60px 40px;
  border-radius: 24px;
  text-align: center;
}
.contact-card p {
  margin-bottom: 30px;
  font-size: 1.1rem;
  font-weight: 500;
  color: #2c3e50;
}
.dark-theme .contact-card p {
  color: #e2e8f0;
}
.contact-img {
  width: 100%;
  max-width: 600px;
  height: 180px;
  object-fit: cover;
  border-radius: 16px;
  margin: 0 auto 30px auto;
  display: block;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}
.dark-theme .contact-img {
  box-shadow: 0 4px 15px rgba(0,0,0,0.4);
}
.contact-links {
  display: flex;
  justify-content: center;
  gap: 20px;
}
.contact-links a {
  display: inline-block;
  padding: 12px 30px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.8);
  color: #4a90e2;
  border: 1px solid rgba(74, 144, 226, 0.3);
  transition: all 0.3s ease;
}
.dark-theme .contact-links a {
  background: rgba(0, 0, 0, 0.3);
  color: #fbc2eb;
  border-color: rgba(251, 194, 235, 0.3);
}
.contact-links a:hover {
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
}
.dark-theme .contact-links a:hover {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
  color: white;
  border-color: transparent;
}

/* Footer */
.footer {
  text-align: center;
  padding: 40px 20px;
  font-weight: 500;
  color: #2c3e50;
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
}
.dark-theme .footer {
  color: #cbd5e1;
  background: rgba(0, 0, 0, 0.3);
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  .links a:not(.blog-btn):not(.theme-toggle) {
    display: none;
  }
  .works-grid {
    grid-template-columns: 1fr;
  }
  .hero-actions {
    flex-direction: column;
  }
  .about-content {
    flex-direction: column;
    text-align: center;
  }
  .about-img {
    width: 100%;
    aspect-ratio: 4/3;
  }
}
</style>
