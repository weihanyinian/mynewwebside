<script setup lang="ts">
/**
 * 留言墙：公开发布；提交后立即展示。
 */
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import BackToBlogButton from '../../components/BackToBlogButton.vue'
import { fetchWallMessages, submitWallMessage, type WallMessagePublic } from '../../api/wall'
import { escapeHtml } from '../../utils/escapeHtml'

const { t } = useI18n()

const loading = ref(true)
const loadError = ref(false)
const messages = ref<WallMessagePublic[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = ref(9)

const newAuthor = ref('')
const newContent = ref('')
const submitting = ref(false)

const canSubmit = computed(() => newContent.value.trim().length > 0)

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const res = await fetchWallMessages({ page: page.value, size: pageSize.value })
    messages.value = res.items
    total.value = res.total
  } catch {
    loadError.value = true
    messages.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

watch(page, () => void load())

function formatTime(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

async function submitMessage() {
  if (!canSubmit.value) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  submitting.value = true
  try {
    await submitWallMessage(newAuthor.value, newContent.value)
    newContent.value = ''
    newAuthor.value = ''
    ElMessage.success(t('messageWall.pendingReview'))
    page.value = 0
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    submitting.value = false
  }
}

function onPageChange(p: number) {
  page.value = p - 1
}

onMounted(load)
</script>

<template>
  <div class="message-wall-page page-animation">
    <div class="wall-back">
      <BackToBlogButton />
    </div>
    <div class="wall-header">
      <h1 class="wall-title">{{ t('messageWall.title') }}</h1>
      <p class="wall-subtitle">{{ t('messageWall.subtitle') }}</p>
    </div>

    <div class="message-form site-module-card site-el-round-16 site-field">
      <el-input v-model="newAuthor" :placeholder="t('messageWall.nicknamePlaceholder')" class="mb-3" clearable />
      <el-input
        v-model="newContent"
        type="textarea"
        :rows="3"
        :placeholder="t('messageWall.contentPlaceholder')"
        class="mb-3"
      />
      <div class="form-actions">
        <button
          type="button"
          class="site-pill site-pill--primary-cta"
          :disabled="!canSubmit || submitting"
          @click="submitMessage"
        >
          <el-icon v-if="submitting" class="submit-ico"><Loading /></el-icon>
          {{ submitting ? t('messageWall.submitting') : t('messageWall.submit') }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="messages-grid" aria-busy="true">
      <div v-for="n in 4" :key="'sk-' + n" class="message-card site-module-card skeleton-card">
        <div class="sk-line sk-line--short" />
        <div class="sk-line" />
        <div class="sk-line" />
        <div class="sk-line sk-line--meta" />
      </div>
    </div>

    <p v-else-if="loadError" class="wall-error">{{ t('messageWall.loadError') }}</p>

    <template v-else>
      <div v-if="messages.length" class="messages-grid">
        <transition-group name="list">
          <div v-for="m in messages" :key="m.id" class="message-card site-module-card">
            <div class="msg-header">
              <div class="msg-author-info">
                <span class="msg-avatar" aria-hidden="true">💬</span>
                <span class="msg-author">{{ m.nickname || t('messageWall.anonymous') }}</span>
              </div>
              <div class="msg-time">{{ formatTime(m.createdAt) }}</div>
            </div>
            <div class="msg-content" v-html="escapeHtml(m.content)"></div>
            <div v-if="m.adminReply" class="msg-reply">
              <span class="msg-reply__label">{{ t('messageWall.replyFromAdmin') }}</span>
              <p class="msg-reply__text">{{ m.adminReply }}</p>
            </div>
          </div>
        </transition-group>
      </div>

      <div v-else class="wall-empty site-module-card">
        <div class="wall-empty__icon" aria-hidden="true">🍃</div>
        <h2 class="wall-empty__title wall-empty__title--emph">{{ t('messageWall.emptyTitle') }}</h2>
        <p class="wall-empty__hint">{{ t('messageWall.emptyHint') }}</p>
      </div>

      <div v-if="total > pageSize" class="wall-pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          :current-page="page + 1"
          :total="total"
          @current-change="onPageChange"
        />
      </div>
    </template>
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
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 8px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.wall-subtitle {
  color: var(--text-muted);
  font-size: 1.1rem;
}

.message-form {
  padding: 24px;
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
