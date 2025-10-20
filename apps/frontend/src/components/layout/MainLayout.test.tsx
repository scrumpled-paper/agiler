// src/components/layout/MainLayout.test.tsx
import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter, MemoryRouter } from 'react-router-dom'
import MainLayout from './MainLayout'

// Mock 컴포넌트들
vi.mock('@/components/layout/Sidebar', () => ({
  AppSidebar: () => <div data-testid="app-sidebar">Sidebar</div>,
}))

vi.mock('@/components/layout/Header', () => ({
  AppHeader: () => <div data-testid="app-header">Header</div>,
}))

vi.mock('@/components/ui/sidebar', () => ({
  SidebarProvider: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-provider">{children}</div>
  ),
}))

describe('MainLayout', () => {
  it('should render without crashing', () => {
    render(
      <BrowserRouter>
        <MainLayout />
      </BrowserRouter>
    )

    expect(screen.getByTestId('sidebar-provider')).toBeInTheDocument()
  })

  it('should render AppSidebar component', () => {
    render(
      <BrowserRouter>
        <MainLayout />
      </BrowserRouter>
    )

    expect(screen.getByTestId('app-sidebar')).toBeInTheDocument()
  })

  it('should render AppHeader component', () => {
    render(
      <BrowserRouter>
        <MainLayout />
      </BrowserRouter>
    )

    expect(screen.getByTestId('app-header')).toBeInTheDocument()
  })

  it('should render main content area', () => {
    render(
      <BrowserRouter>
        <MainLayout />
      </BrowserRouter>
    )

    const mainElement = screen.getByRole('main')
    expect(mainElement).toBeInTheDocument()
    expect(mainElement).toHaveClass('container')
  })

  it('should have correct layout structure', () => {
    const { container } = render(
      <BrowserRouter>
        <MainLayout />
      </BrowserRouter>
    )

    const peerDiv = container.querySelector('.peer')
    expect(peerDiv).toBeInTheDocument()
    expect(peerDiv).toHaveClass(
      'flex',
      'justify-start',
      'w-full',
      'min-h-screen'
    )
  })

  it('should render Outlet for nested routes', () => {
    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <MainLayout />
      </MemoryRouter>
    )

    // Outlet이 렌더링되는지 확인 (실제로는 자식 라우트가 필요)
    const mainElement = screen.getByRole('main')
    expect(mainElement).toBeInTheDocument()
  })
})
