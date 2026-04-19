<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

const toolCards = computed(() => [
  { path: '/tools/reaction', title: t('tools.reaction'), desc: t('toolsHub.cardReactionDesc') },
  { path: '/tools/cps', title: t('tools.cps'), desc: t('toolsHub.cardCpsDesc') },
  { path: '/tools/pomodoro', title: t('tools.pomodoro'), desc: t('toolsHub.cardPomodoroDesc') },
  { path: '/tools/password', title: t('tools.password'), desc: t('toolsHub.cardPasswordDesc') },
  { path: '/tools/base64', title: t('tools.base64'), desc: t('toolsHub.cardBase64Desc') },
  { path: '/tools/oj', title: t('toolsHub.cardOjTitle'), desc: t('toolsHub.cardOjDesc') },
])
</script>

<template>
  <section class="section" id="tools">
    <h2>{{ t('toolsHub.title') }}</h2>
    <p class="section-sub">{{ t('home.sectionToolsLead') }}</p>
    <div class="home-tools-grid">
      <div v-for="card in toolCards" :key="card.path" class="glass-card home-tool-card">
        <h3>{{ card.title }}</h3>
        <p>{{ card.desc }}</p>
        <button type="button" class="site-pill site-pill--active" @click="router.push(card.path)">{{ t('home.sectionToolsOpen') }}</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  opacity: 0.85;
  line-height: 1.6;
}
.home-tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
}
.home-tool-card {
  padding: 20px;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.home-tool-card h3 {
  margin: 0;
  font-size: 1.05rem;
}
.home-tool-card p {
  margin: 0;
  flex: 1;
  font-size: 0.88rem;
  opacity: 0.85;
  line-height: 1.5;
}
</style>
