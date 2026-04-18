<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { fetchPublicFriends, type FriendLink } from '../../api/friends'

const { t } = useI18n()
const loading = ref(true)
const err = ref('')
const friends = ref<FriendLink[]>([])

onMounted(async () => {
  loading.value = true
  try {
    friends.value = await fetchPublicFriends()
  } catch (e) {
    err.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="friends-page">
    <h1 class="page-title">{{ t('pages.friendsTitle') }}</h1>
    <p v-if="loading" class="muted">{{ t('pages.loading') }}</p>
    <p v-else-if="err" class="err-text">{{ err }}</p>
    <p v-else-if="!friends.length" class="muted">{{ t('pages.friendsEmpty') }}</p>
    <div v-else class="friend-grid">
      <a
        v-for="f in friends"
        :key="f.id"
        :href="f.url"
        target="_blank"
        rel="noopener noreferrer"
        class="friend-card card"
      >
        <div class="friend-avatar">
          <img v-if="f.avatarUrl" :src="f.avatarUrl" :alt="f.title" loading="lazy" />
          <span v-else class="friend-avatar-fallback">{{ f.title.slice(0, 1) }}</span>
        </div>
        <div class="friend-body">
          <h3>{{ f.title }}</h3>
          <p class="friend-desc">{{ f.description || f.url }}</p>
          <p class="friend-more">{{ f.url }}</p>
        </div>
      </a>
    </div>
  </div>
</template>

<style scoped>
.friends-page {
  padding: 8px 0 24px;
}
.err-text {
  color: #d97706;
}
:root[data-theme='dark'] .err-text {
  color: #fbbf24;
}
.friend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.friend-card {
  display: flex;
  gap: 14px;
  padding: 18px;
  text-decoration: none;
  color: inherit;
  transition:
    transform 0.28s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.28s ease;
}
.friend-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(102, 217, 255, 0.2);
}
.friend-avatar {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid var(--glass-border);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 217, 255, 0.12);
}
.friend-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.friend-avatar-fallback {
  font-weight: 800;
  font-size: 1.25rem;
  opacity: 0.85;
}
.friend-body h3 {
  margin: 0 0 6px;
  font-size: 1.05rem;
}
.friend-desc {
  margin: 0;
  font-size: 0.82rem;
  opacity: 0.82;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.friend-more {
  margin: 8px 0 0;
  font-size: 0.72rem;
  opacity: 0.55;
  word-break: break-all;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}
.friend-card:hover .friend-more {
  max-height: 4rem;
}
</style>
