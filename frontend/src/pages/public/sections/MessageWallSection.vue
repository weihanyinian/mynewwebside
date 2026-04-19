<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { fetchWallMessages, submitWallMessage, type WallMessagePublic } from '../../../api/wall'

const { t } = useI18n()

const loading = ref(true)
const messages = ref<WallMessagePublic[]>([])
const author = ref('')
const content = ref('')
const submitting = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await fetchWallMessages({ page: 0, size: 50 })
    messages.value = res.items
  } catch {
    messages.value = []
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!content.value.trim()) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  submitting.value = true
  try {
    await submitWallMessage(author.value, content.value)
    content.value = ''
    author.value = ''
    ElMessage.success(t('messageWall.pendingReview'))
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

// 暴露 load 方法供外部 onMounted 调用
load()
</script>

<template>
  <section class="section" id="message">
    <h2>{{ t('messageWall.title') }}</h2>
    <p class="section-sub">{{ t('messageWall.subtitle') }}</p>
    <div class="glass-card home-message-form site-el-round-16">
      <el-input v-model="author" :placeholder="t('messageWall.nickname')" class="home-field" />
      <el-input v-model="content" type="textarea" :rows="3" :placeholder="t('messageWall.placeholder')" class="home-field" />
      <button type="button" class="site-pill site-pill--pink" :disabled="submitting" @click="submit">
        {{ t('messageWall.submit') }}
      </button>
    </div>
    <p v-if="loading" class="muted center">{{ t('pages.loading') }}</p>
    <div v-else class="home-wall-grid">
      <div v-for="m in messages" :key="m.id" class="glass-card home-wall-card">
        <div class="home-wall-meta">{{ m.nickname || t('messageWall.anonymous') }} · {{ formatTime(m.createdAt) }}</div>
        <p class="home-wall-body">{{ m.content }}</p>
        <p v-if="m.adminReply" class="home-wall-reply">{{ t('messageWall.replyFromAdmin') }}：{{ m.adminReply }}</p>
      </div>
      <p v-if="!messages.length" class="muted center">{{ t('messageWall.emptyTitle') }}</p>
    </div>
  </section>
</template>

<style scoped>
.section-sub {
  text-align: center;
  max-width: 640px;
  margin: -1rem auto 1.5rem;
  opacity: 0.85;
  line-height: 1.6;
}
.center { text-align: center; }
.home-message-form {
  max-width: 560px;
  margin: 0 auto 28px;
  padding: 24px;
}
.home-message-form .home-field {
  margin-bottom: 12px;
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
  opacity: 0.65;
  margin-bottom: 8px;
}
.home-wall-body {
  margin: 0;
  line-height: 1.55;
}
.home-wall-reply {
  margin: 10px 0 0;
  font-size: 0.85rem;
  opacity: 0.8;
  border-left: 3px solid rgba(102, 217, 255, 0.5);
  padding-left: 10px;
}
</style>
