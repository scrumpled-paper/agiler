import { useState, useEffect } from 'react'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { slugify, isValidSlug } from '@/utils/slugify'
import { projectService } from '@/api/services/projectService'

interface ProjectInitData {
  title: string
  summary: string
  url: string | undefined
}

interface ProjectFormProps {
  // 프로젝트 생성 성공 시 호출될 콜백.
  // 성공 후 모달 닫기, 페이지 이동 등은 이 콜백 내에서 처리합니다.
  initialData?: ProjectInitData | null
  projectUrl?: string | null
  onCreateSuccess: (projectSlug: string) => void
  onCancel: () => void
  submitButtonLabel?: string
  cancelButtonLabel?: string
}

const validateUrl = (url: string): boolean => {
  return isValidSlug(url)
}

export default function ProjectForm({
  initialData,
  projectUrl = null,
  onCreateSuccess,
  onCancel,
  submitButtonLabel = 'Create New Project',
  cancelButtonLabel = 'Cancel',
}: ProjectFormProps) {
  // initialData가 있으면 해당 값으로, 없으면 빈 문자열로 초기화
  const [projectTitle, setProjectTitle] = useState(initialData?.title || '')
  const [projectSlug, setProjectSlug] = useState(initialData?.url || '')
  const [projectSummary, setProjectSummary] = useState(
    initialData?.summary || ''
  )

  // URL이 초기 데이터로 채워졌다면, 수동 편집으로 간주하여 자동 생성을 막습니다.
  const [isUrlManuallyEdited, setIsUrlManuallyEdited] = useState(
    !!initialData?.url
  )
  const [isCreating, setIsCreating] = useState(false)
  const [errors, setErrors] = useState({
    title: '',
    url: '',
    summary: '',
  })

  // initialData가 변경되면 폼 상태를 업데이트
  useEffect(() => {
    if (initialData) {
      setProjectTitle(initialData.title || '')
      setProjectSlug(initialData.url || '')
      setProjectSummary(initialData.summary || '')
      setIsUrlManuallyEdited(!!initialData.url)
    }
  }, [initialData])

  // Title 변경 시 URL slug 자동 생성 로직
  useEffect(() => {
    if (!isUrlManuallyEdited && projectTitle) {
      const generatedSlug = slugify(projectTitle, { maxLength: 50 })
      setProjectSlug(generatedSlug)
    }
  }, [projectTitle, isUrlManuallyEdited])

  const handleCreate = async () => {
    setIsCreating(true)

    try {
      const newErrors = { title: '', url: '', summary: '' }

      //  Validate fields
      if (!projectTitle.trim()) newErrors.title = 'Please enter a project title'
      if (!projectSlug.trim()) {
        newErrors.url = 'Please enter a project URL'
      } else if (!validateUrl(projectSlug)) {
        newErrors.url =
          'Please enter a valid URL (format: team-name_project-name, 0-40 characters)'
      }
      if (!projectSummary.trim())
        newErrors.summary = 'Please enter a project summary'

      setErrors(newErrors)

      if (newErrors.title || newErrors.url || newErrors.summary) return

      //  Check URL availability
      try {
        const isAvailable = await projectService.getProjectUrlCheck(projectSlug)
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

      // Create project/fetch Project
      try {
        if (projectUrl) {
          // 수정 모드: updateProject API 호출
          await projectService.updateProjectSummery(projectUrl, {
            title: projectTitle,
            url: projectSlug,
            summary: projectSummary,
          })
        } else {
          // 생성 모드: createProject API 호출
          await projectService.createProject({
            title: projectTitle,
            url: projectSlug,
            summary: projectSummary,
          })
        }

        // 성공 시 상태 초기화 및 콜백 호출
        setProjectTitle('')
        setProjectSlug('')
        setProjectSummary('')
        setIsUrlManuallyEdited(false)
        setErrors({ title: '', url: '', summary: '' })
        onCreateSuccess(projectSlug) // 외부로 성공 알림
      } catch {
        const errorMessage = 'Failed to create project. Please try again.'
        setErrors(prev => ({ ...prev, summary: errorMessage }))
      }
    } finally {
      setIsCreating(false)
    }
  }

  const handleCancel = () => {
    // 상태 초기화
    setProjectTitle('')
    setProjectSlug('')
    setProjectSummary('')
    setIsUrlManuallyEdited(false)
    setErrors({ title: '', url: '', summary: '' })
    onCancel() // 외부로 취소 알림
  }

  return (
    <div className="flex flex-col gap-[40px] ">
      {/* Project Title */}
      <div className="flex flex-col gap-1 w-full">
        <label className="text-sm font-medium text-black">Project Title</label>
        <Input
          placeholder="Enter your Title"
          value={projectTitle}
          onChange={e => {
            setProjectTitle(e.target.value)
            setErrors(prev => ({ ...prev, title: '' }))
          }}
          className={errors.title ? 'border-red-500' : ''}
        />
        {errors.title && <p className="text-sm text-red-500">{errors.title}</p>}
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
          placeholder="team-name_project-name"
          value={projectSlug}
          onChange={e => {
            setProjectSlug(e.target.value)
            setIsUrlManuallyEdited(true)
            setErrors(prev => ({ ...prev, url: '' }))
          }}
          className={errors.url ? 'border-red-500' : ''}
        />
        {errors.url && <p className="text-sm text-red-500">{errors.url}</p>}
        {!errors.url && projectSlug && (
          <p className="text-xs text-gray-500">
            URL Preview: /projects/{projectSlug}
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
      <div className="flex justify-center gap-3 sm:gap-3 ">
        {!projectUrl && (
          <Button
            variant="outline"
            onClick={handleCancel}
            disabled={isCreating}
            className="w-[240px] h-12 border-black text-black hover:bg-gray-50"
          >
            {cancelButtonLabel}
          </Button>
        )}
        <Button
          onClick={handleCreate}
          disabled={isCreating}
          className="w-[240px] h-12 bg-black text-white hover:bg-black/90"
        >
          {isCreating ? 'Creating...' : submitButtonLabel}
        </Button>
      </div>
    </div>
  )
}
