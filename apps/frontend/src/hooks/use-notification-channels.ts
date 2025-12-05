import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { notificationService } from '@/api/services/notificationService'
import type { GetRegisteredChannelsResponse } from '@/types/notification'

export function useNotificationChannels(projectUrl: string) {
  const queryClient = useQueryClient()

  // 등록된 채널 조회
  const {
    data: channels,
    isLoading,
    error,
  } = useQuery<GetRegisteredChannelsResponse>({
    queryKey: ['notificationChannels', projectUrl],
    queryFn: () => notificationService.getRegisteredChannels(projectUrl),
    enabled: !!projectUrl,
  })

  // 채널 삭제
  const deleteChannelMutation = useMutation({
    mutationFn: (channelId: number) =>
      notificationService.deleteChannel(projectUrl, channelId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['notificationChannels', projectUrl],
      })
    },
  })

  // Slack 연동 시작
  const startSlackIntegration = useMutation({
    mutationFn: () => notificationService.startSlackIntegration(projectUrl),
    onSuccess: response => {
      // API 응답에 리다이렉트 URL이 있으면 해당 URL로 이동
      if (response?.data?.authUrl) {
        window.location.href = response.data.authUrl
      }
    },
  })

  // Discord 연동 시작
  const startDiscordIntegration = useMutation({
    mutationFn: () => notificationService.startDiscordIntegration(projectUrl),
    onSuccess: response => {
      // API 응답에 리다이렉트 URL이 있으면 해당 URL로 이동
      if (response?.data?.authUrl) {
        window.location.href = response.data.authUrl
      }
    },
  })

  return {
    channels: channels?.channels || [],
    isLoading,
    error,
    deleteChannel: deleteChannelMutation.mutate,
    isDeletingChannel: deleteChannelMutation.isPending,
    startSlackIntegration: startSlackIntegration.mutate,
    isStartingSlack: startSlackIntegration.isPending,
    startDiscordIntegration: startDiscordIntegration.mutate,
    isStartingDiscord: startDiscordIntegration.isPending,
  }
}
