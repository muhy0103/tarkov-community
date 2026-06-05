import request from './request'

const unwrapData = (response) => response?.data

export function fetchPosts(params = {}) {
  return request
    .get('/posts', {
      params: {
        page: 1,
        size: 5,
        ...params,
      },
    })
    .then((response) => unwrapData(response) ?? { page: 1, size: 5, total: 0, pages: 0, records: [] })
}

export function fetchPostDetail(id) {
  return request.get(`/posts/${id}`).then(unwrapData)
}

export function createPost(payload) {
  return request.post('/posts', payload).then(unwrapData)
}

export function fetchPostComments(id, params = {}) {
  return request
    .get(`/posts/${id}/comments`, {
      params: {
        page: 1,
        size: 20,
        ...params,
      },
    })
    .then((response) => unwrapData(response) ?? { page: 1, size: 20, total: 0, pages: 0, records: [] })
}

export function createPostComment(id, payload) {
  return request.post(`/posts/${id}/comments`, payload).then(unwrapData)
}

export function togglePostLike(id) {
  return request.post(`/posts/${id}/likes/toggle`).then(unwrapData)
}

export function togglePostFavorite(id) {
  return request.post(`/posts/${id}/favorites/toggle`).then(unwrapData)
}

export function createReport(payload) {
  return request.post('/reports', payload).then(unwrapData)
}
