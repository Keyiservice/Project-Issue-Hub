const { request } = require('../utils/request')

function getProjectAll() {
  return request({
    url: '/projects/all'
  })
}

function getProjectIssueSummary(projectId) {
  return request({
    url: `/projects/${projectId}/issue-summary`
  })
}

function getProjectMembers(projectId) {
  return request({
    url: `/projects/${projectId}/members`
  })
}

module.exports = {
  getProjectAll,
  getProjectIssueSummary,
  getProjectMembers
}
