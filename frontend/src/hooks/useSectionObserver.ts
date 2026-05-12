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
            // 只增不减：离开视口时不再移除，否则下方区块（如 #works）在首屏或从 #blog 回滚时
            // 会长期保持 opacity:0，表现为「作品展示」整段消失。
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

  /** IO 的 rootMargin 较严时，首屏可能永远不触发 intersect，区块会一直保持 opacity:0 */
  function revealSectionsTouchingViewport() {
    const vh = window.innerHeight
    for (const id of sectionIds) {
      const el = document.getElementById(id)
      if (!el) continue
      const r = el.getBoundingClientRect()
      if (r.top < vh && r.bottom > 0) el.classList.add('section--visible')
    }
  }

  watch(routeFullPath, () => {
    activeSection.value = routeHash.value ? routeHash.value.replace(/^#/, '') : ''
  })

  onMounted(() => {
    setupSectionObserver()
    requestAnimationFrame(() => {
      revealSectionsTouchingViewport()
    })
  })

  onUnmounted(() => {
    sectionObserver?.disconnect()
  })

  return { activeSection, isHashActive, scrollToSection }
}
