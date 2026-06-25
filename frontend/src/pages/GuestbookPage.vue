<script setup lang="ts">
import { ref, onMounted } from 'vue'
import GlassCard from '../components/GlassCard.vue'

interface Message {
  id: number
  nickname: string
  content: string
  createdAt: string
}

const messages = ref<Message[]>([])
const nickname = ref('')
const content = ref('')
const submitting = ref(false)

async function loadMessages() {
  try {
    const res = await fetch('/api/guestbook')
    const data = await res.json()
    if (data.code === 200) messages.value = data.data || []
  } catch (e) { /* backend not available */ }
}

async function submit() {
  if (!nickname.value.trim() || !content.value.trim()) return
  submitting.value = true
  try {
    const res = await fetch('/api/guestbook', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nickname: nickname.value, content: content.value })
    })
    const data = await res.json()
    if (data.code === 200) {
      nickname.value = ''
      content.value = ''
      await loadMessages()
    }
  } catch (e) { /* error */ }
  finally { submitting.value = false }
}

onMounted(loadMessages)
</script>

<template>
  <div class="page-container max-w-2xl">
    <h1 class="text-3xl font-bold mb-8 gradient-text">💬 留言板</h1>

    <p class="text-[var(--text-secondary)] mb-8 leading-relaxed">
      路过就留下点什么吧~<br />
      可以是建议、吐槽、问候，或者随便聊聊。
    </p>

    <!-- Form -->
    <GlassCard class="mb-8">
      <input v-model="nickname" class="glass-input mb-3" placeholder="昵称（必填）" />
      <textarea v-model="content" class="glass-input mb-3" rows="3" placeholder="说点什么吧..." />
      <button @click="submit" :disabled="submitting" class="glass-button primary text-sm">
        {{ submitting ? '提交中...' : '💬 发表留言' }}
      </button>
    </GlassCard>

    <!-- Messages -->
    <div v-if="messages.length === 0" class="glass-card p-8 text-center">
      <p class="text-4xl mb-3">💬</p>
      <p class="text-[var(--text-secondary)]">还没有留言，来做第一个留言的人吧~</p>
    </div>

    <div v-else class="space-y-4">
      <GlassCard v-for="msg in messages" :key="msg.id">
        <div class="flex items-center gap-2 mb-2">
          <span class="font-bold text-sm">{{ msg.nickname }}</span>
          <span class="text-xs text-[var(--text-secondary)]">
            {{ new Date(msg.createdAt).toLocaleDateString('zh-CN') }}
          </span>
        </div>
        <p class="text-sm text-[var(--text-secondary)] whitespace-pre-wrap">{{ msg.content }}</p>
      </GlassCard>
    </div>
  </div>
</template>
