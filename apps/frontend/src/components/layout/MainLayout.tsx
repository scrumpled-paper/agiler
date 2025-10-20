import { Outlet } from 'react-router'
import { AppSidebar } from '@/components/layout/Sidebar'
import { SidebarProvider } from '@/components/ui/sidebar' // 경로 통일된 것으로 가정
import { AppHeader } from '@/components/layout/Header'

export default function MainLayout() {
  return (
    <SidebarProvider>
      <div className="peer flex justify-start w-full min-h-screen">
        <AppSidebar />
        <div className="flex-1 transition-all duration-300 ease-in-out peer-data-[state=expanded]:md:ml-32 peer-data-[state=collapsed]:md:ml-2">
          <AppHeader />
          <main className="w-full h-full container p-4">
            <Outlet />
          </main>
        </div>
      </div>
    </SidebarProvider>
  )
}
