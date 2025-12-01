import { describe, it, expect, vi, beforeEach } from 'vitest'
import { kanbanService } from './kanbanService'
import { apiClient } from '../client'
import type { Issue } from '@/types'

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

  describe('getIssues', () => {
    it('should fetch issues successfully without date', async () => {
      const mockIssues: Issue[] = [
        {
          id: '1',
          name: 'Issue 1',
          column: 'todo',
          owner: {
            nickname: 'user1',
            email: 'user1@example.com',
            imageUrl: 'https://placehold.co/100x100',
          },
          startAt: new Date('2025-01-01'),
          endAt: new Date('2025-01-10'),
          labels: [],
          subscribers: [],
        },
        {
          id: '2',
          name: 'Issue 2',
          column: 'in-progress',
          owner: {
            nickname: 'user2',
            email: 'user2@example.com',
            imageUrl: 'https://placehold.co/100x100',
          },
          startAt: new Date('2025-01-05'),
          endAt: new Date('2025-01-15'),
          labels: [],
          subscribers: [],
        },
      ]

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockIssues })

      const result = await kanbanService.getIssues('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/kanban',
        {
          params: undefined,
        }
      )
      expect(result).toEqual(mockIssues)
    })

    it('should fetch issues successfully with date', async () => {
      const mockIssues: Issue[] = [
        {
          id: '1',
          name: 'Issue 1',
          column: 'todo',
          owner: {
            nickname: 'user1',
            email: 'user1@example.com',
            imageUrl: 'https://placehold.co/100x100',
          },
          startAt: new Date('2025-01-01'),
          endAt: new Date('2025-01-10'),
          labels: [],
          subscribers: [],
        },
      ]

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockIssues })

      const result = await kanbanService.getIssues('test-project', '2025-01-01')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/kanban',
        {
          params: { date: '2025-01-01' },
        }
      )
      expect(result).toEqual(mockIssues)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Failed to fetch issues')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(kanbanService.getIssues('test-project')).rejects.toThrow(
        'Failed to fetch issues'
      )
    })

    it('should handle empty response', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: [] })

      const result = await kanbanService.getIssues('test-project')

      expect(result).toEqual([])
    })
  })

  describe('updateIssue', () => {
    it('should update issue successfully', async () => {
      const updatedTasks: Issue[] = [
        {
          id: '1',
          name: 'Updated Issue',
          column: 'done',
          owner: {
            nickname: 'user1',
            email: 'user1@example.com',
            imageUrl: 'https://placehold.co/100x100',
          },
          startAt: new Date('2025-01-01'),
          endAt: new Date('2025-01-10'),
          labels: [],
          subscribers: [],
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
          column: 'done',
          owner: {
            nickname: 'user1',
            email: 'user1@example.com',
            imageUrl: 'https://placehold.co/100x100',
          },
          startAt: new Date('2025-01-01'),
          endAt: new Date('2025-01-10'),
          labels: [],
          subscribers: [],
        },
        {
          id: '2',
          name: 'Issue 2',
          column: 'done',
          owner: {
            nickname: 'user2',
            email: 'user2@example.com',
            imageUrl: 'https://placehold.co/100x100',
          },
          startAt: new Date('2025-01-05'),
          endAt: new Date('2025-01-15'),
          labels: [],
          subscribers: [],
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
