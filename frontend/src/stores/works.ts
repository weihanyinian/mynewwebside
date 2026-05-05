import { defineStore } from 'pinia'
import type { HomeWorkItem } from '../types/home'
import { fetchPortfolioWorks } from '../api/portfolioApi'

export const useWorksStore = defineStore('works', {
  state: () => ({
    selectedCategory: 'all',
    works: [] as HomeWorkItem[],
    loading: false,
    loadedFromBackend: false,
    loadError: '',
    lastFetch: 0,
  }),
  getters: {
    categories: (s) => ['all', ...Array.from(new Set(s.works.map((w) => w.tag.split('/')[0].trim())))],
    filteredWorks: (s) =>
      s.selectedCategory === 'all'
        ? s.works
        : s.works.filter((w) => w.tag.toLowerCase().includes(s.selectedCategory.toLowerCase())),
  },
  actions: {
    async fetchWorksFromBackend(force = false) {
      if (this.loading) return
      if (!force && this.loadedFromBackend && Date.now() - this.lastFetch < 300_000) return
      this.loading = true
      this.loadError = ''
      try {
        const rows = await fetchPortfolioWorks()
        this.works = rows
        this.loadedFromBackend = true
        this.lastFetch = Date.now()
        if (
          this.selectedCategory !== 'all'
          && !this.categories.some((c) => c.toLowerCase() === this.selectedCategory.toLowerCase())
        ) {
          this.selectedCategory = 'all'
        }
      } catch (e: unknown) {
        this.loadError = e instanceof Error ? e.message : '作品数据加载失败'
      } finally {
        this.loading = false
      }
    },
    setCategory(category: string) {
      this.selectedCategory = category
    },
  },
})
