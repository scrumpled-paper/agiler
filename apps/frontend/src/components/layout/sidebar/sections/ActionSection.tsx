// components/layout/sidebar/sections/ActionSection.tsx

import { SidebarGroup, SidebarGroupContent } from '@/components/ui/sidebar'
import { Button } from '@/components/ui/button'
import type { ActionSection as ActionSectionType } from '@/lib/sidebar/types'

interface ActionSectionProps {
  section: ActionSectionType
}

export function ActionSection({ section }: ActionSectionProps) {
  const handleClick = () => {
    if (typeof section.action.onClick === 'function') {
      section.action.onClick()
    } else if (section.action.onClick === 'generateLink') {
      // TODO: 실제 링크 생성 로직 구현
      alert('프로젝트 참가 링크 생성 기능은 준비 중입니다.')
    }
  }

  return (
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
  )
}
