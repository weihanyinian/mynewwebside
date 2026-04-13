import { http, type ApiResponse } from './http'

export type Category = { id: number; name: string }
export type Tag = { id: number; name: string }

export type ArticleListItem = {
  id: number
  title: string
  summary: string
  coverUrl?: string | null
  views: number
  publishedAt?: string | null
  category?: Category | null
  tags: Tag[]
}

export type ArticleDetail = ArticleListItem & {
  status?: 'DRAFT' | 'PUBLISHED'
  contentMd: string
}

export type PageResponse<T> = {
  items: T[]
  total: number
  page: number
  size: number
}

export type AdminArticleListItem = ArticleListItem & {
  status: 'DRAFT' | 'PUBLISHED'
  createdAt: string
  updatedAt: string
}

export async function login(username: string, password: string) {
  const resp = await http.post<ApiResponse<{ token: string }>>('/api/auth/login', { username, password })
  return resp.data.data.token
}

export async function getPublicArticles(params: {
  keyword?: string
  categoryId?: number
  tagId?: number
  page?: number
  size?: number
}) {
  const resp = await http.get<ApiResponse<PageResponse<ArticleListItem>>>('/api/public/articles', { params })
  return resp.data.data
}

export async function getPublicArticle(id: number) {
  const resp = await http.get<ApiResponse<ArticleDetail>>(`/api/public/articles/${id}`)
  return resp.data.data
}

export async function getCategories(publicOnly = true) {
  const url = publicOnly ? '/api/public/categories' : '/api/admin/categories'
  const resp = await http.get<ApiResponse<Category[]>>(url)
  return resp.data.data
}

export async function getTags(publicOnly = true) {
  const url = publicOnly ? '/api/public/tags' : '/api/admin/tags'
  const resp = await http.get<ApiResponse<Tag[]>>(url)
  return resp.data.data
}

export async function adminPageArticles(params: { keyword?: string; status?: string; page?: number; size?: number }) {
  const resp = await http.get<ApiResponse<PageResponse<AdminArticleListItem>>>('/api/admin/articles', { params })
  return resp.data.data
}

export async function adminGetArticle(id: number) {
  const resp = await http.get<ApiResponse<ArticleDetail>>(`/api/admin/articles/${id}`)
  return resp.data.data
}

export type ArticleUpsert = {
  title: string
  summary: string
  contentMd: string
  coverUrl?: string
  status: 'DRAFT' | 'PUBLISHED'
  categoryId?: number | null
  tagIds?: number[]
}

export async function adminCreateArticle(payload: ArticleUpsert) {
  const resp = await http.post<ApiResponse<ArticleDetail>>('/api/admin/articles', payload)
  return resp.data.data
}

export async function adminUpdateArticle(id: number, payload: ArticleUpsert) {
  const resp = await http.put<ApiResponse<ArticleDetail>>(`/api/admin/articles/${id}`, payload)
  return resp.data.data
}

export async function adminDeleteArticle(id: number) {
  await http.delete<ApiResponse<void>>(`/api/admin/articles/${id}`)
}

export async function adminCreateCategory(name: string) {
  const resp = await http.post<ApiResponse<Category>>('/api/admin/categories', { name })
  return resp.data.data
}

export async function adminUpdateCategory(id: number, name: string) {
  const resp = await http.put<ApiResponse<Category>>(`/api/admin/categories/${id}`, { name })
  return resp.data.data
}

export async function adminDeleteCategory(id: number) {
  await http.delete<ApiResponse<void>>(`/api/admin/categories/${id}`)
}

export async function adminCreateTag(name: string) {
  const resp = await http.post<ApiResponse<Tag>>('/api/admin/tags', { name })
  return resp.data.data
}

export async function adminUpdateTag(id: number, name: string) {
  const resp = await http.put<ApiResponse<Tag>>(`/api/admin/tags/${id}`, { name })
  return resp.data.data
}

export async function adminDeleteTag(id: number) {
  await http.delete<ApiResponse<void>>(`/api/admin/tags/${id}`)
}
