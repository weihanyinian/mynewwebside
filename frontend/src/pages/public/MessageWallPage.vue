<script setup lang="ts">
/**
 * 留言墙：对接后端审核流；列表仅展示已通过记录；提交后为待审核状态。
 */
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import BackToBlogButton from '../../components/BackToBlogButton.vue'
import { fetchWallMessages, submitWallMessage, type WallMessagePublic } from '../../api/wall'

const { t } = useI18n()

const loading = ref(true)
const loadError = ref(false)
const messages = ref<WallMessagePublic[]>([])

const newAuthor = ref('')
const newContent = ref('')
const submitting = ref(false)

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const res = await fetchWallMessages({ page: 0, size: 50 })
    messages.value = res.items
  } catch {
    loadError.value = true
    messages.value = []
  } finally {
    loading.value = false
  }
}

function formatTime(iso: string) {
  try {
    return new Date(iso).toLocaleString()
  } catch {
    return iso
  }
}

async function submitMessage() {
  if (!newContent.value.trim()) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  submitting.value = true
  try {
    await submitWallMessage(newAuthor.value, newContent.value)
    newContent.value = ''
    newAuthor.value = ''
    ElMessage.success(t('messageWall.pendingReview'))
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    submitting.value = false
  }
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

    <!-- site-el-round-16：输入框圆角 16px 与全站统一 -->
    <div class="message-form glass-card site-el-round-16">
      <el-input v-model="newAuthor" :placeholder="t('messageWall.nickname')" class="mb-3" />
      <el-input
        v-model="newContent"
        type="textarea"
        :rows="3"
        :placeholder="t('messageWall.placeholder')"
        class="mb-3"
      />
      <div class="form-actions">
        <!-- 「发射留言」→ 与首页「摸鱼」一致的粉系渐变主按钮 -->
        <button type="button" class="site-pill site-pill--pink" :disabled="submitting" @click="submitMessage">
          {{ t('messageWall.submit') }}
        </button>
      </div>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="messages-grid" aria-busy="true">
      <div v-for="n in 4" :key="'sk-' + n" class="message-card glass-card skeleton-card">
        <div class="sk-line sk-line--short" />
        <div class="sk-line" />
        <div class="sk-line" />
        <div class="sk-line sk-line--meta" />
      </div>
    </div>

    <p v-else-if="loadError" class="wall-error glass-inline">{{ t('messageWall.loadError') }}</p>

    <template v-else>
      <div v-if="messages.length" class="messages-grid">
        <transition-group name="list">
          <div v-for="m in messages" :key="m.id" class="message-card glass-card">
            <div class="msg-header">
              <div class="msg-author-info">
                <span class="msg-avatar">💬</span>
                <span class="msg-author">{{ m.nickname }}</span>
              </div>
              <div class="msg-time">{{ formatTime(m.createdAt) }}</div>
            </div>
            <div class="msg-content">{{ m.content }}</div>
            <div v-if="m.adminReply" class="msg-reply glass-reply">
              <span class="msg-reply__label">{{ t('messageWall.replyFromAdmin') }}</span>
              <p class="msg-reply__text">{{ m.adminReply }}</p>
            </div>
          </div>
        </transition-group>
      </div>

      <div v-else class="wall-empty glass-card">
        <div class="wall-empty__icon">🍃</div>
        <h2 class="wall-empty__title">{{ t('messageWall.emptyTitle') }}</h2>
        <p class="wall-empty__hint">{{ t('messageWall.emptyHint') }}</p>
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
  background: linear-gradient(to right, #fff, #a5d8ff);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.wall-subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1.1rem;
}

.message-form {
  padding: 24px;
  margin-bottom: 40px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
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
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
}

.message-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 36px rgba(102, 217, 255, 0.28);
}

.msg-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.9);
}

.msg-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.msg-content {
  line-height: 1.6;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  white-space: pre-wrap;
}

.msg-reply {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid rgba(80, 227, 194, 0.35);
  background: rgba(0, 40, 60, 0.2);
}

.msg-reply__label {
  font-size: 0.75rem;
  font-weight: 700;
  color: #50e3c2;
}

.msg-reply__text {
  margin: 6px 0 0;
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.92);
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
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0.08));
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
  color: #ffe0e0;
  margin-bottom: 16px;
}

.glass-inline {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 180, 180, 0.4);
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
  color: #fff;
}

.wall-empty__hint {
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.6;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
}

.mb-3 {
  margin-bottom: 12px;
}
</style>
