<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getTags, type Tag } from '../../api/blog'
import BackToBlogButton from '../../components/BackToBlogButton.vue'

const router = useRouter()
const items = ref<Tag[]>([])

onMounted(async () => {
  items.value = await getTags(true)
})
</script>

<template>
  <div class="card panel page-animation">
    <div class="page-top">
      <BackToBlogButton />
    </div>
    <div class="page-title">探索标签</div>
    <div class="tags-container">
      <button
        v-for="t in items"
        :key="t.id"
        type="button"
        class="site-pill site-pill--chip"
        @click="router.push({ path: '/blog', query: { tagId: t.id } })"
      >
        # {{ t.name }}
      </button>
      <div v-if="items.length === 0" class="empty-text">暂无标签数据</div>
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
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 20px;
}
/* 标签探索：交互由全局 .site-pill 统一 */
.empty-text {
  width: 100%;
  text-align: center;
  color: rgba(44, 62, 80, 0.5);
  padding: 40px 0;
  font-weight: 500;
}
</style>

