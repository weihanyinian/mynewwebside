/**
 * HTML 转义：用于 v-html 安全渲染用户输入，或与其它模板引擎配合。
 * Vue 的 {{ }} 已默认转义；显式调用可避免将来误用 v-html 时的 XSS。
 */
export function escapeHtml(raw: string): string {
  return raw
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}
