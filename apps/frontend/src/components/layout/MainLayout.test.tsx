import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import MainLayout from './MainLayout'

vi.mock('@/components/layout/sidebar/AppSidebar', () => ({
  AppSidebar: () => (
    <aside className="peer" data-testid="app-sidebar">
      Sidebar
    </aside>
  ),
}))

vi.mock('@/components/layout/Header', () => ({
  AppHeader: () => <header data-testid="app-header">Header</header>,
}))

vi.mock('@/components/ui/sidebar', () => ({
  SidebarProvider: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-provider">{children}</div>
  ),
}))

describe('MainLayout', () => {
  // 렌더링을 돕는 헬퍼 함수
  const setup = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    })

    return render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <MainLayout />
        </MemoryRouter>
      </QueryClientProvider>
    )
  }

  it('사이드바, 헤더, 메인 콘텐츠 영역을 모두 렌더링해야 한다', () => {
    setup()
    expect(screen.getByTestId('app-sidebar')).toBeInTheDocument()
    expect(screen.getByTestId('app-header')).toBeInTheDocument()
    expect(screen.getByRole('main')).toBeInTheDocument()
  })

  it('올바른 레이아웃 구조를 가져야 한다', () => {
    setup()

    // SidebarProvider 내부에 사이드바와 메인 콘텐츠가 있는지 확인
    const provider = screen.getByTestId('sidebar-provider')
    expect(provider).toBeInTheDocument()

    // 사이드바가 peer 클래스를 가지고 있는지 확인
    const sidebar = screen.getByTestId('app-sidebar')
    expect(sidebar).toHaveClass('peer')

    // 메인 콘텐츠 영역이 올바른 flex 구조를 가지는지 확인
    const main = screen.getByRole('main')
    expect(main).toHaveClass('container', 'h-full', 'w-full', 'flex-1', 'p-4')
  })
})
