import ProjectCard from './ProjectCard'
import PaginationDemo from './Pagination'
import type { ContentItem } from '@/utils/mockData'
import { Button } from './ui/button'

interface ProjectListProps {
  contents: ContentItem[]
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}

export default function ProjectList({
  contents,
  currentPage,
  totalPages,
  onPageChange,
}: ProjectListProps) {
  return (
    <div className="border flex flex-col justify-center items-center">
      <div className="relative flex justify-center items-center w-full p-10 pt-20">
        <p className="text-black text-4xl font-bold font-['Roboto']">
          Project List
        </p>
        <Button className="absolute right-15 bg-black text-white px-4 py-2 rounded-lg text-sm">
          Join New Project
        </Button>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
        {contents.map((item: ContentItem) => (
          <ProjectCard props={item} key={item.url} />
        ))}
      </div>
      <PaginationDemo
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={onPageChange}
      ></PaginationDemo>
    </div>
  )
}
