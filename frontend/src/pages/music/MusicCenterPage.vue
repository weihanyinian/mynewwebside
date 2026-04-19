<script setup lang="ts">
/**
 * 网易云：绑定账号、歌单、喜欢、最近播放；点击曲目在全局播放器播放。
 */
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../../stores/user'
import { useMusicPlayerStore } from '../../stores/musicPlayer'
import {
  fetchNeteaseStatus,
  neteaseLogin,
  neteaseLogout,
  fetchUserPlaylists,
  fetchLikelist,
  fetchRecent,
  fetchPlaylistTracks,
  type SongMeta,
  type PlaylistItem,
} from '../../api/neteaseMusic'

const { t } = useI18n()
const userStore = useUserStore()
const music = useMusicPlayerStore()

const loading = ref(true)
const err = ref('')
const bound = ref(false)
const neteaseNickname = ref<string | null>(null)

const phone = ref('')
const password = ref('')
const loginBusy = ref(false)

const tab = ref<'playlists' | 'likes' | 'recent'>('playlists')
const playlists = ref<PlaylistItem[]>([])
const likes = ref<SongMeta[]>([])
const recent = ref<SongMeta[]>([])
const expandedPid = ref<number | null>(null)
const expandedTracks = ref<SongMeta[]>([])
const tracksLoading = ref(false)

const siteTitle = computed(() => t('pages.musicTitle'))

function toPlayerTrack(m: SongMeta) {
  return music.metaToTrack(m)
}

async function refreshStatus() {
  try {
    const s = await fetchNeteaseStatus()
    bound.value = s.bound
    neteaseNickname.value = s.neteaseNickname
  } catch {
    bound.value = false
    neteaseNickname.value = null
  }
  await music.refreshNeteaseStatus()
}

async function loadAll() {
  loading.value = true
  err.value = ''
  try {
    await refreshStatus()
    if (!bound.value) {
      playlists.value = []
      likes.value = []
      recent.value = []
      return
    }
    const [pl, lk, rc] = await Promise.all([
      fetchUserPlaylists(0, 50),
      fetchLikelist(),
      fetchRecent(50),
    ])
    playlists.value = pl
    likes.value = lk
    recent.value = rc
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : t('pages.loadError')
  } finally {
    loading.value = false
  }
}

async function onLogin() {
  loginBusy.value = true
  err.value = ''
  try {
    await neteaseLogin(phone.value.trim(), password.value)
    password.value = ''
    await loadAll()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : t('pages.musicLoginFail')
  } finally {
    loginBusy.value = false
  }
}

async function onLogout() {
  try {
    await neteaseLogout()
    bound.value = false
    neteaseNickname.value = null
    playlists.value = []
    likes.value = []
    recent.value = []
    await music.refreshNeteaseStatus()
  } catch {
    /* ignore */
  }
}

async function loadPlaylistTracks(pid: number) {
  if (expandedPid.value === pid) {
    expandedPid.value = null
    expandedTracks.value = []
    return
  }
  expandedPid.value = pid
  tracksLoading.value = true
  expandedTracks.value = []
  try {
    expandedTracks.value = await fetchPlaylistTracks(pid)
  } catch {
    expandedTracks.value = []
  } finally {
    tracksLoading.value = false
  }
}

async function playSong(m: SongMeta) {
  await music.playTrack(toPlayerTrack(m))
}

async function playAll(list: SongMeta[]) {
  if (list.length === 0) return
  await music.playTracks(
    list.map((x) => toPlayerTrack(x)),
    0,
  )
}

onMounted(async () => {
  userStore.hydrateFromStorage()
  await loadAll()
  await music.refreshNeteaseStatus()
})
</script>

