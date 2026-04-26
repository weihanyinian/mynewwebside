<script setup lang="ts">
/**
 * OJ 题目详情页 - IDE 风格代码编辑器
 * 使用 Monaco Editor 实现 VS Code 风格的代码编辑体验
 * 支持 C/C++/Java/Python 四种语言
 */
import { computed, onMounted, onUnmounted, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as monaco from 'monaco-editor'
import { fetchOjProblem, postOjJudge, type ProblemDetail, type JudgeResult } from '../../api/oj'

// 配置 Monaco Editor Web Worker（支持多语言）
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

self.MonacoEnvironment = {
  getWorker(_: unknown, label: string) {
    if (label === 'json') {
      return new jsonWorker()
    }
    if (label === 'html' || label === 'handlebars' || label === 'razor') {
      return new htmlWorker()
    }
    if (label === 'typescript' || label === 'javascript') {
      return new tsWorker()
    }
    // C/C++/Java/Python 使用基础 editor worker
    return new editorWorker()
  }
}

// ==================== 状态定义 ====================
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const err = ref('')
const problem = ref<ProblemDetail | null>(null)

// 编辑器相关
const editorContainer = ref<HTMLDivElement | null>(null)
let editorInstance: monaco.editor.IStandaloneCodeEditor | null = null

// 当前选中的语言
const currentLang = ref<'C' | 'CPP' | 'JAVA' | 'PYTHON'>('CPP')

// 代码存储（每种语言保存一份）
const codeStore = ref<Record<string, string>>({
  C: '',
  CPP: '',
  JAVA: '',
  PYTHON: ''
})

// 自定义输入
const customStdin = ref('')
const useCustomInput = ref(false)

// 运行结果
const running = ref(false)
const result = ref<JudgeResult | null>(null)
const activeTab = ref<'stdin' | 'stdout' | 'stderr'>('stdin')

// 题目描述展开状态
const descExpanded = ref(true)

// ==================== 语言配置 ====================
const allLanguageOptions = [
  { value: 'C', label: 'C', monacoLang: 'c' },
  { value: 'CPP', label: 'C++', monacoLang: 'cpp' },
  { value: 'JAVA', label: 'Java', monacoLang: 'java' },
  { value: 'PYTHON', label: 'Python', monacoLang: 'python' }
]

// 根据题目支持的语言过滤选项
const languageOptions = computed(() => {
  if (!problem.value?.supportedLangs) return allLanguageOptions
  return allLanguageOptions.filter(opt => 
    problem.value!.supportedLangs.includes(opt.value)
  )
})

const currentMonacoLang = computed(() => {
  return languageOptions.value.find(l => l.value === currentLang.value)?.monacoLang || 'cpp'
})

// ==================== 代码模板 ====================
const codeTemplates: Record<string, string> = {
  C: `#include <stdio.h>

int main() {
    // 在这里编写你的代码
    
    return 0;
}`,
  CPP: `#include <iostream>
using namespace std;

int main() {
    // 在这里编写你的代码
    
    return 0;
}`,
  JAVA: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // 在这里编写你的代码
        
    }
}`,
  PYTHON: `# 在这里编写你的代码
def main():
    pass

if __name__ == "__main__":
    main()
