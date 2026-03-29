const { createIssue } = require('../../api/issue')
const { uploadFile } = require('../../utils/upload')
const { prepareImages, prepareVideo, MAX_MEDIA_SIZE, MAX_VIDEO_DURATION } = require('../../utils/media')
const {
  getAttachmentType,
  formatFileSize,
  formatDateTime,
  toLocalDateTimeString
} = require('../../utils/presenter')
const { getActiveProject, setActiveProject } = require('../../utils/project-context')

const priorityOptions = [
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'CRITICAL', label: '紧急' }
]

const impactOptions = [
  { value: 'MEDIUM', label: '中影响' },
  { value: 'HIGH', label: '高影响' },
  { value: 'CRITICAL', label: '关键影响' }
]

function resolveProject(options) {
  if (options.projectId) {
    return {
      id: Number(options.projectId),
      projectName: decodeURIComponent(options.projectName || '')
    }
  }
  return getActiveProject()
}

function getCurrentUser() {
  const app = getApp()
  return app.globalData.currentUser || wx.getStorageSync('currentUser') || {}
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

Page({
  data: {
    currentProject: null,
    currentUser: {},
    projectMissing: false,
    uploadLoading: false,
    submitTimeLabel: '',
    mediaRuleLabel: `单文件 <= ${Math.floor(MAX_MEDIA_SIZE / 1024 / 1024)}MB / 视频 <= ${MAX_VIDEO_DURATION}s`,
    form: {
      title: '',
      description: '',
      projectId: null,
      projectName: '',
      categoryCode: 'MECHANICAL',
      sourceCode: 'SITE_CHECK',
      priority: 'HIGH',
      impactLevel: 'HIGH',
      affectShipment: false,
      affectCommissioning: true,
      needShutdown: false,
      occurredAt: ''
    },
    priorityOptions,
    impactOptions,
    attachments: []
  },
  showBusy(title) {
    if (this.loadingVisible) {
      return
    }
    this.loadingVisible = true
    wx.showLoading({
      title,
      mask: true
    })
  },
  hideBusy() {
    if (!this.loadingVisible) {
      return
    }
    this.loadingVisible = false
    wx.hideLoading()
  },
  showError(error, fallback) {
    this.hideBusy()
    wx.showModal({
      title: '处理失败',
      content: getReadableError(error, fallback),
      showCancel: false
    })
  },
  onLoad(options) {
    const currentProject = resolveProject(options)
    this.applyProject(currentProject)
    this.syncReporter()
  },
  onShow() {
    if (!this.data.currentProject || !this.data.currentProject.id) {
      this.applyProject(getActiveProject())
    }
    this.syncReporter()
  },
  syncReporter() {
    const occurredAt = toLocalDateTimeString(new Date())
    this.setData({
      currentUser: getCurrentUser(),
      submitTimeLabel: formatDateTime(occurredAt),
      'form.occurredAt': occurredAt
    })
  },
  applyProject(currentProject) {
    if (!currentProject || !currentProject.id) {
      this.setData({
        currentProject: null,
        projectMissing: true,
        'form.projectId': null,
        'form.projectName': ''
      })
      return
    }

    setActiveProject(currentProject)
    this.setData({
      currentProject,
      projectMissing: false,
      'form.projectId': currentProject.id,
      'form.projectName': currentProject.projectName
    })
  },
  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },
  switchProject() {
    wx.redirectTo({ url: '/pages/project-select/index?entry=create' })
  },
  goSelectProject() {
    wx.redirectTo({ url: '/pages/project-select/index?entry=create' })
  },
  selectPriority(e) {
    this.setData({
      'form.priority': e.currentTarget.dataset.value
    })
  },
  selectImpact(e) {
    this.setData({
      'form.impactLevel': e.currentTarget.dataset.value
    })
  },
  onSwitchChange(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      [`form.${field}`]: e.detail.value
    })
  },
  async chooseImage() {
    const remaining = Math.max(1, 6 - this.data.attachments.length)
    if (this.data.attachments.length >= 6) {
      wx.showToast({ title: '最多上传 6 个附件', icon: 'none' })
      return
    }

    try {
      this.setData({ uploadLoading: true })
      this.showBusy('图片处理中')
      const files = await prepareImages(remaining)
      const uploads = await Promise.all(files.map((item) => this.uploadMedia(item, 'IMAGE')))
      this.setData({
        attachments: this.data.attachments.concat(uploads)
      })
    } catch (error) {
      if (error && error.errMsg && error.errMsg.includes('cancel')) {
        return
      }
      console.error('[issue-create] image upload failed', error)
      this.showError(error, '图片处理失败')
    } finally {
      this.hideBusy()
      this.setData({ uploadLoading: false })
    }
  },
  async chooseVideo() {
    try {
      this.setData({ uploadLoading: true })
      this.showBusy('视频处理中')
      const files = await prepareVideo()
      const uploads = await Promise.all(files.map((item) => this.uploadMedia(item, 'VIDEO')))
      this.setData({
        attachments: this.data.attachments.concat(uploads)
      })
    } catch (error) {
      if (error && error.errMsg && error.errMsg.includes('cancel')) {
        return
      }
      console.error('[issue-create] video upload failed', error)
      this.showError(error, '视频处理失败')
    } finally {
      this.hideBusy()
      this.setData({ uploadLoading: false })
    }
  },
  async uploadMedia(file, mediaType) {
    const folder = mediaType === 'VIDEO' ? 'issue-video' : 'issue-image'
    const uploaded = await uploadFile(file.tempFilePath, folder)
    return {
      ...uploaded,
      fileType: mediaType,
      mediaType,
      duration: file.duration || 0,
      localPath: file.tempFilePath,
      viewType: getAttachmentType(mediaType),
      viewSize: formatFileSize(uploaded.fileSize),
      viewDuration: file.duration ? `${Math.round(file.duration)}s` : ''
    }
  },
  removeAttachment(e) {
    const index = Number(e.currentTarget.dataset.index)
    const attachments = this.data.attachments.slice()
    attachments.splice(index, 1)
    this.setData({ attachments })
  },
  async submit() {
    if (this.data.projectMissing) {
      wx.showToast({ title: '请先选择项目', icon: 'none' })
      return
    }
    if (!this.data.form.title.trim()) {
      wx.showToast({ title: '请填写问题标题', icon: 'none' })
      return
    }
    if (!this.data.attachments.length) {
      wx.showToast({ title: '至少上传 1 张照片或 1 段视频', icon: 'none' })
      return
    }

    await createIssue({
      ...this.data.form,
      attachments: this.data.attachments.map((item) => ({
        fileName: item.fileName,
        objectKey: item.objectKey,
        fileUrl: item.fileUrl,
        fileSize: item.fileSize,
        contentType: item.contentType
      }))
    })
    wx.showToast({ title: '问题已提交', icon: 'success' })
    wx.redirectTo({
      url: `/pages/issue-list/index?projectId=${this.data.form.projectId}&projectName=${encodeURIComponent(this.data.form.projectName)}`
    })
  }
})
