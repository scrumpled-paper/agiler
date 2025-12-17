// components/layout/sidebar/sections/ActionSection.tsx

import { SidebarGroup, SidebarGroupContent } from '@/components/ui/sidebar'
import { Button } from '@/components/ui/button'
import type { ActionSection as ActionSectionType } from '@/lib/sidebar/types'
import { useAuth } from '@/hooks/use-auth'
import { useNotificationSheet } from '@/hooks/use-notification-sheet'
import { SidebarSheet } from '../SidebarSheet'
import { NotificationList } from '@/components/notifications/NotificationList'

export interface ActionSectionProps {
  section: ActionSectionType
}

export function ActionSection({ section }: ActionSectionProps) {
  const { logout, isLoggingOut } = useAuth()
  const { isOpen, open, close } = useNotificationSheet()

  const handleClick = () => {
    if (typeof section.action.onClick === 'function') {
      section.action.onClick()
    } else if (section.action.onClick === 'generateLink') {
      // TODO: 실제 링크 생성 로직 구현
      alert('프로젝트 참가 링크 생성 기능은 준비 중입니다.')
    } else if (section.action.onClick === 'logoutLink' && !isLoggingOut) {
      logout()
    } else if (section.action.onClick === 'notification') {
      open()
    }
  }

  return (
    <>
      <SidebarGroup>
        {/* <SidebarGroupLabel>
        {section.icon && <span className="mr-2">{section.icon}</span>}
        {section.title}
      </SidebarGroupLabel> */}
        <SidebarGroupContent>
          <Button
            variant="outline"
            size="sm"
            onClick={handleClick}
            className="w-full"
          >
            {section.action.label}
          </Button>
        </SidebarGroupContent>
      </SidebarGroup>

      {section.action.onClick === 'notification' && (
        <SidebarSheet
          open={isOpen}
          onOpenChange={close}
          title="Notifications"
          description="Your active issue subscriptions"
        >
          <NotificationList />
        </SidebarSheet>
      )}
    </>
  )
}
