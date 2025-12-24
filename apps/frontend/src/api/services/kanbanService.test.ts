import { describe, it, expect, vi, beforeEach } from 'vitest'
import { kanbanService } from './kanbanService'
import { apiClient } from '../client'
import type { Issue, IssueResponse } from '@/types/issue'

// Mock apiClient
vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    patch: vi.fn(),
  },
}))

describe('kanbanService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getFilteredIssues', () => {
    it('should fetch issues successfully without date', async () => {
      const mockBackendResponse: IssueResponse[] = [
        {
          issueId: 1,
          title: 'Issue 1',
          kanbanConfigId: 1,
          assignees: [1],
          startedAt: '2025-01-01T00:00:00.000Z',
          dueAt: '2025-01-10T00:00:00.000Z',
          createdAt: '2025-01-01T00:00:00.000Z',
          isDone: false,
          labels: [],
          notis: [],
        },
        {
          issueId: 2,
          title: 'Issue 2',
          kanbanConfigId: 2,
          assignees: [2],
          startedAt: '2025-01-05T00:00:00.000Z',
          dueAt: '2025-01-15T00:00:00.000Z',
          createdAt: '2025-01-05T00:00:00.000Z',
          isDone: false,
          labels: [],
          notis: [],
        },
      ]

      vi.mocked(apiClient.get).mockResolvedValue({
        data: {
          issues: mockBackendResponse,
          kanbanConfigs: [],
          labels: [],
          profiles: [],
        },
      })

      const result = await kanbanService.getFilteredIssues('test-project', {})

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        {
          params: {},
        }
      )
      const issues = result.issues
      expect(issues).toHaveLength(2)
      expect(issues[0].issueId).toBe(1)
      expect(issues[0].title).toBe('Issue 1')
      expect(issues[0].title).toBe('Issue 1')
    })

    it('should fetch issues successfully with date', async () => {
      const mockBackendResponse: IssueResponse[] = [
        {
          issueId: 1,
          title: 'Issue 1',
          kanbanConfigId: 1,
          assignees: [1],
          startedAt: '2025-01-01T00:00:00.000Z',
          dueAt: '2025-01-10T00:00:00.000Z',
          createdAt: '2025-01-01T00:00:00.000Z',
          isDone: false,
          labels: [],
          notis: [],
        },
      ]

      vi.mocked(apiClient.get).mockResolvedValue({
        data: {
          issues: mockBackendResponse,
          kanbanConfigs: [],
          labels: [],
          profiles: [],
        },
      })

      const result = await kanbanService.getFilteredIssues('test-project', {
        date: '2025-01-01',
      })

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        {
          params: { date: '2025-01-01' },
        }
      )
      const issues = result.issues
      expect(issues).toHaveLength(1)
      expect(issues[0].issueId).toBe(1)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Failed to fetch issues')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        kanbanService.getFilteredIssues('test-project', {})
      ).rejects.toThrow('Failed to fetch issues')
    })

    it('should handle empty response', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({
        data: {
          issues: [],
          kanbanConfigs: [],
          labels: [],
          profiles: [],
        },
      })

      const result = await kanbanService.getFilteredIssues('test-project', {})

      expect(result.issues).toEqual([])
    })

    it('should fetch issues with multiple filters', async () => {
      const mockBackendResponse: IssueResponse[] = [
        {
          issueId: 1,
          title: 'Issue 1',
          kanbanConfigId: 1,
          assignees: [1],
          startedAt: '2025-01-01T00:00:00.000Z',
          dueAt: '2025-01-10T00:00:00.000Z',
          createdAt: '2025-01-01T00:00:00.000Z',
          isDone: false,
          labels: [],
          notis: [],
        },
      ]

      vi.mocked(apiClient.get).mockResolvedValue({
        data: {
          issues: mockBackendResponse,
          kanbanConfigs: [],
          labels: [],
          profiles: [],
        },
      })

      const result = await kanbanService.getFilteredIssues('test-project', {
        date: '2025-01-01',
        profiles: 'user1,user2',
        noti: 'user3',
        labels: 'bug,feature',
      })

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        {
          params: {
            date: '2025-01-01',
            profiles: 'user1,user2',
            noti: 'user3',
            labels: 'bug,feature',
          },
        }
      )
      const issues = result.issues
      expect(issues).toHaveLength(1)
      expect(issues[0].issueId).toBe(1)
    })
  })

  describe('updateIssue', () => {
    it('should update issue successfully', async () => {
      const updatedTasks: Issue[] = [
        {
          id: '1',
          name: 'Updated Issue',
          column: '3',
          issueId: '1',
          title: 'Updated Issue',
          kanbanConfigId: 3,
          assignees: [1],
          startedAt: '2025-01-01T00:00:00.000Z',
          dueAt: '2025-01-10T00:00:00.000Z',
          createdAt: '2025-01-01T00:00:00.000Z',
          isDone: false,
          labels: [],
          notis: [],
        },
      ]

      vi.mocked(apiClient.patch).mockResolvedValue({ data: updatedTasks })

      const result = await kanbanService.updateIssue(
        'test-project',
        '1',
        updatedTasks
      )

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/kanban/1',
        updatedTasks
      )
      expect(result).toEqual(updatedTasks)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Failed to update issue')

      vi.mocked(apiClient.patch).mockRejectedValue(axiosError)

      await expect(
        kanbanService.updateIssue('test-project', '1', [])
      ).rejects.toThrow('Failed to update issue')
    })

    it('should update multiple issues', async () => {
      const updatedTasks: Issue[] = [
        {
          id: '1',
          name: 'Issue 1',
          column: '3',
          issueId: '1',
          title: 'Issue 1',
          kanbanConfigId: 3,
          assignees: [1],
          startedAt: '2025-01-01T00:00:00.000Z',
          dueAt: '2025-01-10T00:00:00.000Z',
          createdAt: '2025-01-01T00:00:00.000Z',
          isDone: false,
          labels: [],
          notis: [],
        },
        {
          id: '2',
          name: 'Issue 2',
          column: '3',
          issueId: '2',
          title: 'Issue 2',
          kanbanConfigId: 3,
          assignees: [2],
          startedAt: '2025-01-05T00:00:00.000Z',
          dueAt: '2025-01-15T00:00:00.000Z',
          createdAt: '2025-01-05T00:00:00.000Z',
          isDone: false,
          labels: [],
          notis: [],
        },
      ]

      vi.mocked(apiClient.patch).mockResolvedValue({ data: updatedTasks })

      const result = await kanbanService.updateIssue(
        'test-project',
        '1',
        updatedTasks
      )

      expect(result).toEqual(updatedTasks)
    })
  })
})
