<script setup lang="ts">
import { Compartment, EditorState } from '@codemirror/state'
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands'
import { cpp } from '@codemirror/lang-cpp'
import { java } from '@codemirror/lang-java'
import { python } from '@codemirror/lang-python'
import { oneDark } from '@codemirror/theme-one-dark'
import { EditorView, keymap, lineNumbers } from '@codemirror/view'
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchOjProblem, postOjJudge, type JudgeResult, type ProblemDetail } from '../../api/oj'

type Lang = 'C' | 'CPP' | 'JAVA' | 'PYTHON'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const err = ref('')
const problem = ref<ProblemDetail | null>(null)
const lang = ref<Lang>('PYTHON')
const stdin = ref('')
const busy = ref(false)
const result = ref<JudgeResult | null>(null)
/** 运行/提交失败（与题目加载错误 err 分离） */
const actionError = ref('')

const editorHost = ref<HTMLDivElement | null>(null)
let view: EditorView | null = null
const languageConf = new Compartment()

let draftTimer: ReturnType<typeof setTimeout> | null = null

function langExtension(l: Lang) {
  switch (l) {
    case 'JAVA':
      return java()
    case 'CPP':
    case 'C':
      return cpp()
    case 'PYTHON':
    default:
      return python()
  }
}

function initialCode(p: ProblemDetail, l: Lang) {
  const refc = p.referenceSolution?.[l]
  if (refc) return refc
  if (l === 'JAVA') {
    return `import java.util.Scanner;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    \n  }\n}\n`
  }
  return ''
}

function draftKey(id: string) {
  return `oj-draft-${id}`
}

function loadDraft(id: string, p: ProblemDetail, l: Lang) {
  try {
    const raw = localStorage.getItem(draftKey(id))
    if (raw) return raw
  } catch {
    /* ignore */
  }
  return initialCode(p, l)
}

function scheduleSaveDraft(id: string) {
  if (!view) return
  if (draftTimer) clearTimeout(draftTimer)
  draftTimer = setTimeout(() => {
    try {
      localStorage.setItem(draftKey(id), view!.state.doc.toString())
    } catch {
      /* ignore */
    }
  }, 400)
}

function mountEditor(doc: string, l: Lang) {
  if (!editorHost.value) return
  if (view) {
    view.destroy()
    view = null
  }
  const id = String(route.params.id)
  const state = EditorState.create({
    doc,
    extensions: [
      oneDark,
      lineNumbers(),
      history(),
      keymap.of([indentWithTab, ...defaultKeymap, ...historyKeymap]),
      languageConf.of(langExtension(l)),
      EditorView.theme({
        '&': { height: 'min(52vh, 520px)' },
        '.cm-scroller': { overflow: 'auto' },
        '.cm-gutters': { backgroundColor: 'transparent', border: 'none' },
      }),
      EditorView.updateListener.of((u) => {
        if (u.docChanged) scheduleSaveDraft(id)
      }),
    ],
  })
  view = new EditorView({ state, parent: editorHost.value })
}

function applyLanguage(l: Lang) {
  if (!view) return
  view.dispatch({ effects: languageConf.reconfigure(langExtension(l)) })
}

function formatSimple() {
  if (!view) return
  const doc = view.state.doc
  const next = doc
    .toString()
    .split('\n')
    .map((line) => line.replace(/\s+$/, ''))
    .join('\n')
  if (next !== doc.toString()) {
    view.dispatch({ changes: { from: 0, to: doc.length, insert: next } })
  }
}

function clearCode() {
  if (!problem.value) return
  const id = problem.value.id
  const next = initialCode(problem.value, lang.value)
  if (view) {
    view.dispatch({
      changes: { from: 0, to: view.state.doc.length, insert: next },
    })
  }
  try {
    localStorage.removeItem(draftKey(id))
  } catch {
    /* ignore */
  }
}

function loadTemplate() {
  if (!problem.value || !view) return
  const next = initialCode(problem.value, lang.value)
  view.dispatch({
    changes: { from: 0, to: view.state.doc.length, insert: next },
  })
}

async function loadProblem() {
  const id = String(route.params.id)
  loading.value = true
  err.value = ''
  result.value = null
  actionError.value = ''
  try {
    const p = await fetchOjProblem(id)
    problem.value = p
    const allowed = p.supportedLangs as Lang[]
    if (allowed.length && !allowed.includes(lang.value)) {
      lang.value = allowed[0]
    }
    const doc = loadDraft(id, p, lang.value)
    mountEditor(doc, lang.value)
    if (p.samples?.length) {
      stdin.value = p.samples[0].input
    } else {
      stdin.value = ''
    }
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '加载失败'
    problem.value = null
  } finally {
    loading.value = false
  }
}

