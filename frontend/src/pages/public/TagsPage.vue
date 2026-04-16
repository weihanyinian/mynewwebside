<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getTags, type Tag } from '../../api/blog'
import BackToHomeButton from '../../components/BackToHomeButton.vue'

const router = useRouter()
const items = ref<Tag[]>([])

onMounted(async () => {
  items.value = await getTags(true)
})
</script>

<template>
  <div class="card panel page-animation">
    <div class="page-top">
      <BackToHomeButton />
    </div>
    <div class="page-title">探索标签</div>
    <div class="tags-container">
      <span
        v-for="t in items"
        :key="t.id"
        class="anime-tag-lg"
        @click="router.push({ path: '/blog', query: { tagId: t.id } })"
      >
        # {{ t.name }}
      </span>
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
.anime-tag-lg {
  font-size: 1rem;
  padding: 10px 20px;
  background: rgba(74, 144, 226, 0.1);
  color: var(--primary-color);
  border: 1px solid rgba(74, 144, 226, 0.2);
  border-radius: 24px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.anime-tag-lg:hover {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(74, 144, 226, 0.3);
}
.empty-text {
  width: 100%;
  text-align: center;
  color: rgba(44, 62, 80, 0.5);
  padding: 40px 0;
  font-weight: 500;
}
</style>

