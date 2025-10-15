import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import App from './App'

describe('App', () => {
  it('렌더링이 정상적으로 되어야 한다', () => {
    render(<App />)
    expect(screen.getByText('애자일 협업 도구')).toBeInTheDocument()
  })
})
