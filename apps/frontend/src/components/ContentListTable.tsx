import { useState } from 'react'
import PagedTable from '@/components/PagedTable'
import type { PagedResponse } from '@/types/list'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { formatDate } from '@/utils/date-formatter'

// ListContentItem을 상속받는 실제 타입 (예: 데일리스크럼, 회고, 회의록 등)
interface ContentItem {
  id: number
  title: string
  createdAt: Date | string
  participants: Array<{
    id: number
    nickname: string
    imageUrl: string
  }>
}

interface ContentListTableProps {
  data: PagedResponse<ContentItem>
  onPageChange: (page: number) => void
}

export default function ContentListTable({
  data,
  onPageChange,
}: ContentListTableProps) {
  const [currentPage, setCurrentPage] = useState(data.number)

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    onPageChange(page)
  }

  const columns = [
    {
      key: 'title',
      header: '제목',
      render: (item: ContentItem) => (
        <div className="font-medium">{item.title}</div>
      ),
      className: 'w-[40%]',
    },
    {
      key: 'createdAt',
      header: '생성일',
      render: (item: ContentItem) => (
        <div className="text-muted-foreground">
          {formatDate(item.createdAt)}
        </div>
      ),
      className: 'w-[20%]',
    },
    {
      key: 'participants',
      header: '참여자',
      render: (item: ContentItem) => (
        <div className="flex -space-x-2">
          {item.participants.slice(0, 3).map(participant => (
            <Avatar
              key={participant.id}
              className="h-8 w-8 border-2 border-background"
            >
              <AvatarImage
                src={participant.imageUrl}
                alt={participant.nickname}
              />
              <AvatarFallback>{participant.nickname[0]}</AvatarFallback>
            </Avatar>
          ))}
          {item.participants.length > 3 && (
            <div className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-background bg-muted text-xs">
              +{item.participants.length - 3}
            </div>
          )}
        </div>
      ),
      className: 'w-[40%]',
    },
  ]

  return (
    <PagedTable
      data={data}
      columns={columns}
      currentPage={currentPage}
      onPageChange={handlePageChange}
      emptyMessage="아직 작성된 내용이 없습니다."
    />
  )
}
