// components/layout/Header.tsx

import {
  ChevronRight,
  Star,
  MessageSquare,
  Clock,
  MoreHorizontal,
  Share2,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { SidebarTrigger } from '@/components/ui/sidebar'
import { Link, useLocation, useParams } from 'react-router-dom'

interface BreadcrumbItem {
  label: string
  href?: string
}

export function AppHeader() {
  const location = useLocation()
  const params = useParams<{ projectId?: string; scrumId?: string }>()

  // 경로에 따른 브레드크럼 생성
  const breadcrumbs = getBreadcrumbs(location.pathname, params)

  // 경로에 따른 액션 버튼 표시 여부
  const showProjectActions = location.pathname.startsWith('/projects/')
  const isDashboard = location.pathname.startsWith('/dashboard')
  const isHome = location.pathname === '/'

  return (
    <TooltipProvider delayDuration={0}>
      <header className="flex h-14 items-center gap-2 border-b bg-background px-4">
        {/* 왼쪽: 사이드바 트리거 + 브레드크럼 */}
        <div className="flex items-center gap-2">
          <SidebarTrigger />

          {/* 브레드크럼 */}
          <nav className="flex items-center gap-1 text-sm">
            {breadcrumbs.map((crumb, index) => (
              <div key={index} className="flex items-center gap-1">
                {index > 0 && (
                  <ChevronRight className="h-4 w-4 text-muted-foreground" />
                )}
                {crumb.href ? (
                  <Link
                    to={crumb.href}
                    className="text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {crumb.label}
                  </Link>
                ) : (
                  <span className="font-medium text-foreground">
                    {crumb.label}
                  </span>
                )}
              </div>
            ))}
          </nav>
        </div>

        {/* 오른쪽: 액션 버튼 */}
        <div className="ml-auto flex items-center gap-2">
          {/* 프로젝트 페이지에서만 표시되는 액션 */}
          {showProjectActions && (
            <>
              <Button variant="ghost" size="sm">
                Share
                <Share2 className="ml-2 h-4 w-4" />
              </Button>
              <Separator orientation="vertical" className="h-6" />

              <Tooltip>
                <TooltipTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <MessageSquare className="h-4 w-4" />
                  </Button>
                </TooltipTrigger>
                <TooltipContent>
                  <p>Comments</p>
                </TooltipContent>
              </Tooltip>

              <Tooltip>
                <TooltipTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <Clock className="h-4 w-4" />
                  </Button>
                </TooltipTrigger>
                <TooltipContent>
                  <p>View updates</p>
                </TooltipContent>
              </Tooltip>

              <Tooltip>
                <TooltipTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <Star className="h-4 w-4" />
                  </Button>
                </TooltipTrigger>
                <TooltipContent>
                  <p>Favorite</p>
                </TooltipContent>
              </Tooltip>
            </>
          )}

          {/* 공통 메뉴 */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8">
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {showProjectActions && (
                <>
                  <DropdownMenuItem>Page settings</DropdownMenuItem>
                  <DropdownMenuItem>Analytics</DropdownMenuItem>
                  <DropdownMenuItem>Lock page</DropdownMenuItem>
                </>
              )}
              {isDashboard && (
                <>
                  <DropdownMenuItem>Dashboard settings</DropdownMenuItem>
                  <DropdownMenuItem>Export data</DropdownMenuItem>
                </>
              )}
              {isHome && (
                <>
                  <DropdownMenuItem>Preferences</DropdownMenuItem>
                  <DropdownMenuItem>Help</DropdownMenuItem>
                </>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </header>
    </TooltipProvider>
  )
}

// 브레드크럼 생성 함수
function getBreadcrumbs(
  pathname: string,
  params: { projectId?: string; scrumId?: string }
): BreadcrumbItem[] {
  const breadcrumbs: BreadcrumbItem[] = [
    { label: 'Dashboard', href: '/dashboard' },
  ]

  // Dashboard 경로
  if (pathname.startsWith('/dashboard')) {
    if (pathname.includes('/settings')) {
      breadcrumbs.push({ label: 'Settings' })
    }
    return breadcrumbs
  }

  // Projects 경로
  if (pathname.startsWith('/projects/') && params.projectId) {
    breadcrumbs.push({
      label: `Project ${params.projectId}`,
      href: `/projects/${params.projectId}`,
    })

    if (pathname.includes('/settings')) {
      breadcrumbs.push({ label: 'Settings' })
    } else if (pathname.includes('/daily-scrum')) {
      breadcrumbs.push({
        label: 'Daily Scrum',
        href: `/projects/${params.projectId}/daily-scrum`,
      })

      if (params.scrumId) {
        breadcrumbs.push({ label: `Scrum #${params.scrumId}` })
      }
    }
    return breadcrumbs
  }

  return breadcrumbs
}
