import { onMounted, onUnmounted, ref, watch, type Ref } from 'vue'
import type { HomeSectionId } from '../types/home'

export function useSectionObserver(
  routeFullPath: Ref<string>,
  routeHash: Ref<string>,
  sectionIds: readonly HomeSectionId[],
) {
  const activeSection = ref(routeHash.value ? routeHash.value.replace(/^#/, '') : '')
  let sectionObserver: IntersectionObserver | null = null

  function isHashActive(fragment: string) {
    return `#${activeSection.value}` === fragment
  }

  function updateHashWithoutRouteJump(id: string) {
    const hash = `#${id}`
    if (window.location.hash === hash) return
    window.history.replaceState(window.history.state, '', `${window.location.pathname}${window.location.search}${hash}`)
  }

  function scrollToSection(id: string, offset = 92) {
    const target = document.getElementById(id)
    if (!target) return
    activeSection.value = id
    const top = window.scrollY + target.getBoundingClientRect().top - offset
    window.scrollTo({ top: Math.max(top, 0), behavior: 'smooth' })
    updateHashWithoutRouteJump(id)
  }

  function setupSectionObserver() {
    sectionObserver = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          const sectionEl = entry.target as HTMLElement
          if (entry.isIntersecting) {
            activeSection.value = sectionEl.id
            sectionEl.classList.add('section--visible')
          } else {
            sectionEl.classList.remove('section--visible')
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

  watch(routeFullPath, () => {
    activeSection.value = routeHash.value ? routeHash.value.replace(/^#/, '') : ''
  })

  onMounted(() => {
    setupSectionObserver()
  })

  onUnmounted(() => {
    sectionObserver?.disconnect()
  })

  return { activeSection, isHashActive, scrollToSection }
}
