import { describe, it, expect, vi } from 'vitest'
import { screen } from '@testing-library/react'
import { MemberListSection } from './MemberListSection'
import { renderWithRouter } from '@/test-utils/render-with-router'
import type { DisplaySection } from '@/lib/sidebar/types'
import type { ProjectMember } from '@/types'

// Mock UI components
vi.mock('@/components/ui/sidebar', () => ({
  SidebarGroup: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group">{children}</div>
  ),
  SidebarGroupLabel: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group-label">{children}</div>
  ),
  SidebarGroupContent: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group-content">{children}</div>
  ),
}))

vi.mock('../items/DisplayItem', () => ({
  DisplayItem: ({ member }: { member: ProjectMember }) => (
    <div data-testid={`display-item-${member.peopleId}`}>{member.nickname}</div>
  ),
}))

vi.mock('../buttons/ShowMoreButton', () => ({
  ShowMoreButton: ({ to, label }: { to: string; label: string }) => (
    <a href={to} data-testid="show-more-button">
      {label}
    </a>
  ),
}))

describe('MemberListSection', () => {
  const mockSection: DisplaySection = {
    type: 'display',
    title: 'Members',
    icon: '👥',
    dataKey: 'members',
    hasShowMore: true,
    showMoreRoute: ':projectUrl/settings/members',
  }

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
    {
      peopleId: 3,
      nickname: 'Charlie',
      email: 'charlie@example.com',
      imageUrl: 'https://placehold.co/100x100',
      role: 'Backend',
      description: 'Backend developer',
    },
    {
      peopleId: 4,
      nickname: 'David',
      email: 'david@example.com',
      imageUrl: 'https://placehold.co/100x100',
      role: 'QA',
      description: 'Quality Assurance',
    },
  ]

  it('should render section title with icon', () => {
    renderWithRouter(
      <MemberListSection section={mockSection} members={mockMembers} />,
      { initialEntries: ['/projects/42'] }
    )

    const label = screen.getByTestId('sidebar-group-label')
    expect(label).toHaveTextContent('👥')
    expect(label).toHaveTextContent('Members')
  })

  it('should render section without icon when not provided', () => {
    const sectionWithoutIcon = { ...mockSection, icon: undefined }
    renderWithRouter(
      <MemberListSection section={sectionWithoutIcon} members={mockMembers} />,
      { initialEntries: ['/projects/42'] }
    )

    const label = screen.getByTestId('sidebar-group-label')
    expect(label).toHaveTextContent('Members')
    expect(label).not.toHaveTextContent('👥')
  })

  it('should display maximum of 3 members', () => {
    renderWithRouter(
      <MemberListSection section={mockSection} members={mockMembers} />,
      { initialEntries: ['/projects/42'] }
    )

    expect(screen.getByTestId('display-item-1')).toBeInTheDocument()
    expect(screen.getByTestId('display-item-2')).toBeInTheDocument()
    expect(screen.getByTestId('display-item-3')).toBeInTheDocument()
    expect(screen.queryByTestId('display-item-4')).not.toBeInTheDocument()
  })

  it('should show "더보기" button when more than 3 members', () => {
    renderWithRouter(
      <MemberListSection section={mockSection} members={mockMembers} />,
      { initialEntries: ['/projects/42'] }
    )

    const showMoreButton = screen.getByTestId('show-more-button')
    expect(showMoreButton).toBeInTheDocument()
    expect(showMoreButton).toHaveTextContent('더보기')
    expect(showMoreButton).toHaveAttribute(
      'href',
      '/projects/42/settings/members'
    )
  })

  it('should not show "더보기" button when 3 or fewer members', () => {
    const fewMembers = mockMembers.slice(0, 3)
    renderWithRouter(
      <MemberListSection section={mockSection} members={fewMembers} />,
      { initialEntries: ['/projects/42'] }
    )

    expect(screen.queryByTestId('show-more-button')).not.toBeInTheDocument()
  })

  it('should not show "더보기" button when hasShowMore is false', () => {
    const sectionWithoutShowMore = { ...mockSection, hasShowMore: false }
    renderWithRouter(
      <MemberListSection
        section={sectionWithoutShowMore}
        members={mockMembers}
      />,
      { initialEntries: ['/projects/42'] }
    )

    expect(screen.queryByTestId('show-more-button')).not.toBeInTheDocument()
  })

  it('should resolve route with projectUrl parameter', () => {
    renderWithRouter(
      <MemberListSection section={mockSection} members={mockMembers} />,
      { initialEntries: ['/projects/test-project'] }
    )

    const showMoreButton = screen.getByTestId('show-more-button')
    expect(showMoreButton).toHaveAttribute(
      'href',
      '/projects/test-project/settings/members'
    )
  })

  it('should use fallback route when showMoreRoute is not provided', () => {
    const sectionWithoutRoute = { ...mockSection, showMoreRoute: undefined }
    renderWithRouter(
      <MemberListSection section={sectionWithoutRoute} members={mockMembers} />,
      { initialEntries: ['/projects/42'] }
    )

    // Should not render show more button without a route
    expect(screen.queryByTestId('show-more-button')).not.toBeInTheDocument()
  })

  it('should show loading message when members array is empty', () => {
    renderWithRouter(<MemberListSection section={mockSection} members={[]} />, {
      initialEntries: ['/projects/42'],
    })

    expect(screen.getByText('멤버를 불러오는 중...')).toBeInTheDocument()
  })

  it('should handle undefined members prop with default empty array', () => {
    renderWithRouter(<MemberListSection section={mockSection} />, {
      initialEntries: ['/projects/42'],
    })

    expect(screen.getByText('멤버를 불러오는 중...')).toBeInTheDocument()
  })

  it('should render members with correct nicknames', () => {
    const twoMembers = mockMembers.slice(0, 2)
    renderWithRouter(
      <MemberListSection section={mockSection} members={twoMembers} />,
      { initialEntries: ['/projects/42'] }
    )

    expect(screen.getByText('Alice')).toBeInTheDocument()
    expect(screen.getByText('Bob')).toBeInTheDocument()
  })

  it('should resolve route without projectUrl in params', () => {
    const sectionWithNormalRoute = {
      ...mockSection,
      showMoreRoute: '/dashboard/members',
    }
    renderWithRouter(
      <MemberListSection
        section={sectionWithNormalRoute}
        members={mockMembers}
      />,
      { initialEntries: ['/dashboard'] }
    )

    const showMoreButton = screen.getByTestId('show-more-button')
    expect(showMoreButton).toHaveAttribute('href', '/dashboard/members')
  })
})
