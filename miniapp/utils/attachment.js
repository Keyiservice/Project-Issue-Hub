function getAppInstance() {
  try {
    return getApp()
  } catch (error) {
    return null
  }
}

function getBaseUrl() {
  const app = getAppInstance()
  return (app && app.globalData && app.globalData.baseUrl) || wx.getStorageSync('apiBaseUrl') || ''
}

function getToken() {
  const app = getAppInstance()
  return (app && app.globalData && app.globalData.token) || wx.getStorageSync('token') || ''
}

function buildAttachmentContentUrl(attachmentId) {
  return `${getBaseUrl()}/attachments/${attachmentId}/content`
}

function downloadAttachmentToTemp(attachmentId) {
  return new Promise((resolve, reject) => {
    wx.downloadFile({
      url: buildAttachmentContentUrl(attachmentId),
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success(res) {
        if (res.statusCode === 200 && res.tempFilePath) {
          resolve(res.tempFilePath)
          return
        }
        reject(new Error(`附件下载失败，状态码 ${res.statusCode}`))
      },
      fail(err) {
        reject(new Error(err && err.errMsg ? err.errMsg : '附件下载失败'))
      }
    })
  })
}

module.exports = {
  buildAttachmentContentUrl,
  downloadAttachmentToTemp
}
