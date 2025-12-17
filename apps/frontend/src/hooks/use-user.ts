import { projectService } from '@/api/services/projectService'
import { userService } from '@/api/services/userService'
import { s3Service } from '@/api/services/s3Service'
import type {
  UserInfo,
  UserUpdateParams,
  ProjectProfileUpdateParams,
} from '@/types'
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

// 대시보드 유저 프로필 업데이트 뮤테이션 훅
export const useDashboardProfileMutation = () => {
  const queryClient = useQueryClient()

  return useMutation<UserInfo, Error, UserUpdateParams>({
    mutationFn: async payload => {
      await userService.updateUserNickname(payload)
      return userService.getUserInfo()
    },
    onSuccess: updatedUser => {
      queryClient.invalidateQueries({ queryKey: ['userInfo', 'dashboard'] })
      console.log('Dashboard profile updated successfully:', updatedUser)
    },
    onError: error => {
      console.error('Failed to update dashboard profile:', error)
    },
  })
}

// 프로젝트 유저 프로필 업데이트 뮤테이션 훅
export const useProjectProfileMutation = (projectUrl: string) => {
  const queryClient = useQueryClient()

  return useMutation<UserInfo, Error, ProjectProfileUpdateParams>({
    mutationFn: async payload => {
      await projectService.updateMyProfile(projectUrl, payload)
      return projectService.getUserInfo(projectUrl)
    },
    onSuccess: updatedUser => {
      queryClient.invalidateQueries({
        queryKey: ['userInfo', 'project', projectUrl],
      })
      console.log('Project profile updated successfully:', updatedUser)
    },
    onError: error => {
      console.error('Failed to update project profile:', error)
    },
  })
}

// 대시보드 이미지 업로드 뮤테이션 훅
export const useDashboardImageUploadMutation = () => {
  const queryClient = useQueryClient()

  return useMutation<void, Error, File>({
    mutationFn: async file => {
      const objectKey = await s3Service.uploadProfileImage(file)
      await userService.updateUserImage(objectKey)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userInfo', 'dashboard'] })
    },
    onError: error => {
      console.error('Failed to upload dashboard image:', error)
    },
  })
}

// 프로젝트 이미지 업로드 뮤테이션 훅
export const useProjectImageUploadMutation = (projectUrl: string) => {
  const queryClient = useQueryClient()

  return useMutation<void, Error, File>({
    mutationFn: async file => {
      const objectKey = await s3Service.uploadProfileImage(file)
      await projectService.updateUserImage(projectUrl, objectKey)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['userInfo', 'project', projectUrl],
      })
    },
    onError: error => {
      console.error('Failed to upload project image:', error)
    },
  })
}

// 대시보드 이미지 삭제 뮤테이션 훅
export const useDashboardImageDeleteMutation = () => {
  const queryClient = useQueryClient()

  return useMutation<void, Error, void>({
    mutationFn: async () => {
      await userService.deleteUserImage()
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userInfo', 'dashboard'] })
    },
    onError: error => {
      console.error('Failed to delete dashboard image:', error)
    },
  })
}

// 프로젝트 이미지 삭제 뮤테이션 훅
export const useProjectImageDeleteMutation = (projectUrl: string) => {
  const queryClient = useQueryClient()

  return useMutation<void, Error, void>({
    mutationFn: async () => {
      await projectService.deleteUserImage(projectUrl)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['userInfo', 'project', projectUrl],
      })
    },
    onError: error => {
      console.error('Failed to delete project image:', error)
    },
  })
}
