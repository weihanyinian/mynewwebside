<script setup lang="ts">
/**
 * 「← 返回博客」：跳转 `/blog`（博客列表），样式与顶栏「前往博客」一致（site-pill + site-pill--active 青蓝渐变）。
 * 用于分类 / 标签 / 留言墙等页，与面包屑互补，不替代面包屑。
 */
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

withDefaults(
  defineProps<{
    wrapperClass?: string
  }>(),
  { wrapperClass: '' },
)

const router = useRouter()
const { locale } = useI18n()

function handleClick() {
  router.push({ path: '/blog' })
}
</script>

<template>
  <button
    type="button"
    class="site-pill site-pill--active"
    :class="wrapperClass"
    :title="locale === 'zh' ? '返回博客列表' : 'Back to blog list'"
    @click="handleClick"
  >
    <span aria-hidden="true">←</span>
    <span>{{ locale === 'zh' ? '返回博客' : 'Back to blog' }}</span>
  </button>
</template>
