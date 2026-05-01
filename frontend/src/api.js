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
  (err) => {
    const msg = err.response?.data?.message
    if (msg && typeof msg === 'string') {
      return Promise.reject(new Error(msg))
    }
    return Promise.reject(err)
  },
)

export const ncmApi = {
  sendCaptcha(phone) {
    return api.post('/api/ncm/captcha/send', { phone })
  },
  /** @param {string} [countrycode] 默认 86，与 @neteasecloudmusicapienhanced/api login_cellphone 一致 */
  loginByPassword(phone, password, countrycode) {
    return api.post('/api/ncm/login/password', { phone, password, countrycode })
  },
  loginByCaptcha(phone, captcha, countrycode) {
    return api.post('/api/ncm/login/captcha', { phone, captcha, countrycode })
  },
  loginByCookie(cookie) {
    return api.post('/api/ncm/login/cookie', { cookie })
  },
  /** 网易云扫码登录三步：key → create（展示 qrimg）→ 轮询 check（803 为成功） */
  qrLoginKey() {
    return api.get('/api/ncm/login/qr/key', { params: { t: Date.now() } })
  },
  qrLoginCreate(key, qrimg = true) {
    return api.get('/api/ncm/login/qr/create', { params: { key, qrimg, t: Date.now() } })
  },
  qrLoginCheck(key) {
    return api.get('/api/ncm/login/qr/check', { params: { key, t: Date.now() } })
  },
  me() {
    return api.get('/api/ncm/me')
  },
  testLosslessUrl(songId) {
    return api.get('/api/ncm/test/lossless-url', { params: { songId } })
  },
}

export default api
