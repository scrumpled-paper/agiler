import { apiClient } from '../client'

export const getUserInfo = async () => {
  const response = await apiClient.get('api/v1/users')
  return response.data
}

export const updateUserNickname = async (nickname: string): Promise<string> => {
  const response = await apiClient.patch('/api/v1/users', {
    nickname,
  })
  return response.data
}
