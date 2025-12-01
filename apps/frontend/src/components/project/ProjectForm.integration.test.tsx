import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import userEvent from '@testing-library/user-event'
import ProjectForm from './projectForm'
import { server } from '@/mocks/server'
import { http, HttpResponse } from 'msw'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://agiler.p-e.kr'

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

describe('ProjectForm - 통합 테스트', () => {
  beforeEach(() => {
    server.resetHandlers()
    vi.clearAllMocks()
  })

  describe('생성 모드', () => {
    it('빈 폼으로 시작하여 프로젝트를 생성할 수 있다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      // 폼 필드 확인
      expect(screen.getByPlaceholderText('Enter your Title')).toHaveValue('')
      expect(screen.getByPlaceholderText('team-name_project-name')).toHaveValue(
        ''
      )
      expect(screen.getByPlaceholderText('Enter your summary')).toHaveValue('')

      // 프로젝트 정보 입력
      await user.type(
        screen.getByPlaceholderText('Enter your Title'),
        'New Amazing Project'
      )

      // Title을 입력하면 URL이 자동 생성되므로, URL 필드는 그대로 둠
      // (또는 수동으로 변경하려면 clear 후 type)

      await user.type(
        screen.getByPlaceholderText('Enter your summary'),
        'This is a new amazing project'
      )

      // 제출
      await user.click(
        screen.getByRole('button', { name: /Create New Project/i })
      )

      // 성공 콜백 호출 확인 (title에서 자동 생성된 URL)
      await waitFor(() => {
        expect(mockOnCreateSuccess).toHaveBeenCalledWith('new-amazing-project')
      })
    })

    it('Title을 입력하면 URL이 자동으로 생성된다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      const titleInput = screen.getByPlaceholderText('Enter your Title')
      const urlInput = screen.getByPlaceholderText('team-name_project-name')

      // Title 입력
      await user.type(titleInput, 'My Test Project')

      // URL이 자동 생성되는지 확인
      await waitFor(() => {
        expect(urlInput).toHaveValue('my-test-project')
      })
    })

    it('URL을 수동으로 편집하면 자동 생성이 중단된다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      const titleInput = screen.getByPlaceholderText('Enter your Title')
      const urlInput = screen.getByPlaceholderText('team-name_project-name')

      // Title 입력 (URL 자동 생성)
      await user.type(titleInput, 'Initial Title')
      await waitFor(() => {
        expect(urlInput).toHaveValue('initial-title')
      })

      // URL 수동 편집
      await user.clear(urlInput)
      await user.type(urlInput, 'custom-url')

      // Title 다시 변경해도 URL이 변경되지 않음
      await user.clear(titleInput)
      await user.type(titleInput, 'Changed Title')

      // URL은 수동 편집한 값 유지 (다시 쿼리)
      const updatedUrlInput = screen.getByPlaceholderText(
        'team-name_project-name'
      )
      expect(updatedUrlInput).toHaveValue('custom-url')
    })
  })

  describe('수정 모드', () => {
    const initialData = {
      title: 'Existing Project',
      summary: 'Existing summary',
      url: 'existing-project',
    }

    it('초기 데이터가 폼에 올바르게 표시된다', () => {
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          initialData={initialData}
          projectUrl="existing-project"
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      expect(screen.getByDisplayValue('Existing Project')).toBeInTheDocument()
      expect(screen.getByDisplayValue('existing-project')).toBeInTheDocument()
      expect(screen.getByDisplayValue('Existing summary')).toBeInTheDocument()
    })

    it('프로젝트 정보를 수정하고 저장할 수 있다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      // URL 체크 성공 응답
      server.use(
        http.get('/api/v1/projects/check', ({ request }) => {
          const url = new URL(request.url)
          const projectUrl = url.searchParams.get('url')
          return HttpResponse.json(projectUrl === 'updated-project')
        }),
        http.get(`${API_BASE_URL}/api/v1/projects/check`, ({ request }) => {
          const url = new URL(request.url)
          const projectUrl = url.searchParams.get('url')
          return HttpResponse.json(projectUrl === 'updated-project')
        })
      )

      render(
        <ProjectForm
          initialData={initialData}
          projectUrl="existing-project"
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      // 필드 수정
      const titleInput = screen.getByPlaceholderText('Enter your Title')
      const urlInput = screen.getByPlaceholderText('team-name_project-name')
      const summaryInput = screen.getByPlaceholderText('Enter your summary')

      await user.clear(titleInput)
      await user.type(titleInput, 'Updated Project')

      await user.clear(urlInput)
      await user.type(urlInput, 'updated-project')

      await user.clear(summaryInput)
      await user.type(summaryInput, 'Updated summary')

      // 제출
      await user.click(
        screen.getByRole('button', { name: /Create New Project/i })
      )

      // 성공 콜백 호출 확인
      await waitFor(() => {
        expect(mockOnCreateSuccess).toHaveBeenCalledWith('updated-project')
      })
    })

    it('수정 모드에서는 URL이 자동 생성되지 않는다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          initialData={initialData}
          projectUrl="existing-project"
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      const titleInput = screen.getByPlaceholderText('Enter your Title')

      // Title 변경
      await user.clear(titleInput)
      await user.type(titleInput, 'New Title')

      // URL은 변경되지 않음 (다시 쿼리)
      const urlInput = screen.getByPlaceholderText('team-name_project-name')
      expect(urlInput).toHaveValue('existing-project')
    })
  })

  describe('유효성 검증', () => {
    it('필수 필드가 비어있으면 에러 메시지가 표시된다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      // 빈 폼으로 제출
      await user.click(
        screen.getByRole('button', { name: /Create New Project/i })
      )

      // 에러 메시지 확인
      await waitFor(() => {
        expect(
          screen.getByText('Please enter a project title')
        ).toBeInTheDocument()
        expect(
          screen.getByText('Please enter a project URL')
        ).toBeInTheDocument()
        expect(
          screen.getByText('Please enter a project summary')
        ).toBeInTheDocument()
      })

      // 성공 콜백이 호출되지 않음
      expect(mockOnCreateSuccess).not.toHaveBeenCalled()
    })

    it('URL이 이미 사용 중이면 에러 메시지가 표시된다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      // URL 체크 실패 응답
      server.use(
        http.get('/api/v1/projects/check', () => {
          return HttpResponse.json(false) // URL 사용 불가
        }),
        http.get(`${API_BASE_URL}/api/v1/projects/check`, () => {
          return HttpResponse.json(false)
        })
      )

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      // 폼 입력
      await user.type(
        screen.getByPlaceholderText('Enter your Title'),
        'Test Project'
      )
      await user.type(
        screen.getByPlaceholderText('team-name_project-name'),
        'existing-project'
      )
      await user.type(
        screen.getByPlaceholderText('Enter your summary'),
        'Test summary'
      )

      // 제출
      await user.click(
        screen.getByRole('button', { name: /Create New Project/i })
      )

      // 에러 메시지 확인
      await waitFor(() => {
        expect(
          screen.getByText(
            'This URL is already in use. Please choose another one.'
          )
        ).toBeInTheDocument()
      })

      expect(mockOnCreateSuccess).not.toHaveBeenCalled()
    })

    it('잘못된 URL 형식이면 에러 메시지가 표시된다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      // 잘못된 형식의 URL 입력
      await user.type(screen.getByPlaceholderText('Enter your Title'), 'Test')
      await user.type(
        screen.getByPlaceholderText('team-name_project-name'),
        'invalid url with spaces!'
      )
      await user.type(
        screen.getByPlaceholderText('Enter your summary'),
        'Summary'
      )

      // 제출
      await user.click(
        screen.getByRole('button', { name: /Create New Project/i })
      )

      // 에러 메시지 확인
      await waitFor(() => {
        expect(
          screen.getByText(
            /Please enter a valid URL \(format: team-name_project-name/i
          )
        ).toBeInTheDocument()
      })

      expect(mockOnCreateSuccess).not.toHaveBeenCalled()
    })
  })

  describe('취소 기능', () => {
    it('Cancel 버튼을 클릭하면 폼이 초기화되고 콜백이 호출된다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      // 폼 입력
      await user.type(
        screen.getByPlaceholderText('Enter your Title'),
        'Test Project'
      )
      await user.type(
        screen.getByPlaceholderText('team-name_project-name'),
        'test-project'
      )
      await user.type(
        screen.getByPlaceholderText('Enter your summary'),
        'Test summary'
      )

      // Cancel 클릭
      await user.click(screen.getByRole('button', { name: /Cancel/i }))

      // 취소 콜백 호출 확인
      expect(mockOnCancel).toHaveBeenCalled()

      // 폼 초기화 확인
      expect(screen.getByPlaceholderText('Enter your Title')).toHaveValue('')
      expect(screen.getByPlaceholderText('team-name_project-name')).toHaveValue(
        ''
      )
      expect(screen.getByPlaceholderText('Enter your summary')).toHaveValue('')
    })
  })

  describe('로딩 상태', () => {
    it('제출 중일 때 버튼이 비활성화되고 "Creating..." 텍스트가 표시된다', async () => {
      const user = userEvent.setup()
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      // API 응답을 지연시켜 로딩 상태를 확인
      server.use(
        http.post('/api/v1/projects', async () => {
          await new Promise(resolve => setTimeout(resolve, 100))
          return HttpResponse.json(123)
        }),
        http.post(`${API_BASE_URL}/api/v1/projects`, async () => {
          await new Promise(resolve => setTimeout(resolve, 100))
          return HttpResponse.json(123)
        })
      )

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
        />,
        { wrapper: createWrapper() }
      )

      // 폼 입력
      await user.type(
        screen.getByPlaceholderText('Enter your Title'),
        'Test Project'
      )
      await user.type(
        screen.getByPlaceholderText('team-name_project-name'),
        'test-project'
      )
      await user.type(
        screen.getByPlaceholderText('Enter your summary'),
        'Test summary'
      )

      // 제출
      const submitButton = screen.getByRole('button', {
        name: /Create New Project/i,
      })
      await user.click(submitButton)

      // 로딩 상태 확인
      await waitFor(() => {
        expect(screen.getByText('Creating...')).toBeInTheDocument()
        expect(submitButton).toBeDisabled()
      })

      // 완료 대기
      await waitFor(() => {
        expect(mockOnCreateSuccess).toHaveBeenCalled()
      })
    })
  })

  describe('커스텀 버튼 레이블', () => {
    it('커스텀 버튼 레이블을 사용할 수 있다', () => {
      const mockOnCreateSuccess = vi.fn()
      const mockOnCancel = vi.fn()

      render(
        <ProjectForm
          onCreateSuccess={mockOnCreateSuccess}
          onCancel={mockOnCancel}
          submitButtonLabel="Update Project"
          cancelButtonLabel="Go Back"
        />,
        { wrapper: createWrapper() }
      )

      expect(
        screen.getByRole('button', { name: 'Update Project' })
      ).toBeInTheDocument()
      expect(
        screen.getByRole('button', { name: 'Go Back' })
      ).toBeInTheDocument()
    })
  })
})
