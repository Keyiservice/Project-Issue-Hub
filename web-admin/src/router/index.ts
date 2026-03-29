import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/BasicLayout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/dashboard/index.vue') },
        { path: 'issue-projects', component: () => import('@/views/issue/project-selector.vue') },
        { path: 'issues', component: () => import('@/views/issue/list.vue') },
        { path: 'issues/:id', component: () => import('@/views/issue/detail.vue') },
        { path: 'projects', component: () => import('@/views/project/index.vue') },
        { path: 'stats', component: () => import('@/views/stats/index.vue') },
        { path: 'system/users', component: () => import('@/views/system/user.vue') },
        { path: 'system/dicts', component: () => import('@/views/system/dict.vue') }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.path !== '/login' && !authStore.token) {
    return '/login'
  }
  if (to.path === '/login' && authStore.token) {
    return '/dashboard'
  }
  return true
})

export default router
