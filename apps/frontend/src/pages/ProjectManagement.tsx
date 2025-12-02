import { projectService } from '@/api/services/projectService'
import ProjectForm from '@/components/project/projectForm'
import ProjectFormImageBox from '@/components/project/ProjectFormImageBox'
import { useQuery } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'

export default function ProjectManagement() {
  const navigate = useNavigate()
  const { projectUrl } = useParams<{ projectUrl: string }>()

  const { data } = useQuery({
    queryKey: ['projectSummary', projectUrl],
    queryFn: () => projectService.getProjectSummery(projectUrl!),
    enabled: !!projectUrl,
  })

  const initialDataForForm = data
    ? {
        title: data.title,
        summary: data.summary,
        url: projectUrl, // useParams에서 가져온 슬러그를 명시적으로 추가
      }
    : null

  const projectImageUrl = data?.imageUrl

  const handleCreateSuccess = (newProjectUrl: string) => {
    navigate(`/projects/${newProjectUrl}`) // 변경된 URL로 리다이렉트
  }

  const handleCancel = () => {
    console.log('Project creation cancelled.')
    // 추가적인 취소 로직이 필요하면 여기에 추가
  }

  return (
    <div className="container p-4">
      <div className="flex flex-col justify-center items-center gap-5">
        <div className="text-center text-[40px] font-bold leading-[48px] font-['Roboto'] pb-10">
          Project Management
        </div>
        <div className="flex flex-col justify-center gap-10 max-w-3xl">
          <ProjectFormImageBox
            projectUrl={projectUrl || ''}
            projectImageUrl={projectImageUrl || ''}
          />
          <ProjectForm
            projectUrl={projectUrl}
            initialData={initialDataForForm}
            onCreateSuccess={handleCreateSuccess}
            onCancel={handleCancel}
            submitButtonLabel="Save"
          />
        </div>
      </div>
    </div>
  )
}
