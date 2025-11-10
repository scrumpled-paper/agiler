import { useQuery } from '@tanstack/react-query'
import UserProfileBox from '@/components/UserProfileBox'
import ProjectList from '@/components/ProjectList'
import { useState } from 'react'
import { fetchMockContents } from '@/utils/mockData'

export default function DashBoard() {
  const [currentPage, setCurrentPage] = useState(1)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['contents', currentPage],
    queryFn: () => fetchMockContents(currentPage, 6),
    // keepPreviousData: true, // 페이지 전환 시 이전 데이터 유지
  })

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    // window.scrollTo({ top: 0, behavior: 'smooth' })
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
      <UserProfileBox />
      <div className="border h-96">
        <div className="relative flex justify-center items-center w-full p-10 pt-20">
          <p className="text-black text-4xl font-bold font-['Roboto']">
            Todo List
          </p>
        </div>
      </div>

      <ProjectList
        contents={data.contents}
        currentPage={currentPage}
        totalPages={data.totalPages}
        onPageChange={handlePageChange}
      />
    </div>
  )
}
