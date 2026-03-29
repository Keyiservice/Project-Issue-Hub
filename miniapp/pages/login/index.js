const { miniappLogin, bindMiniapp, changePassword } = require('../../api/auth')
const PPT_TARGET_ROUTE = ''

const DEV_OPEN_ID_KEY = 'miniappDevOpenId'

function createDevOpenId() {
  return `dev-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

function ensureDevOpenId() {
  let devOpenId = wx.getStorageSync(DEV_OPEN_ID_KEY)
  if (!devOpenId) {
    devOpenId = createDevOpenId()
    wx.setStorageSync(DEV_OPEN_ID_KEY, devOpenId)
  }
  return devOpenId
}

function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(res) {
        if (res.code) {
          resolve(res.code)
          return
        }
        reject(new Error('未获取到微信登录凭证'))
      },
      fail(err) {
        reject(err)
      }
    })
  })
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

function buildCurrentUser(result) {
  return {
    userId: result.userId,
    username: result.username,
    realName: result.realName,
    departmentCode: result.departmentCode,
    departmentName: result.departmentName,
    roles: result.roles || [],
    wechatBound: !!result.wechatBound,
    passwordChangeRequired: !!result.passwordChangeRequired
  }
}

Page({
  data: {
    mode: 'wechat',
    loading: false,
    devOpenId: '',
    pendingCode: '',
    currentUser: null,
    networkNotice: '',
    bindForm: {
      username: '',
      password: ''
    },
    passwordForm: {
      newPassword: '',
      confirmPassword: ''
    }
  },
  onLoad() {
    if (wx.getStorageSync('token')) {
      try {
        const system = wx.getSystemInfoSync()
        if (system.platform === 'devtools' && PPT_TARGET_ROUTE) {
          wx.reLaunch({ url: PPT_TARGET_ROUTE })
          return
        }
      } catch (error) {}
      wx.reLaunch({ url: '/pages/home/index' })
      return
    }
    const baseUrl = getApp().globalData.baseUrl || ''
    this.setData({
      devOpenId: ensureDevOpenId(),
      networkNotice:
        baseUrl.includes('127.0.0.1') || baseUrl.includes('localhost')
          ? '当前接口地址仍是本机 127.0.0.1，仅开发者工具可用。真机调试前请改成可访问的 HTTPS 域名。'
          : ''
    })
  },
  onInput(e) {
    const form = e.currentTarget.dataset.form
    const field = e.currentTarget.dataset.field
    this.setData({
      [`${form}.${field}`]: e.detail.value
    })
  },
  saveSession(result) {
    const app = getApp()
    const currentUser = buildCurrentUser(result)
    app.globalData.token = result.accessToken
    app.globalData.currentUser = currentUser
    wx.setStorageSync('token', result.accessToken)
    wx.setStorageSync('currentUser', currentUser)
    this.setData({ currentUser })
  },
  showError(error, fallback) {
    wx.showModal({
      title: '操作失败',
      content: getReadableError(error, fallback),
      showCancel: false
    })
  },
  resetToWechat() {
    this.setData({
      mode: 'wechat',
      pendingCode: '',
      bindForm: {
        username: '',
        password: ''
      }
    })
  },
  async finishLogin(result) {
    this.saveSession(result)
    if (result.passwordChangeRequired) {
      this.setData({
        mode: 'change-password',
        passwordForm: {
          newPassword: '',
          confirmPassword: ''
        }
      })
      return
    }
    wx.reLaunch({ url: '/pages/home/index' })
  },
  async handleWechatLogin() {
    this.setData({ loading: true })
    try {
      const code = await wxLogin()
      const result = await miniappLogin({
        code,
        devOpenId: this.data.devOpenId
      })
      if (result.bound === false) {
        this.setData({
          mode: 'bind',
          pendingCode: code
        })
        return
      }
      await this.finishLogin(result)
    } catch (error) {
      console.error('[login] miniapp login failed', error)
      this.showError(error, '微信登录失败')
    } finally {
      this.setData({ loading: false })
    }
  },
  async handleBind() {
    const { username, password } = this.data.bindForm
    if (!username.trim() || !password) {
      wx.showToast({ title: '请输入企业账号和初始密码', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      const code = this.data.pendingCode || await wxLogin()
      const result = await bindMiniapp({
        code,
        devOpenId: this.data.devOpenId,
        username: username.trim(),
        password
      })
      await this.finishLogin(result)
    } catch (error) {
      console.error('[login] miniapp bind failed', error)
      this.showError(error, '账号绑定失败')
    } finally {
      this.setData({ loading: false })
    }
  },
  async handleChangePassword() {
    const { newPassword, confirmPassword } = this.data.passwordForm
    if (!newPassword || !confirmPassword) {
      wx.showToast({ title: '请输入并确认新密码', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
      return
    }

    this.setData({ loading: true })
    try {
      await changePassword({
        oldPassword: '',
        newPassword,
        confirmPassword
      })
      const app = getApp()
      const currentUser = {
        ...(app.globalData.currentUser || wx.getStorageSync('currentUser') || {}),
        passwordChangeRequired: false
      }
      app.globalData.currentUser = currentUser
      wx.setStorageSync('currentUser', currentUser)
      wx.reLaunch({ url: '/pages/home/index' })
    } catch (error) {
      console.error('[login] change password failed', error)
      this.showError(error, '密码修改失败')
    } finally {
      this.setData({ loading: false })
    }
  }
})
