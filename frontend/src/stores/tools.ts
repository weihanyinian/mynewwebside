import { defineStore } from 'pinia'

export type ToolCardItem = {
  id: string
  path: string
  icon: string
  titleKey: string
  descKey: string
}

const FAVORITES_KEY = 'tools_favorites'
const RECENT_KEY = 'tools_recent'
const RECENT_LIMIT = 6

const toolsSeed: ToolCardItem[] = [
  { id: 'reaction', path: '/tools/reaction', icon: 'reaction', titleKey: 'tools.reaction', descKey: 'toolsHub.cardReactionDesc' },
  { id: 'cps', path: '/tools/cps', icon: 'cps', titleKey: 'tools.cps', descKey: 'toolsHub.cardCpsDesc' },
  { id: 'pomodoro', path: '/tools/pomodoro', icon: 'pomodoro', titleKey: 'tools.pomodoro', descKey: 'toolsHub.cardPomodoroDesc' },
  { id: 'password', path: '/tools/password', icon: 'lock', titleKey: 'tools.password', descKey: 'toolsHub.cardPasswordDesc' },
  { id: 'base64', path: '/tools/base64', icon: 'b64', titleKey: 'tools.base64', descKey: 'toolsHub.cardBase64Desc' },
  { id: 'mbti', path: '/tools/mbti', icon: 'mbti', titleKey: 'toolsHub.cardMbtiTitle', descKey: 'toolsHub.cardMbtiDesc' },
  { id: 'oj', path: '/tools/oj', icon: 'code', titleKey: 'toolsHub.cardOjTitle', descKey: 'toolsHub.cardOjDesc' },
]

export const useToolsStore = defineStore('tools', {
  state: () => ({
    tools: toolsSeed as ToolCardItem[],
    favorites: [] as string[],
    recent: [] as string[],
  }),
  getters: {
    favoriteTools: (s) => s.tools.filter((t) => s.favorites.includes(t.id)),
    recentTools: (s) => s.recent.map((id) => s.tools.find((t) => t.id === id)).filter(Boolean) as ToolCardItem[],
  },
  actions: {
    hydrate() {
      try {
        const fav = JSON.parse(localStorage.getItem(FAVORITES_KEY) || '[]')
        const recent = JSON.parse(localStorage.getItem(RECENT_KEY) || '[]')
        this.favorites = Array.isArray(fav) ? fav : []
        this.recent = Array.isArray(recent) ? recent : []
      } catch {
        this.favorites = []
        this.recent = []
      }
    },
    toggleFavorite(id: string) {
      this.favorites = this.favorites.includes(id)
        ? this.favorites.filter((v) => v !== id)
        : [...this.favorites, id]
      localStorage.setItem(FAVORITES_KEY, JSON.stringify(this.favorites))
    },
    markUsed(id: string) {
      this.recent = [id, ...this.recent.filter((v) => v !== id)].slice(0, RECENT_LIMIT)
      localStorage.setItem(RECENT_KEY, JSON.stringify(this.recent))
    },
  },
})
