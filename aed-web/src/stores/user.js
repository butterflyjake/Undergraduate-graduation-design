import { defineStore } from 'pinia'
import { loginApi, getInfoApi } from '@/api/auth'

// 用户状态: token 与用户信息, localStorage 持久化
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  getters: {
    roleCode: state => state.userInfo.roleCode || '',
    realName: state => state.userInfo.realName || state.userInfo.username || ''
  },
  actions: {
    async login(form) {
      const res = await loginApi(form)
      this.token = res.data.token
      this.userInfo = res.data
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    async fetchInfo() {
      const res = await getInfoApi()
      this.userInfo = res.data
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