async function runCode() {
  if (!problem.value || !view) return
  busy.value = true
  result.value = null
  actionError.value = ''
  try {
    const r = await postOjJudge({
      problemId: problem.value.id,
      language: lang.value,
      sourceCode: view.state.doc.toString(),
      stdin: stdin.value.trim() ? stdin.value : null,
      submit: false,
    })
    result.value = r
  } catch (e: unknown) {
    actionError.value = e instanceof Error ? e.message : '运行失败'
  } finally {
    busy.value = false
  }
}

function appendHistory(r: JudgeResult) {
  if (!problem.value) return
  const key = 'oj-submissions-v1'
  try {
    const raw = localStorage.getItem(key)
    const arr = raw ? (JSON.parse(raw) as unknown[]) : []
    arr.unshift({
      problemId: problem.value.id,
      title: problem.value.title,
      lang: lang.value,
      verdict: r.verdict,
      submitted: r.submitted,
      timeSeconds: r.timeSeconds,
      memoryKb: r.memoryKb,
      at: Date.now(),
    })
    localStorage.setItem(key, JSON.stringify(arr.slice(0, 80)))
  } catch {
    /* ignore */
  }
}

async function submitCode() {
  if (!problem.value || !view) return
  busy.value = true
  result.value = null
  actionError.value = ''
  try {
    const r = await postOjJudge({
      problemId: problem.value.id,
      language: lang.value,
      sourceCode: view.state.doc.toString(),
      stdin: null,
      submit: true,
    })
    result.value = r
    appendHistory(r)
  } catch (e: unknown) {
    actionError.value = e instanceof Error ? e.message : '提交失败'
  } finally {
    busy.value = false
  }
}

function verdictClass(v: string) {
  switch (v) {
    case 'AC':
      return 'verdict ac'
    case 'WA':
      return 'verdict wa'
    case 'CE':
      return 'verdict ce'
    case 'TLE':
      return 'verdict tle'
    case 'RE':
      return 'verdict re'
    case 'RUN_OK':
      return 'verdict run'
    default:
      return 'verdict'
  }
}

onMounted(loadProblem)

watch(
  () => route.params.id,
  () => {
    loadProblem()
  },
)

watch(lang, (l) => {
  applyLanguage(l)
})

onBeforeUnmount(() => {
  if (draftTimer) clearTimeout(draftTimer)
  if (view) {
    view.destroy()
    view = null
  }
})
</script>

<template>
  <div class="oj-detail">
    <div class="top-row">
      <button type="button" class="site-pill site-pill--on-dark" @click="router.push('/oj')">← 题目列表</button>
      <span v-if="problem" class="pill">{{ problem.difficulty }} · {{ problem.judgeMode === 'LEETCODE' ? '力扣' : 'ACM' }}</span>
    </div>

    <p v-if="loading" class="hint glass-inline">加载中…</p>
    <p v-else-if="err && !problem" class="err glass-inline">{{ err }}</p>

    <template v-else-if="problem">
      <p v-if="actionError" class="action-err glass-inline">{{ actionError }}</p>

      <div class="layout">
        <section class="glass-card left">
          <h1 class="title">{{ problem.title }}</h1>
          <p class="meta">时间限制 {{ problem.timeLimitSec }}s · 内存 {{ problem.memoryLimitMb }}MB</p>
          <div class="block">
            <h3>题目描述</h3>
            <pre class="desc">{{ problem.description }}</pre>
          </div>
          <div class="block">
            <h3>输入说明</h3>
            <pre class="desc">{{ problem.inputDesc }}</pre>
          </div>
          <div class="block">
            <h3>输出说明</h3>
            <pre class="desc">{{ problem.outputDesc }}</pre>
          </div>
          <div class="block">
            <h3>样例</h3>
            <div v-for="(s, i) in problem.samples" :key="i" class="sample">
              <p class="sample-label">样例 {{ i + 1 }}</p>
              <pre class="mono">{{ s.input }}</pre>
              <p class="sample-label">输出</p>
              <pre class="mono">{{ s.output }}</pre>
            </div>
          </div>
        </section>

        <section class="glass-card right">
          <div class="editor-toolbar">
            <label class="lbl">
              语言
              <select v-model="lang" class="select">
                <option v-for="l in problem.supportedLangs" :key="l" :value="l">{{ l }}</option>
              </select>
            </label>
            <button type="button" class="site-pill site-pill--on-dark" :disabled="busy" @click="runCode">运行</button>
            <button type="button" class="site-pill site-pill--active" :disabled="busy" @click="submitCode">提交判题</button>
            <button type="button" class="site-pill site-pill--on-dark" :disabled="busy" @click="formatSimple">整理空白</button>
            <button type="button" class="site-pill site-pill--on-dark" :disabled="busy" @click="loadTemplate">载入模板</button>
            <button type="button" class="site-pill site-pill--on-dark" :disabled="busy" @click="clearCode">清空</button>
          </div>
          <p class="tip">Java 请保持 <code>public class Main</code>；判题使用 Judge0 公共实例，高峰期可能较慢。</p>
          <div ref="editorHost" class="editor-host" />
          <div class="stdin-wrap">
            <label class="lbl full">运行用 stdin（可留空则使用首个样例输入）</label>
            <textarea v-model="stdin" class="stdin" rows="5" placeholder="自定义输入…" />
          </div>
        </section>
      </div>

      <section v-if="result" class="glass-card result">
        <div class="result-head">
          <span :class="verdictClass(result.verdict)">{{ result.verdict }}</span>
          <span v-if="result.timeSeconds != null" class="muted">时间 {{ result.timeSeconds }} s</span>
          <span v-if="result.memoryKb != null" class="muted">内存 {{ result.memoryKb }} KB</span>
          <span class="muted">{{ result.submitted ? '提交判题' : '仅运行' }}</span>
        </div>
        <p class="msg">{{ result.message }}</p>
        <div v-if="result.stdout" class="out-block">
          <h4>标准输出</h4>
          <pre class="mono out">{{ result.stdout }}</pre>
        </div>
        <div v-if="result.stderr" class="out-block">
          <h4>标准错误</h4>
          <pre class="mono err-out">{{ result.stderr }}</pre>
        </div>
        <div v-if="result.compileOutput" class="out-block">
          <h4>编译信息</h4>
          <pre class="mono err-out">{{ result.compileOutput }}</pre>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(102, 217, 255, 0.2);
  border-radius: 16px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 30, 60, 0.35);
}

