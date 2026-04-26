<script setup>
import Card from "./Card.vue"

defineProps({
  bottomCards: { type: Array, default: () => [] },
  revealBottom: { type: Boolean, default: false },
  tablePlays: { type: Array, default: () => [] },
  trickOwner: { type: Number, default: null },
})
</script>

<template>
  <div class="rounded-2xl border border-white/20 bg-emerald-900/30 p-3 shadow-inner">
    <div class="mb-3">
      <p class="mb-1 text-center text-xs text-white/70">底牌</p>
      <div class="flex justify-center gap-2">
        <Card v-for="c in bottomCards" :key="c.id" :card="c" :face-down="!revealBottom" />
      </div>
    </div>

    <div class="grid grid-cols-1 gap-3 md:grid-cols-3">
      <div
        v-for="row in tablePlays"
        :key="row.playerIndex"
        :class="[
          'rounded-lg border p-2 min-h-28 transition-all',
          trickOwner === row.playerIndex ? 'border-cyan-300 bg-cyan-100/15' : 'border-white/20 bg-black/20',
        ]"
      >
        <div class="mb-2 flex items-center justify-between text-xs text-white/80">
          <span>{{ row.name }}</span>
          <span v-if="row.isPass" class="text-white/60">不出</span>
        </div>
        <TransitionGroup name="play" tag="div" class="flex flex-wrap gap-1">
          <Card v-for="c in row.cards" :key="c.id" :card="c" :small="true" />
        </TransitionGroup>
      </div>
    </div>
  </div>
</template>

