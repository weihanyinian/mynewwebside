import { http, type ApiResponse } from './http'

export type FriendLink = {
  id: number
  title: string
  url: string
  description: string
  avatarUrl: string
}

export async function fetchPublicFriends(): Promise<FriendLink[]> {
  const res = await http.get<ApiResponse<FriendLink[]>>('/api/public/friends')
  return res.data.data ?? []
}

export type FriendLinkAdmin = {
  id: number
  title: string
  url: string
  description?: string
  avatarUrl?: string
  sortOrder: number
  createdAt?: string
}

export async function adminListFriends() {
  const res = await http.get<ApiResponse<FriendLinkAdmin[]>>('/api/admin/friends')
  return res.data.data ?? []
}

export async function adminCreateFriend(body: {
  title: string
  url: string
  description?: string
  avatarUrl?: string
  sortOrder: number
}) {
  const res = await http.post<ApiResponse<FriendLinkAdmin>>('/api/admin/friends', body)
  return res.data.data!
}

export async function adminUpdateFriend(
  id: number,
  body: { title: string; url: string; description?: string; avatarUrl?: string; sortOrder: number },
) {
  const res = await http.put<ApiResponse<FriendLinkAdmin>>(`/api/admin/friends/${id}`, body)
  return res.data.data!
}

export async function adminDeleteFriend(id: number) {
  await http.delete<ApiResponse<void>>(`/api/admin/friends/${id}`)
}
