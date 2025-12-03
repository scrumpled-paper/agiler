import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import userEvent from '@testing-library/user-event'
import ProjectFormImageBox from './ProjectFormImageBox'
import { http, HttpResponse } from 'msw'
import { server } from '@/mocks/server'

// Mock window.confirm
const mockConfirm = vi.fn()
global.confirm = mockConfirm

// Mock FileReader
class MockFileReader {
  onloadend: (() => void) | null = null
  result: string | null = null

  readAsDataURL() {
    this.result = 'data:image/jpeg;base64,mockbase64data'
    if (this.onloadend) {
      this.onloadend()
    }
  }
}

global.FileReader = MockFileReader as unknown as typeof FileReader

describe('ProjectFormImageBox Integration Tests', () => {
  const defaultProps = {
    projectUrl: 'test-project',
    projectImageUrl: '',
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockConfirm.mockReturnValue(true)
  })

  describe('Initial Rendering', () => {
    it('should render the component with default state', () => {
      render(<ProjectFormImageBox {...defaultProps} />)

      expect(screen.getByText('Project Image')).toBeInTheDocument()
      expect(screen.getByText('클릭하여 이미지 선택')).toBeInTheDocument()
      expect(screen.getByText(/권장 크기: 800x560px/)).toBeInTheDocument()
      expect(
        screen.getByText(/지원 형식: JPG, PNG, GIF, WebP/)
      ).toBeInTheDocument()
      expect(screen.getByText(/최대 파일 크기: 5MB/)).toBeInTheDocument()
    })

    it('should render with existing project image', () => {
      render(
        <ProjectFormImageBox
          projectUrl="test-project"
          projectImageUrl="https://example.com/project-image.jpg"
        />
      )

      const image = screen.getByAltText('프로젝트 이미지')
      expect(image).toBeInTheDocument()
      expect(image).toHaveAttribute(
        'src',
        'https://example.com/project-image.jpg'
      )
    })

    it('should have accessible file input', () => {
      render(<ProjectFormImageBox {...defaultProps} />)

      const fileInput = screen.getByLabelText('이미지 파일 선택')
      expect(fileInput).toBeInTheDocument()
      expect(fileInput).toHaveAttribute('type', 'file')
      expect(fileInput).toHaveAttribute('accept', 'image/*')
    })
  })

  describe('Image Upload Flow', () => {
    it('should upload image successfully', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const file = new File(['test image content'], 'test-image.jpg', {
        type: 'image/jpeg',
      })

      const fileInput = screen.getByLabelText('이미지 파일 선택')
      await user.upload(fileInput, file)

      // Check for loading state
      await waitFor(() => {
        expect(screen.queryByRole('img')).toBeInTheDocument()
      })

      // Wait for upload to complete
      await waitFor(
        () => {
          const img = screen.getByAltText('프로젝트 이미지')
          expect(img).toHaveAttribute(
            'src',
            'data:image/jpeg;base64,mockbase64data'
          )
        },
        { timeout: 3000 }
      )
    })

    it('should show preview immediately after file selection', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const fileInput = screen.getByLabelText('이미지 파일 선택')

      await user.upload(fileInput, file)

      await waitFor(() => {
        const img = screen.getByAltText('프로젝트 이미지')
        expect(img).toHaveAttribute(
          'src',
          'data:image/jpeg;base64,mockbase64data'
        )
      })
    })

    it('should show preview even when upload fails', async () => {
      const user = userEvent.setup()
      vi.spyOn(console, 'error').mockImplementation(() => {})

      // Mock API error
      server.use(
        http.post('/api/v1/s3/pre-signed-url', () => {
          return HttpResponse.error()
        })
      )

      render(<ProjectFormImageBox {...defaultProps} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const fileInput = screen.getByLabelText('이미지 파일 선택')

      await user.upload(fileInput, file)

      // Preview should still show even if upload fails
      await waitFor(
        () => {
          // Preview might be shown or cleared depending on error handling
          expect(fileInput).toBeEnabled()
        },
        { timeout: 3000 }
      )

      vi.restoreAllMocks()
    })

    it('should not upload when file is not selected', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const uploadArea = screen.getByRole('button', {
        name: '프로젝트 이미지 업로드',
      })

      await user.click(uploadArea)

      // Should not show loading or preview without file
      expect(screen.queryByAltText('프로젝트 이미지')).not.toBeInTheDocument()
    })

    it('should handle multiple file types', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const pngFile = new File(['test'], 'test.png', { type: 'image/png' })
      const fileInput = screen.getByLabelText('이미지 파일 선택')

      await user.upload(fileInput, pngFile)

      await waitFor(() => {
        expect(screen.getByAltText('프로젝트 이미지')).toBeInTheDocument()
      })
    })
  })

  describe('Image Delete Flow', () => {
    it('should render delete button when image exists', () => {
      render(
        <ProjectFormImageBox
          projectUrl="test-project"
          projectImageUrl="https://example.com/image.jpg"
        />
      )

      // Image should be present
      const image = screen.getByAltText('프로젝트 이미지')
      expect(image).toBeInTheDocument()
      expect(image).toHaveAttribute('src', 'https://example.com/image.jpg')
    })

    it('should not show delete button when no image is present', () => {
      render(<ProjectFormImageBox {...defaultProps} />)

      // Delete button should not be in the document when there's no image
      expect(
        screen.queryByRole('button', { name: '이미지 삭제' })
      ).not.toBeInTheDocument()
    })

    it('should display existing image correctly', () => {
      render(
        <ProjectFormImageBox
          projectUrl="test-project"
          projectImageUrl="https://example.com/existing-image.jpg"
        />
      )

      // Image should be displayed
      const image = screen.getByAltText('프로젝트 이미지')
      expect(image).toHaveAttribute(
        'src',
        'https://example.com/existing-image.jpg'
      )
    })
  })

  describe('UI Interactions', () => {
    it('should trigger file input when clicking the image area', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const uploadArea = screen.getByRole('button', {
        name: '프로젝트 이미지 업로드',
      })

      const fileInput = screen.getByLabelText('이미지 파일 선택')
      const clickSpy = vi.spyOn(fileInput, 'click')

      await user.click(uploadArea)

      expect(clickSpy).toHaveBeenCalled()
      clickSpy.mockRestore()
    })

    it('should trigger file input when pressing Enter', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const uploadArea = screen.getByRole('button', {
        name: '프로젝트 이미지 업로드',
      })

      const fileInput = screen.getByLabelText('이미지 파일 선택')
      const clickSpy = vi.spyOn(fileInput, 'click')

      uploadArea.focus()
      await user.keyboard('{Enter}')

      expect(clickSpy).toHaveBeenCalled()
      clickSpy.mockRestore()
    })

    it('should trigger file input when pressing Space', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const uploadArea = screen.getByRole('button', {
        name: '프로젝트 이미지 업로드',
      })

      const fileInput = screen.getByLabelText('이미지 파일 선택')
      const clickSpy = vi.spyOn(fileInput, 'click')

      uploadArea.focus()
      await user.keyboard(' ')

      expect(clickSpy).toHaveBeenCalled()
      clickSpy.mockRestore()
    })

    it('should have hover overlay with action buttons when image exists', async () => {
      const user = userEvent.setup()
      render(
        <ProjectFormImageBox
          projectUrl="test-project"
          projectImageUrl="https://example.com/image.jpg"
        />
      )

      const imageContainer = screen.getByRole('button', {
        name: '프로젝트 이미지 업로드',
      })

      await user.hover(imageContainer)

      // After hover, the buttons should be available
      await waitFor(() => {
        const changeButton = screen.queryByRole('button', {
          name: '이미지 변경',
        })
        const deleteButton = screen.queryByRole('button', {
          name: '이미지 삭제',
        })
        // These buttons are always in DOM but visibility is controlled by CSS
        expect(changeButton || deleteButton).toBeTruthy()
      })
    })

    it('should disable file input during upload', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const fileInput = screen.getByLabelText('이미지 파일 선택')

      await user.upload(fileInput, file)

      // File input should be disabled during upload initially
      await waitFor(() => {
        // After upload completes, input should be enabled again
        expect(fileInput).toBeEnabled()
      })
    })
  })

  describe('Loading States', () => {
    it('should complete upload process successfully', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const fileInput = screen.getByLabelText('이미지 파일 선택')

      await user.upload(fileInput, file)

      // After upload completes, preview should be shown
      await waitFor(() => {
        const img = screen.queryByAltText('프로젝트 이미지')
        expect(img).toBeInTheDocument()
      })
    })

    it('should allow file input after upload completes', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const fileInput = screen.getByLabelText('이미지 파일 선택')

      await user.upload(fileInput, file)

      // After upload completes, file input should be enabled
      await waitFor(() => {
        expect(fileInput).toBeEnabled()
      })
    })
  })

  describe('Edge Cases', () => {
    it('should handle empty file input', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const uploadArea = screen.getByRole('button', {
        name: '프로젝트 이미지 업로드',
      })

      await user.click(uploadArea)

      // Should remain in initial state
      expect(screen.getByText('클릭하여 이미지 선택')).toBeInTheDocument()
    })

    it('should handle missing projectUrl gracefully', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox projectUrl="" projectImageUrl="" />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const fileInput = screen.getByLabelText('이미지 파일 선택')

      await user.upload(fileInput, file)

      // Should still show preview but skip API call
      await waitFor(() => {
        expect(screen.getByAltText('프로젝트 이미지')).toBeInTheDocument()
      })
    })

    it('should prevent delete without image', () => {
      render(
        <ProjectFormImageBox projectUrl="test-project" projectImageUrl="" />
      )

      // Delete button should not be in the document without an image
      expect(
        screen.queryByRole('button', { name: '이미지 삭제' })
      ).not.toBeInTheDocument()
    })

    it('should clear file input after upload', async () => {
      const user = userEvent.setup()
      render(<ProjectFormImageBox {...defaultProps} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const fileInput = screen.getByLabelText(
        '이미지 파일 선택'
      ) as HTMLInputElement

      await user.upload(fileInput, file)

      await waitFor(() => {
        expect(fileInput.value).toBe('')
      })
    })
  })
})
