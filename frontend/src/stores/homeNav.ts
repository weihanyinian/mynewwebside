import { defineStore } from 'pinia'

/**
 * 首页（/）当前锚点区块 id，不含 #。
 * 与滚动 IntersectionObserver 同步，供 SiteLayout 顶栏高亮；
 * 从子页回到首页或离开首页时清空。
 */
export const useHomeNavStore = defineStore('homeNav', {
  state: () => ({
    activeSectionId: '' as string,
  }),
  actions: {
    setActiveSection(id: string) {
      this.activeSectionId = id
    },
    clear() {
      this.activeSectionId = ''
    },
  },
})
