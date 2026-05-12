<script setup lang="ts">
/**
 * 全局迷你播放器：对接后端播放地址 / 歌词；与 musicPlayerStore、音乐中心联动。
 */
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import QqCookieGuide from './music/QqCookieGuide.vue'
import { useMusicPlayerStore } from '../stores/musicPlayer'
import { useUserStore } from '../stores/user'
import { useThemeStore } from '../stores/theme'
import { neteaseLoginWithCookie, neteaseLogout, qqLoginCookie, qqLogout } from '../api/musicApi'
// ncm 扫码走独立 axios（withCredentials），与 Login.vue 一致
// @ts-expect-error api.js 无 TS 声明，与 vue-tsc 兼容
import { ncmApi } from '../api'
import { activeBilingualLrcIndex, parseBilingualLrc, type BilingualLrcLine } from '../utils/lrc'

const PLAYER_Z_INDEX = 1100
const OFFSET_LEFT = '24px'
const OFFSET_TOP = '84px'

const GLASS = {
  bg: 'rgba(255, 255, 255, 0.2)',
  blur: '10px',
  border: '1px solid rgba(255, 255, 255, 0.5)',
  shadow: '0 8px 32px rgba(102, 217, 255, 0.2)',
  radius: '16px',
} as const

const music = useMusicPlayerStore()
const userStore = useUserStore()
const themeStore = useThemeStore()
const { currentTrack, resolvedUrl, loadError, urlLoading } = storeToRefs(music)

const audioRef = ref<HTMLAudioElement | null>(null)
const isExpanded = ref(false)
const isPlaying = ref(false)
const isSeeking = ref(false)
const isLoading = computed(() => urlLoading.value)
const currentTime = ref(0)
const duration = ref(0)

const lyricBoxRef = ref<HTMLElement | null>(null)
const authBoxRef = ref<HTMLElement | null>(null)

/** 绑定面板当前平台：与顶部两个登录按钮对应，互不混排 */
const authPanelTab = ref<'netease' | 'qq'>('netease')
const lrcLines = computed<BilingualLrcLine[]>(() => parseBilingualLrc(music.lyricLrc || '', music.lyricTLrc || ''))
const activeLine = computed(() => activeBilingualLrcIndex(lrcLines.value, currentTime.value))

const isDarkMode = computed(() => themeStore.isDarkMode)

const displayTitle = computed(() => {
  const t = currentTrack.value
  if (!t) return '暂无曲目'
  return t.artist ? `${t.artist} - ${t.title}` : t.title
})

const sourceLabel = computed(() => {
  if (music.source === 'library') return '📚 音乐库'
  if (music.source === 'playlist') {
    if (music.dailyHotPlatform === 'netease') return '🔥 热歌 · 网易云'
    if (music.dailyHotPlatform === 'qq') return '🔥 热歌 · QQ'
    return '📋 歌单'
  }
  return '🎲 随机'
})

const modeLabel = computed(() =>
  music.playMode === 'loop_one' ? '🔂' : music.playMode === 'shuffle' ? '🔀' : '🔁',
)

const progressFillStr = computed(() =>
  duration.value > 0 ? `${(currentTime.value / duration.value) * 100}%` : '0%',
)
const volumeFillStr = computed(() => `${music.volume * 100}%`)
const authPanelOpen = ref(false)
const neteaseLoginLoading = ref(false)
const neteaseAuthMsg = ref('')
const neteaseAuthErr = ref('')
const qrImg = ref('')
const qrUnikey = ref('')
const qrStatus = ref('')
const qrPolling = ref(false)
let qrPollTimer: ReturnType<typeof setInterval> | null = null

const neteaseLabel = computed(() =>
  music.neteaseBound ? `🎵 ${music.neteaseNickname || '已绑定'}` : '🎵 网易云登录',
)

const qqLoginLabel = computed(() =>
  music.qqBound ? `🎼 ${music.qqNickname || 'QQ已绑定'}` : '🎼 QQ音乐登录',
)

const qqCookieInput = ref('')
const qqAuthErr = ref('')
const qqAuthMsg = ref('')
const qqLoginLoading = ref(false)

