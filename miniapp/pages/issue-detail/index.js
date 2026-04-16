const { changeIssueStatus, createIssueComment, getIssueDetail } = require('../../api/issue')
const { uploadFile } = require('../../utils/upload')
const { prepareImages, prepareVideo, MAX_MEDIA_SIZE, MAX_VIDEO_DURATION } = require('../../utils/media')
const { downloadAttachmentToTemp } = require('../../utils/attachment')
const {
  formatFileSize,
  getAttachmentType,
  getIssueFunctionMeta,
  getPriorityMeta,
  getStatusMeta,
  getNextActionHint,
  formatDateTime,
  getProgressMeta
} = require('../../utils/presenter')
const { getActiveProject, setActiveProject } = require('../../utils/project-context')

const CLOSE_EVIDENCE_PREFIX = '[关闭证据]'

function getActionConfig(status, hasOwner) {
  if (status === 'NEW') {
    if (!hasOwner) {
      return null
    }
    return {
      text: '开始处理',
      targetStatus: 'IN_PROGRESS',
      remark: '现场开始处理'
    }
  }

  if (status === 'ACCEPTED') {
    return {
      text: '提交验证',
      targetStatus: 'PENDING_VERIFY',
      remark: '现场处理完成，提交验证',
      solutionDesc: '现场已完成处理，请验证结果'
    }
  }

  if (status === 'IN_PROGRESS') {
    return {
      text: '提交验证',
      targetStatus: 'PENDING_VERIFY',
      remark: '现场处理完成，提交验证',
      solutionDesc: '现场已完成处理，请验证结果'
    }
  }

  if (status === 'PENDING_VERIFY') {
    return {
      text: '关闭问题',
      targetStatus: 'CLOSED',
      remark: '现场确认关闭'
    }
  }

  if (status === 'ON_HOLD') {
    return {
      text: '恢复处理',
      targetStatus: 'IN_PROGRESS',
      remark: '现场恢复处理'
    }
  }

  return null
}

function resolveLatestAction(detail) {
  const latestComment = detail.comments && detail.comments.length ? detail.comments[0] : null
  if (latestComment) {
    return {
      content: latestComment.content,
      time: formatDateTime(latestComment.createdAt),
      meta: latestComment.commentType === 'SYSTEM' ? '系统记录' : latestComment.operatorName
    }
  }

  if (detail.actionPlan) {
    return {
      content: detail.actionPlan,
      time: formatDateTime(detail.updatedAt || detail.createdAt),
      meta: '导入动作'
    }
  }

  if (detail.legacyComment) {
    return {
      content: detail.legacyComment,
      time: formatDateTime(detail.updatedAt || detail.createdAt),
      meta: '导入备注'
    }
  }

  return {
    content: '暂无动作记录',
    time: '-',
    meta: '-'
  }
}

function getReadableError(error, fallback) {
  if (!error) {
    return fallback
  }
  if (error.message) {
    return error.message
  }
  if (error.errMsg) {
    return error.errMsg
  }
  return fallback
}

function normalizeAttachment(item) {
  return {
    ...item,
    viewType: getAttachmentType(item.fileType),
    viewSize: formatFileSize(item.fileSize),
    previewUrl: item.localPath || '',
    previewReady: Boolean(item.localPath),
    previewError: false
  }
}

function isCloseEvidenceComment(item) {
  return (
    item &&
    item.commentType !== 'SYSTEM' &&
    typeof item.content === 'string' &&
    item.content.indexOf(CLOSE_EVIDENCE_PREFIX) === 0 &&
    item.attachments &&
    item.attachments.length
  )
}

function resolveCloseEvidenceAttachments(comments) {
  const dedicated = (comments || []).find((item) => isCloseEvidenceComment(item))
  if (dedicated) {
    return dedicated.attachments
  }

  const fallback = (comments || []).find(
    (item) => item.commentType !== 'SYSTEM' && item.attachments && item.attachments.length
  )
  return fallback ? fallback.attachments : []
}

function getDraftListKey(scope) {
  return scope === 'close' ? 'closeAttachments' : 'commentAttachments'
}

function buildUploadFolder(scope, mediaType) {
  if (scope === 'close') {
    return mediaType === 'VIDEO' ? 'close-video' : 'close-image'
  }
  return mediaType === 'VIDEO' ? 'comment-video' : 'comment-image'
}

