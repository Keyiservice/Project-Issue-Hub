import request, { downloadBinary } from '@/utils/request'
import type { PageResult, Result, IssueItem } from '@/types/api'

export interface IssueQuery {
  current?: number
  size?: number
  keyword?: string
  projectId?: number
  ownerId?: number
  reporterId?: number
  issueFunctionCode?: string
  priority?: string
  statusList?: string[]
  overdueOnly?: boolean
  currentUserRelated?: boolean
  sortByDueAt?: boolean
}

export interface IssueDetail extends IssueItem {
  createdAt?: string
  updatedAt?: string
  description?: string
  actionPlan?: string
  deviceName?: string
  moduleName?: string
  issueFunctionCode?: string
  impactLevel: string
  affectShipment: boolean
  affectCommissioning: boolean
  needShutdown: boolean
  reporterName: string
  ownerDepartmentName?: string
  occurredAt?: string
  closedAt?: string
  closedById?: number
  closedByName?: string
  holdReason?: string
  closeReason?: string
  closeEvidence?: string
  attachments: IssueAttachmentItem[]
  comments: IssueCommentItem[]
}

export interface IssueAssignPayload {
  ownerId: number
  remark?: string
}

export interface IssuePriorityPayload {
  priority: string
  remark?: string
}

export interface IssueFunctionPayload {
  issueFunctionCode: string
  remark?: string
}

export interface IssueAttachmentItem {
  id: number
  commentId?: number
  fileName: string
  objectKey: string
  fileUrl: string
  previewUrl?: string
  fileType: string
  contentType?: string
  fileSize: number
}

export interface IssueCommentItem {
  id: number
  commentType: string
  content: string
  statusSnapshot?: string
  operatorId: number
  operatorName: string
  createdAt: string
  attachments: IssueAttachmentItem[]
}

export interface IssueStatusChangePayload {
  targetStatus: string
  remark?: string
  holdReason?: string
  closeReason?: string
  closeEvidence?: string
  solutionDesc?: string
}

export interface IssueCommentPayload {
  content: string
  attachments?: Array<{
    fileName: string
    objectKey: string
    fileUrl: string
    fileSize: number
    contentType?: string
  }>
}

export interface IssueAttachmentPayload {
  fileName: string
  objectKey: string
  fileUrl: string
  fileSize: number
  contentType?: string
}

export interface IssueAttachmentAppendPayload {
  attachments: IssueAttachmentPayload[]
  remark?: string
}

export interface IssueCreatePayload {
  title: string
  description?: string
  projectId: number
  projectName: string
  deviceName?: string
  moduleName?: string
  issueFunctionCode: string
  categoryCode: string
  sourceCode: string
  priority: string
  impactLevel: string
  affectShipment?: boolean
  affectCommissioning?: boolean
  needShutdown?: boolean
  ownerId?: number
  occurredAt?: string
  dueAt?: string
  attachments?: IssueAttachmentPayload[]
}

export interface FileUploadResult {
  fileName: string
  objectKey: string
  fileUrl: string
  fileSize: number
  contentType?: string
}

export function fetchIssuePage(params: IssueQuery) {
  return request.get<unknown, Result<PageResult<IssueItem>>>('/issues', { params })
}

export function createIssue(data: IssueCreatePayload) {
  return request.post<unknown, Result<number>>('/issues', data)
}

export function fetchIssueDetail(id: number) {
  return request.get<unknown, Result<IssueDetail>>(`/issues/${id}`)
}

export function changeIssueStatus(id: number, data: IssueStatusChangePayload) {
  return request.put<unknown, Result<void>>(`/issues/${id}/status`, data)
}

export function createIssueComment(id: number, data: IssueCommentPayload) {
  return request.post<unknown, Result<number>>(`/issues/${id}/comments`, data)
}

export function appendIssueAttachments(id: number, data: IssueAttachmentAppendPayload) {
  return request.post<unknown, Result<void>>(`/issues/${id}/attachments`, data)
}

export function deleteIssueAttachment(issueId: number, attachmentId: number) {
  return request.delete<unknown, Result<void>>(`/issues/${issueId}/attachments/${attachmentId}`)
}

export function assignIssue(id: number, data: IssueAssignPayload) {
  return request.put<unknown, Result<void>>(`/issues/${id}/assign`, data)
}

export function updateIssuePriority(id: number, data: IssuePriorityPayload) {
  return request.put<unknown, Result<void>>(`/issues/${id}/priority`, data)
}

export function updateIssueFunction(id: number, data: IssueFunctionPayload) {
  return request.put<unknown, Result<void>>(`/issues/${id}/function`, data)
}

export function deleteIssue(id: number) {
  return request.delete<unknown, Result<void>>(`/issues/${id}`)
}

export function uploadIssueAttachment(file: File, bizFolder = 'issue') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizFolder', bizFolder)
  return request.post<unknown, Result<FileUploadResult>>('/attachments/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export async function fetchAttachmentBlob(id: number) {
  const response = await downloadBinary(`/attachments/${id}/content`)
  return response.data
}
