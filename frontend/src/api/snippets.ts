import { http, type ApiResponse } from './http'

export type CodeSnippet = {
  id: number
  title: string
  language: string
  content: string
  updatedAt: string
}

export async function listSnippets() {
  const res = await http.get<ApiResponse<CodeSnippet[]>>('/api/snippets')
  return res.data.data ?? []
}

export async function getSnippet(id: number) {
  const res = await http.get<ApiResponse<CodeSnippet>>(`/api/snippets/${id}`)
  return res.data.data!
}

export async function createSnippet(body: { title: string; language: string; content: string }) {
  const res = await http.post<ApiResponse<CodeSnippet>>('/api/snippets', body)
  return res.data.data!
}

export async function updateSnippet(id: number, body: { title: string; language: string; content: string }) {
  const res = await http.put<ApiResponse<CodeSnippet>>(`/api/snippets/${id}`, body)
  return res.data.data!
}

export async function deleteSnippet(id: number) {
  await http.delete<ApiResponse<void>>(`/api/snippets/${id}`)
}
