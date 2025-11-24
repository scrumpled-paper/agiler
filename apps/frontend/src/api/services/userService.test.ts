import { describe, it, expect, vi, beforeEach } from 'vitest'
import { userService } from './userService'
import { apiClient } from '../client'

vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    patch: vi.fn(),
  },
}))

describe('userService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getUserInfo', () => {
    it('should fetch user info successfully', async () => {
      const mockUserData = {
        id: 1,
        email: 'user@example.com',
        nickname: 'TestUser',
        imageUrl: 'https://example.com/avatar.jpg',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockUserData })

      const result = await userService.getUserInfo()

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/users')
      expect(result).toEqual(mockUserData)
    })

    it('should handle error when fetching user info fails', async () => {
      const error = new Error('Network error')
      vi.mocked(apiClient.get).mockRejectedValue(error)

      await expect(userService.getUserInfo()).rejects.toThrow('Network error')
      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/users')
    })

    it('should handle 401 Unauthorized error', async () => {
      const error = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' },
        },
      }
      vi.mocked(apiClient.get).mockRejectedValue(error)

      await expect(userService.getUserInfo()).rejects.toEqual(error)
    })

    it('should handle 404 Not Found error', async () => {
      const error = {
        response: {
          status: 404,
          data: { message: 'User not found' },
        },
      }
      vi.mocked(apiClient.get).mockRejectedValue(error)

      await expect(userService.getUserInfo()).rejects.toEqual(error)
    })
  })

  describe('updateUserNickname', () => {
    it('should update user nickname successfully', async () => {
      const newNickname = 'NewNickname'
      const mockResponse = 'NewNickname'

      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      const result = await userService.updateUserNickname(newNickname)

      expect(apiClient.patch).toHaveBeenCalledWith('/api/v1/users', {
        nickname: newNickname,
      })
      expect(result).toBe(mockResponse)
    })

    it('should update nickname with Korean characters', async () => {
      const koreanNickname = '테스트유저'
      vi.mocked(apiClient.patch).mockResolvedValue({ data: koreanNickname })

      const result = await userService.updateUserNickname(koreanNickname)

      expect(apiClient.patch).toHaveBeenCalledWith('/api/v1/users', {
        nickname: koreanNickname,
      })
      expect(result).toBe(koreanNickname)
    })

    it('should update nickname with special characters', async () => {
      const specialNickname = 'User_123!@#'
      vi.mocked(apiClient.patch).mockResolvedValue({ data: specialNickname })

      const result = await userService.updateUserNickname(specialNickname)

      expect(apiClient.patch).toHaveBeenCalledWith('/api/v1/users', {
        nickname: specialNickname,
      })
      expect(result).toBe(specialNickname)
    })

    it('should handle error when updating nickname fails', async () => {
      const error = new Error('Update failed')
      vi.mocked(apiClient.patch).mockRejectedValue(error)

      await expect(
        userService.updateUserNickname('NewNickname')
      ).rejects.toThrow('Update failed')
      expect(apiClient.patch).toHaveBeenCalledWith('/api/v1/users', {
        nickname: 'NewNickname',
      })
    })

    it('should handle 400 Bad Request error (invalid nickname)', async () => {
      const error = {
        response: {
          status: 400,
          data: { message: 'Invalid nickname format' },
        },
      }
      vi.mocked(apiClient.patch).mockRejectedValue(error)

      await expect(
        userService.updateUserNickname('Invalid@@@')
      ).rejects.toEqual(error)
    })

    it('should handle 409 Conflict error (duplicate nickname)', async () => {
      const error = {
        response: {
          status: 409,
          data: { message: 'Nickname already exists' },
        },
      }
      vi.mocked(apiClient.patch).mockRejectedValue(error)

      await expect(
        userService.updateUserNickname('DuplicateNick')
      ).rejects.toEqual(error)
    })

    it('should handle empty string nickname', async () => {
      const emptyNickname = ''
      vi.mocked(apiClient.patch).mockResolvedValue({ data: emptyNickname })

      const result = await userService.updateUserNickname(emptyNickname)

      expect(apiClient.patch).toHaveBeenCalledWith('/api/v1/users', {
        nickname: emptyNickname,
      })
      expect(result).toBe(emptyNickname)
    })
  })
})
