import type { PageResponse } from './blog'
import type { ApiResponse } from './http'
import { http } from './http'

export type WallMessagePublic = {
  id: number
  nickname: string
  content: string
  adminReply: string | null
  createdAt: string
}

export type WallSubmitResult = {
  id: number
  reviewHint: string
}

export async function fetchWallMessages() {
  const res = await http.get<ApiResponse<WallMessagePublic[]>>('/api/public/wall/messages')
  return res.data.data
}

export async function submitWallMessage(nickname: string | undefined, content: string) {
  const res = await http.post<ApiResponse<WallSubmitResult>>('/api/public/wall/messages', {
    nickname: nickname?.trim() || undefined,
    content: content.trim(),
  })
  return res.data.data
}

export type WallMessageAdmin = {
  id: number
  nickname: string
  content: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  adminReply: string | null
  createdAt: string
  reviewedAt: string | null
}

export async function adminPageWallMessages(params?: {
  status?: string
  page?: number
  size?: number
}) {
  const res = await http.get<ApiResponse<PageResponse<WallMessageAdmin>>>('/api/admin/wall/messages', {
    params,
  })
  return res.data.data
}

export async function adminApproveWall(id: number) {
  await http.patch(`/api/admin/wall/messages/${id}/approve`)
}

export async function adminRejectWall(id: number) {
  await http.patch(`/api/admin/wall/messages/${id}/reject`)
}

export async function adminReplyWall(id: number, reply: string) {
  await http.patch(`/api/admin/wall/messages/${id}/reply`, { reply })
}

export async function adminDeleteWall(id: number) {
  await http.delete(`/api/admin/wall/messages/${id}`)
}
