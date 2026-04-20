import { computed, ref, watch } from 'vue'
import { fetchWallMessages, submitWallMessage, type WallMessagePublic } from '../api/wall'

export function useWallMessages(pageSizeDefault = 5) {
  const loading = ref(true)
  const loadError = ref(false)
  const messages = ref<WallMessagePublic[]>([])
  const total = ref(0)
  const page = ref(0)
  const pageSize = ref(pageSizeDefault)
  const nickname = ref('')
  const content = ref('')
  const submitting = ref(false)
  const canSubmit = computed(() => content.value.trim().length > 0)

  async function load() {
    loading.value = true
    loadError.value = false
    try {
      const res = await fetchWallMessages({ page: page.value, size: pageSize.value })
      messages.value = res.items
      total.value = res.total
    } catch {
      loadError.value = true
      messages.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  watch(page, () => void load())

  async function submit() {
    if (!canSubmit.value) return false
    submitting.value = true
    try {
      const created = await submitWallMessage(nickname.value, content.value)
      nickname.value = ''
      content.value = ''

      if (page.value === 0) {
        messages.value = [
          {
            id: created.id,
            nickname: created.nickname,
            content: created.content,
            adminReply: created.adminReply,
            createdAt: created.createdAt,
          },
          ...messages.value,
        ].slice(0, pageSize.value)
        total.value += 1
      } else {
        page.value = 0
        await load()
      }
      return true
    } finally {
      submitting.value = false
    }
  }

  function setPage(p: number) {
    page.value = p - 1
  }

  return {
    loading,
    loadError,
    messages,
    total,
    page,
    pageSize,
    nickname,
    content,
    submitting,
    canSubmit,
    load,
    submit,
    setPage,
  }
}
