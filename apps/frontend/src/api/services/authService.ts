import { apiClient } from '../client'

export interface User {
  id: number
  email: string
  nickname: string
  imageUrl?: string
}

export const authService = {
  /**
   * 로그아웃 - 서버의 accessToken 쿠키 삭제
   */
  logout: async (): Promise<void> => {
    if (import.meta.env.DEV) {
      localStorage.removeItem('mockUser')
      return
    }
    await apiClient.post('/api/v1/logout')
  },

  /**
   * 현재 로그인한 사용자 정보 조회
   */
  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>('/api/v1/users')
    return response.data
  },
}
