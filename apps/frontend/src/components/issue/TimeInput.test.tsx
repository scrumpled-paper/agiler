import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TimeInput } from './TimeInput'
import type { TimeOption } from '@/utils/time-utils'
import { afterEach } from 'node:test'

describe('TimeInput', () => {
  const mockOnChange = vi.fn()
  const mockOptions: TimeOption[] = [
    { value: '09:00', label: '오전 9:00' },
    { value: '09:30', label: '오전 9:30' },
    { value: '10:00', label: '오전 10:00' },
    { value: '14:00', label: '오후 2:00' },
    { value: '14:30', label: '오후 2:30' },
  ]

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('라벨을 렌더링한다', () => {
    render(
      <TimeInput
        label="시작 시간"
        value=""
        onChange={mockOnChange}
        options={mockOptions}
      />
    )

    expect(screen.getByText('시작 시간')).toBeInTheDocument()
  })

  it('옵션이 없을 때 emptyMessage를 표시한다', () => {
    render(
      <TimeInput
        label="시작 시간"
        value=""
        onChange={mockOnChange}
        options={[]}
        emptyMessage="선택 가능한 시간이 없습니다"
      />
    )

    expect(screen.getByText('선택 가능한 시간이 없습니다')).toBeInTheDocument()
  })

  it('옵션이 없을 때 Select가 렌더링되지 않는다', () => {
    render(
      <TimeInput
        label="시작 시간"
        value=""
        onChange={mockOnChange}
        options={[]}
        emptyMessage="선택 가능한 시간이 없습니다"
      />
    )

    expect(screen.queryByRole('combobox')).not.toBeInTheDocument()
  })

  it('disabled 상태일 때 Select가 비활성화된다', () => {
    render(
      <TimeInput
        label="마감 시간"
        value=""
        onChange={mockOnChange}
        options={mockOptions}
        disabled={true}
      />
    )

    const select = screen.getByRole('combobox')
    expect(select).toBeDisabled()
  })

  it('시간 선택 시 onChange 콜백이 호출된다', async () => {
    const user = userEvent.setup()

    render(
      <TimeInput
        label="시작 시간"
        value=""
        onChange={mockOnChange}
        options={mockOptions}
      />
    )

    // Select 클릭
    const select = screen.getByRole('combobox')
    await user.click(select)

    // 옵션 선택
    const option = await screen.findByText('오후 2:30')
    await user.click(option)

    expect(mockOnChange).toHaveBeenCalledWith('14:30')
  })

  it('선택된 값을 올바르게 표시한다', () => {
    render(
      <TimeInput
        label="시작 시간"
        value="14:30"
        onChange={mockOnChange}
        options={mockOptions}
      />
    )

    expect(screen.getByText('오후 2:30')).toBeInTheDocument()
  })

  it('placeholder를 커스터마이즈할 수 있다', () => {
    render(
      <TimeInput
        label="시작 시간"
        value=""
        onChange={mockOnChange}
        options={mockOptions}
        placeholder="시간을 골라주세요"
      />
    )

    expect(screen.getByText('시간을 골라주세요')).toBeInTheDocument()
  })

  it('Clock 아이콘을 렌더링한다', () => {
    render(
      <TimeInput
        label="시작 시간"
        value=""
        onChange={mockOnChange}
        options={mockOptions}
      />
    )

    // Clock 아이콘이 있는지 확인 (lucide-react의 Clock 컴포넌트)
    const clockIcon = document.querySelector('svg')
    expect(clockIcon).toBeInTheDocument()
  })

  it('모든 옵션을 드롭다운에 렌더링한다', async () => {
    const user = userEvent.setup()

    render(
      <TimeInput
        label="시작 시간"
        value=""
        onChange={mockOnChange}
        options={mockOptions}
      />
    )

    // Select 클릭하여 드롭다운 열기
    const select = screen.getByRole('combobox')
    await user.click(select)

    // 모든 옵션이 표시되는지 확인
    expect(await screen.findByText('오전 9:00')).toBeInTheDocument()
    expect(await screen.findByText('오전 9:30')).toBeInTheDocument()
    expect(await screen.findByText('오전 10:00')).toBeInTheDocument()
    expect(await screen.findByText('오후 2:00')).toBeInTheDocument()
    expect(await screen.findByText('오후 2:30')).toBeInTheDocument()
  })
})
