// lib/sidebar/types.ts

import type { ProjectInfo, ProjectMember, UserInfo } from '@/types'

export type SidebarContext = 'dashboard' | 'project' | 'project-settings'

export type SectionType =
  | 'navigation' // 클릭 가능한 링크
  | 'list' // 동적 리스트 (Project List)
  | 'display' // 표시 전용 (Members)
  | 'action' // 액션 버튼
  | 'user-info' // 유저 정보 카드

export interface NavigationItem {
  label: string
  route: string
  icon?: React.ElementType
}

export interface BaseSection {
  title: string
  icon?: React.ElementType
}

export interface NavigationSection extends BaseSection {
  displayTitle: boolean
  type: 'navigation'
  items: NavigationItem[]
}

export interface ListSection extends BaseSection {
  type: 'list'
  dataKey: 'projects'
  hasShowMore?: boolean
  showMoreRoute?: string
}

export interface DisplaySection extends BaseSection {
  type: 'display'
  dataKey: 'members'
  hasShowMore?: boolean
  showMoreRoute?: string
}

export interface ActionSection extends BaseSection {
  type: 'action'
  action: {
    label: string
    onClick: string | (() => void)
  }
}

//사이드바 헤더용
export interface UserInfoSection {
  type: 'user-info'
}

export type SidebarSection =
  | NavigationSection
  | ListSection
  | DisplaySection
  | ActionSection

// 사이드바 전체 데이터 정의
export interface SidebarConfig {
  header?: UserInfoSection
  sections: SidebarSection[]
}
//ListSection이나 DisplaySection에서 사용되는 실제 동적 데이터를 담는 구조
export interface SidebarData {
  projects?: ProjectInfo[]
  members?: ProjectMember[]
  userInfo?: UserInfo
}
