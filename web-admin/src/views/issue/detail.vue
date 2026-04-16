<template>
  <div v-if="detail" class="issue-detail-page">
    <section class="opl-page-head">
      <div>
        <p class="opl-section-title">Issue Detail</p>
        <h2 class="opl-page-title">{{ detail.issueNo }}</h2>
      </div>
      <div class="opl-page-head-actions">
        <el-button v-if="canDeleteIssue" type="danger" plain @click="handleDeleteIssue">误录删除</el-button>
        <el-button plain @click="goBackToProject">返回项目问题库</el-button>
      </div>
    </section>

    <section class="hero-grid">
      <el-card class="summary-card">
        <div class="summary-head">
          <div>
            <div class="issue-code">
              {{ detail.issueNo }}
              <span v-if="detail.oplNo"> / OPL {{ detail.oplNo }}</span>
            </div>
            <h2 class="summary-title">{{ detail.title }}</h2>
          </div>
          <div class="summary-tags">
            <span class="opl-chip" :class="getIssuePriorityMeta(detail.priority).tone">
              {{ getIssuePriorityMeta(detail.priority).label }}
            </span>
            <span class="opl-chip" :class="progressSummary.tone">
              {{ progressSummary.label }}
            </span>
          </div>
        </div>

        <p class="summary-desc">{{ detail.description || '暂无补充说明' }}</p>

        <div v-if="detail.attachments?.length" class="summary-media-gallery">
          <button
            v-for="item in detail.attachments"
            :key="item.id"
            type="button"
            class="summary-media-card"
            @click="openAttachmentPreview(item)"
          >
            <div class="summary-media-frame">
              <span class="summary-media-delete" @click.stop="handleDeleteAttachment(item)">删除</span>
              <img
                v-if="isImageAttachment(item)"
                :src="item.previewUrl || item.fileUrl"
                :alt="item.fileName"
                class="summary-media-thumb"
              />
              <div v-else-if="isVideoAttachment(item)" class="summary-video-thumb">
                <video
                  class="summary-media-thumb"
                  :src="item.previewUrl || item.fileUrl"
                  muted
                  playsinline
                  preload="metadata"
                />
                <span class="summary-video-badge">PLAY</span>
              </div>
              <div v-else class="summary-file-thumb">
                <span>{{ attachmentTypeLabel(item.fileType) }}</span>
              </div>
            </div>
            <div class="summary-media-meta">
              <div class="summary-media-name">{{ item.fileName }}</div>
              <div class="summary-media-extra">
                {{ attachmentTypeLabel(item.fileType) }} / {{ formatFileSize(item.fileSize) }}
              </div>
            </div>
          </button>
        </div>

        <div class="summary-append-panel">
          <div class="summary-append-head">
            <div>
              <div class="opl-kv-label">补充附件</div>
              <div class="summary-append-tip">问题创建后可继续补充图片和视频，保存后会出现在当前附件区。</div>
            </div>
          </div>
          <el-upload
            v-model:file-list="supplementFileList"
            class="summary-uploader"
            multiple
            :limit="6"
            :http-request="uploadSupplementAttachment"
            :before-upload="beforeAttachmentUpload"
            :on-success="handleSupplementAttachmentSuccess"
            :on-remove="handleSupplementAttachmentRemove"
          >
            <el-button plain>上传图片 / 视频</el-button>
            <template #tip>
              <div class="upload-tip">支持图片和视频，单文件不超过 5MB。</div>
            </template>
          </el-upload>
          <el-input
            v-model="attachmentAppendForm.remark"
            type="textarea"
            :rows="2"
            placeholder="可选，补充本次附件说明"
          />
          <div class="summary-append-actions">
            <el-button
              type="primary"
              :disabled="!supplementAttachments.length"
              :loading="appendingAttachments"
              @click="submitSupplementAttachments"
            >
              保存补充附件
            </el-button>
          </div>
        </div>

        <div class="summary-grid">
          <div v-for="item in summaryFactsView" :key="item.label" class="opl-kv">
            <span class="opl-kv-label">{{ item.label }}</span>
            <span class="opl-kv-value">{{ item.value }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="progress-card">
        <div class="progress-header">
          <h3 class="opl-card-title">当前推进</h3>
          <span class="opl-chip" :class="progressSummary.tone">{{ progressSummary.label }}</span>
        </div>

        <div class="progress-grid">
          <div class="progress-item">
            <span class="opl-kv-label">当前阶段</span>
            <strong>{{ getIssueStatusMeta(detail.status).label }}</strong>
          </div>
          <div class="progress-item">
            <span class="opl-kv-label">责任人</span>
            <strong>{{ detail.ownerName || '待分派' }}</strong>
          </div>
          <div class="progress-item">
            <span class="opl-kv-label">下一步</span>
            <strong>{{ nextActionHint }}</strong>
          </div>
          <div class="progress-item">
            <span class="opl-kv-label">截止时间</span>
            <strong>{{ detail.dueAt || '-' }}</strong>
          </div>
        </div>

        <div class="latest-card">
          <div class="latest-head">
            <span class="opl-kv-label">最新动作</span>
            <span class="latest-time">{{ latestActionTime }}</span>
          </div>
          <div class="latest-content">{{ latestActionContent }}</div>
          <div class="latest-meta">{{ latestActionMeta }}</div>
        </div>

        <div v-if="showCloseInfo" class="close-card">
          <div class="close-card-head">关闭信息</div>
          <div class="close-grid">
            <div class="opl-kv">
              <span class="opl-kv-label">关闭人</span>
              <span class="opl-kv-value">{{ detail.closedByName || '-' }}</span>
            </div>
            <div class="opl-kv">
              <span class="opl-kv-label">关闭时间</span>
              <span class="opl-kv-value">{{ detail.closedAt || '-' }}</span>
            </div>
            <div class="opl-kv">
              <span class="opl-kv-label">关闭说明</span>
              <span class="opl-kv-value">{{ detail.closeReason || '-' }}</span>
            </div>
            <div class="opl-kv">
              <span class="opl-kv-label">关闭证据</span>
              <span class="opl-kv-value">{{ detail.closeEvidence || '-' }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </section>

    <section class="detail-grid">
      <div class="detail-stack">
        <el-card>
          <template #header>
            <h3 class="opl-card-title">管理操作</h3>
          </template>

          <div class="action-grid">
            <section class="action-panel">
              <div class="panel-head">
                <span class="panel-title">责任分派</span>
              </div>
              <el-form :model="assignForm" label-position="top">
                <el-form-item label="责任人">
                  <el-select v-model="assignForm.ownerId" placeholder="选择责任人" filterable>
                    <el-option
                      v-for="item in memberOptions"
                      :key="item.userId"
                      :label="`${item.realName || item.username} / ${item.projectRoleName}`"
                      :value="item.userId"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="assignForm.remark" type="textarea" :rows="3" placeholder="补充分派原因" />
                </el-form-item>
                <el-button type="primary" @click="submitAssign">保存责任人</el-button>
              </el-form>
            </section>

            <section class="action-panel">
              <div class="panel-head">
                <span class="panel-title">{{ t('issueDetail.functionPanel') }}</span>
              </div>
              <el-form :model="functionForm" label-position="top">
                <el-form-item :label="t('common.label.issueFunction')">
                  <el-select v-model="functionForm.issueFunctionCode" :placeholder="t('issueList.selectFunction')">
                    <el-option v-for="item in issueFunctionOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="t('issueDetail.functionRemark')">
                  <el-input
                    v-model="functionForm.remark"
                    type="textarea"
                    :rows="3"
                    :placeholder="t('issueDetail.functionRemarkPlaceholder')"
                  />
                </el-form-item>
                <el-button type="primary" @click="submitFunction">{{ t('issueDetail.updateFunction') }}</el-button>
              </el-form>
            </section>

            <section class="action-panel">
              <div class="panel-head">
                <span class="panel-title">优先级</span>
              </div>
              <el-form :model="priorityForm" label-position="top">
                <el-form-item label="优先级">
                  <el-select v-model="priorityForm.priority" placeholder="选择优先级">
                    <el-option v-for="item in issuePriorityOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="priorityForm.remark" type="textarea" :rows="3" placeholder="补充调整原因" />
                </el-form-item>
                <el-button type="primary" @click="submitPriority">更新优先级</el-button>
              </el-form>
            </section>
          </div>
        </el-card>

        <el-card>
          <template #header>
            <h3 class="opl-card-title">下一步操作</h3>
          </template>

          <div class="progress-panel">
            <div class="panel-head">
              <span class="panel-title">推进 / 关闭</span>
              <span class="progress-stage">
                {{ getIssueStatusMeta(detail.status).label }}
                <template v-if="selectedTransitionLabel"> → {{ selectedTransitionLabel }}</template>
              </span>
            </div>

            <div v-if="progressActions.length" class="transition-row">
              <el-button
                v-for="item in progressActions"
                :key="item.value"
                :type="statusForm.targetStatus === item.value ? 'primary' : 'default'"
                @click="pickTransition(item.value)"
              >
                {{ item.label }}
              </el-button>
            </div>
            <div v-else class="opl-empty compact-empty">当前状态无需继续推进</div>

            <el-form :model="statusForm" label-position="top" class="status-form">
              <el-form-item v-if="needsSolutionDesc" label="处理说明">
                <el-input v-model="statusForm.solutionDesc" type="textarea" :rows="3" placeholder="说明已完成的处理动作" />
              </el-form-item>
              <el-form-item v-if="needsHoldReason" label="挂起原因">
                <el-input v-model="statusForm.holdReason" type="textarea" :rows="3" placeholder="说明挂起原因" />
              </el-form-item>
              <el-form-item v-if="needsCloseFields" label="关闭说明">
                <el-input v-model="statusForm.closeReason" type="textarea" :rows="3" placeholder="可选，补充关闭说明" />
              </el-form-item>
              <el-form-item v-if="needsCloseFields" label="关闭证据">
                <el-input v-model="statusForm.closeEvidence" type="textarea" :rows="3" placeholder="可选，补充关闭证据或链接" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="statusForm.remark" type="textarea" :rows="3" placeholder="补充本次推进说明" />
              </el-form-item>
              <el-button type="primary" :disabled="!statusForm.targetStatus" @click="submitStatus">
                {{ statusForm.targetStatus === 'CLOSED' ? '关闭问题' : '提交进度' }}
              </el-button>
            </el-form>
          </div>
        </el-card>
      </div>

      <div class="detail-stack">
        <el-card>
          <template #header>
            <h3 class="opl-card-title">时间线</h3>
          </template>

          <el-form :model="commentForm" class="comment-form">
            <el-form-item>
              <el-input v-model="commentForm.content" type="textarea" :rows="4" placeholder="补充现场进展、处理结果或风险" />
            </el-form-item>
            <div class="comment-actions">
              <el-button type="primary" @click="submitComment">提交跟进</el-button>
            </div>
          </el-form>

          <el-timeline v-if="detail.comments?.length">
            <el-timeline-item
              v-for="item in detail.comments"
              :key="item.id"
              :timestamp="item.createdAt"
              :type="item.commentType === 'SYSTEM' ? 'info' : 'primary'"
            >
              <div class="timeline-card">
                <div class="timeline-head">
                  <strong>{{ item.operatorName }}</strong>
                  <span class="opl-chip" :class="item.commentType === 'SYSTEM' ? 'slate' : 'blue'">
                    {{ item.commentType === 'SYSTEM' ? '系统记录' : '人工跟进' }}
                  </span>
                </div>
                <div class="timeline-content">{{ item.content }}</div>
                <div v-if="item.attachments?.length" class="timeline-attachments">
                  <a
                    v-for="attachment in item.attachments"
                    :key="attachment.id"
                    :href="attachment.previewUrl || attachment.fileUrl"
                    target="_blank"
                    rel="noreferrer"
                  >
                    {{ attachment.fileName }}
                  </a>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <div v-else class="opl-empty compact-empty">还没有时间线记录</div>
        </el-card>
      </div>
    </section>

    <el-dialog
      v-model="previewVisible"
      class="attachment-preview-dialog"
      width="880px"
      top="8vh"
      destroy-on-close
      @closed="resetPreviewZoom"
      align-center
    >
      <template #header>
        <div class="preview-dialog-head">
          <div class="preview-dialog-title">{{ previewAttachment?.fileName || '附件预览' }}</div>
          <div class="preview-dialog-meta">
            {{ previewAttachment ? `${attachmentTypeLabel(previewAttachment.fileType)} / ${formatFileSize(previewAttachment.fileSize)}` : '' }}
          </div>
        </div>
        <div class="preview-dialog-actions">
          <el-button size="small" circle @click="zoomOutPreview">-</el-button>
          <span class="preview-zoom-indicator">{{ previewScalePercent }}%</span>
          <el-button size="small" circle @click="zoomInPreview">+</el-button>
          <el-button size="small" plain @click="resetPreviewZoom">重置</el-button>
        </div>
      </template>

      <div
        v-if="previewAttachment"
        class="preview-dialog-body"
        :class="{ dragging: previewDragging }"
        @wheel.prevent="handlePreviewWheel"
        @mousemove="handlePreviewMouseMove"
        @mouseup="stopPreviewDrag"
        @mouseleave="stopPreviewDrag"
      >
        <div
          v-if="isImageAttachment(previewAttachment) || isVideoAttachment(previewAttachment)"
          class="preview-media-stage"
          @mousedown.left.prevent="handlePreviewMouseDown"
          @dblclick.prevent="togglePreviewZoom"
        >
        <img
          v-if="isImageAttachment(previewAttachment)"
          :src="previewAttachment.previewUrl || previewAttachment.fileUrl"
          :alt="previewAttachment.fileName"
          class="preview-image"
          :style="previewMediaStyle"
        />
        <video
          v-else-if="isVideoAttachment(previewAttachment)"
          :src="previewAttachment.previewUrl || previewAttachment.fileUrl"
          class="preview-video"
          :style="previewMediaStyle"
          controls
          playsinline
          autoplay
        ></video>
        </div>
        <div v-else class="preview-file-fallback">
          <el-link :href="previewAttachment.previewUrl || previewAttachment.fileUrl" target="_blank" type="primary">打开附件</el-link>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, type UploadProps, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import {
  appendIssueAttachments,
  assignIssue,
  changeIssueStatus,
  createIssueComment,
  deleteIssueAttachment,
  deleteIssue,
  fetchAttachmentBlob,
  fetchIssueDetail,
  uploadIssueAttachment,
  type FileUploadResult,
  type IssueAttachmentPayload,
  updateIssueFunction,
  updateIssuePriority,
  type IssueAttachmentItem,
  type IssueDetail
} from '@/api/issue'
import { fetchProjectMembers, type ProjectMemberItem } from '@/api/project'
import { getIssueFunctionMeta, getIssueFunctionOptions, getIssuePriorityMeta, getIssueStatusMeta, getIssuePriorityOptions } from '@/utils/view-mappers'

type ProgressAction = {
  value: string
  label: string
}

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const detail = ref<IssueDetail>()
const memberOptions = ref<ProjectMemberItem[]>([])
const previewVisible = ref(false)
const previewAttachment = ref<IssueAttachmentItem>()
const previewScale = ref(1)
const previewDragging = ref(false)
const previewOffset = reactive({
  x: 0,
  y: 0
})
const previewDragStart = reactive({
  x: 0,
  y: 0,
  originX: 0,
  originY: 0
})
const supplementFileList = ref<UploadUserFile[]>([])
const supplementAttachments = ref<IssueAttachmentPayload[]>([])
const appendingAttachments = ref(false)
const issuePriorityOptions = computed(() => getIssuePriorityOptions())
const issueFunctionOptions = computed(() => getIssueFunctionOptions())

const assignForm = reactive({
  ownerId: undefined as number | undefined,
  remark: ''
})

const functionForm = reactive({
  issueFunctionCode: '',
  remark: ''
})

const priorityForm = reactive({
  priority: '',
  remark: ''
})

const statusForm = reactive({
  targetStatus: '',
  solutionDesc: '',
  holdReason: '',
  closeReason: '',
  closeEvidence: '',
  remark: ''
})

const commentForm = reactive({
  content: ''
})

const attachmentAppendForm = reactive({
  remark: ''
})

const progressSummary = computed(() => {
  if (!detail.value) {
    return { label: '-', tone: 'gray' }
  }
  if (detail.value.status === 'CLOSED') {
    return { label: '已关闭', tone: 'teal' }
  }
  if (detail.value.status === 'CANCELED') {
    return { label: '已取消', tone: 'gray' }
  }
  if (detail.value.status === 'ON_HOLD') {
    return { label: '已挂起', tone: 'rose' }
  }
  if (detail.value.overdue) {
    return { label: '已超期', tone: 'rose' }
  }
  return { label: '进行中', tone: 'amber' }
})

const summaryFacts = computed(() => {
  if (!detail.value) {
    return []
  }
  return [
    { label: '项目', value: detail.value.projectName || '-' },
    { label: '责任人', value: detail.value.ownerName || '待分派' },
    { label: '提交人', value: detail.value.reporterName || '-' },
    { label: '录入时间', value: detail.value.createdAt || detail.value.occurredAt || '-' },
    { label: '截止时间', value: detail.value.dueAt || '-' }
  ]
})

const showCloseInfo = computed(() => {
  return Boolean(detail.value?.closedAt || detail.value?.closedByName || detail.value?.closeReason || detail.value?.closeEvidence)
})

const summaryFactsView = computed(() => {
  if (!detail.value) {
    return []
  }
  const baseFacts = summaryFacts.value.slice()
  const issueFunctionFact = {
    label: t('common.label.issueFunction'),
    value: getIssueFunctionMeta(detail.value.issueFunctionCode).label
  }
  return [baseFacts[0], baseFacts[1], issueFunctionFact, ...baseFacts.slice(2)].filter(Boolean)
})

const latestTimelineItem = computed(() => detail.value?.comments?.[0])

const latestActionContent = computed(() => {
  if (latestTimelineItem.value?.content) {
    return latestTimelineItem.value.content
  }
  if (detail.value?.actionPlan) {
    return detail.value.actionPlan
  }
  if (detail.value?.closeReason) {
    return detail.value.closeReason
  }
  return '暂无动作记录'
})

const latestActionMeta = computed(() => {
  if (latestTimelineItem.value) {
    return `${latestTimelineItem.value.operatorName} / ${latestTimelineItem.value.commentType === 'SYSTEM' ? '系统记录' : '人工跟进'}`
  }
  if (detail.value?.closedByName) {
    return `关闭人 / ${detail.value.closedByName}`
  }
  return '导入记录'
})

const latestActionTime = computed(() => {
  if (latestTimelineItem.value?.createdAt) {
    return latestTimelineItem.value.createdAt
  }
  return detail.value?.updatedAt || detail.value?.createdAt || '-'
})

const nextActionHint = computed(() => {
  if (!detail.value) {
    return '-'
  }
  switch (detail.value.status) {
    case 'NEW':
      return detail.value.ownerName ? '开始处理' : '先分派责任人'
    case 'ACCEPTED':
      return '提交验证'
    case 'IN_PROGRESS':
      return '提交验证'
    case 'PENDING_VERIFY':
      return '关闭问题或退回处理'
    case 'ON_HOLD':
      return '恢复处理'
    case 'CLOSED':
      return '已关闭'
    case 'CANCELED':
      return '已取消'
    default:
      return '-'
  }
})

const progressActions = computed<ProgressAction[]>(() => {
  if (!detail.value) {
    return []
  }
  const hasOwner = Boolean(detail.value.ownerId || detail.value.ownerName)
  switch (detail.value.status) {
    case 'NEW':
      if (!hasOwner) {
        return []
      }
      return [
        { value: 'IN_PROGRESS', label: '开始处理' },
        { value: 'ON_HOLD', label: '挂起' },
        { value: 'CANCELED', label: '取消' }
      ]
    case 'ACCEPTED':
      return [
        { value: 'PENDING_VERIFY', label: '提交验证' },
        { value: 'ON_HOLD', label: '挂起' },
        { value: 'CANCELED', label: '取消' }
      ]
    case 'IN_PROGRESS':
      return [
        { value: 'PENDING_VERIFY', label: '提交验证' },
        { value: 'ON_HOLD', label: '挂起' }
      ]
    case 'PENDING_VERIFY':
      return [
        { value: 'CLOSED', label: '关闭问题' },
        { value: 'IN_PROGRESS', label: '退回处理' }
      ]
    case 'ON_HOLD':
      return [
        { value: 'IN_PROGRESS', label: '恢复处理' },
        { value: 'CANCELED', label: '取消' }
      ]
    default:
      return []
  }
})

const selectedTransitionLabel = computed(() => {
  return progressActions.value.find((item) => item.value === statusForm.targetStatus)?.label || ''
})

const needsSolutionDesc = computed(() => statusForm.targetStatus === 'PENDING_VERIFY')
const needsHoldReason = computed(() => statusForm.targetStatus === 'ON_HOLD')
const needsCloseFields = computed(() => statusForm.targetStatus === 'CLOSED')
const canDeleteIssue = computed(() => Boolean(detail.value?.id))
const previewScalePercent = computed(() => Math.round(previewScale.value * 100))
const previewMediaStyle = computed(() => ({
  transform: `translate(${previewOffset.x}px, ${previewOffset.y}px) scale(${previewScale.value})`
}))

const currentProjectId = computed(() => {
  const queryProjectId = Number(route.query.projectId)
  if (queryProjectId) {
    return queryProjectId
  }
  return detail.value?.projectId
})

function revokeAttachmentUrls(attachments?: IssueAttachmentItem[]) {
  attachments?.forEach((item) => {
    if (item.previewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(item.previewUrl)
    }
  })
}

async function hydrateAttachments(attachments: IssueAttachmentItem[]) {
  const hydrated = await Promise.all(
    attachments.map(async (item) => {
      try {
        const blob = await fetchAttachmentBlob(item.id)
        return {
          ...item,
          previewUrl: URL.createObjectURL(blob)
        }
      } catch (error) {
        return item
      }
    })
  )
  return hydrated
}

function resetStatusForm() {
  Object.assign(statusForm, {
    targetStatus: progressActions.value[0]?.value || '',
    solutionDesc: '',
    holdReason: '',
    closeReason: '',
    closeEvidence: '',
    remark: ''
  })
}

function isImageAttachment(item: IssueAttachmentItem) {
  return item.fileType === 'IMAGE'
}

function isVideoAttachment(item: IssueAttachmentItem) {
  return item.fileType === 'VIDEO'
}

function openAttachmentPreview(item: IssueAttachmentItem) {
  previewAttachment.value = item
  resetPreviewZoom()
  previewVisible.value = true
}

function clampPreviewScale(nextScale: number) {
  return Math.min(4, Math.max(0.5, Number(nextScale.toFixed(2))))
}

function zoomPreview(step: number) {
  previewScale.value = clampPreviewScale(previewScale.value + step)
  if (previewScale.value <= 1) {
    previewOffset.x = 0
    previewOffset.y = 0
  }
}

function zoomInPreview() {
  zoomPreview(0.2)
}

function zoomOutPreview() {
  zoomPreview(-0.2)
}

function resetPreviewZoom() {
  previewScale.value = 1
  previewOffset.x = 0
  previewOffset.y = 0
  previewDragging.value = false
}

function handlePreviewWheel(event: WheelEvent) {
  if (!previewAttachment.value) {
    return
  }
  zoomPreview(event.deltaY < 0 ? 0.1 : -0.1)
}

function handlePreviewMouseDown(event: MouseEvent) {
  if (previewScale.value <= 1) {
    return
  }
  previewDragging.value = true
  previewDragStart.x = event.clientX
  previewDragStart.y = event.clientY
  previewDragStart.originX = previewOffset.x
  previewDragStart.originY = previewOffset.y
}

function handlePreviewMouseMove(event: MouseEvent) {
  if (!previewDragging.value) {
    return
  }
  previewOffset.x = previewDragStart.originX + (event.clientX - previewDragStart.x)
  previewOffset.y = previewDragStart.originY + (event.clientY - previewDragStart.y)
}

function stopPreviewDrag() {
  previewDragging.value = false
}

function togglePreviewZoom() {
  if (previewScale.value > 1.05) {
    resetPreviewZoom()
    return
  }
  previewScale.value = 2
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

async function uploadSupplementAttachment(options: UploadRequestOptions) {
  try {
    const result = await uploadIssueAttachment(options.file as File)
    options.onSuccess(result.data as FileUploadResult)
  } catch (error) {
    options.onError(error as any)
  }
}

const handleSupplementAttachmentSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  const uploaded = response as FileUploadResult
  supplementAttachments.value.push({
    fileName: uploaded.fileName,
    objectKey: uploaded.objectKey,
    fileUrl: uploaded.fileUrl,
    fileSize: uploaded.fileSize,
    contentType: uploaded.contentType
  })
  uploadFile.url = uploaded.fileUrl
}

const handleSupplementAttachmentRemove: UploadProps['onRemove'] = (uploadFile) => {
  const uploaded = uploadFile.response as FileUploadResult | undefined
  const objectKey = uploaded?.objectKey
  if (!objectKey) {
    return
  }
  supplementAttachments.value = supplementAttachments.value.filter((item) => item.objectKey !== objectKey)
}

async function submitSupplementAttachments() {
  if (!supplementAttachments.value.length) {
    ElMessage.warning('请先上传图片或视频')
    return
  }
  appendingAttachments.value = true
  try {
    await appendIssueAttachments(Number(route.params.id), {
      attachments: supplementAttachments.value,
      remark: attachmentAppendForm.remark.trim() || undefined
    })
    ElMessage.success('附件已补充')
    supplementFileList.value = []
    supplementAttachments.value = []
    attachmentAppendForm.remark = ''
    await loadData()
  } finally {
    appendingAttachments.value = false
  }
}

async function handleDeleteAttachment(item: IssueAttachmentItem) {
  await ElMessageBox.confirm(
    `删除后将从问题主附件中移除“${item.fileName}”，是否继续？`,
    '删除附件',
    {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    }
  )

  await deleteIssueAttachment(Number(route.params.id), item.id)
  if (previewAttachment.value?.id === item.id) {
    previewVisible.value = false
    previewAttachment.value = undefined
  }
  ElMessage.success('附件已删除')
  await loadData()
}

async function loadData() {
  revokeAttachmentUrls(detail.value?.attachments)
  const { data } = await fetchIssueDetail(Number(route.params.id))
  const attachments = await hydrateAttachments(data.attachments || [])
  detail.value = {
    ...data,
    attachments
  }
  assignForm.ownerId = data.ownerId
  functionForm.issueFunctionCode = data.issueFunctionCode || ''
  functionForm.remark = ''
  priorityForm.priority = data.priority
  resetStatusForm()
}

async function loadUsers() {
  if (!currentProjectId.value) {
    memberOptions.value = []
    return
  }
  const { data } = await fetchProjectMembers(Number(currentProjectId.value))
  memberOptions.value = data
}

function pickTransition(value: string) {
  statusForm.targetStatus = value
}

async function submitAssign() {
  if (!assignForm.ownerId) {
    ElMessage.warning('请选择责任人')
    return
  }
  await assignIssue(Number(route.params.id), {
    ownerId: Number(assignForm.ownerId),
    remark: assignForm.remark
  })
  ElMessage.success('责任人已更新')
  assignForm.remark = ''
  await loadData()
}

async function submitPriority() {
  if (!priorityForm.priority) {
    ElMessage.warning('请选择优先级')
    return
  }
  await updateIssuePriority(Number(route.params.id), priorityForm)
  ElMessage.success('优先级已更新')
  priorityForm.remark = ''
  await loadData()
}

async function submitFunction() {
  if (!functionForm.issueFunctionCode) {
    ElMessage.warning(t('issueList.validation.functionRequired'))
    return
  }
  await updateIssueFunction(Number(route.params.id), {
    issueFunctionCode: functionForm.issueFunctionCode,
    remark: functionForm.remark.trim() || undefined
  })
  ElMessage.success(t('issueDetail.updateFunction'))
  functionForm.remark = ''
  await loadData()
}

async function submitStatus() {
  if (!statusForm.targetStatus) {
    ElMessage.warning('请选择下一步操作')
    return
  }
  await changeIssueStatus(Number(route.params.id), statusForm)
  ElMessage.success(statusForm.targetStatus === 'CLOSED' ? '问题已关闭' : '进度已更新')
  await loadData()
}

async function submitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入跟进内容')
    return
  }
  await createIssueComment(Number(route.params.id), commentForm)
  ElMessage.success('跟进已提交')
  commentForm.content = ''
  await loadData()
}

