<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const visible = ref(false)

function onScroll() {
  visible.value = window.scrollY > 420
}

function goTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<template>
  <transition name="site-backtop-fade">
    <button
      v-show="visible"
      type="button"
      class="site-back-top"
      :aria-label="t('nav.backTop')"
      @click="goTop"
    >
      <svg class="site-back-top__ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path
          fill="currentColor"
          d="M11 20h2V10.83l3.59 3.58L18 13l-6-6-6 6 1.41 1.41L11 10.83V20z"
        />
      </svg>
    </button>
  </transition>
</template>

<style scoped>
.site-back-top {
  position: fixed;
  right: 18px;
  bottom: calc(22px + env(safe-area-inset-bottom, 0px));
  z-index: 1300;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 35%, rgba(255, 255, 255, 0.5));
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.72));
  color: var(--primary-color);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 4px 18px color-mix(in srgb, var(--primary-color) 22%, transparent),
    0 1px 0 rgba(255, 255, 255, 0.8) inset;
  transition:
    transform 0.25s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.25s ease;
  -webkit-tap-highlight-color: transparent;
}

:root[data-theme='dark'] .site-back-top {
  background: linear-gradient(145deg, rgba(30, 32, 48, 0.92), rgba(22, 24, 38, 0.88));
  color: #c4b5fd;
  border-color: rgba(196, 181, 253, 0.45);
  box-shadow: 0 6px 22px rgba(0, 20, 40, 0.5);
}

.site-back-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px color-mix(in srgb, var(--primary-color) 28%, transparent);
}

.site-back-top__ico {
  width: 22px;
  height: 22px;
}

.site-backtop-fade-enter-active,
.site-backtop-fade-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}
.site-backtop-fade-enter-from,
.site-backtop-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
