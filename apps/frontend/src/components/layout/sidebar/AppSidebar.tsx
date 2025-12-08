import * as React from 'react'
import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
  SidebarRail,
} from '@/components/ui/sidebar'
import { sidebarConfigs } from '@/lib/sidebar/config'
import {
  useSidebarContext,
  useSidebarData,
  useSidebarParams,
} from '@/hooks/hooks'
import { UserInfoSection } from './sections/UserInfoSection'
import { NavigationSection } from './sections/NavigationSection'
import { ProjectListSection } from './sections/ProjectListSection'
import { MemberListSection } from './sections/MemberListSection'
import { ActionSection } from './sections/ActionSection'

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  // 현재 컨텍스트 및 파라미터 가져오기
  const context = useSidebarContext()
  const params = useSidebarParams()
  const config = sidebarConfigs[context]

  // 필요한 데이터 페칭
  const data = useSidebarData(context, params.projectUrl)

  return (
    <Sidebar {...props}>
      {/* Header: project 컨텍스트에서는 유저 정보 카드, 그 외에는 기본 유저 정보 */}
      {config.header?.type === 'user-info' && (
        <SidebarHeader>
          <UserInfoSection userInfo={data.userInfo} context={context} />
        </SidebarHeader>
      )}

      {/* Content: 섹션별 렌더링 */}
      <SidebarContent>
        {config.sections.map((section, index) => {
          const key = `${section.type}-${section.title}-${index}`

          switch (section.type) {
            case 'list':
              return (
                <ProjectListSection
                  key={key}
                  section={section}
                  projects={data.projects}
                />
              )

            case 'display':
              return (
                <MemberListSection
                  key={key}
                  section={section}
                  members={data.members}
                />
              )

            case 'navigation':
              return <NavigationSection key={key} section={section} />

            case 'action':
              return <ActionSection key={key} section={section} />

            default:
              return null
          }
        })}
      </SidebarContent>

      <SidebarRail />
    </Sidebar>
  )
}
