<script setup lang="ts">
/**
 * 全屏背景 MP4，放在 closed Shadow DOM 内，减轻 IDM 等扩展对 document 内 video 的扫描与「下载视频」浮条。
 * 无法 100% 禁止浏览器扩展，但对多数按 DOM 挂钩的下载器有效。
 *
 * 默认片名见 `src/config/homeBackgroundVideos.ts`（对应 `public/videos/` 下两个 mp4）。
 * 若本地文件缺失（404），自动回退到 MDN CC0 示例片，避免整页纯黑；也可通过 props 覆盖地址。
 */
import { onMounted, ref, watch } from 'vue'
import { HOME_BG_LIGHT_SRC, HOME_BG_DARK_SRC } from '../../config/homeBackgroundVideos'

/** 仅作本地源加载失败时的兜底，不是你的正式素材 */
const FALLBACK_LIGHT =
  'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4'
const FALLBACK_DARK = FALLBACK_LIGHT

const props = withDefaults(
  defineProps<{
    isDark: boolean
    lightSrc?: string
    darkSrc?: string
  }>(),
  {
    lightSrc: HOME_BG_LIGHT_SRC,
    darkSrc: HOME_BG_DARK_SRC,
  },
)

const hostRef = ref<HTMLElement | null>(null)
let shadowRoot: ShadowRoot | null = null
let lightVideoEl: HTMLVideoElement | null = null
let darkVideoEl: HTMLVideoElement | null = null

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
      transition: opacity 0.5s ease, transform 0.6s ease-out, filter 0.55s ease;
      will-change: transform, opacity;
    }
    .light-video {
      opacity: 1;
      filter: brightness(1.06) saturate(1.12);
    }
    .dark-video {
      opacity: 0;
      filter: brightness(0.42) contrast(1.12) saturate(1.18);
    }
    :host([data-theme="dark"]) .light-video { opacity: 0; }
    :host([data-theme="dark"]) .dark-video {
      opacity: 1;
      filter: brightness(0.42) contrast(1.12) saturate(1.18);
    }
  `

  const mkVideo = (className: string, primarySrc: string, fallbackSrc: string) => {
    const v = document.createElement('video')
    v.className = className
    v.autoplay = true
    v.loop = true
    v.muted = true
    v.playsInline = true
    v.preload = 'metadata'
    v.disableRemotePlayback = true
    v.setAttribute('controlsList', 'nodownload noremoteplayback nofullscreen')
    v.src = primarySrc
    v.addEventListener('error', function onErr() {
      const cur = v.currentSrc || v.src || ''
      if (cur.includes('mdn.mozilla.net') || cur === fallbackSrc) return
      v.removeEventListener('error', onErr)
      v.src = fallbackSrc
      void v.load()
      void v.play().catch(() => {})
    })
    return v
  }

  const light = mkVideo('light-video', props.lightSrc, FALLBACK_LIGHT)
  const dark = mkVideo('dark-video', props.darkSrc, FALLBACK_DARK)
  lightVideoEl = light
  darkVideoEl = dark
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

function applyVideoPrimary(el: HTMLVideoElement | null, primary: string) {
  if (!el) return
  if (el.dataset.primarySrc === primary) return
  el.dataset.primarySrc = primary
  el.src = primary
  void el.load()
  void el.play().catch(() => {})
}

watch(
  () => [props.lightSrc, props.darkSrc] as const,
  ([light, dark]) => {
    applyVideoPrimary(lightVideoEl, light)
    applyVideoPrimary(darkVideoEl, dark)
  },
)
</script>

<template>
  <!-- 布局由 Shadow 内 :host 负责，避免 light DOM 样式与 fixed 全屏冲突 -->
  <div ref="hostRef" aria-hidden="true" />
</template>
