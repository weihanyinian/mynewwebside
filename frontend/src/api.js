import axios from 'axios'
import { useNcmUserStore } from './user'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  withCredentials: true, // 让浏览器自动携带后端 Session Cookie
})

api.interceptors.request.use((config) => {
  const store = useNcmUserStore()
  if (store.rawCookie) {
    config.headers = config.headers || {}
    // 浏览器不能主动写 Cookie 头，这里用自定义头兜底给后端读取（可选）
    config.headers['X-Ncm-Cookie'] = store.rawCookie
  }
  return config
})

api.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return resp
  },
  (err) => Promise.reject(err),
)

export const ncmApi = {
  sendCaptcha(phone) {
    return api.post('/api/ncm/captcha/send', { phone })
  },
  loginByPassword(phone, password) {
    return api.post('/api/ncm/login/password', { phone, password })
  },
  loginByCaptcha(phone, captcha) {
    return api.post('/api/ncm/login/captcha', { phone, captcha })
  },
  loginByCookie(cookie) {
    return api.post('/api/ncm/login/cookie', { cookie })
  },
  me() {
    return api.get('/api/ncm/me')
  },
  testLosslessUrl(songId) {
    return api.get('/api/ncm/test/lossless-url', { params: { songId } })
  },
}

export default api
