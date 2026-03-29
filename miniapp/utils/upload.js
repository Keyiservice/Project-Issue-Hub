const app = getApp()

function handleUnauthorized(message) {
  app.globalData.token = ''
  app.globalData.currentUser = null
  wx.removeStorageSync('token')
  wx.removeStorageSync('currentUser')
  setTimeout(() => {
    wx.reLaunch({ url: '/pages/login/index' })
  }, 300)
  return new Error(message || '登录已失效，请重新登录')
}

function isLocalBaseUrl() {
  const baseUrl = app.globalData.baseUrl || ''
  return baseUrl.includes('127.0.0.1') || baseUrl.includes('localhost')
}

function normalizeUploadError(error) {
  if (!error) {
    return '上传失败'
  }
  if (error.message) {
    return error.message
  }
  if (error.errMsg) {
    if (error.errMsg.includes('url not in domain list')) {
      return '请关闭开发者工具域名校验或改用已配置域名'
    }
    if (isLocalBaseUrl()) {
      return '当前接口地址是本机 127.0.0.1，仅开发者工具可用；真机请切换到 HTTPS 域名'
    }
    return error.errMsg
  }
  return '上传失败'
}

function uploadFile(filePath, bizFolder = 'issue') {
  return new Promise((resolve, reject) => {
    const token = app.globalData.token || wx.getStorageSync('token') || ''

    wx.uploadFile({
      url: `${app.globalData.baseUrl}/attachments/upload?bizFolder=${bizFolder}`,
      filePath,
      name: 'file',
      timeout: 60000,
      header: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      success(res) {
        let result = null
        try {
          result = JSON.parse(res.data)
        } catch (error) {
          reject(new Error('上传响应解析失败'))
          return
        }

        if (res.statusCode === 401 || result.code === 401) {
          reject(handleUnauthorized(result.message))
          return
        }

        if (res.statusCode !== 200) {
          reject(new Error(result.message || `上传失败，状态码 ${res.statusCode}`))
          return
        }

        if (result.code === 0) {
          resolve(result.data)
          return
        }

        reject(new Error(result.message || '上传失败'))
      },
      fail(err) {
        reject(new Error(normalizeUploadError(err)))
      }
    })
  })
}

module.exports = {
  uploadFile
}
