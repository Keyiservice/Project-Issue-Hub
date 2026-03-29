const { request } = require('../utils/request')

function login(data) {
  return request({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

function miniappLogin(data) {
  return request({
    url: '/auth/miniapp-login',
    method: 'POST',
    data
  })
}

function bindMiniapp(data) {
  return request({
    url: '/auth/miniapp-bind',
    method: 'POST',
    data
  })
}

function changePassword(data) {
  return request({
    url: '/auth/change-password',
    method: 'POST',
    data
  })
}

function getCurrentUser() {
  return request({
    url: '/auth/me',
    method: 'GET'
  })
}

module.exports = {
  login,
  miniappLogin,
  bindMiniapp,
  changePassword,
  getCurrentUser
}
