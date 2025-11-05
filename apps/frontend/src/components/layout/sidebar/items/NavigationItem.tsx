// components/layout/sidebar/items/NavigationItem.tsx

import { Link, useLocation } from 'react-router-dom'
import { SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar'

interface NavigationItemProps {
  label: string
  route: string
  icon?: string
}

export function NavigationItem({ label, route, icon }: NavigationItemProps) {
  const location = useLocation()
  const isActive = location.pathname === route

  return (
    <SidebarMenuItem>
      <SidebarMenuButton asChild isActive={isActive}>
        <Link to={route}>
          {icon && <span className="mr-2">{icon}</span>}
          {label}
        </Link>
      </SidebarMenuButton>
    </SidebarMenuItem>
  )
}
