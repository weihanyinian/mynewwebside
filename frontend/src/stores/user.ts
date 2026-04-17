import { defineStore } from 'pinia'
import { getToken, setToken, clearToken } from '../utils/token'
import { http, type ApiResponse } from '../api/http'

const PROFILE_KEY = 'blog_user_profile'

export type UserProfile = {
  id?: number
  username: string
  nickname: string
  admin: boolean
}

export type LoginPayload = {
  token: string
  username: string
  nickname: string
  admin: boolean
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '' as string,
    profile: null as UserProfile | null,
    hydrated: false,
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    isAdmin: (s) => !!s.profile?.admin,
  },
  actions: {
    hydrateFromStorage() {
      this.token = getToken()
      const raw = localStorage.getItem(PROFILE_KEY)
      if (!raw) {
        this.profile = null
      } else {
        try {
          this.profile = JSON.parse(raw) as UserProfile
        } catch {
          this.profile = null
        }
      }
      this.hydrated = true
    },
    setSession(p: LoginPayload) {
      setToken(p.token)
      this.token = p.token
      this.profile = { username: p.username, nickname: p.nickname, admin: p.admin }
      localStorage.setItem(PROFILE_KEY, JSON.stringify(this.profile))
    },
    logout() {
      clearToken()
      this.token = ''
      this.profile = null
      localStorage.removeItem(PROFILE_KEY)
    },
    async fetchMe() {
      const resp = await http.get<ApiResponse<UserProfile & { id: number }>>('/api/auth/me')
      const d = resp.data.data
      this.profile = { id: d.id, username: d.username, nickname: d.nickname, admin: d.admin }
      localStorage.setItem(PROFILE_KEY, JSON.stringify(this.profile))
    },
  },
})
