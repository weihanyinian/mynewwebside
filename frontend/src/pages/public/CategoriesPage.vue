<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, type Category } from '../../api/blog'

const router = useRouter()
const items = ref<Category[]>([])

onMounted(async () => {
  items.value = await getCategories(true)
})
</script>

<template>
  <div class="card panel page-animation">
    <div class="page-title">探索分类</div>
    <div class="grid">
      <div
        v-for="c in items"
        :key="c.id"
        class="cell glass-card"
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
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.cell {
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.cell:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 8px 24px rgba(74, 144, 226, 0.2);
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

