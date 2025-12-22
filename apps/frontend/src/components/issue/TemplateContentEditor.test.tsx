import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TemplateContentEditor } from './TemplateContentEditor'
import { afterEach } from 'node:test'

// MDEditor 모킹
vi.mock('@uiw/react-md-editor', () => ({
  default: ({
    value,
    onChange,
  }: {
    value: string
    onChange: (val: string | undefined) => void
  }) => (
    <textarea
      data-testid="md-editor"
      value={value}
      onChange={e => onChange(e.target.value)}
      placeholder="Enter markdown content..."
    />
  ),
}))

describe('TemplateContentEditor', () => {
  const mockOnChange = vi.fn()

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('라벨을 렌더링한다', () => {
    render(<TemplateContentEditor value="" onChange={mockOnChange} />)

    expect(screen.getByText('Template content')).toBeInTheDocument()
  })

  it('MDEditor를 렌더링한다', () => {
    render(<TemplateContentEditor value="" onChange={mockOnChange} />)

    const editor = screen.getByTestId('md-editor')
    expect(editor).toBeInTheDocument()
  })

  it('전달받은 value를 표시한다', () => {
    const testContent = '# Test Heading\n\nThis is test content.'

    render(
      <TemplateContentEditor value={testContent} onChange={mockOnChange} />
    )

    const editor = screen.getByTestId('md-editor')
    expect(editor).toHaveValue(testContent)
  })

  it('내용 변경 시 onChange 콜백이 호출된다', async () => {
    const user = userEvent.setup()

    render(<TemplateContentEditor value="" onChange={mockOnChange} />)

    const editor = screen.getByTestId('md-editor')
    const newContent = '# New'

    await user.clear(editor)
    await user.type(editor, newContent)

    // onChange가 호출되었는지 확인 (각 문자마다 호출됨)
    expect(mockOnChange).toHaveBeenCalled()
    expect(mockOnChange.mock.calls.length).toBeGreaterThan(0)
  })

  it('빈 문자열도 올바르게 처리한다', () => {
    render(<TemplateContentEditor value="" onChange={mockOnChange} />)

    const editor = screen.getByTestId('md-editor')
    expect(editor).toHaveValue('')
  })

  it('마크다운 콘텐츠를 포함한 긴 텍스트도 처리한다', () => {
    const longMarkdown = `
# Issue Template

## Description
Describe the issue in detail.

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
What should happen.

## Actual Behavior
What actually happens.

## Screenshots
Add screenshots if applicable.
    `.trim()

    render(
      <TemplateContentEditor value={longMarkdown} onChange={mockOnChange} />
    )

    const editor = screen.getByTestId('md-editor')
    expect(editor).toHaveValue(longMarkdown)
  })
})