function pickNcmUnikey(inner: unknown): string {
  const o = inner as { data?: { unikey?: string }; unikey?: string }
  const d = o?.data ?? o
  if (d && typeof d === 'object') {
    const u = d as { unikey?: string; data?: { unikey?: string } }
    if (u.unikey) return u.unikey
    if (u.data?.unikey) return u.data.unikey
  }
  return ''
}

function stopQrPoll() {
  if (qrPollTimer != null) {
    clearInterval(qrPollTimer)
    qrPollTimer = null
  }
  qrPolling.value = false
}

async function refreshNeteaseQrCode() {
  if (!userStore.isLoggedIn) {
    neteaseAuthErr.value = '请先登录本站账号，再绑定网易云账号'
    return
  }
  stopQrPoll()
  qrImg.value = ''
  qrUnikey.value = ''
  qrStatus.value = '正在获取二维码…'
  neteaseAuthErr.value = ''
  neteaseAuthMsg.value = ''
  try {
    const keyResp = await ncmApi.qrLoginKey()
    const innerKey = keyResp.data?.data
    const unikey = pickNcmUnikey(innerKey)
    if (!unikey) {
      throw new Error('未获取到二维码 key')
    }
    qrUnikey.value = unikey
    const createResp = await ncmApi.qrLoginCreate(unikey, true)
    const payload = createResp.data?.data as { data?: { qrimg?: string }; qrimg?: string } | undefined
    const img = payload?.data?.qrimg ?? payload?.qrimg
    if (!img) {
      throw new Error('未返回二维码图片')
    }
    qrImg.value = img
    qrStatus.value = '请使用网易云音乐 App 扫码'
    qrPolling.value = true
    qrPollTimer = setInterval(async () => {
      if (!qrUnikey.value) return
      try {
        const checkResp = await ncmApi.qrLoginCheck(qrUnikey.value)
        const body = checkResp.data?.data as
          | { code?: number; message?: string; cookie?: string }
          | undefined
        const code = body?.code
        if (code === 800) {
          qrStatus.value = '二维码已过期，请点击刷新'
          stopQrPoll()
          return
        }
        if (code === 801) {
          qrStatus.value = '等待扫码…'
          return
        }
        if (code === 802) {
          qrStatus.value = '请在手机上确认登录'
          return
        }
        if (code === 803) {
          stopQrPoll()
          const ck = body?.cookie
          if (!ck || !String(ck).trim()) {
            throw new Error('登录成功但未返回 Cookie，请重试')
          }
          neteaseLoginLoading.value = true
          try {
            await neteaseLoginWithCookie(String(ck).trim())
            await music.refreshNeteaseStatus()
            neteaseAuthMsg.value = music.neteaseNickname
              ? `绑定成功：${music.neteaseNickname}`
              : '绑定成功'
            qrStatus.value = '绑定成功'
          } finally {
            neteaseLoginLoading.value = false
          }
          return
        }
        qrStatus.value = body?.message || `状态码 ${code ?? '未知'}`
      } catch (e: unknown) {
        stopQrPoll()
        neteaseAuthErr.value = e instanceof Error ? e.message : '轮询失败'
      }
    }, 2000)
  } catch (e: unknown) {
    qrStatus.value = ''
    neteaseAuthErr.value = e instanceof Error ? e.message : '获取二维码失败'
  }
}

watch(authPanelOpen, (open) => {
  if (!open) {
    stopQrPoll()
  }
})

// ---------- 音源切换 ----------
function toggleSource() {
  if (music.source === 'random') {
    void music.loadDefaultPlaylist()
  } else {
    music.source = 'random'
    music.playlistTracks = []
    music.queue = []
    music.currentIndex = -1
  }
}

/** 网易云：再次点击（且当前为网易标签）收起绑定面板 */
function onNeteaseLoginClick() {
  if (authPanelOpen.value && authPanelTab.value === 'netease') {
    authPanelOpen.value = false
    stopQrPoll()
    return
  }
  authPanelTab.value = 'netease'
  authPanelOpen.value = true
  neteaseAuthErr.value = ''
  neteaseAuthMsg.value = ''
  void nextTick(() => {
    authBoxRef.value?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  })
}

