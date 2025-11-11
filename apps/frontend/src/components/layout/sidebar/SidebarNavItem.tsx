import { SidebarMenuButton } from '@/components/ui/sidebar'
import { Link } from 'react-router-dom'

interface SidebarNavItemProps {
  title: string
  url: string
}

export const SidebarNavItem = ({ title, url }: SidebarNavItemProps) => {
  return (
    <SidebarMenuButton asChild>
      <Link to={url!}>{title}</Link>
    </SidebarMenuButton>
  )
}
