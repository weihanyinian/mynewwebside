<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as monaco from 'monaco-editor'
import { fetchOjProblem, postOjJudge, type ProblemDetail, type JudgeResult } from '../../api/oj'
import { defineMonacoThemes } from '../../config/monacoThemes'
import {
  ALL_LANGUAGE_OPTIONS,
  CODE_TEMPLATES,
  VERDICT_CLASS,
  VERDICT_TEXT,
  isValidLanguage,
  type OjLanguage,
} from '../../config/ojConstants'

import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

self.MonacoEnvironment = {
  getWorker(_: unknown, label: string) {
    if (label === 'json') return new jsonWorker()
    if (label === 'html' || label === 'handlebars' || label === 'razor') return new htmlWorker()
    if (label === 'typescript' || label === 'javascript') return new tsWorker()
    return new editorWorker()
  },
}

let themesDefined = false

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const err = ref('')
const problem = ref<ProblemDetail | null>(null)

const editorContainer = ref<HTMLDivElement | null>(null)
let editorInstance: monaco.editor.IStandaloneCodeEditor | null = null

const currentLang = ref<OjLanguage>('CPP')
const codeStore = ref<Record<string, string>>({ C: '', CPP: '', JAVA: '', PYTHON: '' })

const customStdin = ref('')
const useCustomInput = ref(false)

const running = ref(false)
const result = ref<JudgeResult | null>(null)
const activeTab = ref<'stdin' | 'stdout' | 'stderr'>('stdin')
const descExpanded = ref(true)

const languageOptions = computed(() => {
  if (!problem.value?.supportedLangs) return ALL_LANGUAGE_OPTIONS
  return ALL_LANGUAGE_OPTIONS.filter((opt) =>
    problem.value!.supportedLangs.includes(opt.value),
  )
})

const currentMonacoLang = computed(
  () => languageOptions.value.find((l) => l.value === currentLang.value)?.monacoLang || 'cpp',
)

let saveTimer: ReturnType<typeof setTimeout> | null = null

function initMonacoEditor() {
  if (!editorContainer.value) return
  if (editorInstance) {
    editorInstance.dispose()
    editorInstance = null
  }

  if (!themesDefined) {
    defineMonacoThemes(monaco)
    themesDefined = true
  }

  editorInstance = monaco.editor.create(editorContainer.value, {
    value: codeStore.value[currentLang.value] || CODE_TEMPLATES[currentLang.value],
    language: currentMonacoLang.value,
    theme: 'alibaba-dark',
    fontSize: 14,
    fontFamily: "'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', monospace",
    fontLigatures: true,
    lineHeight: 22,
    letterSpacing: 0.5,
    minimap: { enabled: true, scale: 1 },
    scrollBeyondLastLine: false,
    automaticLayout: true,
    tabSize: 4,
    insertSpaces: true,
    wordWrap: 'on',
    lineNumbers: 'on',
    renderLineHighlight: 'all',
    cursorBlinking: 'smooth',
    cursorSmoothCaretAnimation: 'on',
    smoothScrolling: true,
    padding: { top: 16, bottom: 16 },
    roundedSelection: true,
    renderWhitespace: 'selection',
    guides: { indentation: true, bracketPairs: true, highlightActiveIndentation: true },
    bracketPairColorization: { enabled: true },
    suggest: {
      showKeywords: true,
      showSnippets: true,
      showClasses: true,
      showFunctions: true,
      showVariables: true,
      showConstants: true,
    },
    quickSuggestions: { other: true, comments: false, strings: false },
    parameterHints: { enabled: true },
    formatOnPaste: true,
    formatOnType: true,
  })

  editorInstance.onDidChangeModelContent(() => {
    if (editorInstance) {
      codeStore.value[currentLang.value] = editorInstance.getValue()
      scheduleSave()
    }
  })

  editorInstance.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => runCode(false))
  editorInstance.addCommand(
    monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.Enter,
    () => runCode(true),
  )

  requestAnimationFrame(() => editorInstance?.layout())
}

function scheduleSave() {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => saveCode(), 500)
}

function switchLanguage(lang: OjLanguage) {
  if (editorInstance) {
    codeStore.value[currentLang.value] = editorInstance.getValue()
  }
  currentLang.value = lang
  if (editorInstance) {
    const model = editorInstance.getModel()
    if (model) monaco.editor.setModelLanguage(model, currentMonacoLang.value)
    const code = codeStore.value[lang] || CODE_TEMPLATES[lang]
    editorInstance.setValue(code)
  }
}

