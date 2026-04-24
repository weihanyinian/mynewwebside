<script setup lang="ts">
import { useRouter } from 'vue-router'

withDefaults(
  defineProps<{
    /** 顶栏等窄行布局 */
    compact?: boolean
    /** 弹窗内全宽居中 */
    block?: boolean
  }>(),
  { compact: false, block: false },
)

const router = useRouter()

function go() {
  void router.push('/moyu')
}
</script>

<template>
  <button
    type="button"
    class="moyu-back-hub"
    :class="{ 'moyu-back-hub--compact': compact, 'moyu-back-hub--block': block }"
    @click="go"
  >
    <span class="moyu-back-hub__arrow" aria-hidden="true">←</span>
    <span class="moyu-back-hub__label">摸鱼中心</span>
  </button>
</template>

<style scoped>
.moyu-back-hub {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.35em;
  margin: 0;
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #fff;
  padding: 12px 20px;
  border-radius: 9999px;
  background: linear-gradient(120deg, #6d28d9 0%, #8b5cf6 38%, #a78bfa 72%, #c4b5fd 100%);
  box-shadow:
    0 6px 22px rgba(109, 40, 217, 0.38),
    0 2px 8px rgba(15, 23, 42, 0.12);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    filter 0.2s ease;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

.moyu-back-hub:hover {
  filter: brightness(1.07);
  box-shadow:
    0 8px 28px rgba(109, 40, 217, 0.45),
    0 3px 10px rgba(15, 23, 42, 0.14);
}

.moyu-back-hub:active {
  transform: scale(0.98);
}

.moyu-back-hub--compact {
  min-height: 44px;
  padding: 10px 16px;
  font-size: 0.88rem;
}

.moyu-back-hub--block {
  width: 100%;
}

.moyu-back-hub__arrow {
  font-size: 1.05em;
  line-height: 1;
}

.moyu-back-hub__label {
  line-height: 1.2;
}
</style>
