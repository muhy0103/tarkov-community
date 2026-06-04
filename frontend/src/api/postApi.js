import request from './request'

export function fetchPosts(params = {}) {
  return request
    .get('/posts', {
      params: {
        page: 1,
        size: 5,
        ...params,
      },
    })
    .then((response) => response?.data ?? { page: 1, size: 5, total: 0, pages: 0, records: [] })
}
