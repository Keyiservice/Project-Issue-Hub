import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const issueRoles = ['SITE_USER', 'RESP_ENGINEER', 'PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN']
const managerRoles = ['PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN']
const adminRoles = ['ADMIN']

function hasRoutePermission(userRoles: string[], allowedRoles?: string[]) {
  if (!allowedRoles?.length) {
    return true
  }
  return allowedRoles.some((role) => userRoles.includes(role))
}

function firstAllowedPath(userRoles: string[]) {
  const candidates = [
    { path: '/dashboard', roles: managerRoles },
    { path: '/issue-projects', roles: issueRoles },
    { path: '/my-issues', roles: issueRoles },
    { path: '/projects', roles: issueRoles },
    { path: '/stats', roles: managerRoles },
    { path: '/system/users', roles: adminRoles },
    { path: '/system/dicts', roles: adminRoles }
  ]
  return candidates.find((item) => hasRoutePermission(userRoles, item.roles))?.path || '/login'
}

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
        { path: 'dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { roles: managerRoles } },
        { path: 'issue-projects', component: () => import('@/views/issue/project-selector.vue'), meta: { roles: issueRoles } },
        { path: 'my-issues', component: () => import('@/views/issue/my-issues.vue'), meta: { roles: issueRoles } },
        { path: 'issues', component: () => import('@/views/issue/list.vue'), meta: { roles: issueRoles } },
        { path: 'issues/:id', component: () => import('@/views/issue/detail.vue'), meta: { roles: issueRoles } },
        { path: 'projects', component: () => import('@/views/project/index.vue'), meta: { roles: issueRoles } },
        { path: 'stats', component: () => import('@/views/stats/index.vue'), meta: { roles: managerRoles } },
        { path: 'system/users', component: () => import('@/views/system/user.vue'), meta: { roles: adminRoles } },
        { path: 'system/dicts', component: () => import('@/views/system/dict.vue'), meta: { roles: adminRoles } }
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
    return firstAllowedPath(authStore.roles)
  }
  const allowedRoles = to.meta.roles as string[] | undefined
  if (to.path !== '/login' && authStore.token && !hasRoutePermission(authStore.roles, allowedRoles)) {
    return firstAllowedPath(authStore.roles)
  }
  return true
})

export default router
