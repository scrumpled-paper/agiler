import { useParams } from 'react-router-dom'
import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
} from '@/components/ui/sidebar'
import { ProjectItem } from '../items/ProjectItem'
import { ShowMoreButton } from '../buttons/ShowMoreButton'
import type { ListSection } from '@/lib/sidebar/types'
import type { ProjectInfo } from '@/types'

export interface ProjectListSectionProps {
  section: ListSection
  projects?: ProjectInfo[]
}

export function ProjectListSection({
  section,
  projects = [],
}: ProjectListSectionProps) {
  const params = useParams<{ projectUrl: string }>()

  // :projectUrl 등의 동적 경로를 실제 값으로 치환
  const resolveRoute = (route?: string) => {
    if (!route) return undefined
    if (params.projectUrl && route.includes(':projectUrl')) {
      return route.replace(':projectUrl', `/projects/${params.projectUrl}`)
    }
    return route
  }

  // 최대 3개만 표시
  const displayedProjects = projects.slice(0, 3)

  return (
    <SidebarGroup>
      <SidebarGroupLabel>{section.title}</SidebarGroupLabel>
      <SidebarGroupContent>
        <SidebarMenu>
          {displayedProjects.map(project => (
            <ProjectItem key={project.url} project={project} />
          ))}
        </SidebarMenu>
        {section.hasShowMore && section.showMoreRoute && (
          <div className="mt-2">
            <ShowMoreButton
              to={resolveRoute(section.showMoreRoute) || '/dashboard/projects'}
              label="더보기"
            />
          </div>
        )}
      </SidebarGroupContent>
    </SidebarGroup>
  )
}
