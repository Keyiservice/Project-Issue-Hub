<template>
  <div class="layout-shell">
    <aside class="sidebar">
      <div class="brand-panel">
        <p class="brand-kicker">PROJECT ISSUE HUB</p>
        <h2 class="brand-title">项目问题协同平台</h2>
      </div>

      <div class="sidebar-section">
        <div class="section-meta">
          <span>Navigation</span>
          <span class="section-badge">Project First</span>
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
          <div class="signal-title">Asia/Shanghai</div>
        </div>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <p class="opl-section-title">Project Issue Hub</p>
          <h1 class="topbar-title">{{ currentTitle }}</h1>
        </div>

        <div class="topbar-actions">
          <div class="signal-pill">
            <span class="pill-dot"></span>
            <span>{{ nowLabel }}</span>
          </div>

          <div class="identity-card">
            <div class="identity-avatar">{{ avatarText }}</div>
            <div>
              <div class="identity-name">{{ authStore.realName || '未登录用户' }}</div>
              <div class="identity-role">{{ roleText }}</div>
            </div>
          </div>

          <el-button plain @click="logout">退出登录</el-button>
        </div>
      </header>

      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const menuItems = [
  { path: '/dashboard', label: '驾驶舱' },
  { path: '/issue-projects', label: '项目问题库' },
  { path: '/projects', label: '项目协同' },
  { path: '/stats', label: '统计分析' },
  { path: '/system/users', label: '用户管理' },
  { path: '/system/dicts', label: '字典配置' }
]

const titleMap: Record<string, string> = {
  '/dashboard': '管理驾驶舱',
  '/issue-projects': '项目问题库入口',
  '/issues': '项目问题清单',
  '/projects': '项目协同',
  '/stats': '统计分析',
  '/system/users': '用户与角色',
  '/system/dicts': '字典配置'
}

const currentTitle = computed(() => {
  if (route.path.startsWith('/issues/')) {
    return '问题详情'
  }
  return titleMap[route.path] || 'Project Issue Hub'
})

const activeMenu = computed(() => {
  if (route.path.startsWith('/issues')) {
    return '/issue-projects'
  }
  return route.path
})

const roleMap: Record<string, string> = {
  SITE_USER: '现场人员',
  RESP_ENGINEER: '责任工程师',
  PROJECT_MANAGER: '项目经理',
  MANAGEMENT: '管理层',
  ADMIN: '管理员'
}

const roleText = computed(() => {
  if (!authStore.roles.length) {
    return '未分配角色'
  }
  return authStore.roles.map((item) => roleMap[item] || item).join(' / ')
})

const avatarText = computed(() => (authStore.realName || '现').slice(0, 1))

const nowLabel = new Intl.DateTimeFormat('zh-CN', {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
}).format(new Date())

function logout() {
  authStore.signOut()
  router.push('/login')
}
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
