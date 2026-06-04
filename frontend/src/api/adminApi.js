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

export function fetchAdminComments(params = {}) {
  return request
    .get('/admin/comments', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function reviewAdminComment(id, payload) {
  return request.put(`/admin/comments/${id}/review`, payload).then((response) => response?.data)
}

export function fetchAdminCategories(params = {}) {
  return request
    .get('/admin/categories', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function updateAdminCategory(id, payload) {
  return request.put(`/admin/categories/${id}`, payload).then((response) => response?.data)
}

export function fetchAdminMaps(params = {}) {
  return request
    .get('/admin/maps', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function updateAdminMap(id, payload) {
  return request.put(`/admin/maps/${id}`, payload).then((response) => response?.data)
}

export function fetchAdminTraders(params = {}) {
  return request
    .get('/admin/traders', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function updateAdminTrader(id, payload) {
  return request.put(`/admin/traders/${id}`, payload).then((response) => response?.data)
}

export function fetchAdminQuests(params = {}) {
  return request
    .get('/admin/quests', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function updateAdminQuest(id, payload) {
  return request.put(`/admin/quests/${id}`, payload).then((response) => response?.data)
}

export function fetchAdminItems(params = {}) {
  return request
    .get('/admin/items', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function updateAdminItem(id, payload) {
  return request.put(`/admin/items/${id}`, payload).then((response) => response?.data)
}

export function fetchAdminWeapons(params = {}) {
  return request
    .get('/admin/weapons', {
      params: {
        page: 1,
        size: 10,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 10, total: 0, pages: 0, records: [] })
}

export function updateAdminWeapon(id, payload) {
  return request.put(`/admin/weapons/${id}`, payload).then((response) => response?.data)
}
