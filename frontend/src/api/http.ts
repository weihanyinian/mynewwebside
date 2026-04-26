import axios, { type AxiosError } from 'axios'
import { clearToken, getToken } from '../utils/token'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
  /** 后端统一时间戳（毫秒），可选 */
  timestamp?: number
}

/**
 * - 未设置 VITE_API_BASE_URL：走相对路径 `/api`。
 *   - npm run dev：由 Vite 代理到 8080；经 Nginx :88 打开时由网关转发到 8080。
 *   - 生产：同源 Nginx 反代 /api。
 * - 仅当需要跨域直连后端时设置，例如 VITE_API_BASE_URL=http://127.0.0.1:8080
 */
function resolveApiBaseUrl(): string {
  const v = import.meta.env.VITE_API_BASE_URL as string | undefined
  if (v !== undefined && v !== '') {
    return v.trim()
  }
  return ''
}

export const http = axios.create({
  baseURL: resolveApiBaseUrl(),
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

