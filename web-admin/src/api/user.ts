import request from '@/utils/request'
import type { PageResult, Result } from '@/types/api'

export interface UserOption {
  id: number
  username: string
  realName: string
  departmentCode?: string
  departmentName?: string
}

export interface RoleOption {
  id: number
  roleCode: string
  roleName: string
}

export interface UserListQuery {
  current: number
  size: number
  keyword?: string
  status?: string
  departmentCode?: string
}

export interface UserListItem {
  id: number
  userNo: string
  username: string
  realName: string
  mobile?: string
  email?: string
  departmentCode?: string
  departmentName?: string
  status: string
  wechatBound: boolean
  wechatOpenid?: string
  wechatBoundAt?: string
  passwordChangeRequired: boolean
  passwordChangedAt?: string
  lastLoginAt?: string
  createdAt?: string
  projectCount?: number
  roleCodes: string[]
  roleNames: string[]
}

export interface UserCreatePayload {
  username: string
  realName: string
  mobile?: string
  email?: string
  departmentCode?: string
  departmentName?: string
  roleCodes: string[]
  initialPassword?: string
}

export interface UserImportItemPayload {
  username: string
  realName: string
  departmentCode?: string
  departmentName?: string
  roleCodes?: string[]
  mobile?: string
  email?: string
}

export interface UserImportPayload {
  items: UserImportItemPayload[]
  initialPassword?: string
}

export interface UserImportResult {
  totalCount: number
  successCount: number
  failedCount: number
  failedMessages: string[]
}

export interface UserProjectParticipationItem {
  projectId: number
  projectNo: string
  projectName: string
  customerName?: string
  projectStatus: string
  projectManagerName?: string
  projectRoleCode: string
  projectRoleName: string
  projectManager: boolean
  canAssignIssue: boolean
  canVerifyIssue: boolean
  canCloseIssue: boolean
  remark?: string
}

export function fetchUserOptions() {
  return request.get<unknown, Result<UserOption[]>>('/users/options')
}

export function fetchUserPage(params: UserListQuery) {
  return request.get<unknown, Result<PageResult<UserListItem>>>('/users', { params })
}

export function fetchRoleOptions() {
  return request.get<unknown, Result<RoleOption[]>>('/users/roles')
}

export function createUser(data: UserCreatePayload) {
  return request.post<unknown, Result<UserListItem>>('/users', data)
}

export function updateUser(userId: number, data: UserCreatePayload) {
  return request.put<unknown, Result<UserListItem>>(`/users/${userId}`, data)
}

export function importUsers(data: UserImportPayload) {
  return request.post<unknown, Result<UserImportResult>>('/users/import', data)
}

export function fetchUserProjects(userId: number) {
  return request.get<unknown, Result<UserProjectParticipationItem[]>>(`/users/${userId}/projects`)
}

export function resetUserPassword(userId: number) {
  return request.put<unknown, Result<null>>(`/users/${userId}/reset-password`)
}

export function unbindUserWechat(userId: number) {
  return request.put<unknown, Result<null>>(`/users/${userId}/unbind-wechat`)
}
