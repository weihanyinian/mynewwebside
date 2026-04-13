import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/token'

const PortfolioPage = () => import('../pages/public/PortfolioPage.vue')
const HomePage = () => import('../pages/public/HomePage.vue')
const ArticlePage = () => import('../pages/public/ArticlePage.vue')
const CategoriesPage = () => import('../pages/public/CategoriesPage.vue')
const TagsPage = () => import('../pages/public/TagsPage.vue')
const AdminLoginPage = () => import('../pages/admin/AdminLoginPage.vue')
const AdminArticlesPage = () => import('../pages/admin/AdminArticlesPage.vue')
const AdminEditorPage = () => import('../pages/admin/AdminEditorPage.vue')
const AdminCategoriesPage = () => import('../pages/admin/AdminCategoriesPage.vue')
const AdminTagsPage = () => import('../pages/admin/AdminTagsPage.vue')

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: PortfolioPage },
    { path: '/blog', component: HomePage },
    { path: '/article/:id', component: ArticlePage },
    { path: '/categories', component: CategoriesPage },
    { path: '/tags', component: TagsPage },

    { path: '/admin/login', component: AdminLoginPage },
    { path: '/admin', redirect: '/admin/articles' },
    { path: '/admin/articles', component: AdminArticlesPage, meta: { requiresAuth: true } },
    { path: '/admin/editor', component: AdminEditorPage, meta: { requiresAuth: true } },
    { path: '/admin/editor/:id', component: AdminEditorPage, meta: { requiresAuth: true } },
    { path: '/admin/categories', component: AdminCategoriesPage, meta: { requiresAuth: true } },
    { path: '/admin/tags', component: AdminTagsPage, meta: { requiresAuth: true } },
  ],
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    if (to.path !== from.path) return { top: 0 }
    return {}
  },
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getToken()) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/admin/login' && getToken()) {
    return { path: '/admin/articles' }
  }
  return true
})

