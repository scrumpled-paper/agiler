import { projectService } from '@/api/services/projectService'
import { userService } from '@/api/services/userService'
import type { UserInfo, UserUpdateParams } from '@/types'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

// 유저 정보를 불러오는 훅
export const useUserInfo = (context: string, projectUrl?: string) => {
  // 1. 대시보드(전역) 유저 정보 쿼리
  const dashboardQuery = useQuery({
    queryKey: ['userInfo', 'dashboard'],
    queryFn: async () => {
      console.log('[useUserInfo] Fetching dashboard user info')
      const response = await userService.getUserInfo()
      return response
    },
    enabled: context === 'dashboard',
    staleTime: 5 * 60 * 1000,
  })

  // 2. 프로젝트별 유저 정보 쿼리
  const projectQuery = useQuery({
    queryKey: ['userInfo', 'project', projectUrl],
    queryFn: async () => {
      if (!projectUrl)
        throw new Error('projectUrl is required for project context')
      console.log('[useUserInfo] Fetching project user info for:', projectUrl)
      const response = await projectService.getUserInfo(projectUrl)
      return response
    },
    // 'project' 컨텍스트이고 projectUrl이 유효할 때만 실행
    enabled: context === 'project' && !!projectUrl,
    staleTime: 5 * 60 * 1000,
  })

  // 현재 context에 맞는 데이터 반환
  return context === 'dashboard' ? dashboardQuery.data : projectQuery.data
}

// User Profile 업데이트 뮤테이션 훅
export const useUserProfileMutation = (
  context: 'dashboard' | 'project',
  projectUrl?: string
) => {
  const queryClient = useQueryClient()

  return useMutation<UserInfo, Error, UserUpdateParams>({
    mutationFn: async payload => {
      if (context === 'project' && projectUrl) {
        // 프로젝트 컨텍스트: projectService를 통해 이름/이미지 변경
        // 이 API는 UserUpdatePayload와 projectUrl을 받아야 한다고 가정
        await projectService.updateMyProfile(projectUrl, payload)
        return projectService.getUserInfo(projectUrl)
      }
      // 대시보드 컨텍스트: userService를 통해 이름/이미지 변경
      await userService.updateUserNickname(payload)
      return userService.getUserInfo()
    },
    onSuccess: updatedUser => {
      // 성공 시, useUserInfo에서 사용한 쿼리 키를 무효화하여 데이터를 즉시 다시 가져오게 함
      if (context === 'project' && projectUrl) {
        queryClient.invalidateQueries({
          queryKey: ['userInfo', 'project', projectUrl],
        })
      } else if (context === 'dashboard') {
        queryClient.invalidateQueries({ queryKey: ['userInfo', 'dashboard'] })
      }

      // 낙관적 업데이트를 위해 다른 쿼리(예: 사이드바)도 업데이트 가능
      console.log('User profile updated successfully:', updatedUser)
    },
    onError: error => {
      console.error('Failed to update user profile:', error)
      // 사용자에게 에러 메시지를 표시하는 로직 추가
    },
  })
}
