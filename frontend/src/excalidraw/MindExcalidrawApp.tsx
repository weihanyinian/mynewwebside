/** @jsxImportSource react */
import { useCallback, useEffect, useMemo, useState, type ReactElement } from 'react'
import { Excalidraw } from '@excalidraw/excalidraw'
import '@excalidraw/excalidraw/index.css'

/** 与 Excalidraw 交互的句柄（类型由官方包推断，此处放宽便于 Vue 侧持有） */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type ExcalidrawApi = any

export interface MindExcalidrawAppProps {
  theme: 'light' | 'dark'
  initialData: Record<string, unknown> | null
  onReady: (api: ExcalidrawApi) => void
}

export function MindExcalidrawApp({ theme, initialData, onReady }: MindExcalidrawAppProps): ReactElement {
  const [api, setApi] = useState<ExcalidrawApi | null>(null)

  const handleApi = useCallback(
    (a: ExcalidrawApi) => {
      setApi(a)
      onReady(a)
    },
    [onReady],
  )

  const data = useMemo(() => {
    if (initialData && typeof initialData === 'object' && 'elements' in initialData) {
      return initialData
    }
    return { elements: [], appState: { theme, viewBackgroundColor: theme === 'dark' ? '#121212' : '#ffffff' } }
  }, [initialData, theme])

  useEffect(() => {
    if (!api) return
    try {
      const appState = api.getAppState?.()
      if (appState && appState.theme !== theme) {
        api.updateScene?.({ appState: { ...appState, theme } })
      }
    } catch {
      /* ignore */
    }
  }, [theme, api])

  return (
    <div className="mind-excalidraw-root" style={{ height: '100%', width: '100%' }}>
      <Excalidraw excalidrawAPI={handleApi} initialData={data as never} theme={theme} />
    </div>
  )
}
