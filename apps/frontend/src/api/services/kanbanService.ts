import type { GetFilteredIssuesResponse, Issue } from '@/types/issue'
import { apiClient } from '../client'

export const kanbanService = {
  baseURL: '/api/v1/projects/',

  async updateIssue(projectUrl: string, issueId: string, data: Issue[]) {
    const url = `${this.baseURL}${projectUrl}/kanban/${issueId}`
    const response = await apiClient.patch(url, data)
    return response.data
  },

  async getFilteredIssues(
    projectUrl: string,
    params: {
      date?: string
      profiles?: string
      noti?: string
      labels?: string
    }
  ): Promise<GetFilteredIssuesResponse> {
    const url = `${this.baseURL}${projectUrl}/issues`

    const response = await apiClient.get(url, {
      params: params,
    })

    // 백엔드 응답에서 issues 배열만 추출하여 프론트엔드 Issue 타입으로 변환
    // return toIssues(response.data.issues)
    return response.data
  },
}
