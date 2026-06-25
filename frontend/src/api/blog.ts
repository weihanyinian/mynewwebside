import api from './index'

export interface Article {
  id: number
  title: string
  summary: string
  content: string
  coverImage: string | null
  status: 'DRAFT' | 'PUBLISHED'
  viewCount: number
  category: { id: number; name: string; slug: string } | null
  tags: { id: number; name: string }[]
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const blogApi = {
  getPublished(page = 0, size = 10, categoryId?: number) {
    const params: any = { page, size }
    if (categoryId) params.categoryId = categoryId
    return api.get<{ code: number; data: PageResponse<Article> }>('/articles', { params })
  },

  getById(id: number) {
    return api.get<{ code: number; data: Article }>(`/articles/${id}`)
  },

  getAll(page = 0, size = 10) {
    return api.get<{ code: number; data: PageResponse<Article> }>('/admin/articles', { params: { page, size } })
  },

  create(article: Partial<Article>) {
    return api.post<{ code: number; data: Article }>('/admin/articles', article)
  },

  update(id: number, article: Partial<Article>) {
    return api.put<{ code: number; data: Article }>(`/admin/articles/${id}`, article)
  },

  delete(id: number) {
    return api.delete(`/admin/articles/${id}`)
  },

  getCategories() {
    return api.get<{ code: number; data: { id: number; name: string; slug: string }[] }>('/categories')
  },

  getTags() {
    return api.get<{ code: number; data: { id: number; name: string }[] }>('/tags')
  }
}
