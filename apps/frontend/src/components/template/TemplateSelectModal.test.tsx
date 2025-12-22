import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { server } from '@/mocks/server'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { TemplateSelectModal } from './TemplateSelectModal'
import type { TemplateListItem } from '@/types/template'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

/**
 * 테스트마다 독립적인 QueryClient를 생성하여
 * 이전 테스트의 캐시 데이터가 간섭하지 않도록 합니다.
 */
const renderWithClient = (ui: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
        staleTime: 0,
      },
    },
  })
  return render(
    <QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>
  )
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
    // 다른 테스트에서 설정한 MSW 핸들러를 초기화하여 현재 테스트의 모킹이 우선되게 함
    server.resetHandlers()
  })

  it('제목을 렌더링한다', () => {
    renderWithClient(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />
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

    renderWithClient(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />
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

    renderWithClient(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />
    )

    await waitFor(() => {
      // 컴포넌트 내부 로직에 따라 "Blank Issue"로 표시됨
      expect(screen.getByText('Blank Issue')).toBeInTheDocument()
    })
  })

  it('템플릿이 없을 때 적절한 메시지를 표시한다', async () => {
    // 1. 빈 응답을 반환하도록 명확하게 모킹
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

    renderWithClient(
      <TemplateSelectModal
        isOpen={true}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />
    )

    // 2. 텍스트 부분 일치를 위해 정규식 /i 사용 (컴포넌트에 뒤 문장이 더 붙어있기 때문)
    await waitFor(() => {
      // 모킹이 정상 작동했다면 이전 데이터인 "Bug Report Template"이 보이지 않아야 함
      expect(screen.queryByText('Bug Report Template')).not.toBeInTheDocument()
      expect(screen.getByText(/No templates available/i)).toBeInTheDocument()
    })
  })

  it('isOpen이 false일 때 모달을 렌더링하지 않는다', () => {
    renderWithClient(
      <TemplateSelectModal
        isOpen={false}
        onClose={mockOnClose}
        projectUrl="test-project"
        resourceType="issues"
        onSelectTemplate={mockOnSelectTemplate}
      />
    )

    expect(screen.queryByText('Choose a template')).not.toBeInTheDocument()
  })
})