/** QQ：再次点击（且当前为 QQ 标签）收起绑定面板 */
function onQqLoginClick() {
  if (authPanelOpen.value && authPanelTab.value === 'qq') {
    authPanelOpen.value = false
    return
  }
  stopQrPoll()
  authPanelTab.value = 'qq'
  authPanelOpen.value = true
  neteaseAuthErr.value = ''
  neteaseAuthMsg.value = ''
  void nextTick(() => {
    authBoxRef.value?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  })
}

async function submitQqCookie() {
  if (!userStore.isLoggedIn) {
    qqAuthErr.value = '请先登录本站账号'
    return
  }
  const ck = qqCookieInput.value.trim()
  if (!ck) {
    qqAuthErr.value = '请粘贴 Cookie'
    return
  }
  qqLoginLoading.value = true
  qqAuthErr.value = ''
  qqAuthMsg.value = ''
  try {
    await qqLoginCookie(ck)
    await music.refreshNeteaseStatus()
    qqAuthMsg.value = music.qqNickname ? `QQ 音乐已绑定：${music.qqNickname}` : 'QQ 音乐已绑定'
    qqCookieInput.value = ''
  } catch (e: unknown) {
    qqAuthErr.value = e instanceof Error ? e.message : '绑定失败'
  } finally {
    qqLoginLoading.value = false
  }
}

async function submitQqLogout() {
  qqLoginLoading.value = true
  qqAuthErr.value = ''
  qqAuthMsg.value = ''
  try {
    await qqLogout()
    await music.refreshNeteaseStatus()
    qqAuthMsg.value = '已解绑 QQ 音乐'
  } catch (e: unknown) {
    qqAuthErr.value = e instanceof Error ? e.message : '解绑失败'
  } finally {
    qqLoginLoading.value = false
  }
}

async function submitNeteaseLogout() {
  neteaseLoginLoading.value = true
  neteaseAuthErr.value = ''
  neteaseAuthMsg.value = ''
  try {
    await neteaseLogout()
    await music.refreshNeteaseStatus()
    neteaseAuthMsg.value = '已解绑网易云账号'
  } catch (e: unknown) {
    neteaseAuthErr.value = e instanceof Error ? e.message : '解绑失败，请稍后重试'
  } finally {
    neteaseLoginLoading.value = false
  }
}

