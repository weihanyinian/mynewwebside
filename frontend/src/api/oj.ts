import type { ApiResponse } from './http'
import { http } from './http'

export type ProblemListItem = {
  id: string
  title: string
  difficulty: string
  judgeMode: string
}

export type ProblemDetail = {
  id: string
  title: string
  difficulty: string
  judgeMode: string
  description: string
  inputDesc: string
  outputDesc: string
  timeLimitSec: number
  memoryLimitMb: number
  supportedLangs: string[]
  samples: { input: string; output: string }[]
  referenceSolution: Record<string, string>
}

export type JudgeResult = {
  submitted: boolean
  verdict: string
  message: string
  timeSeconds: number | null
  memoryKb: number | null
  stdout: string
  stderr: string
  compileOutput: string
}

export type JudgeRequestBody = {
  problemId: string
  language: string
  sourceCode: string
  stdin: string | null
  submit: boolean
}

export async function fetchOjProblems(params?: { q?: string; difficulty?: string }) {
  const res = await http.get<ApiResponse<ProblemListItem[]>>('/api/oj/problems', {
    params: { q: params?.q, difficulty: params?.difficulty },
  })
  return res.data.data
}

export async function fetchOjProblem(id: string) {
  const res = await http.get<ApiResponse<ProblemDetail>>(`/api/oj/problems/${id}`)
  return res.data.data
}

export async function postOjJudge(body: JudgeRequestBody) {
  // 异步提交，返回 taskId（HTTP 202）
  const res = await http.post<ApiResponse<{ taskId: string }>>('/api/oj/judge', body, { timeout: 30_000 })
  const taskId = res.data.data.taskId

  // 前端轮询结果（每 1.5 秒一次，最多 60 次 = 90 秒）
  const maxAttempts = 60
  const intervalMs = 1500
  for (let i = 0; i < maxAttempts; i++) {
    await new Promise(r => setTimeout(r, intervalMs))
    const pollRes = await http.get<ApiResponse<JudgeResult | null>>(`/api/oj/judge/${taskId}`, { timeout: 10_000 })
    const result = pollRes.data.data
    if (result !== null) {
      return result
    }
  }
  throw new Error('判题超时，请稍后在提交记录中查看结果')
}

export type OjSubmissionRow = {
  id: number
  username: string
  problemId: string
  language: string
  submitted: boolean
  verdict: string
  message: string
  timeSeconds: number | null
  memoryKb: number | null
  createdAt: string
}

export type OjSubmissionPage = {
  items: OjSubmissionRow[]
  total: number
  page: number
  size: number
}

export async function fetchMyOjSubmissions(params?: { page?: number; size?: number }) {
  const res = await http.get<ApiResponse<OjSubmissionPage>>('/api/oj/submissions/mine', { params })
  return res.data.data
}
