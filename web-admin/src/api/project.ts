import request, { downloadBinary } from '@/utils/request'
import type { PageResult, Result } from '@/types/api'

export interface ProjectItem {
  id: number
  projectNo: string
  projectName: string
  customerName: string
  projectManagerId: number
  projectManagerName: string
  status: string
  startDate?: string
  plannedEndDate?: string
  description?: string
  teamSize?: number
  createdAt?: string
}

export interface ProjectQuery {
  current?: number
  size?: number
  keyword?: string
  status?: string
}

export interface ProjectCreatePayload {
  projectNo: string
  projectName: string
  customerName: string
  projectManagerId: number
  projectManagerName: string
  status: string
  startDate?: string
  plannedEndDate?: string
  description?: string
}

export interface ProjectMemberRoleOption {
  code: string
  name: string
}

export interface ProjectMemberItem {
  id: number
  projectId: number
  userId: number
  username?: string
  realName?: string
  mobile?: string
  departmentCode?: string
  departmentName?: string
  projectRoleCode: string
  projectRoleName: string
  projectManager: boolean
  canAssignIssue: boolean
  canVerifyIssue: boolean
  canCloseIssue: boolean
  sortNo?: number
  remark?: string
}

export interface ProjectMemberSavePayload {
  userId: number
  projectRoleCode: string
  projectManager?: boolean
  canAssignIssue?: boolean
  canVerifyIssue?: boolean
  canCloseIssue?: boolean
  sortNo?: number
  remark?: string
}

export interface ProjectMemberImportItemPayload {
  username: string
  projectRoleCode: string
  projectManager?: boolean
  canAssignIssue?: boolean
  canVerifyIssue?: boolean
  canCloseIssue?: boolean
  sortNo?: number
  remark?: string
}

export interface ProjectMemberImportPayload {
  items: ProjectMemberImportItemPayload[]
}

export interface ProjectMemberImportResult {
  totalCount: number
  successCount: number
  failedCount: number
  failedMessages: string[]
}

export interface ProjectIssueSummary {
  totalIssues: number
  openIssues: number
  closedIssues: number
  highPriorityIssues: number
  unassignedIssues: number
  processingIssues: number
  overdueIssues: number
}

export interface ProjectIssueReportQuery {
  keyword?: string
  ownerId?: number
  issueFunctionCode?: string
  priority?: string
  statusList?: string[]
  overdueOnly?: boolean
}

export interface ProjectIssueImportResult {
  totalCount: number
  addedCount: number
  updatedCount: number
  skippedCount: number
  failedCount: number
  failedMessages: string[]
}

export function fetchProjectPage(params: ProjectQuery) {
  return request.get<unknown, Result<PageResult<ProjectItem>>>('/projects', { params })
}

export function fetchProjectAll() {
  return request.get<unknown, Result<ProjectItem[]>>('/projects/all')
}

export function fetchProjectIssueSummary(projectId: number) {
  return request.get<unknown, Result<ProjectIssueSummary>>(`/projects/${projectId}/issue-summary`)
}

export function exportProjectIssueReport(projectId: number, params?: ProjectIssueReportQuery) {
  return downloadBinary(`/projects/${projectId}/issue-report/export`, { params })
}

export function importProjectIssueReport(projectId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, Result<ProjectIssueImportResult>>(`/projects/${projectId}/issue-report/import`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function createProject(data: ProjectCreatePayload) {
  return request.post<unknown, Result<number>>('/projects', data)
}

export function deleteProject(projectId: number) {
  return request.delete<unknown, Result<null>>(`/projects/${projectId}`)
}

export function fetchProjectMemberRoleOptions() {
  return request.get<unknown, Result<ProjectMemberRoleOption[]>>('/projects/member-role-options')
}

export function fetchProjectMembers(projectId: number) {
  return request.get<unknown, Result<ProjectMemberItem[]>>(`/projects/${projectId}/members`)
}

export function createProjectMember(projectId: number, data: ProjectMemberSavePayload) {
  return request.post<unknown, Result<ProjectMemberItem>>(`/projects/${projectId}/members`, data)
}

export function updateProjectMember(projectId: number, memberId: number, data: ProjectMemberSavePayload) {
  return request.put<unknown, Result<ProjectMemberItem>>(`/projects/${projectId}/members/${memberId}`, data)
}

export function deleteProjectMember(projectId: number, memberId: number) {
  return request.delete<unknown, Result<null>>(`/projects/${projectId}/members/${memberId}`)
}

export function importProjectMembers(projectId: number, data: ProjectMemberImportPayload) {
  return request.post<unknown, Result<ProjectMemberImportResult>>(`/projects/${projectId}/members/import`, data)
}

export function syncProjectMembersFromIssues(projectId: number) {
  return request.post<unknown, Result<ProjectMemberImportResult>>(`/projects/${projectId}/members/sync-from-issues`)
}
