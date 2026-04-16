<template>
  <div class="layout-shell">
    <aside class="sidebar">
      <div class="brand-panel">
        <p class="brand-kicker">{{ t('common.appName') }}</p>
        <h2 class="brand-title">{{ t('layout.brandTitle') }}</h2>
      </div>

      <div class="sidebar-section">
        <div class="section-meta">
          <span>{{ t('layout.navigation') }}</span>
          <span class="section-badge">{{ t('layout.projectFirst') }}</span>
        </div>
        <el-menu router :default-active="activeMenu" class="menu">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <div class="menu-item">
              <div class="menu-title">{{ item.label }}</div>
            </div>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="sidebar-footer">
        <div class="signal-card">
          <div class="signal-dot"></div>
          <div class="signal-title">{{ t('layout.timezone') }}</div>
        </div>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <p class="opl-section-title">{{ t('common.appName') }}</p>
          <h1 class="topbar-title">{{ currentTitle }}</h1>
        </div>

        <div class="topbar-actions">
          <div class="signal-pill">
            <span class="pill-dot"></span>
            <span>{{ nowLabel }}</span>
          </div>

          <el-select v-model="localeValue" class="language-select" size="large">
            <el-option
              v-for="item in localeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>

          <div class="identity-card">
            <div class="identity-avatar">{{ avatarText }}</div>
            <div>
              <div class="identity-name">{{ authStore.realName || t('common.empty.notLoggedIn') }}</div>
              <div class="identity-role">{{ roleText }}</div>
            </div>
          </div>

          <el-button plain @click="logout">{{ t('common.action.logout') }}</el-button>
        </div>
      </header>

      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { localeLabelMap, supportedLocales, type AppLocale } from '@/i18n/locales'
import { useAuthStore } from '@/stores/auth'
import { useLocaleStore } from '@/stores/locale'
import { getRoleLabel } from '@/utils/view-mappers'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const localeStore = useLocaleStore()
const { t, locale } = useI18n()

const now = ref(new Date())
let timer: number | null = null

const localeOptions = supportedLocales.map((value) => ({
  value,
  label: localeLabelMap[value]
}))

function hasAnyRole(allowedRoles?: string[]) {
  if (!allowedRoles?.length) {
    return true
  }
  return allowedRoles.some((role) => authStore.roles.includes(role))
}

const menuItems = computed(() =>
  [
    { path: '/dashboard', label: t('layout.menus.dashboard'), roles: ['PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN'] },
    { path: '/issue-projects', label: t('layout.menus.issueProjects'), roles: ['SITE_USER', 'RESP_ENGINEER', 'PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN'] },
    { path: '/my-issues', label: t('layout.menus.myIssues'), roles: ['SITE_USER', 'RESP_ENGINEER', 'PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN'] },
    { path: '/projects', label: t('layout.menus.projects'), roles: ['SITE_USER', 'RESP_ENGINEER', 'PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN'] },
    { path: '/stats', label: t('layout.menus.stats'), roles: ['PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN'] },
    { path: '/system/users', label: t('layout.menus.users'), roles: ['ADMIN'] },
    { path: '/system/dicts', label: t('layout.menus.dicts'), roles: ['ADMIN'] }
  ].filter((item) => hasAnyRole(item.roles))
)

const localeValue = computed({
  get: () => localeStore.currentLocale,
  set: (value: AppLocale) => localeStore.setLocale(value)
})

const titleMap: Record<string, string> = {
  '/dashboard': 'layout.titles.dashboard',
  '/issue-projects': 'layout.titles.issueProjects',
  '/my-issues': 'layout.titles.myIssues',
  '/issues': 'layout.titles.issues',
  '/projects': 'layout.titles.projects',
  '/stats': 'layout.titles.stats',
  '/system/users': 'layout.titles.users',
  '/system/dicts': 'layout.titles.dicts'
}

const currentTitle = computed(() => {
  if (route.path.startsWith('/issues/')) {
    return t('layout.titles.issueDetail')
  }
  const key = titleMap[route.path]
  return key ? t(key) : t('common.appName')
})

const activeMenu = computed(() => {
  if (route.path.startsWith('/my-issues')) {
    return '/my-issues'
  }
  if (route.path.startsWith('/issues')) {
    return '/issue-projects'
  }
  return route.path
})

const roleText = computed(() => {
  if (!authStore.roles.length) {
    return t('common.empty.noRole')
  }
  return authStore.roles.map((item) => getRoleLabel(item)).join(' / ')
})

const avatarText = computed(() => (authStore.realName || 'P').slice(0, 1).toUpperCase())

const nowLabel = computed(() =>
  new Intl.DateTimeFormat(locale.value, {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(now.value)
)

function logout() {
  authStore.signOut()
  router.push('/login')
}

onMounted(() => {
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 60000)
})

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer)
  }
})
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  min-height: 100vh;
}

.sidebar {
  display: flex;
  flex-direction: column;
  padding: 20px 18px;
  background:
    radial-gradient(circle at top, rgba(203, 93, 31, 0.28), transparent 26%),
    linear-gradient(180deg, rgba(18, 25, 33, 0.97), rgba(25, 33, 43, 0.94));
  color: #fff;
}

.brand-panel {
  padding: 24px 22px;
  border-radius: 28px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.03));
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.brand-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.52);
}

.brand-title {
  margin: 16px 0 0;
  font-size: 30px;
  line-height: 1.05;
}

.sidebar-section {
  margin-top: 24px;
  flex: 1;
}

.section-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 12px 12px;
  color: rgba(255, 255, 255, 0.48);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.section-badge {
  padding: 6px 10px;
  border-radius: 999px;
  letter-spacing: 0.04em;
  color: #ffd8c1;
  background: rgba(203, 93, 31, 0.12);
  border: 1px solid rgba(203, 93, 31, 0.24);
}

.menu-item {
  display: flex;
  align-items: center;
  min-height: 46px;
}

.menu-title {
  font-size: 15px;
  font-weight: 700;
}

.sidebar-footer {
  padding: 10px;
}

.signal-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.signal-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffba77, #cb5d1f);
  box-shadow: 0 0 0 6px rgba(203, 93, 31, 0.12);
}

.signal-title {
  font-weight: 700;
}

.workspace {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 26px 28px 12px;
}

.topbar-title {
  margin: 8px 0 0;
  font-size: 36px;
  line-height: 1.05;
  color: #17212b;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.signal-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 16px;
  border-radius: 999px;
  color: var(--opl-muted);
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(24, 33, 43, 0.08);
}

.pill-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--opl-teal);
}

.language-select {
  width: 140px;
}

.identity-card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 60px;
  padding: 10px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(24, 33, 43, 0.08);
}

.identity-avatar {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--opl-accent), var(--opl-teal));
  color: #fff;
  font-weight: 700;
}

.identity-name {
  font-weight: 700;
}

.identity-role {
  margin-top: 2px;
  font-size: 12px;
  color: var(--opl-muted);
}

.content {
  padding: 0 28px 28px;
}

@media (max-width: 1220px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .topbar,
  .content {
    padding-left: 18px;
    padding-right: 18px;
  }

  .topbar {
    flex-direction: column;
  }
}
</style>