<template>
  <div class="music-page">
    <header class="music-head">
      <h1 class="music-title">{{ siteTitle }}</h1>
      <p class="music-lead">{{ t('pages.musicLead') }}</p>
    </header>

    <section v-if="!userStore.isLoggedIn" class="glass-card music-hint">
      {{ t('pages.musicNeedSiteLogin') }}
    </section>

    <template v-else>
      <section class="glass-card music-bind">
        <h2 class="music-h2">{{ t('pages.musicNeteaseAccount') }}</h2>
        <p v-if="bound" class="music-status">
          {{ t('pages.musicBoundAs') }} <strong>{{ neteaseNickname || '—' }}</strong>
          <button type="button" class="mp-btn" @click="onLogout">{{ t('pages.musicUnbind') }}</button>
        </p>
        <form v-else class="music-form" @submit.prevent="onLogin">
          <label class="music-label">{{ t('pages.musicPhone') }}</label>
          <input v-model="phone" type="tel" class="music-input" autocomplete="username" required />
          <label class="music-label">{{ t('pages.musicPassword') }}</label>
          <input v-model="password" type="password" class="music-input" autocomplete="current-password" required />
          <button type="submit" class="music-submit" :disabled="loginBusy">
            {{ loginBusy ? '…' : t('pages.musicBind') }}
          </button>
        </form>
        <p v-if="err" class="music-err">{{ err }}</p>
      </section>

      <section v-if="bound" class="music-body">
        <div class="music-tabs">
          <button
            type="button"
            class="music-tab"
            :class="{ 'music-tab--on': tab === 'playlists' }"
            @click="tab = 'playlists'"
          >
            {{ t('pages.musicTabPlaylists') }}
          </button>
          <button type="button" class="music-tab" :class="{ 'music-tab--on': tab === 'likes' }" @click="tab = 'likes'">
            {{ t('pages.musicTabLikes') }}
          </button>
          <button type="button" class="music-tab" :class="{ 'music-tab--on': tab === 'recent' }" @click="tab = 'recent'">
            {{ t('pages.musicTabRecent') }}
          </button>
        </div>

        <p v-if="loading" class="music-muted">{{ t('pages.loading') }}</p>

        <div v-else-if="tab === 'playlists'" class="music-list">
          <div v-for="p in playlists" :key="p.id" class="music-pl">
            <div class="music-pl-row" @click="loadPlaylistTracks(p.id)">
              <img v-if="p.coverUrl" class="music-pl-cover" :src="p.coverUrl" alt="" />
              <div class="music-pl-meta">
                <span class="music-pl-name">{{ p.name }}</span>
                <span class="music-pl-count">{{ p.trackCount }} {{ t('pages.musicTracks') }}</span>
              </div>
              <span class="music-pl-chev">{{ expandedPid === p.id ? '▾' : '▸' }}</span>
            </div>
            <div v-if="expandedPid === p.id" class="music-pl-tracks">
              <p v-if="tracksLoading" class="music-muted">{{ t('pages.loading') }}</p>
              <template v-else>
                <button type="button" class="music-play-all" @click="playAll(expandedTracks)">
                  {{ t('pages.musicPlayAll') }}
                </button>
                <button
                  v-for="s in expandedTracks"
                  :key="s.id"
                  type="button"
                  class="music-row"
                  @click="playSong(s)"
                >
                  <span class="music-sn">{{ s.name }}</span>
                  <span class="music-sa">{{ s.artist }}</span>
                </button>
              </template>
            </div>
          </div>
          <p v-if="playlists.length === 0" class="music-muted">{{ t('pages.musicEmpty') }}</p>
        </div>

        <div v-else-if="tab === 'likes'" class="music-list">
          <button v-if="likes.length" type="button" class="music-play-all" @click="playAll(likes)">
            {{ t('pages.musicPlayAll') }}
          </button>
          <button v-for="s in likes" :key="s.id" type="button" class="music-row" @click="playSong(s)">
            <span class="music-sn">{{ s.name }}</span>
            <span class="music-sa">{{ s.artist }}</span>
          </button>
          <p v-if="likes.length === 0" class="music-muted">{{ t('pages.musicEmpty') }}</p>
        </div>

        <div v-else class="music-list">
          <button v-for="s in recent" :key="s.id" type="button" class="music-row" @click="playSong(s)">
            <span class="music-sn">{{ s.name }}</span>
            <span class="music-sa">{{ s.artist }}</span>
          </button>
          <p v-if="recent.length === 0" class="music-muted">{{ t('pages.musicEmpty') }}</p>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.music-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 1rem 1rem 3rem;
}

