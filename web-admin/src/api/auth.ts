import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResponse {
  userId: number
  username: string
  realName: string
  departmentCode?: string
  departmentName?: string
  roles: string[]
  accessToken: string
  tokenType: string
  expireInSeconds: number
}

export function login(data: LoginPayload) {
  return request.post<unknown, Result<LoginResponse>>('/auth/login', data)
}
