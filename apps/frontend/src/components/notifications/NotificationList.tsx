import { useQuery } from '@tanstack/react-query'
import { notificationService } from '@/api/services/notificationService'
import { useSidebarParams } from '@/hooks/hooks'
import { Loader2, Bell, BellOff } from 'lucide-react'
import { Button } from '@/components/ui/button'

interface NotificationItemProps {
  subscription: {
    id: number
    issueId: number
    fromKanbanConfigId: number
    toKanbanConfigId: number
    createdAt: Date
    updatedAt: Date
  }
  onUnsubscribe: (id: number) => void
}

function NotificationItem({
  subscription,
  onUnsubscribe,
}: NotificationItemProps) {
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
                Issue #{subscription.issueId}
              </span>
            </div>
            <p className="text-sm text-muted-foreground">
              Status change: Column {subscription.fromKanbanConfigId} →{' '}
              {subscription.toKanbanConfigId}
            </p>
            <p className="text-xs text-muted-foreground">
              {new Date(subscription.createdAt).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </p>
          </div>
        </div>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => onUnsubscribe(subscription.id)}
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

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['issueSubscriptions', projectUrl],
    queryFn: () => notificationService.getIssueSubscriptions(projectUrl),
    enabled: !!projectUrl,
  })

  const handleUnsubscribe = async (subscriptionId: number) => {
    try {
      await notificationService.unsubscribeIssueNotification(
        projectUrl,
        subscriptionId.toString()
      )
      refetch()
    } catch (err) {
      console.error('Failed to unsubscribe:', err)
    }
  }

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
        <Button
          variant="outline"
          size="sm"
          onClick={() => refetch()}
          className="mt-4"
        >
          Retry
        </Button>
      </div>
    )
  }

  const subscriptions = data?.subscriptions || []

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
      {subscriptions.map(subscription => (
        <NotificationItem
          key={subscription.id}
          subscription={subscription}
          onUnsubscribe={handleUnsubscribe}
        />
      ))}
    </div>
  )
}
