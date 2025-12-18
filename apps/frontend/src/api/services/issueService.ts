import type { updateIssuePayload } from '@/types/issue'
import { apiClient } from '../client'
import type { Label } from '../../types/label'
import type { UserInfo } from '@/types'

export const kanbanService = {
  baseURL: '/api/v1/projects/',

  async createIssue(projectUrl: string) {
    const url = `${this.baseURL}${projectUrl}/issue`
    const response = await apiClient.post(url)
    return response.data
  },

  async deleteIssue(projectUrl: string, issueId: number) {
    const url = `${this.baseURL}${projectUrl}/issue`
    const response = await apiClient.delete(url, {
      data: issueId,
    })
    return response.data
  },

  async updateIssue(
    projectUrl: string,
    // issueId: number,
    payload: updateIssuePayload
  ) {
    const url = `${this.baseURL}${projectUrl}/issue`
    const response = await apiClient.patch(url, {
      payload,
    })
    return response.data
  },

  async updateIssueLabels(
    projectUrl: string,
    issueId: number,
    payload: Label[]
  ) {
    const url = `${this.baseURL}${projectUrl}/issue/${issueId}/labels`
    const response = await apiClient.patch(url, {
      payload,
    })
    return response.data
  },

  async updateIssueStatus(
    projectUrl: string,
    issueId: number,
    kanbanConfigId: number
  ) {
    const url = `${this.baseURL}${projectUrl}/issue/${issueId}/kanban-config`
    const response = await apiClient.patch(url, {
      kanbanConfigId,
    })
    return response.data
  },

  async updateIssueAssignees(
    projectUrl: string,
    issueId: number,
    assignees: UserInfo[]
  ) {
    const url = `${this.baseURL}${projectUrl}/issue/${issueId}/assignees`
    const response = await apiClient.patch(url, {
      assignees,
    })
    return response.data
  },
}
