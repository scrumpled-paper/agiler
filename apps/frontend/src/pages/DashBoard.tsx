import { useQuery } from '@tanstack/react-query'
import UserProfileBox from '@/components/UserProfileBox'
import ProjectList from '@/components/ProjectList'
import { useState } from 'react'

import { projectService } from '@/api/services/projectService'

export default function DashBoard() {
  const [currentPage, setCurrentPage] = useState(1) // UI는 1-based pagination

  const { data, isLoading, isError } = useQuery({
    queryKey: ['projects', 'dashboard', currentPage],
    queryFn: () =>
      projectService.getProjectList({ page: currentPage - 1, size: 6 }), // API는 0-based
  })

  const handlePageChange = (page: number) => {
    setCurrentPage(page) // UI page (1-based)
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-96">로딩 중...</div>
    )
  }

  if (isError || !data) {
    return <div>에러가 발생했습니다.</div>
  }

  return (
    <div className="container p-8">
      <UserProfileBox context="dashboard" />
      <div className="border h-96">
        <div className="relative flex justify-center items-center w-full p-10 pt-20">
          <p className="text-black text-4xl font-bold font-['Roboto']">
            Todo List
          </p>
        </div>
      </div>

      <ProjectList
        contents={data.contents}
        currentPage={currentPage} // UI page (1-based)
        totalPages={data.totalPages}
        onPageChange={handlePageChange}
      />
    </div>
  )
}
