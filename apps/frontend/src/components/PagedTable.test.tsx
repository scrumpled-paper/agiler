import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PagedTable from './PagedTable'

// Mock UI table components
vi.mock('@/components/ui/table', () => ({
  Table: ({ children }: { children: React.ReactNode }) => (
    <table>{children}</table>
  ),
  TableHeader: ({ children }: { children: React.ReactNode }) => (
    <thead>{children}</thead>
  ),
  TableBody: ({ children }: { children: React.ReactNode }) => (
    <tbody>{children}</tbody>
  ),
  TableRow: ({ children }: { children: React.ReactNode }) => (
    <tr>{children}</tr>
  ),
  TableHead: ({
    children,
    className,
  }: {
    children: React.ReactNode
    className?: string
  }) => <th className={className}>{children}</th>,
  TableCell: ({
    children,
    className,
    colSpan,
  }: {
    children: React.ReactNode
    className?: string
    colSpan?: number
  }) => (
    <td className={className} colSpan={colSpan}>
      {children}
    </td>
  ),
}))

vi.mock('@/components/Pagination', () => ({
  default: ({
    currentPage,
    totalPages,
    onPageChange,
  }: {
    currentPage: number
    totalPages: number
    onPageChange: (page: number) => void
  }) => (
    <div data-testid="pagination">
      <span>
        Page {currentPage} of {totalPages}
      </span>
      <button onClick={() => onPageChange(currentPage + 1)}>Next</button>
    </div>
  ),
}))

