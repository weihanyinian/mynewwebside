import { http, type ApiResponse } from './http'

export type MindMapListItem = {
  id: number
  title: string
  updatedAt: string
}

export type MindMapDetail = {
  id: number
  title: string
  data: string
  updatedAt: string
}

export async function fetchMindMapList(): Promise<MindMapListItem[]> {
  const res = await http.get<ApiResponse<MindMapListItem[]>>('/api/mind-maps')
  return res.data.data!
}

export async function fetchMindMap(id: number): Promise<MindMapDetail> {
  const res = await http.get<ApiResponse<MindMapDetail>>(`/api/mind-maps/${id}`)
  return res.data.data!
}

export async function createBlankMindMap(title?: string): Promise<MindMapDetail> {
  const res = await http.post<ApiResponse<MindMapDetail>>(
    '/api/mind-maps/blank',
    {},
    { params: title ? { title } : {} },
  )
  return res.data.data!
}

export async function patchMindMap(id: number, body: { title?: string; data?: string }): Promise<MindMapDetail> {
  const res = await http.patch<ApiResponse<MindMapDetail>>(`/api/mind-maps/${id}`, body)
  return res.data.data!
}

export async function deleteMindMap(id: number): Promise<void> {
  await http.delete<ApiResponse<void>>(`/api/mind-maps/${id}`)
}
