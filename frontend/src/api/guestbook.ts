import api from './index'

export interface GuestbookEntry {
  id: number
  nickname: string
  email: string | null
  content: string
  createdAt: string
}

export const guestbookApi = {
  getList() {
    return api.get<{ code: number; data: GuestbookEntry[] }>('/guestbook')
  },

  create(nickname: string, content: string, email?: string) {
    return api.post<{ code: number; data: GuestbookEntry }>('/guestbook', { nickname, content, email })
  },

  getAll(page = 0, size = 20) {
    return api.get<{ code: number; data: any }>('/admin/guestbooks', { params: { page, size } })
  },

  delete(id: number) {
    return api.delete(`/admin/guestbooks/${id}`)
  }
}
