<script setup>
defineProps({
  stage: { type: String, required: true },
  canPlay: { type: Boolean, default: false },
  canPass: { type: Boolean, default: false },
  canHint: { type: Boolean, default: false },
  canBid: { type: Boolean, default: false },
  canSkipBid: { type: Boolean, default: false },
  thinking: { type: Boolean, default: false },
})

const emit = defineEmits(["play", "pass", "hint", "restart", "bid", "skip-bid"])
</script>

<template>
  <div class="rounded-xl border border-white/20 bg-black/25 p-3">
    <div class="grid grid-cols-2 gap-2 md:grid-cols-4">
      <template v-if="stage === 'bidding'">
        <button
          type="button"
          class="rounded-lg bg-amber-400 px-3 py-2 text-sm font-semibold text-slate-900 disabled:cursor-not-allowed disabled:opacity-45"
          :disabled="!canBid || thinking"
          @click="emit('bid')"
        >
          叫地主
        </button>
        <button
          type="button"
          class="rounded-lg bg-slate-300 px-3 py-2 text-sm font-semibold text-slate-800 disabled:cursor-not-allowed disabled:opacity-45"
          :disabled="!canSkipBid || thinking"
          @click="emit('skip-bid')"
        >
          不叫
        </button>
      </template>
      <template v-else-if="stage === 'playing'">
        <button
          type="button"
          class="rounded-lg bg-cyan-400 px-3 py-2 text-sm font-semibold text-slate-900 disabled:cursor-not-allowed disabled:opacity-45"
          :disabled="!canPlay || thinking"
          @click="emit('play')"
        >
          出牌
        </button>
        <button
          type="button"
          class="rounded-lg bg-slate-300 px-3 py-2 text-sm font-semibold text-slate-800 disabled:cursor-not-allowed disabled:opacity-45"
          :disabled="!canPass || thinking"
          @click="emit('pass')"
        >
          不出
        </button>
        <button
          type="button"
          class="rounded-lg bg-violet-400 px-3 py-2 text-sm font-semibold text-slate-900 disabled:cursor-not-allowed disabled:opacity-45"
          :disabled="!canHint || thinking"
          @click="emit('hint')"
        >
          提示
        </button>
      </template>

      <button
        type="button"
        class="rounded-lg bg-emerald-400 px-3 py-2 text-sm font-semibold text-slate-900"
        @click="emit('restart')"
      >
        重新开始
      </button>
    </div>
  </div>
</template>

