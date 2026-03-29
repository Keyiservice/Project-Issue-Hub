export interface Result<T> {
  code: number
  message: string
  data: T
  traceId: string
  timestamp: string
}

export interface PageResult<T> {
  current: number
  size: number
  total: number
  records: T[]
}

export interface IssueItem {
  id: number
  issueNo: string
  oplNo?: string
  title: string
  projectId?: number
  projectName: string
  priority: string
  status: string
  pilotName?: string
  legacyStatus?: string
  reporterName?: string
  ownerId?: number
  ownerName?: string
  dueAt?: string
  overdue: boolean
}
