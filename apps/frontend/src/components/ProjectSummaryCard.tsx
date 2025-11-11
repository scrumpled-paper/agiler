import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@radix-ui/react-collapsible'
import { ChevronUp } from 'lucide-react'

export default function ProjectSummaryCard() {
  function fetchProjectInfo() {
    // const data = await getProjectInfo()
    const data = {
      title: 'project title',
      imageUrl: 'https://placehold.co/600x400',
      summary: ` 프로젝트 상세 내용들
      프로젝트 상세 내용들 프로젝트 상세 내용들
      프로젝트 상세 내용들 프로젝트 상세 내용들',
`,
    }
    return data
  }
  const data = fetchProjectInfo()
  return (
    <div className="w-full flex justify-center">
      <Collapsible className="w-full space-y-2">
        <CollapsibleTrigger className="flex w-full items-center justify-between rounded-md border px-4 py-2 font-medium text-3xl [&[data-state=open]>svg]:rotate-180">
          {data.title}
          <ChevronUp />
        </CollapsibleTrigger>
        <CollapsibleContent className="space-y-2 flex flex-row rounded-md border">
          <img src={data.imageUrl} className="w-1/2 m-0"></img>
          <div className=" w-1/2 rounded-r-md border p-5  text-sm">
            {data.summary}
          </div>
        </CollapsibleContent>
      </Collapsible>
    </div>
  )
}
