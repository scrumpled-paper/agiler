import ContentListTable from '@/components/ContentListTable'
import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { meetingService } from '@/api/services/projectActivityService'
import type { PagedResponse, Participant } from '@/types/list'
import type {
  PaginatedResponse,
  ActivityItem,
  Participant as ActivityParticipant,
} from '@/types/activity'
import { Button } from '@/components/ui/button'
import { Plus } from 'lucide-react'

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

export default function MeetingsList() {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const [currentPage, setCurrentPage] = useState(0)
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  // API를 통해 미팅 목록 조회
  const { data, isLoading, error } = useQuery({
    queryKey: ['meetings', projectUrl, currentPage],
    queryFn: () =>
      meetingService.getMeetings(projectUrl!, {
        page: currentPage,
        size: 10,
      }),
    enabled: !!projectUrl,
  })

  // 노트 생성 mutation
  const createMeetingMutation = useMutation({
    mutationFn: async () => {
      // TODO: 템플릿 선택 기능 추가 시 templateId를 동적으로 받도록 수정
      const DEFAULT_TEMPLATE_ID = 1
      return meetingService.createMeeting(projectUrl!, DEFAULT_TEMPLATE_ID)
    },
    onSuccess: data => {
      // 목록 캐시 무효화
      queryClient.invalidateQueries({ queryKey: ['meetings', projectUrl] })
      // 생성된 스크럼 페이지로 이동
      navigate(`/projects/${projectUrl}/meetings/${data.id}`)
    },
  })

  const handleCreateMeeting = () => {
    createMeetingMutation.mutate()
  }
  const handlePageChange = (page: number) => {
    setCurrentPage(page)
  }

  // 로딩 상태
  if (isLoading) {
    return (
      <div className="container p-4">
        <h1 className="text-3xl font-bold mb-4">회의록 목록</h1>
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
        <h1 className="text-3xl font-bold mb-4">회의록 목록</h1>
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
      <h1 className="text-3xl font-bold mb-4">회의록 목록</h1>
      <Button
        onClick={handleCreateMeeting}
        disabled={createMeetingMutation.isPending}
      >
        <Plus className="mr-2 h-4 w-4" />
        {createMeetingMutation.isPending ? '생성 중...' : '데일리 스크럼 생성'}
      </Button>
      <ContentListTable data={pagedData} onPageChange={handlePageChange} />
    </div>
  )
}
