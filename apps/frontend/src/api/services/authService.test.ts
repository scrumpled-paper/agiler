import { describe, it, expect, vi, beforeEach } from 'vitest'
import { authService, type User } from './authService'
import { apiClient } from '../client'

// apiClient를 mock 처리
vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('authService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('getCurrentUser', () => {
    it('should fetch current user info successfully', async () => {
      const mockUser: User = {
        id: 1,
        email: 'test@agiler.com',
        nickname: 'AgileTester',
        imageUrl: 'https://via.placeholder.com/150',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockUser })

      const user = await authService.getCurrentUser()

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/users/')
      expect(user).toEqual(mockUser)
      expect(user.id).toBe(1)
      expect(user.email).toBe('test@agiler.com')
      expect(user.nickname).toBe('AgileTester')
    })

    it('should handle missing imageUrl property', async () => {
      const mockUser: User = {
        id: 2,
        email: 'noimage@agiler.com',
        nickname: 'NoImageUser',
        imageUrl: undefined,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockUser })

      const user = await authService.getCurrentUser()

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/users/')
      expect(user).toEqual(mockUser)
      expect(user.imageUrl).toBeUndefined()
    })

    it('should throw error when API fails', async () => {
      const error = new Error('Network Error')
      vi.mocked(apiClient.get).mockRejectedValue(error)

      await expect(authService.getCurrentUser()).rejects.toThrow(
        'Network Error'
      )
      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/users/')
    })
  })

  describe('logout', () => {
    it('should remove mockUser from localStorage in DEV environment', async () => {
      // DEV 환경은 기본값이므로 별도 설정 불필요
      localStorage.setItem('mockUser', JSON.stringify({ id: 1 }))
      expect(localStorage.getItem('mockUser')).toBeTruthy()

      await authService.logout()

      // localStorage에서 제거되었는지 확인
      expect(localStorage.getItem('mockUser')).toBeNull()
      // API 호출되지 않았는지 확인
      expect(apiClient.post).not.toHaveBeenCalled()
    })

    // 여기서는 DEV 환경에서의 동작만 unit test로 검증합니다.
  })
})
