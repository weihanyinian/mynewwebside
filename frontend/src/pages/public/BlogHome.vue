<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface BlogPost {
  id: number
  title: string
  summary: string
  cover: string
  tags: string[]
  date: string
  readTime: string
}

const posts = ref<BlogPost[]>([
  {
    id: 1,
    title: '深入理解 Vue3 Composition API',
    summary: '探讨 Vue3 组合式 API 的设计理念、核心优势以及在复杂业务场景下的最佳实践指南。',
    cover: 'https://images.unsplash.com/photo-1627398242454-45a1465c2479?auto=format&fit=crop&q=80&w=800',
    tags: ['Vue3', '前端开发'],
    date: '2026-04-10',
    readTime: '8 min'
  },
  {
    id: 2,
    title: 'Tailwind CSS 高级技巧',
    summary: '如何利用 Tailwind 构建可维护的极简轻奢玻璃态 UI，实现高效且优雅的前端页面开发。',
    cover: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&q=80&w=800',
    tags: ['CSS', '设计'],
    date: '2026-04-05',
    readTime: '6 min'
  },
  {
    id: 3,
    title: '大模型微调：LoRA 原理与实战',
    summary: '从底层数学原理到代码实现，手把手带你使用 PEFT 库对大语言模型进行参数高效微调。',
    cover: 'https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&q=80&w=800',
    tags: ['AI', 'LLM'],
    date: '2026-03-28',
    readTime: '15 min'
  },
  {
    id: 4,
    title: 'Spring Boot 3 + Vue 3 全栈架构指南',
    summary: '现代 Web 应用架构设计，前后端分离下的权限控制、接口设计及工程化部署方案。',
    cover: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&q=80&w=800',
    tags: ['后端', 'Spring Boot'],
    date: '2026-03-15',
    readTime: '12 min'
  },
  {
    id: 5,
    title: 'Transformer 架构硬核解析',
    summary: '抛开复杂的公式，用最直观的图解和代码带你搞懂 Attention 机制的本质与运作流程。',
    cover: 'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?auto=format&fit=crop&q=80&w=800',
    tags: ['深度学习', 'NLP'],
    date: '2026-03-01',
    readTime: '20 min'
  },
  {
    id: 6,
    title: '打造极简个人数字花园',
    summary: '分享我搭建这个轻奢毛玻璃风格博客的完整思路，包含 UI 设计、动效处理与组件封装。',
    cover: 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&q=80&w=800',
    tags: ['随笔', '设计'],
    date: '2026-02-20',
    readTime: '5 min'
  }
])

const navItems = [
  { name: '首页', path: '/', active: false },
  { name: '博客', path: '/blog', active: true },
  { name: '作品', path: '/#works', active: false },
  { name: '留言墙', path: '/message', active: false },
  { name: '摸鱼', path: '/moyu', active: false }
]

const isLoaded = ref(false)

