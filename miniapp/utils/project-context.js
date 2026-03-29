const STORAGE_KEY = 'activeProject'

function setActiveProject(project) {
  wx.setStorageSync(STORAGE_KEY, project)
  const app = getApp()
  app.globalData.activeProject = project
}

function getActiveProject() {
  const app = getApp()
  return app.globalData.activeProject || wx.getStorageSync(STORAGE_KEY) || null
}

function clearActiveProject() {
  wx.removeStorageSync(STORAGE_KEY)
  const app = getApp()
  app.globalData.activeProject = null
}

module.exports = {
  setActiveProject,
  getActiveProject,
  clearActiveProject
}
