import { http, type ApiResponse } from './http'

type CompanionResp = { reply: string }

export async function chatWithCompanion(message: string): Promise<string> {
  const { data } = await http.post<ApiResponse<CompanionResp>>('/api/public/ai/companion/chat', { message })
  return data.data.reply
}
