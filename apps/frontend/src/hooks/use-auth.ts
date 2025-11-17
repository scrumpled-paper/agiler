import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { authService, type User } from '@/api/services/authService'

export function useAuth() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  // 현재 사용자 정보 조회
  const {
    data: user,
    isLoading,
    error,
  } = useQuery<User | null>({
    queryKey: ['currentUser'],
    queryFn: async () => {
      try {
        return await authService.getCurrentUser()
      } catch {
        return null
      }
    },
    retry: false,
    staleTime: 1000 * 60 * 5, // 5분간 캐시 유지
  })

  // 로그아웃 mutation
  const logoutMutation = useMutation({
    mutationFn: authService.logout,
    onSuccess: () => {
      // 사용자 정보 캐시 초기화
      queryClient.setQueryData(['currentUser'], null)
      queryClient.clear()
      // 로그인 페이지로 이동
      navigate('/login')
    },
  })

  return {
    user,
    isAuthenticated: !!user,
    isLoading,
    error,
    logout: () => logoutMutation.mutate(),
    isLoggingOut: logoutMutation.isPending,
  }
}
