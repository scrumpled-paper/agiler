export interface subscribeIssueNotificationPayload {
  issueId: number
  fromKanbanConfigId: number
  toKanbanConfigId: number
}
export interface setScheduleNotificationPayload {
  issueId: number
  delayInMinutes: number
  message: string
}

export interface Subscript {
  issueTitle: string
  targetUser: string
  statusName: string
  id: number
  issueId: number
  fromKanbanConfigId: number
  toKanbanConfigId: number
}
export interface getIssueSubscriptionsResponse {
  subscriptions: Subscript[]
}

export interface NotificationChannel {
  id: number
  channelType: string
}

export interface GetRegisteredChannelsResponse {
  channels: NotificationChannel[]
}

export interface Schedule {
  id: number
  issueId: number
  issueTitle: string
  notificationTime: string // "2025-12-08T10:23:21"
}

export interface GetScheduleNotificationResponse {
  schedules: Schedule[]
}
