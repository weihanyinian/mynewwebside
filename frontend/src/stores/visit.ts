import { defineStore } from 'pinia'

const SITE_VISITS_KEY = 'site_visits_total'
const HOME_VISITS_KEY = 'home_visits_total'
const TODAY_KEY = 'site_visits_today'
const DATE_KEY = 'site_visits_date'

function dateTag() {
  return new Date().toISOString().slice(0, 10)
}

export const useVisitStore = defineStore('visit', {
  state: () => ({
    siteVisits: 0,
    homeVisits: 0,
    todayVisits: 0,
    initialized: false,
  }),
  actions: {
    initHomeVisit() {
      if (this.initialized) return
      this.initialized = true
      const today = dateTag()
      const savedDate = localStorage.getItem(DATE_KEY)
      const todayCount = Number(localStorage.getItem(TODAY_KEY) || '0')
      this.todayVisits = savedDate === today ? todayCount : 0
      this.siteVisits = Number(localStorage.getItem(SITE_VISITS_KEY) || '0')
      this.homeVisits = Number(localStorage.getItem(HOME_VISITS_KEY) || '0')

      this.siteVisits += 1
      this.homeVisits += 1
      this.todayVisits = savedDate === today ? this.todayVisits + 1 : 1

      localStorage.setItem(SITE_VISITS_KEY, String(this.siteVisits))
      localStorage.setItem(HOME_VISITS_KEY, String(this.homeVisits))
      localStorage.setItem(TODAY_KEY, String(this.todayVisits))
      localStorage.setItem(DATE_KEY, today)
    },
  },
})
