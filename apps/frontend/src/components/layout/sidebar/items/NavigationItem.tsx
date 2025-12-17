// components/layout/sidebar/items/NavigationItem.tsx

import { Link, useLocation } from 'react-router-dom'
import { SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar'
import type { NavigationItem } from '@/lib/sidebar/types'

export function NavigationItem({ label, route, icon: Icon }: NavigationItem) {
  const location = useLocation()
  const isActive = location.pathname === route

  return (
    <SidebarMenuItem>
      <SidebarMenuButton asChild isActive={isActive}>
        <Link to={route}>
          {Icon && (
            <span className="">
              <Icon className="h-5 w-5 " />
            </span>
          )}
          {label}
        </Link>
      </SidebarMenuButton>
    </SidebarMenuItem>
  )
}
