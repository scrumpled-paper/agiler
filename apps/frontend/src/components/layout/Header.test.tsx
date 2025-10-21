import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import { AppHeader } from './Header'
import { renderWithRouter } from '@/test-utils/render-with-router'

// SidebarTrigger 컴포넌트를 간단한 버튼으로 모킹(mocking)합니다.
vi.mock('@/components/ui/sidebar', () => ({
  SidebarTrigger: () => <button data-testid="sidebar-trigger">Toggle</button>,
}))

describe('AppHeader', () => {
  // 테스트: 헤더(<header>) 요소가 정상적으로 렌더링되는지 확인합니다.
  it('should render header element', () => {
    renderWithRouter(<AppHeader />)
    expect(screen.getByRole('banner')).toBeInTheDocument()
  })

  // 테스트: 사이드바를 열고 닫는 트리거 버튼이 렌더링되는지 확인합니다.
  it('should render sidebar trigger', () => {
    renderWithRouter(<AppHeader />)
    expect(screen.getByTestId('sidebar-trigger')).toBeInTheDocument()
  })

  // 테스트: 현재 경로를 나타내는 브레드크럼(<nav>)이 렌더링되는지 확인합니다.
  it('should render breadcrumbs', () => {
    renderWithRouter(<AppHeader />, { initialEntries: ['/dashboard'] })
    expect(screen.getByRole('navigation')).toBeInTheDocument()
  })

  // 테스트: 특정 프로젝트 페이지('/projects/1')에 있을 때, 'Share' 버튼과 같은
  // 프로젝트 관련 액션 버튼들이 표시되는지 확인합니다.
  it('should show project actions on project pages', () => {
    renderWithRouter(<AppHeader />, { initialEntries: ['/projects/1'] })
    expect(screen.getByText('Share')).toBeInTheDocument()
  })

  // 테스트: 대시보드 페이지('/dashboard')에 있을 때는, 'Share' 버튼과 같은
  // 프로젝트 관련 액션 버튼들이 표시되지 않는지 확인합니다.
  it('should not show project actions on dashboard', () => {
    renderWithRouter(<AppHeader />, { initialEntries: ['/dashboard'] })
    expect(screen.queryByText('Share')).not.toBeInTheDocument()
  })
})
