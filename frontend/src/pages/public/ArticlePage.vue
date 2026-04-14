<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicArticle, type ArticleDetail } from '../../api/blog'
import MarkdownView from '../../components/MarkdownView.vue'
import { getToken } from '../../utils/token'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref<ArticleDetail | null>(null)

const isLoggedIn = computed(() => !!getToken())

// Mock comments data
const comments = ref([
  { id: 1, author: '匿名魔法使', content: '文章写得真好，受教了！', time: '2026-04-14 10:00', avatar: '🧙‍♂️' },
  { id: 2, author: '二次元爱好者', content: '这个博客的风格太棒了，特别是背景的赛博风！', time: '2026-04-14 12:30', avatar: '🌸' }
])
const newComment = ref('')
const newAuthor = ref('')

function submitComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请填写评论内容')
    return
  }
  comments.value.unshift({
    id: Date.now(),
    author: newAuthor.value.trim() || '匿名魔法使',
    content: newComment.value,
    time: new Date().toLocaleString(),
    avatar: '✨'
  })
  newComment.value = ''
  newAuthor.value = ''
  ElMessage.success('评论发布成功！')
}

function deleteComment(id: number) {
  comments.value = comments.value.filter(c => c.id !== id)
  ElMessage.success('已删除违规评论')
}

