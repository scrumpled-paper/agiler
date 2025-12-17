import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ContentListTable from './ContentListTable'
import type { ListContentItem, PagedResponse } from '@/types/list'

// Mock PagedTable component
vi.mock('@/components/PagedTable', () => ({
  default: ({
    data,
    columns,
    currentPage,
    onPageChange,
    emptyMessage,
  }: {
    data: PagedResponse<ListContentItem>
    columns: Array<{
      key: string
      header: string
      render: (item: unknown) => React.ReactNode
      className?: string
    }>
    currentPage: number
    onPageChange: (page: number) => void
    emptyMessage?: string
  }) => (
    <div data-testid="paged-table">
      <div>Current Page: {currentPage}</div>
      <div>Total Pages: {data.totalPages}</div>
      <div>Empty Message: {emptyMessage}</div>
      <div data-testid="table-content">
        {data.contents.map((item: unknown) => (
          <div
            key={(item as { id: number }).id}
            data-testid={`item-${(item as { id: number }).id}`}
          >
            {columns.map(col => (
              <div key={col.key} data-testid={`col-${col.key}`}>
                {col.render(item)}
              </div>
            ))}
          </div>
        ))}
      </div>
      <button onClick={() => onPageChange(currentPage + 1)}>Change Page</button>
    </div>
  ),
}))

// Mock UI Avatar components
vi.mock('@/components/ui/avatar', () => ({
  Avatar: ({
    children,
    className,
  }: {
    children: React.ReactNode
    className?: string
  }) => <div className={className}>{children}</div>,
  AvatarImage: ({ src, alt }: { src: string; alt: string }) => (
    <img src={src} alt={alt} />
  ),
  AvatarFallback: ({ children }: { children: React.ReactNode }) => (
    <span>{children}</span>
  ),
}))

