import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import UserProfileBox from './UserProfileBox'

// Mock UI components
vi.mock('./ui/button', () => ({
  Button: ({
    children,
    onClick,
  }: {
    children: React.ReactNode
    onClick: () => void
  }) => <button onClick={onClick}>{children}</button>,
}))

describe('UserProfileBox', () => {
  it('should render user profile', () => {
    render(<UserProfileBox />)
    expect(screen.getByText('User Profile')).toBeInTheDocument()
  })

  it('should render default username', () => {
    render(<UserProfileBox />)
    expect(screen.getByDisplayValue('userName')).toBeInTheDocument()
  })

  it('should enable edit mode when edit button is clicked', () => {
    render(<UserProfileBox />)
    const input = screen.getByDisplayValue('userName') as HTMLInputElement

    expect(input.disabled).toBe(true)

    // Click edit button (PencilIcon)
    const editButton = screen.getByRole('button')
    fireEvent.click(editButton)

    expect(input.disabled).toBe(false)
  })

  it('should update username when typing in edit mode', () => {
    render(<UserProfileBox />)

    // Enable edit mode
    const editButton = screen.getByRole('button')
    fireEvent.click(editButton)

    const input = screen.getByDisplayValue('userName')
    fireEvent.change(input, { target: { value: 'newUserName' } })

    expect(screen.getByDisplayValue('newUserName')).toBeInTheDocument()
  })
})
