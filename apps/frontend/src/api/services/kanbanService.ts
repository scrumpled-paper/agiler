import type { Issue } from '@/types/issue'
import { apiClient } from '../client'
// import { mockIssues } from '@/mocks/mockTasks'

export const kanbanService = {
  baseURL: '/api/v1/projects/',

  async updateIssue(projectUrl: string, issueId: string, data: Issue[]) {
    const url = `${this.baseURL}/${projectUrl}/kanban/${issueId}`
    const response = await apiClient.patch(url, data)
    return response.data
  },

  async getIssues(projectUrl: string, date?: string) {
    const url = `${this.baseURL}/${projectUrl}/kanban/`
    const response = await apiClient.get(url, {
      params: date ? { date } : undefined,
    })
    return response.data
  },
}
