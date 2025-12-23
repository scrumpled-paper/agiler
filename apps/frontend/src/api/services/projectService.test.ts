import { describe, it, expect, vi, beforeEach } from 'vitest'
import { projectService } from './projectService'
import { apiClient } from '../client'
import type { GetProjectListResponse, GetProjectMembersResponse } from '@/types'

// Mock apiClient
vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
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
            profileId: 1,
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
        '/api/v1/projects/test-project/profiles',
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

      const projectUrl = 'test-project'
      const result = await projectService.getProjectUrlCheck(projectUrl)

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/projects/check', {
        params: {
          url: projectUrl,
        },
      })
      expect(result).toBe(true)
    })

    it('should return false for invalid URL', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: false })

      const projectUrl = 'invalid-url'
      const result = await projectService.getProjectUrlCheck(projectUrl)

      expect(apiClient.get).toHaveBeenCalledWith('/api/v1/projects/check', {
        params: {
          url: projectUrl,
        },
      })
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

  describe('getProjectSummery', () => {
    it('should fetch project summary successfully', async () => {
      const mockResponse = {
        title: 'Test Project',
        url: 'test-project',
        summary: 'Test project summary',
        imageUrl: 'https://placehold.co/600x400',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await projectService.getProjectSummery('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle axios error', async () => {
      const axiosError = new Error('Project not found')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        projectService.getProjectSummery('nonexistent-project')
      ).rejects.toThrow('Project not found')
    })
  })

  describe('updateProjectSummery', () => {
    it('should update project summary successfully', async () => {
      const projectId = 123
      vi.mocked(apiClient.put).mockResolvedValue({ data: projectId })

      const result = await projectService.updateProjectSummery('old-project', {
        title: 'Updated Project',
        url: 'new-project',
        summary: 'Updated summary',
      })

      expect(apiClient.put).toHaveBeenCalledWith(
        '/api/v1/projects/old-project',
        {
          title: 'Updated Project',
          url: 'new-project',
          summary: 'Updated summary',
        }
      )
      expect(result).toBe(projectId)
    })

    it('should handle axios error during update', async () => {
      const axiosError = new Error('Update failed')

      vi.mocked(apiClient.put).mockRejectedValue(axiosError)

      await expect(
        projectService.updateProjectSummery('test-project', {
          title: 'Updated Project',
          url: 'new-url',
          summary: 'Updated summary',
        })
      ).rejects.toThrow('Update failed')
    })
  })

  describe('getUserInfo', () => {
    it('should fetch user info successfully', async () => {
      const mockUserInfo = {
        profileId: 1,
        nickname: 'Test User',
        email: 'test@example.com',
        imageUrl: 'https://example.com/avatar.jpg',
        role: 'Developer',
        description: 'Test description',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockUserInfo })

      const result = await projectService.getUserInfo('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/profiles/me'
      )
      expect(result).toEqual(mockUserInfo)
    })

    it('should handle error when fetching user info fails', async () => {
      const axiosError = new Error('User not found')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(projectService.getUserInfo('test-project')).rejects.toThrow(
        'User not found'
      )
    })
  })

  describe('getMemberProfileById', () => {
    it('should fetch member profile by ID successfully', async () => {
      const mockProfile = {
        profileId: 5,
        nickname: 'John Doe',
        email: 'john@example.com',
        imageUrl: 'https://example.com/john.jpg',
        role: 'Manager',
        description: 'Project manager',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockProfile })

      const result = await projectService.getMemberProfileById(
        'test-project',
        5
      )

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/profiles/5'
      )
      expect(result).toEqual(mockProfile)
    })

    it('should handle error when member not found', async () => {
      const axiosError = new Error('Member not found')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        projectService.getMemberProfileById('test-project', 999)
      ).rejects.toThrow('Member not found')
    })
  })

  describe('updateMyProfile', () => {
    it('should update user profile successfully', async () => {
      const payload = {
        nickname: 'Updated Name',
        email: 'updated@example.com',
        role: 'Senior Developer',
        description: 'Updated description',
      }

      vi.mocked(apiClient.put).mockResolvedValue({ data: undefined })

      await projectService.updateMyProfile('test-project', payload)

      expect(apiClient.put).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/profiles',
        payload
      )
    })

    it('should handle error when updating profile fails', async () => {
      const payload = {
        nickname: 'Updated Name',
        email: 'updated@example.com',
        role: 'Developer',
        description: 'Test',
      }
      const axiosError = new Error('Update failed')

      vi.mocked(apiClient.put).mockRejectedValue(axiosError)

      await expect(
        projectService.updateMyProfile('test-project', payload)
      ).rejects.toThrow('Update failed')
    })
  })

  describe('updateMemberRole', () => {
    it('should update member role successfully', async () => {
      const payload = {
        profileId: 5,
        role: 'Admin',
      }

      vi.mocked(apiClient.patch).mockResolvedValue({ data: undefined })

      await projectService.updateMemberRole('test-project', payload)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/profiles/role',
        payload
      )
    })

    it('should handle error when updating role fails', async () => {
      const payload = {
        profileId: 5,
        role: 'Admin',
      }
      const axiosError = new Error('Permission denied')

      vi.mocked(apiClient.patch).mockRejectedValue(axiosError)

      await expect(
        projectService.updateMemberRole('test-project', payload)
      ).rejects.toThrow('Permission denied')
    })
  })

  describe('updateUserImage', () => {
    it('should update user image successfully', async () => {
      const objectKey = 'profiles/user123.jpg'

      vi.mocked(apiClient.patch).mockResolvedValue({ data: undefined })

      await projectService.updateUserImage('test-project', objectKey)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/profiles/image',
        { objectKey }
      )
    })

    it('should handle error when updating user image fails', async () => {
      const axiosError = new Error('Image upload failed')

      vi.mocked(apiClient.patch).mockRejectedValue(axiosError)

      await expect(
        projectService.updateUserImage('test-project', 'invalid-key')
      ).rejects.toThrow('Image upload failed')
    })
  })

  describe('deleteUserImage', () => {
    it('should delete user image successfully', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined })

      await projectService.deleteUserImage('test-project')

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/profiles/image'
      )
    })

    it('should handle error when deleting user image fails', async () => {
      const axiosError = new Error('Delete failed')

      vi.mocked(apiClient.delete).mockRejectedValue(axiosError)

      await expect(
        projectService.deleteUserImage('test-project')
      ).rejects.toThrow('Delete failed')
    })
  })

  describe('updateMainImage', () => {
    it('should update main project image successfully', async () => {
      const objectKey = 'projects/main-image.jpg'

      vi.mocked(apiClient.patch).mockResolvedValue({ data: undefined })

      await projectService.updateMainImage('test-project', objectKey)

      expect(apiClient.patch).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/image',
        { objectKey }
      )
    })

    it('should handle error when updating main image fails', async () => {
      const axiosError = new Error('Image update failed')

      vi.mocked(apiClient.patch).mockRejectedValue(axiosError)

      await expect(
        projectService.updateMainImage('test-project', 'invalid-key')
      ).rejects.toThrow('Image update failed')
    })
  })

  describe('deleteMainImage', () => {
    it('should delete main project image successfully', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined })

      await projectService.deleteMainImage('test-project')

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/image'
      )
    })

    it('should handle error when deleting main image fails', async () => {
      const axiosError = new Error('Delete operation failed')

      vi.mocked(apiClient.delete).mockRejectedValue(axiosError)

      await expect(
        projectService.deleteMainImage('test-project')
      ).rejects.toThrow('Delete operation failed')
    })
  })
})
