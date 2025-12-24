import { describe, it, expect, vi, beforeEach } from 'vitest'
import { issueService } from './issueService'
import { apiClient } from '../client'
import type {
  IssuePayload,
  updateIssuePayload,
  IssueDetailResponse,
} from '@/types/issue'

// apiClient 모킹
vi.mock('../client', () => ({
  apiClient: {
    post: vi.fn(),
    get: vi.fn(),
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

  describe('getIssueDetail', () => {
    const mockIssueDetail: IssueDetailResponse = {
      issueId: 1,
      title: 'Test Issue',
      contents: 'Test contents',
      assignees: [],
      labels: [],
      notis: [],
      kanbanConfigId: 1,
      isDone: false,
      createdAt: '2025-01-01T00:00:00.000Z',
      startedAt: '2025-01-01T00:00:00.000Z',
      dueAt: '2025-01-10T00:00:00.000Z',
    }

    it('should get issue detail successfully', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockIssueDetail })

      const result = await issueService.getIssueDetail('test-project', 1)

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1'
      )
      expect(result).toEqual(mockIssueDetail)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to get issue detail')
      vi.mocked(apiClient.get).mockRejectedValue(apiError)

      await expect(
        issueService.getIssueDetail('test-project', 1)
      ).rejects.toThrow('Failed to get issue detail')
    })

    it('should get issue detail with different issueId', async () => {
      const differentDetail: IssueDetailResponse = {
        ...mockIssueDetail,
        issueId: 999,
        title: 'Different Issue',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: differentDetail })

      const result = await issueService.getIssueDetail('another-project', 999)

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/another-project/issues/999'
      )
      expect(result).toEqual(differentDetail)
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
          data: { issueId: 1 },
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
          data: { issueId: 999 },
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
        mockPayload
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
        differentPayload
      )
    })
  })

  describe('updateIssueLabels', () => {
    const mockLabelIds: number[] = [1, 2]

    it('should update issue labels successfully', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      const result = await issueService.updateIssueLabels(
        'test-project',
        1,
        mockLabelIds
      )

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/labels',
        {
          labels: mockLabelIds,
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to update labels')
      vi.mocked(apiClient.patch).mockRejectedValue(apiError)

      await expect(
        issueService.updateIssueLabels('test-project', 1, mockLabelIds)
      ).rejects.toThrow('Failed to update labels')
    })

    it('should update with empty labels array', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssueLabels('test-project', 1, [])

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/labels',
        {
          labels: [],
        }
      )
    })

    it('should update with single label', async () => {
      const singleLabel: number[] = [1]

      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      await issueService.updateIssueLabels('test-project', 5, singleLabel)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/5/labels',
        {
          labels: singleLabel,
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
    const mockAssigneeIds: number[] = [1, 2]

    it('should update issue assignees successfully', async () => {
      const mockResponse = { success: true }
      vi.mocked(apiClient.patch).mockResolvedValue({ data: mockResponse })

      const result = await issueService.updateIssueAssignees(
        'test-project',
        1,
        mockAssigneeIds
      )

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/1/assignees',
        {
          assignees: mockAssigneeIds,
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle API error', async () => {
      const apiError = new Error('Failed to update assignees')
      vi.mocked(apiClient.patch).mockRejectedValue(apiError)

      await expect(
        issueService.updateIssueAssignees('test-project', 1, mockAssigneeIds)
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
      const singleAssignee: number[] = [3]

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
