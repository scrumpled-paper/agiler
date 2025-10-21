import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import { AppSidebar } from './Sidebar'
import { renderWithRouter } from '@/test-utils/render-with-router'
import { getDashboardSidebar, getProjectSidebar } from '@/lib/sidebar-config'

interface SidebarMenuButtonProps {
  children: React.ReactNode
  isActive?: boolean
}

// --- Mocks ---
// AppSidebar가 사용하는 UI 프리미티브 컴포넌트들을 간단한 태그로 모킹합니다.
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

// 사용자 정보를 표시하는 자식 컴포넌트를 모킹합니다.
vi.mock('@/components/layout/SidbarUserInfo', () => ({
  SidebarUserInfo: ({ userName }: { userName: string }) => (
    <div data-testid="user-info">{userName}</div>
  ),
}))

// --- Tests ---
describe('AppSidebar', () => {
  // 섹션: 기본적인 렌더링을 확인합니다.
  describe('Basic rendering', () => {
    // 테스트: 사이드바의 기본 뼈대(<aside>)가 렌더링되는지 확인합니다.
    it('should render sidebar', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar')).toBeInTheDocument()
    })

    // 테스트: 사이드바 헤더와 그 안의 유저 정보가 렌더링되는지 확인합니다.
    it('should render sidebar header with user info', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-header')).toBeInTheDocument()
      expect(screen.getByTestId('user-info')).toBeInTheDocument()
      expect(screen.getByText('user1')).toBeInTheDocument()
    })

    // 테스트: 메뉴 항목들이 들어갈 메인 콘텐츠 영역이 렌더링되는지 확인합니다.
    it('should render sidebar content', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-content')).toBeInTheDocument()
    })

    // 테스트: 사이드바를 접고 펴는 데 사용되는 'rail' 영역이 렌더링되는지 확인합니다.
    it('should render sidebar rail', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-rail')).toBeInTheDocument()
    })
  })

  // 섹션: 대시보드 관련 경로에 있을 때의 사이드바 동작을 확인합니다.
  describe('Dashboard Sidebar', () => {
    // 테스트: 대시보드 사이드바의 모든 섹션 제목들이 렌더링되는지 확인합니다.
    it('should render all dashboard sections', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const sections = getDashboardSidebar()
      sections.forEach(section => {
        expect(screen.getByText(section.title)).toBeInTheDocument()
      })
    })

    // 테스트: 프로젝트 목록 아이템들이 정상적으로 렌더링되는지 확인합니다.
    it('should render project list items', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByText('Project 1')).toBeInTheDocument()
      expect(screen.getByText('Project 2')).toBeInTheDocument()
    })

    // 테스트: 'Settings' 섹션과 그 하위 메뉴가 렌더링되는지 확인합니다.
    it('should render settings section', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByText('Settings')).toBeInTheDocument()
      expect(screen.getByText('General Settings')).toBeInTheDocument()
    })

    // 테스트: 렌더링된 그룹(섹션)의 수가 설정 파일의 섹션 수와 일치하는지 확인합니다.
    it('should have correct number of groups', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const groups = screen.getAllByTestId('sidebar-group')
      const sections = getDashboardSidebar()
      expect(groups).toHaveLength(sections.length)
    })
  })

  // 섹션: 특정 프로젝트 관련 경로에 있을 때의 사이드바 동작을 확인합니다.
  describe('Project Sidebar', () => {
    // 테스트: 프로젝트 사이드바의 모든 섹션 제목들이 렌더링되는지 확인합니다.
    it('should render all project sections', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })
      const sections = getProjectSidebar('1')
      sections.forEach(section => {
        expect(screen.getByText(section.title)).toBeInTheDocument()
      })
    })

    // 테스트: 'Project Settings' 섹션이 렌더링되는지 확인합니다.
    it('should render project settings section', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })
      expect(screen.getByText('Project Settings')).toBeInTheDocument()
      expect(screen.getByText('Settings')).toBeInTheDocument()
    })

    // 테스트: 'Daily Scrum' 섹션이 렌더링되는지 확인합니다.
    it('should render daily scrum section', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })
      expect(screen.getByText('Daily Scrum')).toBeInTheDocument()
      expect(screen.getByText('Scrum List')).toBeInTheDocument()
    })

    // 테스트: URL의 projectId가 메뉴 링크에 동적으로 올바르게 반영되는지 확인합니다.
    it('should use correct projectId in URLs', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/42'] })
      const settingsLink = screen.getByRole('link', { name: 'Settings' })
      expect(settingsLink).toHaveAttribute('href', '/projects/42/settings')
      const scrumLink = screen.getByRole('link', { name: 'Scrum List' })
      expect(scrumLink).toHaveAttribute('href', '/projects/42/daily-scrum')
    })

    // 테스트: 프로젝트 페이지에서 렌더링된 그룹 수가 설정과 일치하는지 확인합니다.
    it('should have correct number of groups for project', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })
      const groups = screen.getAllByTestId('sidebar-group')
      const sections = getProjectSidebar('1')
      expect(groups).toHaveLength(sections.length)
    })
  })

  // 섹션: 현재 경로에 따라 메뉴가 '활성화' 상태가 되는지 확인합니다.
  describe('Active state', () => {
    // 테스트: 대시보드 경로에서 메뉴들의 활성/비활성 상태를 확인합니다.
    it('should mark current page as active in dashboard', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const buttons = screen.getAllByRole('button')
      const inactiveButtons = buttons.filter(
        btn => btn.getAttribute('data-active') === 'false'
      )
      expect(inactiveButtons.length).toBeGreaterThan(0)
    })

    // 테스트: 현재 접속한 프로젝트 페이지의 메뉴가 '활성화' 상태인지 확인합니다.
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

  // 섹션: 메뉴 항목들이 올바른 URL(href)로 연결되는지 확인합니다.
  describe('Navigation', () => {
    // 테스트: 프로젝트 목록의 링크들이 올바른 URL을 가지고 있는지 확인합니다.
    it('should render all project links', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const project1Link = screen.getByRole('link', { name: 'Project 1' })
      const project2Link = screen.getByRole('link', { name: 'Project 2' })
      expect(project1Link).toHaveAttribute('href', '/projects/1')
      expect(project2Link).toHaveAttribute('href', '/projects/2')
    })

    // 테스트: 대시보드의 'General Settings' 링크가 올바른 URL을 가지고 있는지 확인합니다.
    it('should render settings link in dashboard', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const settingsLink = screen.getByRole('link', {
        name: 'General Settings',
      })
      expect(settingsLink).toHaveAttribute('href', '/dashboard/settings')
    })
  })
})