async function loadProblem() {
  const id = route.params.id as string
  if (!id) { err.value = '题目ID不存在'; return }

  loading.value = true
  err.value = ''

  try {
    problem.value = await fetchOjProblem(id)

    const savedCode = localStorage.getItem(`oj-code-${id}`)
    if (savedCode) {
      try {
        const parsed = JSON.parse(savedCode)
        codeStore.value = { ...codeStore.value, ...parsed }
      } catch { /* ignore */ }
    }

    if (problem.value.referenceSolution) {
      for (const [lang, code] of Object.entries(problem.value.referenceSolution)) {
        if (code && isValidLanguage(lang) && !codeStore.value[lang]) {
          codeStore.value[lang] = code
        }
      }
    }

    if (problem.value.supportedLangs?.length > 0) {
      const firstLang = problem.value.supportedLangs[0]
      if (isValidLanguage(firstLang)) currentLang.value = firstLang
    }
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }

  if (!problem.value || err.value) return
  await nextTick()
  initMonacoEditor()
}

function saveCode() {
  if (!problem.value) return
  localStorage.setItem(`oj-code-${problem.value.id}`, JSON.stringify(codeStore.value))
}

async function runCode(submit: boolean) {
  if (!problem.value || !editorInstance) return

  const sourceCode = editorInstance.getValue().trim()
  if (!sourceCode) { ElMessage.warning('请先编写代码'); return }

  running.value = true
  result.value = null

  try {
    const stdin = useCustomInput.value ? customStdin.value : null
    result.value = await postOjJudge({
      problemId: problem.value.id,
      language: currentLang.value,
      sourceCode,
      stdin,
      submit,
    })

    if (result.value.compileOutput) activeTab.value = 'stderr'
    else if (result.value.stdout) activeTab.value = 'stdout'

    if (result.value.verdict === 'AC') ElMessage.success('Accepted! 恭喜通过！')
    else if (result.value.verdict === 'RUN_OK') ElMessage.success('运行完成')
    else if (['CE', 'RE', 'TLE', 'WA'].includes(result.value.verdict))
      ElMessage.error(result.value.message || '运行失败')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    running.value = false
  }
}

function useSampleInput(input: string) {
  customStdin.value = input
  useCustomInput.value = true
}

function formatCode() {
  editorInstance?.getAction('editor.action.formatDocument')?.run()
}

function resetCode() {
  if (editorInstance) editorInstance.setValue(CODE_TEMPLATES[currentLang.value])
}

function getVerdictClass(verdict: string): string {
  return VERDICT_CLASS[verdict as keyof typeof VERDICT_CLASS] || ''
}

function getVerdictText(verdict: string): string {
  return VERDICT_TEXT[verdict as keyof typeof VERDICT_TEXT] || verdict
}

onMounted(() => loadProblem())

onUnmounted(() => {
  if (saveTimer) clearTimeout(saveTimer)
  if (editorInstance) { editorInstance.dispose(); editorInstance = null }
})

watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    if (editorInstance) { editorInstance.dispose(); editorInstance = null }
    result.value = null
    codeStore.value = { C: '', CPP: '', JAVA: '', PYTHON: '' }
    loadProblem()
  }
})
</script>

