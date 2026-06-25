<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const props = defineProps<{
  content: string
}>()

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight(str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      } catch (_) {}
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

const html = computed(() => md.render(props.content))
</script>

<template>
  <div class="markdown-body" v-html="html"></div>
</template>

<style>
.markdown-body {
  color: var(--text-primary);
  line-height: 1.8;
  font-size: 1rem;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4 {
  font-weight: 700;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  line-height: 1.3;
}

.markdown-body h1 { font-size: 1.8rem; }
.markdown-body h2 { font-size: 1.5rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.3em; }
.markdown-body h3 { font-size: 1.25rem; }
.markdown-body h4 { font-size: 1.1rem; }

.markdown-body p { margin-bottom: 1em; }

.markdown-body a {
  color: var(--primary);
  text-decoration: underline;
}

.markdown-body code {
  background: var(--code-bg, rgba(255,255,255,0.08));
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', 'Cascadia Code', 'Consolas', monospace;
  font-size: 0.9em;
}

.markdown-body pre {
  background: #1e1e2e;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 1em 0;
  border: 1px solid rgba(255,255,255,0.05);
}

.markdown-body pre code {
  background: transparent;
  padding: 0;
  font-size: 0.85rem;
  line-height: 1.6;
}

.markdown-body blockquote {
  border-left: 3px solid var(--primary);
  padding: 8px 16px;
  margin: 1em 0;
  background: rgba(98, 167, 234, 0.05);
  border-radius: 0 4px 4px 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 1.5em;
  margin-bottom: 1em;
}

.markdown-body li { margin-bottom: 0.25em; }

.markdown-body table {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

.markdown-body th,
.markdown-body td {
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  text-align: left;
}

.markdown-body th {
  background: rgba(255,255,255,0.05);
  font-weight: 600;
}

.markdown-body img {
  max-width: 100%;
  border-radius: 8px;
  margin: 1em 0;
}

.markdown-body hr {
  border: none;
  border-top: 1px solid var(--border-color);
  margin: 2em 0;
}
</style>