onMounted(() => {
  // Trigger entry animation
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-[#1a1c25] to-[#111318] text-slate-200 font-sans selection:bg-purple-500/30 selection:text-white relative overflow-x-hidden">
    
    <!-- Ambient Background Blobs -->
    <div class="fixed top-[-10%] left-[-10%] w-[40vw] h-[40vw] rounded-full bg-blue-500/10 blur-[120px] pointer-events-none"></div>
    <div class="fixed bottom-[-10%] right-[20%] w-[50vw] h-[50vw] rounded-full bg-purple-600/10 blur-[150px] pointer-events-none"></div>

    <div class="max-w-[1400px] mx-auto min-h-screen flex flex-col lg:flex-row relative z-10 transition-opacity duration-1000" :class="isLoaded ? 'opacity-100' : 'opacity-0'">
      
      <!-- Left Sidebar (Desktop Fixed, Mobile Top) -->
      <aside class="w-full lg:w-[320px] lg:h-screen lg:sticky lg:top-0 p-6 lg:p-10 flex flex-col justify-between border-b lg:border-b-0 lg:border-r border-white/5 bg-white/[0.02] backdrop-blur-xl z-40">
        
        <div class="flex flex-col items-center lg:items-start text-center lg:text-left mt-4 lg:mt-10">
          <!-- Avatar -->
          <div class="relative group cursor-pointer mb-6">
            <div class="absolute inset-0 bg-gradient-to-tr from-[#8be6ff] to-[#a78bfa] rounded-full blur-md opacity-40 group-hover:opacity-70 transition-opacity duration-500"></div>
            <img src="../../assets/images/about-miku.jpg" alt="维寒一念" class="w-28 h-28 rounded-full object-cover border-2 border-white/10 relative z-10 shadow-xl transition-transform duration-500 group-hover:scale-105" />
          </div>

          <!-- Info -->
          <h1 class="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-[#8be6ff] to-[#a78bfa] mb-2 tracking-wide">
            维寒一念
          </h1>
          <p class="text-sm text-slate-400 mb-10 font-medium tracking-wide">
            保持热爱，奔赴山海
          </p>

          <!-- Navigation -->
          <nav class="flex lg:flex-col gap-4 lg:gap-3 w-full overflow-x-auto lg:overflow-visible pb-4 lg:pb-0 scrollbar-hide">
            <a v-for="item in navItems" :key="item.name" :href="item.path" 
               class="px-5 py-3 rounded-2xl text-sm font-medium transition-all duration-300 whitespace-nowrap flex items-center group relative overflow-hidden"
               :class="item.active ? 'text-white bg-white/10 shadow-[0_4px_16px_rgba(0,0,0,0.2)] border border-white/10' : 'text-slate-400 hover:text-white hover:bg-white/5 border border-transparent'">
              <span class="relative z-10">{{ item.name }}</span>
              <div v-if="item.active" class="absolute left-0 w-1 h-full bg-gradient-to-b from-[#8be6ff] to-[#a78bfa] rounded-r-full"></div>
            </a>
          </nav>
        </div>

        <!-- Social Links -->
        <div class="hidden lg:flex gap-5 mt-auto pb-4 px-2">
          <a href="https://github.com/weihanyinian" target="_blank" rel="noopener noreferrer" class="text-slate-400 hover:text-[#8be6ff] transition-colors duration-300 transform hover:-translate-y-1">
            <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path fill-rule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10019 0 0022 12.017C22 6.484 17.522 2 12 2z" clip-rule="evenodd"></path></svg>
          </a>
          <a href="#" class="text-slate-400 hover:text-[#a78bfa] transition-colors duration-300 transform hover:-translate-y-1">
            <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M17.382 3.125a1.25 1.25 0 0 1 1.767 1.768l-1.393 1.393h.744c2.07 0 3.75 1.68 3.75 3.75v7.5c0 2.07-1.68 3.75-3.75 3.75H5.5c-2.07 0-3.75-1.68-3.75-3.75v-7.5c0-2.07 1.68-3.75 3.75-3.75h.744L4.85 4.893a1.25 1.25 0 1 1 1.768-1.768l2.062 2.062h6.639l2.063-2.062zm1.118 5.625H5.5a1.25 1.25 0 0 0-1.25 1.25v7.5a1.25 1.25 0 0 0 1.25 1.25h13a1.25 1.25 0 0 0 1.25-1.25v-7.5a1.25 1.25 0 0 0-1.25-1.25zM8.5 11.25c.69 0 1.25.56 1.25 1.25v1.25a1.25 1.25 0 1 1-2.5 0v-1.25c0-.69.56-1.25 1.25-1.25zm7 0c.69 0 1.25.56 1.25 1.25v1.25a1.25 1.25 0 1 1-2.5 0v-1.25c0-.69.56-1.25 1.25-1.25z"/></svg>
          </a>
          <a href="mailto:1012308753@qq.com" class="text-slate-400 hover:text-[#8be6ff] transition-colors duration-300 transform hover:-translate-y-1">
            <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2zm-8 7L4 8v2l8 5 8-5V8l-8 5z"/></svg>
          </a>
        </div>
      </aside>

      <!-- Main Content -->
      <main class="flex-1 p-6 lg:p-12 xl:p-16 min-h-screen relative pb-40 lg:pb-16">
        
        <!-- Header -->
        <header class="mb-12">
          <h2 class="text-3xl lg:text-4xl font-extrabold text-white tracking-tight mb-3">最新博客</h2>
          <div class="w-16 h-1.5 bg-gradient-to-r from-[#8be6ff] to-[#a78bfa] rounded-full"></div>
        </header>

        <!-- Grid -->
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8 mb-24">
          
          <article v-for="(post, index) in posts" :key="post.id" 
                   @click="$router.push('/article/' + post.id)"
                   class="group relative bg-white/[0.03] border border-white/10 rounded-3xl overflow-hidden backdrop-blur-lg hover:bg-white/[0.06] hover:border-white/20 transition-all duration-300 ease-out hover:-translate-y-2 hover:scale-[1.02] hover:shadow-[0_20px_40px_rgba(0,0,0,0.4)] cursor-pointer flex flex-col"
                   :style="`animation: slideUp 0.6s ease-out ${index * 0.1}s both;`">
            
            <!-- Image Container -->
            <div class="relative h-56 w-full overflow-hidden bg-slate-800">
              <img :src="post.cover" :alt="post.title" class="w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-110" />
              <div class="absolute inset-0 bg-gradient-to-t from-[#111318] via-transparent to-transparent opacity-80"></div>
              
              <!-- Tags -->
              <div class="absolute top-4 left-4 flex gap-2 flex-wrap">
                <span v-for="tag in post.tags" :key="tag" class="px-3 py-1 rounded-full text-xs font-semibold bg-black/40 backdrop-blur-md text-white border border-white/10">
                  {{ tag }}
                </span>
              </div>
            </div>

            <!-- Content Container -->
            <div class="p-6 flex flex-col flex-1">
              <div class="flex items-center gap-3 text-xs text-slate-400 font-medium mb-3">
                <span class="flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                  {{ post.date }}
                </span>
                <span class="w-1 h-1 rounded-full bg-slate-600"></span>
                <span class="flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                  {{ post.readTime }} read
                </span>
              </div>

              <h3 class="text-xl font-bold text-white mb-3 line-clamp-2 group-hover:text-[#8be6ff] transition-colors duration-300">
                {{ post.title }}
              </h3>
              
              <p class="text-sm text-slate-400 leading-relaxed line-clamp-3 mt-auto">
                {{ post.summary }}
              </p>
            </div>
          </article>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>