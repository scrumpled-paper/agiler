import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

function App() {
  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto p-8">
        <h1 className="text-4xl font-bold text-foreground mb-4">
          애자일 협업 도구
        </h1>
        <p className="text-muted-foreground mb-8">
          React 19 + Tailwind CSS 4 + shadcn/ui
        </p>

        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <CardHeader>
              <CardTitle>칸반보드</CardTitle>
              <CardDescription>드래그앤드롭으로 작업 관리</CardDescription>
            </CardHeader>
            <CardContent>
              <Button>시작하기</Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>실시간 편집</CardTitle>
              <CardDescription>팀원과 동시 작업 가능</CardDescription>
            </CardHeader>
            <CardContent>
              <Button variant="secondary">시작하기</Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>알림</CardTitle>
              <CardDescription>마감일 자동 알림</CardDescription>
            </CardHeader>
            <CardContent>
              <Button variant="outline">설정</Button>
            </CardContent>
          </Card>
        </div>

        <div className="mt-8 flex gap-4">
          <Button size="lg">Large Button</Button>
          <Button>Default Button</Button>
          <Button size="sm">Small Button</Button>
          <Button variant="destructive">Delete</Button>
          <Button variant="ghost">Ghost</Button>
        </div>
      </div>
    </div>
  )
}

export default App
