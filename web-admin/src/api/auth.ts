import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface LoginPayload {
  username: string
  password: string
  mfaCode?: string
}

export interface LoginResponse {
  userId: number
  username: string
  realName: string
  departmentCode?: string
  departmentName?: string
  roles: string[]
  accessToken?: string
  tokenType?: string
  expireInSeconds?: number
  passwordChangeRequired?: boolean
  mfaRequired?: boolean
  mfaSetupRequired?: boolean
  mfaToken?: string
}

export function login(data: LoginPayload) {
  return request.post<unknown, Result<LoginResponse>>('/auth/login', data)
}

export interface ChangePasswordPayload {
  oldPassword?: string
  newPassword: string
  confirmPassword: string
}

export function changePassword(data: ChangePasswordPayload) {
  return request.post<unknown, Result<null>>('/auth/change-password', data)
}

export interface MfaSetupPayload {
  mfaToken?: string
}

export interface MfaSetupResponse {
  issuer: string
  account: string
  secret: string
  otpauthUrl: string
}

export function setupMfa(data: MfaSetupPayload) {
  return request.post<unknown, Result<MfaSetupResponse>>('/auth/mfa/setup', data)
}

export interface MfaVerifyPayload {
  mfaToken?: string
  code: string
}

export function verifyMfa(data: MfaVerifyPayload) {
  return request.post<unknown, Result<LoginResponse>>('/auth/mfa/verify', data)
}