<template>
  <div class="oj-detail">
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="err" class="error-state">
      <p class="err-msg">{{ err }}</p>
      <button class="site-pill" @click="router.push('/tools/oj')">返回题目列表</button>
    </div>

    <template v-else-if="problem">
      <div class="ide-container">
        <div class="problem-panel" :class="{ collapsed: !descExpanded }">
          <div class="panel-header">
            <h2 class="problem-title">{{ problem.title }}</h2>
            <div class="problem-meta">
              <span class="badge difficulty" :class="problem.difficulty.toLowerCase()">
                {{ problem.difficulty }}
              </span>
              <span class="badge mode">{{ problem.judgeMode === 'LEETCODE' ? '力扣' : 'ACM' }}</span>
              <span class="badge limit">⏱ {{ problem.timeLimitSec }}s</span>
              <span class="badge limit">💾 {{ problem.memoryLimitMb }}MB</span>
            </div>
            <button class="toggle-btn" @click="descExpanded = !descExpanded">
              {{ descExpanded ? '◀ 收起' : '▶ 展开' }}
            </button>
          </div>

          <div v-if="descExpanded" class="panel-content">
            <section class="section">
              <h3>题目描述</h3>
              <div class="desc-text">{{ problem.description }}</div>
            </section>
            <section class="section">
              <h3>输入格式</h3>
              <p class="desc-text">{{ problem.inputDesc }}</p>
            </section>
            <section class="section">
              <h3>输出格式</h3>
              <p class="desc-text">{{ problem.outputDesc }}</p>
            </section>
            <section class="section">
              <h3>样例</h3>
              <div v-for="(sample, idx) in problem.samples" :key="idx" class="sample-block">
                <div class="sample-header">
                  <span>样例 {{ idx + 1 }}</span>
                  <button class="use-sample-btn" @click="useSampleInput(sample.input)">使用此输入</button>
                </div>
                <div class="sample-io">
                  <div class="sample-input"><label>输入</label><pre>{{ sample.input }}</pre></div>
                  <div class="sample-output"><label>输出</label><pre>{{ sample.output }}</pre></div>
                </div>
              </div>
            </section>
          </div>
        </div>

        <div class="editor-panel">
          <div class="editor-toolbar">
            <div class="lang-selector">
              <button
                v-for="lang in languageOptions"
                :key="lang.value"
                class="lang-btn"
                :class="{ active: currentLang === lang.value }"
                @click="switchLanguage(lang.value)"
              >
                {{ lang.label }}
              </button>
            </div>
            <div class="toolbar-actions">
              <button class="action-btn" @click="formatCode" title="格式化代码">
                <span class="icon">✨</span> 格式化
              </button>
              <button class="action-btn" @click="resetCode" title="重置代码">
                <span class="icon">🔄</span> 重置
              </button>
            </div>
          </div>

          <div ref="editorContainer" class="monaco-container"></div>

          <div class="io-panel">
            <div class="io-tabs">
              <button class="io-tab" :class="{ active: activeTab === 'stdin' }" @click="activeTab = 'stdin'">输入</button>
              <button class="io-tab" :class="{ active: activeTab === 'stdout' }" @click="activeTab = 'stdout'">输出</button>
              <button class="io-tab" :class="{ active: activeTab === 'stderr' }" @click="activeTab = 'stderr'">编译信息</button>
              <div class="io-actions">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="useCustomInput" /> 自定义输入
                </label>
              </div>
            </div>

            <div class="io-content">
              <div v-show="activeTab === 'stdin'" class="stdin-area">
                <textarea v-model="customStdin" placeholder="在此输入测试数据..." :disabled="!useCustomInput"></textarea>
                <p v-if="!useCustomInput" class="hint">未启用自定义输入时，将使用题目第一个样例作为输入</p>
              </div>
              <div v-show="activeTab === 'stdout'" class="stdout-area">
                <template v-if="result">
                  <div class="result-header">
                    <span class="verdict" :class="getVerdictClass(result.verdict)">{{ getVerdictText(result.verdict) }}</span>
                    <span v-if="result.timeSeconds" class="stat">⏱ {{ result.timeSeconds.toFixed(3) }}s</span>
                    <span v-if="result.memoryKb" class="stat">💾 {{ (result.memoryKb / 1024).toFixed(2) }}MB</span>
                  </div>
                  <pre v-if="result.stdout" class="output-text">{{ result.stdout }}</pre>
                  <p v-else class="empty-hint">无输出</p>
                </template>
                <p v-else class="empty-hint">运行代码后查看输出</p>
              </div>
              <div v-show="activeTab === 'stderr'" class="stderr-area">
                <template v-if="result">
                  <pre v-if="result.compileOutput" class="error-text">{{ result.compileOutput }}</pre>
                  <pre v-else-if="result.stderr" class="error-text">{{ result.stderr }}</pre>
                  <p v-else class="empty-hint">无编译错误</p>
                </template>
                <p v-else class="empty-hint">运行代码后查看编译信息</p>
              </div>
            </div>
          </div>

          <div class="action-bar">
            <button class="run-btn" :disabled="running" @click="runCode(false)"><span class="icon">▶</span> 运行</button>
            <button class="submit-btn" :disabled="running" @click="runCode(true)"><span class="icon">📤</span> 提交</button>
            <button class="back-btn" @click="router.push('/tools/oj')">返回列表</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.oj-detail {
  width: 100%;
  min-height: calc(100vh - 200px);
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--text-color, #e2e8f0);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.err-msg { color: #f87171; margin-bottom: 16px; }

.ide-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 220px);
  min-height: 600px;
}

