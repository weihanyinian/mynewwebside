<script setup lang="ts">
/**
 * 网易云：绑定账号、歌单、喜欢、最近播放；点击曲目在全局播放器播放。
 */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../../stores/user'
import { useMusicPlayerStore } from '../../stores/musicPlayer'
import QqCookieGuide from '../../components/music/QqCookieGuide.vue'
import {
  fetchNeteaseStatus,
  neteaseLogout,
  fetchUserPlaylists,
  fetchLikelist,
  fetchRecent,
  fetchPlaylistTracks,
  fetchPublicPlaylist,
  searchNetease,
  searchQq,
  qqLoginCookie,
  qqLogout,
  type SongMeta,
  type PlaylistItem,
  type MusicSearchHit,
} from '../../api/musicApi'

const { t } = useI18n()
const userStore = useUserStore()
const music = useMusicPlayerStore()

const loading = ref(true)
const err = ref('')
const bound = ref(false)
const neteaseNickname = ref<string | null>(null)

const tab = ref<'playlists' | 'likes' | 'recent'>('playlists')
const playlists = ref<PlaylistItem[]>([])
const likes = ref<SongMeta[]>([])
const recent = ref<SongMeta[]>([])
const expandedPid = ref<number | null>(null)
const expandedTracks = ref<SongMeta[]>([])
const tracksLoading = ref(false)

const searchPlatform = ref<'netease' | 'qq'>('netease')
const searchKind = ref<'song' | 'artist' | 'album' | 'playlist'>('song')
const searchQ = ref('')
const searchLoading = ref(false)
const searchErr = ref('')
const searchInfo = ref('')
const searchResults = ref<MusicSearchHit[]>([])
let searchDebounceTimer: ReturnType<typeof setTimeout> | null = null
let searchSeq = 0
const SEARCH_DEBOUNCE_MS = 300

const qqCookieDraft = ref('')
const qqBindLoading = ref(false)
const qqBindErr = ref('')
const qqBindMsg = ref('')

const siteTitle = computed(() => t('pages.musicTitle'))

function clearSearchDebounce() {
  if (searchDebounceTimer != null) {
    clearTimeout(searchDebounceTimer)
    searchDebounceTimer = null
  }
}

function setSearchPlatform(p: 'netease' | 'qq') {
  searchPlatform.value = p
  clearSearchDebounce()
  searchResults.value = []
  searchErr.value = ''
}

function onSearchInput() {
  clearSearchDebounce()
  const q = searchQ.value.trim()
  if (!q) {
    searchSeq += 1
    searchResults.value = []
    searchErr.value = ''
    searchInfo.value = ''
    return
  }
  searchDebounceTimer = setTimeout(() => {
    searchDebounceTimer = null
    void executeSearch()
  }, SEARCH_DEBOUNCE_MS)
}

function setSearchKind(k: 'song' | 'artist' | 'album' | 'playlist') {
  searchKind.value = k
  clearSearchDebounce()
  const q = searchQ.value.trim()
  if (!q || !userStore.isLoggedIn) return
  searchDebounceTimer = setTimeout(() => {
    searchDebounceTimer = null
    void executeSearch()
  }, SEARCH_DEBOUNCE_MS)
}

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

async function executeSearch() {
  if (!userStore.isLoggedIn) {
    searchResults.value = []
    searchErr.value = t('pages.musicSearchNeedLogin')
    return
  }
  const q = searchQ.value.trim()
  if (!q) {
    searchResults.value = []
    return
  }
  const seq = ++searchSeq
  searchLoading.value = true
  searchErr.value = ''
  searchInfo.value = ''
  try {
    const rows =
      searchPlatform.value === 'netease'
        ? await searchNetease(q, 30, searchKind.value)
        : await searchQq(q, 1, 25, searchKind.value)
    if (seq !== searchSeq) return
    searchResults.value = rows
  } catch (e: unknown) {
    if (seq !== searchSeq) return
    searchResults.value = []
    searchErr.value = e instanceof Error ? e.message : t('pages.loadError')
  } finally {
    if (seq === searchSeq) searchLoading.value = false
  }
}

async function runSearch() {
  clearSearchDebounce()
  await executeSearch()
}

function kindLabel(k: MusicSearchHit['kind']) {
  switch (k) {
    case 'artist':
      return t('pages.musicSearchKindArtist')
    case 'album':
      return t('pages.musicSearchKindAlbum')
    case 'playlist':
      return t('pages.musicSearchKindPlaylist')
    default:
      return t('pages.musicSearchKindSong')
  }
}

