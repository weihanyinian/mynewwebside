import type { Router } from 'vue-router'

/**
 * 回到站点首页（路由 `/`，即 Portfolio 主页）。
 * 使用 `hash: ''` 避免从带锚点的子页返回时残留 `#xxx`。
 * 子页面组件会在离开路由时卸载，再次进入 `/blog` 等路由时会重新挂载，列表/搜索状态自然恢复为初始或 URL 决定的状态。
 */
export function goToSiteHome(router: Router) {
  router.push({ path: '/', hash: '' })
}
