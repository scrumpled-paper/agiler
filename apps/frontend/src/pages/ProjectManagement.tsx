import { projectService } from '@/api/services/projectService'
import ProjectForm from '@/components/project/projectForm'
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

  const handleCreateSuccess = (newProjectUrl: string) => {
    navigate(`/projects/${newProjectUrl}`) // 변경된 URL로 리다이렉트
  }

  const handleCancel = () => {
    console.log('Project creation cancelled.')
    // 추가적인 취소 로직이 필요하면 여기에 추가
  }

  return (
    <div className="container p-8">
      <div>
        <div className="sm:max-w-[640px] p-0">
          <div className="flex flex-col gap-[30px] py-[30px]">
            <div>
              <div className="text-center text-[40px] font-bold leading-[48px] font-['Roboto']">
                Project Create
              </div>
            </div>

            <ProjectForm
              projectUrl={projectUrl}
              initialData={initialDataForForm}
              onCreateSuccess={handleCreateSuccess}
              onCancel={handleCancel}
            />
          </div>
        </div>
      </div>
    </div>
  )
}
