import { describe, it, expect, vi, beforeEach } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import { AppSidebar } from './sidebar/AppSidebar'
import { renderWithRouter } from '@/test-utils/render-with-router'
import { sidebarConfigs } from '@/lib/sidebar/config'
import type { ProjectInfo, ProjectMember } from '@/types'
import type { NavigationSectionProps } from './sidebar/sections/NavigationSection'
import type { NavigationItem } from '@/lib/sidebar/types'
import type { ProjectListSectionProps } from './sidebar/sections/ProjectListSection'
import type { MemberListSectionProps } from './sidebar/sections/MemberListSection'
import type { ActionSectionProps } from './sidebar/sections/ActionSection'

interface SidebarMenuButtonProps {
  children: React.ReactNode
  isActive?: boolean
}

// --- Mock Data ---
const mockProjects: ProjectInfo[] = [
  {
    title: 'Project 1',
    url: '/projects/1',
    imageUrl: 'https://placehold.co/600x400',
    summary: 'Test project 1',
  },
  {
    title: 'Project 2',
    url: '/projects/2',
    imageUrl: 'https://placehold.co/600x400',
    summary: 'Test project 2',
  },
]

const mockMembers: ProjectMember[] = [
  {
    peopleId: 1,
    nickname: 'Alice',
    email: 'alice@example.com',
    imageUrl: 'https://placehold.co/100x100',
    role: 'Developer',
    description: 'Frontend developer',
  },
  {
    peopleId: 2,
    nickname: 'Bob',
    email: 'bob@example.com',
    imageUrl: 'https://placehold.co/100x100',
    role: 'Designer',
    description: 'UI/UX designer',
  },
]

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

vi.mock('./sidebar/sections/UserInfoSection', () => ({
  UserInfoSection: () => <div data-testid="user-info">Test User</div>,
}))

vi.mock('./sidebar/sections/NavigationSection', () => ({
  NavigationSection: ({ section }: NavigationSectionProps) => (
    <div data-testid="navigation-section">
      {section.title && <div>{section.title}</div>}
      {section.items.map((item: NavigationItem, index: number) => (
        <a key={index} href={item.route}>
          {item.label}
        </a>
      ))}
    </div>
  ),
}))

vi.mock('./sidebar/sections/ProjectListSection', () => ({
  ProjectListSection: ({ section, projects }: ProjectListSectionProps) => (
    <div data-testid="project-list-section">
      <div>{section.title}</div>
      {projects?.map((project, index) => (
        <a key={index} href={project.url}>
          {project.title}
        </a>
      ))}
    </div>
  ),
}))

vi.mock('./sidebar/sections/MemberListSection', () => ({
  MemberListSection: ({ section, members }: MemberListSectionProps) => (
    <div data-testid="member-list-section">
      <div>{section.title}</div>
      {members?.map((member, index) => (
        <div key={index}>{member.nickname}</div>
      ))}
    </div>
  ),
}))

vi.mock('./sidebar/sections/ActionSection', () => ({
  ActionSection: ({ section }: ActionSectionProps) => (
    <div data-testid="action-section">
      <div>{section.title}</div>
      <button>{section.action.label}</button>
    </div>
  ),
}))

// Mock hooks
const mockUseSidebarContext = vi.fn()
const mockUseSidebarData = vi.fn()
const mockUseSidebarParams = vi.fn()

vi.mock('@/lib/sidebar/hooks', () => ({
  useSidebarContext: () => mockUseSidebarContext(),
  useSidebarData: () => mockUseSidebarData(),
  useSidebarParams: () => mockUseSidebarParams(),
}))