// Mock date formatter utility
vi.mock('@/utils/date-formatter', () => ({
  formatDate: vi.fn((date: Date | string) => {
    const d = typeof date === 'string' ? new Date(date) : date
    return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`
  }),
}))

describe('ContentListTable', () => {
  const mockData = {
    contents: [
      {
        id: 1,
        title: '테스트 항목 1',
        createdAt: '2024-01-15T10:00:00',
        participants: [
          { id: 1, nickname: '김철수', imageUrl: 'https://example.com/1.jpg' },
          { id: 2, nickname: '이영희', imageUrl: 'https://example.com/2.jpg' },
        ],
      },
      {
        id: 2,
        title: '테스트 항목 2',
        createdAt: '2024-01-14T09:30:00',
        participants: [
          { id: 1, nickname: '김철수', imageUrl: 'https://example.com/1.jpg' },
          { id: 2, nickname: '이영희', imageUrl: 'https://example.com/2.jpg' },
          { id: 3, nickname: '박민수', imageUrl: 'https://example.com/3.jpg' },
          { id: 4, nickname: '최지민', imageUrl: 'https://example.com/4.jpg' },
        ],
      },
    ],
    size: 10,
    number: 1,
    totalPages: 3,
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('기본 렌더링', () => {
    it('PagedTable에 올바른 props를 전달한다', () => {
      const onPageChange = vi.fn()
      render(<ContentListTable data={mockData} onPageChange={onPageChange} />)

      expect(screen.getByTestId('paged-table')).toBeInTheDocument()
      expect(screen.getByText('Total Pages: 3')).toBeInTheDocument()
      expect(
        screen.getByText('Empty Message: 아직 작성된 내용이 없습니다.')
      ).toBeInTheDocument()
    })

    it('제목이 올바르게 렌더링된다', () => {
      render(<ContentListTable data={mockData} onPageChange={vi.fn()} />)

      expect(screen.getByText('테스트 항목 1')).toBeInTheDocument()
      expect(screen.getByText('테스트 항목 2')).toBeInTheDocument()
    })

    it('데이터의 모든 항목이 렌더링된다', () => {
      render(<ContentListTable data={mockData} onPageChange={vi.fn()} />)

      expect(screen.getByTestId('item-1')).toBeInTheDocument()
      expect(screen.getByTestId('item-2')).toBeInTheDocument()
    })
  })

  describe('날짜 포맷팅', () => {
    it('formatDate 유틸리티를 사용하여 날짜를 표시한다', async () => {
      const { formatDate } = await import('@/utils/date-formatter')

      render(<ContentListTable data={mockData} onPageChange={vi.fn()} />)

      expect(formatDate).toHaveBeenCalledWith('2024-01-15T10:00:00')
      expect(formatDate).toHaveBeenCalledWith('2024-01-14T09:30:00')
    })

    it('포맷된 날짜가 렌더링된다', () => {
      render(<ContentListTable data={mockData} onPageChange={vi.fn()} />)

      expect(screen.getByText('2024년 1월 15일')).toBeInTheDocument()
      expect(screen.getByText('2024년 1월 14일')).toBeInTheDocument()
    })
  })

  describe('참여자 표시 로직', () => {
    it('참여자가 3명 이하일 때 모두 표시한다', () => {
      const dataWith2Participants = {
        ...mockData,
        contents: [mockData.contents[0]], // 2명만
      }

      render(
        <ContentListTable data={dataWith2Participants} onPageChange={vi.fn()} />
      )

      expect(screen.getByAltText('김철수')).toBeInTheDocument()
      expect(screen.getByAltText('이영희')).toBeInTheDocument()
      expect(screen.queryByText(/^\+/)).not.toBeInTheDocument() // +N 표시 없음
    })

    it('참여자가 3명일 때 모두 표시하고 +N이 없다', () => {
      const dataWith3Participants = {
        ...mockData,
        contents: [
          {
            id: 3,
            title: '테스트 항목 3',
            createdAt: '2024-01-13T08:00:00',
            participants: [
              {
                id: 1,
                nickname: '김철수',
                imageUrl: 'https://example.com/1.jpg',
              },
              {
                id: 2,
                nickname: '이영희',
                imageUrl: 'https://example.com/2.jpg',
              },
              {
                id: 3,
                nickname: '박민수',
                imageUrl: 'https://example.com/3.jpg',
              },
            ],
          },
        ],
      }

      render(
        <ContentListTable data={dataWith3Participants} onPageChange={vi.fn()} />
      )

      expect(screen.getByAltText('김철수')).toBeInTheDocument()
      expect(screen.getByAltText('이영희')).toBeInTheDocument()
      expect(screen.getByAltText('박민수')).toBeInTheDocument()
      expect(screen.queryByText(/^\+/)).not.toBeInTheDocument()
    })

    it('참여자가 3명 초과일 때 처음 3명만 표시하고 +N을 표시한다', () => {
      const dataWith4Participants = {
        ...mockData,
        contents: [mockData.contents[1]], // 4명
      }

      render(
        <ContentListTable data={dataWith4Participants} onPageChange={vi.fn()} />
      )

      // 처음 3명만 렌더링되는지 확인
      expect(screen.getByAltText('김철수')).toBeInTheDocument()
      expect(screen.getByAltText('이영희')).toBeInTheDocument()
      expect(screen.getByAltText('박민수')).toBeInTheDocument()

      // 4번째 참여자는 표시되지 않음
      expect(screen.queryByAltText('최지민')).not.toBeInTheDocument()

      // +1 표시 확인
      expect(screen.getByText('+1')).toBeInTheDocument()
    })

    it('참여자가 5명일 때 처음 3명만 표시하고 +2를 표시한다', () => {
      const dataWith5Participants = {
        ...mockData,
        contents: [
          {
            id: 5,
            title: '테스트 항목 5',
            createdAt: '2024-01-10T12:00:00',
            participants: [
              {
                id: 1,
                nickname: '김철수',
                imageUrl: 'https://example.com/1.jpg',
              },
              {
                id: 2,
                nickname: '이영희',
                imageUrl: 'https://example.com/2.jpg',
              },
              {
                id: 3,
                nickname: '박민수',
                imageUrl: 'https://example.com/3.jpg',
              },
              {
                id: 4,
                nickname: '최지민',
                imageUrl: 'https://example.com/4.jpg',
              },
              {
                id: 5,
                nickname: '정영수',
                imageUrl: 'https://example.com/5.jpg',
              },
            ],
          },
        ],
      }

      render(
        <ContentListTable data={dataWith5Participants} onPageChange={vi.fn()} />
      )

      // +2 표시 확인
      expect(screen.getByText('+2')).toBeInTheDocument()
    })

    it('참여자 아바타에 올바른 fallback이 표시된다', () => {
      render(<ContentListTable data={mockData} onPageChange={vi.fn()} />)

      // 첫 글자만 표시되는지 확인 (여러 개 있을 수 있으므로 getAllByText 사용)
      const kimElements = screen.getAllByText('김')
      const leeElements = screen.getAllByText('이')

      expect(kimElements.length).toBeGreaterThan(0)
      expect(leeElements.length).toBeGreaterThan(0)
    })

    it('참여자가 없을 때 빈 div만 렌더링된다', () => {
      const dataWithNoParticipants = {
        ...mockData,
        contents: [
          {
            id: 99,
            title: '참여자 없는 항목',
            createdAt: '2024-01-01T00:00:00',
            participants: [],
          },
        ],
      }

      const { container } = render(
        <ContentListTable
          data={dataWithNoParticipants}
          onPageChange={vi.fn()}
        />
      )

      // 참여자 섹션이 비어있는지 확인
      const item = screen.getByTestId('item-99')
      expect(item).toBeInTheDocument()

      // 아바타나 +N이 없어야 함
      expect(container.querySelector('img')).not.toBeInTheDocument()
      expect(screen.queryByText(/^\+/)).not.toBeInTheDocument()
    })
  })

  describe('페이지 변경', () => {
    it('초기 currentPage가 data.number와 일치한다', () => {
      const dataPageTwo = { ...mockData, number: 2 }

      render(<ContentListTable data={dataPageTwo} onPageChange={vi.fn()} />)

      expect(screen.getByText('Current Page: 2')).toBeInTheDocument()
    })

    it('페이지 변경 시 onPageChange가 호출되고 내부 state가 업데이트된다', async () => {
      const onPageChange = vi.fn()
      const user = userEvent.setup()

      render(<ContentListTable data={mockData} onPageChange={onPageChange} />)

      const changeButton = screen.getByRole('button', { name: /change page/i })
      await user.click(changeButton)

      expect(onPageChange).toHaveBeenCalledWith(2)
      // currentPage state가 업데이트되었는지 확인
      expect(screen.getByText('Current Page: 2')).toBeInTheDocument()
    })

    it('다른 페이지 번호로 시작할 수 있다', () => {
      const dataPageFive = { ...mockData, number: 5 }

      render(<ContentListTable data={dataPageFive} onPageChange={vi.fn()} />)

      expect(screen.getByText('Current Page: 5')).toBeInTheDocument()
    })
  })

  describe('컬럼 정의', () => {
    it('3개의 컬럼(제목, 생성일, 참여자)이 정의된다', () => {
      render(<ContentListTable data={mockData} onPageChange={vi.fn()} />)

      // 각 컬럼의 render 함수가 호출되었는지 확인
      expect(screen.getByText('테스트 항목 1')).toBeInTheDocument() // title
      expect(screen.getByText('2024년 1월 15일')).toBeInTheDocument() // createdAt
      // participants - 여러 개 있을 수 있으므로 getAllByAltText 사용
      const participants = screen.getAllByAltText('김철수')
      expect(participants.length).toBeGreaterThan(0)
    })
  })

  describe('빈 데이터 처리', () => {
    it('데이터가 없을 때도 올바르게 렌더링된다', () => {
      const emptyData = {
        contents: [],
        size: 10,
        number: 1,
        totalPages: 0,
      }

      render(<ContentListTable data={emptyData} onPageChange={vi.fn()} />)

      expect(screen.getByTestId('paged-table')).toBeInTheDocument()
      expect(
        screen.getByText('Empty Message: 아직 작성된 내용이 없습니다.')
      ).toBeInTheDocument()
    })
  })
})
