import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LabelSelector } from './LabelSelector'
import type { Label } from '@/types/label'
import { afterEach } from 'node:test'

describe('LabelSelector', () => {
  const mockLabels: Label[] = [
    { labelId: 1, name: 'Bug', description: 'test', color: '#ff0000' },
    {
      labelId: 2,
      name: 'Feature',
      description: 'test',
      color: '#00ff00',
    },
    {
      labelId: 3,
      name: 'Documentation',
      description: 'test',
      color: '#0000ff',
    },
  ]

  const mockSelectedLabels: Label[] = [mockLabels[0]] // Bug is selected

  const mockOnAdd = vi.fn()
  const mockOnRemove = vi.fn()
  const mockOnOpenChange = vi.fn()

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('라벨과 Plus 버튼을 렌더링한다', () => {
    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={[]}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    expect(screen.getByText('Label')).toBeInTheDocument()
    const button = screen.getByRole('button')
    expect(button).toBeInTheDocument()
  })

  it('버튼 클릭 시 레이블 목록을 표시한다', async () => {
    const user = userEvent.setup()

    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={[]}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    const button = screen.getByRole('button')
    await user.click(button)

    expect(mockOnOpenChange).toHaveBeenCalledWith(true)
  })

  it('isOpen이 true일 때 레이블 목록이 표시된다', () => {
    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={[]}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    expect(screen.getByText('Bug')).toBeInTheDocument()
    expect(screen.getByText('Feature')).toBeInTheDocument()
    expect(screen.getByText('Documentation')).toBeInTheDocument()
  })

  it('레이블 선택 시 onAdd 콜백이 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={[]}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    const labelItem = screen.getByText('Bug')
    await user.click(labelItem)

    expect(mockOnAdd).toHaveBeenCalledWith(mockLabels[0])
  })

  it('선택된 레이블을 Badge로 표시한다', () => {
    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={mockSelectedLabels}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    // Bug 레이블이 선택되어 있어야 함
    const badges = screen.getAllByText('Bug')
    expect(badges.length).toBeGreaterThan(0)
  })

  it('선택된 레이블의 삭제 버튼 클릭 시 onRemove 콜백이 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={mockSelectedLabels}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    // Badge의 삭제 버튼 클릭
    const removeButtons = screen.getAllByRole('button')
    // 첫 번째는 Plus 버튼, 두 번째는 Badge의 삭제 버튼
    await user.click(removeButtons[1])

    expect(mockOnRemove).toHaveBeenCalledWith(mockLabels[0]) // Bug의 id
  })

  it('선택된 레이블이 없으면 Badge를 표시하지 않는다', () => {
    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={[]}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    // Plus 버튼만 있어야 함
    const buttons = screen.getAllByRole('button')
    expect(buttons).toHaveLength(1)
  })

  it('레이블 색상을 올바르게 표시한다', () => {
    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={mockSelectedLabels}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    // Badge의 배경색이 #ff0000이어야 함
    const badge = screen.getAllByText('Bug')[0].closest('.inline-flex')
    expect(badge).toHaveStyle({ backgroundColor: '#ff0000' })
  })

  it('검색 입력 필드를 렌더링한다', () => {
    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={[]}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    const searchInput = screen.getByPlaceholderText('Search label...')
    expect(searchInput).toBeInTheDocument()
  })

  it('레이블이 없을 때 "No label found" 메시지를 표시한다', () => {
    render(
      <LabelSelector
        labels={[]}
        selectedLabels={[]}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={true}
        onOpenChange={mockOnOpenChange}
      />
    )

    expect(screen.getByText('No label found.')).toBeInTheDocument()
  })

  it('여러 레이블을 선택할 수 있다', () => {
    const multipleSelectedLabels = [mockLabels[0], mockLabels[1]]

    render(
      <LabelSelector
        labels={mockLabels}
        selectedLabels={multipleSelectedLabels}
        onAdd={mockOnAdd}
        onRemove={mockOnRemove}
        isOpen={false}
        onOpenChange={mockOnOpenChange}
      />
    )

    expect(screen.getByText('Bug')).toBeInTheDocument()
    expect(screen.getByText('Feature')).toBeInTheDocument()
  })
})
