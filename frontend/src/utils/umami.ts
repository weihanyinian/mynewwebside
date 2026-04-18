/** 在 main.ts 调用：需自托管 Umami 并配置网站后填入 .env */
export function initUmami() {
  const src = import.meta.env.VITE_UMAMI_SRC as string | undefined
  const websiteId = import.meta.env.VITE_UMAMI_WEBSITE_ID as string | undefined
  if (!src || !websiteId || typeof document === 'undefined') return
  const s = document.createElement('script')
  s.async = true
  s.src = src
  s.setAttribute('data-website-id', websiteId)
  document.head.appendChild(s)
}
