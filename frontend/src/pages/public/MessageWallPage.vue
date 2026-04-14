<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getToken } from '../../utils/token'

const { t } = useI18n()

// Using token utility to check admin
function isAdmin() {
  const token = getToken()
  return token === 'mock-token-admin' || (token && token.length > 20)
}

const messages = ref([
  { id: 1, author: '旅行者', content: '这里真好看！', time: '2026-04-14 10:00', avatar: '🌸' },
  { id: 2, author: '代码之神', content: '测试一下留言功能。', time: '2026-04-14 11:30', avatar: '💻' }
])

const newAuthor = ref('')
const newContent = ref('')

const myMessages = ref<number[]>([])

function submitMessageAndTrack() {
  if (!newContent.value.trim()) {
    ElMessage.warning(t('messageWall.emptyContent'))
    return
  }
  const id = Date.now()
  messages.value.unshift({
    id,
    author: newAuthor.value.trim() || t('messageWall.anonymous'),
    content: newContent.value,
    time: new Date().toLocaleString(),
    avatar: '✨'
  })
  myMessages.value.push(id)
  newContent.value = ''
  newAuthor.value = ''
  ElMessage.success(t('messageWall.success'))
}

function deleteMessage(id: number) {
  messages.value = messages.value.filter(m => m.id !== id)
  ElMessage.success(t('messageWall.deleteSuccess'))
}
</script>

<template>
  <div class="message-wall-page page-animation">
    <div class="wall-header">
      <h1 class="wall-title">{{ t('messageWall.title') }}</h1>
      <p class="wall-subtitle">{{ t('messageWall.subtitle') }}</p>
    </div>

    <div class="message-form glass-card">
      <el-input v-model="newAuthor" :placeholder="t('messageWall.nickname')" class="cyber-input mb-3" />
      <el-input
        v-model="newContent"
        type="textarea"
        :rows="3"
        :placeholder="t('messageWall.placeholder')"
        class="cyber-textarea mb-3"
      />
      <div class="form-actions">
        <button class="cyber-btn" @click="submitMessageAndTrack">{{ t('messageWall.submit') }}</button>
      </div>
    </div>

    <div class="messages-grid">
      <transition-group name="list">
        <div v-for="m in messages" :key="m.id" class="message-card glass-card">
          <div class="msg-header">
            <div class="msg-author-info">
              <span class="msg-avatar">{{ m.avatar }}</span>
              <span class="msg-author">{{ m.author }}</span>
            </div>
            <div class="msg-time">{{ m.time }}</div>
          </div>
          <div class="msg-content">{{ m.content }}</div>
          
          <!-- Delete button for admin or self -->
          <button 
            v-if="isAdmin() || myMessages.includes(m.id)" 
            class="delete-btn" 
            @click="deleteMessage(m.id)"
            title="删除留言"
          >
            🗑️
          </button>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<style scoped>
.page-animation {
  animation: fadeIn 0.5s ease-out;
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.wall-header {
  text-align: center;
  margin-bottom: 40px;
}
.wall-title {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(to right, #66d9ff, #fca2e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10px;
}
.wall-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.1rem;
}

.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.1);
  border-radius: 20px;
  padding: 24px;
  color: #fff;
  transition: all 0.3s ease;
}

.message-form {
  margin-bottom: 40px;
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.15) 0%, rgba(252, 162, 229, 0.15) 100%);
}

.mb-3 {
  margin-bottom: 15px;
}

/* Override element-plus input styles to match cyber theme */
:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  background-color: rgba(0, 0, 0, 0.2) !important;
  box-shadow: 0 0 0 1px rgba(102, 217, 255, 0.3) inset !important;
  color: #fff !important;
}
:deep(.el-input__wrapper.is-focus), :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #fca2e5 inset !important;
}
:deep(.el-input__inner::placeholder), :deep(.el-textarea__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5) !important;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.cyber-btn {
  background: linear-gradient(135deg, #66d9ff, #fca2e5);
  color: #fff;
  border: none;
  padding: 10px 24px;
  border-radius: 20px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}
.cyber-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(252, 162, 229, 0.4);
}

.messages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.message-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.message-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(102, 217, 255, 0.2);
  background: rgba(255, 255, 255, 0.15);
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px dashed rgba(255,255,255,0.2);
  padding-bottom: 10px;
}
.msg-author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg-avatar {
  font-size: 1.5rem;
  background: rgba(255,255,255,0.2);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}
.msg-author {
  font-weight: 600;
  color: #66d9ff;
}
.msg-time {
  font-size: 0.8rem;
  color: rgba(255,255,255,0.5);
}
.msg-content {
  font-size: 1.05rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.delete-btn {
  position: absolute;
  bottom: 15px;
  right: 15px;
  background: transparent;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  filter: grayscale(1);
}
.message-card:hover .delete-btn {
  opacity: 1;
}
.delete-btn:hover {
  filter: grayscale(0);
  transform: scale(1.1);
}

/* List Transitions */
.list-enter-active, .list-leave-active {
  transition: all 0.4s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
.list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

@media (max-width: 768px) {
  .messages-grid {
    grid-template-columns: 1fr;
  }
  .wall-title {
    font-size: 2rem;
  }
  .delete-btn {
    opacity: 0.5; /* Always partially visible on mobile */
  }
}
</style>