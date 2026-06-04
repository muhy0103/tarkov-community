import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    isAdmin: (state) => state.userInfo?.role === 'ADMIN',
  },
  actions: {
    setAuth(token, userInfo) {
      this.token = token
      this.userInfo = userInfo
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    clearAuth() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})
