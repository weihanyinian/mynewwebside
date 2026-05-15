import { computed, onMounted, onUnmounted, watch, type Ref } from 'vue'
import type { HomeSectionId } from '../types/home'
import { useHomeNavStore } from '../stores/homeNav'
import { scrollToHomeSection } from '../utils/homeSectionScroll'

export function useSectionObserver(
  routeFullPath: Ref<string>,
  routeHash: Ref<string>,
  sectionIds: readonly HomeSectionId[],
) {
  const homeNav = useHomeNavStore()

  const activeSection = computed(() => homeNav.activeSectionId)

  function isHashActive(fragment: string) {
    const id = fragment.startsWith('#') ? fragment.slice(1) : fragment
    return homeNav.activeSectionId === id
  }

  function scrollToSection(id: string, offset = 92) {
    scrollToHomeSection(id, offset)
  }

  let sectionObserver: IntersectionObserver | null = null

  function setupSectionObserver() {
    sectionObserver = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          const sectionEl = entry.target as HTMLElement
          if (entry.isIntersecting) {
            homeNav.setActiveSection(sectionEl.id)
            sectionEl.classList.add('section--visible')
          }
        }
      },
      { rootMargin: '-90px 0px -65% 0px', threshold: 0 },
    )

    for (const id of sectionIds) {
      const el = document.getElementById(id)
      if (el) sectionObserver.observe(el)
    }
  }

  function revealSectionsTouchingViewport() {
    const vh = window.innerHeight
    for (const id of sectionIds) {
      const el = document.getElementById(id)
      if (!el) continue
      const r = el.getBoundingClientRect()
      if (r.top < vh && r.bottom > 0) el.classList.add('section--visible')
    }
  }

  watch(
    [routeFullPath, routeHash],
    () => {
      const h = routeHash.value ? routeHash.value.replace(/^#/, '') : ''
      homeNav.setActiveSection(h)
    },
    { immediate: true },
  )

  onMounted(() => {
    setupSectionObserver()
    requestAnimationFrame(() => {
      revealSectionsTouchingViewport()
    })
  })

  onUnmounted(() => {
    sectionObserver?.disconnect()
    sectionObserver = null
    homeNav.clear()
  })

  return { activeSection, isHashActive, scrollToSection }
}
