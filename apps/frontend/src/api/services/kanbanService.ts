import type { Issue } from '@/types'
import { apiClient } from '../client'
import { mockIssues } from '@/mocks/mockTasks'

export const kanbanService = {
  getIssues: async (projectUrl: string, date?: string) => {
    //[ ] test 중 네트워크 에러 혼동으로 인해 목데이터로 대신함
    // const response = await apiClient.get(
    //   `/api/v1/projects/${projectUrl}/kanban`,
    //   {
    //     params: date ? { date } : undefined,
    //   }
    // )
    // return response.data
    const response = {
      contents: mockIssues,
      size: mockIssues.length,
    }
    console.log('kanban : ', projectUrl, date)
    return response
  },
  updateIssue: async (projectUrl: string, issueId: string, data: Issue[]) => {
    const response = await apiClient.patch(
      `/api/v1/projects/${projectUrl}/kanban/${issueId}`,
      data
    )
    return response.data
  },
}
