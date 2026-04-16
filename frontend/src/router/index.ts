import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'

const PortfolioPage = () => import('../pages/public/PortfolioPage.vue')
const HomePage = () => import('../pages/public/BlogHome.vue')
const ArticlePage = () => import('../pages/public/ArticlePage.vue')
const CategoriesPage = () => import('../pages/public/CategoriesPage.vue')
const TagsPage = () => import('../pages/public/TagsPage.vue')
const MessageWallPage = () => import('../pages/public/MessageWallPage.vue')
const MoyuPage = () => import('../pages/public/MoyuPage.vue')
const OjView = () => import('../pages/oj/OjView.vue')
const OjProblemList = () => import('../pages/oj/OjProblemList.vue')
const OjProblemDetail = () => import('../pages/oj/OjProblemDetail.vue')
const AdminLoginPage = () => import('../pages/admin/AdminLoginPage.vue')
const AdminArticlesPage = () => import('../pages/admin/AdminArticlesPage.vue')
const AdminEditorPage = () => import('../pages/admin/AdminEditorPage.vue')
const AdminCategoriesPage = () => import('../pages/admin/AdminCategoriesPage.vue')
const AdminTagsPage = () => import('../pages/admin/AdminTagsPage.vue')
const AdminWallPage = () => import('../pages/admin/AdminWallPage.vue')

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
    {
      path: '/oj',
      component: OjView,
      children: [
        { path: '', component: OjProblemList },
        { path: 'p/:id', component: OjProblemDetail },
      ],
    },

    { path: '/admin/login', component: AdminLoginPage },
    { path: '/admin', redirect: '/admin/articles' },
    { path: '/admin/articles', component: AdminArticlesPage, meta: { requiresAuth: true } },
    { path: '/admin/editor', component: AdminEditorPage, meta: { requiresAuth: true } },
    { path: '/admin/editor/:id', component: AdminEditorPage, meta: { requiresAuth: true } },
    { path: '/admin/categories', component: AdminCategoriesPage, meta: { requiresAuth: true } },
    { path: '/admin/tags', component: AdminTagsPage, meta: { requiresAuth: true } },
    { path: '/admin/messages', component: AdminWallPage, meta: { requiresAuth: true } },
  ],
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    if (to.path !== from.path) return { top: 0 }
    return {}
  },
})

/** 管理后台路由：meta.requiresAuth 时校验 JWT（与后端 /api/admin/** 的 ROLE_ADMIN 一致） */
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/admin/login' && getToken()) {
    return { path: '/admin/articles' }
  }
  return true
})

