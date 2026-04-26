<script setup>
import { computed } from "vue"
import Card from "./Card.vue"
import { sortCards } from "../utils/cardUtils"

const props = defineProps({
  name: { type: String, required: true },
  cards: { type: Array, default: () => [] },
  isSelf: { type: Boolean, default: false },
  selectedIds: { type: Array, default: () => [] },
  isLandlord: { type: Boolean, default: false },
  active: { type: Boolean, default: false },
})

const emit = defineEmits(["toggle-card"])

const sortedCards = computed(() => sortCards(props.cards))

function toggle(card) {
  emit("toggle-card", card)
}
</script>

<template>
  <div :class="['rounded-xl border px-3 py-2 shadow-sm', active ? 'border-amber-300 bg-amber-100/25' : 'border-white/20 bg-black/20']">
    <div class="mb-2 flex items-center justify-between text-xs text-white/90">
      <div class="flex items-center gap-2">
        <span class="font-semibold">{{ name }}</span>
        <span v-if="isLandlord" class="rounded bg-amber-400/90 px-1.5 py-0.5 text-[10px] font-bold text-slate-900">地主</span>
      </div>
      <span>{{ cards.length }} 张</span>
    </div>

    <div v-if="isSelf" class="overflow-x-auto pb-1">
      <div class="flex min-w-max items-end gap-1">
        <Card
          v-for="c in sortedCards"
          :key="c.id"
          :card="c"
          :selected="selectedIds.includes(c.id)"
          :clickable="true"
          @toggle="toggle"
        />
      </div>
    </div>

    <div v-else class="flex flex-wrap gap-1">
      <Card v-for="c in cards" :key="c.id" :card="c" :face-down="true" :small="true" />
    </div>
  </div>
</template>

