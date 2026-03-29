<template>
  <div class="issue-list-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Project Issue Library</p>
        <h2 class="opl-page-title">项目问题库</h2>
        <p class="opl-page-lead">先锁定项目，再看该项目自己的 OPL 和推进情况。</p>
      </div>
      <div class="opl-page-head-actions">
        <el-button plain :loading="exporting" @click="handleExportReport">导出 Report</el-button>
        <el-button type="primary" @click="openCreateDialog">新建问题</el-button>
        <el-button plain @click="switchProject">切换项目</el-button>
      </div>
    </section>

    <el-card v-if="currentProject" class="context-card">
      <div class="context-head">
        <div>
          <div class="context-no">{{ currentProject.projectNo }}</div>
          <h3 class="context-title">{{ currentProject.projectName }}</h3>
          <p class="context-desc">{{ currentProject.customerName }} / 项目经理 {{ currentProject.projectManagerName || '-' }}</p>
        </div>
        <el-tag :type="getProjectStatusMeta(currentProject.status).tagType">
          {{ getProjectStatusMeta(currentProject.status).label }}
        </el-tag>
      </div>

      <div class="opl-inline-meta">
        <span><strong>开始：</strong>{{ currentProject.startDate || '-' }}</span>
        <span><strong>计划结束：</strong>{{ currentProject.plannedEndDate || '-' }}</span>
      </div>
    </el-card>

    <section class="opl-grid-4">
      <el-card v-for="card in summaryCards" :key="card.label" class="opl-stat-card">
        <div class="opl-stat-label">{{ card.label }}</div>
        <div class="opl-stat-value">{{ card.value }}</div>
        <div class="opl-stat-note">{{ card.note }}</div>
      </el-card>
    </section>

    <el-card>
      <template #header>
        <div>
          <h3 class="opl-card-title">筛选条件</h3>
          <p class="opl-card-subtitle">列表可以按条件缩小范围，但顶部统计始终按整个项目口径统计。</p>
        </div>
      </template>

      <el-form :model="query" class="filter-grid">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="问题编号 / 标题 / 描述" clearable />
        </el-form-item>
        <el-form-item label="责任人">
          <el-select v-model="query.ownerId" placeholder="全部责任人" clearable filterable>
            <el-option
              v-for="item in memberOptions"
              :key="item.userId"
              :label="`${item.realName || item.username} / ${item.projectRoleName}`"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable>
            <el-option v-for="item in issueStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="query.priority" placeholder="全部优先级" clearable>
            <el-option v-for="item in issuePriorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="超期筛选">
          <div class="overdue-switch">
            <el-checkbox v-model="query.overdueOnly">仅看超期问题</el-checkbox>
          </div>
        </el-form-item>
      </el-form>

      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="table-head">
          <div>
            <h3 class="opl-card-title">问题清单</h3>
            <p class="opl-card-subtitle">分页只影响列表，不影响项目统计。</p>
          </div>
          <div class="table-head-actions">
            <span class="table-caption">共 {{ total }} 条</span>
            <el-button type="primary" plain @click="openCreateDialog">录入新问题</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list">
        <el-table-column label="问题信息" min-width="320">
          <template #default="{ row }">
            <div class="issue-primary">
              <div class="issue-no">{{ row.issueNo }}<span v-if="row.oplNo"> / OPL {{ row.oplNo }}</span></div>
              <div class="issue-title">{{ row.title }}</div>
              <div class="issue-subline">
                <span>责任人 {{ row.ownerName || '待分派' }}</span>
                <span>提交人 {{ row.reporterName || '-' }}</span>
                <span>Pilot {{ row.pilotName || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="优先级" width="110">
          <template #default="{ row }">
            <el-tag :type="getIssuePriorityMeta(row.priority).tagType">
              {{ getIssuePriorityMeta(row.priority).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getIssueStatusMeta(row.status).tagType">
              {{ getIssueStatusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="截止时间" width="180">
          <template #default="{ row }">
            <div class="deadline-cell">
              <span>{{ row.dueAt || '-' }}</span>
              <span v-if="row.overdue" class="deadline-overdue">已超期</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">查看详情</el-button>
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

    <el-dialog v-model="createDialogVisible" title="新建问题" width="920px" destroy-on-close>
      <div v-if="currentProject" class="create-project-bar">
        <span class="create-project-label">当前项目</span>
        <strong>{{ currentProject.projectName }}</strong>
        <span class="create-project-meta">{{ currentProject.projectNo }}</span>
      </div>

      <el-form :model="createForm" label-position="top" class="create-grid">
        <el-form-item label="问题标题" class="full-span">
          <el-input v-model="createForm.title" maxlength="200" show-word-limit placeholder="一句话说清问题本质" />
        </el-form-item>
        <el-form-item label="问题描述" class="full-span">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            placeholder="补充现场现象、影响和当前判断"
          />
        </el-form-item>
        <el-form-item label="问题分类">
          <el-select v-model="createForm.categoryCode" placeholder="选择分类">
            <el-option v-for="item in issueCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题来源">
          <el-select v-model="createForm.sourceCode" placeholder="选择来源">
            <el-option v-for="item in issueSourceOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="createForm.priority" placeholder="选择优先级">
            <el-option v-for="item in issuePriorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响等级">
          <el-select v-model="createForm.impactLevel" placeholder="选择影响等级">
            <el-option v-for="item in issueImpactOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备">
          <el-input v-model="createForm.deviceName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="模块">
          <el-input v-model="createForm.moduleName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="责任人">
          <el-select v-model="createForm.ownerId" placeholder="可选" clearable filterable>
            <el-option
              v-for="item in memberOptions"
              :key="item.userId"
              :label="`${item.realName || item.username} / ${item.projectRoleName}`"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发生时间">
          <el-date-picker
            v-model="createForm.occurredAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="可选"
          />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="createForm.dueAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="可选"
          />
        </el-form-item>
        <el-form-item label="现场标记" class="full-span">
          <div class="toggle-row">
            <el-checkbox v-model="createForm.affectShipment">影响出货</el-checkbox>
            <el-checkbox v-model="createForm.affectCommissioning">影响联调</el-checkbox>
            <el-checkbox v-model="createForm.needShutdown">需要停机</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="附件" class="full-span">
          <el-upload
            v-model:file-list="attachmentFileList"
            class="create-uploader"
            multiple
            :limit="6"
            :http-request="uploadCreateAttachment"
            :before-upload="beforeAttachmentUpload"
            :on-success="handleAttachmentSuccess"
            :on-remove="handleAttachmentRemove"
          >
            <el-button plain>上传图片 / 视频</el-button>
            <template #tip>
              <div class="upload-tip">支持图片和视频，单文件不超过 5MB。</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">提交问题</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type UploadProps, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { createIssue, fetchIssuePage, uploadIssueAttachment, type FileUploadResult, type IssueAttachmentPayload } from '@/api/issue'
import {
  exportProjectIssueReport,
  fetchProjectAll,
  fetchProjectIssueSummary,
  fetchProjectMembers,
  type ProjectIssueSummary,
  type ProjectItem,
  type ProjectMemberItem
} from '@/api/project'
import type { IssueItem } from '@/types/api'
import {
  getIssueImpactMeta,
  getIssuePriorityMeta,
  getIssueStatusMeta,
  getProjectStatusMeta,
  issuePriorityOptions,
  issueStatusOptions
} from '@/utils/view-mappers'

const issueCategoryOptions = [
  { value: 'MECHANICAL', label: '机械' },
  { value: 'ELECTRICAL', label: '电气' },
  { value: 'AUTOMATION', label: '自动化' },
  { value: 'PROCESS', label: '工艺' },
  { value: 'QUALITY', label: '质量' },
  { value: 'DELIVERY', label: '交付' },
  { value: 'OTHER', label: '其他' }
]

const issueSourceOptions = [
  { value: 'SITE_CHECK', label: '现场巡检' },
  { value: 'CUSTOMER_FEEDBACK', label: '客户反馈' },
  { value: 'COMMISSIONING', label: '联调发现' },
  { value: 'INTERNAL_REVIEW', label: '内部评审' }
]

const issueImpactOptions = [
  { value: 'LOW', label: getIssueImpactMeta('LOW').label },
  { value: 'MEDIUM', label: getIssueImpactMeta('MEDIUM').label },
  { value: 'HIGH', label: getIssueImpactMeta('HIGH').label },
  { value: 'CRITICAL', label: getIssueImpactMeta('CRITICAL').label }
]

const route = useRoute()
const router = useRouter()
const list = ref<IssueItem[]>([])
const total = ref(0)
const projectOptions = ref<ProjectItem[]>([])
const currentProject = ref<ProjectItem>()
const memberOptions = ref<ProjectMemberItem[]>([])
const createDialogVisible = ref(false)
const creating = ref(false)
const exporting = ref(false)
const attachmentFileList = ref<UploadUserFile[]>([])
const createAttachments = ref<IssueAttachmentPayload[]>([])
const projectSummary = ref<ProjectIssueSummary>({
  totalIssues: 0,
  openIssues: 0,
  closedIssues: 0,
  highPriorityIssues: 0,
  unassignedIssues: 0,
  processingIssues: 0,
  overdueIssues: 0
})

const createForm = reactive({
  title: '',
  description: '',
  categoryCode: 'OTHER',
  sourceCode: 'SITE_CHECK',
  priority: 'MEDIUM',
  impactLevel: 'MEDIUM',
  deviceName: '',
  moduleName: '',
  ownerId: undefined as number | undefined,
  occurredAt: '',
  dueAt: '',
  affectShipment: false,
  affectCommissioning: false,
  needShutdown: false
})

const query = reactive({
  current: 1,
  size: 10,
  keyword: '',
  projectId: undefined as number | undefined,
  ownerId: undefined as number | undefined,
  status: '',
  priority: '',
  overdueOnly: false
})

const summaryCards = computed(() => {
  return [
    { label: '高优先级', value: projectSummary.value.highPriorityIssues, note: '整项目范围内待优先处理的问题。' },
    { label: '待分派', value: projectSummary.value.unassignedIssues, note: '整项目范围内尚未明确责任人的问题。' },
    { label: '处理中 / 待验证', value: projectSummary.value.processingIssues, note: '整项目范围内正在推进闭环的问题。' },
    { label: '超期', value: projectSummary.value.overdueIssues, note: '整项目范围内已经超期的问题。' }
  ]
})

function applyProjectContext() {
  const rawProjectId = Number(route.query.projectId)
  if (!rawProjectId) {
    router.replace('/issue-projects')
    return false
  }
  query.projectId = rawProjectId
  currentProject.value = projectOptions.value.find((item) => item.id === rawProjectId)
  if (!currentProject.value && projectOptions.value.length) {
    router.replace('/issue-projects')
    return false
  }
  return true
}

async function loadData() {
  if (!query.projectId) {
    return
  }
  const { data } = await fetchIssuePage({
    current: query.current,
    size: query.size,
    keyword: query.keyword,
    projectId: query.projectId,
    ownerId: query.ownerId,
    priority: query.priority,
    overdueOnly: query.overdueOnly,
    statusList: query.status ? [query.status] : undefined
  })
  list.value = data.records
  total.value = data.total
}

async function loadProjectSummary() {
  if (!query.projectId) {
    return
  }
  const { data } = await fetchProjectIssueSummary(query.projectId)
  projectSummary.value = data
}

async function loadMemberOptions() {
  if (!query.projectId) {
    memberOptions.value = []
    return
  }
  const { data } = await fetchProjectMembers(query.projectId)
  memberOptions.value = data
}

async function loadOptions() {
  const { data } = await fetchProjectAll()
  projectOptions.value = data
  if (applyProjectContext()) {
    await Promise.all([loadData(), loadProjectSummary(), loadMemberOptions()])
  }
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
    projectId: query.projectId,
    ownerId: undefined,
    status: '',
    priority: '',
    overdueOnly: false
  })
  loadData()
}

function resetCreateForm() {
  Object.assign(createForm, {
    title: '',
    description: '',
    categoryCode: 'OTHER',
    sourceCode: 'SITE_CHECK',
    priority: 'MEDIUM',
    impactLevel: 'MEDIUM',
    deviceName: '',
    moduleName: '',
    ownerId: undefined,
    occurredAt: '',
    dueAt: '',
    affectShipment: false,
    affectCommissioning: false,
    needShutdown: false
  })
  attachmentFileList.value = []
  createAttachments.value = []
}

function openCreateDialog() {
  resetCreateForm()
  createDialogVisible.value = true
}

function switchProject() {
  router.push('/issue-projects')
}

async function handleExportReport() {
  if (!query.projectId || !currentProject.value) {
    ElMessage.warning('请先选择项目')
    return
  }

  exporting.value = true
  try {
    const response = await exportProjectIssueReport(query.projectId, {
      keyword: query.keyword || undefined,
      ownerId: query.ownerId,
      priority: query.priority || undefined,
      overdueOnly: query.overdueOnly || undefined,
      statusList: query.status ? [query.status] : undefined
    })
    const blobUrl = URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = `project-issue-report-${currentProject.value.projectNo || query.projectId}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(blobUrl)
    ElMessage.success('Report 已开始下载')
  } finally {
    exporting.value = false
  }
}

function goDetail(id: number) {
  router.push({
    path: `/issues/${id}`,
    query: {
      projectId: String(query.projectId)
    }
  })
}

function beforeAttachmentUpload(file: File) {
  const isValidSize = file.size <= 5 * 1024 * 1024
  const isValidType = file.type.startsWith('image/') || file.type.startsWith('video/')
  if (!isValidType) {
    ElMessage.warning('只支持图片和视频')
    return false
  }
  if (!isValidSize) {
    ElMessage.warning('单文件不能超过 5MB')
    return false
  }
  return true
}

async function uploadCreateAttachment(options: UploadRequestOptions) {
  try {
    const result = await uploadIssueAttachment(options.file as File)
    options.onSuccess(result.data as FileUploadResult)
  } catch (error) {
    options.onError(error as any)
  }
}

const handleAttachmentSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  const uploaded = response as FileUploadResult
  createAttachments.value.push({
    fileName: uploaded.fileName,
    objectKey: uploaded.objectKey,
    fileUrl: uploaded.fileUrl,
    fileSize: uploaded.fileSize,
    contentType: uploaded.contentType
  })
  uploadFile.url = uploaded.fileUrl
}

const handleAttachmentRemove: UploadProps['onRemove'] = (uploadFile) => {
  const uploaded = uploadFile.response as FileUploadResult | undefined
  const objectKey = uploaded?.objectKey
  if (!objectKey) {
    return
  }
  createAttachments.value = createAttachments.value.filter((item) => item.objectKey !== objectKey)
}

async function submitCreate() {
  if (!currentProject.value) {
    ElMessage.warning('请先选择项目')
    return
  }
  if (!createForm.title.trim()) {
    ElMessage.warning('请填写问题标题')
    return
  }
  creating.value = true
  try {
    const { data: issueId } = await createIssue({
      title: createForm.title.trim(),
      description: createForm.description.trim() || undefined,
      projectId: currentProject.value.id,
      projectName: currentProject.value.projectName,
      categoryCode: createForm.categoryCode,
      sourceCode: createForm.sourceCode,
      priority: createForm.priority,
      impactLevel: createForm.impactLevel,
      deviceName: createForm.deviceName.trim() || undefined,
      moduleName: createForm.moduleName.trim() || undefined,
      ownerId: createForm.ownerId,
      occurredAt: createForm.occurredAt || undefined,
      dueAt: createForm.dueAt || undefined,
      affectShipment: createForm.affectShipment,
      affectCommissioning: createForm.affectCommissioning,
      needShutdown: createForm.needShutdown,
      attachments: createAttachments.value
    })
    ElMessage.success('问题已创建')
    createDialogVisible.value = false
    await Promise.all([loadData(), loadProjectSummary()])
    goDetail(issueId)
  } finally {
    creating.value = false
  }
}

watch(
  () => route.query.projectId,
  () => {
    if (projectOptions.value.length && applyProjectContext()) {
      query.current = 1
      loadData()
      loadProjectSummary()
      loadMemberOptions()
    }
  }
)

onMounted(loadOptions)
</script>

<style scoped>
.issue-list-page {
  display: grid;
  gap: 16px;
}

.context-card {
  background:
    radial-gradient(circle at right top, rgba(15, 118, 110, 0.14), transparent 30%),
    radial-gradient(circle at left, rgba(203, 93, 31, 0.12), transparent 26%),
    linear-gradient(145deg, rgba(255, 251, 246, 0.94), rgba(255, 255, 255, 0.8));
}

.context-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.context-no {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.context-title {
  margin: 10px 0 0;
  font-size: 28px;
  color: var(--opl-text);
}

.context-desc {
  margin: 12px 0 0;
  color: var(--opl-muted);
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px 16px;
}

.filter-grid :deep(.el-form-item) {
  margin-bottom: 16px;
}

.overdue-switch {
  display: flex;
  align-items: center;
  min-height: 40px;
}

.filter-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.table-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.table-head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.table-caption {
  font-size: 13px;
  color: var(--opl-muted);
}

.issue-primary {
  display: grid;
  gap: 8px;
}

.issue-no {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.issue-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--opl-text);
  line-height: 1.6;
}

.issue-subline {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  color: var(--opl-muted);
  font-size: 13px;
}

.deadline-cell {
  display: grid;
  gap: 4px;
}

.deadline-overdue {
  font-size: 12px;
  color: var(--opl-rose);
  font-weight: 700;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.create-project-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(24, 33, 43, 0.06);
  color: var(--opl-muted);
}

.create-project-label,
.create-project-meta {
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.create-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full-span {
  grid-column: 1 / -1;
}

.toggle-row {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  min-height: 40px;
}

.create-uploader {
  width: 100%;
}

.upload-tip {
  color: var(--opl-muted);
}

@media (max-width: 1120px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .table-head,
  .context-head,
  .create-project-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .create-grid {
    grid-template-columns: 1fr;
  }
}
</style>
