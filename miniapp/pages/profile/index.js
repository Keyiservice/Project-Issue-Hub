const { getCurrentUser, changePassword } = require('../../api/auth')

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
    currentUser: {},
    version: 'MVP 0.3',
    passwordFormVisible: false,
    submitting: false,
    passwordForm: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  },
  async onShow() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/index' })
      return
    }
    await this.loadCurrentUser()
  },
  async loadCurrentUser() {
    try {
      const user = await getCurrentUser()
      const app = getApp()
      app.globalData.currentUser = user
      wx.setStorageSync('currentUser', user)
      this.setData({ currentUser: user })
    } catch (error) {
      console.error('[profile] load current user failed', error)
    }
  },
  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      [`passwordForm.${field}`]: e.detail.value
    })
  },
  togglePasswordForm() {
    this.setData({
      passwordFormVisible: !this.data.passwordFormVisible,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    })
  },
  async submitPasswordChange() {
    const { oldPassword, newPassword, confirmPassword } = this.data.passwordForm
    if (!newPassword || !confirmPassword) {
      wx.showToast({ title: '请输入并确认新密码', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
      return
    }
    if (!this.data.currentUser.passwordChangeRequired && !oldPassword) {
      wx.showToast({ title: '请输入旧密码', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    try {
      await changePassword({
        oldPassword,
        newPassword,
        confirmPassword
      })
      wx.showToast({ title: '密码已更新', icon: 'success' })
      this.setData({
        passwordFormVisible: false,
        passwordForm: {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      })
      await this.loadCurrentUser()
    } catch (error) {
      wx.showModal({
        title: '修改失败',
        content: getReadableError(error, '密码修改失败'),
        showCancel: false
      })
    } finally {
      this.setData({ submitting: false })
    }
  },
  logout() {
    const app = getApp()
    app.globalData.token = ''
    app.globalData.currentUser = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('currentUser')
    wx.redirectTo({ url: '/pages/login/index' })
  }
})
