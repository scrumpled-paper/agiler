import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AssigneeSelector } from './AssigneeSelector'
import type { UserInfo } from '@/types'

describe('AssigneeSelector', () => {
  const mockMembers: UserInfo[] = [
    {
      profileId: 1,
      nickname: 'John Doe',
      email: 'john@example.com',
      imageUrl: 'https://example.com/john.jpg',
      role: 'MEMBER',
      description: 'Frontend Developer',
    },
    {
      profileId: 2,
      nickname: 'Jane Smith',
      email: 'jane@example.com',
      imageUrl: '',
      role: 'MEMBER',
      description: 'Backend Developer',
    },
    {
      profileId: 3,
      nickname: 'Bob Johnson',
      email: 'bob@example.com',
      imageUrl: 'https://example.com/bob.jpg',
      role: 'ADMIN',
      description: 'Team Lead',
    },
  ]

  const mockOnAdd = vi.fn()
  const mockOnOpenChange = vi.fn()

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('Plus 버튼을 렌더링한다', () => {
    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    const button = screen.getByRole('button')
    expect(button).toBeInTheDocument()
  })

  it('버튼 클릭 시 멤버 목록을 표시한다', async () => {
    const user = userEvent.setup()

    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    const button = screen.getByRole('button')
    await user.click(button)

    // Popover가 열리면 onOpenChange가 호출되어야 함
    expect(mockOnOpenChange).toHaveBeenCalledWith(true)
  })

  it('isOpen이 true일 때 멤버 목록이 표시된다', () => {
    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.getByText('Jane Smith')).toBeInTheDocument()
    expect(screen.getByText('Bob Johnson')).toBeInTheDocument()
  })

  it('멤버 선택 시 onAdd 콜백이 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    const memberItem = screen.getByText('John Doe')
    await user.click(memberItem)

    expect(mockOnAdd).toHaveBeenCalledWith(mockMembers[0])
  })

  it('이미지가 있는 멤버는 이미지를 표시한다', () => {
    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    const johnImage = screen.getByAltText('John Doe')
    expect(johnImage).toBeInTheDocument()
    expect(johnImage).toHaveAttribute('src', 'https://example.com/john.jpg')
  })

  it('이미지가 없는 멤버는 이니셜을 표시한다', () => {
    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    // Jane Smith의 이니셜 'J'가 표시되어야 함
    expect(screen.getByText('J')).toBeInTheDocument()
  })

  it('검색 입력 필드를 렌더링한다', () => {
    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    const searchInput = screen.getByPlaceholderText('Search member...')
    expect(searchInput).toBeInTheDocument()
  })

  it('멤버가 없을 때 "No member found" 메시지를 표시한다', () => {
    render(
      <AssigneeSelector
        members={[]}
        onAdd={mockOnAdd}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    expect(screen.getByText('No member found.')).toBeInTheDocument()
  })

  it('검색어 입력 시 멤버를 필터링한다', async () => {
    const user = userEvent.setup()

    render(
      <AssigneeSelector
        members={mockMembers}
        onAdd={mockOnAdd}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    const searchInput = screen.getByPlaceholderText('Search member...')
    await user.type(searchInput, 'John')

    // "John Doe"는 표시되어야 하고, 다른 멤버들은 표시되지 않아야 함
    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument()
  })
})
