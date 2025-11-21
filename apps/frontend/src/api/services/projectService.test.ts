import { describe, it, expect, vi, beforeEach } from 'vitest'
import { projectService } from './projectService'
import { apiClient } from '../client'
import type { GetProjectListResponse, GetProjectMembersResponse } from '@/types'

// Mock apiClient
vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('projectService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getProjectList', () => {
    it('should fetch project list successfully', async () => {
      const mockResponse: GetProjectListResponse = {
        contents: [
          {
            title: 'Project 1',
            url: '/projects/1',
            imageUrl: 'https://placehold.co/600x400',
            summary: 'Test project 1',
          },
        ],
        totalPages: 1,
        totalElements: 1,
        currentPage: 0,
        pageSize: 6,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await projectService.getProjectList({ size: 6, page: 0 })

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/projects/info', {
        params: { size: 6, page: 0 },
      })
      expect(result).toEqual(mockResponse)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Server error message')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        projectService.getProjectList({ size: 6, page: 0 })
      ).rejects.toThrow('Server error message')
    })
  })

  describe('getProjectSidebar', () => {
    it('should fetch project sidebar successfully', async () => {
      const mockResponse: GetProjectListResponse = {
        contents: [
          {
            title: 'Project 1',
            url: '/projects/1',
            imageUrl: 'https://placehold.co/600x400',
            summary: 'Test project 1',
          },
        ],
        currentPage: 0,
        totalPages: 1,
        totalElements: 1,
        pageSize: 6,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await projectService.getProjectSidebar({
        size: 6,
        page: 0,
      })

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/projects', {
        params: { size: 6, page: 0 },
      })
      expect(result).toEqual(mockResponse)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Server error message')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        projectService.getProjectSidebar({ size: 6, page: 0 })
      ).rejects.toThrow('Server error message')
    })
  })

  describe('getProjectMember', () => {
    it('should fetch project members successfully', async () => {
      const mockResponse: GetProjectMembersResponse = {
        contents: [
          {
            peopleId: 1,
            nickname: 'Alice',
            email: 'alice@example.com',
            imageUrl: 'https://placehold.co/100x100',
            role: 'Developer',
            description: 'Frontend developer',
          },
        ],
        totalPages: 1,
        number: 0,
        size: 5,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await projectService.getProjectMember({
        projectUrl: 'test-project',
        size: 5,
        page: 0,
      })

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/people',
        {
          params: { size: 5, page: 0 },
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Server error message')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        projectService.getProjectMember({
          projectUrl: 'test-project',
          size: 5,
          page: 0,
        })
      ).rejects.toThrow('Server error message')
    })
  })

  describe('getProjectUrlCheck', () => {
    it('should validate project URL successfully', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: true })

      const result = await projectService.getProjectUrlCheck('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/check/test-project'
      )
      expect(result).toBe(true)
    })

    it('should return false for invalid URL', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: false })

      const result = await projectService.getProjectUrlCheck('invalid-url')

      expect(result).toBe(false)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('URL already exists')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        projectService.getProjectUrlCheck('test-project')
      ).rejects.toThrow('URL already exists')
    })
  })

  describe('createProject', () => {
    it('should create project successfully', async () => {
      const projectId = 123
      vi.mocked(apiClient.post).mockResolvedValue({ data: projectId })

      const result = await projectService.createProject({
        title: 'New Project',
        url: 'new-project',
        summary: 'A new project',
      })

      expect(apiClient.post).toHaveBeenCalledWith('/api/v1/projects', {
        title: 'New Project',
        url: 'new-project',
        summary: 'A new project',
      })
      expect(result).toBe(projectId)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Project creation failed')

      vi.mocked(apiClient.post).mockRejectedValue(axiosError)

      await expect(
        projectService.createProject({
          title: 'New Project',
          url: 'new-project',
          summary: 'A new project',
        })
      ).rejects.toThrow('Project creation failed')
    })
  })
})
