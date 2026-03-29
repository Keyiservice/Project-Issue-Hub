import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface DashboardStats {
  totalIssues: number
  openIssues: number
  highPriorityIssues: number
  overdueIssues: number
  trendDates: string[]
  createdTrend: number[]
  closedTrend: number[]
}

export function fetchDashboardStats(projectId?: number) {
  return request.get<unknown, Result<DashboardStats>>('/stats/dashboard', {
    params: projectId ? { projectId } : undefined
  })
}
