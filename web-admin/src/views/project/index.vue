<template>
  <div class="project-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Project Registry</p>
        <h2 class="opl-page-title">项目管理</h2>
      </div>
      <div v-if="canManageProjects" class="opl-page-head-actions">
        <el-button type="primary" @click="openCreateDialog">新建项目</el-button>
      </div>
    </section>

    <section class="opl-grid-4">
      <el-card v-for="card in summaryCards" :key="card.label" class="opl-stat-card">
        <div class="opl-stat-label">{{ card.label }}</div>
        <div class="opl-stat-value">{{ card.value }}</div>
        <div class="card-note">{{ card.note }}</div>
      </el-card>
    </section>

    <el-card>
      <div class="toolbar">
        <el-form :model="query" class="filter-grid" @submit.prevent>
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" placeholder="项目编号 / 项目名称 / 客户" clearable />
          </el-form-item>
          <el-form-item label="项目状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable>
              <el-option
                v-for="item in projectStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="toolbar-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-card>
      <div class="table-head">
        <div>
          <h3 class="opl-card-title">项目列表</h3>
        </div>
        <span class="table-caption">共 {{ total }} 个项目</span>
      </div>

      <el-table :data="list">
        <el-table-column label="项目" min-width="280">
          <template #default="{ row }">
            <div class="project-main">
              <div class="project-no">{{ row.projectNo }}</div>
              <div class="project-name">{{ row.projectName }}</div>
              <div class="project-customer">{{ row.customerName }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="项目经理" min-width="120" prop="projectManagerName" />
        <el-table-column label="团队人数" width="100" align="center">
          <template #default="{ row }">
            <span class="member-count">{{ row.teamSize || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getProjectStatusMeta(row.status).tagType">
              {{ getProjectStatusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计划结束" width="140" prop="plannedEndDate" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="enterIssueLibrary(row.id)">问题库</el-button>
              <el-button link @click="openMemberDrawer(row)">项目团队</el-button>
              <el-button
                v-if="canManageProjects"
                link
                type="danger"
                @click="removeProject(row)"
              >
                删除
              </el-button>
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

  <el-dialog v-model="dialogVisible" title="新建项目" width="760px">
    <el-form :model="form" label-position="top" class="dialog-grid">
      <el-form-item label="项目编号">
        <el-input v-model="form.projectNo" placeholder="例如 NSD-2026-001" />
      </el-form-item>
      <el-form-item label="项目名称">
        <el-input v-model="form.projectName" placeholder="请输入项目名称" />
      </el-form-item>
      <el-form-item label="客户名称">
        <el-input v-model="form.customerName" placeholder="请输入客户名称" />
      </el-form-item>
      <el-form-item label="项目经理">
        <el-select v-model="form.projectManagerId" placeholder="请选择项目经理" filterable @change="handleManagerChange">
          <el-option v-for="user in userOptions" :key="user.id" :label="user.realName" :value="user.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目状态">
        <el-select v-model="form.status">
          <el-option
            v-for="item in projectStatusOptions.slice(0, 3)"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择开始日期" />
      </el-form-item>
      <el-form-item label="计划结束">
        <el-date-picker v-model="form.plannedEndDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择计划结束" />
      </el-form-item>
      <el-form-item label="项目说明" class="full-span">
        <el-input v-model="form.description" type="textarea" :rows="4" placeholder="补充项目背景和交付节点" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitCreate">创建项目</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="memberDrawerVisible" :size="920" destroy-on-close>
    <template #header>
      <div class="drawer-head" v-if="activeProject">
        <div>
          <div class="project-no">{{ activeProject.projectNo }}</div>
          <div class="drawer-title">{{ activeProject.projectName }} 项目团队</div>
        </div>
        <div class="drawer-meta">
          <span>项目经理 {{ activeProject.projectManagerName || '-' }}</span>
          <span>成员 {{ projectMembers.length }}</span>
        </div>
      </div>
    </template>

    <div class="team-stack" v-if="activeProject">
      <div class="team-toolbar">
        <p class="team-tip">问题分派只允许从当前项目团队中选择。</p>
        <div class="toolbar-actions">
          <el-button v-if="canManageProjects" @click="handleSyncMembersFromIssues">从 OPL 同步成员</el-button>
          <el-button v-if="canManageProjects" @click="openImportDialog">批量导入</el-button>
          <el-button v-if="canManageProjects" type="primary" @click="openCreateMemberDialog">新增成员</el-button>
        </div>
      </div>

      <el-table :data="projectMembers">
        <el-table-column label="成员" min-width="220">
          <template #default="{ row }">
            <div class="member-main">
              <div class="member-name">{{ row.realName || row.username }}</div>
              <div class="member-dept">{{ row.departmentName || row.departmentCode || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="项目岗位" min-width="160">
          <template #default="{ row }">
            <div class="member-role-cell">
              <span>{{ row.projectRoleName }}</span>
              <el-tag v-if="row.projectManager" type="success" effect="plain">项目经理</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="问题权限" min-width="220">
          <template #default="{ row }">
            <div class="member-tags">
              <el-tag v-if="row.canAssignIssue" effect="plain">可分派</el-tag>
              <el-tag v-if="row.canVerifyIssue" effect="plain" type="warning">可验证</el-tag>
              <el-tag v-if="row.canCloseIssue" effect="plain" type="danger">可关闭</el-tag>
              <span v-if="!row.canAssignIssue && !row.canVerifyIssue && !row.canCloseIssue" class="empty-inline">普通成员</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="180" prop="remark" />
        <el-table-column v-if="canManageProjects" label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openEditMemberDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="removeMember(row)">移除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-drawer>

  <el-dialog v-model="memberDialogVisible" :title="memberDialogMode === 'create' ? '新增项目成员' : '编辑项目成员'" width="640px">
    <el-form :model="memberForm" label-position="top" class="dialog-grid">
      <el-form-item label="成员">
        <el-select
          v-model="memberForm.userId"
          filterable
          placeholder="请选择成员"
          :disabled="memberDialogMode === 'edit'"
        >
          <el-option
            v-for="user in availableUserOptions"
            :key="user.id"
            :label="`${user.realName} / ${user.departmentName || user.departmentCode || '未分配部门'}`"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="项目岗位">
        <el-select v-model="memberForm.projectRoleCode" placeholder="请选择项目岗位">
          <el-option
            v-for="item in memberRoleOptions"
            :key="item.code"
            :label="item.name"
            :value="item.code"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="项目经理">
        <el-switch v-model="memberForm.projectManager" @change="handleProjectManagerToggle" />
      </el-form-item>
      <el-form-item label="可分派问题">
        <el-switch v-model="memberForm.canAssignIssue" :disabled="memberForm.projectManager" />
      </el-form-item>
      <el-form-item label="可验证问题">
        <el-switch v-model="memberForm.canVerifyIssue" :disabled="memberForm.projectManager" />
      </el-form-item>
      <el-form-item label="可关闭问题">
        <el-switch v-model="memberForm.canCloseIssue" :disabled="memberForm.projectManager" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="memberForm.sortNo" :min="0" :max="999" />
      </el-form-item>
      <el-form-item label="备注" class="full-span">
        <el-input v-model="memberForm.remark" type="textarea" :rows="3" placeholder="补充岗位职责或范围" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="memberDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitMember">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="importDialogVisible" title="批量导入项目成员" width="860px">
    <div class="import-guide">
      <div>每行一个成员，格式：</div>
      <code>账号,项目岗位代码,是否项目经理,可分派,可验证,可关闭,排序,备注</code>
      <div>布尔字段可填 <code>Y/N</code>、<code>是/否</code>、<code>1/0</code>。</div>
      <div>项目岗位代码示例：<code>PROJECT_MANAGER</code>、<code>MECH_ENGINEER</code>、<code>ELECTRICAL_TECH</code>。</div>
    </div>
    <el-form :model="importForm" label-position="top">
      <el-form-item label="导入内容">
        <el-input
          v-model="importForm.rawText"
          type="textarea"
          :rows="12"
          placeholder="zhang.peng,MECH_ENGINEER,N,Y,N,N,10,机械负责人"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="importDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitImport">开始导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createProject,
  createProjectMember,
  deleteProject,
  deleteProjectMember,
  fetchProjectAll,
  fetchProjectMemberRoleOptions,
  fetchProjectMembers,
  fetchProjectPage,
  importProjectMembers,
  syncProjectMembersFromIssues,
  type ProjectItem,
  type ProjectMemberImportItemPayload,
  type ProjectMemberItem,
  type ProjectMemberRoleOption,
  updateProjectMember
} from '@/api/project'
import { fetchUserOptions, type UserOption } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { getProjectStatusMeta, projectStatusOptions } from '@/utils/view-mappers'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const list = ref<ProjectItem[]>([])
const total = ref(0)
const userOptions = ref<UserOption[]>([])
const memberRoleOptions = ref<ProjectMemberRoleOption[]>([])
const projectMembers = ref<ProjectMemberItem[]>([])
const dialogVisible = ref(false)
const memberDrawerVisible = ref(false)
const memberDialogVisible = ref(false)
const importDialogVisible = ref(false)
const activeProject = ref<ProjectItem>()
const editingMemberId = ref<number>()
const memberDialogMode = ref<'create' | 'edit'>('create')
const routeProjectHandled = ref(false)

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  status: ''
})

const form = reactive({
  projectNo: '',
  projectName: '',
  customerName: '',
  projectManagerId: undefined as number | undefined,
  projectManagerName: '',
  status: 'PLANNING',
  startDate: '',
  plannedEndDate: '',
  description: ''
})

const memberForm = reactive({
  userId: undefined as number | undefined,
  projectRoleCode: '',
  projectManager: false,
  canAssignIssue: false,
  canVerifyIssue: false,
  canCloseIssue: false,
  sortNo: 0,
  remark: ''
})

const importForm = reactive({
  rawText: ''
})

const canManageProjects = computed(() => authStore.roles.includes('ADMIN') || authStore.roles.includes('PROJECT_MANAGER'))

const summaryCards = computed(() => {
  const planningCount = list.value.filter((item) => item.status === 'PLANNING').length
  const progressCount = list.value.filter((item) => item.status === 'IN_PROGRESS').length
  const totalTeamSize = list.value.reduce((sum, item) => sum + (item.teamSize || 0), 0)
  return [
    { label: '当前项目', value: list.value.length, note: '当前筛选范围内的项目数量' },
    { label: '进行中', value: progressCount, note: '正在推进的问题项目' },
    { label: '规划中', value: planningCount, note: '尚未进入交付节奏的项目' },
    { label: '团队成员', value: totalTeamSize, note: '已配置到项目内的成员数' }
  ]
})

const memberUserIds = computed(() => new Set(projectMembers.value.map((item) => item.userId)))

const availableUserOptions = computed(() =>
  userOptions.value.filter((item) => item.id === memberForm.userId || !memberUserIds.value.has(item.id))
)

async function loadData() {
  const { data } = await fetchProjectPage(query)
  list.value = data.records
  total.value = data.total
  if (activeProject.value) {
    activeProject.value = data.records.find((item) => item.id === activeProject.value?.id) || activeProject.value
  }
  await tryOpenProjectFromRoute()
}

async function loadUsers() {
  const { data } = await fetchUserOptions()
  userOptions.value = data
}

async function loadMemberRoleOptions() {
  const { data } = await fetchProjectMemberRoleOptions()
  memberRoleOptions.value = data
}

async function loadProjectMembers(projectId: number) {
  const { data } = await fetchProjectMembers(projectId)
  projectMembers.value = data
}

function openCreateDialog() {
  dialogVisible.value = true
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
    status: ''
  })
  loadData()
}

function handleManagerChange(value: number) {
  const manager = userOptions.value.find((item) => item.id === value)
  form.projectManagerName = manager?.realName || ''
}

function enterIssueLibrary(projectId: number) {
  router.push({
    path: '/issues',
    query: {
      projectId: String(projectId)
    }
  })
}

async function removeProject(project: ProjectItem) {
  await ElMessageBox.confirm(
    `确认删除项目 ${project.projectName} 吗？仅允许删除没有问题单的项目。`,
    '删除项目',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  await deleteProject(project.id)
  ElMessage.success('项目已删除')
  if (activeProject.value?.id === project.id) {
    memberDrawerVisible.value = false
    activeProject.value = undefined
    projectMembers.value = []
  }
  if (list.value.length === 1 && query.current > 1) {
    query.current -= 1
  }
  await loadData()
}

async function submitCreate() {
  if (!form.projectManagerId) {
    ElMessage.warning('请选择项目经理')
    return
  }
  const manager = userOptions.value.find((item) => item.id === form.projectManagerId)
  form.projectManagerName = manager?.realName || ''
  await createProject({
    ...form,
    projectManagerId: Number(form.projectManagerId),
    projectManagerName: form.projectManagerName
  })
  ElMessage.success('项目已创建')
  dialogVisible.value = false
  Object.assign(form, {
    projectNo: '',
    projectName: '',
    customerName: '',
    projectManagerId: undefined,
    projectManagerName: '',
    status: 'PLANNING',
    startDate: '',
    plannedEndDate: '',
    description: ''
  })
  await loadData()
}

async function openMemberDrawer(project: ProjectItem) {
  activeProject.value = project
  memberDrawerVisible.value = true
  await loadProjectMembers(project.id)
}

async function tryOpenProjectFromRoute() {
  if (routeProjectHandled.value) {
    return
  }
  const rawProjectId = Number(route.query.focusProjectId)
  if (!rawProjectId || route.query.openMembers !== '1') {
    return
  }
  let target = list.value.find((item) => item.id === rawProjectId)
  if (!target) {
    const { data } = await fetchProjectAll()
    target = data.find((item) => item.id === rawProjectId)
  }
  if (!target) {
    return
  }
  routeProjectHandled.value = true
  await openMemberDrawer(target)
}

function resetMemberForm() {
  Object.assign(memberForm, {
    userId: undefined,
    projectRoleCode: memberRoleOptions.value[0]?.code || '',
    projectManager: false,
    canAssignIssue: false,
    canVerifyIssue: false,
    canCloseIssue: false,
    sortNo: 0,
    remark: ''
  })
}

function openCreateMemberDialog() {
  memberDialogMode.value = 'create'
  editingMemberId.value = undefined
  resetMemberForm()
  memberDialogVisible.value = true
}

function openEditMemberDialog(member: ProjectMemberItem) {
  memberDialogMode.value = 'edit'
  editingMemberId.value = member.id
  Object.assign(memberForm, {
    userId: member.userId,
    projectRoleCode: member.projectRoleCode,
    projectManager: member.projectManager,
    canAssignIssue: member.canAssignIssue,
    canVerifyIssue: member.canVerifyIssue,
    canCloseIssue: member.canCloseIssue,
    sortNo: member.sortNo || 0,
    remark: member.remark || ''
  })
  memberDialogVisible.value = true
}

function handleProjectManagerToggle(value: boolean | string | number) {
  if (!value) {
    return
  }
  memberForm.canAssignIssue = true
  memberForm.canVerifyIssue = true
  memberForm.canCloseIssue = true
}

async function submitMember() {
  if (!activeProject.value) {
    return
  }
  if (!memberForm.userId) {
    ElMessage.warning('请选择成员')
    return
  }
  if (!memberForm.projectRoleCode) {
    ElMessage.warning('请选择项目岗位')
    return
  }

  const payload = {
    userId: Number(memberForm.userId),
    projectRoleCode: memberForm.projectRoleCode,
    projectManager: memberForm.projectManager,
    canAssignIssue: memberForm.canAssignIssue,
    canVerifyIssue: memberForm.canVerifyIssue,
    canCloseIssue: memberForm.canCloseIssue,
    sortNo: memberForm.sortNo,
    remark: memberForm.remark
  }

  if (memberDialogMode.value === 'create') {
    await createProjectMember(activeProject.value.id, payload)
    ElMessage.success('项目成员已新增')
  } else if (editingMemberId.value) {
    await updateProjectMember(activeProject.value.id, editingMemberId.value, payload)
    ElMessage.success('项目成员已更新')
  }

  memberDialogVisible.value = false
  await Promise.all([loadProjectMembers(activeProject.value.id), loadData()])
}

async function removeMember(member: ProjectMemberItem) {
  if (!activeProject.value) {
    return
  }
  await ElMessageBox.confirm(`确认移除成员 ${member.realName || member.username} 吗？`, '移除成员', {
    confirmButtonText: '移除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteProjectMember(activeProject.value.id, member.id)
  ElMessage.success('成员已移除')
  await Promise.all([loadProjectMembers(activeProject.value.id), loadData()])
}

function openImportDialog() {
  importForm.rawText = ''
  importDialogVisible.value = true
}

function parseBooleanFlag(value?: string) {
  const normalized = (value || '').trim().toLowerCase()
  return ['y', 'yes', 'true', '1', '是'].includes(normalized)
}

function parseImportRows(rawText: string) {
  const rows = rawText
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  const items: ProjectMemberImportItemPayload[] = []
  const localErrors: string[] = []

  rows.forEach((line, index) => {
    const parts = line.split(/,|，|\t/).map((item) => item.trim())
    const firstCell = parts[0]?.toLowerCase()
    if (index === 0 && ['账号', 'username', 'user_name'].includes(firstCell)) {
      return
    }
    if (parts.length < 2) {
      localErrors.push(`第 ${index + 1} 行至少需要 2 列`)
      return
    }

    const [username, projectRoleCode, projectManager, canAssignIssue, canVerifyIssue, canCloseIssue, sortNo, remark] = parts
    if (!username || !projectRoleCode) {
      localErrors.push(`第 ${index + 1} 行缺少账号或项目岗位代码`)
      return
    }

    items.push({
      username,
      projectRoleCode,
      projectManager: parseBooleanFlag(projectManager),
      canAssignIssue: parseBooleanFlag(canAssignIssue),
      canVerifyIssue: parseBooleanFlag(canVerifyIssue),
      canCloseIssue: parseBooleanFlag(canCloseIssue),
      sortNo: sortNo ? Number(sortNo) || 0 : 0,
      remark: remark || undefined
    })
  })

  return { items, localErrors }
}

async function submitImport() {
  if (!activeProject.value) {
    return
  }
  const { items, localErrors } = parseImportRows(importForm.rawText)
  if (localErrors.length) {
    ElMessageBox.alert(localErrors.join('\n'), '导入格式错误', { type: 'warning' })
    return
  }
  if (!items.length) {
    ElMessage.warning('请先粘贴导入内容')
    return
  }

  const { data } = await importProjectMembers(activeProject.value.id, { items })
  const lines = [`总数 ${data.totalCount}`, `成功 ${data.successCount}`, `失败 ${data.failedCount}`]
  if (data.failedMessages.length) {
    lines.push('', ...data.failedMessages)
  }
  await ElMessageBox.alert(lines.join('\n'), '导入结果', {
    type: data.failedCount ? 'warning' : 'success'
  })
  importDialogVisible.value = false
  await Promise.all([loadProjectMembers(activeProject.value.id), loadData()])
}

async function handleSyncMembersFromIssues() {
  if (!activeProject.value) {
    return
  }
  const { data } = await syncProjectMembersFromIssues(activeProject.value.id)
  const lines = [`识别 ${data.totalCount}`, `同步成功 ${data.successCount}`, `未匹配 ${data.failedCount}`]
  if (data.failedMessages.length) {
    lines.push('', ...data.failedMessages)
  }
  await ElMessageBox.alert(lines.join('\n'), '同步结果', {
    type: data.failedCount ? 'warning' : 'success'
  })
  await Promise.all([loadProjectMembers(activeProject.value.id), loadData()])
}

onMounted(async () => {
  await Promise.all([loadData(), loadUsers(), loadMemberRoleOptions()])
})
</script>

<style scoped>
.project-page {
  display: grid;
  gap: 16px;
}

.toolbar {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(220px, 280px));
  gap: 0 16px;
}

.toolbar-actions,
.row-actions,
.member-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.table-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.table-caption,
.card-note,
.project-customer,
.member-dept,
.team-tip,
.drawer-meta {
  color: var(--opl-muted);
}

.project-main,
.member-main {
  display: grid;
  gap: 6px;
}

.project-no {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.project-name,
.member-name,
.drawer-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--opl-text);
}

.member-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(24, 33, 43, 0.06);
  color: var(--opl-text);
  font-weight: 700;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full-span {
  grid-column: 1 / -1;
}

.drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.team-stack {
  display: grid;
  gap: 16px;
}

.team-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 4px;
}

.member-role-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.empty-inline {
  color: var(--opl-subtle);
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

@media (max-width: 1080px) {
  .dialog-grid,
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .toolbar,
  .table-head,
  .drawer-head,
  .team-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
