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
    focusIssues: [],
    loading: false
  },
  onLoad(options) {
    const currentProject = resolveProject(options)
    if (currentProject) {
      setActiveProject(currentProject)
      wx.setNavigationBarTitle({
        title: `${currentProject.projectName || '项目'}工作台`
      })
    }
    this.setData({ currentProject })
  },
  onShow() {
    const currentProject = this.data.currentProject || getActiveProject()
    if (!currentProject || !currentProject.id) {
      wx.redirectTo({ url: '/pages/project-select/index?entry=hub' })
      return
    }
    this.setData({ currentProject })
    this.loadWorkbench()
  },
  async loadWorkbench() {
    const currentProject = this.data.currentProject
    if (!currentProject || !currentProject.id) {
      return
    }
    this.setData({ loading: true })
    wx.showLoading({ title: '加载中' })
    try {
      const dataset = await loadProjectIssueDataset(currentProject.id)
      const overview = buildProjectOverview(dataset.records, dataset.total)
      this.setData({
        summary: overview.summary,
        focusIssues: overview.focusIssues
      })
    } finally {
      this.setData({ loading: false })
      wx.hideLoading()
    }
  },
  goCreate() {
    const currentProject = this.data.currentProject
    wx.navigateTo({
      url: `/pages/issue-create/index?projectId=${currentProject.id}&projectName=${encodeURIComponent(currentProject.projectName)}`
    })
  },
  goIssueList() {
    const currentProject = this.data.currentProject
    wx.navigateTo({
      url: `/pages/issue-list/index?projectId=${currentProject.id}&projectName=${encodeURIComponent(currentProject.projectName)}`
    })
  },
  goProjectStats() {
    const currentProject = this.data.currentProject
    wx.navigateTo({
      url: `/pages/project-stats/index?projectId=${currentProject.id}&projectName=${encodeURIComponent(currentProject.projectName)}`
    })
  },
  goTodoIssues() {
    wx.navigateTo({ url: '/pages/todo-issues/index' })
  },
  switchProject() {
    wx.redirectTo({ url: '/pages/project-select/index?entry=hub' })
  },
  goIssueDetail(e) {
    const issue = this.data.focusIssues[Number(e.currentTarget.dataset.index)]
    const currentProject = this.data.currentProject
    wx.navigateTo({
      url: `/pages/issue-detail/index?id=${issue.id}&projectId=${currentProject.id}&projectName=${encodeURIComponent(currentProject.projectName)}`
    })
  }
})
