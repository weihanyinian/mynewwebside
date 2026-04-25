import { defineStore } from 'pinia'
import { ncmApi } from './api'

const COOKIE_KEY = 'ncm_raw_cookie'

export const useNcmUserStore = defineStore('ncmUser', {
  state: () => ({
    loginType: '',
    rawCookie: '',
    profile: null,
    loggedIn: false,
    loading: false,
  }),
  actions: {
    hydrate() {
      this.rawCookie = localStorage.getItem(COOKIE_KEY) || ''
    },
    setCookie(cookie) {
      this.rawCookie = cookie || ''
      if (this.rawCookie) {
        localStorage.setItem(COOKIE_KEY, this.rawCookie)
      } else {
        localStorage.removeItem(COOKIE_KEY)
      }
    },
    async refreshProfile() {
      const resp = await ncmApi.me()
      this.profile = resp.data.data
      this.loggedIn = true
      return this.profile
    },
    async loginWithPassword(phone, password) {
      this.loading = true
      try {
        const resp = await ncmApi.loginByPassword(phone, password)
        this.loginType = 'password'
        this.loggedIn = true
        return resp.data.data
      } finally {
        this.loading = false
      }
    },
    async loginWithCaptcha(phone, captcha) {
      this.loading = true
      try {
        const resp = await ncmApi.loginByCaptcha(phone, captcha)
        this.loginType = 'captcha'
        this.loggedIn = true
        return resp.data.data
      } finally {
        this.loading = false
      }
    },
    async loginWithCookie(cookie) {
      this.loading = true
      try {
        this.setCookie(cookie)
        const resp = await ncmApi.loginByCookie(cookie)
        this.loginType = 'cookie'
        this.loggedIn = true
        return resp.data.data
      } finally {
        this.loading = false
      }
    },
    logoutLocal() {
      this.loggedIn = false
      this.profile = null
      this.loginType = ''
      this.setCookie('')
    },
  },
})
