<script setup lang="ts">
import GlassCard from '../components/GlassCard.vue'
</script>

<template>
  <div class="page-container max-w-2xl">
    <h1 class="text-3xl font-bold mb-8 gradient-text">👤 关于我</h1>

    <!-- Introduction -->
    <GlassCard class="mb-6">
      <div class="text-5xl mb-4">👋</div>
      <p class="text-lg font-bold mb-3 gradient-text">你好，我是维寒一念</p>
      <p class="text-[var(--text-secondary)] leading-relaxed mb-4">
        一枚热爱技术的全栈开发者，目前专注于 Java 生态与前端工程化。
      </p>
    </GlassCard>

    <!-- Tech Stack -->
    <GlassCard title="🔧 技术栈" class="mb-6">
      <div class="space-y-2 text-sm text-[var(--text-secondary)]">
        <p><strong>后端：</strong>Java / Spring Boot / MyBatis / Redis / MySQL</p>
        <p><strong>前端：</strong>Vue 3 / TypeScript / Tailwind CSS / Vite</p>
        <p><strong>运维：</strong>Docker / Linux / Nginx</p>
        <p><strong>偶尔玩玩：</strong>Python / Selenium / AI</p>
      </div>
    </GlassCard>

    <!-- Contact -->
    <GlassCard title="📬 找到我" class="mb-6">
      <div class="space-y-2 text-sm text-[var(--text-secondary)]">
        <p>🔗 GitHub：<a href="https://github.com/weihanyinian" target="_blank" class="text-[#62a7ea] no-underline">github.com/weihanyinian</a></p>
        <p>📺 B站：关注「维寒一念」</p>
        <p>📧 邮箱：1012308753@qq.com</p>
      </div>
    </GlassCard>

    <!-- Interests -->
    <GlassCard title="🎵 兴趣爱好">
      <div class="space-y-1 text-sm text-[var(--text-secondary)]">
        <p>🎧 初音未来 / VOCALOID</p>
        <p>📖 技术阅读 / 科幻小说</p>
        <p>🎮 独立游戏</p>
        <p>☕ 咖啡星人</p>
      </div>
    </GlassCard>
  </div>
</template>
