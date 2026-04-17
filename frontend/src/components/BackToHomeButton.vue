<script setup lang="ts">
/**
 * 「← 首页」：全站统一使用全局 .site-pill 玻璃态样式（见 styles/site-ui.css），
 * 跳转逻辑仍为 Portfolio 主页（goToSiteHome）。
 */
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { goToSiteHome } from '../utils/siteHome'

withDefaults(
  defineProps<{
    /** 附加 class：如 Tailwind shrink-0 */
    wrapperClass?: string
    /** 深色玻璃背景页面（如文章页）用 on-dark，提高对比 */
    variant?: 'default' | 'on-dark'
  }>(),
  { wrapperClass: '', variant: 'default' },
)

const router = useRouter()
const { locale } = useI18n()

function handleClick() {
  goToSiteHome(router)
}
</script>

<template>
  <button
    type="button"
    class="site-pill"
    :class="[wrapperClass, variant === 'on-dark' ? 'site-pill--on-dark' : '']"
    :title="locale === 'zh' ? '返回主页（重置站点首页视图）' : 'Back to home'"
    @click="handleClick"
  >
    <span aria-hidden="true">←</span>
    <span>{{ locale === 'zh' ? '首页' : 'Home' }}</span>
  </button>
</template>