describe('PagedTable', () => {
  const mockColumns = [
    {
      key: 'id',
      header: 'ID',
      render: (item: { id: number; name: string }) => <span>{item.id}</span>,
    },
    {
      key: 'name',
      header: 'Name',
      render: (item: { id: number; name: string }) => <span>{item.name}</span>,
      className: 'w-[50%]',
    },
  ]

  describe('기본 렌더링', () => {
    it('데이터가 있을 때 테이블과 행들을 렌더링한다', () => {
      const data = {
        contents: [
          { id: 1, name: 'Item 1' },
          { id: 2, name: 'Item 2' },
        ],
        size: 10,
        number: 1,
        totalPages: 1,
      }

      render(
        <PagedTable
          data={data}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      expect(screen.getByText('Item 1')).toBeInTheDocument()
      expect(screen.getByText('Item 2')).toBeInTheDocument()
    })

    it('컬럼 헤더가 올바르게 렌더링된다', () => {
      const data = {
        contents: [{ id: 1, name: 'Item 1' }],
        size: 10,
        number: 1,
        totalPages: 1,
      }

      render(
        <PagedTable
          data={data}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      expect(screen.getByText('ID')).toBeInTheDocument()
      expect(screen.getByText('Name')).toBeInTheDocument()
    })

    it('컬럼 className이 적용된다', () => {
      const data = {
        contents: [{ id: 1, name: 'Item 1' }],
        size: 10,
        number: 1,
        totalPages: 1,
      }

      const { container } = render(
        <PagedTable
          data={data}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      const nameHeader = container.querySelector('th.w-\\[50\\%\\]')
      expect(nameHeader).toBeInTheDocument()
      expect(nameHeader).toHaveTextContent('Name')
    })
  })

  describe('빈 상태 처리', () => {
    it('데이터가 없을 때 기본 빈 메시지를 표시한다', () => {
      const emptyData = {
        contents: [],
        size: 10,
        number: 1,
        totalPages: 0,
      }

      render(
        <PagedTable
          data={emptyData}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      expect(screen.getByText('데이터가 없습니다.')).toBeInTheDocument()
    })

    it('커스텀 빈 메시지를 표시한다', () => {
      const emptyData = {
        contents: [],
        size: 10,
        number: 1,
        totalPages: 0,
      }

      render(
        <PagedTable
          data={emptyData}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
          emptyMessage="검색 결과가 없습니다."
        />
      )

      expect(screen.getByText('검색 결과가 없습니다.')).toBeInTheDocument()
      expect(screen.queryByText('데이터가 없습니다.')).not.toBeInTheDocument()
    })

    it('빈 상태일 때 colSpan이 컬럼 수와 일치한다', () => {
      const emptyData = {
        contents: [],
        size: 10,
        number: 1,
        totalPages: 0,
      }

      const { container } = render(
        <PagedTable
          data={emptyData}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      const emptyCell = container.querySelector('td[colspan="2"]')
      expect(emptyCell).toBeInTheDocument()
    })
  })

  describe('페이지네이션', () => {
    it('totalPages > 0일 때 페이지네이션을 렌더링한다', () => {
      const data = {
        contents: [{ id: 1, name: 'Item 1' }],
        size: 10,
        number: 1,
        totalPages: 5,
      }

      render(
        <PagedTable
          data={data}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      expect(screen.getByTestId('pagination')).toBeInTheDocument()
      expect(screen.getByText('Page 1 of 5')).toBeInTheDocument()
    })

    it('totalPages = 0일 때 페이지네이션을 렌더링하지 않는다', () => {
      const data = {
        contents: [],
        size: 10,
        number: 1,
        totalPages: 0,
      }

      render(
        <PagedTable
          data={data}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      expect(screen.queryByTestId('pagination')).not.toBeInTheDocument()
    })

    it('페이지 변경 시 onPageChange가 호출된다', async () => {
      const onPageChange = vi.fn()
      const user = userEvent.setup()
      const data = {
        contents: [{ id: 1, name: 'Item 1' }],
        size: 10,
        number: 2,
        totalPages: 5,
      }

      render(
        <PagedTable
          data={data}
          columns={mockColumns}
          currentPage={2}
          onPageChange={onPageChange}
        />
      )

      const nextButton = screen.getByRole('button', { name: /next/i })
      await user.click(nextButton)

      expect(onPageChange).toHaveBeenCalledWith(3)
    })
  })

  describe('제네릭 타입 지원', () => {
    it('다양한 타입의 데이터를 렌더링할 수 있다', () => {
      interface CustomType {
        id: string
        title: string
        status: string
      }

      const customColumns = [
        {
          key: 'title',
          header: 'Title',
          render: (item: CustomType) => <span>{item.title}</span>,
        },
        {
          key: 'status',
          header: 'Status',
          render: (item: CustomType) => (
            <span className="badge">{item.status}</span>
          ),
        },
      ]

      const customData = {
        contents: [
          { id: 'task-1', title: 'Task 1', status: 'active' },
          { id: 'task-2', title: 'Task 2', status: 'done' },
        ],
        size: 10,
        number: 1,
        totalPages: 1,
      }

      render(
        <PagedTable
          data={customData}
          columns={customColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      expect(screen.getByText('Task 1')).toBeInTheDocument()
      expect(screen.getByText('Task 2')).toBeInTheDocument()
      expect(screen.getByText('active')).toBeInTheDocument()
      expect(screen.getByText('done')).toBeInTheDocument()
    })
  })

  describe('여러 행 렌더링', () => {
    it('모든 데이터 행이 올바르게 렌더링된다', () => {
      const data = {
        contents: [
          { id: 1, name: 'Item 1' },
          { id: 2, name: 'Item 2' },
          { id: 3, name: 'Item 3' },
          { id: 4, name: 'Item 4' },
          { id: 5, name: 'Item 5' },
        ],
        size: 10,
        number: 1,
        totalPages: 2,
      }

      render(
        <PagedTable
          data={data}
          columns={mockColumns}
          currentPage={1}
          onPageChange={vi.fn()}
        />
      )

      expect(screen.getByText('Item 1')).toBeInTheDocument()
      expect(screen.getByText('Item 2')).toBeInTheDocument()
      expect(screen.getByText('Item 3')).toBeInTheDocument()
      expect(screen.getByText('Item 4')).toBeInTheDocument()
      expect(screen.getByText('Item 5')).toBeInTheDocument()
    })
  })
})
