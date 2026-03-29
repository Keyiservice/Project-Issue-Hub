const { getDashboardStats } = require('../../api/stats')
const PPT_TARGET_ROUTE = ''

function canViewManagementStats(currentUser) {
  const roles = currentUser.roles || []
  return roles.includes('PROJECT_MANAGER') || roles.includes('MANAGEMENT') || roles.includes('ADMIN')
}

Page({
  data: {
    currentUser: {},
    activeProject: null,
    canViewStats: false,
    stats: {
      totalIssues: 0,
      openIssues: 0,
      highPriorityIssues: 0,
      overdueIssues: 0
    }
  },
  onShow() {
    const app = getApp()
    if (!app.globalData.token) {
      wx.reLaunch({ url: '/pages/login/index' })
      return
    }
    if (PPT_TARGET_ROUTE) {
      const system = wx.getSystemInfoSync()
      if (system.platform === 'devtools') {
        wx.redirectTo({ url: PPT_TARGET_ROUTE })
        return
      }
    }
    const currentUser = app.globalData.currentUser || wx.getStorageSync('currentUser') || {}
    const activeProject = app.globalData.activeProject || wx.getStorageSync('activeProject') || null
    const canViewStats = canViewManagementStats(currentUser)
    this.setData({
      currentUser,
      activeProject,
      canViewStats
    })
    if (canViewStats) {
      this.loadStats()
    }
  },
  async loadStats() {
    try {
      const stats = await getDashboardStats()
      this.setData({ stats })
    } catch (error) {
      this.setData({
        stats: {
          totalIssues: 0,
          openIssues: 0,
          highPriorityIssues: 0,
          overdueIssues: 0
        }
      })
    }
  },
  goProjectHub() {
    wx.navigateTo({ url: '/pages/project-select/index?entry=hub' })
  },
  goCurrentProject() {
    const activeProject = this.data.activeProject
    if (activeProject && activeProject.id) {
      wx.navigateTo({
        url: `/pages/project-hub/index?projectId=${activeProject.id}&projectName=${encodeURIComponent(activeProject.projectName || '')}`
      })
      return
    }
    this.goProjectHub()
  },
  goQuickCreate() {
    wx.navigateTo({ url: '/pages/project-select/index?entry=create' })
  },
  goProjectOpl() {
    wx.navigateTo({ url: '/pages/project-select/index?entry=opl' })
  },
  goProjectStats() {
    wx.navigateTo({ url: '/pages/project-select/index?entry=stats' })
  },
  goMyIssues() {
    wx.navigateTo({ url: '/pages/my-issues/index' })
  },
  goTodoIssues() {
    wx.navigateTo({ url: '/pages/todo-issues/index' })
  },
  goProfile() {
    wx.navigateTo({ url: '/pages/profile/index' })
  }
})