async function handleDeleteIssue() {
  await ElMessageBox.confirm(
    '删除后该问题会从列表和详情中消失，附件和评论也会一起隐藏。仅适用于误录场景，是否继续？',
    '误录删除',
    {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    }
  )

  await deleteIssue(Number(route.params.id))
  ElMessage.success('问题已删除')
  goBackToProject()
}

function formatFileSize(size: number) {
  if (!size) {
    return '0 B'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function attachmentTypeLabel(fileType?: string) {
  if (fileType === 'VIDEO') {
    return '视频'
  }
  if (fileType === 'IMAGE') {
    return '图片'
  }
  return '文件'
}

function goBackToProject() {
  if (route.query.source === 'my-issues') {
    router.push('/my-issues')
    return
  }
  if (currentProjectId.value) {
    router.push({
      path: '/issues',
      query: {
        projectId: String(currentProjectId.value)
      }
    })
    return
  }
  router.push('/issue-projects')
}

onMounted(async () => {
  await loadData()
  await loadUsers()
})

onBeforeUnmount(() => {
  revokeAttachmentUrls(detail.value?.attachments)
})
</script>

<style scoped>
.issue-detail-page {
  display: grid;
  gap: 16px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 0.9fr);
  gap: 16px;
  align-items: stretch;
}

.summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.issue-code {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--opl-subtle);
}

