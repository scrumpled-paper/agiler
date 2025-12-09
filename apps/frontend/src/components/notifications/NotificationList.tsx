import { useQuery } from '@tanstack/react-query'
import { notificationService } from '@/api/services/notificationService'
import { useSidebarParams } from '@/hooks/hooks'
import { Loader2, Bell, BellOff } from 'lucide-react'
import { Button } from '@/components/ui/button'
import type { Schedule, Subscript } from '@/types/notification'
import { useState } from 'react'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'

interface NotificationItemProps {
  isLoading: boolean
  error: Error | null
  data: Subscript | Schedule
  onUnsubscribe: (id: number) => void
}

// type ViewMode = 'subscribe' | 'schedule'

function isSubscript(item: Subscript | Schedule): item is Subscript {
  return (item as Subscript).fromKanbanConfigId !== undefined
}

function formattingDate(date: string): string {
  // 1. Date 객체 생성 (브라우저/환경의 로컬 시간대로 변환됨)
  const dateObj = new Date(date)

  // 2. 한국어 로케일('ko-KR')과 원하는 옵션을 지정하여 출력
  const formattedDate = dateObj.toLocaleDateString('ko-KR', {
    year: 'numeric', // 2025년
    month: 'long', // 12월
    day: 'numeric', // 8일
  })
  // 출력: "2025년 12월 8일"

  const formattedTime = dateObj.toLocaleTimeString('ko-KR', {
    hour: '2-digit', // 오전/오후 10시
    minute: '2-digit', // 23분
    hour12: true, // 오전/오후 표시 사용
  })
  // 출력: "오전 10:23" (시간대는 사용자 환경에 따라 다름)

  const fullDateTime = `${formattedDate} ${formattedTime}`
  return fullDateTime
}

function NotificationItem({
  data,
  onUnsubscribe,
  isLoading,
  error,
}: NotificationItemProps) {
  const isSub = isSubscript(data)
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-sm text-destructive">Failed to load notifications</p>
      </div>
    )
  }
  return (
    <div className="border-b border-border py-4 last:border-0">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 flex-1">
          <div className="rounded-full bg-primary/10 p-2">
            <Bell className="h-4 w-4 text-primary" />
          </div>
          <div className="flex-1 space-y-1">
            <div className="flex items-center gap-2">
              <span className="font-medium text-sm">
                Issue {data.issueTitle}
              </span>
            </div>
            {isSub ? (
              // [ ] fromKanbanConfigId, toKanbanConfigId, issueId
              <>
                <p className="text-sm text-muted-foreground">
                  Status change: {data.statusName}
                </p>
                <p className="text-xs text-muted-foreground">
                  {data.targetUser}
                </p>
              </>
            ) : (
              <>
                <p className="text-xs text-muted-foreground">
                  {formattingDate(data.notificationTime)}
                </p>
              </>
            )}
          </div>
        </div>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onUnsubscribe(data.id)}
          className="shrink-0"
        >
          <BellOff className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}

export function NotificationList() {
  const { projectUrl } = useSidebarParams()
  const [activeTab, setActiveTab] = useState('subscribe')

  // --- Subscript (구독) 데이터 쿼리 ---
  const {
    data: subscriptData,
    isLoading: isSubscriptLoading,
    error: subscriptError,
    refetch: refetchSubscript,
  } = useQuery({
    queryKey: ['issueSubscriptions', projectUrl],
    queryFn: () => notificationService.getIssueSubscriptions(projectUrl),
    // 탭이 'subscribe'일 때만 API 요청 활성화
    enabled: !!projectUrl && activeTab === 'subscribe',
  })

  // ---  Schedule (스케줄) 데이터 쿼리 ---

  const {
    data: scheduleData,
    isLoading: isScheduleLoading,
    error: scheduleError,
    refetch: refetchSchedule,
  } = useQuery({
    queryKey: ['scheduleNotifications', projectUrl],
    queryFn: () => notificationService.getScheduleNotifications(projectUrl),
    // 탭이 'schedule'일 때만 API 요청 활성화
    enabled: !!projectUrl && activeTab === 'schedule',
  })

  const handleUnsubscribe = async (subscriptionId: number) => {
    try {
      await notificationService.unsubscribeIssueNotification(
        projectUrl,
        subscriptionId
      )
      refetchSubscript()
    } catch (err) {
      console.error('Failed to unsubscribe:', err)
    }
  }
  const handleDeleteSchedule = async (scheduleId: number) => {
    try {
      await notificationService.deleteScheduleNotification(
        projectUrl,
        scheduleId
      )
      refetchSchedule()
    } catch (err) {
      console.error('Failed to unsubscribe:', err)
    }
  }

  const subscriptions = subscriptData?.subscriptions || []
  const schedules = scheduleData?.schedules || [] // 가정: Schedule API 응답 구조

  if (subscriptions.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="mx-auto w-12 h-12 rounded-full bg-muted flex items-center justify-center mb-4">
          <Bell className="h-6 w-6 text-muted-foreground" />
        </div>
        <p className="text-sm text-muted-foreground">No active subscriptions</p>
        <p className="text-xs text-muted-foreground mt-1">
          Subscribe to issues to get notified about status changes
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-0">
      <Tabs
        defaultValue="subscribe"
        value={activeTab}
        onValueChange={setActiveTab}
        className="w-full"
      >
        <TabsList className="w-full ">
          <TabsTrigger className="w-1/2 " value="subscribe">
            subscribe
          </TabsTrigger>
          <TabsTrigger className="w-1/2" value="schedule">
            schedule
          </TabsTrigger>
        </TabsList>
        <TabsContent value="subscribe">
          {subscriptions.map(subscription => (
            <NotificationItem
              key={subscription.id}
              data={subscription}
              onUnsubscribe={handleUnsubscribe}
              isLoading={isSubscriptLoading}
              error={subscriptError}
            />
          ))}
        </TabsContent>
        <TabsContent value="schedule">
          {schedules.map(schedule => (
            <NotificationItem
              key={schedule.id}
              data={schedule}
              onUnsubscribe={handleDeleteSchedule}
              isLoading={isScheduleLoading}
              error={scheduleError}
            />
          ))}
        </TabsContent>
      </Tabs>
    </div>
  )
}
