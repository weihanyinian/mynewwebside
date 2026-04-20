<script setup lang="ts">
defineProps<{
  total: number
  pageSize: number
  currentPage: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()
</script>

<template>
  <div v-if="total > pageSize" class="wall-pager">
    <el-pagination
      background
      layout="prev, pager, next"
      :page-size="pageSize"
      :current-page="currentPage"
      :total="total"
      @current-change="(p: number) => emit('change', p)"
    />
  </div>
</template>

<style scoped>
.wall-pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
.wall-pager :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--primary-color) !important;
}
</style>
