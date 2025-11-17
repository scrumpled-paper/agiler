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
import { getBreadcrumbs } from '@/lib/breadcrumbs'
import { useAuth } from '@/hooks/use-auth'

export function AppHeader() {
  const location = useLocation()
  const params = useParams<{ projectUrl?: string; scrumId?: string }>()
  const { logout, isLoggingOut } = useAuth()

  // 경로에 따른 브레드크럼 생성
  const breadcrumbs = getBreadcrumbs(location.pathname, params)

  // 경로에 따른 액션 버튼 표시 여부
  const showProjectActions = location.pathname.startsWith('/projects/')
  const isDashboard = location.pathname.startsWith('/dashboard')

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
              <Button
                variant="ghost"
                size="sm"
                onClick={() => alert('프로젝트 참가 링크 생성 기능은 준비 중')}
              >
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
            <DropdownMenuContent align="end" className="bg-white">
              {showProjectActions && (
                <>
                  <DropdownMenuItem>Page settings</DropdownMenuItem>
                  <DropdownMenuItem onClick={logout} disabled={isLoggingOut}>
                    Logout
                  </DropdownMenuItem>
                </>
              )}
              {isDashboard && (
                <>
                  <DropdownMenuItem onClick={logout} disabled={isLoggingOut}>
                    Logout
                  </DropdownMenuItem>
                </>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </header>
    </TooltipProvider>
  )
}
