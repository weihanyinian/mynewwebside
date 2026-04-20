<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

const props = defineProps<{
  nickname: string
  content: string
  submitting: boolean
  canSubmit: boolean
  nicknamePlaceholder: string
  contentPlaceholder: string
  submitLabel: string
  submittingLabel: string
}>()

const emit = defineEmits<{
  'update:nickname': [value: string]
  'update:content': [value: string]
  submit: []
}>()
</script>

<template>
  <div class="site-module-card home-message-form site-el-round-16 site-field">
    <el-input
      :model-value="props.nickname"
      class="home-field"
      :placeholder="props.nicknamePlaceholder"
      clearable
      @update:model-value="(v: string | number | null | undefined) => emit('update:nickname', String(v ?? ''))"
    />
    <el-input
      :model-value="props.content"
      type="textarea"
      :rows="3"
      class="home-field"
      :placeholder="props.contentPlaceholder"
      @update:model-value="(v: string | number | null | undefined) => emit('update:content', String(v ?? ''))"
    />
    <button
      type="button"
      class="site-pill site-pill--primary-cta submit-btn"
      :disabled="!props.canSubmit || props.submitting"
      @click="emit('submit')"
    >
      <el-icon v-if="props.submitting" class="submit-btn__spin"><Loading /></el-icon>
      {{ props.submitting ? props.submittingLabel : props.submitLabel }}
    </button>
  </div>
</template>

<style scoped>
.home-message-form {
  max-width: 560px;
  margin: 0 auto 28px;
  padding: 28px;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 2px solid rgba(102, 217, 255, 0.35);
  border-radius: 24px;
  box-shadow:
    0 16px 48px rgba(15, 23, 42, 0.12),
    0 0 32px color-mix(in srgb, rgba(102, 217, 255, 0.2) 40%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  position: relative;
  overflow: hidden;
}
.home-message-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 3px;
  background: linear-gradient(90deg, rgba(102, 217, 255, 0.6), rgba(167, 139, 250, 0.6), rgba(244, 167, 194, 0.6));
  border-radius: 0 0 3px 3px;
}
.home-message-form .home-field {
  margin-bottom: 12px;
}
.home-message-form :deep(.el-input__wrapper),
.home-message-form :deep(.el-textarea__inner) {
  border-radius: 16px !important;
  padding: 10px 14px !important;
  border: 1.5px solid rgba(167, 139, 250, 0.25);
  background: rgba(255, 255, 255, 0.72);
}
.home-message-form :deep(.el-input__inner),
.home-message-form :deep(.el-textarea__inner) {
  font-size: 1rem;
  line-height: 1.7;
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
}
:global([data-theme='dark']) .home-message-form :deep(.el-input__wrapper),
:global([data-theme='dark']) .home-message-form :deep(.el-textarea__inner) {
  background: rgba(15, 23, 42, 0.72);
  border-color: rgba(167, 139, 250, 0.35);
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
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