function formatTime(sec: number) {
  if (!Number.isFinite(sec) || sec < 0) return '0:00'
  const s = Math.floor(sec % 60)
  const m = Math.floor(sec / 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

watch(
  () => music.resolvedUrl,
  async (url) => {
    await nextTick()
    const el = audioRef.value
    if (!el || !url) return
    el.src = url
    try {
      await el.play()
    } catch {
      isPlaying.value = false
    }
  },
)

watch(
  () => music.currentTrack,
  () => {
    duration.value = 0
    currentTime.value = 0
  },
)

watch(
  () => activeLine.value,
  (idx) => {
    if (idx < 0 || !lyricBoxRef.value) return
    const el = lyricBoxRef.value.querySelector(`[data-lrc-line="${idx}"]`) as HTMLElement | null
    el?.scrollIntoView({ block: 'center', behavior: 'smooth' })
  },
)

async function togglePlay() {
  const el = audioRef.value
  if (!el) return
  if (!currentTrack.value) {
    await music.refreshNeteaseStatus()
    if (music.source === 'playlist' && !music.playlistLoaded) {
      await music.loadDefaultPlaylist()
    } else if (music.source === 'playlist' && music.queue.length > 0) {
      await music.loadCurrentUrl()
    } else {
      await music.playRandom()
    }
    return
  }
  if (!resolvedUrl.value && !loadError.value) {
    await music.loadCurrentUrl()
    return
  }
  if (loadError.value) {
    await music.loadCurrentUrl()
    return
  }
  if (!resolvedUrl.value) {
    return
  }
  if (isPlaying.value) {
    el.pause()
  } else {
    try {
      await el.play()
    } catch {
      isPlaying.value = false
    }
  }
}

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

function collapse() {
  isExpanded.value = false
  authPanelOpen.value = false
  stopQrPoll()
}

function cyclePlayMode() {
  music.cyclePlayMode()
}

async function playPrev() {
  if (music.source === 'random') return
  await music.playPrev({
    shuffle: music.playMode === 'shuffle',
  })
}

async function playNext() {
  if (music.playMode === 'loop_one') {
    await music.playNext({ loopOne: true })
    return
  }
  await music.playNext({
    shuffle: music.playMode === 'shuffle',
  })
}

function onEnded() {
  if (music.playMode === 'loop_one') {
    const el = audioRef.value
    if (el) {
      el.currentTime = 0
      void el.play()
    }
    return
  }
  void playNext()
}

function onTimeUpdate() {
  const el = audioRef.value
  if (!el || isSeeking.value) return
  currentTime.value = el.currentTime
}

function onLoadedMetadata() {
  const el = audioRef.value
  if (!el) return
  duration.value = el.duration || 0
}

function onPlay() {
  isPlaying.value = true
}

function onPause() {
  isPlaying.value = false
}

function applyVolume() {
  const el = audioRef.value
  if (el) el.volume = music.volume
}

function onVolumeInput(e: Event) {
  music.setVolume(Number((e.target as HTMLInputElement).value))
  applyVolume()
}

function onSeekStart() {
  isSeeking.value = true
}

function onSeekInput(e: Event) {
  const el = audioRef.value
  if (!el) return
  const v = Number((e.target as HTMLInputElement).value)
  el.currentTime = v
  currentTime.value = v
}

function onSeekEnd() {
  isSeeking.value = false
}

function onAudioError() {
  music.loadError = '音频加载失败，已自动尝试下一首'
  void playNext()
}

let rafId = 0
function tick() {
  if (!isSeeking.value && audioRef.value && isPlaying.value) {
    currentTime.value = audioRef.value.currentTime
  }
  rafId = requestAnimationFrame(tick)
}

onMounted(() => {
  applyVolume()
  rafId = requestAnimationFrame(tick)
  userStore.hydrateFromStorage()
  void music.initPlayer()
})

onUnmounted(() => {
  cancelAnimationFrame(rafId)
  stopQrPoll()
})
</script>

<template>
  <div
    class="mp-root"
    :class="{ 'mp-root--dark': isDarkMode }"
    :style="{
      zIndex: PLAYER_Z_INDEX,
      left: OFFSET_LEFT,
      top: OFFSET_TOP,
    }"
  >
    <button
      type="button"
      class="mp-trigger glass-surface"
      :class="{ 'mp-trigger--pulse': isPlaying }"
      aria-label="展开音乐播放器"
      @click="toggleExpand"
    >
      <img
        v-if="currentTrack?.cover"
        class="mp-trigger__cover"
        :src="currentTrack.cover"
        alt=""
      />
      <span v-else class="mp-trigger__icon">🎵</span>
    </button>

    <transition name="mp-panel">
      <div v-show="isExpanded" class="mp-panel glass-surface">
        <header class="mp-panel__head">
          <div class="mp-panel__meta">
            <img
              v-if="currentTrack?.cover"
              class="mp-cover"
              :src="currentTrack.cover"
              alt=""
            />
            <div class="mp-panel__titles">
              <span class="mp-panel__title" :title="displayTitle">
                {{ isLoading ? '🎵 解析地址…' : displayTitle }}
              </span>
              <button
                type="button"
                class="mp-source-chip"
                title="点击切换默认歌单 / 随机"
                @click="toggleSource"
              >
                {{ sourceLabel }}
              </button>
              <span v-if="loadError" class="mp-panel__err">{{ loadError }}</span>
              <span v-if="userStore.isLoggedIn && music.neteaseBound" class="mp-panel__sub">
                网易云：{{ music.neteaseNickname || '已登录' }}
              </span>
              <span v-if="userStore.isLoggedIn && music.qqBound" class="mp-panel__sub">
                QQ 音乐：{{ music.qqNickname || '已登录' }}
              </span>
            </div>
          </div>
          <button type="button" class="mp-panel__close" aria-label="收起" @click="collapse">
            ✕
          </button>
        </header>

        <div ref="lyricBoxRef" class="mp-lyric" aria-live="polite">
          <template v-if="lrcLines.length">
            <p
              v-for="(line, idx) in lrcLines"
              :key="idx"
              :data-lrc-line="idx"
              class="mp-lyric__line"
              :class="{ 'mp-lyric__line--on': idx === activeLine }"
            >
              <span class="mp-lyric__primary">{{ line.primary }}</span>
              <span v-if="line.secondary" class="mp-lyric__secondary">{{ line.secondary }}</span>
            </p>
          </template>
          <p v-else class="mp-lyric__empty">暂无歌词</p>
        </div>

        <div class="mp-extra">
          <button
            type="button"
            class="mp-mini"
            :class="{ 'mp-mini--active': authPanelOpen && authPanelTab === 'qq' }"
            title="QQ 音乐登录"
            @click="onQqLoginClick"
          >
            {{ qqLoginLabel }}
          </button>
          <button
            type="button"
            class="mp-mini"
            :class="{ 'mp-mini--active': authPanelOpen && authPanelTab === 'netease' }"
            title="网易云账号"
            @click="onNeteaseLoginClick"
          >
            {{ neteaseLabel }}
          </button>
          <button type="button" class="mp-mini" title="播放模式" @click="cyclePlayMode">{{ modeLabel }}</button>
        </div>

        <div v-if="authPanelOpen" ref="authBoxRef" class="mp-auth-box">
          <!-- 仅网易云 -->
          <template v-if="authPanelTab === 'netease'">
            <p class="mp-auth-box__hint">网易云音乐</p>
            <p class="mp-auth-box__qr-hint mp-auth-box__qr-hint--muted" style="margin: 0 0 8px">
              {{
                userStore.isLoggedIn
                  ? '绑定后可同步歌单、喜欢与更高音质播放等。'
                  : '请先登录本站账号，再使用下方扫码绑定网易云。'
              }}
            </p>

            <div v-if="!music.neteaseBound" class="mp-auth-box__qr">
              <button
                type="button"
                class="mp-mini"
                :disabled="neteaseLoginLoading || qrPolling || !userStore.isLoggedIn"
                @click="refreshNeteaseQrCode"
              >
                {{ qrImg ? '刷新二维码' : '生成二维码' }}
              </button>
              <div v-if="qrImg" class="mp-auth-box__qr-img-wrap">
                <img :src="qrImg" alt="网易云扫码登录" class="mp-auth-box__qr-img" />
              </div>
              <p v-if="qrStatus" class="mp-auth-box__qr-hint">{{ qrStatus }}</p>
              <p v-else class="mp-auth-box__qr-hint mp-auth-box__qr-hint--muted">
                使用网易云音乐 App 扫码；成功后自动绑定到当前本站账号。
              </p>
            </div>

            <div class="mp-auth-box__actions">
              <button
                v-if="music.neteaseBound"
                type="button"
                class="mp-mini"
                :disabled="neteaseLoginLoading"
                @click="submitNeteaseLogout"
              >
                {{ neteaseLoginLoading ? '处理中…' : '解绑网易云' }}
              </button>
            </div>
            <p v-if="neteaseAuthErr" class="mp-auth-box__err">{{ neteaseAuthErr }}</p>
            <p v-else-if="neteaseAuthMsg" class="mp-auth-box__ok">{{ neteaseAuthMsg }}</p>
          </template>

          <!-- 仅 QQ 音乐 -->
          <template v-else>
            <p class="mp-auth-box__hint">
              QQ 音乐（<a href="https://github.com/jsososo/QQMusicApi" target="_blank" rel="noopener">QQMusicApi</a>）
            </p>
            <QqCookieGuide
              class="mp-qq-guide"
              compact
              :require-site-user="!userStore.isLoggedIn"
            />
            <textarea
              v-model="qqCookieInput"
              class="mp-auth-box__textarea"
              rows="3"
              placeholder="uin=…; qm_keyst=…"
              :disabled="!userStore.isLoggedIn || qqLoginLoading"
            />
            <div class="mp-auth-box__actions">
              <button
                type="button"
                class="mp-mini"
                :disabled="!userStore.isLoggedIn || qqLoginLoading"
                @click="submitQqCookie"
              >
                {{ qqLoginLoading ? '处理中…' : '绑定 QQ Cookie' }}
              </button>
              <button
                v-if="music.qqBound"
                type="button"
                class="mp-mini"
                :disabled="qqLoginLoading"
                @click="submitQqLogout"
              >
                解绑 QQ
              </button>
            </div>
            <p v-if="qqAuthErr" class="mp-auth-box__err">{{ qqAuthErr }}</p>
            <p v-else-if="qqAuthMsg" class="mp-auth-box__ok">{{ qqAuthMsg }}</p>
          </template>
        </div>

        <div class="mp-controls">
          <button type="button" class="mp-round" aria-label="上一首" @click="playPrev">⏮</button>
          <button type="button" class="mp-round mp-round--lg" aria-label="播放或暂停" @click="togglePlay">
            {{ isLoading ? '⏳' : isPlaying ? '⏸' : '▶' }}
          </button>
          <button type="button" class="mp-round" aria-label="下一首" @click="playNext">⏭</button>
        </div>

        <div class="mp-row">
          <span class="mp-time">{{ formatTime(currentTime) }}</span>
          <input
            type="range"
            class="mp-range mp-range--progress"
            :min="0"
            :max="duration > 0 ? duration : 0"
            :step="0.1"
            :value="currentTime"
            @pointerdown="onSeekStart"
            @pointerup="onSeekEnd"
            @pointerleave="onSeekEnd"
            @input="onSeekInput"
          />
          <span class="mp-time">{{ formatTime(duration) }}</span>
        </div>

        <div class="mp-row mp-row--vol">
          <span class="mp-vol-ico" aria-hidden="true">🔊</span>
          <input
            type="range"
            class="mp-range mp-range--vol"
            min="0"
            max="1"
            step="0.01"
            :value="music.volume"
            @input="onVolumeInput"
          />
        </div>
      </div>
    </transition>

    <audio
      ref="audioRef"
      :src="resolvedUrl || undefined"
      preload="metadata"
      @ended="onEnded"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      @play="onPlay"
      @pause="onPause"
      @error="onAudioError"
    />
  </div>
</template>

<style scoped>
.mp-root {
  position: fixed;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 12px;
  pointer-events: none;
}

.mp-root.mp-root--dark .glass-surface {
  background: rgba(22, 28, 42, 0.55);
  border-color: rgba(255, 255, 255, 0.28);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35);
}

