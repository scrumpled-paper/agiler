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
    // 1. 개발 환경 확인
    if (import.meta.env.DEV) {
      const mockUserString = localStorage.getItem('mockUser')

      // 2. 로컬 스토리지에 'mockUser' 키가 있는지 확인
      if (mockUserString) {
        try {
          // 3. Mock User 정보 파싱 후 반환 (null/undefined 체크를 위해 타입 단언 사용)
          return JSON.parse(mockUserString) as User
        } catch (e) {
          console.error('Failed to parse mockUser from localStorage', e)
          // 파싱 실패 시, 무시하고 실제 API 요청으로 넘어감
        }
      }
    }

    // 4. Mock User가 없거나 개발 환경이 아닌 경우 실제 API 요청
    const response = await apiClient.get<User>('/api/v1/users/')
    return response.data
  },
}
