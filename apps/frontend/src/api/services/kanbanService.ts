import type { Issue } from '@/types'
import { apiClient } from '../client'

export const kanbanService = {
  getIssues: async (projectUrl: string) => {
    const response = await apiClient.get(
      `/api/v1/projects/${projectUrl}/kanban`
    )
    return response.data
  },
  updateIssue: async (projectUrl: string, issueId: string, data: Issue[]) => {
    const response = await apiClient.patch(
      `/api/v1/projects/${projectUrl}/kanban/${issueId}`,
      data
    )
    return response.data
  },
}
