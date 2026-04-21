import { computed, ref, watch } from 'vue'
import { useWallStore } from '../stores/wall'

export function useWallMessages(pageSizeDefault = 5) {
  const wallStore = useWallStore()
  if (wallStore.pageSize !== pageSizeDefault) {
    wallStore.pageSize = pageSizeDefault
  }
  const nickname = ref('')
  const content = ref('')
  const submitting = ref(false)
  const canSubmit = computed(() => content.value.trim().length > 0)

  async function load() {
    await wallStore.load()
  }

  watch(
    () => wallStore.page,
    () => void load(),
  )

  async function submit() {
    if (!canSubmit.value) return false
    submitting.value = true
    try {
      await wallStore.submit(nickname.value, content.value)
      nickname.value = ''
      content.value = ''
      return true
    } finally {
      submitting.value = false
    }
  }

  function setPage(p: number) {
    wallStore.setPage(p - 1)
  }

  return {
    loading: computed(() => wallStore.loading),
    loadError: computed(() => wallStore.loadError),
    messages: computed(() => wallStore.messages),
    total: computed(() => wallStore.total),
    page: computed(() => wallStore.page),
    pageSize: computed(() => wallStore.pageSize),
    nickname,
    content,
    submitting,
    canSubmit,
    load,
    submit,
    setPage,
  }
}
