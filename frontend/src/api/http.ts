import axios, { type AxiosError } from 'axios'
import { clearToken, getToken } from '../utils/token'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
  /** 后端统一时间戳（毫秒），可选 */
  timestamp?: number
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => {
    const body = resp.data as ApiResponse<unknown>
    if (body && typeof body.code === 'number' && body.code !== 0) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return resp
  },
  (err: AxiosError<ApiResponse<unknown>>) => {
    const reqUrl = err.config?.url || ''
    if (
      err.response?.status === 401
      && !reqUrl.includes('/api/auth/login')
      && !reqUrl.includes('/api/auth/register')
    ) {
      clearToken()
      try {
        localStorage.removeItem('blog_user_profile')
      } catch {
        /* ignore */
      }
    }
    const data = err.response?.data
    if (data && typeof data === 'object' && typeof data.message === 'string' && data.message) {
      return Promise.reject(new Error(data.message))
    }
    if (err.message) return Promise.reject(err)
    return Promise.reject(new Error('网络异常'))
  },
)

