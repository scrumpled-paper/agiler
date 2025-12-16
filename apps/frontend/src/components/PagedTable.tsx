import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import DynamicPagination from '@/components/Pagination'
import type { PagedResponse } from '@/types/list'

// Base type constraint - only requires id for keying
interface TableItem {
  id: number | string
}

interface Column<T> {
  key: string
  header: string
  render: (item: T) => React.ReactNode
  className?: string
}

interface PagedTableProps<T extends TableItem> {
  data: PagedResponse<T>
  columns: Column<T>[]
  currentPage: number
  onPageChange: (page: number) => void
  emptyMessage?: string
}

export default function PagedTable<T extends TableItem>({
  data,
  columns,
  currentPage,
  onPageChange,
  emptyMessage = '데이터가 없습니다.',
}: PagedTableProps<T>) {
  return (
    <div className="w-full space-y-4">
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map(column => (
                <TableHead key={column.key} className={column.className}>
                  {column.header}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.contents.length > 0 ? (
              data.contents.map(item => (
                <TableRow key={item.id}>
                  {columns.map(column => (
                    <TableCell key={column.key} className={column.className}>
                      {column.render(item)}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className="h-24 text-center"
                >
                  {emptyMessage}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {data.totalPages > 0 && (
        <DynamicPagination
          currentPage={currentPage}
          totalPages={data.totalPages}
          onPageChange={onPageChange}
        />
      )}
    </div>
  )
}
