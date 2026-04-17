/** 仅允许站内相对路径，避免开放重定向 */
export function getSafeInternalPath(raw: unknown): string | null {
  if (typeof raw !== 'string') return null
  const s = raw.trim()
  if (!s.startsWith('/') || s.startsWith('//')) return null
  if (s.includes('://')) return null
  return s
}
