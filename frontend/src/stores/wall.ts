import { defineStore } from 'pinia'
import type { WallMessagePublic } from '../api/wall'
import { fetchWallMessages, submitWallMessage } from '../api/wall'

export const useWallStore = defineStore('wall', {
  state: () => ({
    loading: false,
    loadError: false,
    messages: [] as WallMessagePublic[],
    total: 0,
    page: 0,
    pageSize: 5,
  }),
  actions: {
    async load() {
      this.loading = true
      this.loadError = false
      try {
        const res = await fetchWallMessages({ page: this.page, size: this.pageSize })
        this.messages = res.items
        this.total = res.total
      } catch {
        this.loadError = true
        this.messages = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    async submit(nickname: string, content: string) {
      const created = await submitWallMessage(nickname, content)
      if (this.page === 0) {
        this.messages = [
          {
            id: created.id,
            nickname: created.nickname,
            content: created.content,
            adminReply: created.adminReply,
            createdAt: created.createdAt,
          },
          ...this.messages,
        ].slice(0, this.pageSize)
        this.total += 1
      } else {
        this.page = 0
        await this.load()
      }
      return created
    },
    setPage(p: number) {
      this.page = p
    },
  },
})
