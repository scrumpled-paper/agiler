import type { UserUpdateParams } from '@/types'
import { apiClient } from '../client'

export const userService = {
  apiUrl: '/api/v1/users',
  //내 정보 불러오기
  async getUserInfo() {
    const response = await apiClient.get(this.apiUrl)
    return response.data
  },
  async updateUserNickname(payload: UserUpdateParams): Promise<string> {
    const response = await apiClient.patch(this.apiUrl, payload)
    return response.data
  },

  async updateUserImage(objectKey: string) {
    const imageUrl = `${this.apiUrl}/image`
    await apiClient.patch(imageUrl, { objectKey })
  },

  async deleteUserImage() {
    const imageUrl = `${this.apiUrl}/image`
    await apiClient.delete(imageUrl)
  },
}
