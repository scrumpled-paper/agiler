import { Button } from '@/components/ui/button'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/hooks/use-auth'

export default function Home() {
  const navigate = useNavigate()
  const { isAuthenticated, isLoading } = useAuth()

  const handleGetStarted = () => {
    if (isAuthenticated) {
      navigate('/dashboard')
    } else {
      navigate('/login')
    }
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-8">
      <div className="max-w-4xl mx-auto text-center space-y-8">
        <div className="space-y-4">
          <h1 className="text-5xl font-bold tracking-tight">Agiler</h1>
          <p className="text-xl text-muted-foreground">
            팀의 생산성을 높이는 애자일 프로젝트 관리 도구
          </p>
        </div>

        <div className="space-y-6 text-left max-w-2xl mx-auto">
          <div className="p-6 border rounded-lg space-y-2">
            <h2 className="text-2xl font-semibold">주요 기능</h2>
            <ul className="space-y-2 text-muted-foreground">
              <li>• 칸반 보드를 통한 직관적인 작업 관리</li>
              <li>• 데일리 스크럼으로 팀 협업 강화</li>
              <li>• 이슈 템플릿으로 업무 표준화</li>
              <li>• 실시간 협업과 진행 상황 추적</li>
            </ul>
          </div>

          <div className="p-6 border rounded-lg space-y-2">
            <h2 className="text-2xl font-semibold">애자일 방법론 지원</h2>
            <p className="text-muted-foreground">
              스크럼과 칸반 방식을 결합하여 유연한 프로젝트 관리를 제공합니다.
              빠른 스프린트 계획부터 백로그 관리까지, 애자일 개발의 모든 단계를
              효율적으로 지원합니다.
            </p>
          </div>
        </div>

        <div className="pt-4">
          <Button
            size="lg"
            onClick={handleGetStarted}
            disabled={isLoading}
            className="text-lg px-8 py-6"
          >
            {isLoading ? '로딩 중...' : '시작하기'}
          </Button>
        </div>
      </div>
    </div>
  )
}
