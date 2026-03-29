const { getActiveProject, setActiveProject } = require('../../utils/project-context')
const { loadProjectIssueDataset, buildProjectOverview } = require('../../utils/project-analytics')

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
    summary: {
      totalIssues: 0,
      openIssues: 0,
      closedIssues: 0,
      overdueIssues: 0,
      highPriorityIssues: 0,
      pendingVerifyIssues: 0
    },
    statusBars: [],
    priorityBars: [],
    focusIssues: []
  },
  onLoad(options) {
    const currentProject = resolveProject(options)
    if (currentProject) {
      setActiveProject(currentProject)
      wx.setNavigationBarTitle({
        title: `${currentProject.projectName || '项目'}统计`
      })
    }
    this.setData({ currentProject })
  },
  onShow() {
    const currentProject = this.data.currentProject || getActiveProject()
    if (!currentProject || !currentProject.id) {
      wx.redirectTo({ url: '/pages/project-select/index?entry=stats' })
      return
    }
    this.setData({ currentProject })
    this.loadStats()
  },
  async loadStats() {
    const currentProject = this.data.currentProject
    wx.showLoading({ title: '加载中' })
    try {
      const dataset = await loadProjectIssueDataset(currentProject.id)
      const overview = buildProjectOverview(dataset.records, dataset.total)
      this.setData({
        summary: overview.summary,
        statusBars: overview.statusBars,
        priorityBars: overview.priorityBars,
        focusIssues: overview.focusIssues
      })
    } finally {
      wx.hideLoading()
    }
  },
  switchProject() {
    wx.redirectTo({ url: '/pages/project-select/index?entry=stats' })
  },
  goProjectHub() {
    const currentProject = this.data.currentProject
    wx.redirectTo({
      url: `/pages/project-hub/index?projectId=${currentProject.id}&projectName=${encodeURIComponent(currentProject.projectName)}`
    })
  },
  goIssueDetail(e) {
    const issue = this.data.focusIssues[Number(e.currentTarget.dataset.index)]
    const currentProject = this.data.currentProject
    wx.navigateTo({
      url: `/pages/issue-detail/index?id=${issue.id}&projectId=${currentProject.id}&projectName=${encodeURIComponent(currentProject.projectName)}`
    })
  }
})
