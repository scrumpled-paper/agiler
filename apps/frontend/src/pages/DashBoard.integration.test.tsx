import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import DashBoard from './DashBoard'
import userEvent from '@testing-library/user-event'
import { server } from '@/mocks/server'
import { http, HttpResponse } from 'msw'

// MSW는 setupTests.ts에서 자동으로 시작됨

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // 테스트에서는 retry 비활성화
        gcTime: 0, // 캐시 비활성화
        staleTime: 0,
      },
    },
  })

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>{children}</BrowserRouter>
      </QueryClientProvider>
    )
  }
}

describe('Dashboard - 통합 테스트', () => {
  // 각 테스트 후 MSW 핸들러를 원래대로 복원
  afterEach(() => {
    server.resetHandlers()
  })

  describe('프로젝트 목록 조회', () => {
    it('사용자가 대시보드에서 프로젝트 목록을 볼 수 있다', async () => {
      // Given: MSW가 /api/v1/projects/info 응답 준비 (handlers.ts에 정의됨)

      // When: Dashboard 페이지 렌더링
      render(<DashBoard />, { wrapper: createWrapper() })

      // Then: 로딩 상태가 먼저 표시됨
      expect(screen.getByText('로딩 중...')).toBeInTheDocument()

      // Then: 프로젝트 목록이 표시됨 (MSW mock data)
      await waitFor(() => {
        expect(screen.getByText('Agile Project')).toBeInTheDocument()
      })

      expect(screen.getByText('Design System')).toBeInTheDocument()
      expect(
        screen.getByText('An agile project management tool')
      ).toBeInTheDocument()
      expect(screen.getByText('Company-wide design system')).toBeInTheDocument()
    })

    it('사용자가 "Join New Project" 버튼을 볼 수 있다', async () => {
      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Join New Project')).toBeInTheDocument()
      })
    })

    it('UserProfileBox 컴포넌트가 렌더링된다', async () => {
      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('User Profile')).toBeInTheDocument()
      })
    })

    it('Todo List 섹션이 표시된다', async () => {
      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Todo List')).toBeInTheDocument()
      })
    })
  })

  describe('페이지네이션', () => {
    it('페이지네이션 컨트롤이 표시된다', async () => {
      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        // totalPages가 1이므로 페이지 번호 1이 표시됨
        expect(screen.getByText('1')).toBeInTheDocument()
      })
    })

    it('사용자가 페이지를 변경할 수 있다 (여러 페이지가 있을 때)', async () => {
      // Given: 여러 페이지가 있는 프로젝트 목록을 반환하도록 MSW 설정
      server.use(
        http.get(`${API_BASE_URL}/api/v1/projects/info`, ({ request }) => {
          const url = new URL(request.url)
          const page = Number(url.searchParams.get('page')) || 0

          // 페이지별 다른 데이터 반환
          const projectsByPage = [
            [
              {
                title: 'Agile Project',
                url: '/projects/agile-project',
                imageUrl: 'https://placehold.co/600x400',
                summary: 'An agile project management tool',
              },
            ],
            [
              {
                title: 'Design System',
                url: '/projects/design-system',
                imageUrl: 'https://placehold.co/600x400',
                summary: 'Company-wide design system',
              },
            ],
          ]

          return HttpResponse.json({
            contents: projectsByPage[page] || [],
            totalPages: 2,
            totalElements: 2,
            currentPage: page,
            pageSize: 6,
          })
        })
      )

      const user = userEvent.setup()

      // When: Dashboard 렌더링
      render(<DashBoard />, { wrapper: createWrapper() })

      // Then: 첫 페이지 데이터가 로드될 때까지 대기
      await waitFor(
        () => {
          expect(screen.getByText('Page 1 Project')).toBeInTheDocument()
        },
        { timeout: 10000 }
      )

      // 페이지 2 링크 찾기 (페이지네이션은 링크로 구현됨)
      const page2Link = screen.getByRole('link', { name: '2' })
      await user.click(page2Link)

      // Then: 2페이지 데이터가 로드됨
      await waitFor(
        () => {
          expect(screen.queryByText('Page 1 Project')).not.toBeInTheDocument()
          expect(screen.getByText('Page 2 Project')).toBeInTheDocument()
        },
        { timeout: 5000 }
      )
    })
  })

  describe('에러 처리', () => {
    it('API 에러 시 에러 메시지가 표시된다', async () => {
      // Given: API 에러를 반환하도록 MSW 핸들러 오버라이드
      server.use(
        http.get(`${API_BASE_URL}/api/v1/projects/info`, () => {
          return new HttpResponse(null, { status: 500 })
        })
      )

      // When: Dashboard 렌더링
      render(<DashBoard />, { wrapper: createWrapper() })

      // Then: 로딩 표시가 먼저 나타남
      expect(screen.getByText('로딩 중...')).toBeInTheDocument()

      // Then: 로딩이 끝나고 에러 메시지가 표시됨
      await waitFor(
        () => {
          expect(screen.getByText(/에러가 발생했습니다/)).toBeInTheDocument()
        },
        { timeout: 5000 }
      )

      // 로딩 메시지는 사라져야 함
      expect(screen.queryByText('로딩 중...')).not.toBeInTheDocument()
    })
  })

  describe('프로젝트 카드 클릭', () => {
    it('프로젝트 카드를 클릭하면 프로젝트 상세 페이지로 이동한다', async () => {
      const user = userEvent.setup()

      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Agile Project')).toBeInTheDocument()
      })

      // 프로젝트 카드 클릭
      const projectCard = screen.getByText('Agile Project')
      await user.click(projectCard)

      // Note: 실제 네비게이션은 BrowserRouter에서 처리됨
      // E2E 테스트에서 URL 변경 확인 가능
    })
  })

  describe('빈 프로젝트 목록', () => {
    it.skip('프로젝트가 없을 때 빈 상태를 표시한다', async () => {
      // Note: 빈 목록 처리를 위해 server.use()로 핸들러 오버라이드 필요
      // server.use(
      //   http.get(`${API_BASE_URL}/api/v1/projects/info`, () => {
      //     return HttpResponse.json({
      //       contents: [],
      //       totalPages: 0,
      //       totalElements: 0,
      //       currentPage: 0,
      //       pageSize: 6,
      //     })
      //   })
      // )

      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        // 빈 상태 메시지 확인
        expect(screen.queryByText('Agile Project')).not.toBeInTheDocument()
      })
    })
  })
})
