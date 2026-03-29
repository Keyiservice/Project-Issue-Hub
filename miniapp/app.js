const DEFAULT_API_BASE_URL = wx.getStorageSync('apiBaseUrl') || 'http://127.0.0.1:8081/api'

App({
  globalData: {
    baseUrl: DEFAULT_API_BASE_URL,
    token: wx.getStorageSync('token') || '',
    currentUser: wx.getStorageSync('currentUser') || null,
    activeProject: wx.getStorageSync('activeProject') || null
  },
  onLaunch() {
    const token = wx.getStorageSync('token')
    const currentUser = wx.getStorageSync('currentUser')
    const activeProject = wx.getStorageSync('activeProject')
    const apiBaseUrl = wx.getStorageSync('apiBaseUrl')

    if (token) {
      this.globalData.token = token
    }
    if (currentUser) {
      this.globalData.currentUser = currentUser
    }
    if (activeProject) {
      this.globalData.activeProject = activeProject
    }
    if (apiBaseUrl) {
      this.globalData.baseUrl = apiBaseUrl
    }
  }
})
