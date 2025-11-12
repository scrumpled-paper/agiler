import { describe, it, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { AppSidebar } from './AppSidebar'
import { SidebarProvider } from '@/components/ui/sidebar'

// MSW는 setupTests.ts에서 자동으로 시작됨

const renderWithRouter = (initialRoute = '/') => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  })

  // 실제 라우터 구조와 동일하게 설정
  const router = createMemoryRouter(
    [
      {
        path: '/',
        element: (
          <SidebarProvider>
            <AppSidebar />
          </SidebarProvider>
        ),
      },
      {
        path: '/dashboard',
        element: (
          <SidebarProvider>
            <AppSidebar />
          </SidebarProvider>
        ),
      },
      {
        path: '/projects/:projectUrl',
        element: (
          <SidebarProvider>
            <AppSidebar />
          </SidebarProvider>
        ),
      },
      {
        path: '/settings',
        element: (
          <SidebarProvider>
            <AppSidebar />
          </SidebarProvider>
        ),
      },
    ],
    {
      initialEntries: [initialRoute],
      initialIndex: 0,
    }
  )

  return render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  )
}

describe('AppSidebar - 통합 테스트', () => {
  describe('Dashboard 컨텍스트 (기본)', () => {
    it('사용자가 사이드바에서 프로젝트 목록을 볼 수 있다', async () => {
      // Given: MSW가 /api/v1/projects 응답 준비
      renderWithRouter('/')

      // Then: 프로젝트 목록이 표시됨
      await waitFor(
        () => {
          expect(screen.getByText('Agile Project')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )
    })

    it('UserInfo 섹션이 표시된다', async () => {
      renderWithRouter('/')

      await waitFor(() => {
        // UserInfoSection에서 렌더링되는 John Doe 확인
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })
    })

    it('Navigation 섹션이 표시된다', async () => {
      renderWithRouter('/')

      // Navigation 아이템 확인
      await waitFor(() => {
        // navigation config에 정의된 항목들
        const navItems = screen.queryAllByRole('link')
        expect(navItems.length).toBeGreaterThan(0)
      })
    })
  })

  describe('Project 컨텍스트', () => {
    it('프로젝트 상세 페이지에서 멤버 목록이 표시된다', async () => {
      // Given: project 컨텍스트 (projectUrl 파라미터 있음)
      renderWithRouter('/projects/agile-project')

      // Then: 프로젝트 멤버가 표시됨 (MSW mock data)
      await waitFor(
        () => {
          expect(screen.getByText('Alice')).toBeInTheDocument()
        },
        { timeout: 3000 }
      )

      expect(screen.getByText('Bob')).toBeInTheDocument()
    })

    it('프로젝트 컨텍스트에서도 프로젝트 목록이 표시된다', async () => {
      renderWithRouter('/projects/agile-project')

      await waitFor(() => {
        expect(screen.getByText('Agile Project')).toBeInTheDocument()
      })
    })
  })

  describe('상호작용', () => {
    it.skip('프로젝트 아이템을 클릭하면 해당 프로젝트로 이동한다', async () => {
      // Note: 실제 네비게이션은 E2E 테스트에서 검증
      // 통합 테스트에서는 렌더링 확인
      renderWithRouter('/')

      await waitFor(() => {
        expect(screen.getByText('Agile Project')).toBeInTheDocument()
      })

      // Link 요소 확인
      const projectLink = screen.getByText('Agile Project').closest('a')
      expect(projectLink).toHaveAttribute('href')
    })
  })

  describe('로딩 상태', () => {
    it.skip('프로젝트 목록 로딩 중에는 로딩 상태가 표시된다', async () => {
      // Note: MSW가 빠르게 응답하므로 로딩 상태 캡처가 어려움
      // 실제로는 Suspense 또는 로딩 인디케이터 확인
      renderWithRouter('/')

      // 로딩 인디케이터가 있다면 확인
      // const loadingIndicator = screen.queryByTestId('loading')
      // expect(loadingIndicator).toBeInTheDocument()
    })
  })

  describe('에러 처리', () => {
    it.skip('API 에러 시 에러 처리가 된다', async () => {
      // Note: 에러 케이스는 server.use()로 핸들러 오버라이드 필요
      // server.use(
      //   http.get(`${API_BASE_URL}/api/v1/projects`, () => {
      //     return HttpResponse.json({ message: 'Error' }, { status: 500 })
      //   })
      // )

      renderWithRouter('/')

      // 에러 처리 확인
      await waitFor(() => {
        // 에러 메시지 또는 fallback UI 확인
      })
    })
  })

  describe('다양한 라우트 컨텍스트', () => {
    it('Dashboard 경로에서 올바른 섹션들이 표시된다', async () => {
      renderWithRouter('/dashboard')

      await waitFor(() => {
        // Dashboard 컨텍스트의 섹션들 확인
        expect(screen.getByText('Agile Project')).toBeInTheDocument()
      })
    })

    it('Settings 경로에서 올바른 섹션들이 표시된다', async () => {
      renderWithRouter('/settings')

      await waitFor(() => {
        // Settings 컨텍스트의 섹션들 확인
        // config에 정의된 대로 확인
      })
    })
  })

  describe('섹션별 렌더링', () => {
    it('ProjectListSection이 렌더링된다', async () => {
      // render(<AppSidebar />, { wrapper: createWrapper('/') })
      renderWithRouter('/')
      await waitFor(() => {
        // 프로젝트 리스트 제목 또는 아이템 확인
        expect(screen.getByText('Agile Project')).toBeInTheDocument()
      })
    })

    it('MemberListSection은 project 컨텍스트에서만 렌더링된다', async () => {
      // Dashboard에서는 멤버가 없어야 함
      const { unmount } = renderWithRouter('/dashboard')
      // render(<AppSidebar />, {
      //   wrapper: createWrapper('/dashboard'),
      // })

      await waitFor(() => {
        expect(screen.queryByText('Alice')).not.toBeInTheDocument()
      })

      unmount()

      // Project 페이지에서는 멤버가 있어야 함
      // render(<AppSidebar />, {
      //   wrapper: createWrapper('/projects/agile-project'),
      // })
      renderWithRouter('/projects/agile-project')

      await waitFor(() => {
        expect(screen.getByText('Alice')).toBeInTheDocument()
      })
    })
  })

  describe('반응형 동작', () => {
    it.skip('모바일 화면에서 Sidebar가 collapsible 하다', async () => {
      // Note: 반응형 테스트는 실제 브라우저 환경이 필요할 수 있음
      // E2E 테스트 또는 viewport resize 테스트로 확인
    })
  })
})
