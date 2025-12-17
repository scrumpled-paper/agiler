import ContentListTable from '@/components/ContentListTable'
import { mockListData } from '@/mocks/mockTasks'
import { useState } from 'react'

export default function DailyScrumsList() {
  // [ ] api 서비스 구현 후 대체
  const [data] = useState(mockListData)

  const handlePageChange = (page: number) => {
    console.log('페이지 변경:', page)
    // 실제로는 여기서 API를 호출하여 새 데이터를 가져옵니다
    // 예: fetchData(page)
  }

  return (
    <div className="container p-4">
      <h1 className="text-3xl font-bold mb-4">DailyScrumList</h1>
      <ContentListTable data={data} onPageChange={handlePageChange} />
    </div>
  )
}
