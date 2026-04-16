<script setup lang="ts">
/**
 * 青蓝玻璃态「返回主页」按钮，用于博客/分类/标签/留言/摸鱼等子页左上角。
 * 与左下角 14 酱、音乐播放器无位置冲突（本组件仅占内容区左上角）。
 */
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { goToSiteHome } from '../utils/siteHome'

withDefaults(
  defineProps<{
    /** 附加到按钮上的 class，用于各页微调间距（可传 Tailwind 或自定义类名） */
    wrapperClass?: string
  }>(),
  { wrapperClass: '' },
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
    class="back-to-home"
    :class="wrapperClass"
    :title="locale === 'zh' ? '返回主页（重置站点首页视图）' : 'Back to home'"
    @click="handleClick"
  >
    <span class="back-to-home__icon" aria-hidden="true">←</span>
    <span class="back-to-home__text">{{ locale === 'zh' ? '首页' : 'Home' }}</span>
  </button>
</template>

<style scoped>
.back-to-home {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background 0.25s ease;
}

.back-to-home:hover {
  transform: translateY(-3px) scale(1.02);
  background: rgba(255, 255, 255, 0.32);
  box-shadow: 0 12px 40px rgba(102, 217, 255, 0.35);
}

.back-to-home:active {
  transform: translateY(0) scale(0.98);
}

.back-to-home__icon {
  font-size: 1rem;
  line-height: 1;
}
</style>
