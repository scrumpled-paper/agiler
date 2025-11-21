import { useState, useEffect } from 'react'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from './ui/dialog'
import { Input } from './ui/input'
import { Textarea } from './ui/textarea'
import { Button } from './ui/button'
import { slugify, isValidSlug } from '@/utils/slugify'
import { projectService } from '@/api/services/projectService'
import { useNavigate } from 'react-router-dom'

interface JoinProjectModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export default function JoinProjectModal({
  open,
  onOpenChange,
}: JoinProjectModalProps) {
  const [projectTitle, setProjectTitle] = useState('')
  const [projectUrl, setProjectUrl] = useState('')
  const [projectSummary, setProjectSummary] = useState('')
  const [isUrlManuallyEdited, setIsUrlManuallyEdited] = useState(false)
  const [isCreating, setIsCreating] = useState(false)
  const [errors, setErrors] = useState({
    title: '',
    url: '',
    summary: '',
  })

  const navigate = useNavigate()
  // Title이 변경되면 자동으로 URL slug 생성 (수동 편집하지 않은 경우만)
  useEffect(() => {
    if (!isUrlManuallyEdited && projectTitle) {
      const generatedSlug = slugify(projectTitle, { maxLength: 50 })
      setProjectUrl(generatedSlug)
    }
  }, [projectTitle, isUrlManuallyEdited])

  const validateUrl = (url: string): boolean => {
    return isValidSlug(url)
  }

  const handleCreate = async () => {
    setIsCreating(true)

    try {
      const newErrors = {
        title: '',
        url: '',
        summary: '',
      }

      // Validate all fields
      if (!projectTitle.trim()) {
        newErrors.title = 'Please enter a project title'
      }

      if (!projectUrl.trim()) {
        newErrors.url = 'Please enter a project URL'
      } else if (!validateUrl(projectUrl)) {
        newErrors.url =
          'Please enter a valid URL (lowercase letters, numbers, hyphens only)'
      }

      if (!projectSummary.trim()) {
        newErrors.summary = 'Please enter a project summary'
      }

      setErrors(newErrors)

      // If any errors, don't proceed
      if (newErrors.title || newErrors.url || newErrors.summary) {
        return
      }

      // Check URL availability
      try {
        const isAvailable = await projectService.getProjectUrlCheck(projectUrl)
        if (!isAvailable) {
          setErrors(prev => ({
            ...prev,
            url: 'This URL is already in use. Please choose another one.',
          }))
          return
        }
      } catch {
        setErrors(prev => ({
          ...prev,
          url: 'Failed to check URL availability. Please try again.',
        }))
        return
      }

      // Create project
      try {
        // TODO : id 어디에쓰지
        const projectId = await projectService.createProject({
          title: projectTitle,
          url: projectUrl,
          summary: projectSummary,
        })

        console.log('Project created successfully with ID:', projectId)

        // Reset and close
        setProjectTitle('')
        setProjectUrl('')
        setProjectSummary('')
        setIsUrlManuallyEdited(false)
        setErrors({ title: '', url: '', summary: '' })
        onOpenChange(false)

        navigate(`/projects/${projectUrl}`)
      } catch (error) {
        // Handle project creation error
        const errorMessage =
          error instanceof Error
            ? error.message
            : 'Failed to create project. Please try again.'

        setErrors(prev => ({
          ...prev,
          summary: errorMessage,
        }))
      }
    } finally {
      setIsCreating(false)
    }
  }

  const handleCancel = () => {
    setProjectTitle('')
    setProjectUrl('')
    setProjectSummary('')
    setIsUrlManuallyEdited(false)
    setErrors({ title: '', url: '', summary: '' })
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[640px] p-0">
        <div className="flex flex-col gap-[30px] py-[30px]">
          <DialogHeader>
            <DialogTitle className="text-center text-[40px] font-bold leading-[48px] font-['Roboto']">
              Project Create
            </DialogTitle>
          </DialogHeader>

          <div className="flex flex-col gap-[40px] px-[60px]">
            {/* Project Title */}
            <div className="flex flex-col gap-1 w-full">
              <label className="text-sm font-medium text-black">
                Project Title
              </label>
              <Input
                placeholder="Enter your Title"
                value={projectTitle}
                onChange={e => {
                  setProjectTitle(e.target.value)
                  setErrors(prev => ({ ...prev, title: '' }))
                }}
                className={errors.title ? 'border-red-500' : ''}
              />
              {errors.title && (
                <p className="text-sm text-red-500">{errors.title}</p>
              )}
            </div>

            {/* Project URL */}
            <div className="flex flex-col gap-1 w-full">
              <label className="text-sm font-medium text-black">
                Project URL
                <span className="ml-2 text-xs text-gray-500 font-normal">
                  (Auto-generated from title)
                </span>
              </label>
              <Input
                placeholder="my-project-url"
                value={projectUrl}
                onChange={e => {
                  setProjectUrl(e.target.value)
                  setIsUrlManuallyEdited(true)
                  setErrors(prev => ({ ...prev, url: '' }))
                }}
                className={errors.url ? 'border-red-500' : ''}
              />
              {errors.url && (
                <p className="text-sm text-red-500">{errors.url}</p>
              )}
              {!errors.url && projectUrl && (
                <p className="text-xs text-gray-500">
                  URL Preview: /projects/{projectUrl}
                </p>
              )}
            </div>

            {/* Project Summary */}
            <div className="flex flex-col gap-1 w-full">
              <label className="text-sm font-medium text-black">
                Project summary
              </label>
              <Textarea
                placeholder="Enter your summary"
                value={projectSummary}
                onChange={e => {
                  setProjectSummary(e.target.value)
                  setErrors(prev => ({ ...prev, summary: '' }))
                }}
                className={`h-[122px] resize-none ${errors.summary ? 'border-red-500' : ''}`}
              />
              {errors.summary && (
                <p className="text-sm text-red-500">{errors.summary}</p>
              )}
            </div>

            {/* Buttons */}
            <DialogFooter className="flex gap-3 sm:gap-3">
              <Button
                variant="outline"
                onClick={handleCancel}
                disabled={isCreating}
                className="w-[240px] h-12 border-black text-black hover:bg-gray-50"
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreate}
                disabled={isCreating}
                className="w-[240px] h-12 bg-black text-white hover:bg-black/90"
              >
                {isCreating ? 'Creating...' : 'Create New Project'}
              </Button>
            </DialogFooter>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
