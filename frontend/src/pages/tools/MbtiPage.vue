<script setup lang="ts">
import { ref } from 'vue'
import GlassCard from '../../components/GlassCard.vue'

interface Question {
  q: string
  dimension: string // EI, SN, TF, JP
  positive: boolean // true means agree = E/S/T/J side
}

const questions: Question[] = [
  { q: '在聚会中，你更喜欢与许多人交流而不是只和少数几人交谈', dimension: 'EI', positive: true },
  { q: '你更喜欢关注具体事实和细节，而非抽象概念', dimension: 'SN', positive: true },
  { q: '做决定时，你更依赖逻辑分析而非个人感受', dimension: 'TF', positive: true },
  { q: '你更喜欢有计划、有条理的工作方式', dimension: 'JP', positive: true },
  { q: '你很容易结识新朋友', dimension: 'EI', positive: true },
  { q: '你更喜欢实践和动手操作，而非理论思考', dimension: 'SN', positive: true },
  { q: '面对批评时，你会更关注其中的逻辑而非情感', dimension: 'TF', positive: true },
  { q: '截止日期让你更有动力完成任务', dimension: 'JP', positive: true },
]

const currentQ = ref(0)
const scores = ref<Record<string, number>>({ EI: 0, SN: 0, TF: 0, JP: 0 })
const result = ref('')

function answer(agree: boolean) {
  const q = questions[currentQ.value]
  scores.value[q.dimension] += (agree === q.positive) ? 1 : -1
  if (currentQ.value < questions.length - 1) {
    currentQ.value++
  } else {
    calcResult()
  }
}

function calcResult() {
  result.value = ''
  result.value += scores.value.EI >= 0 ? 'E' : 'I'
  result.value += scores.value.SN >= 0 ? 'S' : 'N'
  result.value += scores.value.TF >= 0 ? 'T' : 'F'
  result.value += scores.value.JP >= 0 ? 'J' : 'P'
}

function restart() {
  currentQ.value = 0
  scores.value = { EI: 0, SN: 0, TF: 0, JP: 0 }
  result.value = ''
}

const q = () => questions[currentQ.value]
</script>

<template>
  <div class="page-container max-w-xl">
    <button @click="$router.push('/tools')" class="glass-button text-sm mb-8">← 返回工具箱</button>
    <h1 class="text-3xl font-bold mb-6 gradient-text text-center">🧬 MBTI 简易测试</h1>

    <GlassCard v-if="!result">
      <div class="text-xs text-[var(--text-secondary)] mb-4 text-center">
        {{ currentQ + 1 }} / {{ questions.length }}
      </div>
      <div class="w-full bg-[var(--glass-bg)] rounded-full h-1.5 mb-6">
        <div class="h-full rounded-full bg-gradient-to-r from-[#62a7ea] to-[#a58eea] transition-all"
          :style="{ width: ((currentQ) / questions.length * 100) + '%' }" />
      </div>

      <p class="text-lg mb-8 text-center leading-relaxed">{{ q().q }}</p>

      <div class="flex justify-center gap-4">
        <button @click="answer(true)" class="glass-button primary px-8">✓ 同意</button>
        <button @click="answer(false)" class="glass-button px-8">✗ 不同意</button>
      </div>
    </GlassCard>

    <GlassCard v-else class="text-center">
      <div class="text-5xl mb-4">🧬</div>
      <p class="text-[var(--text-secondary)] mb-4">你的 MBTI 类型是：</p>
      <p class="text-4xl font-bold gradient-text mb-6">{{ result }}</p>
      <p class="text-xs text-[var(--text-secondary)] italic mb-6">
        * 简易测试仅供娱乐，完整测试请访问 16personalities.com
      </p>
      <button @click="restart" class="glass-button text-sm">重新测试</button>
    </GlassCard>
  </div>
</template>
