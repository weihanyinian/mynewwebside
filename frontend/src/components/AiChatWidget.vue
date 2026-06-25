<script setup lang="ts">
import { ref } from 'vue'

const expanded = ref(false)
const messages = ref<{ role: string; content: string }[]>([])
const input = ref('')

function send() {
  if (!input.value.trim()) return
  messages.value.push({ role: 'user', content: input.value })
  messages.value.push({ role: 'assistant', content: 'AI 伴聊功能即将上线，敬请期待~ 🤖' })
  input.value = ''
}
</script>

<template>
  <!-- AI Chat: top-right floating -->
  <div class="fixed top-20 right-4 z-40">
    <!-- Collapsed -->
    <button
      v-if="!expanded"
      @click="expanded = true"
      class="glass-button !p-3 !rounded-full text-xl animate-float"
      title="AI 伴聊"
    >
      🤖
    </button>

    <!-- Expanded -->
    <div v-else class="glass-card p-4 w-80 max-h-[70vh] flex flex-col">
      <div class="flex items-center justify-between mb-3">
        <span class="text-sm font-bold gradient-text">🤖 AI 伴聊</span>
        <button @click="expanded = false" class="glass-button !p-1 !rounded-full text-xs !px-2">✕</button>
      </div>

      <!-- Messages -->
      <div class="flex-1 overflow-y-auto mb-3 space-y-2 max-h-96">
        <div v-for="(msg, i) in messages" :key="i" class="text-xs">
          <div :class="msg.role === 'user' ? 'text-right' : 'text-left'">
            <span
              class="inline-block px-3 py-1.5 rounded-xl max-w-[85%]"
              :class="msg.role === 'user'
                ? 'bg-[#62a7ea33]'
                : 'bg-[var(--glass-bg)]'"
            >
              {{ msg.content }}
            </span>
          </div>
        </div>
        <p v-if="messages.length === 0" class="text-xs text-[var(--text-secondary)] text-center py-4">
          你好，我是小寒~ 👋<br />有什么想聊的吗？
        </p>
      </div>

      <!-- Input -->
      <div class="flex gap-2">
        <input
          v-model="input"
          @keyup.enter="send"
          class="glass-input !text-xs flex-1"
          placeholder="说点什么..."
        />
        <button @click="send" class="glass-button primary !p-2 !rounded-lg text-xs">发送</button>
      </div>
    </div>
  </div>
</template>
