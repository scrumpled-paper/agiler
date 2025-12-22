import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { server } from '@/mocks/server'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { TemplateSelectModal } from './TemplateSelectModal'
import type { TemplateListItem } from '@/types/template'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
        staleTime: 0,
      },
    },
  })

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    )
  }
}

describe('TemplateSelectModal', () => {
  const mockOnClose = vi.fn()
  const mockOnSelectTemplate = vi.fn()

  const mockTemplates: TemplateListItem[] = [
    {
      templateId: 1,
      title: 'Bug Report Template',
      description: 'Template for reporting bugs',
    },
    {
      templateId: 2,
      title: 'Feature Request Template',
      description: 'Template for requesting new features',
    },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('제목을 렌더링한다', () => {
    render(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />,
      { wrapper: createWrapper() }
    )

    expect(screen.getByText('Choose a template')).toBeInTheDocument()
  })

  it('템플릿 목록을 성공적으로 로드한다', async () => {
    server.use(
      http.get(
        `${API_BASE_URL}/api/v1/projects/test-project/issues/templates`,
        () => {
          return HttpResponse.json({
            issueTemplates: mockTemplates,
            size: mockTemplates.length,
          })
        }
      )
    )

    render(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />,
      { wrapper: createWrapper() }
    )

    await waitFor(() => {
      expect(screen.getByText('Bug Report Template')).toBeInTheDocument()
      expect(screen.getByText('Feature Request Template')).toBeInTheDocument()
    })
  })

  it('Blank 템플릿 카드를 항상 표시한다', async () => {
    server.use(
      http.get(
        `${API_BASE_URL}/api/v1/projects/test-project/issues/templates`,
        () => {
          return HttpResponse.json({
            issueTemplates: mockTemplates,
            size: mockTemplates.length,
          })
        }
      )
    )

    render(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />,
      { wrapper: createWrapper() }
    )

    await waitFor(() => {
      expect(screen.getByText('Blank Issue')).toBeInTheDocument()
    })
  })

  it('템플릿이 없을 때 적절한 메시지를 표시한다', async () => {
    server.use(
      http.get(
        `${API_BASE_URL}/api/v1/projects/test-project/issues/templates`,
        () => {
          return HttpResponse.json({
            issueTemplates: [],
            size: 0,
          })
        }
      )
    )

    render(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />,
      { wrapper: createWrapper() }
    )

    await waitFor(() => {
      expect(screen.getByText(/No templates available/i)).toBeInTheDocument()
    })
  })

  it('isOpen이 false일 때 모달을 렌더링하지 않는다', () => {
    render(
      <TemplateSelectModal
        isOpen={false}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />,
      { wrapper: createWrapper() }
    )

    expect(screen.queryByText('Choose a template')).not.toBeInTheDocument()
  })
})
