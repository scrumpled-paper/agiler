// components/layout/sidebar/sections/MemberListSection.tsx

import { useParams } from 'react-router-dom'
import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
} from '@/components/ui/sidebar'
import { DisplayItem } from '../items/DisplayItem'
import { ShowMoreButton } from '../buttons/ShowMoreButton'
import type { DisplaySection } from '@/lib/sidebar/types'
import type { ProjectMember } from '@/types'

export interface MemberListSectionProps {
  section: DisplaySection
  members?: ProjectMember[]
}

export function MemberListSection({
  section,
  members = [],
}: MemberListSectionProps) {
  const params = useParams<{ projectUrl: string }>()

  // :projectUrl 등의 동적 경로를 실제 값으로 치환
  const resolveRoute = (route?: string) => {
    if (!route) return undefined
    if (params.projectUrl && route.includes(':projectUrl')) {
      return route.replace(':projectUrl', `/projects/${params.projectUrl}`)
    }
    return route
  }

  // 최대 3명만 표시
  const displayedMembers = members.slice(0, 3)

  return (
    <SidebarGroup>
      <SidebarGroupLabel>
        {section.icon && <span className="mr-2">{section.icon}</span>}
        {section.title}
      </SidebarGroupLabel>
      <SidebarGroupContent>
        <div className="space-y-1">
          {displayedMembers.length > 0 ? (
            displayedMembers.map(member => (
              <DisplayItem key={member.peopleId} member={member} />
            ))
          ) : (
            <p className="px-2 py-1.5 text-sm text-muted-foreground">
              멤버를 불러오는 중...
            </p>
          )}
        </div>
        {section.hasShowMore && section.showMoreRoute && members.length > 3 && (
          <div className="mt-2">
            <ShowMoreButton
              to={
                resolveRoute(section.showMoreRoute) ||
                `/projects/${params.projectUrl}/settings/members`
              }
              label="더보기"
            />
          </div>
        )}
      </SidebarGroupContent>
    </SidebarGroup>
  )
}
