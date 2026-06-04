import request from './request'

export const login = (payload) => request.post('/auth/login', payload).then((response) => response?.data)

export const register = (payload) =>
  request.post('/auth/register', payload).then((response) => response?.data)
