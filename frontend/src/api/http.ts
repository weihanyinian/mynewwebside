import axios, { type AxiosError } from 'axios'
import { clearToken, getToken } from '../utils/token'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
  /** 后端统一时间戳（毫秒），可选 */
  timestamp?: number
}

/** 生产构建若 VITE_API_BASE_URL 为空字符串，用同源相对路径（需 Nginx 反代 /api）；开发默认连本机后端 */
function resolveApiBaseUrl(): string {
  const v = import.meta.env.VITE_API_BASE_URL as string | undefined
  if (v === '' || v === undefined) {
    return import.meta.env.DEV ? 'http://localhost:8080' : ''
  }
  return v
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

