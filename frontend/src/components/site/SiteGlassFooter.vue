<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const icpNo = computed(() => import.meta.env.VITE_PUBLIC_ICP_NO || '桂ICP备2025060137号-2')
const icpUrl = computed(() => import.meta.env.VITE_PUBLIC_ICP_URL || 'https://beian.miit.gov.cn/')
const psb = computed(() => import.meta.env.VITE_PUBLIC_PSB_NO || '')
const psbUrl = computed(() => import.meta.env.VITE_PUBLIC_PSB_URL || '#')
const githubRepo = computed(() => import.meta.env.VITE_PUBLIC_GITHUB_REPO || 'https://github.com/weihanyinian/mynewwebside')
const umamiShare = computed(() => import.meta.env.VITE_UMAMI_SHARE_URL || '')
</script>

<template>
  <footer class="site-rich-footer">
    <div class="site-rich-footer__badges">
      <a
        class="footer-badge footer-badge--license"
        href="https://creativecommons.org/licenses/by-nc-sa/4.0/"
        target="_blank"
        rel="noopener noreferrer"
      >
        <span class="footer-badge__ico">◎</span>
        <span class="footer-badge__k">{{ t('footer.badgeLicense') }}</span>
        <span class="footer-badge__v">CC BY-NC-SA 4.0</span>
      </a>
      <span class="footer-badge footer-badge--stack">
        <span class="footer-badge__ico">◇</span>
        <span class="footer-badge__k">{{ t('footer.badgeStack') }}</span>
        <span class="footer-badge__v">Vue 3 · Spring Boot 3</span>
      </span>
      <span class="footer-badge footer-badge--host">
        <span class="footer-badge__ico">▣</span>
        <span class="footer-badge__k">{{ t('footer.badgeDeploy') }}</span>
        <span class="footer-badge__v">{{ t('footer.deployHint') }}</span>
      </span>
      <a :href="githubRepo" target="_blank" rel="noopener noreferrer" class="footer-badge footer-badge--gh">
        <span class="footer-badge__ico">⎇</span>
        <span class="footer-badge__k">GitHub</span>
        <span class="footer-badge__v">Source</span>
      </a>
      <RouterLink v-if="umamiShare" :to="{ path: '/stats' }" class="footer-badge footer-badge--stats">
        <span class="footer-badge__ico">◔</span>
        <span class="footer-badge__k">Umami</span>
        <span class="footer-badge__v">{{ t('footer.stats') }}</span>
      </RouterLink>
    </div>

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
      <p>© 2024 - 2026 {{ t('footer.copyName') }}</p>
      <p class="footer-tagline">{{ t('footer.tagline') }}</p>
    </div>
  </footer>
</template>

<style scoped>
.site-rich-footer {
  margin-top: auto;
  padding: 28px 16px 36px;
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

.site-rich-footer__badges {
  max-width: 920px;
  margin: 0 auto 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.footer-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
  text-decoration: none;
  color: inherit;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  transition:
    transform 0.22s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.22s ease;
}

:root[data-theme='dark'] .footer-badge {
  border-color: rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.06);
}

.footer-badge:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 26px rgba(102, 217, 255, 0.18);
}

.footer-badge--license {
  border-color: rgba(251, 146, 60, 0.45);
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.15), rgba(255, 255, 255, 0.08));
}

.footer-badge--stack {
  border-color: rgba(244, 114, 182, 0.4);
  background: linear-gradient(135deg, rgba(244, 114, 182, 0.12), rgba(255, 255, 255, 0.06));
}

.footer-badge--host {
  border-color: rgba(52, 211, 153, 0.4);
}

.footer-badge--gh {
  border-color: rgba(96, 165, 250, 0.45);
}

.footer-badge--stats {
  border-color: rgba(250, 204, 21, 0.45);
}

.footer-badge__ico {
  opacity: 0.85;
  font-size: 0.85rem;
}

.footer-badge__k {
  opacity: 0.72;
  font-weight: 600;
}

.footer-badge__v {
  opacity: 0.95;
}

.site-rich-footer__filings {
  max-width: 920px;
  margin: 0 auto 16px;
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

.footer-tagline {
  font-size: 0.72rem !important;
  font-weight: 500 !important;
  opacity: 0.55 !important;
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
