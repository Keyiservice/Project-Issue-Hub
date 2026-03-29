const app = getApp()

function handleUnauthorized(message) {
  app.globalData.token = ''
  app.globalData.currentUser = null
  wx.removeStorageSync('token')
  wx.removeStorageSync('currentUser')
  wx.showToast({ title: message || '登录已失效，请重新登录', icon: 'none' })
  setTimeout(() => {
    wx.reLaunch({ url: '/pages/login/index' })
  }, 300)
}

function sanitizeQuery(data) {
  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    return data
  }
  const result = {}
  Object.keys(data).forEach((key) => {
    const value = data[key]
    if (value === undefined || value === null || value === '') {
      return
    }
    if (Array.isArray(value) && value.length === 0) {
      return
    }
    result[key] = value
  })
  return result
}

function isLocalBaseUrl() {
  const baseUrl = app.globalData.baseUrl || ''
  return baseUrl.includes('127.0.0.1') || baseUrl.includes('localhost')
}

function request(options) {
  return new Promise((resolve, reject) => {
    const token = app.globalData.token || wx.getStorageSync('token') || ''
    const method = options.method || 'GET'
    wx.request({
      url: `${app.globalData.baseUrl}${options.url}`,
      method,
      data: method === 'GET' ? sanitizeQuery(options.data) : options.data,
      header: {
        'content-type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        ...(options.header || {})
      },
      success(res) {
        const result = res.data
        if (result.code === 0) {
          resolve(result.data)
          return
        }
        if (result.code === 401) {
          handleUnauthorized(result.message)
          reject(result)
          return
        }
        wx.showToast({ title: result.message || '请求失败', icon: 'none' })
        reject(result)
      },
      fail(err) {
        let message = '网络异常'
        if (err && err.errMsg && err.errMsg.includes('url not in domain list')) {
          message = '请关闭开发者工具域名校验或改用已配置域名'
        } else if (isLocalBaseUrl()) {
          message = '当前接口地址是本机 127.0.0.1，仅开发者工具可用；真机请切换到 HTTPS 域名'
        }
        wx.showToast({ title: message, icon: 'none' })
        reject(err)
      }
    })
  })
}

module.exports = {
  request
}