async function onSearchHitClick(hit: MusicSearchHit) {
  searchInfo.value = ''
  if (hit.kind === 'song') {
    if (searchPlatform.value === 'netease') {
      await playSong({ id: hit.id, name: hit.title, artist: hit.subtitle, cover: hit.cover })
    } else {
      if (!hit.mid) return
      await music.playTrack(
        music.metaToQqTrack({
          songmid: hit.mid,
          name: hit.title,
          artist: hit.subtitle,
          cover: hit.cover,
        }),
      )
    }
    return
  }
  if (hit.kind === 'artist') {
    searchKind.value = 'song'
    searchQ.value = hit.title
    await runSearch()
    return
  }
  if (hit.kind === 'album') {
    searchKind.value = 'song'
    searchQ.value = hit.subtitle ? `${hit.title} ${hit.subtitle}` : hit.title
    await runSearch()
    return
  }
  if (hit.kind === 'playlist') {
    if (searchPlatform.value === 'netease' && hit.id > 0) {
      try {
        const tracks = await fetchPublicPlaylist(String(hit.id))
        await playAll(tracks)
      } catch {
        searchErr.value = t('pages.loadError')
      }
    } else if (searchPlatform.value === 'qq') {
      searchInfo.value = t('pages.musicSearchQqPlaylistHint')
      searchKind.value = 'song'
      searchQ.value = hit.title
    }
  }
}

async function onQqBind() {
  if (!userStore.isLoggedIn) return
  const ck = qqCookieDraft.value.trim()
  if (!ck) {
    qqBindErr.value = '请粘贴 Cookie'
    return
  }
  qqBindLoading.value = true
  qqBindErr.value = ''
  qqBindMsg.value = ''
  try {
    await qqLoginCookie(ck)
    await music.refreshNeteaseStatus()
    qqBindMsg.value = t('pages.musicQqBindOk')
    qqCookieDraft.value = ''
  } catch (e: unknown) {
    qqBindErr.value = e instanceof Error ? e.message : t('pages.loadError')
  } finally {
    qqBindLoading.value = false
  }
}

async function onQqUnbind() {
  qqBindLoading.value = true
  qqBindErr.value = ''
  qqBindMsg.value = ''
  try {
    await qqLogout()
    await music.refreshNeteaseStatus()
  } catch (e: unknown) {
    qqBindErr.value = e instanceof Error ? e.message : t('pages.loadError')
  } finally {
    qqBindLoading.value = false
  }
}

onMounted(async () => {
  userStore.hydrateFromStorage()
  await loadAll()
})

