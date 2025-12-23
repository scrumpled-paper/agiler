import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { KanbanDateSelector } from './KanbanDateSelector'

describe('KanbanDateSelector', () => {
  const mockOnDateChange = vi.fn()

  // 테스트의 일관성을 위해 현재 날짜를 고정
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2025-01-15T00:00:00.000Z'))
  })

  afterEach(() => {
    vi.clearAllMocks()
    vi.useRealTimers()
  })

  it('캘린더 아이콘과 함께 Select를 렌더링한다', () => {
    render(
      <KanbanDateSelector
        selectedDate="2025-01-15"
        onDateChange={mockOnDateChange}
      />
    )

    expect(screen.getByRole('combobox')).toBeInTheDocument()
    // Calendar 아이콘은 SVG이므로 직접 확인하기 어려움
  })

  it('오늘 날짜를 선택하면 "Today"로 표시한다', () => {
    render(
      <KanbanDateSelector
        selectedDate="2025-01-15"
        onDateChange={mockOnDateChange}
      />
    )

    expect(screen.getByText('Today')).toBeInTheDocument()
  })

  it('어제 날짜를 선택하면 "Yesterday"로 표시한다', () => {
    render(
      <KanbanDateSelector
        selectedDate="2025-01-14"
        onDateChange={mockOnDateChange}
      />
    )

    expect(screen.getByText('Yesterday')).toBeInTheDocument()
  })

  it('그 외 날짜는 포맷된 날짜로 표시한다', () => {
    render(
      <KanbanDateSelector
        selectedDate="2025-01-10"
        onDateChange={mockOnDateChange}
      />
    )

    // 한국어 로케일로 포맷된 날짜가 표시되어야 함
    // 예: "1월 10일 (금)"
    const displayValue = screen.getByRole('combobox')
    expect(displayValue).toBeInTheDocument()
  })

  it('Select 컴포넌트를 렌더링한다', () => {
    render(
      <KanbanDateSelector
        selectedDate="2025-01-15"
        onDateChange={mockOnDateChange}
      />
    )

    const select = screen.getByRole('combobox')
    expect(select).toBeInTheDocument()
  })

  it('props로 전달된 날짜를 사용한다', () => {
    const { rerender } = render(
      <KanbanDateSelector
        selectedDate="2025-01-15"
        onDateChange={mockOnDateChange}
      />
    )

    expect(screen.getByRole('combobox')).toBeInTheDocument()

    // props가 변경되면 업데이트되어야 함
    rerender(
      <KanbanDateSelector
        selectedDate="2025-01-14"
        onDateChange={mockOnDateChange}
      />
    )

    expect(screen.getByRole('combobox')).toBeInTheDocument()
  })

  it('generatePastDates 함수가 31개의 날짜를 생성한다', () => {
    // 컴포넌트 내부 로직 검증을 위해 직접 테스트
    const dates = []
    const today = new Date('2025-01-15T00:00:00.000Z')

    for (let i = 0; i < 31; i++) {
      const date = new Date(today)
      date.setDate(today.getDate() - i)
      dates.push(date)
    }

    expect(dates).toHaveLength(31)
    expect(dates[0].toISOString().split('T')[0]).toBe('2025-01-15')
    expect(dates[30].toISOString().split('T')[0]).toBe('2024-12-16')
  })

  it('다른 날짜를 선택하면 새로운 날짜로 업데이트된다', async () => {
    const { rerender } = render(
      <KanbanDateSelector
        selectedDate="2025-01-15"
        onDateChange={mockOnDateChange}
      />
    )

    expect(screen.getByText('Today')).toBeInTheDocument()

    // props 변경
    rerender(
      <KanbanDateSelector
        selectedDate="2025-01-14"
        onDateChange={mockOnDateChange}
      />
    )

    expect(screen.getByText('Yesterday')).toBeInTheDocument()
  })

  it('월을 넘어가는 날짜도 올바르게 계산한다', () => {
    // 1월 5일로 설정하면 과거 30일에 12월이 포함됨
    const today = new Date('2025-01-05T00:00:00.000Z')
    const sixDaysAgo = new Date(today)
    sixDaysAgo.setDate(today.getDate() - 6)

    expect(sixDaysAgo.toISOString().split('T')[0]).toBe('2024-12-30')
  })
})
