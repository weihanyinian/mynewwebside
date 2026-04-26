<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, type Category } from '../../api/blog'
import BackToBlogButton from '../../components/BackToBlogButton.vue'

const router = useRouter()
const items = ref<Category[]>([])

onMounted(async () => {
  items.value = await getCategories(true)
})
</script>

<template>
  <div class="panel-card panel page-animation">
    <div class="page-top">
      <BackToBlogButton />
    </div>
    <div class="page-title">探索分类</div>
    <div class="grid">
      <div
        v-for="c in items"
        :key="c.id"
        class="cell content-card"
        @click="router.push({ path: '/blog', query: { categoryId: c.id } })"
      >
        <span class="cell-icon">📁</span>
        <span class="cell-name">{{ c.name }}</span>
      </div>
      <div v-if="items.length === 0" class="empty-text">暂无分类数据</div>
    </div>
  </div>
</template>

<style scoped>
.page-animation {
  animation: fadeIn 0.4s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.panel {
  padding: 30px;
}
.page-top {
  margin-bottom: 12px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.cell {
  border-radius: 18px;
  padding: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s;
  background: color-mix(in srgb, var(--surface-2) 84%, transparent);
  border: 1px solid color-mix(in srgb, var(--glass-border) 72%, var(--accent-cyan) 28%);
}
.cell:hover {
  transform: translateY(-4px);
  background: color-mix(in srgb, var(--surface-2) 92%, transparent);
  box-shadow: 0 10px 24px rgba(98, 167, 234, 0.18);
}
.cell-icon {
  font-size: 1.5rem;
}
.cell-name {
  font-weight: 700;
  font-size: 1.1rem;
  color: var(--text-color);
}
.empty-text {
  grid-column: 1 / -1;
  text-align: center;
  color: rgba(44, 62, 80, 0.5);
  padding: 40px 0;
  font-weight: 500;
}
</style>

