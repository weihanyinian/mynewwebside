import { http, type ApiResponse } from './http'
import type { ProblemListItem } from './oj'
import type { OjSubmissionPage } from './oj'

export type AdminUserRow = {
  id: number
  username: string
  nickname: string
  createdAt: string
}

export async function adminListUsers() {
  const res = await http.get<ApiResponse<AdminUserRow[]>>('/api/admin/users')
  return res.data.data
}

export async function adminDeleteUser(id: number) {
  await http.delete<ApiResponse<unknown>>(`/api/admin/users/${id}`)
}

export async function adminListOjProblems(params?: { q?: string; difficulty?: string }) {
  const res = await http.get<ApiResponse<ProblemListItem[]>>('/api/admin/oj/problems', { params })
  return res.data.data
}

export async function adminListOjSubmissions(params?: { page?: number; size?: number }) {
  const res = await http.get<ApiResponse<OjSubmissionPage>>('/api/admin/oj/submissions', { params })
  return res.data.data
}
