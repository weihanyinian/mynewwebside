import { http, type ApiResponse } from './http'
import type { HomeWorkItem } from '../types/home'

type PortfolioWorkDto = {
  id: number
  title: string
  desc: string
  detail: string
  tag: string
  link: string
  cover: string
}

export async function fetchPortfolioWorks(): Promise<HomeWorkItem[]> {
  const { data } = await http.get<ApiResponse<PortfolioWorkDto[]>>('/api/public/portfolio/works')
  return data.data.map((item) => ({
    id: item.id,
    title: item.title,
    desc: item.desc,
    detail: item.detail,
    tag: item.tag,
    link: item.link,
    cover: item.cover,
  }))
}

export type PortfolioWorkAdmin = PortfolioWorkDto & {
  enabled: boolean
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export type PortfolioWorkUpsertPayload = {
  title: string
  desc: string
  detail: string
  tag: string
  link: string
  cover: string
  enabled: boolean
  sortOrder: number
}

export async function adminListPortfolioWorks() {
  const { data } = await http.get<ApiResponse<PortfolioWorkAdmin[]>>('/api/admin/portfolio/works')
  return data.data
}

export async function adminCreatePortfolioWork(payload: PortfolioWorkUpsertPayload) {
  const { data } = await http.post<ApiResponse<PortfolioWorkAdmin>>('/api/admin/portfolio/works', payload)
  return data.data
}

export async function adminUpdatePortfolioWork(id: number, payload: PortfolioWorkUpsertPayload) {
  const { data } = await http.put<ApiResponse<PortfolioWorkAdmin>>(`/api/admin/portfolio/works/${id}`, payload)
  return data.data
}

export async function adminDeletePortfolioWork(id: number) {
  await http.delete<ApiResponse<void>>(`/api/admin/portfolio/works/${id}`)
}
