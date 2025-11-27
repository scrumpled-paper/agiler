import { Dialog, DialogContent, DialogHeader, DialogTitle } from './ui/dialog'
import { useNavigate } from 'react-router-dom'
import ProjectForm from './project/projectForm'

interface JoinProjectModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export default function JoinProjectModal({
  open,
  onOpenChange,
}: JoinProjectModalProps) {
  const navigate = useNavigate()
  const handleCreateSuccess = (projectUrl: string) => {
    //[ ] 생성 성공 요청
    onOpenChange(false) // 모달 닫기
    navigate(`/projects/${projectUrl}`) // 페이지 이동
  }

  const handleCancel = () => {
    onOpenChange(false) // 모달 닫기
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
          <ProjectForm
            onCreateSuccess={handleCreateSuccess}
            onCancel={handleCancel}
          />
        </div>
      </DialogContent>
    </Dialog>
  )
}
