<script setup lang="ts">
import { computed, watch } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark-dimmed.css'

const props = defineProps<{
  content: string
}>()

type TocItem = {
  id: string
  level: number
  text: string
}

const emit = defineEmits<{
  (e: 'toc', items: TocItem[]): void
}>()

const escapeHtml = new MarkdownIt().utils.escapeHtml

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return `<pre class="hljs"><code>${hljs.highlight(code, { language: lang }).value}</code></pre>`
    }
    return `<pre class="hljs"><code>${escapeHtml(code)}</code></pre>`
  },
})

function slugify(text: string) {
  return (text || '')
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/[^\w\u4e00-\u9fa5-]+/g, '')
}

const rendered = computed(() => {
  const content = props.content || ''
  const env: Record<string, unknown> = {}
  const tokens = md.parse(content, env)

  const used = new Map<string, number>()
  const toc: TocItem[] = []

  for (let i = 0; i < tokens.length; i++) {
    const t = tokens[i]
    if (t.type !== 'heading_open') continue

    const level = Number(String(t.tag).slice(1))
    if (level < 2 || level > 4) continue
    const inline = tokens[i + 1]
    const text = inline?.type === 'inline' ? inline.content : ''
    const base = slugify(text) || `section-${i}`
    const count = used.get(base) ?? 0
    used.set(base, count + 1)
    const id = count === 0 ? base : `${base}-${count + 1}`
    t.attrSet('id', id)

    toc.push({ id, level, text })
  }

  return { html: md.renderer.render(tokens, md.options, env), toc }
})

watch(
  () => rendered.value.toc,
  items => emit('toc', items),
  { immediate: true }
)
</script>

<template>
  <div
    class="md-body prose prose-invert max-w-none prose-headings:scroll-mt-24 prose-p:leading-8 prose-li:leading-8 prose-pre:bg-white/[0.06] prose-pre:border prose-pre:border-white/10 prose-pre:backdrop-blur-xl prose-pre:rounded-2xl prose-pre:shadow-[0_16px_40px_rgba(0,0,0,0.35)] prose-pre:px-5 prose-pre:py-4 prose-code:text-[#8be6ff] prose-code:bg-white/[0.06] prose-code:px-1 prose-code:py-0.5 prose-code:rounded-md prose-code:before:content-[''] prose-code:after:content-['']"
    v-html="rendered.html"
  ></div>
</template>

<style scoped>
.md-body :deep(pre) {
  overflow: auto;
  margin: 20px 0;
}

.md-body :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.9em;
}
</style>
