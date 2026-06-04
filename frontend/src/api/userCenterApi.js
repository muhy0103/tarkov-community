import request from './request'

const pageFallback = (page = 1, size = 6) => ({
  page,
  size,
  total: 0,
  pages: 0,
  records: [],
})

export function fetchUserCenterSummary() {
  return request.get('/users/me/summary').then((response) => response?.data)
}

export function updateMyProfile(data) {
  return request.put('/users/me/profile', data).then((response) => response?.data)
}

export function updateMyPassword(data) {
  return request.put('/users/me/password', data).then((response) => response?.data)
}

export function fetchMyPosts(params = {}) {
  return request
    .get('/users/me/posts', {
      params: {
        page: 1,
        size: 6,
        ...params,
      },
    })
    .then((response) => response?.data ?? pageFallback(params.page, params.size))
}

export function fetchMyComments(params = {}) {
  return request
    .get('/users/me/comments', {
      params: {
        page: 1,
        size: 6,
        ...params,
      },
    })
    .then((response) => response?.data ?? pageFallback(params.page, params.size))
}

export function fetchMyFavorites(params = {}) {
  return request
    .get('/users/me/favorites', {
      params: {
        page: 1,
        size: 6,
        ...params,
      },
    })
    .then((response) => response?.data ?? pageFallback(params.page, params.size))
}
