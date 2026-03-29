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
    ownerOptions: [{ label: '全部责任人', value: '' }],
    ownerIndex: 0,
    ownerLabel: '全部责任人',
    ownerId: '',
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
    const currentProject = resolveProject(options)
    this.applyProject(currentProject)
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
    if (!currentProject || !currentProject.id) {
      this.setData({
        currentProject: null,
        projectMissing: true,
        list: [],
        pageNo: 0,
        hasMore: false,
        ownerOptions: [{ label: '全部责任人', value: '' }],
        ownerIndex: 0,
        ownerLabel: '全部责任人',
        ownerId: '',
        summary: {
          total: 0,
          loaded: 0,
          processing: 0,
          overdue: 0,
          closed: 0
        }
      })
      return
    }
    setActiveProject(currentProject)
    this.setData(
      {
        currentProject,
        projectMissing: false,
        list: [],
        pageNo: 0,
        hasMore: false,
        ownerOptions: [{ label: '全部责任人', value: '' }],
        ownerIndex: 0,
        ownerLabel: '全部责任人',
        ownerId: '',
        lastError: ''
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
      const ownerOptions = [{ label: '全部责任人', value: '' }].concat(
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
        keyword: this.data.keyword,
        ownerId: this.data.ownerId || undefined
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
      const totalIssues = projectSummary ? (projectSummary.totalIssues || 0) : this.data.summary.total
      const processingIssues = projectSummary ? (projectSummary.processingIssues || 0) : this.data.summary.processing
      const overdueIssues = projectSummary ? (projectSummary.overdueIssues || 0) : this.data.summary.overdue
      const closedIssues = projectSummary ? (projectSummary.closedIssues || 0) : this.data.summary.closed
      this.setData({
        list,
        pageNo: current,
        hasMore: list.length < (data.total || 0),
        lastError: '',
        summary: {
          total: totalIssues || data.total || list.length,
          loaded: list.length,
          processing: processingIssues || 0,
          overdue: overdueIssues || 0,
          closed: closedIssues || 0
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
