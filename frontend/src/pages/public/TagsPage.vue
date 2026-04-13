<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getTags, type Tag } from '../../api/blog'

const router = useRouter()
const items = ref<Tag[]>([])

onMounted(async () => {
  items.value = await getTags(true)
})
</script>

<template>
  <div class="card panel">
    <div class="page-title">标签</div>
    <div class="tags">
      <el-tag
        v-for="t in items"
        :key="t.id"
        size="large"
        effect="plain"
        class="tag"
        @click="router.push({ path: '/', query: { tagId: t.id } })"
      >
        {{ t.name }}
      </el-tag>
    </div>
  </div>
</template>

<style scoped>
.panel {
  padding: 16px;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tag {
  cursor: pointer;
}
</style>

