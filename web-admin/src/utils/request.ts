import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types/api'

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

service.interceptors.request.use((config) => {
  const token = localStorage.getItem('opl_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

binaryService.interceptors.request.use((config) => {
  const token = localStorage.getItem('opl_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const result = response.data as Result<unknown>
    if (result.code === 401) {
      redirectToLogin(result.message || '未认证或认证已过期')
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
    const message = error?.response?.data?.message || '网络异常'
    if (status === 401) {
      redirectToLogin(message || '未认证或认证已过期')
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
