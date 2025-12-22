import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { SelectedAssignees } from './SelectedAssignees'
import type { ProjectMember } from '@/types'
import { afterEach } from 'node:test'

describe('SelectedAssignees', () => {
  const mockAssignees: ProjectMember[] = [
    {
      peopleId: 1,
      nickname: 'John Doe',
      email: 'john@example.com',
      imageUrl: 'https://example.com/john.jpg',
      role: 'MEMBER',
      description: 'Frontend Developer',
    },
    {
      peopleId: 2,
      nickname: 'Jane Smith',
      email: 'jane@example.com',
      imageUrl: '',
      role: 'MEMBER',
      description: 'Backend Developer',
    },
  ]

  const mockOnRemove = vi.fn()

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('assignees가 비어있으면 아무것도 렌더링하지 않는다', () => {
    const { container } = render(
      <SelectedAssignees assignees={[]} onRemove={mockOnRemove} />
    )

    expect(container.firstChild).toBeNull()
  })

  it('선택된 모든 assignee를 렌더링한다', () => {
    render(
      <SelectedAssignees assignees={mockAssignees} onRemove={mockOnRemove} />
    )

    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.getByText('Jane Smith')).toBeInTheDocument()
  })

  it('이미지가 있는 assignee는 이미지를 표시한다', () => {
    render(
      <SelectedAssignees assignees={mockAssignees} onRemove={mockOnRemove} />
    )

    const johnImage = screen.getByAltText('John Doe')
    expect(johnImage).toBeInTheDocument()
    expect(johnImage).toHaveAttribute('src', 'https://example.com/john.jpg')
  })

  it('이미지가 없는 assignee는 이니셜을 표시한다', () => {
    render(
      <SelectedAssignees assignees={mockAssignees} onRemove={mockOnRemove} />
    )

    // Jane Smith의 이니셜 'J'가 표시되어야 함
    expect(screen.getByText('J')).toBeInTheDocument()
  })

  it('각 assignee에 삭제 버튼을 표시한다', () => {
    render(
      <SelectedAssignees assignees={mockAssignees} onRemove={mockOnRemove} />
    )

    const removeButtons = screen.getAllByRole('button')
    expect(removeButtons).toHaveLength(2)
  })

  it('삭제 버튼 클릭 시 onRemove 콜백이 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <SelectedAssignees assignees={mockAssignees} onRemove={mockOnRemove} />
    )

    const removeButtons = screen.getAllByRole('button')
    await user.click(removeButtons[0])

    expect(mockOnRemove).toHaveBeenCalledWith(1) // John Doe의 peopleId
  })

  it('여러 assignee의 삭제 버튼이 각각 올바른 peopleId로 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <SelectedAssignees assignees={mockAssignees} onRemove={mockOnRemove} />
    )

    const removeButtons = screen.getAllByRole('button')

    // 첫 번째 assignee 삭제
    await user.click(removeButtons[0])
    expect(mockOnRemove).toHaveBeenCalledWith(1)

    // 두 번째 assignee 삭제
    await user.click(removeButtons[1])
    expect(mockOnRemove).toHaveBeenCalledWith(2)
  })

  it('단일 assignee만 있을 때도 올바르게 렌더링한다', () => {
    const singleAssignee = [mockAssignees[0]]

    render(
      <SelectedAssignees assignees={singleAssignee} onRemove={mockOnRemove} />
    )

    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument()
  })
})
