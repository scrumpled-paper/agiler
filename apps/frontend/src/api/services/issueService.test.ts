import { describe, it, expect, vi, beforeEach } from 'vitest'
import { issueService } from './issueService'
import { apiClient } from '../client'
import type { IssuePayload, updateIssuePayload } from '@/types/issue'
import type { Label } from '@/types/label'
import type { UserInfo } from '@/types'

// apiClient 모킹
vi.mock('../client', () => ({
  apiClient: {
    post: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
  },
}))

describe('issueService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createIssue', () => {
    const mockPayload: IssuePayload = {
      title: 'New Issue',
      contents: 'Issue description',
      startedAt: '2025-01-01T00:00:00.000Z',
      dueAt: '2025-01-10T00:00:00.000Z',
      assignees: [1, 2],
      labels: [1],
    }

    it('should create issue successfully', async () => {
      const mockResponse = {
        issueId: 1,
        title: 'New Issue',
        kanbanConfigId: 1,
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse })

      const result = await issueService.createIssue('test-project', mockPayload)

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        mockPayload
      )
      expect(result).toEqual(mockResponse)
    })

    it('should throw error when projectUrl is undefined', async () => {
      await expect(
        issueService.createIssue(undefined, mockPayload)
      ).rejects.toThrow('프로젝트 url이 유효하지 않습니다.')

      expect(apiClient.post).not.toHaveBeenCalled()
    })

    it('should throw error when projectUrl is empty string', async () => {
      await expect(issueService.createIssue('', mockPayload)).rejects.toThrow(
        '프로젝트 url이 유효하지 않습니다.'
      )

      expect(apiClient.post).not.toHaveBeenCalled()
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to create issue')
      vi.mocked(apiClient.post).mockRejectedValue(apiError)

      await expect(
        issueService.createIssue('test-project', mockPayload)
      ).rejects.toThrow('Failed to create issue')
    })

    it('should create issue with minimal payload', async () => {
      const minimalPayload: IssuePayload = {
        title: 'Minimal Issue',
        contents: 'Description',
        startedAt: '2025-01-01T00:00:00.000Z',
        dueAt: '2025-01-10T00:00:00.000Z',
      }

      const mockResponse = { issueId: 2, title: 'Minimal Issue' }
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse })

      const result = await issueService.createIssue(
        'test-project',
        minimalPayload
      )

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        minimalPayload
      )
      expect(result).toEqual(mockResponse)
    })
  })

  describe('deleteIssue', () => {
    it('should delete issue successfully', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.delete).mockResolvedValue({ data: mockResponse })

      const result = await issueService.deleteIssue('test-project', 1)

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        {
          data: 1,
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to delete issue')
      vi.mocked(apiClient.delete).mockRejectedValue(apiError)

      await expect(issueService.deleteIssue('test-project', 1)).rejects.toThrow(
        'Failed to delete issue'
      )
    })

    it('should delete issue with different issueId', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.delete).mockResolvedValue({ data: mockResponse })

      await issueService.deleteIssue('another-project', 999)

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/another-project/issues',
        {
          data: 999,
        }
      )
    })
  })

  describe('updateIssue', () => {
    const mockPayload: updateIssuePayload = {
      issueId: 1,
      title: 'Updated Issue',
      contents: 'Updated description',
      startedAt: '2025-01-01T00:00:00.000Z',
      dueAt: '2025-01-15T00:00:00.000Z',
    }

    it('should update issue successfully', async () => {
      const mockResponse = {
        issueId: 1,
        title: 'Updated Issue',
        kanbanConfigId: 2,
      }

      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      const result = await issueService.updateIssue('test-project', mockPayload)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        {
          payload: mockPayload,
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to update issue')
      vi.mocked(apiClient.patch).mockRejectedValue(apiError)

      await expect(
        issueService.updateIssue('test-project', mockPayload)
      ).rejects.toThrow('Failed to update issue')
    })

    it('should update issue with different payload', async () => {
      const differentPayload: updateIssuePayload = {
        issueId: 2,
        title: 'Another Issue',
        contents: 'Another description',
        startedAt: '2025-02-01T00:00:00.000Z',
        dueAt: '2025-02-28T00:00:00.000Z',
      }

      const mockResponse = { issueId: 2, title: 'Another Issue' }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssue('test-project', differentPayload)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues',
        {
          payload: differentPayload,
        }
      )
    })
  })

  describe('updateIssueLabels', () => {
    const mockLabels: Label[] = [
      {
        labelId: 1,
        name: 'Bug',
        description: 'Bug label',
        color: '#ff0000',
      },
      {
        labelId: 2,
        name: 'Feature',
        description: 'Feature label',
        color: '#00ff00',
      },
    ]

    it('should update issue labels successfully', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      const result = await issueService.updateIssueLabels(
        'test-project',
        1,
        mockLabels
      )

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/labels',
        {
          payload: mockLabels,
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to update labels')
      vi.mocked(apiClient.patch).mockRejectedValue(apiError)

      await expect(
        issueService.updateIssueLabels('test-project', 1, mockLabels)
      ).rejects.toThrow('Failed to update labels')
    })

    it('should update with empty labels array', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssueLabels('test-project', 1, [])

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/labels',
        {
          payload: [],
        }
      )
    })

    it('should update with single label', async () => {
      const singleLabel: Label[] = [
        {
          labelId: 1,
          name: 'Bug',
          description: 'Bug label',
          color: '#ff0000',
        },
      ]

      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssueLabels('test-project', 5, singleLabel)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/5/labels',
        {
          payload: singleLabel,
        }
      )
    })
  })

  describe('updateIssueStatus', () => {
    it('should update issue status successfully', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      const result = await issueService.updateIssueStatus('test-project', 1, 2)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/kanban-config',
        {
          kanbanConfigId: 2,
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to update status')
      vi.mocked(apiClient.patch).mockRejectedValue(apiError)

      await expect(
        issueService.updateIssueStatus('test-project', 1, 2)
      ).rejects.toThrow('Failed to update status')
    })

    it('should update with different kanbanConfigId', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssueStatus('test-project', 10, 5)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/10/kanban-config',
        {
          kanbanConfigId: 5,
        }
      )
    })
  })

  describe('updateIssueAssignees', () => {
    const mockAssignees: UserInfo[] = [
      {
        profileId: 1,
        nickname: 'User 1',
        email: 'user1@test.com',
      },
      {
        profileId: 2,
        nickname: 'User 2',
        email: 'user2@test.com',
      },
    ]

    it('should update issue assignees successfully', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      const result = await issueService.updateIssueAssignees(
        'test-project',
        1,
        mockAssignees
      )

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/assignees',
        {
          assignees: mockAssignees,
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to update assignees')
      vi.mocked(apiClient.patch).mockRejectedValue(apiError)

      await expect(
        issueService.updateIssueAssignees('test-project', 1, mockAssignees)
      ).rejects.toThrow('Failed to update assignees')
    })

    it('should update with empty assignees array', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssueAssignees('test-project', 1, [])

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/assignees',
        {
          assignees: [],
        }
      )
    })

    it('should update with single assignee', async () => {
      const singleAssignee: UserInfo[] = [
        {
          profileId: 3,
          nickname: 'User 3',
          email: 'user3@test.com',
        },
      ]

      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssueAssignees('test-project', 5, singleAssignee)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/5/assignees',
        {
          assignees: singleAssignee,
        }
      )
    })
  })
})
