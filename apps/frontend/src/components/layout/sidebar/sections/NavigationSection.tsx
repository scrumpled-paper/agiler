// components/layout/sidebar/sections/NavigationSection.tsx

import { useParams } from 'react-router-dom'
import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
} from '@/components/ui/sidebar'
import { NavigationItem } from '../items/NavigationItem'
import type { NavigationSection as NavigationSectionType } from '@/lib/sidebar/types'

interface NavigationSectionProps {
  section: NavigationSectionType
}

export function NavigationSection({ section }: NavigationSectionProps) {
  const params = useParams<{ projectUrl: string }>()

  // :projectUrl 등의 동적 경로를 실제 값으로 치환
  const resolveRoute = (route: string) => {
    if (params.projectUrl && route.includes(':projectUrl')) {
      return route.replace(':projectUrl', `/projects/${params.projectUrl}`)
    }
    return route
  }

  return (
    <SidebarGroup>
      {section.title && (
        <SidebarGroupLabel>
          {section.icon && <span className="mr-2">{section.icon}</span>}
          {section.title}
        </SidebarGroupLabel>
      )}
      <SidebarGroupContent>
        <SidebarMenu>
          {section.items.map((item, index) => (
            <NavigationItem
              key={`${item.route}-${index}`}
              label={item.label}
              route={resolveRoute(item.route)}
              icon={item.icon}
            />
          ))}
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  )
}