.summary-title {
  margin: 12px 0 0;
  font-size: 34px;
  line-height: 1.08;
  color: var(--opl-text);
}

.summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.summary-desc {
  margin: 18px 0 0;
  color: var(--opl-muted);
  line-height: 1.8;
}

.summary-media-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.summary-append-panel {
  display: grid;
  gap: 14px;
  margin-top: 18px;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(24, 33, 43, 0.06);
}

.summary-append-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.summary-append-tip,
.upload-tip {
  margin-top: 6px;
  color: var(--opl-muted);
  font-size: 13px;
  line-height: 1.7;
}

.summary-uploader {
  width: 100%;
}

.summary-append-actions {
  display: flex;
  justify-content: flex-end;
}

.summary-media-card {
  display: grid;
  gap: 10px;
  padding: 0;
  border: 0;
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.summary-media-frame {
  position: relative;
  overflow: hidden;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(24, 33, 43, 0.08);
  aspect-ratio: 1.35 / 1;
}

.summary-media-delete {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 54px;
  height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(168, 32, 32, 0.88);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  box-shadow: 0 12px 24px rgba(168, 32, 32, 0.22);
}

.summary-media-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.summary-video-thumb {
  position: relative;
  width: 100%;
  height: 100%;
}

.summary-video-badge {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 68px;
  height: 34px;
  border-radius: 999px;
  background: rgba(23, 33, 43, 0.72);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.summary-file-thumb {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--opl-muted);
  font-weight: 700;
  background: rgba(255, 255, 255, 0.8);
}

.summary-media-meta {
  padding: 0 6px;
}

.summary-media-name {
  font-weight: 700;
  line-height: 1.5;
  color: var(--opl-text);
  word-break: break-all;
}

.summary-media-extra {
  margin-top: 4px;
  color: var(--opl-muted);
  font-size: 13px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18px 16px;
  margin-top: 24px;
}

.progress-card {
  display: flex;
  flex-direction: column;
}

.progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.progress-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 20px;
}

