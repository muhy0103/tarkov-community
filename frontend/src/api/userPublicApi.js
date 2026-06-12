import request from './request'

const pageFallback = (page = 1, size = 6) => ({
  page,
  size,
  total: 0,
  pages: 0,
  records: [],
})

export function fetchPublicUserProfile(id) {
  return request.get(`/users/${id}/profile`).then((response) => response?.data)
}

export function fetchPublicUserPosts(id, params = {}) {
  return request
    .get(`/users/${id}/posts`, {
      params: {
        page: 1,
        size: 6,
        ...params,
      },
    })
    .then((response) => response?.data ?? pageFallback(params.page, params.size))
}
