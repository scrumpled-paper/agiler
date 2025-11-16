import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import {
  getProjectList,
  getProjectSidebar,
  getProjectMember,
  getProjectUrlCheck,
  createProject,
} from './projectService'
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

      const result = await getProjectList({ size: 6, page: 0 })

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/projects/info', {
        params: { size: 6, page: 0 },
      })
      expect(result).toEqual(mockResponse)
    })

    it('should handle axios error with server message', async () => {
      const errorMessage = 'Server error message'
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {
            message: errorMessage,
          },
        },
      }

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(getProjectList({ size: 6, page: 0 })).rejects.toThrow(
        errorMessage
      )
    })

    it('should handle axios error without server message', async () => {
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {},
        },
      }

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(getProjectList({ size: 6, page: 0 })).rejects.toThrow(
        'Failed to fetch project list'
      )
    })

    it('should handle unknown error', async () => {
      const unknownError = new Error('Some error')

      vi.mocked(apiClient.get).mockRejectedValue(unknownError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(false)

      await expect(getProjectList({ size: 6, page: 0 })).rejects.toThrow(
        'Unknown error occurred'
      )
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

      const result = await getProjectSidebar({ size: 6, page: 0 })

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/projects', {
        params: { size: 6, page: 0 },
      })
      expect(result).toEqual(mockResponse)
    })

    it('should handle axios error with server message', async () => {
      const errorMessage = 'Server error message'
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {
            message: errorMessage,
          },
        },
      }

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(getProjectSidebar({ size: 6, page: 0 })).rejects.toThrow(
        errorMessage
      )
    })

    it('should handle axios error without server message', async () => {
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {},
        },
      }

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(getProjectSidebar({ size: 6, page: 0 })).rejects.toThrow(
        'Failed to fetch project sidebar'
      )
    })

    it('should handle unknown error', async () => {
      const unknownError = new Error('Some error')

      vi.mocked(apiClient.get).mockRejectedValue(unknownError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(false)

      await expect(getProjectSidebar({ size: 6, page: 0 })).rejects.toThrow(
        'Unknown error occurred'
      )
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

      const result = await getProjectMember({
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

    it('should handle axios error with server message', async () => {
      const errorMessage = 'Server error message'
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {
            message: errorMessage,
          },
        },
      }

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(
        getProjectMember({ projectUrl: 'test-project', size: 5, page: 0 })
      ).rejects.toThrow(errorMessage)

      expect(consoleErrorSpy).toHaveBeenCalledWith(
        'Failed to fetch project members:',
        axiosError
      )
      consoleErrorSpy.mockRestore()
    })

    it('should handle axios error without server message', async () => {
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {},
        },
      }

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(
        getProjectMember({ projectUrl: 'test-project', size: 5, page: 0 })
      ).rejects.toThrow('API 요청 중 오류가 발생했습니다.')

      consoleErrorSpy.mockRestore()
    })

    it('should handle unknown error', async () => {
      const unknownError = new Error('Some error')

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.get).mockRejectedValue(unknownError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(false)

      await expect(
        getProjectMember({ projectUrl: 'test-project', size: 5, page: 0 })
      ).rejects.toThrow('알 수 없는 오류가 발생했습니다.')

      consoleErrorSpy.mockRestore()
    })
  })

  describe('getProjectUrlCheck', () => {
    it('should validate project URL successfully', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: true })

      const result = await getProjectUrlCheck('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/check/test-project'
      )
      expect(result).toBe(true)
    })

    it('should return false for invalid URL', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: false })

      const result = await getProjectUrlCheck('invalid-url')

      expect(result).toBe(false)
    })

    it('should handle axios error with server message', async () => {
      const errorMessage = 'URL already exists'
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {
            message: errorMessage,
          },
        },
      }

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(getProjectUrlCheck('test-project')).rejects.toThrow(
        errorMessage
      )

      expect(consoleErrorSpy).toHaveBeenCalledWith(
        '프로젝트 url 검증 오류 :',
        axiosError
      )
      consoleErrorSpy.mockRestore()
    })

    it('should handle axios error without server message', async () => {
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {},
        },
      }

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.get).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(getProjectUrlCheck('test-project')).rejects.toThrow(
        '프로젝트 url 검증  API 요청 중 오류가 발생했습니다.'
      )

      consoleErrorSpy.mockRestore()
    })

    it('should handle unknown error', async () => {
      const unknownError = new Error('Some error')

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.get).mockRejectedValue(unknownError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(false)

      await expect(getProjectUrlCheck('test-project')).rejects.toThrow(
        '알 수 없는 오류가 발생했습니다.'
      )

      consoleErrorSpy.mockRestore()
    })
  })

  describe('createProject', () => {
    it('should create project successfully', async () => {
      const projectId = 123
      vi.mocked(apiClient.post).mockResolvedValue({ data: projectId })

      const result = await createProject({
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

    it('should handle axios error with server message', async () => {
      const errorMessage = 'Project creation failed'
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {
            message: errorMessage,
          },
        },
      }

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.post).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(
        createProject({
          title: 'New Project',
          url: 'new-project',
          summary: 'A new project',
        })
      ).rejects.toThrow(errorMessage)

      expect(consoleErrorSpy).toHaveBeenCalledWith(
        '프로젝트 생성 오류 :',
        axiosError
      )
      consoleErrorSpy.mockRestore()
    })

    it('should handle axios error without server message', async () => {
      const axiosError = {
        isAxiosError: true,
        response: {
          data: {},
        },
      }

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.post).mockRejectedValue(axiosError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(true)

      await expect(
        createProject({
          title: 'New Project',
          url: 'new-project',
          summary: 'A new project',
        })
      ).rejects.toThrow('프로젝트 생성 API 요청 중 오류가 발생했습니다.')

      consoleErrorSpy.mockRestore()
    })

    it('should handle unknown error', async () => {
      const unknownError = new Error('Some error')

      const consoleErrorSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {})
      vi.mocked(apiClient.post).mockRejectedValue(unknownError)
      vi.spyOn(axios, 'isAxiosError').mockReturnValue(false)

      await expect(
        createProject({
          title: 'New Project',
          url: 'new-project',
          summary: 'A new project',
        })
      ).rejects.toThrow('알 수 없는 오류가 발생했습니다.')

      consoleErrorSpy.mockRestore()
    })
  })
})
