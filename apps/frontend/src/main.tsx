import React from 'react'
import ReactDOM from 'react-dom/client'
import { RouterProvider } from 'react-router'
import { QueryClientProvider } from '@tanstack/react-query'
import { queryClient } from './lib/query-client'
import { routers } from './router'
import './index.css'

/**
 * Conditionally enable MSW for development
 * Set VITE_USE_MSW=true in .env to use mock data instead of real API
 */
async function enableMocking() {
  if (import.meta.env.VITE_USE_MSW !== 'true') {
    return
  }

  const { worker } = await import('./mocks/browser')

  return worker.start({
    onUnhandledRequest(request, print) {
      // MSW가 처리하지 못한 요청 로깅
      if (request.url.includes('/api/')) {
        console.warn('[MSW] Unhandled request:', request.method, request.url)
        print.warning()
      }
    },
  })
}

enableMocking().then(() => {
  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={routers} />
      </QueryClientProvider>
    </React.StrictMode>
  )
})
