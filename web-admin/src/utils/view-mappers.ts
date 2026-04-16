import i18n from '@/i18n'

type TagType = 'success' | 'warning' | 'info' | 'danger' | 'primary'

interface ViewMeta {
  label: string
  tagType: TagType
  tone: string
}

const issueStatusToneMap: Record<string, Omit<ViewMeta, 'label'>> = {
  NEW: { tagType: 'info', tone: 'slate' },
  IN_PROGRESS: { tagType: 'warning', tone: 'amber' },
  PENDING_VERIFY: { tagType: 'warning', tone: 'orange' },
  CLOSED: { tagType: 'success', tone: 'teal' },
  ON_HOLD: { tagType: 'danger', tone: 'rose' },
  CANCELED: { tagType: 'info', tone: 'gray' }
}

const issuePriorityToneMap: Record<string, Omit<ViewMeta, 'label'>> = {
  LOW: { tagType: 'info', tone: 'gray' },
  MEDIUM: { tagType: 'primary', tone: 'blue' },
  HIGH: { tagType: 'warning', tone: 'amber' },
  CRITICAL: { tagType: 'danger', tone: 'rose' }
}

const issueImpactToneMap: Record<string, Omit<ViewMeta, 'label'>> = {
  LOW: { tagType: 'info', tone: 'gray' },
  MEDIUM: { tagType: 'primary', tone: 'blue' },
  HIGH: { tagType: 'warning', tone: 'amber' },
  CRITICAL: { tagType: 'danger', tone: 'rose' }
}

const issueFunctionToneMap: Record<string, Omit<ViewMeta, 'label'>> = {
  PAT: { tagType: 'primary', tone: 'blue' },
  FAT: { tagType: 'warning', tone: 'amber' },
  DESIGN: { tagType: 'info', tone: 'slate' },
  SAFETY: { tagType: 'danger', tone: 'rose' },
  LOGISTICS: { tagType: 'info', tone: 'gray' },
  PROCUREMENT: { tagType: 'warning', tone: 'orange' },
  ASSEMBLY: { tagType: 'success', tone: 'teal' }
}

const projectStatusToneMap: Record<string, Omit<ViewMeta, 'label'>> = {
  PLANNING: { tagType: 'info', tone: 'slate' },
  IN_PROGRESS: { tagType: 'warning', tone: 'amber' },
  DELIVERING: { tagType: 'primary', tone: 'blue' },
  CLOSED: { tagType: 'success', tone: 'teal' },
  CANCELED: { tagType: 'info', tone: 'gray' }
}

const issueStatusOrder = ['NEW', 'IN_PROGRESS', 'PENDING_VERIFY', 'CLOSED', 'ON_HOLD', 'CANCELED']
const issuePriorityOrder = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const issueFunctionOrder = ['PAT', 'FAT', 'DESIGN', 'SAFETY', 'LOGISTICS', 'PROCUREMENT', 'ASSEMBLY']
const projectStatusOrder = ['PLANNING', 'IN_PROGRESS', 'DELIVERING', 'CLOSED', 'CANCELED']

function t(key: string, fallback: string) {
  const translated = i18n.global.t(key)
  return typeof translated === 'string' && translated !== key ? translated : fallback
}

function fallbackMeta(value?: string): ViewMeta {
  return {
    label: value || '-',
    tagType: 'info',
    tone: 'gray'
  }
}

export function getIssueStatusMeta(status?: string) {
  const normalized = status === 'ACCEPTED' ? 'IN_PROGRESS' : status || ''
  const tone = issueStatusToneMap[normalized]
  if (!tone) {
    return fallbackMeta(status)
  }
  return {
    ...tone,
    label: t(`enums.issueStatus.${normalized}`, normalized)
  }
}

export function getIssuePriorityMeta(priority?: string) {
  const tone = issuePriorityToneMap[priority || '']
  if (!tone) {
    return fallbackMeta(priority)
  }
  return {
    ...tone,
    label: t(`enums.priority.${priority}`, priority || '-')
  }
}

export function getIssueImpactMeta(impact?: string) {
  const tone = issueImpactToneMap[impact || '']
  if (!tone) {
    return fallbackMeta(impact)
  }
  return {
    ...tone,
    label: t(`enums.impact.${impact}`, impact || '-')
  }
}

export function getIssueFunctionMeta(issueFunctionCode?: string) {
  const tone = issueFunctionToneMap[issueFunctionCode || '']
  if (!tone) {
    return fallbackMeta(issueFunctionCode)
  }
  return {
    ...tone,
    label: t(`enums.issueFunction.${issueFunctionCode}`, issueFunctionCode || '-')
  }
}

export function getProjectStatusMeta(status?: string) {
  const tone = projectStatusToneMap[status || '']
  if (!tone) {
    return fallbackMeta(status)
  }
  return {
    ...tone,
    label: t(`enums.projectStatus.${status}`, status || '-')
  }
}

export function getIssueStatusOptions() {
  return issueStatusOrder.map((value) => ({
    value,
    label: getIssueStatusMeta(value).label
  }))
}

export function getIssuePriorityOptions() {
  return issuePriorityOrder.map((value) => ({
    value,
    label: getIssuePriorityMeta(value).label
  }))
}

export function getIssueFunctionOptions() {
  return issueFunctionOrder.map((value) => ({
    value,
    label: getIssueFunctionMeta(value).label
  }))
}

export function getProjectStatusOptions() {
  return projectStatusOrder.map((value) => ({
    value,
    label: getProjectStatusMeta(value).label
  }))
}

export function getRoleLabel(roleCode?: string) {
  if (!roleCode) {
    return '-'
  }
  return t(`roles.${roleCode}`, roleCode)
}