.progress-item,
.close-card {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(24, 33, 43, 0.08);
}

.progress-item strong {
  display: block;
  margin-top: 10px;
  font-size: 18px;
  line-height: 1.5;
  color: var(--opl-text);
}

.latest-card {
  margin-top: 20px;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(24, 33, 43, 0.08);
}

.latest-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.latest-time {
  font-size: 12px;
  color: var(--opl-subtle);
}

.latest-content {
  margin-top: 10px;
  line-height: 1.8;
  color: var(--opl-text);
  white-space: pre-wrap;
}

.latest-meta {
  margin-top: 10px;
  font-size: 13px;
  color: var(--opl-muted);
}

.close-card {
  margin-top: 20px;
}

.close-card-head {
  font-weight: 700;
  color: var(--opl-text);
}

.close-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
  margin-top: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(380px, 0.9fr);
  gap: 16px;
  align-items: start;
}

.detail-stack {
  display: grid;
  gap: 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.action-panel,
.progress-panel {
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(24, 33, 43, 0.06);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--opl-text);
}

.progress-stage {
  font-size: 13px;
  color: var(--opl-muted);
}

.transition-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.status-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.comment-form {
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(24, 33, 43, 0.06);
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.timeline-card {
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(24, 33, 43, 0.06);
}

.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.timeline-content {
  margin-top: 10px;
  line-height: 1.8;
  color: var(--opl-muted);
  white-space: pre-wrap;
}

.timeline-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.timeline-attachments a {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  color: #164a84;
  background: var(--opl-blue-soft);
}

.preview-dialog-head {
  display: grid;
  gap: 6px;
}

.preview-dialog-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
}

.preview-dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--opl-text);
}

.preview-dialog-meta {
  color: var(--opl-muted);
  font-size: 13px;
}

.preview-zoom-indicator {
  min-width: 54px;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--opl-text);
}

.preview-dialog-body {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 440px;
  border-radius: 24px;
  background: rgba(17, 25, 32, 0.9);
  overflow: hidden;
}

.preview-dialog-body.dragging {
  cursor: grabbing;
}

.preview-media-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 440px;
  cursor: grab;
  user-select: none;
}

.preview-image,
.preview-video {
  width: 100%;
  max-height: 72vh;
  object-fit: contain;
  display: block;
  transform-origin: center center;
  transition: transform 0.12s ease-out;
  will-change: transform;
}

.preview-file-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 220px;
}

.compact-empty {
  padding: 12px 0 0;
  text-align: left;
}

@media (max-width: 1320px) {
  .hero-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1080px) {
  .summary-grid,
  .action-grid,
  .progress-grid,
  .close-grid {
    grid-template-columns: 1fr;
  }

  .summary-head,
  .progress-header,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-tags {
    justify-content: flex-start;
  }
}
</style>