onMounted(async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    article.value = await getPublicArticle(id)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading" class="article-page">
    <div v-if="article" class="card article">
      <div class="article__head">
        <h1 class="article__title">{{ article.title }}</h1>
        <div class="article__meta muted">
          <span class="meta-item">✍️ 维寒一念</span>
          <span v-if="article.category" class="meta-item category-meta">📁 {{ article.category.name }}</span>
          <span v-if="article.publishedAt" class="meta-item">📅 {{ new Date(article.publishedAt).toLocaleString() }}</span>
          <span class="meta-item">👁️ {{ article.views }} 次阅读</span>
        </div>
        <div class="article__tags">
          <span v-for="t in article.tags" :key="t.id" class="anime-tag" @click="router.push({ path: '/blog', query: { tagId: t.id } })">
            #{{ t.name }}
          </span>
        </div>
      </div>

      <div v-if="article.coverUrl" class="article__cover">
        <img :src="article.coverUrl" alt="cover" />
      </div>

      <div class="article__body">
        <MarkdownView :content="article.contentMd" />
      </div>
    </div>

    <!-- Comments Section -->
    <div v-if="article" class="card comments-section">
      <h3 class="comments-title">留言板</h3>
      
      <!-- Comment Input -->
      <div class="comment-form">
        <el-input v-model="newAuthor" placeholder="昵称（选填，默认匿名）" class="comment-author-input" />
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="3"
          placeholder="留下你的魔法印记..."
          class="comment-textarea"
        />
        <div class="comment-actions">
          <button class="btn-primary" @click="submitComment">发送留言</button>
        </div>
      </div>

      <!-- Comment List -->
      <div class="comment-list">
        <div v-for="c in comments" :key="c.id" class="comment-item">
          <div class="comment-avatar">{{ c.avatar }}</div>
          <div class="comment-content-wrap">
            <div class="comment-header">
              <span class="comment-author">{{ c.author }}</span>
              <span class="comment-time">{{ c.time }}</span>
            </div>
            <div class="comment-text">{{ c.content }}</div>
          </div>
          <button v-if="isLoggedIn" class="btn-delete" @click="deleteComment(c.id)">删除</button>
        </div>
        <div v-if="comments.length === 0" class="no-comments">暂无留言，来做第一个吧！</div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <svg viewBox="0 0 200 200" width="160" height="160" xmlns="http://www.w3.org/2000/svg">
        <path fill="rgba(74, 144, 226, 0.2)" d="M42.7,-73.4C56.6,-66.1,70.1,-55.8,78.8,-42.1C87.5,-28.4,91.3,-14.2,90.4,-0.5C89.5,13.2,83.8,26.4,75.1,38.1C66.4,49.8,54.7,60.1,41.4,66.6C28.1,73.1,14,75.9,-0.6,76.9C-15.2,78,-30.4,77.3,-43.3,70.5C-56.2,63.7,-66.8,50.8,-73.8,36.5C-80.8,22.2,-84.2,6.5,-81.4,-8.2C-78.6,-22.9,-69.6,-36.6,-57.6,-45.5C-45.6,-54.4,-30.6,-58.5,-17.1,-63.4C-3.6,-68.3,8.8,-74.3,21.5,-76C34.2,-77.7,46.9,-75.1,42.7,-73.4Z" transform="translate(100 100)" />
        <text x="100" y="100" font-size="40" text-anchor="middle" dominant-baseline="central" fill="rgba(74, 144, 226, 0.5)">❓</text>
      </svg>
      <p>文章可能被虚空吞噬了...</p>
    </div>
  </div>
</template>

<style scoped>
.article-page {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.article {
  padding: 40px;
  margin-bottom: 24px;
}

.article__head {
  text-align: center;
  margin-bottom: 40px;
  padding-bottom: 20px;
  border-bottom: 1px dashed rgba(74, 144, 226, 0.3);
}

.article__title {
  font-size: 2.2rem;
  font-weight: 900;
  line-height: 1.4;
  margin-bottom: 16px;
  color: var(--text-color);
}

.article__meta {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 0.95rem;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.category-meta {
  color: var(--primary-color);
  font-weight: 600;
}

.article__tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
}

.anime-tag {
  font-size: 0.85rem;
  padding: 6px 14px;
  background: rgba(74, 144, 226, 0.1);
  color: var(--primary-color);
  border-radius: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.anime-tag:hover {
  background: var(--primary-color);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(74, 144, 226, 0.3);
}

.article__cover {
  margin-top: 16px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.article__cover img {
  width: 100%;
  display: block;
}

.article__body {
  line-height: 1.8;
  font-size: 1.05rem;
  color: #34495e;
}

/* Override Markdown styles to fit theme */
.article__body :deep(h1),
.article__body :deep(h2),
.article__body :deep(h3) {
  color: var(--text-color);
  margin: 30px 0 16px;
  font-weight: 800;
}

.article__body :deep(h2) {
  border-bottom: 2px solid rgba(74, 144, 226, 0.2);
  padding-bottom: 8px;
}

.article__body :deep(p) {
  margin: 16px 0;
}

.article__body :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
  border-bottom: 1px dashed var(--primary-color);
  transition: all 0.3s;
}
.article__body :deep(a:hover) {
  color: var(--secondary-color);
  border-bottom-color: var(--secondary-color);
}

.article__body :deep(blockquote) {
  margin: 20px 0;
  padding: 16px 20px;
  border-left: 4px solid var(--primary-color);
  background: linear-gradient(to right, rgba(74, 144, 226, 0.1), rgba(255,255,255,0));
  border-radius: 0 12px 12px 0;
  font-style: italic;
  color: #5a6c7d;
}

.article__body :deep(pre) {
  background: #1e1e1e !important;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}
.article__body :deep(code) {
  font-family: 'Fira Code', Consolas, monospace;
  color: #e06c75;
}
.article__body :deep(pre code) {
  color: #abb2bf;
}

/* Comments Section */
.comments-section {
  padding: 40px;
}

.comments-title {
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 24px;
  color: var(--text-color);
  display: flex;
  align-items: center;
  gap: 8px;
}
.comments-title::before {
  content: "💬";
}

.comment-form {
  background: rgba(255,255,255,0.4);
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 30px;
  border: 1px dashed rgba(74, 144, 226, 0.3);
}

.comment-author-input {
  margin-bottom: 12px;
  max-width: 300px;
}

:deep(.comment-author-input .el-input__wrapper),
:deep(.comment-textarea .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.6);
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border-radius: 12px;
}
:deep(.comment-author-input .el-input__wrapper.is-focus),
:deep(.comment-textarea .el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(74, 144, 226, 0.3);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: rgba(255,255,255,0.5);
  border-radius: 16px;
  transition: transform 0.2s;
  position: relative;
}
.comment-item:hover {
  transform: translateX(4px);
  background: rgba(255,255,255,0.7);
}

.comment-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #fbc2eb 0%, #a18cd1 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  box-shadow: 0 4px 10px rgba(161, 140, 209, 0.3);
}

.comment-content-wrap {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 700;
  color: var(--text-color);
  font-size: 1.05rem;
}

.comment-time {
  font-size: 0.85rem;
  color: #7f8c8d;
}

.comment-text {
  line-height: 1.6;
  color: #34495e;
}

.btn-delete {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border: none;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  opacity: 0;
}
.comment-item:hover .btn-delete {
  opacity: 1;
}
.btn-delete:hover {
  background: #e74c3c;
  color: white;
}

.no-comments {
  text-align: center;
  color: #7f8c8d;
  padding: 20px 0;
  font-style: italic;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  color: rgba(44, 62, 80, 0.6);
  font-size: 1.2rem;
  font-weight: 600;
}

@media (max-width: 768px) {
  .article {
    padding: 20px;
  }
  .article__title {
    font-size: 1.8rem;
  }
  .comments-section {
    padding: 20px;
  }
}
</style>

