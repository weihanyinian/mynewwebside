<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const base64Input = ref('')
const base64Output = ref('')
const base64Mode = ref<'encode' | 'decode'>('encode')
const hint = ref('')

function utf8ToBase64(str: string): string {
  const bytes = new TextEncoder().encode(str)
  let binary = ''
  bytes.forEach((b) => {
    binary += String.fromCharCode(b)
  })
  return btoa(binary)
}

function base64ToUtf8(b64: string): string {
  const binary = atob(b64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
  return new TextDecoder('utf-8', { fatal: false }).decode(bytes)
}

function processBase64() {
  hint.value = ''
  try {
    if (!base64Input.value) {
      base64Output.value = ''
      return
    }
    if (base64Mode.value === 'encode') {
      base64Output.value = utf8ToBase64(base64Input.value)
    } else {
      const trimmed = base64Input.value.replace(/\s/g, '')
      base64Output.value = base64ToUtf8(trimmed)
    }
  } catch {
    base64Output.value = ''
    hint.value = t('tools.b64Error')
  }
}

watch(base64Mode, () => processBase64())

async function copyOut() {
  if (!base64Output.value) return
  try {
    await navigator.clipboard.writeText(base64Output.value)
    hint.value = t('tools.copied')
    window.setTimeout(() => {
      hint.value = ''
    }, 1500)
  } catch {
    hint.value = t('tools.copyFail')
  }
}

async function copyIn() {
  if (!base64Input.value) return
  try {
    await navigator.clipboard.writeText(base64Input.value)
    hint.value = t('tools.copied')
    window.setTimeout(() => {
      hint.value = ''
    }, 1500)
  } catch {
    hint.value = t('tools.copyFail')
  }
}

function clearAll() {
  base64Input.value = ''
  base64Output.value = ''
  hint.value = ''
}

function resetMode() {
  base64Mode.value = 'encode'
  clearAll()
}
</script>

<template>
  <div class="tool-inner">
    <h2 class="tool-title">🔤 {{ t('tools.base64') }}</h2>
    <p class="b64-note">{{ t('tools.b64Utf8Note') }}</p>
    <div class="b64-modes">
      <button
        type="button"
        class="b64-mode-btn"
        :class="{ active: base64Mode === 'encode' }"
        @click="base64Mode = 'encode'; processBase64()"
      >
        {{ t('tools.encode') }}
      </button>
      <button
        type="button"
        class="b64-mode-btn"
        :class="{ active: base64Mode === 'decode' }"
        @click="base64Mode = 'decode'; processBase64()"
      >
        {{ t('tools.decode') }}
      </button>
    </div>
    <div class="ta-toolbar">
      <span class="ta-label">{{ t('tools.b64InputLabel') }}</span>
      <div class="ta-btns">
        <button type="button" class="site-pill site-pill--nav mini" :disabled="!base64Input" @click="copyIn">{{ t('tools.copy') }}</button>
      </div>
    </div>
    <textarea v-model="base64Input" class="cyber-textarea b64-input" :placeholder="t('tools.inputB64')" @input="processBase64" />
    <div class="b64-arrow">↓</div>
    <div class="ta-toolbar">
      <span class="ta-label">{{ t('tools.outputB64') }}</span>
      <div class="ta-btns">
        <button type="button" class="site-pill site-pill--nav mini" :disabled="!base64Output" @click="copyOut">{{ t('tools.copyResult') }}</button>
      </div>
    </div>
    <textarea v-model="base64Output" class="cyber-textarea b64-output" readonly :placeholder="t('tools.outputB64')" />
    <p v-if="hint" class="hint-line">{{ hint }}</p>
    <div class="b64-footer-btns">
      <button type="button" class="site-pill site-pill--nav" @click="clearAll">{{ t('tools.b64Clear') }}</button>
      <button type="button" class="site-pill site-pill--nav" @click="resetMode">{{ t('tools.b64Reset') }}</button>
    </div>
  </div>
</template>

<style scoped>
.tool-inner {
  min-height: 120px;
}

.tool-title {
  font-size: 1.35rem;
  font-weight: 800;
  margin: 0 0 0.5rem;
  text-align: center;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

:root[data-theme='dark'] .tool-title {
  background: linear-gradient(to right, #a18cd1, #66d9ff);
  -webkit-background-clip: text;
}

.b64-note {
  margin: 0 0 1rem;
  text-align: center;
  font-size: 0.78rem;
  opacity: 0.75;
  font-weight: 600;
}

.b64-modes {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.b64-mode-btn {
  padding: 6px 18px;
  border-radius: 20px;
  background: rgba(74, 144, 226, 0.12);
  border: 1px solid transparent;
  cursor: pointer;
  font: inherit;
  font-weight: 600;
  color: inherit;
}

.b64-mode-btn.active {
  background: linear-gradient(135deg, #66d9ff, #50e3c2);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.2);
}

.ta-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  gap: 8px;
}

.ta-label {
  font-size: 0.78rem;
  font-weight: 700;
  opacity: 0.85;
}

.ta-btns {
  display: flex;
  gap: 6px;
}

.mini {
  font-size: 0.75rem;
  padding: 4px 10px;
  cursor: pointer;
  border: none;
  font: inherit;
}

.cyber-textarea {
  width: 100%;
  height: 120px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(74, 144, 226, 0.35);
  border-radius: 12px;
  padding: 10px;
  color: inherit;
  font-family: ui-monospace, monospace;
  resize: vertical;
  box-sizing: border-box;
}

:root[data-theme='dark'] .cyber-textarea {
  background: rgba(0, 0, 0, 0.35);
  border-color: rgba(102, 217, 255, 0.3);
}

.cyber-textarea:focus {
  outline: none;
  border-color: #50e3c2;
}

.b64-arrow {
  text-align: center;
  margin: 8px 0;
  font-size: 1.25rem;
  color: #4a90e2;
}

.hint-line {
  margin: 10px 0 0;
  font-size: 0.82rem;
  font-weight: 600;
  color: #e67e22;
  text-align: center;
}

:root[data-theme='dark'] .hint-line {
  color: #f1c40f;
}

.b64-footer-btns {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 1rem;
}

.b64-footer-btns button {
  cursor: pointer;
  border: none;
  font: inherit;
  padding: 8px 14px;
}
</style>