.glass-inline {
  display: inline-block;
  padding: 0.5rem 0.75rem;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.45);
}

.top-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.pill {
  font-size: 0.85rem;
  padding: 0.2rem 0.65rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(102, 217, 255, 0.2);
}

.layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  align-items: start;
}

@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

.left {
  padding: 1.1rem 1.25rem;
}

.right {
  padding: 1rem 1.1rem;
}

.title {
  margin: 0 0 0.35rem;
  font-size: 1.35rem;
}

.meta {
  margin: 0 0 1rem;
  opacity: 0.9;
  font-size: 0.9rem;
}

.block h3 {
  margin: 0 0 0.35rem;
  font-size: 1rem;
}

.desc {
  margin: 0 0 0.85rem;
  white-space: pre-wrap;
  font-family: ui-sans-serif, system-ui, sans-serif;
  font-size: 0.92rem;
  line-height: 1.55;
}

.sample {
  margin-bottom: 0.75rem;
}

.sample-label {
  margin: 0.25rem 0;
  font-size: 0.85rem;
  opacity: 0.85;
}

.mono {
  margin: 0;
  padding: 0.5rem 0.65rem;
  border-radius: 16px;
  background: rgba(0, 30, 60, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.35);
  overflow: auto;
  font-size: 0.85rem;
  white-space: pre-wrap;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.lbl {
  display: flex;
  gap: 0.35rem;
  align-items: center;
  font-size: 0.85rem;
}

.lbl.full {
  width: 100%;
}

.select {
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  background: rgba(0, 40, 80, 0.2);
  color: #fff;
  padding: 0.35rem 0.5rem;
}

.tip {
  font-size: 0.8rem;
  opacity: 0.88;
  margin: 0 0 0.5rem;
}

.tip code {
  background: rgba(0, 30, 60, 0.35);
  padding: 0.1rem 0.3rem;
  border-radius: 6px;
}

.editor-host {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.35);
  margin-bottom: 0.75rem;
}

.stdin-wrap {
  margin-top: 0.25rem;
}

.stdin {
  width: 100%;
  box-sizing: border-box;
  margin-top: 0.35rem;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(0, 30, 60, 0.25);
  color: #fff;
  padding: 0.5rem 0.65rem;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  font-size: 0.85rem;
  resize: vertical;
}

.result {
  margin-top: 1rem;
  padding: 1rem 1.15rem;
}

.result-head {
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
  align-items: center;
  margin-bottom: 0.35rem;
}

.verdict {
  font-weight: 800;
  letter-spacing: 0.02em;
  padding: 0.2rem 0.55rem;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.12);
}

.verdict.ac {
  background: rgba(46, 204, 113, 0.28);
}

.verdict.wa {
  background: rgba(241, 196, 15, 0.28);
}

.verdict.ce,
.verdict.re,
.verdict.tle {
  background: rgba(231, 76, 60, 0.22);
}

.verdict.run {
  background: rgba(52, 152, 219, 0.25);
}

.muted {
  opacity: 0.85;
  font-size: 0.88rem;
}

.msg {
  margin: 0.25rem 0 0.75rem;
  white-space: pre-wrap;
}

.out-block h4 {
  margin: 0.35rem 0;
  font-size: 0.9rem;
}

.out {
  border-color: rgba(102, 217, 255, 0.35);
}

.err-out {
  border-color: rgba(255, 180, 180, 0.45);
}

.hint,
.err {
  color: #fff;
}

.err {
  color: #ffd0d0;
}

.action-err {
  margin: 0 0 0.75rem;
  color: #ffecec;
  border-color: rgba(255, 160, 160, 0.55);
}
</style>
