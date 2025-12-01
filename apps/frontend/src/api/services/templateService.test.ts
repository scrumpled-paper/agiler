import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  templateService,
  issueTemplateService,
  meetingTemplateService,
  retroTemplateService,
  scrumTemplateService,
} from './templateService'
import { apiClient } from '../client'
import type {
  IssueTemplateListResponse,
  MeetingTemplateListResponse,
  RetroTemplateListResponse,
  ScrumTemplateListResponse,
  TemplateDetail,
} from '@/types/template'

// Mock apiClient
vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('templateService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getTemplates', () => {
    it('should fetch issue templates successfully', async () => {
      const mockResponse: IssueTemplateListResponse = {
        issueTemplates: [
          {
            templateId: 1,
            title: 'Bug Report',
            description: 'Template for bug reports',
          },
          {
            templateId: 2,
            title: 'Feature Request',
            description: 'Template for feature requests',
          },
        ],
        size: 2,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await templateService.getTemplates(
        'test-project',
        'issues'
      )

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/templates'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should fetch meeting templates successfully', async () => {
      const mockResponse: MeetingTemplateListResponse = {
        meetingTemplates: [
          {
            templateId: 3,
            title: 'Sprint Planning',
            description: 'Template for sprint planning',
          },
        ],
        size: 1,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await templateService.getTemplates(
        'test-project',
        'meetings'
      )

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/meetings/templates'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should fetch retro templates successfully', async () => {
      const mockResponse: RetroTemplateListResponse = {
        retroTemplates: [
          {
            templateId: 4,
            title: 'Sprint Retrospective',
            description: 'Template for retrospectives',
          },
        ],
        size: 1,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await templateService.getTemplates(
        'test-project',
        'retros'
      )

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/retros/templates'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should fetch scrum templates successfully', async () => {
      const mockResponse: ScrumTemplateListResponse = {
        scrumTemplates: [
          {
            templateId: 5,
            title: 'Daily Standup',
            description: 'Template for daily standups',
          },
        ],
        size: 1,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await templateService.getTemplates(
        'test-project',
        'scrums'
      )

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/scrums/templates'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when fetching templates fails', async () => {
      const axiosError = new Error('Failed to fetch templates')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        templateService.getTemplates('test-project', 'issues')
      ).rejects.toThrow('Failed to fetch templates')
    })

    it('should fetch empty templates list', async () => {
      const mockResponse: IssueTemplateListResponse = {
        issueTemplates: [],
        size: 0,
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await templateService.getTemplates(
        'empty-project',
        'issues'
      )

      expect(result.issueTemplates).toHaveLength(0)
      expect(result.size).toBe(0)
    })
  })

  describe('getTemplateDetail', () => {
    it('should fetch template detail successfully', async () => {
      const mockResponse: TemplateDetail = {
        title: 'Bug Report',
        description: 'Template for bug reports',
        contents: '# Bug Report\n\n## Description\n\n## Steps to Reproduce',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result = await templateService.getTemplateDetail(
        'test-project',
        'issues',
        1
      )

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/templates/1'
      )
      expect(result).toEqual(mockResponse)
      expect(result.contents).toContain('# Bug Report')
    })

    it('should handle error when fetching template detail fails', async () => {
      const axiosError = new Error('Template not found')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        templateService.getTemplateDetail('test-project', 'issues', 999)
      ).rejects.toThrow('Template not found')
    })
  })

  describe('createTemplate', () => {
    it('should create template successfully', async () => {
      const templateData: TemplateDetail = {
        title: 'New Template',
        description: 'A new template',
        contents: '# New Template\n\nContent here',
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: undefined })

      await templateService.createTemplate(
        'test-project',
        'issues',
        templateData
      )

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/templates',
        templateData
      )
    })

    it('should handle error when creating template fails', async () => {
      const templateData: TemplateDetail = {
        title: 'Duplicate Template',
        description: 'This already exists',
        contents: '# Duplicate',
      }
      const axiosError = new Error('Template already exists')

      vi.mocked(apiClient.post).mockRejectedValue(axiosError)

      await expect(
        templateService.createTemplate('test-project', 'issues', templateData)
      ).rejects.toThrow('Template already exists')
    })

    it('should create template with markdown content', async () => {
      const templateData: TemplateDetail = {
        title: 'Markdown Template',
        description: 'Template with markdown',
        contents:
          '# Title\n\n## Section 1\n\n- Item 1\n- Item 2\n\n```js\ncode block\n```',
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: undefined })

      await templateService.createTemplate(
        'test-project',
        'meetings',
        templateData
      )

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/meetings/templates',
        templateData
      )
    })
  })

  describe('updateTemplate', () => {
    it('should update template successfully', async () => {
      const templateData = {
        templateId: 1,
        title: 'Updated Template',
        description: 'Updated description',
        contents: '# Updated\n\nNew content',
      }

      vi.mocked(apiClient.put).mockResolvedValue({ data: undefined })

      await templateService.updateTemplate(
        'test-project',
        'issues',
        templateData
      )

      expect(apiClient.put).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/templates',
        templateData
      )
    })

    it('should handle error when updating template fails', async () => {
      const templateData = {
        templateId: 999,
        title: 'Non-existent Template',
        description: 'This does not exist',
        contents: '# Error',
      }
      const axiosError = new Error('Template not found')

      vi.mocked(apiClient.put).mockRejectedValue(axiosError)

      await expect(
        templateService.updateTemplate('test-project', 'issues', templateData)
      ).rejects.toThrow('Template not found')
    })
  })

  describe('deleteTemplate', () => {
    it('should delete template successfully', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined })

      await templateService.deleteTemplate('test-project', 'issues', {
        templateId: 1,
      })

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/issues/templates',
        {
          data: { templateId: 1 },
        }
      )
    })

    it('should handle error when deleting template fails', async () => {
      const axiosError = new Error('Template not found')

      vi.mocked(apiClient.delete).mockRejectedValue(axiosError)

      await expect(
        templateService.deleteTemplate('test-project', 'issues', {
          templateId: 999,
        })
      ).rejects.toThrow('Template not found')
    })

    it('should delete template with specific ID', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined })

      await templateService.deleteTemplate('my-project', 'meetings', {
        templateId: 42,
      })

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/my-project/meetings/templates',
        {
          data: { templateId: 42 },
        }
      )
    })
  })
})

