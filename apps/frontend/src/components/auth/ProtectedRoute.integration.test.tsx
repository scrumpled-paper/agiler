import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
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

describe('ProtectedRoute - 통합 테스트', () => {
  afterEach(() => {
    server.resetHandlers()
    localStorage.clear()
  })

  describe('인증된 사용자', () => {
    it('인증된 사용자는 보호된 콘텐츠를 볼 수 있다', async () => {
      // Given: MSW가 유효한 사용자 정보를 반환 (handlers.ts의 기본 설정)

      // When: ProtectedRoute로 감싼 컴포넌트를 렌더링
      render(
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>,
        { wrapper: createWrapper() }
      )

      // Then: 로딩 상태가 먼저 표시됨
      expect(screen.getByText('Loading...')).toBeInTheDocument()

      // Then: 인증 후 보호된 콘텐츠가 표시됨
      await waitFor(() => {
        expect(screen.getByText('Protected Content')).toBeInTheDocument()
      })

      // 로그인 페이지로 리다이렉트되지 않음
      expect(screen.queryByText('Login Page')).not.toBeInTheDocument()
    })

    it('DEV 환경에서 localStorage의 mockUser를 사용할 수 있다', async () => {
      // Given: localStorage에 mockUser 설정
      localStorage.setItem(
        'mockUser',
        JSON.stringify({
          id: 999,
          email: 'mock@test.com',
          nickname: 'MockUser',
        })
      )

      // When: ProtectedRoute 렌더링
      render(
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Mock User Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>,
        { wrapper: createWrapper() }
      )

      // Then: mockUser로 인증되어 콘텐츠 표시
      await waitFor(() => {
        expect(screen.getByText('Mock User Content')).toBeInTheDocument()
      })
    })
  })

  describe('인증되지 않은 사용자', () => {
    it('인증되지 않은 사용자는 로그인 페이지로 리다이렉트된다', async () => {
      // Given: API가 401 에러를 반환하도록 설정
      server.use(
        http.get('/api/v1/users', () => {
          return new HttpResponse(null, { status: 401 })
        }),
        http.get(`${API_BASE_URL}/api/v1/users`, () => {
          return new HttpResponse(null, { status: 401 })
        })
      )

      // When: ProtectedRoute 렌더링
      render(
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>,
        { wrapper: createWrapper() }
      )

      // Then: 로딩 후 로그인 페이지로 리다이렉트
      await waitFor(() => {
        expect(screen.getByText('Login Page')).toBeInTheDocument()
      })

      // 보호된 콘텐츠는 표시되지 않음
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
    })

    it('네트워크 에러 시 로그인 페이지로 리다이렉트된다', async () => {
      // Given: API가 네트워크 에러를 반환
      server.use(
        http.get('/api/v1/users', () => HttpResponse.error()),
        http.get(`${API_BASE_URL}/api/v1/users`, () => HttpResponse.error())
      )

      // When: ProtectedRoute 렌더링
      render(
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>,
        { wrapper: createWrapper() }
      )

      // Then: 에러 발생 후 로그인 페이지로 리다이렉트
      await waitFor(() => {
        expect(screen.getByText('Login Page')).toBeInTheDocument()
      })
    })
  })

  // Note: 로딩 상태 테스트는 로딩이 너무 빨라서 통합 테스트에서 캐치하기 어려움
  // ProtectedRoute 컴포넌트의 로딩 UI는 실제 브라우저 환경이나
  // 느린 네트워크 조건에서 확인 가능
})