`
}

// ==================== Monaco Editor 配置 ====================
function initMonacoEditor() {
  if (!editorContainer.value) return

  // 定义阿里巴巴/GitHub 风格主题
  monaco.editor.defineTheme('alibaba-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      // 关键字 - 橙红色
      { token: 'keyword', foreground: 'ff7b72', fontStyle: 'bold' },
      // 类型 - 青色
      { token: 'type', foreground: '79c0ff' },
      // 字符串 - 绿色
      { token: 'string', foreground: 'a5d6ff' },
      // 数字 - 浅蓝
      { token: 'number', foreground: 'b2d8f7' },
      // 注释 - 灰色斜体
      { token: 'comment', foreground: '8b949e', fontStyle: 'italic' },
      // 函数名 - 黄色
      { token: 'function', foreground: 'd2a8ff' },
      // 变量 - 浅灰
      { token: 'variable', foreground: 'c9d1d9' },
      // 操作符 - 红色
      { token: 'operator', foreground: 'ff7b72' },
      // 预处理指令 - 粉色
      { token: 'preprocessor', foreground: 'f97583' },
    ],
    colors: {
      'editor.background': '#1e1e1e',
      'editor.foreground': '#c9d1d9',
      'editor.lineHighlightBackground': '#264f78',
      'editor.selectionBackground': '#264f78',
      'editorCursor.foreground': '#aeafad',
      'editorLineNumber.foreground': '#6e7681',
      'editorLineNumber.activeForeground': '#c9d1d9',
      'editorIndentGuide.background': '#2d2d2d',
      'editorIndentGuide.activeBackground': '#3d3d3d',
      'editor.selectionHighlightBackground': '#3a3d4166',
      'editorBracketMatch.background': '#006400',
      'editorBracketMatch.border': '#888888',
    }
  })

  monaco.editor.defineTheme('alibaba-light', {
    base: 'vs',
    inherit: true,
    rules: [
      // 关键字 - 深蓝
      { token: 'keyword', foreground: '0000ff', fontStyle: 'bold' },
      // 类型 - 深青
      { token: 'type', foreground: '267f99' },
      // 字符串 - 深红
      { token: 'string', foreground: 'a31515' },
      // 数字 - 深蓝
      { token: 'number', foreground: '098658' },
      // 注释 - 绿色斜体
      { token: 'comment', foreground: '008000', fontStyle: 'italic' },
      // 函数名 - 紫色
      { token: 'function', foreground: '795e26' },
      // 变量 - 黑色
      { token: 'variable', foreground: '001080' },
      // 操作符 - 深灰
      { token: 'operator', foreground: '000000' },
    ],
    colors: {
      'editor.background': '#ffffff',
      'editor.foreground': '1e1e1e',
      'editor.lineHighlightBackground': '#f7f7f7',
      'editor.selectionBackground': '#add6ff',
      'editorCursor.foreground': '#000000',
      'editorLineNumber.foreground': '#999999',
      'editorLineNumber.activeForeground': '#1e1e1e',
      'editorIndentGuide.background': '#d3d3d3',
      'editorIndentGuide.activeBackground': '#939393',
    }
  })

  // 创建编辑器实例
  editorInstance = monaco.editor.create(editorContainer.value, {
    value: codeStore.value[currentLang.value] || codeTemplates[currentLang.value],
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
    guides: {
      indentation: true,
      bracketPairs: true,
      highlightActiveIndentation: true
    },
    bracketPairColorization: { enabled: true },
    suggest: {
      showKeywords: true,
      showSnippets: true,
      showClasses: true,
      showFunctions: true,
      showVariables: true,
      showConstants: true
    },
    quickSuggestions: {
      other: true,
      comments: false,
      strings: false
    },
    parameterHints: { enabled: true },
    formatOnPaste: true,
    formatOnType: true
  })

  // 监听内容变化
  editorInstance.onDidChangeModelContent(() => {
    if (editorInstance) {
      codeStore.value[currentLang.value] = editorInstance.getValue()
      // 自动保存到 localStorage
      saveCode()
    }
  })

  // 添加快捷键
  editorInstance.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => {
    runCode(false)
  })
  editorInstance.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.Enter, () => {
    runCode(true)
  })
}

// ==================== 语言切换 ====================
function switchLanguage(lang: 'C' | 'CPP' | 'JAVA' | 'PYTHON') {
  // 保存当前代码
  if (editorInstance) {
    codeStore.value[currentLang.value] = editorInstance.getValue()
  }
  
  currentLang.value = lang
  
  // 切换编辑器语言
  if (editorInstance) {
    const model = editorInstance.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, currentMonacoLang.value)
    }
    
    // 加载该语言的代码（如果为空则加载模板）
    const code = codeStore.value[lang] || codeTemplates[lang]
    editorInstance.setValue(code)
  }
}

// ==================== 加载题目 ====================
async function loadProblem() {
  const id = route.params.id as string
  if (!id) {
    err.value = '题目ID不存在'
    return
  }

  loading.value = true
  err.value = ''
  
  try {
    problem.value = await fetchOjProblem(id)
    
    // 尝试从 localStorage 恢复代码
    const savedCode = localStorage.getItem(`oj-code-${id}`)
    if (savedCode) {
      try {
        const parsed = JSON.parse(savedCode)
        codeStore.value = { ...codeStore.value, ...parsed }
      } catch {
        // 忽略解析错误
      }
    }
    
    // 加载参考解答（如果本地没有保存的代码）
    if (problem.value.referenceSolution) {
      for (const [lang, code] of Object.entries(problem.value.referenceSolution)) {
        if (code && ['C', 'CPP', 'JAVA', 'PYTHON'].includes(lang) && !codeStore.value[lang]) {
          codeStore.value[lang] = code
        }
      }
    }
    
    // 设置默认语言为题目支持的第一个语言
    if (problem.value.supportedLangs?.length > 0) {
      const firstLang = problem.value.supportedLangs[0] as 'C' | 'CPP' | 'JAVA' | 'PYTHON'
      if (['C', 'CPP', 'JAVA', 'PYTHON'].includes(firstLang)) {
        currentLang.value = firstLang
      }
    }
    
    // 等待 DOM 更新后初始化编辑器
    await nextTick()
    initMonacoEditor()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

// 保存代码到 localStorage
function saveCode() {
  if (!problem.value) return
  localStorage.setItem(`oj-code-${problem.value.id}`, JSON.stringify(codeStore.value))
}

// ==================== 运行代码 ====================
async function runCode(submit: boolean) {
  if (!problem.value || !editorInstance) return
  
  const sourceCode = editorInstance.getValue().trim()
  if (!sourceCode) {
    ElMessage.warning('请先编写代码')
    return
  }

  running.value = true
  result.value = null
  
  try {
    const stdin = useCustomInput.value ? customStdin.value : undefined
    
    result.value = await postOjJudge({
      problemId: problem.value.id,
      language: currentLang.value,
      sourceCode,
      stdin,
      submit
    })
    
    // 切换到输出标签页
    if (result.value.compileOutput) {
      activeTab.value = 'stderr'
    } else if (result.value.stdout) {
      activeTab.value = 'stdout'
    }
    
    if (result.value.verdict === 'AC') {
      ElMessage.success('Accepted! 恭喜通过！')
    } else if (result.value.verdict === 'RUN_OK') {
      ElMessage.success('运行完成')
    } else if (['CE', 'RE', 'TLE', 'WA'].includes(result.value.verdict)) {
      ElMessage.error(result.value.message || '运行失败')
    }
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    running.value = false
  }
}

// ==================== 使用样例输入 ====================
function useSampleInput(input: string) {
  customStdin.value = input
  useCustomInput.value = true
}

// ==================== 格式化代码 ====================
function formatCode() {
  if (editorInstance) {
    editorInstance.getAction('editor.action.formatDocument')?.run()
  }
}

// ==================== 重置代码 ====================
function resetCode() {
  if (editorInstance) {
    editorInstance.setValue(codeTemplates[currentLang.value])
  }
}

// ==================== 结果状态样式 ====================
function getVerdictClass(verdict: string): string {
  switch (verdict) {
    case 'AC': return 'verdict-ac'
    case 'WA': return 'verdict-wa'
    case 'TLE': return 'verdict-tle'
    case 'RE': return 'verdict-re'
    case 'CE': return 'verdict-ce'
    case 'RUN_OK': return 'verdict-run'
    default: return ''
  }
}

function getVerdictText(verdict: string): string {
  switch (verdict) {
    case 'AC': return 'Accepted'
    case 'WA': return 'Wrong Answer'
    case 'TLE': return 'Time Limit Exceeded'
    case 'RE': return 'Runtime Error'
    case 'CE': return 'Compile Error'
    case 'RUN_OK': return '运行成功'
    case 'PENDING': return '运行中...'
    default: return verdict
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadProblem()
})

onUnmounted(() => {
  if (editorInstance) {
    editorInstance.dispose()
    editorInstance = null
  }
})

// 监听路由变化 - 切换题目时重新加载
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    // 销毁旧编辑器
    if (editorInstance) {
      editorInstance.dispose()
      editorInstance = null
    }
    // 重置状态
    result.value = null
    codeStore.value = { C: '', CPP: '', JAVA: '', PYTHON: '' }
    // 重新加载
    loadProblem()
  }
})
</script>

<template>
  <div class="oj-detail">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="err" class="error-state">
      <p class="err-msg">{{ err }}</p>
      <button class="site-pill" @click="router.push('/tools/oj')">返回题目列表</button>
    </div>

    <!-- 主内容 -->
    <template v-else-if="problem">
      <div class="ide-container">
        <!-- 左侧：题目描述 -->
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
            <!-- 题目描述 -->
            <section class="section">
              <h3>题目描述</h3>
              <div class="desc-text">{{ problem.description }}</div>
            </section>

            <!-- 输入输出说明 -->
            <section class="section">
              <h3>输入格式</h3>
              <p class="desc-text">{{ problem.inputDesc }}</p>
            </section>

            <section class="section">
              <h3>输出格式</h3>
              <p class="desc-text">{{ problem.outputDesc }}</p>
            </section>

            <!-- 样例 -->
            <section class="section">
              <h3>样例</h3>
              <div v-for="(sample, idx) in problem.samples" :key="idx" class="sample-block">
                <div class="sample-header">
                  <span>样例 {{ idx + 1 }}</span>
                  <button class="use-sample-btn" @click="useSampleInput(sample.input)">
                    使用此输入
                  </button>
                </div>
                <div class="sample-io">
                  <div class="sample-input">
                    <label>输入</label>
                    <pre>{{ sample.input }}</pre>
                  </div>
                  <div class="sample-output">
                    <label>输出</label>
                    <pre>{{ sample.output }}</pre>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </div>

        <!-- 右侧：代码编辑器 -->
        <div class="editor-panel">
          <!-- 工具栏 -->
          <div class="editor-toolbar">
            <div class="lang-selector">
              <button
                v-for="lang in languageOptions"
                :key="lang.value"
                class="lang-btn"
                :class="{ active: currentLang === lang.value }"
                @click="switchLanguage(lang.value as any)"
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

          <!-- Monaco Editor 容器 -->
          <div ref="editorContainer" class="monaco-container"></div>

          <!-- 底部面板：输入/输出 -->
          <div class="io-panel">
            <div class="io-tabs">
              <button
                class="io-tab"
                :class="{ active: activeTab === 'stdin' }"
                @click="activeTab = 'stdin'"
              >
                输入
              </button>
              <button
                class="io-tab"
                :class="{ active: activeTab === 'stdout' }"
                @click="activeTab = 'stdout'"
              >
                输出
              </button>
              <button
                class="io-tab"
                :class="{ active: activeTab === 'stderr' }"
                @click="activeTab = 'stderr'"
              >
                编译信息
              </button>
              
              <div class="io-actions">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="useCustomInput" />
                  自定义输入
                </label>
              </div>
            </div>

            <div class="io-content">
              <!-- 输入面板 -->
              <div v-show="activeTab === 'stdin'" class="stdin-area">
                <textarea
                  v-model="customStdin"
                  placeholder="在此输入测试数据..."
                  :disabled="!useCustomInput"
                ></textarea>
                <p v-if="!useCustomInput" class="hint">
                  未启用自定义输入时，将使用题目第一个样例作为输入
                </p>
              </div>

              <!-- 输出面板 -->
              <div v-show="activeTab === 'stdout'" class="stdout-area">
                <template v-if="result">
                  <div class="result-header">
                    <span class="verdict" :class="getVerdictClass(result.verdict)">
                      {{ getVerdictText(result.verdict) }}
                    </span>
                    <span v-if="result.timeSeconds" class="stat">
                      ⏱ {{ result.timeSeconds.toFixed(3) }}s
                    </span>
                    <span v-if="result.memoryKb" class="stat">
                      💾 {{ (result.memoryKb / 1024).toFixed(2) }}MB
                    </span>
                  </div>
                  <pre v-if="result.stdout" class="output-text">{{ result.stdout }}</pre>
                  <p v-else class="empty-hint">无输出</p>
                </template>
                <p v-else class="empty-hint">运行代码后查看输出</p>
              </div>

              <!-- 编译信息面板 -->
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

          <!-- 操作按钮 -->
          <div class="action-bar">
            <button
              class="run-btn"
              :disabled="running"
              @click="runCode(false)"
            >
              <span class="icon">▶</span> 运行
            </button>
            <button
              class="submit-btn"
              :disabled="running"
              @click="runCode(true)"
            >
              <span class="icon">📤</span> 提交
            </button>
            <button class="back-btn" @click="router.push('/tools/oj')">
              返回列表
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* ==================== 主容器 ==================== */
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

.err-msg {
  color: #f87171;
  margin-bottom: 16px;
}

/* ==================== IDE 布局 ==================== */
.ide-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 220px);
  min-height: 600px;
}

/* ==================== 题目面板 ==================== */
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

.problem-panel.collapsed {
  width: 60px;
  min-width: 60px;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
}

.problem-title {
  margin: 0 0 12px;
  font-size: 1.25rem;
  font-weight: 700;
  color: #fff;
}

.problem-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge.difficulty {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
}

.badge.difficulty.入门 { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
.badge.difficulty.简单 { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }
.badge.difficulty.中等 { background: rgba(234, 179, 8, 0.2); color: #facc15; }
.badge.difficulty.困难 { background: rgba(239, 68, 68, 0.2); color: #f87171; }

.badge.mode {
  background: rgba(168, 85, 247, 0.2);
  color: #c084fc;
}

.badge.limit {
  background: rgba(100, 116, 139, 0.2);
  color: #94a3b8;
}

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

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.section {
  margin-bottom: 20px;
}

.section h3 {
  margin: 0 0 8px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.desc-text {
  color: #e2e8f0;
  line-height: 1.7;
  white-space: pre-wrap;
  font-size: 0.9rem;
}

/* 样例块 */
.sample-block {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.sample-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.2);
  font-size: 0.8rem;
  color: #94a3b8;
}

.use-sample-btn {
  background: rgba(59, 130, 246, 0.2);
  border: none;
  color: #60a5fa;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.75rem;
  transition: all 0.2s;
}

.use-sample-btn:hover {
  background: rgba(59, 130, 246, 0.3);
}

.sample-io {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.sample-input,
.sample-output {
  padding: 12px;
}

.sample-input label,
.sample-output label {
  display: block;
  font-size: 0.75rem;
  color: #64748b;
  margin-bottom: 4px;
}

.sample-io pre {
  margin: 0;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.85rem;
  color: #e2e8f0;
  white-space: pre-wrap;
  word-break: break-all;
}

/* ==================== 编辑器面板 ==================== */
.editor-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(30, 30, 30, 0.95);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

/* 工具栏 */
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.lang-selector {
  display: flex;
  gap: 4px;
}

.lang-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #94a3b8;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.2s;
}

.lang-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.lang-btn.active {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.5);
  color: #60a5fa;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #94a3b8;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* Monaco 容器 */
.monaco-container {
  flex: 1;
  min-height: 300px;
}

/* ==================== 输入输出面板 ==================== */
.io-panel {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
}

.io-tabs {
  display: flex;
  align-items: center;
  padding: 0 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.1);
}

.io-tab {
  background: transparent;
  border: none;
  color: #64748b;
  padding: 10px 16px;
  cursor: pointer;
  font-size: 0.85rem;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.io-tab:hover {
  color: #94a3b8;
}

.io-tab.active {
  color: #60a5fa;
  border-bottom-color: #60a5fa;
}

.io-actions {
  margin-left: auto;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
  font-size: 0.8rem;
  cursor: pointer;
}

.io-content {
  padding: 12px;
  min-height: 120px;
  max-height: 200px;
  overflow: auto;
}

.stdin-area textarea {
  width: 100%;
  min-height: 80px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  color: #e2e8f0;
  padding: 10px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.85rem;
  resize: vertical;
}

.stdin-area textarea:focus {
  outline: none;
  border-color: rgba(59, 130, 246, 0.5);
}

.stdin-area textarea:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.hint {
  color: #64748b;
  font-size: 0.8rem;
  margin-top: 8px;
}

.stdout-area,
.stderr-area {
  font-family: 'JetBrains Mono', monospace;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.verdict {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
}

.verdict-ac { background: rgba(34, 197, 94, 0.2); color: #4ade80; }
.verdict-wa { background: rgba(234, 179, 8, 0.2); color: #facc15; }
.verdict-tle { background: rgba(249, 115, 22, 0.2); color: #fb923c; }
.verdict-re { background: rgba(239, 68, 68, 0.2); color: #f87171; }
.verdict-ce { background: rgba(239, 68, 68, 0.2); color: #f87171; }
.verdict-run { background: rgba(59, 130, 246, 0.2); color: #60a5fa; }

.stat {
  color: #94a3b8;
  font-size: 0.8rem;
}

.output-text,
.error-text {
  margin: 0;
  padding: 10px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  font-size: 0.85rem;
  color: #e2e8f0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 150px;
  overflow: auto;
}

.error-text {
  color: #f87171;
}

.empty-hint {
  color: #64748b;
  font-size: 0.85rem;
  text-align: center;
  padding: 20px;
}

/* ==================== 操作按钮 ==================== */
.action-bar {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.run-btn,
.submit-btn,
.back-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.run-btn {
  background: rgba(34, 197, 94, 0.2);
  border: 1px solid rgba(34, 197, 94, 0.4);
  color: #4ade80;
}

.run-btn:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.3);
}

.submit-btn {
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
  color: #60a5fa;
}

.submit-btn:hover:not(:disabled) {
  background: rgba(59, 130, 246, 0.3);
}

.back-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #94a3b8;
  margin-left: auto;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.run-btn:disabled,
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1024px) {
  .ide-container {
    flex-direction: column;
    height: auto;
  }

  .problem-panel {
    width: 100%;
    min-width: 100%;
    max-height: 300px;
  }

  .problem-panel.collapsed {
    max-height: 60px;
    width: 100%;
    min-width: 100%;
  }

  .editor-panel {
    min-height: 500px;
  }
}

/* ==================== 滚动条样式 ==================== */
.panel-content::-webkit-scrollbar,
.io-content::-webkit-scrollbar,
.output-text::-webkit-scrollbar,
.error-text::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.panel-content::-webkit-scrollbar-track,
.io-content::-webkit-scrollbar-track,
.output-text::-webkit-scrollbar-track,
.error-text::-webkit-scrollbar-track {
  background: transparent;
}

.panel-content::-webkit-scrollbar-thumb,
.io-content::-webkit-scrollbar-thumb,
.output-text::-webkit-scrollbar-thumb,
.error-text::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.panel-content::-webkit-scrollbar-thumb:hover,
.io-content::-webkit-scrollbar-thumb:hover,
.output-text::-webkit-scrollbar-thumb:hover,
.error-text::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
