import { describe, it, expect, vi, beforeEach } from 'vitest'
import { labelService } from './labelService'
import { apiClient } from '../client'
import type { LabelListResponse } from '@/types/label'

// Mock apiClient
vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('labelService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getLabels', () => {
    it('should fetch labels successfully', async () => {
      const mockResponse: LabelListResponse = {
        labels: [
          {
            id: 1,
            name: 'bug',
            description: 'Bug report label',
            color: '#FF4040',
          },
          {
            id: 2,
            name: 'feature',
            description: 'Feature request label',
            color: '#4040FF',
          },
        ],
        size: 2,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await labelService.getLabels('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/labels'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when fetching labels fails', async () => {
      const axiosError = new Error('Failed to fetch labels')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(labelService.getLabels('test-project')).rejects.toThrow(
        'Failed to fetch labels'
      )
    })

    it('should fetch empty labels list', async () => {
      const mockResponse: LabelListResponse = {
        labels: [],
        size: 0,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await labelService.getLabels('empty-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/empty-project/labels'
      )
      expect(result).toEqual(mockResponse)
      expect(result.labels).toHaveLength(0)
    })
  })

  describe('createLabel', () => {
    it('should create label successfully', async () => {
      const labelData = {
        name: 'enhancement',
        description: 'Enhancement label',
        color: '#40FF46',
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: undefined })

      await labelService.createLabel('test-project', labelData)

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/labels',
        labelData
      )
    })

    it('should handle error when creating label fails', async () => {
      const labelData = {
        name: 'duplicate',
        description: 'Duplicate label',
        color: '#FF4040',
      }
      const axiosError = new Error('Label already exists')

      vi.mocked(apiClient.post).mockRejectedValue(axiosError)

      await expect(
        labelService.createLabel('test-project', labelData)
      ).rejects.toThrow('Label already exists')
    })

    it('should create label with special characters in name', async () => {
      const labelData = {
        name: 'work-in-progress',
        description: 'Work in progress label',
        color: '#FFE240',
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: undefined })

      await labelService.createLabel('test-project', labelData)

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/labels',
        labelData
      )
    })
  })

  describe('updateLabel', () => {
    it('should update label successfully', async () => {
      const labelData = {
        name: 'updated-bug',
        description: 'Updated bug description',
        color: '#FF6060',
      }

      vi.mocked(apiClient.put).mockResolvedValue({ data: undefined })

      await labelService.updateLabel('test-project', 1, labelData)

      expect(apiClient.put).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/labels/1',
        labelData
      )
    })

    it('should handle error when updating label fails', async () => {
      const labelData = {
        name: 'invalid-label',
        description: 'Invalid label',
        color: '#000000',
      }
      const axiosError = new Error('Label not found')

      vi.mocked(apiClient.put).mockRejectedValue(axiosError)

      await expect(
        labelService.updateLabel('test-project', 999, labelData)
      ).rejects.toThrow('Label not found')
    })

    it('should update only label color', async () => {
      const labelData = {
        name: 'bug',
        description: 'Bug report label',
        color: '#AABBCC',
      }

      vi.mocked(apiClient.put).mockResolvedValue({ data: undefined })

      await labelService.updateLabel('test-project', 5, labelData)

      expect(apiClient.put).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/labels/5',
        labelData
      )
    })
  })

  describe('deleteLabel', () => {
    it('should delete label successfully', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined })

      await labelService.deleteLabel('test-project', { labelId: 1 })

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/labels',
        {
          data: { labelId: 1 },
        }
      )
    })

    it('should handle error when deleting label fails', async () => {
      const axiosError = new Error('Label not found')

      vi.mocked(apiClient.delete).mockRejectedValue(axiosError)

      await expect(
        labelService.deleteLabel('test-project', { labelId: 999 })
      ).rejects.toThrow('Label not found')
    })

    it('should delete label with specific ID', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined })

      await labelService.deleteLabel('my-project', { labelId: 42 })

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/my-project/labels',
        {
          data: { labelId: 42 },
        }
      )
    })
  })
})
