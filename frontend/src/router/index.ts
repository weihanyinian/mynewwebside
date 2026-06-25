import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
      meta: { title: '维寒一念的小站' }
    },
    // ─── Blog ───
    {
      path: '/blog',
      name: 'blog',
      component: () => import('../pages/BlogPage.vue'),
      meta: { title: '博客 - 维寒一念的小站' }
    },
    {
      path: '/blog/:id',
      name: 'article',
      component: () => import('../pages/ArticlePage.vue'),
      meta: { title: '文章 - 维寒一念的小站' }
    },
    // ─── Guestbook ───
    {
      path: '/guestbook',
      name: 'guestbook',
      component: () => import('../pages/GuestbookPage.vue'),
      meta: { title: '留言板 - 维寒一念的小站' }
    },
    // ─── Games ───
    {
      path: '/games',
      name: 'games',
      component: () => import('../pages/GamesPage.vue'),
      meta: { title: '小游戏 - 维寒一念的小站' }
    },
    {
      path: '/games/:id',
      name: 'game',
      component: () => import('../pages/GamePage.vue'),
      meta: { title: '小游戏 - 维寒一念的小站' }
    },
    // ─── Tools ───
    {
      path: '/tools',
      name: 'tools',
      component: () => import('../pages/ToolsPage.vue'),
      meta: { title: '工具箱 - 维寒一念的小站' }
    },
    {
      path: '/tools/reaction',
      name: 'tool-reaction',
      component: () => import('../pages/tools/ReactionPage.vue'),
      meta: { title: '反应力测试 - 维寒一念的小站' }
    },
    {
      path: '/tools/cps',
      name: 'tool-cps',
      component: () => import('../pages/tools/CpsPage.vue'),
      meta: { title: 'CPS测试 - 维寒一念的小站' }
    },
    {
      path: '/tools/pomodoro',
      name: 'tool-pomodoro',
      component: () => import('../pages/tools/PomodoroPage.vue'),
      meta: { title: '番茄钟 - 维寒一念的小站' }
    },
    {
      path: '/tools/schulte',
      name: 'tool-schulte',
      component: () => import('../pages/tools/SchultePage.vue'),
      meta: { title: '舒尔特方格 - 维寒一念的小站' }
    },
    {
      path: '/tools/mbti',
      name: 'tool-mbti',
      component: () => import('../pages/tools/MbtiPage.vue'),
      meta: { title: 'MBTI测试 - 维寒一念的小站' }
    },
    // ─── About ───
    {
      path: '/about',
      name: 'about',
      component: () => import('../pages/AboutPage.vue'),
      meta: { title: '关于我 - 维寒一念的小站' }
    },
    // ─── Login ───
    {
      path: '/login',
      name: 'login',
      component: () => import('../pages/LoginPage.vue'),
      meta: { title: '登录 - 维寒一念的小站' }
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../pages/LoginPage.vue'),
      meta: { title: '登录 - 维寒一念的小站' }
    },
    // ─── Admin Dashboard ───
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../pages/admin/DashboardPage.vue'),
      meta: { title: '管理后台 - 维寒一念的小站', requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/articles',
      name: 'admin-articles',
      component: () => import('../pages/admin/ArticleManager.vue'),
      meta: { title: '文章管理 - 维寒一念的小站', requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/guestbooks',
      name: 'admin-guestbooks',
      component: () => import('../pages/admin/GuestbookManager.vue'),
      meta: { title: '留言管理 - 维寒一念的小站', requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/visitors',
      name: 'admin-visitors',
      component: () => import('../pages/admin/VisitorLogPage.vue'),
      meta: { title: '访问日志 - 维寒一念的小站', requiresAuth: true, requiresAdmin: true }
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

// Navigation guard for admin pages
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    next('/admin/login')
  } else if (to.meta.requiresAdmin && role !== 'ADMIN') {
    next('/')
  } else {
    next()
  }
})

// Update document title
router.afterEach((to) => {
  document.title = (to.meta.title as string) || '维寒一念的小站'
})
