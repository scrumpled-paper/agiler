import ContentListTable from '@/components/ContentListTable'
import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { scrumService } from '@/api/services/projectActivityService'
import type { PagedResponse, Participant } from '@/types/list'
import type {
  PaginatedResponse,
  ActivityItem,
  Participant as ActivityParticipant,
} from '@/types/activity'

/**
 * PaginatedResponse를 PagedResponse로 변환하는 헬퍼 함수
 * ActivityItem의 Participant (profileId) → ContentItem의 Participant (id) 변환 포함
 */
function convertToPagedResponse(
  paginatedResponse: PaginatedResponse<ActivityItem>
): PagedResponse<{
  id: number
  title: string
  createdAt: Date | string
  participants: Participant[]
}> {
  return {
    contents: paginatedResponse.contents.map(item => ({
      id: item.id,
      title: item.title,
      createdAt: item.createdAt,
      participants: item.participants.map(
        (participant: ActivityParticipant): Participant => ({
          id: participant.profileId,
          nickname: participant.nickname,
          imageUrl: participant.imageUrl,
        })
      ),
    })),
    size: paginatedResponse.pageSize,
    number: paginatedResponse.currentPage,
    totalPages: paginatedResponse.totalPages,
  }
}

export default function DailyScrumsList() {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const [currentPage, setCurrentPage] = useState(0)

  // API를 통해 데일리 스크럼 목록 조회
  const { data, isLoading, error } = useQuery({
    queryKey: ['dailyScrums', projectUrl, currentPage],
    queryFn: () =>
      scrumService.getScrums(projectUrl!, {
        page: currentPage,
        size: 10,
      }),
    enabled: !!projectUrl,
  })

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
  }

  // 로딩 상태
  if (isLoading) {
    return (
      <div className="container p-4">
        <h1 className="text-3xl font-bold mb-4">데일리 스크럼 목록</h1>
        <div className="flex justify-center items-center h-64">
          <p className="text-muted-foreground">로딩 중...</p>
        </div>
      </div>
    )
  }

  // 에러 상태
  if (error) {
    return (
      <div className="container p-4">
        <h1 className="text-3xl font-bold mb-4">데일리 스크럼 목록</h1>
        <div className="flex justify-center items-center h-64">
          <p className="text-destructive">
            데이터를 불러오는 중 오류가 발생했습니다.
          </p>
        </div>
      </div>
    )
  }

  // 데이터가 없는 경우
  if (!data) {
    return null
  }

  // PaginatedResponse를 PagedResponse로 변환
  const pagedData = convertToPagedResponse(data)

  return (
    <div className="container p-4">
      <h1 className="text-3xl font-bold mb-4">데일리 스크럼 목록</h1>
      <ContentListTable data={pagedData} onPageChange={handlePageChange} />
    </div>
  )
}
