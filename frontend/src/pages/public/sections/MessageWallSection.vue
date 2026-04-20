<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { onMounted } from 'vue'
import { useWallMessages } from '../../../hooks/useWallMessages'
import WallComposer from '../../../components/home/wall/WallComposer.vue'
import WallList from '../../../components/home/wall/WallList.vue'
import WallPager from '../../../components/home/wall/WallPager.vue'

const { t } = useI18n()
const wall = useWallMessages(5)

async function submit() {
  if (!wall.canSubmit.value) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  try {
    const ok = await wall.submit()
    if (!ok) return
    ElMessage.success(t('messageWall.pendingReview'))
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : 'Error')
  }
}
onMounted(() => void wall.load())
</script>

<template>
  <section class="section message-wall-section" id="message">
    <h2>{{ t('messageWall.title') }}</h2>
    <p class="section-sub">{{ t('messageWall.subtitle') }}</p>

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
      @submit="submit"
    />

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
  </section>
</template>

<style scoped>
.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  font-size: 0.98rem;
  font-weight: 450;
  color: var(--text-muted, rgba(15, 23, 42, 0.68));
  line-height: 1.72;
  letter-spacing: 0.02em;
}
.center {
  text-align: center;
}
.home-message-form {
  max-width: 560px;
  margin: 0 auto 28px;
  padding: 28px;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 2px solid rgba(102, 217, 255, 0.35);
  border-radius: 24px;
  box-shadow:
    0 16px 48px rgba(15, 23, 42, 0.12),
    0 0 32px color-mix(in srgb, rgba(102, 217, 255, 0.2) 40%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  position: relative;
  overflow: hidden;
}

/* 顶部装饰线 */
.home-message-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 3px;
  background: linear-gradient(90deg, 
    rgba(102, 217, 255, 0.6),
    rgba(167, 139, 250, 0.6),
    rgba(244, 167, 194, 0.6)
  );
  border-radius: 0 0 3px 3px;
}
.home-message-form .home-field {
  margin-bottom: 12px;
}

.home-message-form :deep(.el-input__wrapper),
.home-message-form :deep(.el-textarea__inner) {
  border-radius: 16px !important;
  padding: 10px 14px !important;
  border: 1.5px solid rgba(167, 139, 250, 0.25);
  background: rgba(255, 255, 255, 0.72);
}

.home-message-form :deep(.el-input__inner),
.home-message-form :deep(.el-textarea__inner) {
  font-size: 1rem;
  line-height: 1.7;
  letter-spacing: 0.01em;
  color: color-mix(in srgb, var(--text-color) 92%, #000 8%);
}

.home-message-form :deep(.el-input__inner::placeholder),
.home-message-form :deep(.el-textarea__inner::placeholder) {
  color: #94a3b8;
  font-weight: 500;
}
.home-message-form :deep(.el-input__wrapper.is-focus),
.home-message-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--primary-color) 30%, transparent) !important;
  border-color: color-mix(in srgb, var(--primary-color) 52%, #fff 48%) !important;
  filter: brightness(1.02);
  animation: inputNeonBreath 1.9s ease-in-out infinite;
}
.dark-theme .home-message-form :deep(.el-input__wrapper),
.dark-theme .home-message-form :deep(.el-textarea__inner) {
  background: rgba(15, 23, 42, 0.72);
  border-color: rgba(167, 139, 250, 0.35);
}
.submit-btn {
  width: 100%;
  justify-content: center;
  min-height: 46px;
  gap: 8px;
  transition: transform 0.24s ease, box-shadow 0.24s ease, filter 0.24s ease;
  position: relative;
  overflow: hidden;
}
.submit-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(110deg, transparent 25%, rgba(255, 255, 255, 0.34) 48%, transparent 72%);
  transform: translateX(-120%);
  transition: transform 0.52s ease;
}
.submit-btn__spin {
  animation: spin 0.9s linear infinite;
}
.submit-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px color-mix(in srgb, var(--primary-color) 30%, transparent);
}
.submit-btn:not(:disabled):hover::before {
  transform: translateX(120%);
}
.submit-btn:disabled {
  filter: grayscale(0.2);
  opacity: 0.52;
  animation: disabledFlicker 1.8s ease-in-out infinite;
}
@keyframes disabledFlicker {
  0%, 100% { opacity: 0.52; }
  50% { opacity: 0.62; }
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@keyframes inputNeonBreath {
  0%, 100% {
    box-shadow:
      0 0 0 2px color-mix(in srgb, var(--primary-color) 30%, transparent),
      0 0 0 0 color-mix(in srgb, var(--secondary-color) 0%, transparent) !important;
  }
  50% {
    box-shadow:
      0 0 0 2px color-mix(in srgb, var(--primary-color) 36%, transparent),
      0 0 14px 0 color-mix(in srgb, var(--secondary-color) 22%, transparent) !important;
  }
}
.wall-empty-block {
  text-align: center;
  margin: 8px 0 16px;
}

.wall-empty-title {
  text-align: center;
  font-weight: 900;
  font-size: 1.2rem;
  color: color-mix(in srgb, var(--text-color) 90%, #000 10%);
  margin: 0 0 10px;
  text-shadow: 0 1px 10px rgba(255, 255, 255, 0.32);
  letter-spacing: 0.01em;
}
:root[data-theme='dark'] .wall-empty-title {
  color: #eaf8ff;
  text-shadow: 0 1px 14px rgba(0, 0, 0, 0.4);
}
.home-wall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  max-width: 1100px;
  margin: 0 auto;
}
.home-wall-card {
  padding: 20px;
  border-radius: 20px;
  border: 1.5px solid rgba(167, 139, 250, 0.25);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

/* 卡片左上角装饰圆点 */
.home-wall-card::before {
  content: '';
  position: absolute;
  top: 12px;
  left: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #66d9ff, #a78bfa);
  box-shadow: 0 0 8px rgba(102, 217, 255, 0.5);
}

.home-wall-card:hover {
  transform: translateY(-4px);
  border-color: rgba(167, 139, 250, 0.5);
  box-shadow: 
    0 16px 40px rgba(15, 23, 42, 0.15),
    0 0 24px color-mix(in srgb, rgba(167, 139, 250, 0.3) 50%, transparent);
}

.home-wall-meta {
  font-size: 0.86rem;
  font-weight: 600;
  color: rgba(102, 217, 255, 0.9);
  margin-bottom: 12px;
  margin-left: 16px;
  letter-spacing: 0.02em;
}
.home-wall-body {
  margin: 0;
  margin-left: 16px;
  line-height: 1.65;
  font-size: 0.99rem;
  color: var(--text-color, #0f172a);
  word-break: break-word;
}
.home-wall-reply {
  margin: 12px 0 0;
  margin-left: 16px;
  font-size: 0.9rem;
  color: rgba(167, 139, 250, 0.9);
  background: rgba(167, 139, 250, 0.1);
  border-left: 3px solid rgba(167, 139, 250, 0.6);
  padding: 10px 12px;
  border-radius: 0 12px 12px 0;
}
.wall-empty-hint {
  margin-top: 8px;
}
.wall-pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--primary-color) !important;
}
</style>
