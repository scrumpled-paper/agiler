import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import userEvent from '@testing-library/user-event'
import ProjectManagement from './ProjectManagement'
import { server } from '@/mocks/server'
import { http, HttpResponse } from 'msw'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'https://agiler.p-e.kr'

const createWrapper = (initialRoute = '/projects/test-project/management') => {
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
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[initialRoute]}>
          <Routes>
            <Route path="/projects/:projectUrl/management" element={children} />
            <Route
              path="/projects/:projectUrl"
              element={<div>Project Detail Page</div>}
            />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    )
  }
}

describe('ProjectManagement Page - 통합 테스트', () => {
  beforeEach(() => {
    server.resetHandlers()
    vi.clearAllMocks()
  })

  describe('UI 렌더링', () => {
    it('프로젝트 수정 페이지가 올바르게 렌더링된다', async () => {
      render(<ProjectManagement />, { wrapper: createWrapper() })

      // 페이지 타이틀 확인
      expect(screen.getByText('Project Management')).toBeInTheDocument()

      // 폼 필드 확인
      expect(
        screen.getByPlaceholderText('Enter your Title')
      ).toBeInTheDocument()
      expect(
        screen.getByPlaceholderText('team-name_project-name')
      ).toBeInTheDocument()
      expect(
        screen.getByPlaceholderText('Enter your summary')
      ).toBeInTheDocument()

      // 버튼 확인
      expect(screen.getByRole('button', { name: /Save/i })).toBeInTheDocument()
      expect(
        screen.getByRole('button', { name: /Cancel/i })
      ).toBeInTheDocument()

      // 프로젝트 데이터 로드 대기 (더 긴 timeout)
      const titleInput = await screen.findByDisplayValue(
        'Test Project',
        {},
        { timeout: 10000 }
      )
      expect(titleInput).toBeInTheDocument()
    })

    it('초기 데이터가 폼에 올바르게 로드된다', async () => {
      render(<ProjectManagement />, { wrapper: createWrapper() })

      await waitFor(
        () => {
          expect(screen.getByDisplayValue('Test Project')).toBeInTheDocument()
          expect(screen.getByDisplayValue('test-project')).toBeInTheDocument()
          expect(
            screen.getByDisplayValue('Test project summary')
          ).toBeInTheDocument()
        },
        { timeout: 5000 }
      )
    })
  })

  describe('프로젝트 수정 성공 시나리오', () => {
    it('프로젝트 정보를 수정하면 성공적으로 업데이트되고 새 URL로 리다이렉트된다', async () => {
      const user = userEvent.setup()

      // MSW에서 URL 체크를 성공으로 응답하도록 설정
      server.use(
        http.get('/api/v1/projects/check', ({ request }) => {
          const url = new URL(request.url)
          const projectUrl = url.searchParams.get('url')
          // 'updated-project' URL은 사용 가능하다고 응답
          return HttpResponse.json(projectUrl === 'updated-project')
        }),
        http.get(`${API_BASE_URL}/api/v1/projects/check`, ({ request }) => {
          const url = new URL(request.url)
          const projectUrl = url.searchParams.get('url')
          return HttpResponse.json(projectUrl === 'updated-project')
        })
      )

      render(<ProjectManagement />, { wrapper: createWrapper() })

      // 초기 데이터 로드 대기
      await waitFor(() => {
        expect(screen.getByDisplayValue('Test Project')).toBeInTheDocument()
      })

      // 프로젝트 정보 수정
      const titleInput = screen.getByPlaceholderText('Enter your Title')
      const urlInput = screen.getByPlaceholderText('team-name_project-name')
      const summaryInput = screen.getByPlaceholderText('Enter your summary')

      await user.clear(titleInput)
      await user.type(titleInput, 'Updated Project Title')

      await user.clear(urlInput)
      await user.type(urlInput, 'updated-project')

      await user.clear(summaryInput)
      await user.type(summaryInput, 'Updated project summary')

      // 제출 버튼 클릭
      const submitButton = screen.getByRole('button', {
        name: /Save/i,
      })
      await user.click(submitButton)

      // 성공 후 새 URL로 리다이렉트 확인
      await waitFor(
        () => {
          expect(screen.getByText('Project Detail Page')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('URL만 변경해도 새 URL로 리다이렉트된다', async () => {
      const user = userEvent.setup()

      server.use(
        http.get('/api/v1/projects/check', ({ request }) => {
          const url = new URL(request.url)
          const projectUrl = url.searchParams.get('url')
          return HttpResponse.json(projectUrl === 'new-url')
        }),
        http.get(`${API_BASE_URL}/api/v1/projects/check`, ({ request }) => {
          const url = new URL(request.url)
          const projectUrl = url.searchParams.get('url')
          return HttpResponse.json(projectUrl === 'new-url')
        })
      )

      render(<ProjectManagement />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByDisplayValue('test-project')).toBeInTheDocument()
      })

      // URL만 변경
      const urlInput = screen.getByPlaceholderText('team-name_project-name')
      await user.clear(urlInput)
      await user.type(urlInput, 'new-url')

      // 제출
      const submitButton = screen.getByRole('button', {
        name: /Save/i,
      })
      await user.click(submitButton)

      // 새 URL로 리다이렉트 확인
      await waitFor(
        () => {
          expect(screen.getByText('Project Detail Page')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })
  })

  describe('취소 기능', () => {
    it('Cancel 버튼을 클릭하면 handleCancel이 호출된다', async () => {
      const user = userEvent.setup()

      render(<ProjectManagement />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByDisplayValue('Test Project')).toBeInTheDocument()
      })

      // 일부 필드 변경
      const titleInput = screen.getByPlaceholderText('Enter your Title')
      await user.clear(titleInput)
      await user.type(titleInput, 'Changed Title')

      // Cancel 버튼 클릭
      const cancelButton = screen.getByRole('button', { name: /Cancel/i })
      await user.click(cancelButton)

      // 폼이 초기화됨 (ProjectForm이 상태를 초기화함)
      await waitFor(() => {
        // Cancel 클릭 후 필드가 비워진 것을 확인
        const titleField = screen.getByPlaceholderText('Enter your Title')
        expect(titleField).toHaveValue('')
      })
    })
  })

  describe('에러 처리', () => {
    it('프로젝트 데이터 로드 실패 시에도 폼이 렌더링된다', async () => {
      // 프로젝트 조회 API 실패
      server.use(
        http.get('/api/v1/projects/:projectUrl', () => {
          return new HttpResponse(null, { status: 404 })
        }),
        http.get(`${API_BASE_URL}/api/v1/projects/:projectUrl`, () => {
          return new HttpResponse(null, { status: 404 })
        })
      )

      render(<ProjectManagement />, { wrapper: createWrapper() })

      // 페이지는 여전히 렌더링됨
      expect(screen.getByText('Project Management')).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /Save/i })).toBeInTheDocument()
    })

    it('프로젝트 수정 API 실패 시 에러 메시지가 표시된다', async () => {
      const user = userEvent.setup()

      // PUT 요청 실패 설정
      server.use(
        http.put('/api/v1/projects/:projectUrl', () => {
          return new HttpResponse(null, { status: 500 })
        }),
        http.put(`${API_BASE_URL}/api/v1/projects/:projectUrl`, () => {
          return new HttpResponse(null, { status: 500 })
        })
      )

      render(<ProjectManagement />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByDisplayValue('Test Project')).toBeInTheDocument()
      })

      // 제출 시도
      const submitButton = screen.getByRole('button', {
        name: /Save/i,
      })
      await user.click(submitButton)

      // 에러 메시지 확인
      await waitFor(() => {
        expect(
          screen.getByText(/Failed to create project. Please try again./i)
        ).toBeInTheDocument()
      })
    })
  })
})
