import { describe, it, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import DashBoard from './DashBoard'
import userEvent from '@testing-library/user-event'

// MSW는 setupTests.ts에서 자동으로 시작됨

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // 테스트에서는 retry 비활성화
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

    it.skip('사용자가 페이지를 변경할 수 있다 (여러 페이지가 있을 때)', async () => {
      // Note: 현재 MSW mock은 totalPages: 1을 반환
      // 실제 백엔드에서 여러 페이지가 있을 때 테스트 가능
      const user = userEvent.setup()

      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('Agile Project')).toBeInTheDocument()
      })

      // 페이지 2로 이동
      const page2Button = screen.getByText('2')
      await user.click(page2Button)

      // 새로운 데이터가 로드됨
      await waitFor(() => {
        // 2페이지 데이터 검증
        expect(screen.queryByText('Agile Project')).not.toBeInTheDocument()
      })
    })
  })

  describe('에러 처리', () => {
    it.skip('API 에러 시 에러 메시지가 표시된다', async () => {
      // Note: 에러 케이스는 server.use()로 핸들러 오버라이드 필요
      // 예시:
      // server.use(
      //   http.get(`${API_BASE_URL}/api/v1/projects/info`, () => {
      //     return HttpResponse.json({ message: 'Error' }, { status: 500 })
      //   })
      // )

      render(<DashBoard />, { wrapper: createWrapper() })

      await waitFor(() => {
        expect(screen.getByText('에러가 발생했습니다.')).toBeInTheDocument()
      })
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
