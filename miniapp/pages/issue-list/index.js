const { getIssueList } = require('../../api/issue')
const { getProjectIssueSummary, getProjectMembers } = require('../../api/project')
const { getActiveProject, setActiveProject } = require('../../utils/project-context')

const statusTabs = [
  { value: 'ALL', label: '全部' },
  { value: 'NEW', label: '新建' },
  { value: 'IN_PROGRESS', label: '处理中' },
  { value: 'PENDING_VERIFY', label: '待验证' },
  { value: 'CLOSED', label: '已关闭' }
]

const issueFunctionOptions = [
  { label: '全部属性', value: '' },
  { label: 'PAT', value: 'PAT' },
  { label: 'FAT', value: 'FAT' },
  { label: '设计', value: 'DESIGN' },
  { label: '安全', value: 'SAFETY' },
  { label: '物流', value: 'LOGISTICS' },
  { label: '采购', value: 'PROCUREMENT' },
  { label: '装配', value: 'ASSEMBLY' }
]

const defaultOwnerOption = { label: '全部责任人', value: '' }

function resolveProject(options) {
  if (options.projectId) {
    return {
      id: Number(options.projectId),
      projectName: decodeURIComponent(options.projectName || '')
    }
  }
  return getActiveProject()
}

Page({
  data: {
    currentProject: null,
    projectMissing: false,
    keyword: '',
    status: 'ALL',
    statusTabs,
    ownerOptions: [defaultOwnerOption],
    ownerIndex: 0,
    ownerLabel: defaultOwnerOption.label,
    ownerId: '',
    functionOptions: issueFunctionOptions,
    functionIndex: 0,
    functionLabel: issueFunctionOptions[0].label,
    issueFunctionCode: '',
    list: [],
    pageNo: 0,
    pageSize: 100,
    loading: false,
    hasMore: false,
    summary: {
      total: 0,
      loaded: 0,
      processing: 0,
      overdue: 0,
      closed: 0
    },
    lastError: ''
  },

  onLoad(options) {
    wx.setNavigationBarTitle({ title: '项目 OPL' })
    this.applyProject(resolveProject(options))
  },

  onShow() {
    if (this.data.currentProject && this.data.currentProject.id) {
      this.loadData(true)
      return
    }
    this.applyProject(getActiveProject())
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadData(false)
    }
  },

  applyProject(currentProject) {
    const resetPayload = {
      list: [],
      pageNo: 0,
      hasMore: false,
      ownerOptions: [defaultOwnerOption],
      ownerIndex: 0,
      ownerLabel: defaultOwnerOption.label,
      ownerId: '',
      functionOptions: issueFunctionOptions,
      functionIndex: 0,
      functionLabel: issueFunctionOptions[0].label,
      issueFunctionCode: '',
      summary: {
        total: 0,
        loaded: 0,
        processing: 0,
        overdue: 0,
        closed: 0
      }
    }

    if (!currentProject || !currentProject.id) {
      this.setData({
        currentProject: null,
        projectMissing: true,
        ...resetPayload
      })
      return
    }

    setActiveProject(currentProject)
    this.setData(
      {
        currentProject,
        projectMissing: false,
        lastError: '',
        ...resetPayload
      },
      async () => {
        await this.loadOwnerOptions()
        await this.loadData(true)
      }
    )
  },

  async loadOwnerOptions() {
    if (!this.data.currentProject || !this.data.currentProject.id) {
      return
    }
    try {
      const members = await getProjectMembers(this.data.currentProject.id)
      const ownerOptions = [defaultOwnerOption].concat(
        (members || []).map((item) => ({
          label: `${item.realName || item.username}${item.projectRoleName ? ` / ${item.projectRoleName}` : ''}`,
          value: String(item.userId)
        }))
      )
      this.setData({
        ownerOptions,
        ownerIndex: 0,
        ownerLabel: ownerOptions[0].label,
        ownerId: ''
      })
    } catch (error) {
      console.error('[issue-list] load owners failed', error)
    }
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onOwnerChange(e) {
    const ownerIndex = Number(e.detail.value || 0)
    const owner = this.data.ownerOptions[ownerIndex] || this.data.ownerOptions[0]
    this.setData({
      ownerIndex,
      ownerLabel: owner.label,
      ownerId: owner.value
    })
  },

  onFunctionChange(e) {
    const functionIndex = Number(e.detail.value || 0)
    const issueFunction = this.data.functionOptions[functionIndex] || this.data.functionOptions[0]
    this.setData({
      functionIndex,
      functionLabel: issueFunction.label,
      issueFunctionCode: issueFunction.value
    })
  },

  handleSearch() {
    this.loadData(true)
  },

  chooseStatus(e) {
    this.setData({ status: e.currentTarget.dataset.value }, () => {
      this.loadData(true)
    })
  },

  switchProject() {
    wx.redirectTo({ url: '/pages/project-select/index?entry=opl' })
  },

  goSelectProject() {
    wx.redirectTo({ url: '/pages/project-select/index?entry=opl' })
  },

  async loadData(reset = true) {
    if (!this.data.currentProject || !this.data.currentProject.id || this.data.loading) {
      return
    }

    const current = reset ? 1 : this.data.pageNo + 1
    this.setData({ loading: true })

    try {
      const params = {
        current,
        size: this.data.pageSize,
        projectId: this.data.currentProject.id,
        keyword: this.data.keyword || undefined,
        ownerId: this.data.ownerId || undefined,
        issueFunctionCode: this.data.issueFunctionCode || undefined
      }

      if (this.data.status !== 'ALL') {
        params.statusList = this.data.status
      }

      const requests = [getIssueList(params)]
      if (reset) {
        requests.push(getProjectIssueSummary(this.data.currentProject.id))
      }

      const [data, projectSummary] = await Promise.all(requests)
      const records = data.records || []
      const list = reset ? records : this.data.list.concat(records)

      this.setData({
        list,
        pageNo: current,
        hasMore: list.length < (data.total || 0),
        lastError: '',
        summary: {
          total: projectSummary ? projectSummary.totalIssues || 0 : this.data.summary.total,
          loaded: list.length,
          processing: projectSummary ? projectSummary.processingIssues || 0 : this.data.summary.processing,
          overdue: projectSummary ? projectSummary.overdueIssues || 0 : this.data.summary.overdue,
          closed: projectSummary ? projectSummary.closedIssues || 0 : this.data.summary.closed
        }
      })
    } catch (error) {
      console.error('[issue-list] load failed', error)
      const message = error && error.message ? error.message : '问题列表加载失败'
      this.setData({ lastError: message })
      wx.showToast({ title: message, icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },

  goDetail(e) {
    const issue = e.detail
    wx.navigateTo({
      url: `/pages/issue-detail/index?id=${issue.id}&projectId=${this.data.currentProject.id}&projectName=${encodeURIComponent(this.data.currentProject.projectName)}`
    })
  }
})
