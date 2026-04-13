<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark-dimmed.css'

const props = defineProps<{
  content: string
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

const html = computed(() => md.render(props.content || ''))
</script>

<template>
  <div class="md-body" v-html="html"></div>
</template>

<style scoped>
.md-body :deep(pre) {
  overflow: auto;
  border-radius: 10px;
  padding: 14px;
  margin: 14px 0;
}

.md-body :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 13px;
}
</style>