// --- Tests ---
describe('AppSidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    // Default mock returns
    mockUseSidebarParams.mockReturnValue({ projectUrl: undefined })
    mockUseSidebarData.mockReturnValue({
      projects: mockProjects,
      members: undefined,
    })
  })

  describe('Basic rendering', () => {
    it('should render sidebar', () => {
      mockUseSidebarContext.mockReturnValue('dashboard')
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar')).toBeInTheDocument()
    })

    it('should render sidebar header with user info', () => {
      mockUseSidebarContext.mockReturnValue('dashboard')
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-header')).toBeInTheDocument()
      expect(screen.getByTestId('user-info')).toBeInTheDocument()
    })

    it('should render sidebar content', () => {
      mockUseSidebarContext.mockReturnValue('dashboard')
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-content')).toBeInTheDocument()
    })

    it('should render sidebar rail', () => {
      mockUseSidebarContext.mockReturnValue('dashboard')
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      expect(screen.getByTestId('sidebar-rail')).toBeInTheDocument()
    })
  })

  describe('Dashboard Sidebar', () => {
    beforeEach(() => {
      mockUseSidebarContext.mockReturnValue('dashboard')
      mockUseSidebarData.mockReturnValue({
        projects: mockProjects,
        members: undefined,
      })
    })

    it('should render all dashboard sections', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      await waitFor(() => {
        expect(screen.getByText('Project List')).toBeInTheDocument()
        expect(screen.getByText('Settings')).toBeInTheDocument()
      })
    })

    it('should render project list items', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      await waitFor(() => {
        expect(screen.getByTestId('project-list-section')).toBeInTheDocument()
        expect(screen.getByText('Project 1')).toBeInTheDocument()
        expect(screen.getByText('Project 2')).toBeInTheDocument()
      })
    })

    it('should have correct number of sections', () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })
      const sections = sidebarConfigs.dashboard.sections

      // Navigation sections + project list section
      const navigationSections = screen.getAllByTestId('navigation-section')
      const projectListSections = screen.getAllByTestId('project-list-section')

      expect(navigationSections.length + projectListSections.length).toBe(
        sections.length
      )
    })
  })

  describe('Project Sidebar', () => {
    beforeEach(() => {
      mockUseSidebarContext.mockReturnValue('project')
      mockUseSidebarParams.mockReturnValue({ projectUrl: '42' })
      mockUseSidebarData.mockReturnValue({
        projects: mockProjects,
        members: mockMembers,
      })
    })

    it('should render all project sections', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/42'] })

      await waitFor(() => {
        expect(screen.getByText('Project List')).toBeInTheDocument()
        // Both ActionSection and MemberListSection have "Members" as title
        const membersElements = screen.getAllByText('Members')
        expect(membersElements.length).toBeGreaterThan(0)
      })
    })

    it('should render member list with members', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/42'] })

      await waitFor(() => {
        expect(screen.getByTestId('member-list-section')).toBeInTheDocument()
        expect(screen.getByText('Alice')).toBeInTheDocument()
        expect(screen.getByText('Bob')).toBeInTheDocument()
      })
    })

    it('should render action section', async () => {
      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/42'] })

      await waitFor(() => {
        const actionSection = screen.getByTestId('action-section')
        expect(actionSection).toBeInTheDocument()
        expect(screen.getByText('프로젝트 참가 링크 생성')).toBeInTheDocument()
      })
    })
  })

  describe('Project Settings Sidebar', () => {
    beforeEach(() => {
      mockUseSidebarContext.mockReturnValue('project-settings')
      mockUseSidebarParams.mockReturnValue({ projectUrl: '42' })
      mockUseSidebarData.mockReturnValue({
        projects: undefined,
        members: undefined,
      })
    })

    it('should render project settings sections', async () => {
      renderWithRouter(<AppSidebar />, {
        initialEntries: ['/projects/42/settings'],
      })

      await waitFor(() => {
        expect(screen.getByText('Project Settings')).toBeInTheDocument()
      })
    })

    it('should render all settings navigation items', async () => {
      renderWithRouter(<AppSidebar />, {
        initialEntries: ['/projects/42/settings'],
      })

      await waitFor(() => {
        expect(screen.getByText('User Profile')).toBeInTheDocument()
        expect(screen.getByText('Project Management')).toBeInTheDocument()
        expect(screen.getByText('Database Management')).toBeInTheDocument()
      })
    })
  })

  describe('Data Loading', () => {
    it('should handle empty project list', () => {
      mockUseSidebarContext.mockReturnValue('dashboard')
      mockUseSidebarData.mockReturnValue({
        projects: [],
        members: undefined,
      })

      renderWithRouter(<AppSidebar />, { initialEntries: ['/dashboard'] })

      expect(screen.getByTestId('project-list-section')).toBeInTheDocument()
    })

    it('should handle empty member list', () => {
      mockUseSidebarContext.mockReturnValue('project')
      mockUseSidebarParams.mockReturnValue({ projectUrl: '42' })
      mockUseSidebarData.mockReturnValue({
        projects: mockProjects,
        members: [],
      })

      renderWithRouter(<AppSidebar />, { initialEntries: ['/projects/42'] })

      expect(screen.getByTestId('member-list-section')).toBeInTheDocument()
    })
  })

  describe('Context Switching', () => {
    it('should switch from dashboard to project context', async () => {
      mockUseSidebarContext.mockReturnValue('dashboard')
      const { rerender } = renderWithRouter(<AppSidebar />, {
        initialEntries: ['/dashboard'],
      })

      expect(screen.getByText('Project List')).toBeInTheDocument()

      // Switch context
      mockUseSidebarContext.mockReturnValue('project')
      mockUseSidebarParams.mockReturnValue({ projectUrl: '42' })
      mockUseSidebarData.mockReturnValue({
        projects: mockProjects,
        members: mockMembers,
      })

      rerender(<AppSidebar />)

      await waitFor(() => {
        expect(screen.getByTestId('member-list-section')).toBeInTheDocument()
      })
    })
  })
})
