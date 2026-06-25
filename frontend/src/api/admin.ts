import api from './index'

export interface DashboardStats {
  articleCount: number
  guestbookCount: number
  commentCount: number
  todayVisits: number
  weekVisits: number
  totalVisits: number
  topPaths: { path: string; count: number }[]
  topIps: { ip: string; count: number }[]
  dailyVisits: { date: string; count: number }[]
}

export interface VisitorLog {
  id: number
  ip: string
  userAgent: string
  referer: string
  path: string
  method: string
  visitTime: string
}

export const adminApi = {
  login(username: string, password: string) {
    return api.post<{ code: number; data: { token: string; username: string; role: string } }>('/admin/login', { username, password })
  },

  getDashboard() {
    return api.get<{ code: number; data: DashboardStats }>('/admin/dashboard')
  },

  getVisitors(page = 0, size = 20) {
    return api.get<{ code: number; data: any }>('/admin/visitors', { params: { page, size } })
  },

  getVisitorStats() {
    return api.get<{ code: number; data: DashboardStats }>('/admin/visitors/stats')
  }
}
