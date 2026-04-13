<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

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
</script>

<template>
  <div class="portfolio-container">
    <!-- Navbar -->
    <nav class="glass-nav">
      <div class="nav-inner">
        <div class="logo">MyPortfolio</div>
        <div class="links">
          <a @click="scrollTo('about')">关于</a>
          <a @click="scrollTo('skills')">技能</a>
          <a @click="scrollTo('works')">作品</a>
          <a @click="scrollTo('contact')">联系</a>
          <a @click="router.push('/blog')" class="blog-btn">前往博客</a>
        </div>
      </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero" id="hero">
      <div class="hero-content">
        <h1 class="hero-title">你好，我是 <span>Developer</span></h1>
        <p class="hero-subtitle">热爱技术，专注开源，分享代码与生活</p>
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
        <p>
          我是一名前端与后端兼修的开发者，熟练掌握 Vue 3 生态以及 Spring Boot 后端技术栈。
          喜欢将优雅的界面设计与健壮的后台逻辑相结合。在 GitHub 上活跃，热爱分享开源项目，
          希望通过代码让世界变得更美好一点。
        </p>
      </div>
    </section>

    <!-- Skills Section -->
    <section class="section" id="skills">
      <h2>技能标签</h2>
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
        <p>期待与你交流技术，探讨合作可能。</p>
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
/* Base Theme & Gradient Background */
.portfolio-container {
  min-height: 100vh;
  /* Light mode: blue-purple soft gradient */
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #2c3e50;
  font-family: system-ui, -apple-system, sans-serif;
  overflow-x: hidden;
}

/* Dark mode overrides for background and text */
@media (prefers-color-scheme: dark) {
  .portfolio-container {
    background: linear-gradient(135deg, #1e1e2f 0%, #2a1b3d 100%);
    color: #e2e8f0;
  }
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
  background: linear-gradient(to right, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
@media (prefers-color-scheme: dark) {
  h2 {
    background: linear-gradient(to right, #a18cd1, #fbc2eb);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

/* Glassmorphism Mixin */
.glass-nav, .glass-card {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.07);
}

@media (prefers-color-scheme: dark) {
  .glass-nav, .glass-card {
    background: rgba(16, 18, 27, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
  }
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
  background: linear-gradient(to right, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
@media (prefers-color-scheme: dark) {
  .logo {
    background: linear-gradient(to right, #a18cd1, #fbc2eb);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}
.links {
  display: flex;
  gap: 24px;
  align-items: center;
}
.links a {
  cursor: pointer;
  font-weight: 500;
  transition: color 0.3s;
}
.links a:hover {
  color: #764ba2;
}
@media (prefers-color-scheme: dark) {
  .links a:hover {
    color: #fbc2eb;
  }
}
.blog-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white !important;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
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
}
.hero-title span {
  background: linear-gradient(to right, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
@media (prefers-color-scheme: dark) {
  .hero-title span {
    background: linear-gradient(to right, #a18cd1, #fbc2eb);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}
.hero-subtitle {
  font-size: 1.25rem;
  opacity: 0.8;
  max-width: 600px;
  margin: 0 auto 2.5rem;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(118, 75, 162, 0.3);
}
.btn-primary:hover {
  box-shadow: 0 6px 20px rgba(118, 75, 162, 0.4);
  transform: translateY(-2px);
}
.btn-outline {
  background: transparent;
  border: 2px solid #764ba2;
  color: #764ba2;
}
.btn-outline:hover {
  background: rgba(118, 75, 162, 0.1);
}
@media (prefers-color-scheme: dark) {
  .btn-outline {
    border-color: #fbc2eb;
    color: #fbc2eb;
  }
  .btn-outline:hover {
    background: rgba(251, 194, 235, 0.1);
  }
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
  text-align: center;
  font-size: 1.1rem;
  line-height: 1.8;
}
.about-card p {
  max-width: 800px;
  margin: 0 auto;
  opacity: 0.9;
}

/* Cards Hover Interaction */
.glass-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.glass-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px 0 rgba(31, 38, 135, 0.15);
}
@media (prefers-color-scheme: dark) {
  .glass-card:hover {
    box-shadow: 0 12px 40px 0 rgba(0, 0, 0, 0.5);
  }
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
  cursor: default;
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
  background: rgba(118, 75, 162, 0.15);
  color: #764ba2;
  padding: 4px 10px;
  border-radius: 12px;
  align-self: flex-start;
  margin-bottom: 16px;
  font-weight: 700;
}
@media (prefers-color-scheme: dark) {
  .work-tag {
    background: rgba(251, 194, 235, 0.15);
    color: #fbc2eb;
  }
}
.work-card h3 {
  font-size: 1.4rem;
}
.work-card p {
  opacity: 0.8;
  margin-bottom: 24px;
  flex-grow: 1;
}
.work-link {
  font-weight: 600;
  color: #667eea;
  text-decoration: none;
  cursor: pointer;
  transition: opacity 0.3s;
}
.work-link:hover {
  opacity: 0.8;
}
@media (prefers-color-scheme: dark) {
  .work-link {
    color: #a18cd1;
  }
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
  opacity: 0.9;
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
  background: rgba(255, 255, 255, 0.5);
  color: #2c3e50;
  transition: all 0.3s ease;
}
@media (prefers-color-scheme: dark) {
  .contact-links a {
    background: rgba(255, 255, 255, 0.1);
    color: #e2e8f0;
  }
}
.contact-links a:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

/* Footer */
.footer {
  text-align: center;
  padding: 40px 20px;
  opacity: 0.6;
  font-size: 0.9rem;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  .links a:not(.blog-btn) {
    display: none; /* Hide links on very small screens for simplicity, keep blog btn */
  }
  .works-grid {
    grid-template-columns: 1fr;
  }
  .hero-actions {
    flex-direction: column;
  }
}
</style>
