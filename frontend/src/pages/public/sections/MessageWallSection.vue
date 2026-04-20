<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { fetchWallMessages, submitWallMessage, type WallMessagePublic } from '../../../api/wall'
import { escapeHtml } from '../../../utils/escapeHtml'

const { t } = useI18n()

const loading = ref(true)
const messages = ref<WallMessagePublic[]>([])
const total = ref(0)
const page = ref(0)
const pageSize = ref(8)
const author = ref('')
const content = ref('')
const submitting = ref(false)

const canSubmit = computed(() => content.value.trim().length > 0)

async function load() {
  loading.value = true
  try {
    const res = await fetchWallMessages({ page: page.value, size: pageSize.value })
    messages.value = res.items
    total.value = res.total
  } catch {
    messages.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

watch(page, () => void load())

async function submit() {
  if (!canSubmit.value) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  submitting.value = true
  try {
    await submitWallMessage(author.value, content.value)
    content.value = ''
    author.value = ''
    ElMessage.success(t('messageWall.pendingReview'))
    page.value = 0
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : 'Error')
  } finally {
    submitting.value = false
  }
}

function formatTime(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

function onPageChange(p: number) {
  page.value = p - 1
}

void load()
</script>

<template>
  <section class="section message-wall-section" id="message">
    <h2>{{ t('messageWall.title') }}</h2>
    <p class="section-sub">{{ t('messageWall.subtitle') }}</p>

    <div class="site-module-card home-message-form site-el-round-16 site-field">
      <el-input
        v-model="author"
        class="home-field"
        :placeholder="t('messageWall.nicknamePlaceholder')"
        clearable
      />
      <el-input
        v-model="content"
        type="textarea"
        :rows="3"
        class="home-field"
        :placeholder="t('messageWall.contentPlaceholder')"
      />
      <button
        type="button"
        class="site-pill site-pill--primary-cta submit-btn"
        :disabled="!canSubmit || submitting"
        @click="submit"
      >
        <el-icon v-if="submitting" class="submit-btn__spin"><Loading /></el-icon>
        {{ submitting ? t('messageWall.submitting') : t('messageWall.submit') }}
      </button>
    </div>

    <p v-if="loading" class="muted center">{{ t('pages.loading') }}</p>
    <template v-else>
      <div v-if="messages.length" class="home-wall-grid">
        <div v-for="m in messages" :key="m.id" class="site-module-card home-wall-card">
          <div class="home-wall-meta">{{ m.nickname || t('messageWall.anonymous') }} · {{ formatTime(m.createdAt) }}</div>
          <p class="home-wall-body" v-html="escapeHtml(m.content)"></p>
          <p v-if="m.adminReply" class="home-wall-reply">{{ t('messageWall.replyFromAdmin') }}：{{ m.adminReply }}</p>
        </div>
      </div>
      <div v-else class="wall-empty-block">
        <p class="wall-empty-title"><span aria-hidden="true">💬</span> {{ t('messageWall.emptyTitle') }}</p>
        <p class="muted center wall-empty-hint">{{ t('messageWall.emptyHint') }}</p>
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
  </section>
</template>

<style scoped>
.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  font-weight: 400;
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
  padding: 24px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.48);
  box-shadow:
    0 12px 34px rgba(15, 23, 42, 0.1),
    0 0 20px color-mix(in srgb, var(--primary-color) 24%, transparent);
}
.home-message-form .home-field {
  margin-bottom: 12px;
}

.home-message-form :deep(.el-input__wrapper),
.home-message-form :deep(.el-textarea__inner) {
  border-radius: 14px !important;
  padding: 8px 12px !important;
}

.home-message-form :deep(.el-input__inner),
.home-message-form :deep(.el-textarea__inner) {
  font-size: 0.95rem;
  line-height: 1.65;
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
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
}
.home-wall-card {
  padding: 16px;
  border-radius: 16px;
}
.home-wall-meta {
  font-size: 0.78rem;
  color: var(--text-muted);
  margin-bottom: 8px;
}
.home-wall-body {
  margin: 0;
  line-height: 1.55;
  color: var(--text-color, #0f172a);
  word-break: break-word;
}
.home-wall-reply {
  margin: 10px 0 0;
  font-size: 0.85rem;
  color: var(--text-muted);
  border-left: 3px solid color-mix(in srgb, var(--primary-color) 55%, transparent);
  padding-left: 10px;
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
