const statusMap = {
  NEW: { label: '新建', tone: 'gray' },
  IN_PROGRESS: { label: '处理中', tone: 'amber' },
  PENDING_VERIFY: { label: '待验证', tone: 'orange' },
  CLOSED: { label: '已关闭', tone: 'teal' },
  ON_HOLD: { label: '已挂起', tone: 'rose' },
  CANCELED: { label: '已取消', tone: 'gray' }
}

const priorityMap = {
  LOW: { label: '低', tone: 'gray' },
  MEDIUM: { label: '中', tone: 'blue' },
  HIGH: { label: '高', tone: 'amber' },
  CRITICAL: { label: '紧急', tone: 'rose' }
}

const issueFunctionMap = {
  PAT: { label: 'PAT', tone: 'blue' },
  FAT: { label: 'FAT', tone: 'amber' },
  DESIGN: { label: '设计', tone: 'slate' },
  SAFETY: { label: '安全', tone: 'rose' },
  LOGISTICS: { label: '物流', tone: 'gray' },
  PROCUREMENT: { label: '采购', tone: 'orange' },
  ASSEMBLY: { label: '装配', tone: 'teal' }
}

const projectStatusMap = {
  PLANNING: { label: '规划中', tone: 'gray' },
  IN_PROGRESS: { label: '进行中', tone: 'amber' },
  DELIVERING: { label: '交付中', tone: 'blue' },
  CLOSED: { label: '已关闭', tone: 'teal' },
  CANCELED: { label: '已取消', tone: 'gray' }
}

function getStatusMeta(status) {
  if (status === 'ACCEPTED') {
    return statusMap.IN_PROGRESS
  }
  return statusMap[status] || { label: status || '-', tone: 'gray' }
}

function getPriorityMeta(priority) {
  return priorityMap[priority] || { label: priority || '-', tone: 'gray' }
}

function getProjectStatusMeta(status) {
  return projectStatusMap[status] || { label: status || '-', tone: 'gray' }
}

function getIssueFunctionMeta(issueFunctionCode) {
  return issueFunctionMap[issueFunctionCode] || { label: issueFunctionCode || '-', tone: 'gray' }
}

function getProgressMeta(status, overdue) {
  if (status === 'CLOSED') {
    return { label: '已关闭', tone: 'teal' }
  }
  if (status === 'CANCELED') {
    return { label: '已取消', tone: 'gray' }
  }
  if (status === 'ON_HOLD') {
    return { label: '已挂起', tone: 'rose' }
  }
  if (overdue) {
    return { label: '已超期', tone: 'rose' }
  }
  return { label: '进行中', tone: 'amber' }
}

function formatFileSize(size) {
  if (!size) {
    return '0 B'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function formatDateTime(value) {
  if (!value) {
    return '-'
  }
  const text = String(value).replace('T', ' ')
  return text.length >= 16 ? text.slice(0, 16) : text
}

function toLocalDateTimeString(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hour}:${minute}:${second}`
}

function getAttachmentType(fileType) {
  if (fileType === 'VIDEO') {
    return '视频'
  }
  if (fileType === 'IMAGE') {
    return '图片'
  }
  return '文件'
}

function getNextActionHint(status, hasOwner = true) {
  if (status === 'NEW' && !hasOwner) {
    return '先分派责任人'
  }
  if (status === 'NEW') {
    return '开始处理'
  }
  if (status === 'ACCEPTED') {
    return '提交验证'
  }
  if (status === 'IN_PROGRESS') {
    return '提交验证'
  }
  if (status === 'PENDING_VERIFY') {
    return '关闭问题或退回处理'
  }
  if (status === 'ON_HOLD') {
    return '恢复处理'
  }
  if (status === 'CANCELED') {
    return '已取消'
  }
  return '已结束'
}

module.exports = {
  getStatusMeta,
  getPriorityMeta,
  getProjectStatusMeta,
  getIssueFunctionMeta,
  getProgressMeta,
  formatFileSize,
  formatDateTime,
  toLocalDateTimeString,
  getAttachmentType,
  getNextActionHint
}