.mp-root > * {
  pointer-events: auto;
}

.glass-surface {
  background: v-bind('GLASS.bg');
  backdrop-filter: blur(v-bind('GLASS.blur'));
  -webkit-backdrop-filter: blur(v-bind('GLASS.blur'));
  border: v-bind('GLASS.border');
  box-shadow: v-bind('GLASS.shadow');
  border-radius: v-bind('GLASS.radius');
}

.mp-trigger {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: v-bind('GLASS.border');
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.45) 0%, rgba(252, 162, 229, 0.42) 100%);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: v-bind('GLASS.shadow');
  overflow: hidden;
  padding: 0;
}

.mp-trigger__cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mp-trigger:hover {
  transform: scale(1.06);
  box-shadow: 0 10px 28px rgba(102, 217, 255, 0.35);
}

.mp-trigger--pulse .mp-trigger__icon {
  animation: mp-spin 4s linear infinite;
}

@keyframes mp-spin {
  to {
    transform: rotate(360deg);
  }
}

.mp-trigger__icon {
  font-size: 1.45rem;
  line-height: 1;
  display: block;
}

.mp-panel {
  width: min(340px, calc(100vw - 100px));
  max-height: min(520px, 70vh);
  padding: 14px 16px 16px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.mp-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.mp-panel__meta {
  display: flex;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.mp-cover {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.mp-panel__titles {
  min-width: 0;
  flex: 1;
}

.mp-panel__title {
  font-size: 0.88rem;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.mp-source-chip {
  display: inline-flex;
  align-items: center;
  margin-top: 6px;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.95);
  font-size: 0.72rem;
  font-weight: 600;
  cursor: pointer;
  width: fit-content;
  max-width: 100%;
  transition: background 0.2s ease, transform 0.15s ease;
}

.mp-source-chip:hover {
  background: rgba(102, 217, 255, 0.22);
  transform: scale(1.02);
}

.mp-panel__err {
  display: block;
  font-size: 0.75rem;
  color: #ffb4b4;
  margin-top: 4px;
}

.mp-panel__sub {
  display: block;
  font-size: 0.72rem;
  opacity: 0.85;
  margin-top: 4px;
}

.mp-panel__close {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.85);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.mp-panel__close:hover {
  background: rgba(252, 162, 229, 0.35);
  color: #fff;
}

.mp-lyric {
  flex: 1;
  min-height: 120px;
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 10px;
  padding: 6px 4px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.12);
  font-size: 0.8rem;
  line-height: 1.65;
  text-align: center;
}

.mp-lyric__line {
  margin: 0.2rem 0;
  opacity: 0.78;
  color: rgba(255, 255, 255, 0.9);
  padding: 0.2rem 0.35rem;
  border-radius: 8px;
  transition: opacity 0.2s ease, transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.mp-lyric__primary,
.mp-lyric__secondary {
  display: block;
}

.mp-lyric__secondary {
  font-size: 0.74rem;
  opacity: 0.92;
}

.mp-lyric__line--on {
  opacity: 1;
  font-weight: 700;
  transform: scale(1.02);
  color: #f7fcff;
  background: linear-gradient(135deg, rgba(102, 217, 255, 0.26), rgba(252, 162, 229, 0.2));
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.16), 0 8px 18px rgba(102, 217, 255, 0.2);
}

.mp-lyric__line--on .mp-lyric__secondary {
  color: #d7f5ff;
  opacity: 1;
}

:root[data-theme='light'] .mp-lyric__line {
  color: rgba(255, 255, 255, 0.96);
}

:root[data-theme='light'] .mp-lyric__secondary {
  color: rgba(240, 248, 255, 0.98);
}

.mp-lyric__empty {
  margin: 0;
  opacity: 0.55;
  padding: 1rem 0;
}

.mp-extra {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.mp-mini {
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border-radius: 10px;
  padding: 3px 8px;
  font-size: 0.78rem;
  cursor: pointer;
  transition: transform 0.2s ease;
  white-space: nowrap;
}
.mp-mini:hover {
  transform: scale(1.05);
}

.mp-mini--active {
  background: rgba(102, 217, 255, 0.35);
  border-color: rgba(102, 217, 255, 0.75);
  box-shadow: 0 0 0 1px rgba(102, 217, 255, 0.35);
}

.mp-auth-box {
  margin: 0 0 10px;
  padding: 10px;
  border-radius: 12px;
  background: rgba(10, 18, 30, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.mp-auth-box__hint {
  margin: 0 0 8px;
  font-size: 0.76rem;
  color: rgba(255, 255, 255, 0.9);
}

.mp-auth-box__qr {
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.mp-auth-box__qr-img-wrap {
  background: #fff;
  border-radius: 10px;
  padding: 8px;
  line-height: 0;
}

.mp-auth-box__qr-img {
  width: 160px;
  height: 160px;
  display: block;
  object-fit: contain;
}

.mp-auth-box__qr-hint {
  margin: 0;
  font-size: 0.72rem;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.4;
}

.mp-auth-box__qr-hint--muted {
  opacity: 0.8;
}

.mp-qq-guide {
  margin: 0 0 8px;
}

.mp-auth-box__inline-code {
  padding: 0.1em 0.35em;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.12);
  font-family: ui-monospace, monospace;
  font-size: 0.88em;
}

:root[data-theme='dark'] .mp-auth-box__inline-code {
  background: rgba(255, 255, 255, 0.15);
}

.mp-auth-box__row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.mp-auth-box__input {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  min-height: 40px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.95);
  color: #0f1f32;
  font: inherit;
  font-size: 0.84rem;
  line-height: 1.2;
}

.mp-auth-box__input::placeholder {
  color: rgba(15, 31, 50, 0.52);
}

.mp-auth-box__input:focus {
  outline: none;
  border-color: rgba(102, 217, 255, 0.95);
  box-shadow: 0 0 0 2px rgba(102, 217, 255, 0.25);
}

.mp-auth-box__input:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.mp-auth-box__hr {
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.22);
  margin: 12px 0;
}

.mp-auth-box__textarea {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  margin-top: 6px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.95);
  color: #0f1f32;
  font: inherit;
  font-size: 0.78rem;
  line-height: 1.35;
  resize: vertical;
}

.mp-auth-box__textarea:focus {
  outline: none;
  border-color: rgba(102, 217, 255, 0.95);
}

:root[data-theme='dark'] .mp-auth-box__textarea {
  background: rgba(18, 26, 38, 0.92);
  color: #edf7ff;
  border-color: rgba(255, 255, 255, 0.24);
}

.mp-auth-box__actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-start;
}

.mp-auth-box__err,
.mp-auth-box__ok {
  margin: 8px 0 0;
  font-size: 0.78rem;
  font-weight: 600;
}

.mp-auth-box__err {
  color: #ffb5b5;
}

.mp-auth-box__ok {
  color: #b9f7d8;
}

:root[data-theme='dark'] .mp-auth-box {
  background: rgba(8, 12, 20, 0.42);
  border-color: rgba(255, 255, 255, 0.16);
}

:root[data-theme='dark'] .mp-auth-box__input {
  background: rgba(18, 26, 38, 0.92);
  color: #edf7ff;
  border-color: rgba(255, 255, 255, 0.24);
}

:root[data-theme='dark'] .mp-auth-box__input::placeholder {
  color: rgba(237, 247, 255, 0.58);
}

@media (max-width: 520px) {
  .mp-auth-box__row {
    grid-template-columns: 1fr;
  }
}

.mp-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 14px;
}

