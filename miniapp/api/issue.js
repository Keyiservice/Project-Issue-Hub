const { request } = require('../utils/request')

function createIssue(data) {
  return request({
    url: '/issues',
    method: 'POST',
    data
  })
}

function getIssueList(data) {
  return request({
    url: '/issues',
    data
  })
}

function getIssueDetail(id) {
  return request({
    url: `/issues/${id}`
  })
}

function changeIssueStatus(id, data) {
  return request({
    url: `/issues/${id}/status`,
    method: 'PUT',
    data
  })
}

function createIssueComment(id, data) {
  return request({
    url: `/issues/${id}/comments`,
    method: 'POST',
    data
  })
}

module.exports = {
  createIssue,
  getIssueList,
  getIssueDetail,
  changeIssueStatus,
  createIssueComment
}
