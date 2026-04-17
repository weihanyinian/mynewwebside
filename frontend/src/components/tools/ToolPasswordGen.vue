<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const LS_HISTORY = 'weihan_tool_pwd_history'
const MAX_HISTORY = 10

const pwdLength = ref(12)
const pwdUpper = ref(true)
const pwdLower = ref(true)
const pwdNum = ref(true)
const pwdSym = ref(true)
const pwdResult = ref('')
const history = ref<string[]>([])
const copyHint = ref('')

onMounted(() => {
  try {
    const h = localStorage.getItem(LS_HISTORY)
    if (h) {
      const p = JSON.parse(h) as string[]
      if (Array.isArray(p)) history.value = p.slice(0, MAX_HISTORY)
    }
  } catch {
    /* ignore */
  }
})

function persistHistory(list: string[]) {
  try {
    localStorage.setItem(LS_HISTORY, JSON.stringify(list.slice(0, MAX_HISTORY)))
  } catch {
    /* ignore */
  }
}

function generatePwd() {
  const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const lower = 'abcdefghijklmnopqrstuvwxyz'
  const num = '0123456789'
  const sym = '!@#$%^&*()_+~`|}{[]:;?><,./-='
  let charset = ''
  if (pwdUpper.value) charset += upper
  if (pwdLower.value) charset += lower
  if (pwdNum.value) charset += num
  if (pwdSym.value) charset += sym
  if (!charset) {
    pwdResult.value = t('tools.pwdNeedCharset')
    return
  }
  let res = ''
  const arr = new Uint32Array(pwdLength.value)
  crypto.getRandomValues(arr)
  for (let i = 0; i < pwdLength.value; i++) {
    res += charset[arr[i]! % charset.length]
  }
  pwdResult.value = res
  const nextHist = [res, ...history.value.filter((x) => x !== res)].slice(0, MAX_HISTORY)
  history.value = nextHist
  persistHistory(nextHist)
}

async function copyPwd(text?: string) {
  const v = text ?? pwdResult.value
  if (!v || v === t('tools.pwdNeedCharset')) return
  try {
    await navigator.clipboard.writeText(v)
    copyHint.value = t('tools.copied')
    window.setTimeout(() => {
      copyHint.value = ''
    }, 1600)
  } catch {
    copyHint.value = t('tools.copyFail')
  }
}

function clearHistory() {
  history.value = []
  try {
    localStorage.removeItem(LS_HISTORY)
  } catch {
    /* ignore */
  }
}
</script>

<template>
  <div class="tool-inner">
    <h2 class="tool-title">🔑 {{ t('tools.password') }}</h2>
    <div class="pwd-result-box">
      <span class="pwd-text">{{ pwdResult || t('tools.generatePwdHint') }}</span>
    </div>
    <div class="pwd-actions">
      <button type="button" class="site-pill site-pill--active pwd-action-btn" @click="generatePwd">
        {{ t('tools.generatePwd') }}
      </button>
      <button type="button" class="site-pill site-pill--nav pwd-action-btn" :disabled="!pwdResult" @click="copyPwd()">
        {{ t('tools.copy') }}
      </button>
    </div>
    <p v-if="copyHint" class="copy-hint">{{ copyHint }}</p>
    <div class="pwd-settings">
      <div class="pwd-row">
        <label>{{ t('tools.pwdLength') }} ({{ pwdLength }})</label>
        <input v-model.number="pwdLength" type="range" min="4" max="64" class="pwd-slider" />
      </div>
      <div class="pwd-options">
        <label class="cyber-checkbox"><input v-model="pwdUpper" type="checkbox" /> <span>{{ t('tools.pwdUpper') }}</span></label>
        <label class="cyber-checkbox"><input v-model="pwdLower" type="checkbox" /> <span>{{ t('tools.pwdLower') }}</span></label>
        <label class="cyber-checkbox"><input v-model="pwdNum" type="checkbox" /> <span>{{ t('tools.pwdNum') }}</span></label>
        <label class="cyber-checkbox"><input v-model="pwdSym" type="checkbox" /> <span>{{ t('tools.pwdSym') }}</span></label>
      </div>
    </div>
    <section v-if="history.length" class="hist-section">
      <div class="hist-head">
        <h3 class="hist-title">{{ t('tools.pwdRecent') }}</h3>
        <button type="button" class="hist-clear" @click="clearHistory">{{ t('tools.clearHistory') }}</button>
      </div>
      <ul class="hist-list">
        <li v-for="(p, i) in history" :key="i" class="hist-item">
          <code class="hist-code">{{ p }}</code>
          <button type="button" class="hist-copy site-pill site-pill--nav" @click="copyPwd(p)">{{ t('tools.copy') }}</button>
        </li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.tool-inner {
  min-height: 120px;
}

.tool-title {
  font-size: 1.35rem;
  font-weight: 800;
  margin: 0 0 1.25rem;
  text-align: center;
  background: linear-gradient(to right, #4a90e2, #50e3c2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

:root[data-theme='dark'] .tool-title {
  background: linear-gradient(to right, #a18cd1, #66d9ff);
  -webkit-background-clip: text;
}

.pwd-result-box {
  background: rgba(74, 144, 226, 0.08);
  border: 1px solid rgba(74, 144, 226, 0.35);
  padding: 14px;
  border-radius: 12px;
  font-family: ui-monospace, monospace;
  font-size: 1.05rem;
  margin-bottom: 0.75rem;
  word-break: break-all;
  min-height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.pwd-text {
  width: 100%;
}

.pwd-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.pwd-action-btn {
  cursor: pointer;
  border: none;
  font: inherit;
  padding: 8px 16px;
}

.copy-hint {
  text-align: center;
  font-size: 0.82rem;
  font-weight: 600;
  color: #50e3c2;
  margin: 0 0 0.75rem;
}

.pwd-settings {
  text-align: left;
  margin-bottom: 1rem;
}

.pwd-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}

.pwd-slider {
  width: 58%;
  min-width: 0;
}

.pwd-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.cyber-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.88rem;
}

.hist-section {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(74, 144, 226, 0.2);
}

.hist-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

.hist-title {
  margin: 0;
  font-size: 0.88rem;
  font-weight: 800;
}

.hist-clear {
  border: none;
  background: none;
  cursor: pointer;
  font: inherit;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--primary-color, #4a90e2);
  text-decoration: underline;
}

.hist-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hist-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hist-code {
  flex: 1;
  min-width: 0;
  font-size: 0.78rem;
  padding: 6px 8px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.06);
  word-break: break-all;
}

:root[data-theme='dark'] .hist-code {
  background: rgba(255, 255, 255, 0.06);
}

.hist-copy {
  flex-shrink: 0;
  cursor: pointer;
  border: none;
  font: inherit;
  font-size: 0.72rem;
  padding: 4px 10px;
}
</style>
