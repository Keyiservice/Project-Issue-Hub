type TagType = 'success' | 'warning' | 'info' | 'danger' | 'primary'

interface ViewMeta {
  label: string
  tagType: TagType
  tone: string
}

const issueStatusMetaMap: Record<string, ViewMeta> = {
  NEW: { label: '新建', tagType: 'info', tone: 'slate' },
  IN_PROGRESS: { label: '处理中', tagType: 'warning', tone: 'amber' },
  PENDING_VERIFY: { label: '待验证', tagType: 'warning', tone: 'orange' },
  CLOSED: { label: '已关闭', tagType: 'success', tone: 'teal' },
  ON_HOLD: { label: '已挂起', tagType: 'danger', tone: 'rose' },
  CANCELED: { label: '已取消', tagType: 'info', tone: 'gray' }
}

const issuePriorityMetaMap: Record<string, ViewMeta> = {
  LOW: { label: '低', tagType: 'info', tone: 'gray' },
  MEDIUM: { label: '中', tagType: 'primary', tone: 'blue' },
  HIGH: { label: '高', tagType: 'warning', tone: 'amber' },
  CRITICAL: { label: '紧急', tagType: 'danger', tone: 'rose' }
}

const issueImpactMetaMap: Record<string, ViewMeta> = {
  LOW: { label: '低影响', tagType: 'info', tone: 'gray' },
  MEDIUM: { label: '中影响', tagType: 'primary', tone: 'blue' },
  HIGH: { label: '高影响', tagType: 'warning', tone: 'amber' },
  CRITICAL: { label: '致命影响', tagType: 'danger', tone: 'rose' }
}

const projectStatusMetaMap: Record<string, ViewMeta> = {
  PLANNING: { label: '规划中', tagType: 'info', tone: 'slate' },
  IN_PROGRESS: { label: '进行中', tagType: 'warning', tone: 'amber' },
  DELIVERING: { label: '交付中', tagType: 'primary', tone: 'blue' },
  CLOSED: { label: '已关闭', tagType: 'success', tone: 'teal' },
  CANCELED: { label: '已取消', tagType: 'info', tone: 'gray' }
}

function fallbackMeta(value?: string): ViewMeta {
  return {
    label: value || '-',
    tagType: 'info',
    tone: 'gray'
  }
}

export function getIssueStatusMeta(status?: string) {
  if (status === 'ACCEPTED') {
    return issueStatusMetaMap.IN_PROGRESS
  }
  return issueStatusMetaMap[status || ''] || fallbackMeta(status)
}

export function getIssuePriorityMeta(priority?: string) {
  return issuePriorityMetaMap[priority || ''] || fallbackMeta(priority)
}

export function getIssueImpactMeta(impact?: string) {
  return issueImpactMetaMap[impact || ''] || fallbackMeta(impact)
}

export function getProjectStatusMeta(status?: string) {
  return projectStatusMetaMap[status || ''] || fallbackMeta(status)
}

export const issueStatusOptions = Object.entries(issueStatusMetaMap).map(([value, meta]) => ({
  value,
  label: meta.label
}))

export const issuePriorityOptions = Object.entries(issuePriorityMetaMap).map(([value, meta]) => ({
  value,
  label: meta.label
}))

export const projectStatusOptions = Object.entries(projectStatusMetaMap).map(([value, meta]) => ({
  value,
  label: meta.label
}))
