const { request } = require('../utils/request')

function getDashboardStats() {
  return request({
    url: '/stats/dashboard'
  })
}

module.exports = {
  getDashboardStats
}
