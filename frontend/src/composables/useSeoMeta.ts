/**
 * Dynamically update <title> and OG/Twitter meta tags for article/blog pages.
 */
export function useSeoMeta(opts: {
  title: string
  description?: string
  url?: string
  image?: string
  type?: string
}) {
  document.title = opts.title
  setMeta('og:title', opts.title)
  setMeta('twitter:title', opts.title)

  if (opts.description) {
    setMeta('description', opts.description)
    setMeta('og:description', opts.description)
    setMeta('twitter:description', opts.description)
  }
  if (opts.url) {
    setMeta('og:url', opts.url)
  }
  if (opts.image) {
    setMeta('og:image', opts.image)
  }
  if (opts.type) {
    setMeta('og:type', opts.type)
  }
}

function setMeta(name: string, content: string) {
  let el = document.querySelector(`meta[name="${name}"], meta[property="${name}"]`) as HTMLMetaElement | null
  if (!el) {
    el = document.createElement('meta')
    if (name.startsWith('og:')) el.setAttribute('property', name)
    else el.setAttribute('name', name)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}
