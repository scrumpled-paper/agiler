import { Outlet } from 'react-router-dom'

import { SidebarProvider } from '@/components/ui/sidebar'
import { AppHeader } from '@/components/layout/Header'
import { cn } from '@/lib/utils'
import { AppSidebar } from './sidebar/AppSidebar'

export default function MainLayout() {
  return (
    <SidebarProvider>
      <div
        className="flex min-h-screen w-full justify-center"
        data-testid="main-layout-container"
      >
        <AppSidebar />
        <div
          className={cn(
            'flex-1 transition-all duration-300 ease-in-out',
            'peer-data-[state=expanded]:ml-[15rem]' // 15rem = 240px
          )}
        >
          <AppHeader />
          <main className="container h-full w-full p-4 ">
            <Outlet />
          </main>
        </div>
      </div>
    </SidebarProvider>
  )
}
