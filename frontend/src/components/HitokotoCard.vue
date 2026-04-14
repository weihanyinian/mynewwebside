<script setup lang="ts">
import { ref, onMounted } from 'vue'

const quotes = [
  '即使是微小的光芒，也能照亮前行的路。✨',
  '代码是写给机器的诗，也是写给自己的信。📝',
  '不积跬步，无以至千里。🏃‍♂️',
  '你所热爱的，就是你的生活。❤️',
  'Bug 不可怕，可怕的是放弃寻找答案的心。🐛',
  '保持热爱，奔赴山海。🌊',
  '二次元的世界里，一切皆有可能。🌌'
]

const currentQuote = ref('')
const isLoading = ref(false)

function fetchQuote() {
  isLoading.value = true
  setTimeout(() => {
    currentQuote.value = quotes[Math.floor(Math.random() * quotes.length)]
    isLoading.value = false
  }, 300) // Simulate network delay for animation
}

onMounted(() => {
  fetchQuote()
})
</script>

<template>
  <div class="hitokoto-card glass-ui">
    <div class="quote-content" :class="{ 'is-loading': isLoading }">
      <span class="quote-mark left">“</span>
      <p class="text">{{ currentQuote }}</p>
      <span class="quote-mark right">”</span>
    </div>
    <div class="actions">
      <button class="refresh-btn" @click="fetchQuote" :disabled="isLoading">
        <span class="icon" :class="{ 'spin': isLoading }">🔄</span> 换一句
      </button>
    </div>
  </div>
</template>

<style scoped>
.hitokoto-card {
  padding: 30px;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin: 40px auto;
  max-width: 600px;
  position: relative;
  overflow: hidden;
}

.glass-ui {
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.1) 0%, rgba(252, 162, 229, 0.1) 100%);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.15);
  color: #fff;
  transition: all 0.3s ease;
}

.hitokoto-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(252, 162, 229, 0.2);
}

.quote-content {
  position: relative;
  text-align: center;
  transition: opacity 0.3s;
  padding: 0 30px;
}
.quote-content.is-loading {
  opacity: 0.5;
}

.text {
  font-size: 1.2rem;
  font-weight: 600;
  line-height: 1.6;
  letter-spacing: 1px;
  color: #e0f7fa;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  margin: 0;
}

.quote-mark {
  position: absolute;
  font-size: 4rem;
  font-family: Georgia, serif;
  color: rgba(252, 162, 229, 0.3);
  line-height: 1;
}
.quote-mark.left {
  top: -20px;
  left: -10px;
}
.quote-mark.right {
  bottom: -40px;
  right: -10px;
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}
.refresh-btn:hover {
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.4), rgba(252, 162, 229, 0.4));
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 12px rgba(102, 217, 255, 0.3);
}

.icon {
  display: inline-block;
}
.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .hitokoto-card {
    margin: 20px;
    padding: 20px;
  }
  .text {
    font-size: 1rem;
  }
  .quote-mark {
    font-size: 3rem;
  }
}
</style>