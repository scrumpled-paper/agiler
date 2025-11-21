import { projectService } from '@/api/services/projectService'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@radix-ui/react-collapsible'
import { useQuery } from '@tanstack/react-query'
import { ChevronDown, ImageIcon } from 'lucide-react'
import { useParams } from 'react-router-dom'

export default function ProjectSummaryCard() {
  const { projectUrl } = useParams<{ projectUrl: string }>()
  const { data, isError } = useQuery({
    queryKey: ['projectSummary', projectUrl],
    queryFn: () => projectService.getProjectSummery(projectUrl!),
    enabled: !!projectUrl,
  })

  if (isError || !data) {
    return (
      <div className="container p-4">
        <div className="flex justify-center items-center h-96 bg-destructive/10 rounded-lg border border-destructive/20">
          <p className="text-destructive font-medium">에러가 발생했습니다.</p>
        </div>
      </div>
    )
  }
  return (
    <div className="w-full flex justify-center">
      <Collapsible className="w-full">
        <div className="bg-card rounded-lg border shadow-sm hover:shadow-md transition-shadow duration-200">
          <CollapsibleTrigger className="group flex w-full items-center justify-between px-6 py-4 font-semibold text-2xl hover:bg-accent/50 rounded-lg transition-colors duration-200">
            <span className="bg-gradient-to-r from-primary to-primary/70 bg-clip-text text-transparent">
              {data.title}
            </span>
            <ChevronDown className="h-6 w-6 text-muted-foreground transition-transform duration-300 group-data-[state=open]:rotate-180" />
          </CollapsibleTrigger>
          <CollapsibleContent className="overflow-hidden data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95">
            <div className="flex flex-col md:flex-row gap-6 p-6 pt-2">
              <div className="w-full md:w-1/2 rounded-lg overflow-hidden bg-muted/30 border">
                {data.imageUrl !== '' ? (
                  <img
                    src={data.imageUrl}
                    alt={`${data.title} 대표 이미지`}
                    className="w-full h-full object-cover aspect-video"
                  />
                ) : (
                  <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                    <ImageIcon className="h-16 w-16 mb-3 opacity-20" />
                    <p className="text-sm text-center px-4">
                      프로젝트 설정에서
                      <br />
                      대표 이미지를 업로드하세요
                    </p>
                  </div>
                )}
              </div>
              <div className="w-full md:w-1/2 flex items-center">
                <div className="prose prose-sm dark:prose-invert max-w-none">
                  <p className="text-muted-foreground leading-relaxed whitespace-pre-wrap">
                    {data.summary || '프로젝트 요약이 없습니다.'}
                  </p>
                </div>
              </div>
            </div>
          </CollapsibleContent>
        </div>
      </Collapsible>
    </div>
  )
}
