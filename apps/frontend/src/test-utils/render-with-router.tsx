import { render, type RenderOptions } from '@testing-library/react'
import type { ReactElement } from 'react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

interface RenderWithRouterOptions extends Omit<RenderOptions, 'wrapper'> {
  initialEntries?: string[]
}

export function renderWithRouter(
  ui: ReactElement,
  { initialEntries = ['/'], ...renderOptions }: RenderWithRouterOptions = {}
) {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <Routes>
        <Route path="/projects/:projectUrl/daily-scrum/:scrumId" element={ui} />
        <Route path="/projects/:projectUrl/daily-scrum" element={ui} />
        <Route path="/projects/:projectUrl/settings" element={ui} />
        <Route path="/projects/:projectUrl" element={ui} />
        <Route path="/dashboard/settings" element={ui} />
        <Route path="/dashboard" element={ui} />
        <Route path="/" element={ui} />
      </Routes>
    </MemoryRouter>,
    renderOptions
  )
}
