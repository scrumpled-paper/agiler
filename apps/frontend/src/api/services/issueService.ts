import type {
  IssuePayload,
  updateIssuePayload,
  IssueDetailResponse,
} from '@/types/issue'
import { apiClient } from '../client'

export const issueService = {
  baseURL: '/api/v1/projects/',

  async createIssue(projectUrl: string | undefined, payload: IssuePayload) {
    if (!projectUrl) {
      throw new Error('프로젝트 url이 유효하지 않습니다.')
    }
    const url = `${this.baseURL}${projectUrl}/issues`
    const response = await apiClient.post(url, payload)
    return response.data
  },

  async getIssueDetail(
    projectUrl: string,
    issueId: number
  ): Promise<IssueDetailResponse> {
    const url = `${this.baseURL}${projectUrl}/issues/${issueId}`
    const response = await apiClient.get<IssueDetailResponse>(url)
    return response.data
  },

  async deleteIssue(projectUrl: string, issueId: number) {
    const url = `${this.baseURL}${projectUrl}/issues`
    const response = await apiClient.delete(url, {
      data: { issueId: issueId },
    })
    return response.data
  },

  async updateIssue(
    projectUrl: string,
    // issueId: number,
    payload: updateIssuePayload
  ) {
    const url = `${this.baseURL}${projectUrl}/issues`
    const response = await apiClient.patch(url, payload)
    return response.data
  },

  async updateIssueLabels(
    projectUrl: string,
    issueId: number,
    payload: number[]
  ) {
    const url = `${this.baseURL}${projectUrl}/issues/${issueId}/labels`
    const response = await apiClient.patch(url, { labels: payload })
    return response.data
  },

  async updateIssueStatus(
    projectUrl: string,
    issueId: number,
    kanbanConfigId: number
  ) {
    const url = `${this.baseURL}${projectUrl}/issues/${issueId}/kanban-config`
    const response = await apiClient.patch(url, {
      kanbanConfigId,
    })
    return response.data
  },

  async updateIssueAssignees(
    projectUrl: string,
    issueId: number,
    assignees: number[]
  ) {
    const url = `${this.baseURL}${projectUrl}/issues/${issueId}/assignees`
    const response = await apiClient.patch(url, {
      assignees,
    })
    return response.data
  },
}
