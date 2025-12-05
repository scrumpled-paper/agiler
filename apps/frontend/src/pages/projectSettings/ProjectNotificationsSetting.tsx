import { useParams } from 'react-router-dom'
import { useNotificationChannels } from '@/hooks/use-notification-channels'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { AlertCircle, Plus, Trash2 } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'
import type { NotificationChannel } from '@/types/notification'

export default function ProjectNotificationsSetting() {
  const { projectUrl } = useParams<{ projectUrl: string }>()

  const {
    channels,
    isLoading,
    error,
    deleteChannel,
    isDeletingChannel,
    startSlackIntegration,
    isStartingSlack,
    startDiscordIntegration,
    isStartingDiscord,
  } = useNotificationChannels(projectUrl || '')

  if (!projectUrl) {
    return (
      <div className="container p-6">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>프로젝트 URL을 찾을 수 없습니다.</AlertDescription>
        </Alert>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container p-6">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            알림 채널 정보를 불러오는데 실패했습니다.
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  const slackChannels = channels.filter(
    (ch: NotificationChannel) => ch.channelType === 'slack'
  )
  const discordChannels = channels.filter(
    (ch: NotificationChannel) => ch.channelType === 'discord'
  )

  return (
    <div className="container p-10 space-y-6">
      <div>
        <h1 className="text-center text-[40px] font-bold leading-[48px] font-['Roboto'] pb-10">
          Notification Channels
        </h1>
        <p className="text-muted-foreground">
          프로젝트 알림을 받을 Slack 또는 Discord 채널을 연동하세요.
        </p>
      </div>

      <Separator />

      {/* Slack 섹션 */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <CardTitle>Slack</CardTitle>
              {/* <Badge variant="outline">{slackChannels.length}개 연동됨</Badge> */}
            </div>
            <Button
              onClick={() => startSlackIntegration()}
              disabled={isStartingSlack}
              size="sm"
            >
              <Plus className="h-4 w-4 mr-2" />
              Slack 연동하기
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-sm text-muted-foreground">로딩 중...</div>
          ) : slackChannels.length === 0 ? (
            <div className="text-sm text-muted-foreground">
              연동된 Slack 채널이 없습니다.
            </div>
          ) : (
            <div className="space-y-3">
              {slackChannels.map((channel: NotificationChannel) => (
                <div
                  key={channel.id}
                  className="flex items-center justify-between p-4 border rounded-lg"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <h3 className="font-medium">{channel.channelType}</h3>
                      <Badge variant="default" className="text-xs">
                        활성
                      </Badge>
                    </div>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => deleteChannel(channel.id)}
                    disabled={isDeletingChannel}
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Discord 섹션 */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <CardTitle>Discord</CardTitle>
              <Badge variant="outline">{discordChannels.length}개 연동됨</Badge>
            </div>
            <Button
              onClick={() => startDiscordIntegration()}
              disabled={isStartingDiscord}
              size="sm"
            >
              <Plus className="h-4 w-4 mr-2" />
              Discord 연동하기
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-sm text-muted-foreground">로딩 중...</div>
          ) : discordChannels.length === 0 ? (
            <div className="text-sm text-muted-foreground">
              연동된 Discord 채널이 없습니다.
            </div>
          ) : (
            <div className="space-y-3">
              {discordChannels.map((channel: NotificationChannel) => (
                <div
                  key={channel.id}
                  className="flex items-center justify-between p-4 border rounded-lg"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <h3 className="font-medium">{channel.channelType}</h3>
                      <Badge variant="default" className="text-xs">
                        활성
                      </Badge>
                    </div>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => deleteChannel(channel.id)}
                    disabled={isDeletingChannel}
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
