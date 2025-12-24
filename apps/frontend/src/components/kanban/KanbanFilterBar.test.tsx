import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { KanbanFilterBar, type KanbanFilters } from './KanbanFilterBar'
import type { UserInfo } from '@/types'
import type { Label } from '@/types/label'

describe('KanbanFilterBar', () => {
  const mockOwners: UserInfo[] = [
    {
      profileId: 1,
      nickname: 'John Doe',
      email: 'john@example.com',
    },
    {
      profileId: 2,
      nickname: 'Jane Smith',
      email: 'jane@example.com',
    },
  ]

  const mockLabels: Label[] = [
    {
      labelId: 1,
      name: 'Bug',
      description: 'Bug label',
      color: '#ff0000',
    },
    {
      labelId: 2,
      name: 'Feature',
      description: 'Feature label',
      color: '#00ff00',
    },
  ]

  const mockSubscribers: UserInfo[] = [
    {
      profileId: 3,
      nickname: 'Bob Johnson',
      email: 'bob@example.com',
    },
  ]

  const defaultFilters: KanbanFilters = {
    search: '',
    sortBy: 'endAt-asc',
    selectedOwners: [],
    selectedLabels: [],
    selectedSubscribers: [],
  }

  const mockOnFiltersChange = vi.fn()

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('검색 입력 필드를 렌더링한다', () => {
    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    expect(screen.getByPlaceholderText('이슈 검색...')).toBeInTheDocument()
  })

  it('정렬 셀렉트를 렌더링한다', () => {
    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    expect(screen.getByRole('combobox')).toBeInTheDocument()
  })

  it('담당자, 라벨, 구독자 필터 버튼을 렌더링한다', () => {
    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    expect(screen.getByText('담당자')).toBeInTheDocument()
    expect(screen.getByText('라벨')).toBeInTheDocument()
    expect(screen.getByText('구독자')).toBeInTheDocument()
  })

  it('검색어 입력 시 onFiltersChange가 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    const searchInput = screen.getByPlaceholderText('이슈 검색...')
    await user.type(searchInput, 't')

    expect(mockOnFiltersChange).toHaveBeenCalled()
    expect(mockOnFiltersChange).toHaveBeenLastCalledWith(
      expect.objectContaining({
        search: 't',
      })
    )
  })

  it('정렬 옵션 변경 시 onFiltersChange가 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    const sortSelect = screen.getByRole('combobox')
    await user.click(sortSelect)

    // 정렬 옵션 클릭
    const option = screen.getByText('마감일 느린순')
    await user.click(option)

    expect(mockOnFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        sortBy: 'endAt-desc',
      })
    )
  })

  it('담당자 필터 버튼 클릭 시 목록이 표시된다', async () => {
    const user = userEvent.setup()

    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    const ownerButton = screen.getByText('담당자')
    await user.click(ownerButton)

    // 담당자 목록이 표시되어야 함
    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.getByText('Jane Smith')).toBeInTheDocument()
  })

  it('라벨 필터 버튼 클릭 시 목록이 표시된다', async () => {
    const user = userEvent.setup()

    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    const labelButton = screen.getByText('라벨')
    await user.click(labelButton)

    // 라벨 목록이 표시되어야 함
    expect(screen.getByText('Bug')).toBeInTheDocument()
    expect(screen.getByText('Feature')).toBeInTheDocument()
  })

  it('담당자 선택 시 onFiltersChange가 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    const ownerButton = screen.getByText('담당자')
    await user.click(ownerButton)

    const johnDoe = screen.getByText('John Doe')
    await user.click(johnDoe)

    expect(mockOnFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        selectedOwners: [1],
      })
    )
  })

  it('라벨 선택 시 onFiltersChange가 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    const labelButton = screen.getByText('라벨')
    await user.click(labelButton)

    const bugLabel = screen.getByText('Bug')
    await user.click(bugLabel)

    expect(mockOnFiltersChange).toHaveBeenCalledWith(
      expect.objectContaining({
        selectedLabels: [1],
      })
    )
  })

  it('선택된 필터가 있을 때 배지로 표시한다', () => {
    const filtersWithSelection: KanbanFilters = {
      search: 'test',
      sortBy: 'endAt-asc',
      selectedOwners: [1],
      selectedLabels: [1],
      selectedSubscribers: [3],
    }

    render(
      <KanbanFilterBar
        filters={filtersWithSelection}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    // 활성화된 필터 배지가 표시되어야 함
    expect(screen.getByText('검색: test')).toBeInTheDocument()
    expect(screen.getByText('담당: John Doe')).toBeInTheDocument()
    expect(screen.getByText('Bug')).toBeInTheDocument()
    expect(screen.getByText('구독: Bob Johnson')).toBeInTheDocument()
  })

  it('선택된 필터가 있을 때 초기화 버튼을 표시한다', () => {
    const filtersWithSelection: KanbanFilters = {
      search: 'test',
      sortBy: 'endAt-asc',
      selectedOwners: [1],
      selectedLabels: [],
      selectedSubscribers: [],
    }

    render(
      <KanbanFilterBar
        filters={filtersWithSelection}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    expect(screen.getByText('초기화')).toBeInTheDocument()
  })

  it('초기화 버튼 클릭 시 모든 필터가 초기화된다', async () => {
    const user = userEvent.setup()

    const filtersWithSelection: KanbanFilters = {
      search: 'test',
      sortBy: 'name-asc',
      selectedOwners: [1],
      selectedLabels: [1],
      selectedSubscribers: [3],
    }

    render(
      <KanbanFilterBar
        filters={filtersWithSelection}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    const clearButton = screen.getByText('초기화')
    await user.click(clearButton)

    expect(mockOnFiltersChange).toHaveBeenCalledWith({
      search: '',
      sortBy: 'endAt-asc',
      selectedOwners: [],
      selectedLabels: [],
      selectedSubscribers: [],
    })
  })

  it('선택된 필터가 없을 때 초기화 버튼을 표시하지 않는다', () => {
    render(
      <KanbanFilterBar
        filters={defaultFilters}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    expect(screen.queryByText('초기화')).not.toBeInTheDocument()
  })

  it('선택된 담당자 수를 배지로 표시한다', () => {
    const filtersWithOwners: KanbanFilters = {
      ...defaultFilters,
      selectedOwners: [1, 2],
    }

    render(
      <KanbanFilterBar
        filters={filtersWithOwners}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    // 담당자 버튼 옆에 '2' 배지가 표시되어야 함
    const ownerButton = screen.getByText('담당자')
    expect(ownerButton.parentElement).toHaveTextContent('2')
  })

  it('선택된 라벨 수를 배지로 표시한다', () => {
    const filtersWithLabels: KanbanFilters = {
      ...defaultFilters,
      selectedLabels: [1, 2],
    }

    render(
      <KanbanFilterBar
        filters={filtersWithLabels}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    // 라벨 버튼 옆에 '2' 배지가 표시되어야 함
    const labelButton = screen.getByText('라벨')
    expect(labelButton.parentElement).toHaveTextContent('2')
  })

  it('선택된 필터 배지가 표시된다', () => {
    const filtersWithSelection: KanbanFilters = {
      search: '',
      sortBy: 'endAt-asc',
      selectedOwners: [1, 2],
      selectedLabels: [],
      selectedSubscribers: [],
    }

    render(
      <KanbanFilterBar
        filters={filtersWithSelection}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    // 담당자 배지가 표시되어야 함
    expect(screen.getByText('담당: John Doe')).toBeInTheDocument()
    expect(screen.getByText('담당: Jane Smith')).toBeInTheDocument()
  })

  it('검색어 배지가 표시된다', () => {
    const filtersWithSearch: KanbanFilters = {
      search: 'test',
      sortBy: 'endAt-asc',
      selectedOwners: [],
      selectedLabels: [],
      selectedSubscribers: [],
    }

    render(
      <KanbanFilterBar
        filters={filtersWithSearch}
        onFiltersChange={mockOnFiltersChange}
        availableOwners={mockOwners}
        availableLabels={mockLabels}
        availableSubscribers={mockSubscribers}
      />
    )

    // 검색어 배지가 표시되어야 함
    expect(screen.getByText('검색: test')).toBeInTheDocument()
  })
})
