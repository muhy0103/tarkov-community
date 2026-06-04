import request from './request'

export function fetchAdminDashboardSummary() {
  return request.get('/admin/dashboard/summary').then((response) => response?.data)
}
