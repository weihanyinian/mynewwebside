export type WallMessageItem = {
  id: number
  nickname: string
  content: string
  adminReply: string | null
  createdAt: string
}

export type WallComposerForm = {
  nickname: string
  content: string
}
