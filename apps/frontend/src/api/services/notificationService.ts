import type {
  getIssueSubscriptionsResponse,
  GetRegisteredChannelsResponse,
  setScheduleNotificationPayload,
  subscribeIssueNotificationPayload,
} from '@/types/notification'
import { apiClient } from '../client'

export const notificationService = {
  apiUrl: `/api/v1/notifications`,
  projectApiUrl: `/api/v1/projects`,

  // 구독중인 이슈 조회
  async getIssueSubscriptions(
    projectUrl: string | undefined
  ): Promise<getIssueSubscriptionsResponse> {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/subscriptions`
    const response = await apiClient.get(url)
    return response.data
  },
  //이슈 구독 취소
  async unsubscribeIssueNotification(
    projectUrl: string | undefined,
    subscriptionId: string
  ) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/subscriptions/${subscriptionId}`
    const response = await apiClient.delete(url)
    return response.data
  },
  // 이슈 구독 요청
  async subscribeIssueNotification(
    projectUrl: string,
    payload: subscribeIssueNotificationPayload
  ) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/subscriptions`
    const response = await apiClient.post(url, payload)
    return response.data
  },
  // 시간 알림 설정 요청
  async setScheduleNotification(
    projectUrl: string,
    payload: setScheduleNotificationPayload
  ) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/schedule`
    const response = await apiClient.post(url, payload)
    return response.data
  },

  // 알림 채널 조회
  async getRegisteredChannels(
    projectUrl: string
  ): Promise<GetRegisteredChannelsResponse> {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/channels`
    const response = await apiClient.get(url)
    return response.data
  },
  // 채널 삭제
  async deleteChannel(projectUrl: string, channelId: number) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/channels/${channelId}`
    await apiClient.delete(url)
  },
  // 슬랙 연동 요청
  async startSlackIntegration(projectUrl: string) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/slack/connect`
    const response = await apiClient.get(url)
    return response.data
  },
  // 디스코드 연동 요청
  async startDiscordIntegration(projectUrl: string) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/discord/connect`
    const response = await apiClient.get(url)
    return response.data
  },
}
