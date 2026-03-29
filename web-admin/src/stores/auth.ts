import { defineStore } from 'pinia'
import { login, type LoginPayload } from '@/api/auth'

interface UserState {
  token: string
  realName: string
  roles: string[]
}

export const useAuthStore = defineStore('auth', {
  state: (): UserState => ({
    token: localStorage.getItem('opl_token') || '',
    realName: localStorage.getItem('opl_real_name') || '',
    roles: JSON.parse(localStorage.getItem('opl_roles') || '[]')
  }),
  actions: {
    async signIn(payload: LoginPayload) {
      const { data } = await login(payload)
      this.token = data.accessToken
      this.realName = data.realName
      this.roles = data.roles
      localStorage.setItem('opl_token', data.accessToken)
      localStorage.setItem('opl_real_name', data.realName)
      localStorage.setItem('opl_roles', JSON.stringify(data.roles))
    },
    signOut() {
      this.token = ''
      this.realName = ''
      this.roles = []
      localStorage.removeItem('opl_token')
      localStorage.removeItem('opl_real_name')
      localStorage.removeItem('opl_roles')
    }
  }
})