describe('issueTemplateService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call templateService.getTemplates with issues type', async () => {
    const mockResponse: IssueTemplateListResponse = {
      issueTemplates: [],
      size: 0,
    }

    vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

    await issueTemplateService.getTemplates('test-project')

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/v1/projects/test-project/issues/templates'
    )
  })

  it('should call templateService.createTemplate with issues type', async () => {
    const templateData: TemplateDetail = {
      title: 'Issue Template',
      description: 'Description',
      contents: 'Content',
    }

    vi.mocked(apiClient.post).mockResolvedValue({ data: undefined })

    await issueTemplateService.createTemplate('test-project', templateData)

    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/v1/projects/test-project/issues/templates',
      templateData
    )
  })
})

describe('meetingTemplateService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call templateService.getTemplates with meetings type', async () => {
    const mockResponse: MeetingTemplateListResponse = {
      meetingTemplates: [],
      size: 0,
    }

    vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

    await meetingTemplateService.getTemplates('test-project')

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/v1/projects/test-project/meetings/templates'
    )
  })
})

describe('retroTemplateService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call templateService.getTemplates with retros type', async () => {
    const mockResponse: RetroTemplateListResponse = {
      retroTemplates: [],
      size: 0,
    }

    vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

    await retroTemplateService.getTemplates('test-project')

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/v1/projects/test-project/retros/templates'
    )
  })
})

describe('scrumTemplateService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call templateService.getTemplates with scrums type', async () => {
    const mockResponse: ScrumTemplateListResponse = {
      scrumTemplates: [],
      size: 0,
    }

    vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

    await scrumTemplateService.getTemplates('test-project')

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/v1/projects/test-project/scrums/templates'
    )
  })
})
