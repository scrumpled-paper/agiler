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

export interface getIssueSubscriptionsResponse {
  subscriptions: [
    {
      createdAt: Date
      updatedAt: Date
      deletedAt: Date
      id: number
      userId: number
      profileId: number
      issueId: number
      fromKanbanConfigId: number
      toKanbanConfigId: number
    },
  ]
}
