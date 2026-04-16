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
  const res = await http.post<ApiResponse<JudgeResult>>('/api/oj/judge', body, { timeout: 120_000 })
  return res.data.data
}
