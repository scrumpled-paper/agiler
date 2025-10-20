import { Outlet } from 'react-router-dom'
import { AppSidebar } from '@/components/layout/Sidebar'
import { SidebarProvider } from '@/components/ui/sidebar'
import { AppHeader } from '@/components/layout/Header'
import { cn } from '@/lib/utils'

export default function MainLayout() {
  return (
    <SidebarProvider>
      <div
        className="flex min-h-screen w-full justify-start"
        data-testid="main-layout-container"
      >
        <AppSidebar />
        <div
          className={cn(
            'flex-1 transition-all duration-300 ease-in-out',
            'peer-data-[state=expanded]:ml-[12rem]' // 12rem = w-48
          )}
        >
          <AppHeader />
          <main className="container h-full w-full p-4">
            <Outlet />
          </main>
        </div>
      </div>
    </SidebarProvider>
  )
}
