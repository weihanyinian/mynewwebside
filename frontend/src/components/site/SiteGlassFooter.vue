<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const icpNo = computed(() => import.meta.env.VITE_PUBLIC_ICP_NO || '桂ICP备2025060137号-2')
const icpUrl = computed(() => import.meta.env.VITE_PUBLIC_ICP_URL || 'https://beian.miit.gov.cn/')
const psb = computed(() => import.meta.env.VITE_PUBLIC_PSB_NO || '')
const psbUrl = computed(() => import.meta.env.VITE_PUBLIC_PSB_URL || '#')
</script>

<template>
  <footer class="site-rich-footer">
    <div class="site-rich-footer__filings">
      <span class="filing-item">
        <span class="filing-label">{{ t('footer.icpLabel') }}</span>
        <a :href="icpUrl" target="_blank" rel="noopener noreferrer" class="filing-link">{{ icpNo }}</a>
      </span>
      <template v-if="psb">
        <span class="filing-sep">|</span>
        <span class="filing-item">
          <span class="filing-label">{{ t('footer.psbLabel') }}</span>
          <a :href="psbUrl" target="_blank" rel="noopener noreferrer" class="filing-link">{{ psb }}</a>
        </span>
      </template>
      <span class="filing-sep">|</span>
      <span class="filing-muted">{{ t('footer.siteDomain') }}</span>
    </div>

    <div class="site-rich-footer__copy">
      <span class="footer-glow-dot" aria-hidden="true" />
      <p>© 2026 {{ t('footer.copyName') }}</p>
    </div>
  </footer>
</template>

<style scoped>
.site-rich-footer {
  margin-top: auto;
  padding: 24px 16px 32px;
  border-top: 1px solid var(--footer-border, rgba(255, 255, 255, 0.22));
  background: var(--footer-bg, rgba(255, 255, 255, 0.22));
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  color: var(--text-color);
}

:root[data-theme='dark'] .site-rich-footer {
  --footer-bg: rgba(12, 14, 24, 0.55);
  --footer-border: rgba(255, 255, 255, 0.1);
}

.site-rich-footer__filings {
  max-width: 920px;
  margin: 0 auto 14px;
  text-align: center;
  font-size: 0.78rem;
  color: var(--text-color);
  opacity: 0.72;
}

.filing-link {
  color: inherit;
  text-decoration: none;
  transition: color 0.2s ease;
}

.filing-link:hover {
  color: var(--primary-color);
}

.filing-sep {
  margin: 0 0.5rem;
  opacity: 0.45;
}

.filing-muted {
  opacity: 0.65;
}

.filing-item {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  flex-wrap: wrap;
  justify-content: center;
}

.filing-label {
  opacity: 0.55;
  font-size: 0.72rem;
}

.site-rich-footer__copy {
  text-align: center;
  position: relative;
}

.site-rich-footer__copy p {
  margin: 0.35rem 0 0;
  font-size: 0.85rem;
  font-weight: 600;
  opacity: 0.88;
}

.footer-glow-dot {
  display: block;
  width: 10px;
  height: 10px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: radial-gradient(circle, #7dd3fc 0%, #4a90e2 45%, transparent 70%);
  box-shadow: 0 0 18px rgba(102, 217, 255, 0.55);
}
</style>
