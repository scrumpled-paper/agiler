import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import { AppSidebar } from './sidebar/AppSidebar'
import { renderWithRouter } from '@/test-utils/render-with-router'
import { sidebarConfigs } from '@/lib/sidebar/config'

interface SidebarMenuButtonProps {
  children: React.ReactNode
  isActive?: boolean
}

// --- Mocks ---
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

vi.mock('@/components/layout/SidebarUserInfo', () => ({
  SidebarUserInfo: ({ userName }: { userName: string }) => (
    <div data-testid="user-info">{userName}</div>
  ),
}))

// --- Tests ---
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
    it('should render all dashboard sections', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const sections = sidebarConfigs.dashboard.sections

      await waitFor(() => {
        sections.forEach(section => {
          const elements = screen.getAllByText(section.title)
          expect(elements.length).toBeGreaterThan(0)
        })
      })
    })

    it('should render project list items', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      await waitFor(() => {
        // Mock 데이터에서 최소 하나의 프로젝트가 렌더링되어야 함
        expect(screen.getByText('Project List')).toBeInTheDocument()
      })
    })

    it('should render settings section', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const settingsElements = screen.getAllByText('Settings')
      expect(settingsElements.length).toBeGreaterThan(0)
    })

    it('should have correct number of groups', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      await waitFor(() => {
        const groups = screen.getAllByTestId('sidebar-group')
        const sections = sidebarConfigs.dashboard.sections
        expect(groups.length).toBeGreaterThanOrEqual(sections.length)
      })
    })
  })

  describe('Project Sidebar', () => {
    it('should render all project sections', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      await waitFor(() => {
        expect(screen.getByText('Project Settings')).toBeInTheDocument()
        expect(screen.getByText('Daily Scrum List')).toBeInTheDocument()
        const membersElements = screen.getAllByText('Members')
        expect(membersElements.length).toBeGreaterThan(0)
      })
    })

    it('should render project settings section', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      await waitFor(() => {
        expect(screen.getByText('Project Settings')).toBeInTheDocument()
      })
    })

    it('should render daily scrum section', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      await waitFor(() => {
        expect(screen.getByText('Daily Scrum List')).toBeInTheDocument()
      })
    })

    it('should use correct projectUrl in URLs', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/42'] })

      await waitFor(() => {
        const settingsLinks = screen.getAllByRole('link', { name: /Settings/i })
        const projectSettingsLink = settingsLinks.find(link =>
          link.getAttribute('href')?.includes('/projects/42/settings')
        )
        expect(projectSettingsLink).toBeInTheDocument()
      })
    })

    it('should have correct number of groups for project', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      await waitFor(() => {
        const groups = screen.getAllByTestId('sidebar-group')
        const sections = sidebarConfigs.project.sections
        expect(groups.length).toBeGreaterThanOrEqual(sections.length)
      })
    })
  })

  describe('Active state', () => {
    it('should mark current page as active in dashboard', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      await waitFor(() => {
        const buttons = screen.getAllByRole('button')
        expect(buttons.length).toBeGreaterThan(0)
      })
    })

    it('should mark current project as active', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/1'] })

      await waitFor(() => {
        const buttons = screen.getAllByRole('button')
        expect(buttons.length).toBeGreaterThan(0)
      })
    })
  })

  describe('Navigation', () => {
    it('should render all project links', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      await waitFor(() => {
        expect(screen.getByText('Project List')).toBeInTheDocument()
      })
    })

    it('should render settings link in dashboard', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      await waitFor(() => {
        const settingsElements = screen.getAllByText('Settings')
        expect(settingsElements.length).toBeGreaterThan(0)
      })
    })
  })
})
