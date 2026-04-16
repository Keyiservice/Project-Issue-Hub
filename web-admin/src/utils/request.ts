import axios from 'axios'
import type { AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types/api'
import i18n from '@/i18n'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

const binaryService = axios.create({
  baseURL: '/api',
  timeout: 30000,
  responseType: 'blob'
})

function redirectToLogin(message: string) {
  localStorage.removeItem('opl_token')
  localStorage.removeItem('opl_real_name')
  localStorage.removeItem('opl_roles')
  ElMessage.error(message)
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

function attachToken(config: InternalAxiosRequestConfig) {
  const token = localStorage.getItem('opl_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}

service.interceptors.request.use(attachToken)
binaryService.interceptors.request.use(attachToken)

service.interceptors.response.use(
  (response) => {
    const result = response.data as Result<unknown>
    if (result.code === 401) {
      redirectToLogin(result.message || i18n.global.t('common.message.unauthorized').toString())
      return Promise.reject(result)
    }
    if (result.code !== 0) {
      ElMessage.error(result.message)
      return Promise.reject(result)
    }
    return result as any
  },
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || i18n.global.t('common.message.networkError').toString()
    if (status === 401) {
      redirectToLogin(message || i18n.global.t('common.message.unauthorized').toString())
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service

export function downloadBinary(url: string, config?: AxiosRequestConfig) {
  return binaryService.get<Blob>(url, config)
}
