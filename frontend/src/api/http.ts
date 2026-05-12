import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { clearToken, getToken } from '../utils/token'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
  timestamp?: number
}

function resolveApiBaseUrl(): string {
  const v = import.meta.env.VITE_API_BASE_URL as string | undefined
  if (v !== undefined && v !== '') {
    return v.trim()
  }
  return ''
}

export type PageResponse<T> = {
  items: T[]
  total: number
  page: number
  size: number
}

/** 短时间内同一 GET 请求去重：共享实际 HTTP 调用结果 */
const inflight = new Map<string, Promise<unknown>>()
const DEDUP_WINDOW_MS = 800

function dedupKey(config: { method?: string; url?: string; params?: unknown }): string | null {
  if (config.method?.toUpperCase() !== 'GET') return null
  const u = config.url || ''
  const p = config.params ? JSON.stringify(config.params) : ''
  return `${u}|${p}`
}

function scheduleDedupCleanup(key: string, promise: Promise<unknown>) {
  promise.finally(() => {
    setTimeout(() => {
      if (inflight.get(key) === promise) inflight.delete(key)
    }, DEDUP_WINDOW_MS)
  })
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

const _rawGet = http.get.bind(http)

http.get = function <T = unknown>(url: string, config?: Record<string, unknown>): Promise<T> {
  const key = dedupKey({ method: 'GET', url, params: config?.params })
  if (key) {
    const existing = inflight.get(key)
    if (existing) return existing as Promise<T>
    const promise = _rawGet(url, config) as unknown as Promise<T>
    inflight.set(key, promise as unknown as Promise<unknown>)
    scheduleDedupCleanup(key, promise as unknown as Promise<unknown>)
    return promise
  }
  return _rawGet(url, config) as unknown as Promise<T>
}

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
