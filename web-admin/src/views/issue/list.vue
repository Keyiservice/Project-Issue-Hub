<template>
  <div class="issue-list-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Project Issue Library</p>
        <h2 class="opl-page-title">{{ t('page.issueList') }}</h2>
        <p class="opl-page-lead">{{ t('issueList.lead') }}</p>
      </div>
      <div class="opl-page-head-actions">
        <el-button plain :loading="exporting" @click="handleExportReport">{{ t('common.action.exportReport') }}</el-button>
        <el-button plain :loading="importing" @click="triggerImport">{{ t('common.action.import') }}</el-button>
        <el-button type="primary" @click="openCreateDialog">{{ t('issueList.issueCreate') }}</el-button>
        <el-button plain @click="switchProject">{{ t('common.action.switchProject') }}</el-button>
      </div>
    </section>
    <input ref="issueImportInputRef" class="hidden-file-input" type="file" accept=".xlsx,.xls" @change="handleImportChange" />

    <el-card v-if="currentProject" class="context-card">
      <div class="context-head">
        <div>
          <div class="context-no">{{ currentProject.projectNo }}</div>
          <h3 class="context-title">{{ currentProject.projectName }}</h3>
          <p class="context-desc">
            {{ currentProject.customerName }} / {{ t('common.label.projectManager') }} {{ currentProject.projectManagerName || '-' }}
          </p>
        </div>
        <el-tag :type="getProjectStatusMeta(currentProject.status).tagType">
          {{ getProjectStatusMeta(currentProject.status).label }}
        </el-tag>
      </div>

      <div class="opl-inline-meta">
        <span><strong>{{ t('common.label.startDate') }}:</strong>{{ currentProject.startDate || '-' }}</span>
        <span><strong>{{ t('common.label.plannedEndDate') }}:</strong>{{ currentProject.plannedEndDate || '-' }}</span>
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
          <h3 class="opl-card-title">{{ t('issueList.filterTitle') }}</h3>
          <p class="opl-card-subtitle">{{ t('issueList.filterSubtitle') }}</p>
        </div>
      </template>

      <el-form :model="query" class="filter-grid">
        <el-form-item :label="t('common.label.keyword')">
          <el-input v-model="query.keyword" :placeholder="t('issueList.issueNoPlaceholder')" clearable />
        </el-form-item>
        <el-form-item :label="t('common.label.owner')">
          <el-select v-model="query.ownerId" :placeholder="t('issueList.allOwners')" clearable filterable>
            <el-option
              v-for="item in memberOptions"
              :key="item.userId"
              :label="`${item.realName || item.username} / ${item.projectRoleName}`"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.issueFunction')">
          <el-select v-model="query.issueFunctionCode" :placeholder="t('issueList.allFunction')" clearable>
            <el-option v-for="item in issueFunctionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.status')">
          <el-select v-model="query.status" :placeholder="t('issueList.allStatus')" clearable>
            <el-option v-for="item in issueStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.priority')">
          <el-select v-model="query.priority" :placeholder="t('issueList.allPriority')" clearable>
            <el-option v-for="item in issuePriorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('issueList.overdueOnlyLabel')">
          <div class="overdue-switch">
            <el-checkbox v-model="query.overdueOnly">{{ t('issueList.overdueOnly') }}</el-checkbox>
          </div>
        </el-form-item>
      </el-form>

      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">{{ t('common.action.search') }}</el-button>
        <el-button @click="resetQuery">{{ t('common.action.reset') }}</el-button>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="table-head">
          <div>
            <h3 class="opl-card-title">{{ t('issueList.tableTitle') }}</h3>
            <p class="opl-card-subtitle">{{ t('issueList.tableSubtitle') }}</p>
          </div>
          <div class="table-head-actions">
            <span class="table-caption">{{ t('issueList.tableCount', { count: total }) }}</span>
            <el-button type="primary" plain @click="openCreateDialog">{{ t('issueList.createHere') }}</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list">
        <el-table-column :label="t('issueList.infoColumn')" min-width="320">
          <template #default="{ row }">
            <div class="issue-primary">
              <div class="issue-no">{{ row.issueNo }}<span v-if="row.oplNo"> / OPL {{ row.oplNo }}</span></div>
              <div class="issue-title">{{ row.title }}</div>
              <div class="issue-subline">
                <span>{{ t('common.label.owner') }} {{ row.ownerName || t('common.empty.unassigned') }}</span>
                <span>{{ t('common.label.reporter') }} {{ row.reporterName || '-' }}</span>
                <span>{{ t('common.label.issueFunction') }} {{ getIssueFunctionMeta(row.issueFunctionCode).label }}</span>
                <span>Pilot {{ row.pilotName || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.label.priority')" width="110">
          <template #default="{ row }">
            <el-tag :type="getIssuePriorityMeta(row.priority).tagType">
              {{ getIssuePriorityMeta(row.priority).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.label.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="getIssueStatusMeta(row.status).tagType">
              {{ getIssueStatusMeta(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.label.dueAt')" width="180">
          <template #default="{ row }">
            <div class="deadline-cell">
              <span>{{ row.dueAt || '-' }}</span>
              <span v-if="row.overdue" class="deadline-overdue">{{ t('issueList.overdueFlag') }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column :label="t('common.action.viewDetail')" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">{{ t('common.action.viewDetail') }}</el-button>
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

    <el-dialog v-model="createDialogVisible" :title="t('issueList.createDialogTitle')" width="920px" destroy-on-close>
      <div v-if="currentProject" class="create-project-bar">
        <span class="create-project-label">{{ t('common.label.currentProject') }}</span>
        <strong>{{ currentProject.projectName }}</strong>
        <span class="create-project-meta">{{ currentProject.projectNo }}</span>
      </div>

      <el-form :model="createForm" label-position="top" class="create-grid">
        <el-form-item :label="t('common.label.title')" class="full-span">
          <el-input v-model="createForm.title" maxlength="200" show-word-limit :placeholder="t('issueList.createTitlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('common.label.description')" class="full-span">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            :placeholder="t('issueList.createDescriptionPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('issueList.category')">
          <el-select v-model="createForm.categoryCode" :placeholder="t('issueList.selectCategory')">
            <el-option v-for="item in issueCategoryOptions" :key="item.id" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('issueList.source')">
          <el-select v-model="createForm.sourceCode" :placeholder="t('issueList.selectSource')">
            <el-option v-for="item in issueSourceOptions" :key="item.id" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.issueFunction')">
          <el-select v-model="createForm.issueFunctionCode" :placeholder="t('issueList.selectFunction')">
            <el-option v-for="item in issueFunctionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.priority')">
          <el-select v-model="createForm.priority" :placeholder="t('issueList.selectPriority')">
            <el-option v-for="item in issuePriorityOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.impact')">
          <el-select v-model="createForm.impactLevel" :placeholder="t('issueList.selectImpact')">
            <el-option v-for="item in issueImpactOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('issueList.device')">
          <el-input v-model="createForm.deviceName" :placeholder="t('issueList.optional')" />
        </el-form-item>
        <el-form-item :label="t('issueList.module')">
          <el-input v-model="createForm.moduleName" :placeholder="t('issueList.optional')" />
        </el-form-item>
        <el-form-item :label="t('common.label.owner')">
          <el-select v-model="createForm.ownerId" :placeholder="t('issueList.optional')" clearable filterable>
            <el-option
              v-for="item in memberOptions"
              :key="item.userId"
              :label="`${item.realName || item.username} / ${item.projectRoleName}`"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.label.occurredAt')">
          <el-date-picker
            v-model="createForm.occurredAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :placeholder="t('issueList.optional')"
          />
        </el-form-item>
        <el-form-item :label="t('common.label.dueAt')">
          <el-date-picker
            v-model="createForm.dueAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :placeholder="t('issueList.optional')"
          />
        </el-form-item>
        <el-form-item :label="t('issueList.siteFlags')" class="full-span">
          <div class="toggle-row">
            <el-checkbox v-model="createForm.affectShipment">{{ t('issueList.affectShipment') }}</el-checkbox>
            <el-checkbox v-model="createForm.affectCommissioning">{{ t('issueList.affectCommissioning') }}</el-checkbox>
            <el-checkbox v-model="createForm.needShutdown">{{ t('issueList.needShutdown') }}</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item :label="t('common.label.attachments')" class="full-span">
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
            <el-button plain>{{ t('common.action.uploadMedia') }}</el-button>
            <template #tip>
              <div class="upload-tip">{{ t('common.uploadTip') }}</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">{{ t('common.action.cancel') }}</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">{{ t('common.action.submit') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type UploadProps, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { fetchDictOptions, type DictItem as DictOptionItem } from '@/api/dict'
import { createIssue, fetchIssuePage, uploadIssueAttachment, type FileUploadResult, type IssueAttachmentPayload } from '@/api/issue'
import {
  exportProjectIssueReport,
  fetchProjectAll,
  fetchProjectIssueSummary,
  fetchProjectMembers,
  importProjectIssueReport,
  type ProjectIssueSummary,
  type ProjectIssueImportResult,
  type ProjectItem,
  type ProjectMemberItem
} from '@/api/project'
import type { IssueItem } from '@/types/api'
import {
  getIssueImpactMeta,
  getIssueFunctionMeta,
  getIssueFunctionOptions,
  getIssuePriorityMeta,
  getIssueStatusMeta,
  getProjectStatusMeta,
  getIssuePriorityOptions,
  getIssueStatusOptions
} from '@/utils/view-mappers'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const list = ref<IssueItem[]>([])
const total = ref(0)
const projectOptions = ref<ProjectItem[]>([])
const currentProject = ref<ProjectItem>()
const memberOptions = ref<ProjectMemberItem[]>([])
const createDialogVisible = ref(false)
const creating = ref(false)
const exporting = ref(false)
const importing = ref(false)
const attachmentFileList = ref<UploadUserFile[]>([])
const createAttachments = ref<IssueAttachmentPayload[]>([])
const issueImportInputRef = ref<HTMLInputElement | null>(null)
const issueCategoryOptions = ref<DictOptionItem[]>([])
const issueSourceOptions = ref<DictOptionItem[]>([])
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
  issueFunctionCode: 'PAT',
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
  issueFunctionCode: '',
  status: '',
  priority: '',
  overdueOnly: false
})

const issueStatusOptions = computed(() => getIssueStatusOptions())
const issuePriorityOptions = computed(() => getIssuePriorityOptions())
const issueFunctionOptions = computed(() => getIssueFunctionOptions())
const issueImpactOptions = computed(() => [
  { value: 'LOW', label: getIssueImpactMeta('LOW').label },
  { value: 'MEDIUM', label: getIssueImpactMeta('MEDIUM').label },
  { value: 'HIGH', label: getIssueImpactMeta('HIGH').label },
  { value: 'CRITICAL', label: getIssueImpactMeta('CRITICAL').label }
])

function applyDictDefault(field: 'categoryCode' | 'sourceCode', options: DictOptionItem[]) {
  if (!options.length) {
    return
  }
  if (options.some((item) => item.itemValue === createForm[field])) {
    return
  }
  const defaultItem = options.find((item) => item.isDefault) || options[0]
  createForm[field] = defaultItem.itemValue
}

const summaryCards = computed(() => [
  {
    label: t('dashboard.highPriorityIssues'),
    value: projectSummary.value.highPriorityIssues,
    note: t('issueList.summary.highPriority')
  },
  {
    label: t('issueList.summary.unassignedLabel'),
    value: projectSummary.value.unassignedIssues,
    note: t('issueList.summary.unassignedNote')
  },
  {
    label: t('issueList.summary.processingLabel'),
    value: projectSummary.value.processingIssues,
    note: t('issueList.summary.processingNote')
  },
  {
    label: t('stats.overdue'),
    value: projectSummary.value.overdueIssues,
    note: t('issueList.summary.overdueNote')
  }
])

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
    issueFunctionCode: query.issueFunctionCode || undefined,
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

async function loadDictOptions() {
  const [{ data: categoryData }, { data: sourceData }] = await Promise.all([
    fetchDictOptions('ISSUE_CATEGORY'),
    fetchDictOptions('ISSUE_SOURCE')
  ])
  issueCategoryOptions.value = categoryData
  issueSourceOptions.value = sourceData
  applyDictDefault('categoryCode', categoryData)
  applyDictDefault('sourceCode', sourceData)
}

async function loadOptions() {
  const [{ data }] = await Promise.all([fetchProjectAll(), loadDictOptions()])
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
    issueFunctionCode: '',
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
    categoryCode: '',
    sourceCode: '',
    issueFunctionCode: 'PAT',
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
  applyDictDefault('categoryCode', issueCategoryOptions.value)
  applyDictDefault('sourceCode', issueSourceOptions.value)
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
    ElMessage.warning(t('common.message.chooseProjectFirst'))
    return
  }

  exporting.value = true
  try {
    const response = await exportProjectIssueReport(query.projectId, {
      keyword: query.keyword || undefined,
      ownerId: query.ownerId,
      issueFunctionCode: query.issueFunctionCode || undefined,
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
    ElMessage.success(t('common.message.downloaded'))
  } finally {
    exporting.value = false
  }
}

function triggerImport() {
  issueImportInputRef.value?.click()
}

async function handleImportChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  target.value = ''
  if (!file || !currentProject.value) {
    return
  }
  if (!/\.(xlsx|xls)$/i.test(file.name)) {
    ElMessage.error('请上传 .xlsx 或 .xls 文件')
    return
  }

  importing.value = true
  try {
    const { data } = await importProjectIssueReport(currentProject.value.id, file)
    await Promise.all([loadData(), loadProjectSummary()])
    await showImportResult(data)
  } finally {
    importing.value = false
  }
}

async function showImportResult(result: ProjectIssueImportResult) {
  const lines = [
    `总行数：${result.totalCount}`,
    `新增：${result.addedCount}`,
    `覆盖：${result.updatedCount}`,
    `跳过：${result.skippedCount}`,
    `失败：${result.failedCount}`
  ]
  if (result.failedMessages?.length) {
    lines.push('', '失败明细：', ...result.failedMessages.slice(0, 10))
  }
  await ElMessageBox.alert(lines.join('<br/>'), '导入结果', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确定'
  })
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
    ElMessage.warning(t('common.message.uploadImageVideoOnly'))
    return false
  }
  if (!isValidSize) {
    ElMessage.warning(t('common.message.uploadSizeLimit'))
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
    ElMessage.warning(t('common.message.chooseProjectFirst'))
    return
  }
  if (!createForm.title.trim()) {
    ElMessage.warning(t('issueList.validation.titleRequired'))
    return
  }
  if (!createForm.issueFunctionCode) {
    ElMessage.warning(t('issueList.validation.functionRequired'))
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
      issueFunctionCode: createForm.issueFunctionCode,
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
    ElMessage.success(t('issueList.messages.created'))
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

.hidden-file-input {
  display: none;
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
