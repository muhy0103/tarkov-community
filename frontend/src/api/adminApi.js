import request from './request'

export function fetchAdminDashboardSummary() {
  return request.get('/admin/dashboard/summary').then((response) => response?.data)
}

export function fetchAdminPosts(params = {}) {
  return request
    .get('/admin/posts', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function reviewAdminPost(id, payload) {
  return request.put(`/admin/posts/${id}/review`, payload).then((response) => response?.data)
}

export function fetchAdminUsers(params = {}) {
  return request
    .get('/admin/users', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function updateAdminUser(id, payload) {
  return request.put(`/admin/users/${id}`, payload).then((response) => response?.data)
}
