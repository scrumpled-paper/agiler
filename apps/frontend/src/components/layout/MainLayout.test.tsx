import { describe, it, expect, vi } from 'vitest'
import { render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import MainLayout from './MainLayout'

vi.mock('@/components/layout/Sidebar', () => ({
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
    return render(
      <MemoryRouter>
        <MainLayout />
      </MemoryRouter>
    )
  }

  it('사이드바, 헤더, 메인 콘텐츠 영역을 모두 렌더링해야 한다', () => {
    setup()
    expect(screen.getByTestId('app-sidebar')).toBeInTheDocument()
    expect(screen.getByTestId('app-header')).toBeInTheDocument()
    expect(screen.getByRole('main')).toBeInTheDocument()
  })

  it('올바른 레이아웃 구조와 스타일을 가져야 한다', () => {
    setup()

    // 1. 최상위 flex 컨테이너를 찾고 테스트.
    const container = screen.getByTestId('main-layout-container') // MainLayout.tsx에 data-testid 추가 필요
    expect(container).toBeInTheDocument()
    expect(container).toHaveClass('flex min-h-screen w-full justify-start')

    // 2. 그 컨테이너 내부에 peer 클래스를 가진 사이드바가 있는지 확인합니다.
    const sidebar = within(container).getByTestId('app-sidebar')
    expect(sidebar).toHaveClass('peer')
  })
})
