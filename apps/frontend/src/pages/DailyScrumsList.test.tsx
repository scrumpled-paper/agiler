import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import DailyScrumsList from './DailyScrumsList'

// Mock ContentListTable component
vi.mock('@/components/ContentListTable', () => ({
  default: ({
    data,
    onPageChange,
  }: {
    data: { contents: unknown[] }
    onPageChange: (page: number) => void
  }) => (
    <div data-testid="content-list-table">
      <div>Total Items: {data.contents.length}</div>
      <button onClick={() => onPageChange(2)}>Change to Page 2</button>
    </div>
  ),
}))

describe('DailyScrumsList', () => {
  const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

  afterEach(() => {
    consoleSpy.mockClear()
  })

  describe('기본 렌더링', () => {
    it('페이지 제목과 ContentListTable을 렌더링한다', () => {
      render(<DailyScrumsList />)

      expect(
        screen.getByRole('heading', { name: /DailyScrumList/i })
      ).toBeInTheDocument()
      expect(screen.getByTestId('content-list-table')).toBeInTheDocument()
    })

    it('mockListData를 ContentListTable에 전달한다', () => {
      render(<DailyScrumsList />)

      // mockListData의 contents 길이는 2
      expect(screen.getByText('Total Items: 2')).toBeInTheDocument()
    })

    it('제목이 올바른 스타일로 렌더링된다', () => {
      render(<DailyScrumsList />)

      const heading = screen.getByRole('heading', { name: /DailyScrumList/i })
      expect(heading).toHaveClass('text-3xl', 'font-bold', 'mb-4')
    })

    it('컨테이너가 올바른 스타일로 렌더링된다', () => {
      const { container } = render(<DailyScrumsList />)

      const containerDiv = container.querySelector('.container.p-4')
      expect(containerDiv).toBeInTheDocument()
    })
  })

  describe('페이지 변경 핸들러', () => {
    it('페이지 변경 시 console.log가 호출된다', async () => {
      const user = userEvent.setup()

      render(<DailyScrumsList />)

      const changeButton = screen.getByRole('button', {
        name: /change to page 2/i,
      })
      await user.click(changeButton)

      expect(consoleSpy).toHaveBeenCalledWith('페이지 변경:', 2)
    })

    it('페이지 변경 핸들러가 올바른 페이지 번호를 전달한다', async () => {
      const user = userEvent.setup()

      render(<DailyScrumsList />)

      const changeButton = screen.getByRole('button', {
        name: /change to page 2/i,
      })
      await user.click(changeButton)

      // 가장 최근 호출 확인
      expect(consoleSpy).toHaveBeenCalledTimes(1)
      expect(consoleSpy.mock.calls[0][0]).toBe('페이지 변경:')
      expect(consoleSpy.mock.calls[0][1]).toBe(2)
    })
  })

  describe('상태 관리', () => {
    it('초기 상태로 mockListData를 사용한다', () => {
      render(<DailyScrumsList />)

      // mockListData는 2개의 항목을 가지고 있음
      expect(screen.getByText('Total Items: 2')).toBeInTheDocument()
    })
  })
})