Page({
  data: {
    id: null,
    detail: null,
    actionConfig: null,
    nextActionText: '',
    latestAction: null,
    commentContent: '',
    commentAttachments: [],
    closeReason: '',
    closeEvidence: '',
    closeAttachments: [],
    currentProject: null,
    uploadLoading: false,
    previewVisible: false,
    previewType: 'IMAGE',
    previewUrl: '',
    previewTitle: '',
    mediaRuleLabel: `单文件 <= ${Math.floor(MAX_MEDIA_SIZE / 1024 / 1024)}MB / 视频 <= ${MAX_VIDEO_DURATION}s`
  },

  onLoad(options) {
    const currentProject = options.projectId
      ? {
          id: Number(options.projectId),
          projectName: decodeURIComponent(options.projectName || '')
        }
      : getActiveProject()

    if (currentProject) {
      setActiveProject(currentProject)
    }

    this.setData({
      id: Number(options.id),
      currentProject
    })
  },

  onShow() {
    this.loadData()
  },

  async loadData() {
    try {
      const detail = await getIssueDetail(this.data.id)
      const hydrated = await this.hydrateDetail(detail)
      const actionConfig = getActionConfig(hydrated.status, Boolean(hydrated.ownerName))
      this.setData({
        detail: hydrated,
        actionConfig,
        nextActionText: getNextActionHint(hydrated.status, Boolean(hydrated.ownerName)),
        latestAction: resolveLatestAction(hydrated)
      })
    } catch (error) {
      wx.showToast({
        title: getReadableError(error, '问题详情加载失败'),
        icon: 'none'
      })
    }
  },

  async hydrateDetail(detail) {
    const statusMeta = getStatusMeta(detail.status)
    const priorityMeta = getPriorityMeta(detail.priority)
    const issueFunctionMeta = getIssueFunctionMeta(detail.issueFunctionCode)
    const progressMeta = getProgressMeta(detail.status, Boolean(detail.overdue))
    const attachments = await this.hydrateAttachments(detail.attachments || [])
    const comments = await this.hydrateComments(detail.comments || [])

    return {
      ...detail,
      createdText: formatDateTime(detail.createdAt),
      dueText: formatDateTime(detail.dueAt),
      closedText: formatDateTime(detail.closedAt),
      statusLabel: statusMeta.label,
      priorityLabel: priorityMeta.label,
      issueFunctionLabel: issueFunctionMeta.label,
      priorityTone: priorityMeta.tone,
      progressLabel: progressMeta.label,
      progressTone: progressMeta.tone,
      attachments,
      comments,
      closeEvidenceAttachments: detail.status === 'CLOSED' ? resolveCloseEvidenceAttachments(comments) : []
    }
  },

  async hydrateComments(comments) {
    const hydratedComments = []
    for (const item of comments) {
      const attachments = await this.hydrateAttachments(item.attachments || [])
      hydratedComments.push({
        ...item,
        createdText: formatDateTime(item.createdAt),
        badge: item.commentType === 'SYSTEM' ? '系统记录' : isCloseEvidenceComment(item) ? '关闭证据' : '人工跟进',
        badgeTone: item.commentType === 'SYSTEM' ? 'gray' : isCloseEvidenceComment(item) ? 'orange' : 'blue',
        attachments
      })
    }
    return hydratedComments
  },

  async hydrateAttachments(attachments) {
    const tasks = (attachments || []).map(async (item) => {
      const attachment = normalizeAttachment(item)
      if (!attachment.id) {
        return attachment
      }

      try {
        const previewUrl = await downloadAttachmentToTemp(attachment.id)
        return {
          ...attachment,
          previewUrl,
          previewReady: true
        }
      } catch (error) {
        console.warn('[issue-detail] attachment preview hydrate failed', attachment.id, error)
        return {
          ...attachment,
          previewUrl: '',
          previewReady: false,
          previewError: true
        }
      }
    })
    return Promise.all(tasks)
  },

  onCommentInput(e) {
    this.setData({ commentContent: e.detail.value })
  },

  onCloseReasonInput(e) {
    this.setData({ closeReason: e.detail.value })
  },

  onCloseEvidenceInput(e) {
    this.setData({ closeEvidence: e.detail.value })
  },

  async chooseCommentImage() {
    await this.uploadMedia('comment', 'IMAGE')
  },

  async chooseCommentVideo() {
    await this.uploadMedia('comment', 'VIDEO')
  },

  async chooseCloseImage() {
    await this.uploadMedia('close', 'IMAGE')
  },

  async chooseCloseVideo() {
    await this.uploadMedia('close', 'VIDEO')
  },

  async uploadMedia(scope, mediaType) {
    const listKey = getDraftListKey(scope)
    const currentList = this.data[listKey] || []
    if (currentList.length >= 6) {
      wx.showToast({
        title: '最多上传 6 个附件',
        icon: 'none'
      })
      return
    }

    try {
      this.setData({ uploadLoading: true })
      wx.showLoading({
        title: mediaType === 'VIDEO' ? '视频处理中' : '图片处理中',
        mask: true
      })

      const maxCount = Math.max(1, 6 - currentList.length)
      const files = mediaType === 'VIDEO' ? await prepareVideo() : await prepareImages(maxCount)
      const uploads = await Promise.all(
        files.map((item) => this.uploadDraftAttachment(item, mediaType, scope))
      )

      this.setData({
        [listKey]: currentList.concat(uploads)
      })
    } catch (error) {
      if (error && error.errMsg && error.errMsg.includes('cancel')) {
        return
      }
      wx.showModal({
        title: '处理失败',
        content: getReadableError(error, mediaType === 'VIDEO' ? '视频处理失败' : '图片处理失败'),
        showCancel: false
      })
    } finally {
      wx.hideLoading()
      this.setData({ uploadLoading: false })
    }
  },

  async uploadDraftAttachment(file, mediaType, scope) {
    const uploaded = await uploadFile(file.tempFilePath, buildUploadFolder(scope, mediaType))
    return normalizeAttachment({
      ...uploaded,
      id: null,
      fileType: mediaType,
      localPath: file.tempFilePath,
      previewUrl: file.tempFilePath,
      previewReady: true,
      duration: file.duration || 0
    })
  },

  removeDraftAttachment(e) {
    const index = Number(e.currentTarget.dataset.index)
    const attachments = (this.data.commentAttachments || []).slice()
    attachments.splice(index, 1)
    this.setData({ commentAttachments: attachments })
  },

  removeCloseAttachment(e) {
    const index = Number(e.currentTarget.dataset.index)
    const attachments = (this.data.closeAttachments || []).slice()
    attachments.splice(index, 1)
    this.setData({ closeAttachments: attachments })
  },

  async previewAttachment(e) {
    const attachment = this.resolveAttachmentByDataset(e.currentTarget.dataset)
    if (!attachment) {
      return
    }

    let previewUrl = attachment.previewUrl || attachment.localPath || ''
    if (!previewUrl && attachment.id) {
      try {
        previewUrl = await downloadAttachmentToTemp(attachment.id)
      } catch (error) {
        wx.showToast({
          title: getReadableError(error, '附件预览失败'),
          icon: 'none'
        })
        return
      }
    }

    if (!previewUrl) {
      wx.showToast({
        title: '附件暂时无法预览',
        icon: 'none'
      })
      return
    }

    this.setData({
      previewVisible: true,
      previewType: attachment.fileType === 'VIDEO' ? 'VIDEO' : 'IMAGE',
      previewUrl,
      previewTitle: attachment.fileName || attachment.viewType || '附件预览'
    })
  },

  resolveAttachmentByDataset(dataset) {
    const scope = dataset.scope
    const index = Number(dataset.index || 0)

    if (scope === 'issue') {
      return this.data.detail && this.data.detail.attachments ? this.data.detail.attachments[index] : null
    }

    if (scope === 'close') {
      return this.data.detail && this.data.detail.closeEvidenceAttachments
        ? this.data.detail.closeEvidenceAttachments[index]
        : null
    }

    if (scope === 'comment') {
      const commentIndex = Number(dataset.commentIndex || 0)
      const comment = this.data.detail && this.data.detail.comments ? this.data.detail.comments[commentIndex] : null
      return comment && comment.attachments ? comment.attachments[index] : null
    }

    if (scope === 'draft-comment') {
      return this.data.commentAttachments[index]
    }

    if (scope === 'draft-close') {
      return this.data.closeAttachments[index]
    }

    return null
  },

  closePreviewLayer() {
    this.setData({
      previewVisible: false,
      previewType: 'IMAGE',
      previewUrl: '',
      previewTitle: ''
    })
  },

  noop() {},

  buildAttachmentPayload(list) {
    return (list || []).map((item) => ({
      fileName: item.fileName,
      objectKey: item.objectKey,
      fileUrl: item.fileUrl,
      fileSize: item.fileSize,
      contentType: item.contentType
    }))
  },

  async flushDraftComment(defaultContent) {
    if (!this.data.commentContent.trim() && !this.data.commentAttachments.length) {
      return false
    }

    await createIssueComment(this.data.id, {
      content: this.data.commentContent.trim() || defaultContent,
      attachments: this.buildAttachmentPayload(this.data.commentAttachments)
    })

    this.setData({
      commentContent: '',
      commentAttachments: []
    })
    return true
  },

  async flushCloseEvidenceComment(defaultContent) {
    if (!this.data.closeAttachments.length) {
      return false
    }

    const reason = this.data.closeEvidence.trim() || this.data.closeReason.trim() || defaultContent
    await createIssueComment(this.data.id, {
      content: `${CLOSE_EVIDENCE_PREFIX} ${reason}`,
      attachments: this.buildAttachmentPayload(this.data.closeAttachments)
    })

    this.setData({
      closeAttachments: []
    })
    return true
  },

  async submitComment() {
    if (!this.data.commentContent.trim() && !this.data.commentAttachments.length) {
      wx.showToast({
        title: '请先输入跟进或上传附件',
        icon: 'none'
      })
      return
    }

    wx.showLoading({
      title: '提交中',
      mask: true
    })
    try {
      await this.flushDraftComment('补充现场跟进')
      wx.showToast({
        title: '已提交跟进',
        icon: 'success'
      })
      await this.loadData()
    } catch (error) {
      wx.showToast({
        title: getReadableError(error, '提交跟进失败'),
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  },

  async handleStatusAction() {
    const detail = this.data.detail || {}
    const actionConfig = this.data.actionConfig || getActionConfig(detail.status, Boolean(detail.ownerName))
    if (!actionConfig) {
      wx.showToast({
        title: '当前状态无需推进',
        icon: 'none'
      })
      return
    }

    wx.showLoading({
      title: actionConfig.targetStatus === 'CLOSED' ? '关闭中' : '更新中',
      mask: true
    })

    try {
      if (actionConfig.targetStatus === 'CLOSED') {
        await this.flushDraftComment(this.data.closeReason.trim() || '关闭前补充跟进')
      } else {
        await this.flushDraftComment(`${actionConfig.text}前补充跟进`)
      }

      const payload = {
        targetStatus: actionConfig.targetStatus,
        remark: actionConfig.remark
      }

      if (actionConfig.solutionDesc) {
        payload.solutionDesc = actionConfig.solutionDesc
      }

      if (actionConfig.targetStatus === 'CLOSED') {
        payload.closeReason = this.data.closeReason.trim() || '问题已关闭'
        payload.closeEvidence = this.data.closeEvidence.trim() || (this.data.closeAttachments.length ? '见关闭证据附件' : '')
        payload.remark = payload.closeReason
      }

      await changeIssueStatus(this.data.id, payload)

      let closeEvidenceWarning = ''
      if (actionConfig.targetStatus === 'CLOSED' && this.data.closeAttachments.length) {
        try {
          await this.flushCloseEvidenceComment('关闭证据附件')
        } catch (error) {
          closeEvidenceWarning = getReadableError(error, '关闭证据上传失败，请稍后补传')
        }
      }

      wx.showToast({
        title: actionConfig.targetStatus === 'CLOSED' ? '问题已关闭' : '进度已更新',
        icon: 'success'
      })

      this.setData({
        closeReason: '',
        closeEvidence: '',
        closeAttachments: closeEvidenceWarning ? this.data.closeAttachments : []
      })

      await this.loadData()

      if (closeEvidenceWarning) {
        wx.showModal({
          title: '关闭已完成',
          content: closeEvidenceWarning,
          showCancel: false
        })
      }
    } catch (error) {
      wx.showToast({
        title: getReadableError(error, '状态更新失败'),
        icon: 'none'
      })
    } finally {
      wx.hideLoading()
    }
  },

  goBackToProject() {
    const currentProject = this.data.currentProject || getActiveProject()
    if (!currentProject || !currentProject.id) {
      wx.navigateBack()
      return
    }

    wx.redirectTo({
      url: `/pages/issue-list/index?projectId=${currentProject.id}&projectName=${encodeURIComponent(currentProject.projectName)}`
    })
  }
})
