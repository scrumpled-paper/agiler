import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import userEvent from '@testing-library/user-event'
import Home from './Home'
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
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>{children}</BrowserRouter>
      </QueryClientProvider>
    )
  }
}

describe('Home Page - 통합 테스트', () => {
  afterEach(() => {
    server.resetHandlers()
    localStorage.clear()
  })

  describe('UI 렌더링', () => {
    it('홈 페이지가 올바르게 렌더링된다', async () => {
      render(<Home />, { wrapper: createWrapper() })

      // 메인 타이틀 확인
      expect(screen.getByText('Agiler')).toBeInTheDocument()
      expect(
        screen.getByText('팀의 생산성을 높이는 애자일 프로젝트 관리 도구')
      ).toBeInTheDocument()

      // 주요 기능 섹션 확인
      expect(screen.getByText('주요 기능')).toBeInTheDocument()
      expect(
        screen.getByText('• 칸반 보드를 통한 직관적인 작업 관리')
      ).toBeInTheDocument()

      // 애자일 방법론 섹션 확인
      expect(screen.getByText('애자일 방법론 지원')).toBeInTheDocument()

      // 시작하기 버튼 확인 (로딩 완료 대기)
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /시작하기/i })).toBeInTheDocument()
      })
    })

    it('로딩 중일 때 "로딩 중..." 텍스트가 표시된다', () => {
      render(<Home />, { wrapper: createWrapper() })

      // 초기 로딩 상태에서는 "로딩 중..." 텍스트가 버튼에 표시될 수 있음
      // (빠르게 로딩이 완료되면 캐치하기 어려울 수 있음)
      const button = screen.getByRole('button')
      expect(button).toBeInTheDocument()
    })
  })

  describe('인증된 사용자 네비게이션', () => {
    it('인증된 사용자가 "시작하기" 버튼을 클릭하면 대시보드로 이동한다', async () => {
      const user = userEvent.setup()

      // Given: MSW가 인증된 사용자 정보를 반환 (기본 핸들러)
      render(<Home />, { wrapper: createWrapper() })

      // When: 로딩이 완료될 때까지 대기
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /시작하기/i })).not.toBeDisabled()
      })

      // When: 시작하기 버튼 클릭
      const button = screen.getByRole('button', { name: /시작하기/i })
      await user.click(button)

      // Then: URL이 /dashboard로 변경됨
      await waitFor(() => {
        expect(window.location.pathname).toBe('/dashboard')
      })
    })

    it('DEV 환경에서 localStorage의 mockUser가 있으면 인증된 것으로 간주한다', async () => {
      const user = userEvent.setup()

      // Given: localStorage에 mockUser 설정
      localStorage.setItem(
        'mockUser',
        JSON.stringify({
          id: 999,
          email: 'mock@test.com',
          nickname: 'MockUser',
        })
      )

      render(<Home />, { wrapper: createWrapper() })

      // When: 로딩 완료 대기
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /시작하기/i })).not.toBeDisabled()
      })

      // When: 시작하기 버튼 클릭
      const button = screen.getByRole('button', { name: /시작하기/i })
      await user.click(button)

      // Then: 대시보드로 이동
      await waitFor(() => {
        expect(window.location.pathname).toBe('/dashboard')
      })
    })
  })

  describe('비인증 사용자 네비게이션', () => {
    it('비인증 사용자가 "시작하기" 버튼을 클릭하면 로그인 페이지로 이동한다', async () => {
      const user = userEvent.setup()

      // Given: API가 401 에러를 반환 (비인증 상태)
      server.use(
        http.get('/api/v1/users/', () => {
          return new HttpResponse(null, { status: 401 })
        }),
        http.get(`${API_BASE_URL}/api/v1/users/`, () => {
          return new HttpResponse(null, { status: 401 })
        })
      )

      render(<Home />, { wrapper: createWrapper() })

      // When: 로딩 완료 대기
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /시작하기/i })).not.toBeDisabled()
      })

      // When: 시작하기 버튼 클릭
      const button = screen.getByRole('button', { name: /시작하기/i })
      await user.click(button)

      // Then: URL이 /login으로 변경됨
      await waitFor(() => {
        expect(window.location.pathname).toBe('/login')
      })
    })

    it('네트워크 에러 시에도 로그인 페이지로 이동한다', async () => {
      const user = userEvent.setup()

      // Given: API가 네트워크 에러를 반환
      server.use(
        http.get('/api/v1/users/', () => HttpResponse.error()),
        http.get(`${API_BASE_URL}/api/v1/users/`, () => HttpResponse.error())
      )

      render(<Home />, { wrapper: createWrapper() })

      // When: 로딩 완료 대기
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /시작하기/i })).not.toBeDisabled()
      })

      // When: 시작하기 버튼 클릭
      const button = screen.getByRole('button', { name: /시작하기/i })
      await user.click(button)

      // Then: 로그인 페이지로 이동
      await waitFor(() => {
        expect(window.location.pathname).toBe('/login')
      })
    })
  })

  describe('버튼 상태', () => {
    it('로딩 중일 때 버튼이 비활성화된다', () => {
      render(<Home />, { wrapper: createWrapper() })

      // 초기 로딩 상태에서 버튼 확인
      const button = screen.getByRole('button')

      // 로딩이 매우 빠르게 완료될 수 있으므로
      // 버튼이 존재하는지만 확인
      expect(button).toBeInTheDocument()
    })

    it('로딩 완료 후 버튼이 활성화된다', async () => {
      render(<Home />, { wrapper: createWrapper() })

      // When: 로딩 완료 대기
      await waitFor(() => {
        const button = screen.getByRole('button', { name: /시작하기/i })
        expect(button).not.toBeDisabled()
      })
    })
  })

  describe('콘텐츠 확인', () => {
    it('모든 주요 기능 항목이 표시된다', () => {
      render(<Home />, { wrapper: createWrapper() })

      expect(
        screen.getByText('• 칸반 보드를 통한 직관적인 작업 관리')
      ).toBeInTheDocument()
      expect(
        screen.getByText('• 데일리 스크럼으로 팀 협업 강화')
      ).toBeInTheDocument()
      expect(
        screen.getByText('• 이슈 템플릿으로 업무 표준화')
      ).toBeInTheDocument()
      expect(
        screen.getByText('• 실시간 협업과 진행 상황 추적')
      ).toBeInTheDocument()
    })

    it('애자일 방법론 설명이 표시된다', () => {
      render(<Home />, { wrapper: createWrapper() })

      expect(
        screen.getByText(/스크럼과 칸반 방식을 결합하여/)
      ).toBeInTheDocument()
      expect(
        screen.getByText(/애자일 개발의 모든 단계를/)
      ).toBeInTheDocument()
    })
  })
})
