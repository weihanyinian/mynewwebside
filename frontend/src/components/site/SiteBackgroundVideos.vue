<script setup lang="ts">
/**
 * 全屏背景 MP4，放在 closed Shadow DOM 内，减轻 IDM 等扩展对 document 内 video 的扫描与「下载视频」浮条。
 * 无法 100% 禁止浏览器扩展，但对多数按 DOM 挂钩的下载器有效。
 *
 * 默认使用 MDN CC0 示例片（仓库未提交大体积 mp4）。若需自有素材：将 light.mp4 / dark.mp4 放到
 * `public/videos/` 并在父组件传入 :light-src="/videos/light.mp4" :dark-src="/videos/dark.mp4"。
 */
import { onMounted, ref, watch } from 'vue'

/** 公网可直连的占位片，避免本地未放置 public/videos/*.mp4 时背景全黑 */
const DEFAULT_LIGHT =
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4'
const DEFAULT_DARK =
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4'

const props = withDefaults(
  defineProps<{
    isDark: boolean
    lightSrc?: string
    darkSrc?: string
  }>(),
  {
    lightSrc: DEFAULT_LIGHT,
    darkSrc: DEFAULT_DARK,
  },
)

const hostRef = ref<HTMLElement | null>(null)
let shadowRoot: ShadowRoot | null = null

function syncTheme() {
  const el = hostRef.value
  if (!el) return
  el.setAttribute('data-theme', props.isDark ? 'dark' : 'light')
}

function mountShadow() {
  const el = hostRef.value
  if (!el || shadowRoot) return

  shadowRoot = el.attachShadow({ mode: 'closed' })

  const style = document.createElement('style')
  style.textContent = `
    :host {
      display: block;
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      z-index: -2;
      pointer-events: none;
    }
    video {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: opacity 0.5s ease, transform 0.6s ease-out;
      will-change: transform, opacity;
    }
    .light-video { opacity: 1; }
    .dark-video { opacity: 0; }
    :host([data-theme="dark"]) .light-video { opacity: 0; }
    :host([data-theme="dark"]) .dark-video { opacity: 1; }
  `

  const mkVideo = (className: string, src: string) => {
    const v = document.createElement('video')
    v.className = className
    v.autoplay = true
    v.loop = true
    v.muted = true
    v.playsInline = true
    v.preload = 'metadata'
    v.disableRemotePlayback = true
    v.setAttribute('controlsList', 'nodownload noremoteplayback nofullscreen')
    v.src = src
    return v
  }

  const light = mkVideo('light-video', props.lightSrc)
  const dark = mkVideo('dark-video', props.darkSrc)
  shadowRoot.append(style, light, dark)
  syncTheme()
  for (const v of [light, dark]) {
    void v.play().catch(() => {
      /* 部分环境需用户手势后才可 play；静音 autoplay 通常可直接播 */
    })
  }
}

onMounted(() => {
  mountShadow()
})

watch(
  () => props.isDark,
  () => syncTheme(),
)
</script>

<template>
  <!-- 布局由 Shadow 内 :host 负责，避免 light DOM 样式与 fixed 全屏冲突 -->
  <div ref="hostRef" aria-hidden="true" />
</template>
