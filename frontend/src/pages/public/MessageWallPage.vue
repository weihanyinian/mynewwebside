<script setup lang="ts">
/**
 * 留言墙：公开发布；提交后立即展示。
 */
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import BackToBlogButton from '../../components/BackToBlogButton.vue'
import { useWallMessages } from '../../hooks/useWallMessages'
import { useUserStore } from '../../stores/user'
import WallComposer from '../../components/home/wall/WallComposer.vue'
import WallList from '../../components/home/wall/WallList.vue'
import WallPager from '../../components/home/wall/WallPager.vue'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
const wall = useWallMessages(5)

async function submitMessage() {
  if (!wall.canSubmit.value) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  try {
    const ok = await wall.submit()
    if (!ok) return
    ElMessage.success(t('messageWall.pendingReview'))
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  }
}
onMounted(() => void wall.load())
</script>

<template>
  <div class="message-wall-page page-animation">
    <div class="wall-back">
      <BackToBlogButton />
    </div>
    <div class="wall-header">
      <h1 class="wall-title">{{ t('messageWall.title') }}</h1>
      <p class="wall-subtitle">{{ t('messageWall.subtitle') }}</p>
      <button
        v-if="userStore.isAdmin"
        type="button"
        class="wall-admin-btn"
        @click="router.push('/admin/messages')"
      >
        管理留言
      </button>
    </div>

    <div class="message-form-wrap">
      <WallComposer
        :nickname="wall.nickname.value"
        :content="wall.content.value"
        :submitting="wall.submitting.value"
        :can-submit="wall.canSubmit.value"
        :nickname-placeholder="t('messageWall.nicknamePlaceholder')"
        :content-placeholder="t('messageWall.contentPlaceholder')"
        :submit-label="t('messageWall.submit')"
        :submitting-label="t('messageWall.submitting')"
        @update:nickname="wall.nickname.value = $event"
        @update:content="wall.content.value = $event"
        @submit="submitMessage"
      />
    </div>

    <WallList
      :loading="wall.loading.value"
      :load-error="wall.loadError.value"
      :load-error-label="t('messageWall.loadError')"
      :items="wall.messages.value"
      :anonymous-label="t('messageWall.anonymous')"
      :reply-from-admin-label="t('messageWall.replyFromAdmin')"
      :empty-title="t('messageWall.emptyTitle')"
      :empty-hint="t('messageWall.emptyHint')"
      :loading-label="t('pages.loading')"
    />

    <WallPager
      :total="wall.total.value"
      :page-size="wall.pageSize.value"
      :current-page="wall.page.value + 1"
      @change="wall.setPage"
    />
  </div>
</template>

<style scoped>
.page-animation {
  animation: fadeIn 0.5s ease-out;
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px 7rem;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.wall-back {
  margin-bottom: 16px;
}

.wall-header {
  text-align: center;
  margin-bottom: 32px;
}

.wall-title {
  font-size: clamp(1.9rem, 4.8vw, 2.5rem);
  font-weight: 800;
  margin-bottom: 8px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.wall-subtitle {
  color: var(--text-muted);
  font-size: 1rem;
}
.wall-admin-btn {
  margin-top: 10px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 35%, rgba(255, 255, 255, 0.6));
  background: rgba(255, 255, 255, 0.28);
  color: var(--text-color);
  border-radius: 999px;
  min-height: 34px;
  padding: 0 0.86rem;
  cursor: pointer;
}

.message-form-wrap {
  margin-bottom: 40px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.submit-ico {
  margin-right: 6px;
  animation: spin 0.9s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.messages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.message-card {
  padding: 20px;
  border-radius: 16px;
  position: relative;
}

.message-card:hover {
  transform: translateY(-4px);
}

.msg-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.msg-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  color: var(--text-color);
}

.msg-content {
  line-height: 1.6;
  font-size: 0.99rem;
  color: var(--text-color);
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-reply {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid color-mix(in srgb, var(--primary-color) 35%, transparent);
  background: color-mix(in srgb, var(--primary-color) 8%, transparent);
}

.msg-reply__label {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--primary-color);
}

.msg-reply__text {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: var(--text-color);
  white-space: pre-wrap;
}

.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.skeleton-card {
  min-height: 140px;
  pointer-events: none;
}

.sk-line {
  height: 12px;
  border-radius: 8px;
  background: linear-gradient(
    90deg,
    color-mix(in srgb, var(--text-color) 8%, transparent),
    color-mix(in srgb, var(--text-color) 16%, transparent),
    color-mix(in srgb, var(--text-color) 8%, transparent)
  );
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
  margin-bottom: 10px;
}

.sk-line--short {
  width: 40%;
}

.sk-line--meta {
  width: 55%;
  margin-top: 16px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.wall-error {
  padding: 12px 16px;
  border-radius: 16px;
  color: #b91c1c;
  background: rgba(254, 226, 226, 0.85);
  border: 1px solid rgba(248, 113, 113, 0.5);
  margin-bottom: 16px;
}

:root[data-theme='dark'] .wall-error {
  color: #fecaca;
  background: rgba(127, 29, 29, 0.35);
  border-color: rgba(248, 113, 113, 0.35);
}

.wall-empty {
  text-align: center;
  padding: 48px 24px;
  border-radius: 16px;
}

.wall-empty__icon {
  font-size: 3rem;
  margin-bottom: 12px;
}

.wall-empty__title {
  margin: 0 0 8px;
  font-size: 1.25rem;
  color: var(--text-color);
}

.wall-empty__title--emph {
  font-weight: 900;
  font-size: 1.35rem;
  color: var(--text-color);
}

.wall-empty__hint {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.6;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
}

.wall-pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--primary-color) !important;
}

.mb-3 {
  margin-bottom: 12px;
}
</style>
