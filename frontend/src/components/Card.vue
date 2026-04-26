<script setup>
import { computed } from "vue"
import { formatCardText } from "../utils/cardUtils"

const props = defineProps({
  card: { type: Object, required: true },
  faceDown: { type: Boolean, default: false },
  selected: { type: Boolean, default: false },
  clickable: { type: Boolean, default: false },
  small: { type: Boolean, default: false },
})

const emit = defineEmits(["toggle"])

const colorClass = computed(() => {
  if (props.faceDown) return "text-white"
  return props.card.suit === "heart" || props.card.suit === "diamond" ? "text-red-600" : "text-slate-900"
})

const sizeClass = computed(() =>
  props.small ? "w-10 h-14 text-[10px] rounded-md" : "w-14 h-20 text-xs rounded-lg",
)

function onClick() {
  if (!props.clickable) return
  emit("toggle", props.card)
}
</script>

<template>
  <button
    type="button"
    :disabled="!clickable"
    :class="[
      'relative border shadow-md transition-all duration-200 select-none',
      sizeClass,
      selected ? '-translate-y-2 border-amber-300 ring-2 ring-amber-300/70' : 'border-slate-300 hover:-translate-y-1',
      faceDown
        ? 'bg-gradient-to-br from-indigo-700 to-cyan-700 border-cyan-200/60'
        : 'bg-white',
      clickable ? 'cursor-pointer' : 'cursor-default',
      colorClass,
    ]"
    @click="onClick"
  >
    <template v-if="faceDown">
      <span class="absolute inset-0 m-auto h-5 w-5 rounded-full border border-white/60 bg-white/10" />
    </template>
    <template v-else>
      <span class="absolute left-1 top-1 font-semibold leading-none">{{ formatCardText(card) }}</span>
      <span class="absolute bottom-1 right-1 rotate-180 font-semibold leading-none">{{ formatCardText(card) }}</span>
    </template>
  </button>
</template>

