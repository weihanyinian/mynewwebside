<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'

const email = ref('')
const subscribing = ref(false)

async function subscribe() {
  const e = email.value.trim()
  if (!e || !e.includes('@')) { ElMessage.warning('请输入有效邮箱'); return }
  subscribing.value = true
  try {
    // Buttondown API: POST https://api.buttondown.email/v1/subscribers
    await http.post('https://api.buttondown.email/v1/subscribers', { email: e },
      { headers: { 'Authorization': `Token ${import.meta.env.VITE_BUTTONDOWN_API_KEY || ''}` } })
    ElMessage.success('订阅成功！请查收确认邮件')
    email.value = ''
  } catch {
    // If no Buttondown key configured, open external form
    const formUrl = import.meta.env.VITE_BUTTONDOWN_FORM_URL as string | undefined
    if (formUrl) window.open(formUrl, '_blank')
    else ElMessage.info('订阅功能即将上线')
  }
  finally { subscribing.value = false }
}
</script>

<template>
  <div class="newsletter-signup">
    <h4 class="newsletter-title">📬 邮件订阅</h4>
    <p class="newsletter-desc">新文章发布时收到通知，无垃圾邮件。</p>
    <div class="newsletter-row">
      <input v-model="email" type="email" placeholder="your@email.com" class="newsletter-input" @keydown.enter="subscribe" />
      <button class="newsletter-btn" :disabled="subscribing" @click="subscribe">{{ subscribing ? '...' : '订阅' }}</button>
    </div>
  </div>
</template>

<style scoped>
.newsletter-signup {
  padding: 16px; border-radius: 14px;
  background: var(--surface-2); border: 1px solid var(--glass-border);
}
.newsletter-title { font-size: 0.9rem; font-weight: 700; margin: 0 0 4px; color: var(--text-color); }
.newsletter-desc { font-size: 0.75rem; color: var(--text-muted); margin: 0 0 10px; }
.newsletter-row { display: flex; gap: 6px; }
.newsletter-input {
  flex: 1; padding: 8px 12px; border-radius: 8px; font-size: 0.82rem;
  border: 1px solid var(--glass-border); background: var(--surface-1); color: var(--text-color); outline: none;
}
.newsletter-input:focus { border-color: var(--primary-color); }
.newsletter-btn {
  padding: 8px 16px; border-radius: 8px; border: none; background: var(--primary-color); color: #fff;
  font-weight: 700; font-size: 0.82rem; cursor: pointer; transition: opacity 0.2s;
}
.newsletter-btn:disabled { opacity: 0.6; }
</style>
