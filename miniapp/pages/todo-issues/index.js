const { getIssueList } = require('../../api/issue')

Page({
  data: {
    list: []
  },
  onShow() {
    this.loadData()
  },
  async loadData() {
    const app = getApp()
    const currentUser = app.globalData.currentUser || wx.getStorageSync('currentUser')
    if (!currentUser || !currentUser.userId) {
      this.setData({ list: [] })
      return
    }
    const data = await getIssueList({
      current: 1,
      size: 20,
      ownerId: currentUser.userId,
      statusList: ['NEW', 'ACCEPTED', 'IN_PROGRESS', 'PENDING_VERIFY', 'ON_HOLD']
    })
    this.setData({ list: data.records || [] })
  },
  goDetail(e) {
    const issue = e.detail
    wx.navigateTo({ url: `/pages/issue-detail/index?id=${issue.id}` })
  }
})