onUnmounted(() => {
  clearSearchDebounce()
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
      <section class="glass-card music-search">
        <h2 class="music-h2">{{ t('pages.musicSearchTitle') }}</h2>
        <div class="music-search-platform">
          <button
            type="button"
            class="music-tab"
            :class="{ 'music-tab--on': searchPlatform === 'netease' }"
            @click="setSearchPlatform('netease')"
          >
            {{ t('pages.musicSearchPlatformNetease') }}
          </button>
          <button
            type="button"
            class="music-tab"
            :class="{ 'music-tab--on': searchPlatform === 'qq' }"
            @click="setSearchPlatform('qq')"
          >
            {{ t('pages.musicSearchPlatformQq') }}
          </button>
        </div>
        <div class="music-search-kind">
          <button
            type="button"
            class="music-tab music-tab--sm"
            :class="{ 'music-tab--on': searchKind === 'song' }"
            @click="setSearchKind('song')"
          >
            {{ t('pages.musicSearchKindSong') }}
          </button>
          <button
            type="button"
            class="music-tab music-tab--sm"
            :class="{ 'music-tab--on': searchKind === 'artist' }"
            @click="setSearchKind('artist')"
          >
            {{ t('pages.musicSearchKindArtist') }}
          </button>
          <button
            type="button"
            class="music-tab music-tab--sm"
            :class="{ 'music-tab--on': searchKind === 'album' }"
            @click="setSearchKind('album')"
          >
            {{ t('pages.musicSearchKindAlbum') }}
          </button>
          <button
            type="button"
            class="music-tab music-tab--sm"
            :class="{ 'music-tab--on': searchKind === 'playlist' }"
            @click="setSearchKind('playlist')"
          >
            {{ t('pages.musicSearchKindPlaylist') }}
          </button>
        </div>
        <div class="music-search-row">
          <input
            v-model="searchQ"
            type="search"
            class="music-input music-search-input"
            :placeholder="t('pages.musicSearchPlaceholder')"
            @input="onSearchInput"
            @keydown.enter.prevent="runSearch"
          />
          <button type="button" class="music-submit music-search-btn" :disabled="searchLoading" @click="runSearch">
            {{ searchLoading ? t('pages.loading') : t('pages.musicSearchBtn') }}
          </button>
        </div>
        <p v-if="searchErr" class="music-err">{{ searchErr }}</p>
        <p v-else-if="searchInfo" class="music-muted music-search-info">{{ searchInfo }}</p>
        <div v-if="searchResults.length" class="music-list music-search-results">
          <button
            v-for="(h, idx) in searchResults"
            :key="`${h.kind}-${h.id}-${h.mid}-${idx}`"
            type="button"
            class="music-row music-row--hit"
            @click="onSearchHitClick(h)"
          >
            <img
              v-if="h.cover"
              class="music-hit-cover"
              :src="h.cover"
              alt=""
              loading="lazy"
              decoding="async"
            />
            <div class="music-hit-text">
              <span class="music-sn">{{ h.title }}</span>
              <span v-if="h.subtitle" class="music-sa">{{ h.subtitle }}</span>
              <span class="music-hit-kind">{{ kindLabel(h.kind) }}</span>
            </div>
          </button>
        </div>
      </section>

      <section class="glass-card music-bind">
        <h2 class="music-h2">{{ t('pages.musicQqAccount') }}</h2>
        <p v-if="music.qqBound" class="music-status">
          {{ t('pages.musicQqBoundAs') }} <strong>{{ music.qqNickname || '—' }}</strong>
          <button type="button" class="mp-btn" :disabled="qqBindLoading" @click="onQqUnbind">
            {{ t('pages.musicQqUnbind') }}
          </button>
        </p>
        <QqCookieGuide v-else class="music-qq-guide" />
        <textarea
          v-model="qqCookieDraft"
          class="music-textarea"
          rows="3"
          :placeholder="t('pages.musicQqCookiePh')"
          :disabled="qqBindLoading"
        />
        <button
          v-if="!music.qqBound"
          type="button"
          class="music-submit"
          :disabled="qqBindLoading"
          @click="onQqBind"
        >
          {{ qqBindLoading ? t('pages.loading') : t('pages.musicQqBind') }}
        </button>
        <p v-if="qqBindErr" class="music-err">{{ qqBindErr }}</p>
        <p v-else-if="qqBindMsg" class="music-muted">{{ qqBindMsg }}</p>
      </section>

      <section class="glass-card music-bind">
        <h2 class="music-h2">{{ t('pages.musicNeteaseAccount') }}</h2>
        <p v-if="bound" class="music-status">
          {{ t('pages.musicBoundAs') }} <strong>{{ neteaseNickname || '—' }}</strong>
          <button type="button" class="mp-btn" @click="onLogout">{{ t('pages.musicUnbind') }}</button>
        </p>
        <p v-else class="music-qr-hint">
          {{ t('pages.musicQrBindHint') }}
        </p>
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
              <img v-if="p.coverUrl" class="music-pl-cover" :src="p.coverUrl" alt="" loading="lazy" decoding="async" />
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

.music-qr-hint {
  margin: 0;
  max-width: 36rem;
  line-height: 1.55;
  font-size: 0.92rem;
  opacity: 0.95;
}

.music-qq-guide {
  margin-bottom: 0.75rem;
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

.music-search-platform {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.65rem;
}

.music-search-kind {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin-bottom: 0.65rem;
}

.music-tab--sm {
  padding: 0.25rem 0.65rem;
  font-size: 0.82rem;
}

.music-search-info {
  margin-top: 0.5rem;
}

.music-row--hit {
  flex-direction: row;
  align-items: center;
  gap: 0.6rem;
}

.music-hit-cover {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.music-hit-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.music-hit-kind {
  font-size: 0.72rem;
  opacity: 0.65;
  margin-top: 0.15rem;
}

.music-search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.music-search-input {
  flex: 1;
  min-width: 180px;
}

.music-search-btn {
  margin-top: 0;
  white-space: nowrap;
}

.music-search-results {
  margin-top: 0.75rem;
}

.music-textarea {
  width: 100%;
  box-sizing: border-box;
  margin: 0.5rem 0;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  padding: 0.45rem 0.65rem;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
}

:root[data-theme='dark'] .music-textarea {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  color: #eaf8ff;
}

.music-qq-cmd {
  margin: 0.35rem 0 0.5rem;
  font-size: 0.88rem;
}

.music-qq-cmd code {
  display: inline-block;
  padding: 0.2rem 0.45rem;
  border-radius: 6px;
  background: rgba(0, 0, 0, 0.06);
  font-family: ui-monospace, monospace;
  font-size: 0.85em;
}

:root[data-theme='dark'] .music-qq-cmd code {
  background: rgba(255, 255, 255, 0.1);
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