.music-head {
  margin-bottom: 1.25rem;
}

.music-title {
  margin: 0 0 0.5rem;
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-color, #2c3e50);
}

.music-lead {
  margin: 0;
  opacity: 0.85;
  font-size: 0.95rem;
  color: var(--blog-on-glass-muted, rgba(26, 58, 82, 0.75));
}

.glass-card {
  border-radius: 16px;
  padding: 1rem 1.25rem;
  background: var(--glass-bg, rgba(255, 255, 255, 0.35));
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.55));
  box-shadow: var(--glass-shadow, 0 8px 32px rgba(102, 217, 255, 0.2));
  backdrop-filter: blur(10px);
  margin-bottom: 1rem;
}

.music-hint {
  font-weight: 600;
}

.music-h2 {
  margin: 0 0 0.75rem;
  font-size: 1.1rem;
}

.music-status {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
}

.music-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 320px;
}

.music-label {
  font-size: 0.85rem;
  font-weight: 600;
}

.music-input {
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  padding: 0.45rem 0.65rem;
  font-size: 0.95rem;
}

:root[data-theme='dark'] .music-input {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  color: #eaf8ff;
}

.music-submit {
  margin-top: 0.25rem;
  border: none;
  border-radius: 12px;
  padding: 0.5rem 1rem;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #4a90e2 0%, #50e3c2 100%);
  color: #fff;
}

.music-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.mp-btn {
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 10px;
  padding: 0.25rem 0.6rem;
  background: transparent;
  cursor: pointer;
  font-size: 0.85rem;
}

.music-err {
  color: #c0392b;
  margin: 0.5rem 0 0;
  font-size: 0.9rem;
}

.music-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.music-tab {
  border: 1px solid var(--glass-border, rgba(255, 255, 255, 0.55));
  border-radius: 999px;
  padding: 0.35rem 0.85rem;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  font-weight: 600;
  color: var(--text-color, #2c3e50);
}

.music-tab--on {
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.35) 0%, rgba(80, 227, 194, 0.35) 100%);
}

.music-muted {
  opacity: 0.75;
  margin: 0;
}

.music-pl {
  margin-bottom: 0.5rem;
}

.music-pl-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.25rem;
  cursor: pointer;
  border-radius: 12px;
}

.music-pl-row:hover {
  background: rgba(0, 0, 0, 0.04);
}

.music-pl-cover {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.music-pl-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.music-pl-name {
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.music-pl-count {
  font-size: 0.8rem;
  opacity: 0.75;
}

.music-pl-tracks {
  padding: 0.25rem 0 0.5rem 0.5rem;
  border-left: 2px solid rgba(102, 217, 255, 0.35);
  margin-left: 0.5rem;
}

.music-play-all {
  display: block;
  width: 100%;
  margin-bottom: 0.35rem;
  border-radius: 10px;
  border: 1px dashed rgba(102, 217, 255, 0.5);
  padding: 0.35rem;
  background: rgba(102, 217, 255, 0.12);
  cursor: pointer;
  font-weight: 600;
}

.music-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
  text-align: left;
  border: none;
  border-radius: 10px;
  padding: 0.45rem 0.5rem;
  margin-bottom: 0.25rem;
  background: rgba(255, 255, 255, 0.12);
  cursor: pointer;
  color: inherit;
}

.music-row:hover {
  background: rgba(102, 217, 255, 0.2);
}

.music-sn {
  font-weight: 600;
}

.music-sa {
  font-size: 0.8rem;
  opacity: 0.8;
}
</style>
