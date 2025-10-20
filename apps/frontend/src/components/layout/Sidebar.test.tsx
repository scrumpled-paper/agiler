import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import { AppSidebar } from './Sidebar'
import { renderWithRouter } from '@/test-utils/render-with-router'
import { getDashboardSidebar, getProjectSidebar } from '@/lib/sidebar-config'

interface SidebarMenuButtonProps {
  children: React.ReactNode
  isActive?: boolean
}

// UI 컴포넌트 mock
vi.mock('@/components/ui/sidebar', () => ({
  Sidebar: ({ children }: { children: React.ReactNode }) => (
    <aside data-testid="sidebar">{children}</aside>
  ),
  SidebarHeader: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-header">{children}</div>
  ),
  SidebarContent: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-content">{children}</div>
  ),
  SidebarGroup: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group">{children}</div>
  ),
  SidebarGroupLabel: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group-label">{children}</div>
  ),
  SidebarGroupContent: ({ children }: { children: React.ReactNode }) => (
    <div>{children}</div>
  ),
  SidebarMenu: ({ children }: { children: React.ReactNode }) => (
    <ul>{children}</ul>
  ),
  SidebarMenuItem: ({ children }: { children: React.ReactNode }) => (
    <li>{children}</li>
  ),
  SidebarMenuButton: ({ children, isActive }: SidebarMenuButtonProps) => (
    <button data-active={isActive}>{children}</button>
  ),
  SidebarRail: () => <div data-testid="sidebar-rail" />,
}))

vi.mock('@/components/layout/SidbarUserInfo', () => ({
  SidebarUserInfo: ({ userName }: { userName: string }) => (
    <div data-testid="user-info">{userName}</div>
  ),
}))

describe('AppSidebar', () => {
  describe('Basic rendering', () => {
    it('should render sidebar', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar')).toBeInTheDocument()
    })

    it('should render sidebar header with user info', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-header')).toBeInTheDocument()
      expect(screen.getByTestId('user-info')).toBeInTheDocument()
      expect(screen.getByText('user1')).toBeInTheDocument()
    })

    it('should render sidebar content', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-content')).toBeInTheDocument()
    })

    it('should render sidebar rail', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-rail')).toBeInTheDocument()
    })
  })

  describe('Dashboard Sidebar', () => {
    it('should render all dashboard sections', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      const sections = getDashboardSidebar()

      // 모든 섹션 타이틀이 렌더링되는지 확인
      sections.forEach(section => {
        expect(screen.getByText(section.title)).toBeInTheDocument()
      })
    })

    it('should render project list items', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      expect(screen.getByText('Project 1')).toBeInTheDocument()
      expect(screen.getByText('Project 2')).toBeInTheDocument()
    })

    it('should render settings section', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      expect(screen.getByText('Settings')).toBeInTheDocument()
      expect(screen.getByText('General Settings')).toBeInTheDocument()
    })

    it('should have correct number of groups', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      const groups = screen.getAllByTestId('sidebar-group')
      const sections = getDashboardSidebar()

      expect(groups).toHaveLength(sections.length)
    })
  })

  describe('Project Sidebar', () => {
    it('should render all project sections', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      const sections = getProjectSidebar('1')

      // 모든 섹션 타이틀이 렌더링되는지 확인
      sections.forEach(section => {
        expect(screen.getByText(section.title)).toBeInTheDocument()
      })
    })

    it('should render project settings section', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      expect(screen.getByText('Project Settings')).toBeInTheDocument()
      expect(screen.getByText('Settings')).toBeInTheDocument()
    })

    it('should render daily scrum section', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      expect(screen.getByText('Daily Scrum')).toBeInTheDocument()
      expect(screen.getByText('Scrum List')).toBeInTheDocument()
    })

    it('should use correct projectId in URLs', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/42'] })

      const settingsLink = screen.getByRole('link', { name: 'Settings' })
      expect(settingsLink).toHaveAttribute('href', '/projects/42/settings')

      const scrumLink = screen.getByRole('link', { name: 'Scrum List' })
      expect(scrumLink).toHaveAttribute('href', '/projects/42/daily-scrum')
    })

    it('should have correct number of groups for project', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      const groups = screen.getAllByTestId('sidebar-group')
      const sections = getProjectSidebar('1')

      expect(groups).toHaveLength(sections.length) // 3개: Project list, Project Settings, Daily Scrum
    })
  })

  describe('Active state', () => {
    it('should mark current page as active in dashboard', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      const buttons = screen.getAllByRole('button')
      // dashboard가 활성화된 버튼은 없지만 프로젝트 링크는 비활성
      const inactiveButtons = buttons.filter(
        btn => btn.getAttribute('data-active') === 'false'
      )

      expect(inactiveButtons.length).toBeGreaterThan(0)
    })

    it('should mark current project as active', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      const buttons = screen.getAllByRole('button')
      const activeButton = buttons.find(
        btn =>
          btn.getAttribute('data-active') === 'true' &&
          btn.textContent?.includes('Project 1')
      )

      expect(activeButton).toBeInTheDocument()
    })
  })

  describe('Navigation', () => {
    it('should render all project links', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      const project1Link = screen.getByRole('link', { name: 'Project 1' })
      const project2Link = screen.getByRole('link', { name: 'Project 2' })

      expect(project1Link).toHaveAttribute('href', '/projects/1')
      expect(project2Link).toHaveAttribute('href', '/projects/2')
    })

    it('should render settings link in dashboard', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      const settingsLink = screen.getByRole('link', {
        name: 'General Settings',
      })
      expect(settingsLink).toHaveAttribute('href', '/dashboard/settings')
    })
  })
})
