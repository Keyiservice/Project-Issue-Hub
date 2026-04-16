<template>
  <div class="user-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Identity</p>
        <h2 class="opl-page-title">{{ t('page.users') }}</h2>
        <p class="opl-page-lead">{{ t('user.lead') }}</p>
      </div>
      <div class="opl-page-head-actions">
        <el-button @click="loadData">{{ t('common.action.refresh') }}</el-button>
        <el-button @click="openImportDialog">{{ t('user.importUsers') }}</el-button>
        <el-button type="primary" @click="openCreateDialog">{{ t('user.createUser') }}</el-button>
      </div>
    </section>

    <section class="opl-grid-4">
      <el-card class="opl-stat-card">
        <div class="opl-stat-label">用户总数</div>
        <div class="opl-stat-value">{{ total }}</div>
      </el-card>
      <el-card class="opl-stat-card">
        <div class="opl-stat-label">本页已绑定微信</div>
        <div class="opl-stat-value">{{ boundCount }}</div>
      </el-card>
      <el-card class="opl-stat-card">
        <div class="opl-stat-label">本页待改密</div>
        <div class="opl-stat-value">{{ passwordPendingCount }}</div>
      </el-card>
      <el-card class="opl-stat-card">
        <div class="opl-stat-label">本页参与项目</div>
        <div class="opl-stat-value">{{ projectUserCount }}</div>
      </el-card>
    </section>

    <el-card>
      <div class="filter-grid">
          <el-form-item :label="t('common.label.keyword')">
            <el-input v-model="query.keyword" :placeholder="t('user.searchKeywordPlaceholder')" clearable />
          </el-form-item>
        <el-form-item :label="t('common.label.status')">
          <el-select v-model="query.status" :placeholder="t('issueList.allStatus')" clearable>
            <el-option :label="t('common.status.enabled')" value="ENABLED" />
            <el-option :label="t('common.status.disabled')" value="DISABLED" />
            <el-option :label="t('common.status.locked')" value="LOCKED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.departmentCode')">
          <el-input v-model="query.departmentCode" placeholder="例如 FS21_ENG" clearable />
        </el-form-item>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">{{ t('common.action.search') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.action.reset') }}</el-button>
      </div>
    </el-card>

    <el-card>
      <el-table :data="list">
        <el-table-column :label="t('common.label.username')" min-width="200">
          <template #default="{ row }">
            <div class="user-main">
              <div class="user-name-line">
                <span class="user-real-name">{{ row.realName }}</span>
                <span class="user-username">{{ row.username }}</span>
              </div>
              <div class="user-meta">{{ row.userNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.label.department')" min-width="160">
          <template #default="{ row }">
            <div class="user-meta-strong">{{ row.departmentName || '-' }}</div>
            <div class="user-meta">{{ row.departmentCode || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.label.role')" min-width="180">
          <template #default="{ row }">
            <div class="tag-stack">
              <el-tag v-for="role in getDisplayRoleNames(row)" :key="role" effect="plain">{{ role }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.label.projectCount')" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openProjectsDrawer(row)">
              {{ row.projectCount || 0 }} 个
            </el-button>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.label.wechatBinding')" width="140">
          <template #default="{ row }">
            <el-tag :type="row.wechatBound ? 'success' : 'info'">
              {{ row.wechatBound ? t('common.status.bound') : t('common.status.unbound') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.label.passwordStatus')" width="140">
          <template #default="{ row }">
            <el-tag :type="row.passwordChangeRequired ? 'warning' : 'success'">
              {{ row.passwordChangeRequired ? t('common.status.passwordPending') : t('common.status.passwordUpdated') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.label.recentLogin')" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="t('common.action.viewDetail')" width="280" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">{{ t('common.action.edit') }}</el-button>
              <el-button link @click="openProjectsDrawer(row)">{{ t('common.label.projectCount') }}</el-button>
              <el-button link type="primary" @click="handleResetPassword(row)">{{ t('user.passwordReset') }}</el-button>
              <el-button link type="warning" :disabled="!row.wechatBound" @click="handleUnbindWechat(row)">{{ t('user.wechatUnbound') }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>

  <el-dialog v-model="createDialogVisible" :title="t('user.createUserTitle')" width="760px">
    <el-form :model="createForm" label-position="top" class="dialog-grid">
      <el-form-item :label="t('common.label.username')">
        <el-input v-model="createForm.username" placeholder="例如 zhang.peng / 工号" />
      </el-form-item>
      <el-form-item :label="t('common.label.realName')">
        <el-input v-model="createForm.realName" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item :label="t('common.label.departmentCode')">
        <el-input v-model="createForm.departmentCode" placeholder="例如 FS21_ENG" />
      </el-form-item>
      <el-form-item :label="t('common.label.department')">
        <el-input v-model="createForm.departmentName" placeholder="例如 FS21 Engineering" />
      </el-form-item>
      <el-form-item :label="t('common.label.mobile')">
        <el-input v-model="createForm.mobile" placeholder="可选" />
      </el-form-item>
      <el-form-item :label="t('common.label.email')">
        <el-input v-model="createForm.email" placeholder="可选" />
      </el-form-item>
      <el-form-item :label="t('common.label.role')" class="full-span">
        <el-select v-model="createForm.roleCodes" multiple filterable :placeholder="t('user.roleRequired')">
          <el-option v-for="role in roleOptions" :key="role.roleCode" :label="role.roleName" :value="role.roleCode" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('user.initialPassword')" class="full-span">
        <el-input v-model="createForm.initialPassword" placeholder="留空则使用 123456" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createDialogVisible = false">{{ t('common.action.cancel') }}</el-button>
      <el-button type="primary" @click="submitCreate">{{ t('common.action.create') }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="editDialogVisible" :title="t('user.editUserTitle')" width="760px">
    <el-form :model="editForm" label-position="top" class="dialog-grid">
      <el-form-item :label="t('common.label.username')">
        <el-input v-model="editForm.username" placeholder="例如 zhang.peng / 工号" />
      </el-form-item>
      <el-form-item :label="t('common.label.realName')">
        <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item :label="t('common.label.departmentCode')">
        <el-input v-model="editForm.departmentCode" placeholder="例如 FS21_ENG" />
      </el-form-item>
      <el-form-item :label="t('common.label.department')">
        <el-input v-model="editForm.departmentName" placeholder="例如 FS21 Engineering" />
      </el-form-item>
      <el-form-item :label="t('common.label.mobile')">
        <el-input v-model="editForm.mobile" placeholder="可选" />
      </el-form-item>
      <el-form-item :label="t('common.label.email')">
        <el-input v-model="editForm.email" placeholder="可选" />
      </el-form-item>
      <el-form-item :label="t('common.label.role')" class="full-span">
        <el-select v-model="editForm.roleCodes" multiple filterable :placeholder="t('user.roleRequired')">
          <el-option v-for="role in roleOptions" :key="role.roleCode" :label="role.roleName" :value="role.roleCode" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialogVisible = false">{{ t('common.action.cancel') }}</el-button>
      <el-button type="primary" @click="submitEdit">{{ t('common.action.save') }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="importDialogVisible" :title="t('user.importTitle')" width="860px">
    <div class="import-guide">
      <div>每行一个用户，格式：</div>
      <code>账号,姓名,部门编码,部门名称,角色代码,手机号,邮箱</code>
      <div>角色代码多个时用 <code>|</code> 分隔，例如 <code>SITE_USER|PROJECT_MANAGER</code></div>
    </div>
    <el-form :model="importForm" label-position="top">
      <el-form-item :label="t('user.initialPassword')">
        <el-input v-model="importForm.initialPassword" placeholder="留空则使用 123456" />
      </el-form-item>
      <el-form-item :label="t('common.action.import')">
        <el-input
          v-model="importForm.rawText"
          type="textarea"
          :rows="12"
          placeholder="zhang.peng,张鹏,FS21_ENG,FS21 Engineering,SITE_USER,13800000000,zhang@example.com"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="importDialogVisible = false">{{ t('common.action.cancel') }}</el-button>
      <el-button type="primary" @click="submitImport">{{ t('common.action.import') }}</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="projectDrawerVisible" :size="760" destroy-on-close>
    <template #header>
      <div class="drawer-head" v-if="activeUser">
        <div>
          <div class="user-real-name">{{ activeUser.realName }}</div>
          <div class="user-meta">{{ activeUser.username }}</div>
        </div>
        <div class="drawer-meta">参与项目 {{ userProjects.length }}</div>
      </div>
    </template>

    <div class="project-drawer-stack">
      <el-empty v-if="!userProjects.length" description="当前用户还没有加入任何项目团队" />
      <el-table v-else :data="userProjects">
        <el-table-column label="项目" min-width="240">
          <template #default="{ row }">
            <div class="user-main">
              <div class="user-name-line">
                <span class="user-real-name">{{ row.projectName }}</span>
                <span class="user-username">{{ row.projectNo }}</span>
              </div>
              <div class="user-meta">{{ row.customerName || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="项目岗位" min-width="160">
          <template #default="{ row }">
            <div class="tag-stack">
              <el-tag effect="plain">{{ row.projectRoleName }}</el-tag>
              <el-tag v-if="row.projectManager" type="success" effect="plain">项目经理</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="问题权限" min-width="180">
          <template #default="{ row }">
            <div class="tag-stack">
              <el-tag v-if="row.canAssignIssue" effect="plain">可分派</el-tag>
              <el-tag v-if="row.canVerifyIssue" effect="plain" type="warning">可验证</el-tag>
              <el-tag v-if="row.canCloseIssue" effect="plain" type="danger">可关闭</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goProjectTeam(row.projectId)">项目团队</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createUser,
  fetchRoleOptions,
  fetchUserPage,
  fetchUserProjects,
  importUsers,
  resetUserPassword,
  unbindUserWechat,
  updateUser,
  type RoleOption,
  type UserImportItemPayload,
  type UserListItem,
  type UserProjectParticipationItem
} from '@/api/user'
import { getRoleLabel } from '@/utils/view-mappers'

const router = useRouter()
const { t } = useI18n()
const ACCESS_ROLE_CODES = ['SITE_USER', 'RESP_ENGINEER', 'PROJECT_MANAGER', 'MANAGEMENT', 'ADMIN']
const list = ref<UserListItem[]>([])
const total = ref(0)
const roleOptions = ref<RoleOption[]>([])
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const importDialogVisible = ref(false)
const projectDrawerVisible = ref(false)
const activeUser = ref<UserListItem>()
const userProjects = ref<UserProjectParticipationItem[]>([])

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: '',
  departmentCode: ''
})

const createForm = reactive({
  username: '',
  realName: '',
  mobile: '',
  email: '',
  departmentCode: '',
  departmentName: '',
  roleCodes: [] as string[],
  initialPassword: ''
})

const editForm = reactive({
  id: undefined as number | undefined,
  username: '',
  realName: '',
  mobile: '',
  email: '',
  departmentCode: '',
  departmentName: '',
  roleCodes: [] as string[]
})

const importForm = reactive({
  initialPassword: '',
  rawText: ''
})

const boundCount = computed(() => list.value.filter((item) => item.wechatBound).length)
const passwordPendingCount = computed(() => list.value.filter((item) => item.passwordChangeRequired).length)
const projectUserCount = computed(() => list.value.filter((item) => (item.projectCount || 0) > 0).length)

function formatDateTime(value?: string) {
  if (!value) {
    return '-'
  }
  return value.replace('T', ' ').slice(0, 16)
}

async function loadData() {
  const { data } = await fetchUserPage(query)
  list.value = data.records
  total.value = data.total
}

async function loadRoles() {
  const { data } = await fetchRoleOptions()
  roleOptions.value = data
}

function handleSearch() {
  query.current = 1
  loadData()
}

function resetQuery() {
  Object.assign(query, {
    current: 1,
    size: 10,
    keyword: '',
    status: '',
    departmentCode: ''
  })
  loadData()
}

function resetCreateForm() {
  Object.assign(createForm, {
    username: '',
    realName: '',
    mobile: '',
    email: '',
    departmentCode: '',
    departmentName: '',
    roleCodes: [],
    initialPassword: ''
  })
}

function getDisplayRoleCodes(roleCodes: string[]) {
  const hasNonAccessRole = roleCodes.some((code) => !ACCESS_ROLE_CODES.includes(code))
  const hasOtherAccessRole = roleCodes.some((code) => ACCESS_ROLE_CODES.includes(code) && code !== 'SITE_USER')
  if (hasNonAccessRole && !hasOtherAccessRole) {
    return roleCodes.filter((code) => code !== 'SITE_USER')
  }
  return [...roleCodes]
}

function getDisplayRoleNames(row: UserListItem) {
  const displayCodes = new Set(getDisplayRoleCodes(row.roleCodes))
  return row.roleCodes
    .filter((code) => displayCodes.has(code))
    .map((code) => getRoleLabel(code))
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

function openEditDialog(row: UserListItem) {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    mobile: row.mobile || '',
    email: row.email || '',
    departmentCode: row.departmentCode || '',
    departmentName: row.departmentName || '',
    roleCodes: getDisplayRoleCodes(row.roleCodes)
  })
  editDialogVisible.value = true
}

function openImportDialog() {
  importForm.initialPassword = ''
  importForm.rawText = ''
  importDialogVisible.value = true
}

async function openProjectsDrawer(row: UserListItem) {
  activeUser.value = row
  projectDrawerVisible.value = true
  const { data } = await fetchUserProjects(row.id)
  userProjects.value = data
}

function goProjectTeam(projectId: number) {
  projectDrawerVisible.value = false
  router.push({
    path: '/projects',
    query: {
      focusProjectId: String(projectId),
      openMembers: '1'
    }
  })
}

async function submitCreate() {
  if (!createForm.username.trim() || !createForm.realName.trim()) {
    ElMessage.warning(t('user.usernameRequired'))
    return
  }
  if (!createForm.roleCodes.length) {
    ElMessage.warning(t('user.roleRequired'))
    return
  }
  await createUser({
    username: createForm.username.trim(),
    realName: createForm.realName.trim(),
    mobile: createForm.mobile.trim() || undefined,
    email: createForm.email.trim() || undefined,
    departmentCode: createForm.departmentCode.trim() || undefined,
    departmentName: createForm.departmentName.trim() || undefined,
    roleCodes: createForm.roleCodes,
    initialPassword: createForm.initialPassword.trim() || undefined
  })
  ElMessage.success(t('user.created'))
  createDialogVisible.value = false
  await loadData()
}

async function submitEdit() {
  if (!editForm.id) {
    return
  }
  if (!editForm.username.trim() || !editForm.realName.trim()) {
    ElMessage.warning(t('user.usernameRequired'))
    return
  }
  if (!editForm.roleCodes.length) {
    ElMessage.warning(t('user.roleRequired'))
    return
  }
  await updateUser(editForm.id, {
    username: editForm.username.trim(),
    realName: editForm.realName.trim(),
    mobile: editForm.mobile.trim() || undefined,
    email: editForm.email.trim() || undefined,
    departmentCode: editForm.departmentCode.trim() || undefined,
    departmentName: editForm.departmentName.trim() || undefined,
    roleCodes: editForm.roleCodes
  })
  ElMessage.success(t('user.updated'))
  editDialogVisible.value = false
  await loadData()
}

function parseImportRows(rawText: string) {
  const rows = rawText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  const items: UserImportItemPayload[] = []
  const localErrors: string[] = []

  rows.forEach((line, index) => {
    const parts = line.split(/,|，|\t/).map((item) => item.trim())
    const firstCell = parts[0]?.toLowerCase()
    if (index === 0 && ['账号', 'username', 'user_name'].includes(firstCell)) {
      return
    }
    if (parts.length < 5) {
      localErrors.push(`第 ${index + 1} 行字段不足，至少需要 5 列`)
      return
    }
    const [username, realName, departmentCode, departmentName, roleCodesRaw, mobile, email] = parts
    if (!username || !realName || !roleCodesRaw) {
      localErrors.push(`第 ${index + 1} 行缺少账号、姓名或角色代码`)
      return
    }
    items.push({
      username,
      realName,
      departmentCode: departmentCode || undefined,
      departmentName: departmentName || undefined,
      roleCodes: roleCodesRaw.split(/[|/]/).map((item) => item.trim()).filter(Boolean),
      mobile: mobile || undefined,
      email: email || undefined
    })
  })

  return { items, localErrors }
}

async function submitImport() {
  const { items, localErrors } = parseImportRows(importForm.rawText)
  if (localErrors.length) {
    ElMessageBox.alert(localErrors.join('\n'), '导入格式错误', { type: 'warning' })
    return
  }
  if (!items.length) {
    ElMessage.warning('请先粘贴导入内容')
    return
  }

  const { data } = await importUsers({
    items,
    initialPassword: importForm.initialPassword.trim() || undefined
  })

  const lines = [
    `总数 ${data.totalCount}`,
    `成功 ${data.successCount}`,
    `失败 ${data.failedCount}`
  ]
  if (data.failedMessages.length) {
    lines.push('', ...data.failedMessages)
  }
  await ElMessageBox.alert(lines.join('\n'), '导入结果', {
    type: data.failedCount ? 'warning' : 'success'
  })
  importDialogVisible.value = false
  await loadData()
}

async function handleResetPassword(row: UserListItem) {
  await ElMessageBox.confirm(
    `确认将 ${row.realName} 的密码重置为初始密码 123456，并要求首次登录后修改吗？`,
    '重置密码',
    { type: 'warning' }
  )
  await resetUserPassword(row.id)
  ElMessage.success(t('user.passwordReset'))
  await loadData()
}

async function handleUnbindWechat(row: UserListItem) {
  await ElMessageBox.confirm(
    `确认解绑 ${row.realName} 当前绑定的微信吗？解绑后需要重新绑定企业账号。`,
    '解绑微信',
    { type: 'warning' }
  )
  await unbindUserWechat(row.id)
  ElMessage.success(t('user.wechatUnbound'))
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadData(), loadRoles()])
})
</script>

<style scoped>
.user-page {
  display: grid;
  gap: 16px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 16px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.user-main {
  display: grid;
  gap: 8px;
}

.user-name-line {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.user-real-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--opl-text);
}

.user-username {
  font-size: 13px;
  color: var(--opl-muted);
}

.user-meta,
.user-meta-strong,
.drawer-meta {
  color: var(--opl-muted);
}

.user-meta-strong {
  color: var(--opl-text);
  font-weight: 600;
}

.tag-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full-span {
  grid-column: 1 / -1;
}

.import-guide {
  display: grid;
  gap: 8px;
  margin-bottom: 18px;
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(24, 33, 43, 0.06);
  color: var(--opl-muted);
}

.import-guide code {
  padding: 2px 6px;
  border-radius: 8px;
  background: rgba(203, 93, 31, 0.08);
  color: var(--opl-accent-deep);
}

.drawer-head,
.project-drawer-stack {
  display: grid;
  gap: 16px;
}

@media (max-width: 960px) {
  .filter-grid,
  .dialog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
