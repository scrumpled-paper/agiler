import { apiClient } from '../client'

export const userService = {
  apiUrl: '/api/v1/users',
  //내 정보 불러오기
  async getUserInfo() {
    const response = await apiClient.get(this.apiUrl)
    return response.data
  },
  async updateUserNickname(nickname: string): Promise<string> {
    const response = await apiClient.patch(this.apiUrl, {
      nickname,
    })
    return response.data
  },
}
