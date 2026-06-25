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
    {
      path: '/guestbook',
      name: 'guestbook',
      component: () => import('../pages/GuestbookPage.vue'),
      meta: { title: '留言板 - 维寒一念的小站' }
    },
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
    {
      path: '/about',
      name: 'about',
      component: () => import('../pages/AboutPage.vue'),
      meta: { title: '关于我 - 维寒一念的小站' }
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

// Update document title
router.afterEach((to) => {
  document.title = (to.meta.title as string) || '维寒一念的小站'
})
