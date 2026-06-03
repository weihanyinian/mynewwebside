<script setup lang="ts">
/**
 * 全屏背景 MP4，放在 closed Shadow DOM 内，减轻 IDM 等扩展对 document 内 video 的扫描与「下载视频」浮条。
 * 无法 100% 禁止浏览器扩展，但对多数按 DOM 挂钩的下载器有效。
 *
 * 默认片名见 `src/config/homeBackgroundVideos.ts`（对应 `public/videos/` 下两个 mp4）。
 * 若本地文件缺失（404），自动回退到 MDN CC0 示例片，避免整页纯黑；也可通过 props 覆盖地址。
 */
import { onMounted, onUnmounted, ref, watch } from 'vue'
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

function resumeBothVideos() {
  for (const v of [lightVideoEl, darkVideoEl]) {
    if (!v) continue
    if (v.paused) void v.play().catch(() => {})
  }
}

function onVisibilityResume() {
  if (document.visibilityState === 'visible') resumeBothVideos()
}

let suspendHandlers: Array<{ el: HTMLVideoElement; fn: () => void }> = []

function syncTheme() {
  const el = hostRef.value
  if (!el) return
  el.setAttribute('data-theme', props.isDark ? 'dark' : 'light')
  // 暂停隐藏的视频以节省 GPU 解码资源
  const active = props.isDark ? darkVideoEl : lightVideoEl
  const hidden = props.isDark ? lightVideoEl : darkVideoEl
  if (hidden && !hidden.paused) hidden.pause()
  if (active && active.paused) void active.play().catch(() => {})
}

function mountShadow() {
  const el = hostRef.value
  if (!el || shadowRoot) return

  shadowRoot = el.attachShadow({ mode: 'closed' })

  const style = document.createElement('style')
  /* 滤镜加在包裹层而非 video 上：Chrome 等对 video 直接 filter 易导致画面不刷新、像「定格」 */
  style.textContent = `
    :host {
      display: block;
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: -2;
      pointer-events: none;
    }
    .bg-video-wrap {
      position: absolute;
      inset: 0;
      overflow: hidden;
      transition: opacity 0.5s ease;
    }
    .bg-video-wrap video {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
      transform: translateZ(0);
    }
    .light-video-wrap {
      opacity: 1;
      filter: brightness(1.06) saturate(1.12);
    }
    .dark-video-wrap {
      opacity: 0;
      filter: brightness(0.42) contrast(1.12) saturate(1.18);
    }
    :host([data-theme="dark"]) .light-video-wrap { opacity: 0; }
    :host([data-theme="dark"]) .dark-video-wrap {
      opacity: 1;
      filter: brightness(0.42) contrast(1.12) saturate(1.18);
    }
  `

  const mkVideo = (className: string, wrapClass: string, primarySrc: string, fallbackSrc: string) => {
    const wrap = document.createElement('div')
    wrap.className = wrapClass
    const v = document.createElement('video')
    v.className = className
    v.autoplay = true
    v.loop = true
    v.muted = true
    v.playsInline = true
    v.preload = 'auto'
    v.disableRemotePlayback = true
    v.setAttribute('controlsList', 'nodownload noremoteplayback nofullscreen')
    /** 少数 WebView / 省电策略下 loop 不生效，ended 后手动重头播 */
    v.addEventListener('ended', () => {
      v.currentTime = 0
      void v.play().catch(() => {})
    })
    /** 缓冲卡住时尝试恢复（与 suspend / visibility 兜底互补） */
    v.addEventListener('stalled', () => {
      window.setTimeout(() => void v.play().catch(() => {}), 1000)
    })
    v.src = primarySrc
    v.addEventListener('error', function onErr() {
      const cur = v.currentSrc || v.src || ''
      if (cur.includes('mdn.mozilla.net') || cur === fallbackSrc) return
      v.removeEventListener('error', onErr)
      v.src = fallbackSrc
      void v.load()
      void v.play().catch(() => {})
    })
    wrap.appendChild(v)
    return wrap
  }

  const lightWrap = mkVideo('light-video', 'bg-video-wrap light-video-wrap', props.lightSrc, FALLBACK_LIGHT)
  const darkWrap = mkVideo('dark-video', 'bg-video-wrap dark-video-wrap', props.darkSrc, FALLBACK_DARK)
  lightVideoEl = lightWrap.querySelector('video') as HTMLVideoElement | null
  darkVideoEl = darkWrap.querySelector('video') as HTMLVideoElement | null
  shadowRoot.append(style, lightWrap, darkWrap)
  syncTheme()
  for (const v of [lightVideoEl, darkVideoEl]) {
    if (!v) continue
    void v.play().catch(() => {
      /* 部分环境需用户手势后才可 play；静音 autoplay 通常可直接播 */
    })
    const onSuspend = () => {
      window.setTimeout(() => void v.play().catch(() => {}), 500)
    }
    v.addEventListener('suspend', onSuspend)
    suspendHandlers.push({ el: v, fn: onSuspend })
  }

  document.addEventListener('visibilitychange', onVisibilityResume)
  window.addEventListener('pageshow', resumeBothVideos)
}

onMounted(() => {
  mountShadow()
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', onVisibilityResume)
  window.removeEventListener('pageshow', resumeBothVideos)
  for (const { el, fn } of suspendHandlers) {
    el.removeEventListener('suspend', fn)
  }
  suspendHandlers = []
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
