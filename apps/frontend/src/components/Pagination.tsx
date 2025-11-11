import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'

interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  showPageInfo?: boolean
}

export default function PaginationDemo({
  totalPages,
  currentPage,
  onPageChange,
}: PaginationProps) {
  return (
    <div
      className="flex flex-col items-center gap-4 pt-6 w-full"
      style={{
        all: 'revert',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '1rem',
        paddingTop: '1.5rem',
        width: '100%',
        fontSize: '14px',
        lineHeight: '1.5',
        letterSpacing: 'normal',
      }}
    >
      <Pagination>
        <PaginationContent>
          {/* 이전 버튼 */}
          <PaginationItem>
            <PaginationPrevious
              href="#"
              onClick={e => {
                e.preventDefault()
                if (currentPage > 1) {
                  onPageChange(currentPage - 1)
                }
              }}
              className={
                currentPage === 1 ? 'pointer-events-none opacity-50' : ''
              }
            />
          </PaginationItem>

          {/* 동적으로 생성된 페이지 번호들 */}
          {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
            <PaginationItem key={page}>
              <PaginationLink
                href="#"
                onClick={e => {
                  e.preventDefault()
                  onPageChange(page)
                }}
                isActive={currentPage === page}
              >
                {page}
              </PaginationLink>
            </PaginationItem>
          ))}

          {/* 다음 버튼 */}
          <PaginationItem>
            <PaginationNext
              href="#"
              onClick={e => {
                e.preventDefault()
                if (currentPage < totalPages) {
                  onPageChange(currentPage + 1)
                }
              }}
              className={
                currentPage === totalPages
                  ? 'pointer-events-none opacity-50'
                  : ''
              }
            />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    </div>
  )
}
