<script setup lang="ts">
/**
 * 青蓝玻璃态面包屑：前台（SiteLayout）与后台（AdminLayout）共用。
 * 根据当前路由自动生成层级，避免各页重复手写。
 */
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const props = withDefaults(
  defineProps<{
    /** site：个人站内容区；admin：管理后台 */
    variant?: 'site' | 'admin'
  }>(),
  { variant: 'site' },
)

const route = useRoute()
const { t } = useI18n()

type Crumb = { label: string; to?: string }

const items = computed<Crumb[]>(() => {
  const path = route.path
  if (props.variant === 'admin') {
    const list: Crumb[] = [{ label: t('breadcrumb.adminHome'), to: '/admin/articles' }]
    if (path === '/admin/articles' || path === '/admin') {
      list.push({ label: t('breadcrumb.adminArticles') })
      return list
    }
    if (path.startsWith('/admin/editor')) {
      list.push({ label: t('breadcrumb.adminEditor') })
      return list
    }
    if (path === '/admin/categories') {
      list.push({ label: t('breadcrumb.adminCategories') })
      return list
    }
    if (path === '/admin/tags') {
      list.push({ label: t('breadcrumb.adminTags') })
      return list
    }
    if (path === '/admin/messages') {
      list.push({ label: t('breadcrumb.adminMessages') })
      return list
    }
    return list
  }

  // ---------- 前台 ----------
  const list: Crumb[] = [{ label: t('breadcrumb.home'), to: '/' }]
  if (path === '/blog') {
    list.push({ label: t('breadcrumb.blog') })
    return list
  }
  if (path.startsWith('/article/')) {
    list.push({ label: t('breadcrumb.blog'), to: '/blog' })
    list.push({ label: t('breadcrumb.article') })
    return list
  }
  if (path === '/categories') {
    list.push({ label: t('breadcrumb.categories') })
    return list
  }
  if (path === '/tags') {
    list.push({ label: t('breadcrumb.tags') })
    return list
  }
  if (path === '/message') {
    list.push({ label: t('breadcrumb.message') })
    return list
  }
  if (path.startsWith('/oj')) {
    list.push({ label: t('nav.oj'), to: '/oj' })
    if (route.params.id) {
      list.push({ label: t('breadcrumb.ojProblem') })
    }
    return list
  }
  return []
})

const visible = computed(() => items.value.length >= 2)
</script>

<template>
  <nav v-if="visible" class="glass-crumb" aria-label="Breadcrumb">
    <ol class="glass-crumb__list">
      <li v-for="(c, i) in items" :key="i" class="glass-crumb__item">
        <RouterLink v-if="c.to && i < items.length - 1" :to="c.to" class="glass-crumb__link">
          {{ c.label }}
        </RouterLink>
        <span v-else class="glass-crumb__current">{{ c.label }}</span>
        <span v-if="i < items.length - 1" class="glass-crumb__sep" aria-hidden="true">/</span>
      </li>
    </ol>
  </nav>
</template>

<style scoped>
.glass-crumb {
  margin-bottom: 1rem;
  padding: 0.5rem 0.85rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
}

.glass-crumb__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.35rem 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-color, #2c3e50);
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.35);
}

:root[data-theme='dark'] .glass-crumb__list {
  color: #eaf8ff;
  text-shadow: 0 1px 2px rgba(0, 30, 60, 0.35);
}

.glass-crumb__link {
  color: inherit;
  text-decoration: none;
  opacity: 0.88;
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.glass-crumb__link:hover {
  opacity: 1;
  transform: translateY(-1px);
}

.glass-crumb__current {
  opacity: 1;
  color: var(--primary-color, #4a90e2);
}

.glass-crumb__sep {
  opacity: 0.45;
  user-select: none;
}
</style>
