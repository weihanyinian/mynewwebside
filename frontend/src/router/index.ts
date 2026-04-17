import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'
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
const ToolsWidgetPage = () => import('../pages/tools/ToolsWidgetPage.vue')
const ToolMarkmapPage = () => import('../pages/tools/ToolMarkmapPage.vue')
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
    { path: '/register', component: RegisterPage },
    {
      path: '/admin/login',
      redirect: (to) => ({
        path: '/login',
        query: { ...to.query, redirect: (to.query.redirect as string) || '/admin/articles' },
      }),
    },
    { path: '/tools', component: ToolsHubPage },
    {
      path: '/tools/reaction',
      component: ToolsWidgetPage,
      meta: { toolId: 'reaction' },
    },
    {
      path: '/tools/cps',
      component: ToolsWidgetPage,
      meta: { toolId: 'cps' },
    },
    {
      path: '/tools/pomodoro',
      component: ToolsWidgetPage,
      meta: { toolId: 'pomodoro' },
    },
    {
      path: '/tools/password',
      component: ToolsWidgetPage,
      meta: { toolId: 'password' },
    },
    {
      path: '/tools/base64',
      component: ToolsWidgetPage,
      meta: { toolId: 'base64' },
    },
    { path: '/tools/markmap', component: ToolMarkmapPage },
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
    if (to.path !== from.path) return { top: 0 }
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

  if (to.path === '/login' && getToken()) {
    const redir = to.query.redirect as string
    if (redir) return redir
    return userStore.isAdmin ? '/admin/articles' : '/'
  }

  return true
})
