export const HOME_SECTION_IDS = ['about', 'works', 'blog', 'contact', 'message', 'tools'] as const

export type HomeSectionId = (typeof HOME_SECTION_IDS)[number]

export type HomeWorkItem = {
  title: string
  desc: string
  detail: string
  tag: string
  link: string
  cover: string
}
