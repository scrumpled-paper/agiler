// components/layout/sidebar/items/ProjectItem.tsx

import { Link, useLocation } from 'react-router-dom'
import { SidebarMenuButton, SidebarMenuItem } from '@/components/ui/sidebar'
import type { ProjectInfo } from '@/types'

interface ProjectItemProps {
  project: ProjectInfo
}

export function ProjectItem({ project }: ProjectItemProps) {
  const location = useLocation()
  const isActive = location.pathname === project.url

  return (
    <SidebarMenuItem>
      <SidebarMenuButton asChild isActive={isActive}>
        <Link
          to={`/projects/${project.url}`}
          className="flex items-center gap-2"
        >
          {project.imageUrl && (
            <img
              src={project.imageUrl}
              alt={project.title}
              className="h-6 w-6 rounded object-cover"
            />
          )}
          <span className="truncate">{project.title}</span>
        </Link>
      </SidebarMenuButton>
    </SidebarMenuItem>
  )
}
