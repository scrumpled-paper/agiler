import { describe, it, expect, vi, beforeEach } from 'vitest'
import { notificationService } from './notificationService'
import { apiClient } from '../client'
import type {
  getIssueSubscriptionsResponse,
  GetRegisteredChannelsResponse,
  setScheduleNotificationPayload,
  subscribeIssueNotificationPayload,
} from '@/types/notification'

// Mock apiClient
vi.mock('../client', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('notificationService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getIssueSubscriptions', () => {
    it('should fetch issue subscriptions successfully', async () => {
      const mockResponse: getIssueSubscriptionsResponse = {
        subscriptions: [
          {
            issueTitle: 'issue 1',
            targetUser: 'user 1',
            statusName: 'done',
            id: 1,
            issueId: 100,
            fromKanbanConfigId: 1,
            toKanbanConfigId: 2,
          },
        ],
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result =
        await notificationService.getIssueSubscriptions('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/subscriptions'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when fetching issue subscriptions fails', async () => {
      const axiosError = new Error('Failed to fetch subscriptions')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        notificationService.getIssueSubscriptions('test-project')
      ).rejects.toThrow('Failed to fetch subscriptions')
    })
  })

  describe('unsubscribeIssueNotification', () => {
    it('should unsubscribe from issue notification successfully', async () => {
      const mockResponse = { success: true }

      vi.mocked(apiClient.delete).mockResolvedValue({ data: mockResponse })

      const result = await notificationService.unsubscribeIssueNotification(
        'test-project',
        123
      )

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/subscriptions/123'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when unsubscribing fails', async () => {
      const axiosError = new Error('Unsubscribe failed')

      vi.mocked(apiClient.delete).mockRejectedValue(axiosError)

      await expect(
        notificationService.unsubscribeIssueNotification('test-project', 1234)
      ).rejects.toThrow('Unsubscribe failed')
    })
  })

  describe('subscribeIssueNotification', () => {
    it('should subscribe to issue notification successfully', async () => {
      const payload: subscribeIssueNotificationPayload = {
        issueId: 100,
        fromKanbanConfigId: 1,
        toKanbanConfigId: 2,
      }
      const mockResponse = { subscriptionId: 'sub-456' }

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse })

      const result = await notificationService.subscribeIssueNotification(
        'test-project',
        payload
      )

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/subscriptions',
        payload
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when subscribing fails', async () => {
      const payload: subscribeIssueNotificationPayload = {
        issueId: 100,
        fromKanbanConfigId: 1,
        toKanbanConfigId: 2,
      }
      const axiosError = new Error('Subscribe failed')

      vi.mocked(apiClient.post).mockRejectedValue(axiosError)

      await expect(
        notificationService.subscribeIssueNotification('test-project', payload)
      ).rejects.toThrow('Subscribe failed')
    })
  })

  describe('setScheduleNotification', () => {
    it('should set schedule notification successfully', async () => {
      const payload: setScheduleNotificationPayload = {
        issueId: 100,
        delayInMinutes: 30,
        message: 'Reminder: Task due soon',
      }
      const mockResponse = { scheduleId: 'schedule-789' }

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse })

      const result = await notificationService.setScheduleNotification(
        'test-project',
        payload
      )

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/schedule',
        payload
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when setting schedule notification fails', async () => {
      const payload: setScheduleNotificationPayload = {
        issueId: 100,
        delayInMinutes: 30,
        message: 'Reminder: Task due soon',
      }
      const axiosError = new Error('Schedule creation failed')

      vi.mocked(apiClient.post).mockRejectedValue(axiosError)

      await expect(
        notificationService.setScheduleNotification('test-project', payload)
      ).rejects.toThrow('Schedule creation failed')
    })
  })

  describe('getRegisteredChannels', () => {
    it('should fetch registered notification channels successfully', async () => {
      const mockResponse: GetRegisteredChannelsResponse = {
        channels: [
          {
            id: 1,
            channelType: 'slack',
          },
          {
            id: 2,
            channelType: 'discord',
          },
        ],
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result =
        await notificationService.getRegisteredChannels('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/channels'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when fetching channels fails', async () => {
      const axiosError = new Error('Failed to fetch channels')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        notificationService.getRegisteredChannels('test-project')
      ).rejects.toThrow('Failed to fetch channels')
    })
  })

  describe('deleteChannel', () => {
    it('should delete notification channel successfully', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({ data: undefined })

      await notificationService.deleteChannel('test-project', 123)

      expect(apiClient.delete).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/channels/123'
      )
    })

    it('should handle error when deleting channel fails', async () => {
      const axiosError = new Error('Delete failed')

      vi.mocked(apiClient.delete).mockRejectedValue(axiosError)

      await expect(
        notificationService.deleteChannel('test-project', 123)
      ).rejects.toThrow('Delete failed')
    })
  })

  describe('startSlackIntegration', () => {
    it('should start Slack integration successfully', async () => {
      const mockResponse = {
        authUrl: 'https://slack.com/oauth/authorize?client_id=xxx',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result =
        await notificationService.startSlackIntegration('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/slack/connect'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when starting Slack integration fails', async () => {
      const axiosError = new Error('Slack integration failed')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        notificationService.startSlackIntegration('test-project')
      ).rejects.toThrow('Slack integration failed')
    })
  })

  describe('startDiscordIntegration', () => {
    it('should start Discord integration successfully', async () => {
      const mockResponse = {
        authUrl: 'https://discord.com/api/oauth2/authorize?client_id=yyy',
      }

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse })

      const result =
        await notificationService.startDiscordIntegration('test-project')

      expect(apiClient.get).toHaveBeenCalledWith(
        '/api/v1/projects/test-project/notifications/discord/connect'
      )
      expect(result).toEqual(mockResponse)
    })

    it('should handle error when starting Discord integration fails', async () => {
      const axiosError = new Error('Discord integration failed')

      vi.mocked(apiClient.get).mockRejectedValue(axiosError)

      await expect(
        notificationService.startDiscordIntegration('test-project')
      ).rejects.toThrow('Discord integration failed')
    })
  })
})
