<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { chatWithCompanion } from '../api/companion'

type ChatRole = 'user' | 'ai'
type ChatRow = { id: number; role: ChatRole; text: string }

const open = ref(false)
const loading = ref(false)
const input = ref('')
const listRef = ref<HTMLElement | null>(null)
const rows = ref<ChatRow[]>([])
let seq = 1

const hasRows = computed(() => rows.value.length > 0)

function toggleOpen() {
  open.value = !open.value
}

function push(role: ChatRole, text: string) {
  rows.value.push({ id: seq++, role, text })
}

async function toBottom() {
  await nextTick()
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return
  input.value = ''
  push('user', text)
  await toBottom()
  loading.value = true
  try {
    const reply = await chatWithCompanion(text)
    push('ai', reply || '嗯，我在。')
  } catch (e) {
    push('ai', e instanceof Error ? e.message : '请求失败')
  } finally {
    loading.value = false
    await toBottom()
  }
}
</script>

<template>
  <div class="avatar-shell">
    <button class="avatar-badge" type="button" @click="toggleOpen">
      <img src="/avatar.png" alt="avatar" />
    </button>

    <section v-if="open" class="chat-panel">
      <header class="chat-head">
        <span>AI 对话</span>
        <button type="button" class="link-btn" @click="toggleOpen">收起</button>
      </header>
      <div ref="listRef" class="chat-list">
        <p v-if="!hasRows" class="tip">点头像开始聊吧。</p>
        <div v-for="r in rows" :key="r.id" class="row" :class="r.role === 'user' ? 'row--user' : 'row--ai'">
          <div class="bubble">{{ r.text }}</div>
        </div>
        <p v-if="loading" class="tip">正在思考...</p>
      </div>
      <footer class="chat-input">
        <input v-model="input" maxlength="300" :disabled="loading" @keydown.enter.prevent="send" />
        <button type="button" :disabled="loading || !input.trim()" @click="send">发送</button>
      </footer>
    </section>
  </div>
</template>

<style scoped>
.avatar-shell {
  position: fixed;
  right: 16px;
  top: 84px;
  z-index: 1200;
  display: flex;
  flex-direction: row-reverse;
  align-items: flex-start;
  gap: 10px;
}

.avatar-badge {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  z-index: 1200;
  border: 1px solid rgba(176, 192, 214, 0.65);
  box-shadow: 0 8px 24px rgba(32, 55, 82, 0.18);
  background: rgba(255, 255, 255, 0.85);
  cursor: pointer;
}

.avatar-badge img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.chat-panel {
  width: min(340px, calc(100vw - 96px));
  height: 400px;
  border-radius: 12px;
  border: 1px solid var(--companion-panel-border);
  background: var(--companion-panel-bg);
  box-shadow: var(--companion-panel-shadow);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-head {
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  border-bottom: 1px solid var(--companion-head-border);
  background: var(--companion-head-bg);
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--companion-head-text);
}

.link-btn {
  border: none;
  background: transparent;
  color: var(--companion-link);
  font-weight: 500;
  cursor: pointer;
}

.link-btn:hover {
  color: var(--companion-link-hover);
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: var(--companion-list-bg);
}

.tip {
  color: var(--companion-tip);
  font-size: 0.84rem;
}

.row {
  display: flex;
  margin-bottom: 8px;
}

.row--user {
  justify-content: flex-end;
}

.bubble {
  max-width: 80%;
  padding: 7px 10px;
  border-radius: 10px;
  font-size: 0.88rem;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}

.row--ai .bubble {
  background: var(--companion-ai-bubble-bg);
  color: var(--companion-ai-bubble-text);
  border: 1px solid var(--companion-ai-bubble-border);
}

.row--user .bubble {
  background: var(--companion-user-bubble-bg);
  color: var(--companion-user-bubble-text);
  border: 1px solid var(--companion-user-bubble-border);
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid var(--companion-head-border);
  background: var(--companion-head-bg);
}

.chat-input input {
  flex: 1;
  border: 1px solid var(--companion-input-border);
  border-radius: 8px;
  padding: 8px 10px;
  background: var(--companion-input-bg);
  color: var(--companion-input-text);
  outline: none;
}

.chat-input input:focus-visible {
  border-color: var(--companion-focus-ring);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--companion-focus-ring) 35%, transparent);
}

.chat-input input::placeholder {
  color: var(--companion-input-placeholder);
}

.chat-input button {
  border: 1px solid var(--companion-send-border);
  background: var(--companion-send-bg);
  color: var(--companion-send-text);
  font-weight: 500;
  border-radius: 8px;
  padding: 0 14px;
  cursor: pointer;
  outline: none;
}

.chat-input button:focus-visible {
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--companion-focus-ring) 40%, transparent);
}

.chat-input button:hover:not(:disabled) {
  background: var(--companion-send-hover-bg);
}

.chat-input button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .avatar-shell {
    right: 10px;
    top: 78px;
  }
  .avatar-badge {
    width: 50px;
    height: 50px;
  }
  .chat-panel {
    width: calc(100vw - 84px);
    height: 360px;
  }
}
</style>
