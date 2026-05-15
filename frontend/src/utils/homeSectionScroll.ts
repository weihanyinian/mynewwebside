import { useHomeNavStore } from '../stores/homeNav'

const DEFAULT_OFFSET = 92

/** 平滑滚动到首页区块，并同步 URL hash（replaceState，不触发整页路由跳转）与高亮 store */
export function scrollToHomeSection(id: string, offset = DEFAULT_OFFSET) {
  const homeNav = useHomeNavStore()
  homeNav.setActiveSection(id)

  const target = document.getElementById(id)
  if (!target) return

  const top = window.scrollY + target.getBoundingClientRect().top - offset
  window.scrollTo({ top: Math.max(top, 0), behavior: 'smooth' })

  const hash = `#${id}`
  if (window.location.hash !== hash) {
    window.history.replaceState(
      window.history.state,
      '',
      `${window.location.pathname}${window.location.search}${hash}`,
    )
  }
}
