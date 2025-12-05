import type {
  setScheduleNotificationPayload,
  subscribeIssueNotificationPayload,
} from '@/types/notification'
import { apiClient } from '../client'

export const notificationService = {
  apiUrl: `/api/v1/notifications`,
  projectApiUrl: `/api/v1/projects`,

  // 이슈
  async getIssueSubscriptions(projectUrl: string) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/subscriptions`
    const response = await apiClient.get(url)
    return response
  },
  async unsubscribeIssueNotification(
    projectUrl: string,
    subscriptionId: string
  ) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/subscriptions/${subscriptionId}`
    const response = await apiClient.delete(url)
    return response
  },
  async subscribeIssueNotification(
    projectUrl: string,
    payload: subscribeIssueNotificationPayload
  ) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/subscriptions`
    const response = await apiClient.post(url, payload)
    return response
  },
  async setScheduleNotification(
    projectUrl: string,
    payload: setScheduleNotificationPayload
  ) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/schedule/`
    const response = await apiClient.post(url, payload)
    return response
  },

  // 채널
  async getRegisteredChannels(projectUrl: string) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/channels/`
    const response = await apiClient.get(url)
    return response
  },
  async deleteChannel(projectUrl: string, channelId: string) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/channels/${channelId}`
    await apiClient.delete(url)
  },
  async startSlackIntegration(projectUrl: string) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/slack/connect`
    const response = await apiClient.get(url)
    return response
  },
  async startDiscordIntegration(projectUrl: string) {
    const url = `${this.projectApiUrl}/${projectUrl}/notifications/discord/connect`
    const response = await apiClient.get(url)
    return response
  },
}
