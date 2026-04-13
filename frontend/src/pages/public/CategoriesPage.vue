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
  <div class="card panel">
    <div class="page-title">分类</div>
    <div class="grid">
      <div
        v-for="c in items"
        :key="c.id"
        class="cell"
        @click="router.push({ path: '/blog', query: { categoryId: c.id } })"
      >
        {{ c.name }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.panel {
  padding: 16px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}
.cell {
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  font-weight: 700;
}
.cell:hover {
  border-color: rgba(0, 0, 0, 0.14);
}
@media (max-width: 780px) {
  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