.problem-panel {
  width: 420px;
  min-width: 320px;
  background: rgba(30, 30, 30, 0.95);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s ease;
}

.problem-panel.collapsed { width: 60px; min-width: 60px; }

.panel-header {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
}

.problem-title { margin: 0 0 12px; font-size: 1.25rem; font-weight: 700; color: #fff; }

.problem-meta { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }

.badge { padding: 4px 10px; border-radius: 6px; font-size: 0.75rem; font-weight: 600; }
.badge.difficulty { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
.badge.difficulty.入门 { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
.badge.difficulty.简单 { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
.badge.difficulty.中等 { background: rgba(234, 179, 8, 0.2); color: #facc15; }
.badge.difficulty.困难 { background: rgba(239, 68, 68, 0.2); color: #f87171; }
.badge.mode { background: rgba(168, 85, 247, 0.2); color: #c084fc; }
.badge.limit { background: rgba(100, 116, 139, 0.2); color: #94a3b8; }

.toggle-btn {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #94a3b8;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}
.toggle-btn:hover { background: rgba(255, 255, 255, 0.15); color: #fff; }

.panel-content { flex: 1; overflow-y: auto; padding: 16px; }

.section { margin-bottom: 20px; }
.section h3 {
  margin: 0 0 8px; font-size: 0.9rem; font-weight: 600; color: #94a3b8;
  text-transform: uppercase; letter-spacing: 0.5px;
}
.desc-text { color: #e2e8f0; line-height: 1.7; white-space: pre-wrap; font-size: 0.9rem; }

.sample-block { background: rgba(0, 0, 0, 0.3); border-radius: 8px; margin-bottom: 12px; overflow: hidden; }
.sample-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; background: rgba(0, 0, 0, 0.2); font-size: 0.8rem; color: #94a3b8;
}
.use-sample-btn {
  background: rgba(59, 130, 246, 0.2); border: none; color: #60a5fa;
  padding: 4px 8px; border-radius: 4px; cursor: pointer; font-size: 0.75rem; transition: all 0.2s;
}
.use-sample-btn:hover { background: rgba(59, 130, 246, 0.3); }

.sample-io { display: grid; grid-template-columns: 1fr 1fr; gap: 1px; background: rgba(255, 255, 255, 0.1); }
.sample-input, .sample-output { padding: 12px; }
.sample-input label, .sample-output label { display: block; font-size: 0.75rem; color: #64748b; margin-bottom: 4px; }
.sample-io pre {
  margin: 0; font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.85rem; color: #e2e8f0; white-space: pre-wrap; word-break: break-all;
}

.editor-panel {
  flex: 1; display: flex; flex-direction: column;
  background: rgba(30, 30, 30, 0.95); border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1); overflow: hidden;
}

.editor-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; background: rgba(0, 0, 0, 0.3); border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.lang-selector { display: flex; gap: 4px; }
.lang-btn {
  background: transparent; border: 1px solid rgba(255, 255, 255, 0.1); color: #94a3b8;
  padding: 6px 14px; border-radius: 6px; cursor: pointer; font-size: 0.85rem; font-weight: 500; transition: all 0.2s;
}
.lang-btn:hover { background: rgba(255, 255, 255, 0.05); color: #fff; }
.lang-btn.active { background: rgba(59, 130, 246, 0.2); border-color: rgba(59, 130, 246, 0.5); color: #60a5fa; }

.toolbar-actions { display: flex; gap: 8px; }
.action-btn {
  background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); color: #94a3b8;
  padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 0.8rem;
  display: flex; align-items: center; gap: 4px; transition: all 0.2s;
}
.action-btn:hover { background: rgba(255, 255, 255, 0.1); color: #fff; }

.monaco-container { flex: 1; min-height: 300px; }

.io-panel { border-top: 1px solid rgba(255, 255, 255, 0.1); background: rgba(0, 0, 0, 0.2); }
.io-tabs {
  display: flex; align-items: center; padding: 0 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1); background: rgba(0, 0, 0, 0.1);
}
.io-tab {
  background: transparent; border: none; color: #64748b; padding: 10px 16px;
  cursor: pointer; font-size: 0.85rem; border-bottom: 2px solid transparent; transition: all 0.2s;
}
.io-tab:hover { color: #94a3b8; }
.io-tab.active { color: #60a5fa; border-bottom-color: #60a5fa; }
.io-actions { margin-left: auto; }

.checkbox-label { display: flex; align-items: center; gap: 6px; color: #94a3b8; font-size: 0.8rem; cursor: pointer; }

.io-content { padding: 12px; min-height: 120px; max-height: 200px; overflow: auto; }

.stdin-area textarea {
  width: 100%; min-height: 80px; background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 6px; color: #e2e8f0;
  padding: 10px; font-family: 'JetBrains Mono', monospace; font-size: 0.85rem; resize: vertical;
}
.stdin-area textarea:focus { outline: none; border-color: rgba(59, 130, 246, 0.5); }
.stdin-area textarea:disabled { opacity: 0.5; cursor: not-allowed; }

.hint { color: #64748b; font-size: 0.8rem; margin-top: 8px; }
.stdout-area, .stderr-area { font-family: 'JetBrains Mono', monospace; }

.result-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.verdict { padding: 4px 12px; border-radius: 6px; font-size: 0.85rem; font-weight: 600; }

.verdict-ac { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
.verdict-wa { background: rgba(234, 179, 8, 0.2); color: #facc15; }
.verdict-tle { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
.verdict-re { background: rgba(239, 68, 68, 0.2); color: #f87171; }
.verdict-ce { background: rgba(239, 68, 68, 0.2); color: #f87171; }
.verdict-run { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }

.stat { color: #94a3b8; font-size: 0.8rem; }

.output-text, .error-text {
  margin: 0; padding: 10px; background: rgba(0, 0, 0, 0.3); border-radius: 6px;
  font-size: 0.85rem; color: #e2e8f0; white-space: pre-wrap; word-break: break-all;
  max-height: 150px; overflow: auto;
}
.error-text { color: #f87171; }

.empty-hint { color: #64748b; font-size: 0.85rem; text-align: center; padding: 20px; }

.action-bar {
  display: flex; gap: 12px; padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2); border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.run-btn, .submit-btn, .back-btn {
  padding: 10px 24px; border-radius: 8px; font-size: 0.9rem; font-weight: 600;
  cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.2s;
}
.run-btn { background: rgba(34, 197, 94, 0.2); border: 1px solid rgba(34, 197, 94, 0.4); color: #4ade80; }
.run-btn:hover:not(:disabled) { background: rgba(34, 197, 94, 0.3); }
.submit-btn { background: rgba(59, 130, 246, 0.2); border: 1px solid rgba(59, 130, 246, 0.4); color: #60a5fa; }
.submit-btn:hover:not(:disabled) { background: rgba(59, 130, 246, 0.3); }
.back-btn { background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); color: #94a3b8; margin-left: auto; }
.back-btn:hover { background: rgba(255, 255, 255, 0.1); color: #fff; }
.run-btn:disabled, .submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 1024px) {
  .ide-container { flex-direction: column; height: auto; }
  .problem-panel { width: 100%; min-width: 100%; max-height: 300px; }
  .problem-panel.collapsed { max-height: 60px; width: 100%; min-width: 100%; }
  .editor-panel { min-height: 500px; }
}

.panel-content::-webkit-scrollbar,
.io-content::-webkit-scrollbar,
.output-text::-webkit-scrollbar,
.error-text::-webkit-scrollbar { width: 6px; height: 6px; }

.panel-content::-webkit-scrollbar-track,
.io-content::-webkit-scrollbar-track,
.output-text::-webkit-scrollbar-track,
.error-text::-webkit-scrollbar-track { background: transparent; }

.panel-content::-webkit-scrollbar-thumb,
.io-content::-webkit-scrollbar-thumb,
.output-text::-webkit-scrollbar-thumb,
.error-text::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.2); border-radius: 3px; }

.panel-content::-webkit-scrollbar-thumb:hover,
.io-content::-webkit-scrollbar-thumb:hover,
.output-text::-webkit-scrollbar-thumb:hover,
.error-text::-webkit-scrollbar-thumb:hover { background: rgba(255, 255, 255, 0.3); }
</style>
