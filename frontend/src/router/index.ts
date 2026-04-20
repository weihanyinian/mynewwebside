import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'
import { getSafeInternalPath } from '../utils/safeRedirect'
import { useUserStore } from '../stores/user'

const PortfolioPage = () => import('../pages/public/PortfolioPage.vue')
const HomePage = () => import('../pages/public/BlogHome.vue')
const ArticlePage = () => import('../pages/public/ArticlePage.vue')
const CategoriesPage = () => import('../pages/public/CategoriesPage.vue')
const TagsPage = () => import('../pages/public/TagsPage.vue')
const MessageWallPage = () => import('../pages/public/MessageWallPage.vue')
const MoyuPage = () => import('../pages/public/MoyuPage.vue')
const Game2048 = () => import('../pages/moyu/Game2048.vue')
const GameTetris = () => import('../pages/moyu/GameTetris.vue')
const GameSnake = () => import('../pages/moyu/GameSnake.vue')
const GameMinesweeper = () => import('../pages/moyu/GameMinesweeper.vue')
const GameTrex = () => import('../pages/moyu/GameTrex.vue')
const GameFlappy = () => import('../pages/moyu/GameFlappy.vue')
const GameBreakout = () => import('../pages/moyu/GameBreakout.vue')
const GameGomoku = () => import('../pages/moyu/GameGomoku.vue')
const OjView = () => import('../pages/oj/OjView.vue')
const OjProblemList = () => import('../pages/oj/OjProblemList.vue')
const OjProblemDetail = () => import('../pages/oj/OjProblemDetail.vue')
const OjMySubmissions = () => import('../pages/oj/OjMySubmissions.vue')
const ToolsHubPage = () => import('../pages/tools/ToolsHubPage.vue')
const ToolReactionPage = () => import('../pages/tools/ToolReactionPage.vue')
const ToolCpsPage = () => import('../pages/tools/ToolCpsPage.vue')
const ToolPomodoroPage = () => import('../pages/tools/ToolPomodoroPage.vue')
const ToolPasswordPage = () => import('../pages/tools/ToolPasswordPage.vue')
const ToolBase64Page = () => import('../pages/tools/ToolBase64Page.vue')
const LoginPage = () => import('../pages/auth/LoginPage.vue')
const RegisterPage = () => import('../pages/auth/RegisterPage.vue')
const AdminArticlesPage = () => import('../pages/admin/AdminArticlesPage.vue')
const AdminEditorPage = () => import('../pages/admin/AdminEditorPage.vue')
const AdminCategoriesPage = () => import('../pages/admin/AdminCategoriesPage.vue')
const AdminTagsPage = () => import('../pages/admin/AdminTagsPage.vue')
const AdminWallPage = () => import('../pages/admin/AdminWallPage.vue')
const AdminUsersPage = () => import('../pages/admin/AdminUsersPage.vue')
const AdminOjProblemsPage = () => import('../pages/admin/AdminOjProblemsPage.vue')
const AdminOjSubmissionsPage = () => import('../pages/admin/AdminOjSubmissionsPage.vue')
const AlbumsPage = () => import('../pages/public/AlbumsPage.vue')
const StatsPage = () => import('../pages/public/StatsPage.vue')
const MusicCenterPage = () => import('../pages/music/MusicCenterPage.vue')
const MbtiTestPage = () => import('../pages/tools/MbtiTestPage.vue')
const GameGuessNumber = () => import('../pages/moyu/GameGuessNumber.vue')
const GameMemoryCard = () => import('../pages/moyu/GameMemoryCard.vue')
const MemoriesPage = () => import('../pages/public/MemoriesPage.vue')

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: PortfolioPage },
    { path: '/blog', component: HomePage },
    { path: '/article/:id', component: ArticlePage },
    { path: '/categories', component: CategoriesPage },
    { path: '/tags', component: TagsPage },
    { path: '/message', component: MessageWallPage },
    { path: '/moyu', component: MoyuPage },
    { path: '/moyu/2048', component: Game2048 },
    { path: '/moyu/tetris', component: GameTetris },
    { path: '/moyu/snake', component: GameSnake },
    { path: '/moyu/minesweeper', component: GameMinesweeper },
    { path: '/moyu/trex', component: GameTrex },
    { path: '/moyu/flappy', component: GameFlappy },
    { path: '/moyu/breakout', component: GameBreakout },
    { path: '/moyu/gomoku', component: GameGomoku },
    { path: '/login', component: LoginPage },
    { path: '/register', component: RegisterPage, meta: { guestOnly: true } },
    { path: '/archives', redirect: '/blog' },
    { path: '/friends', redirect: '/' },
    { path: '/search', redirect: '/blog' },
    { path: '/albums', component: AlbumsPage },
    { path: '/snippets', redirect: '/tools' },
    { path: '/stats', component: StatsPage },
    { path: '/music', component: MusicCenterPage, meta: { requiresAuth: true } },
    { path: '/memories', component: MemoriesPage, meta: { requiresAuth: true } },
    { path: '/tools/mbti', component: MbtiTestPage },
    { path: '/moyu/guess', component: GameGuessNumber },
    { path: '/moyu/memory-card', component: GameMemoryCard },
    { path: '/admin/login', redirect: '/login' },
    { path: '/tools', component: ToolsHubPage },
    { path: '/tools/reaction', component: ToolReactionPage },
    { path: '/tools/cps', component: ToolCpsPage },
    { path: '/tools/pomodoro', component: ToolPomodoroPage },
    { path: '/tools/password', component: ToolPasswordPage },
    { path: '/tools/base64', component: ToolBase64Page },
    {
      path: '/tools/oj',
      component: OjView,
      meta: { requiresAuth: true },
      children: [
        { path: '', component: OjProblemList },
        { path: 'submissions', component: OjMySubmissions },
        { path: 'p/:id', component: OjProblemDetail },
      ],
    },
    { path: '/oj', redirect: '/tools/oj' },
    {
      path: '/oj/:pathMatch(.*)*',
      redirect: (to) => {
        const raw = to.params.pathMatch
        if (!raw) return '/tools/oj'
        const suffix = Array.isArray(raw) ? raw.filter(Boolean).join('/') : String(raw)
        return suffix ? `/tools/oj/${suffix}` : '/tools/oj'
      },
    },

    { path: '/admin/friends', redirect: '/admin/articles' },
    { path: '/admin', redirect: '/admin/articles' },
    { path: '/admin/articles', component: AdminArticlesPage, meta: { requiresAdmin: true } },
    { path: '/admin/editor', component: AdminEditorPage, meta: { requiresAdmin: true } },
    { path: '/admin/editor/:id', component: AdminEditorPage, meta: { requiresAdmin: true } },
    { path: '/admin/categories', component: AdminCategoriesPage, meta: { requiresAdmin: true } },
    { path: '/admin/tags', component: AdminTagsPage, meta: { requiresAdmin: true } },
    { path: '/admin/messages', component: AdminWallPage, meta: { requiresAdmin: true } },
    { path: '/admin/users', component: AdminUsersPage, meta: { requiresAdmin: true } },
    { path: '/admin/oj/problems', component: AdminOjProblemsPage, meta: { requiresAdmin: true } },
    { path: '/admin/oj/submissions', component: AdminOjSubmissionsPage, meta: { requiresAdmin: true } },
  ],
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth', top: 80 }
    }
    if (to.path !== from.path) {
      return { top: 0, behavior: 'smooth' }
    }
    return {}
  },
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  userStore.hydrateFromStorage()

  if (to.meta.requiresAuth || to.meta.requiresAdmin) {
    if (!getToken()) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    const needRefresh = !!to.meta.requiresAdmin || !userStore.profile
    if (needRefresh) {
      try {
        await userStore.fetchMe()
      } catch {
        userStore.logout()
        return { path: '/login', query: { redirect: to.fullPath } }
      }
    }
  }

  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return { path: '/' }
  }

  // 已登录用户访问 guestOnly 页面（如注册页）直接跳走
  if (to.meta.guestOnly && getToken()) {
    const next = getSafeInternalPath(to.query.redirect)
    return next || '/'
  }

  if (to.path === '/login' && getToken()) {
    const next = getSafeInternalPath(to.query.redirect)
    return next || '/'
  }

  return true
})