.mp-round {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 0.95rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.25s ease, background 0.25s ease, box-shadow 0.25s ease;
}

.mp-round:hover {
  transform: scale(1.08);
  background: rgba(102, 217, 255, 0.35);
  box-shadow: 0 6px 18px rgba(102, 217, 255, 0.25);
}

.mp-round--lg {
  width: 48px;
  height: 48px;
  font-size: 1.1rem;
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  border-color: rgba(255, 255, 255, 0.55);
}

.mp-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.mp-row--vol {
  margin-bottom: 0;
}

.mp-time {
  font-size: 0.7rem;
  font-weight: 600;
  opacity: 0.9;
  width: 2.25rem;
  flex-shrink: 0;
  text-align: center;
}

.mp-vol-ico {
  font-size: 0.85rem;
  width: 1.5rem;
  flex-shrink: 0;
  text-align: center;
}

.mp-range {
  flex: 1;
  height: 6px;
  border-radius: 6px;
  appearance: none;
  -webkit-appearance: none;
  background: linear-gradient(
    to right,
    #4a90e2 0%,
    #50e3c2 var(--fill, 0%),
    rgba(30, 40, 50, 0.55) var(--fill, 0%),
    rgba(30, 40, 50, 0.55) 100%
  );
  cursor: pointer;
}

.mp-range--progress {
  --fill: v-bind(progressFillStr);
}

.mp-range--vol {
  --fill: v-bind(volumeFillStr);
}

.mp-range::-webkit-slider-thumb {
  appearance: none;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(145deg, #fca2e5, #66d9ff);
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(102, 217, 255, 0.45);
  cursor: grab;
  transition: transform 0.2s ease;
}

.mp-range::-webkit-slider-thumb:hover {
  transform: scale(1.12);
}

.mp-range::-moz-range-thumb {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(145deg, #fca2e5, #66d9ff);
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(102, 217, 255, 0.45);
}

.mp-panel-enter-active,
.mp-panel-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.mp-panel-enter-from,
.mp-panel-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

@media (max-width: 480px) {
  .mp-root {
    left: 12px !important;
    top: 78px !important;
    flex-direction: row;
    align-items: flex-start;
  }

  .mp-panel {
    width: calc(100vw - 24px);
    max-width: 360px;
  }
}
</style>
