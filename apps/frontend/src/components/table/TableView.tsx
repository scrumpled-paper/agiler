import type { ColumnDef } from '@/components/ui/shadcn-io/table'
import {
  TableBody,
  TableCell,
  TableColumnHeader,
  TableHead,
  TableHeader,
  TableHeaderGroup,
  TableProvider,
  TableRow,
} from '@/components/ui/shadcn-io/table'
import type { Issue } from '@/types/issue'
import type { IssueColumn } from '@/types'
import { useMemo } from 'react'
import type { Column, Row } from '@tanstack/react-table'

interface TableViewProps {
  columns: IssueColumn[]
  tasks: Issue[]
}

export default function TableView({ columns, tasks }: TableViewProps) {
  const tableColumns: ColumnDef<Issue, unknown>[] = useMemo(
    () => [
      {
        accessorKey: 'name',
        header: ({ column }: { column: Column<Issue, unknown> }) => (
          <TableColumnHeader column={column} title="Task Name" />
        ),
        cell: ({ row }: { row: Row<Issue> }) => (
          <div className="font-medium">{row.getValue('name')}</div>
        ),
      },
      {
        accessorKey: 'column',
        header: ({ column }: { column: Column<Issue, unknown> }) => (
          <TableColumnHeader column={column} title="Status" />
        ),
        cell: ({ row }: { row: Row<Issue> }) => {
          // row.getValue('column')은 Issue['column'] (string) 타입입니다.
          const columnId = row.getValue('column') as string
          const statusColumn = columns.find(col => col.id === columnId)
          return (
            <div className="flex items-center gap-2">
              <div
                className="h-2 w-2 rounded-full"
                style={{ backgroundColor: statusColumn?.color }}
              />
              <span>{statusColumn?.name || 'Unknown'}</span>
            </div>
          )
        },
      },
      {
        accessorKey: 'owner',
        header: ({ column }: { column: Column<Issue, unknown> }) => (
          <TableColumnHeader column={column} title="Owner" />
        ),
        cell: ({ row }: { row: Row<Issue> }) => {
          const owner = row.getValue('owner') as Issue['owner']
          return <div>{owner.nickname}</div>
        },
      },
      {
        accessorKey: 'startAt',
        header: ({ column }: { column: Column<Issue, unknown> }) => (
          <TableColumnHeader column={column} title="Start Date" />
        ),
        cell: ({ row }: { row: Row<Issue> }) => {
          // Issue 타입에서 startAt이 Date 타입이므로, 타입 캐스팅 후 날짜 포맷팅
          const date = row.getValue('startAt') as Date
          return (
            <div>
              {/* Date 객체가 아닌 경우를 대비해 new Date(date)로 감싸는 것은 유지 */}
              {new Date(date).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
              })}
            </div>
          )
        },
      },
      {
        accessorKey: 'endAt',
        header: ({ column }: { column: Column<Issue, unknown> }) => (
          <TableColumnHeader column={column} title="End Date" />
        ),
        cell: ({ row }: { row: Row<Issue> }) => {
          // Issue 타입에서 endAt이 Date 타입이므로, 타입 캐스팅 후 날짜 포맷팅
          const date = row.getValue('endAt') as Date
          return (
            <div>
              {new Date(date).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
              })}
            </div>
          )
        },
      },
    ],
    [columns]
  )

  // ⚠️data prop에 tasks 대신 issues를 사용하는 것이 일반적이지만,
  // props 이름은 그대로 유지하고 data={tasks}를 사용합니다.
  return (
    <div className="rounded-md border">
      <TableProvider columns={tableColumns} data={tasks}>
        <TableHeader>
          {({ headerGroup }) => (
            <TableHeaderGroup key={headerGroup.id} headerGroup={headerGroup}>
              {({ header }) => <TableHead key={header.id} header={header} />}
            </TableHeaderGroup>
          )}
        </TableHeader>
        <TableBody>
          {({ row }) => (
            <TableRow key={row.id} row={row}>
              {({ cell }) => <TableCell key={cell.id} cell={cell} />}
            </TableRow>
          )}
        </TableBody>
      </TableProvider>
    </div>
  )
}
