import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { projectService } from '@/api/services/projectService'
import { MemberList } from '@/components/members/MemberList'
import UserProfileBox from '@/components/UserProfileBox'

export default function ProjectUserSetting() {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const queryClient = useQueryClient()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['project-members', projectUrl],
    queryFn: () =>
      projectService.getProjectMember({
        projectUrl: projectUrl!,
        page: 0,
        size: 100,
      }),
    enabled: !!projectUrl,
  })

  // 역할 변경 mutation
  const roleChangeMutation = useMutation({
    mutationFn: ({
      memberId,
      newRole,
    }: {
      memberId: number
      newRole: string
    }) => {
      if (!projectUrl) throw new Error('Project URL is required')
      return projectService.updateMemberRole(projectUrl, {
        profileId: memberId,
        role: newRole,
      })
    },
    onSuccess: () => {
      // 성공 시 캐시 무효화하여 멤버 목록 다시 로드
      queryClient.invalidateQueries({
        queryKey: ['project-members', projectUrl],
      })
      console.log('Role updated successfully')
    },
    onError: error => {
      console.error('Failed to update role:', error)
      alert('역할 변경에 실패했습니다.')
    },
  })

  // 멤버 제거 mutation
  const removeMemberMutation = useMutation({
    mutationFn: (_memberId: number) => {
      if (!projectUrl) throw new Error('Project URL is required')
      // TODO [ ] : API 엔드포인트가 준비되면 구현
      return Promise.reject(new Error('Not implemented yet'))
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['project-members', projectUrl],
      })
      console.log('Member removed successfully')
    },
    onError: error => {
      console.error('Failed to remove member:', error)
      alert('멤버 제거에 실패했습니다.')
    },
  })

  const handleRoleChange = (memberId: number, newRole: string) => {
    roleChangeMutation.mutate({ memberId, newRole })
  }

  const handleRemoveMember = (memberId: number) => {
    if (confirm('정말 이 멤버를 제거하시겠습니까?')) {
      removeMemberMutation.mutate(memberId)
    }
  }

  if (isLoading) {
    return (
      <div className="flex h-96 items-center justify-center">로딩 중...</div>
    )
  }

  if (isError || !data) {
    return <div>에러가 발생했습니다.</div>
  }

  const members = data.contents.map(member => ({
    nickname: member.nickname,
    imageUrl: member.imageUrl,
    role: member.role,
  }))

  return (
    <div className="container flex flex-col justify-center items-center gap-10 p-10">
      <div className="w-full">
        <h1 className="text-center text-[40px] font-bold leading-[48px] font-['Roboto'] pb-10">
          My Information
        </h1>
        <UserProfileBox context="project" projectUrl={projectUrl} />
      </div>
      <div className="w-full mt-10">
        <h1 className="text-center text-[40px] font-bold leading-[48px] font-['Roboto'] pb-10">
          Current Members
        </h1>
        <MemberList
          members={members}
          onRoleChange={handleRoleChange}
          onRemove={handleRemoveMember}
          canEdit={true}
        />
      </div>
    </div>
  )
}
