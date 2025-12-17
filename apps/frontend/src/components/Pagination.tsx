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

// 한 번에 표시할 최대 페이지 개수를 정의합니다.
const MAX_PAGES_TO_SHOW = 5

export default function DynamicPagination({
  totalPages,
  currentPage,
  onPageChange,
}: PaginationProps) {
  // 1. 표시할 페이지 번호의 시작과 끝을 계산합니다.
  let startPage: number
  let endPage: number

  if (totalPages <= MAX_PAGES_TO_SHOW) {
    // 전체 페이지가 표시 가능한 최대 개수보다 작거나 같으면, 모든 페이지를 표시합니다.
    startPage = 1
    endPage = totalPages
  } else {
    // 전체 페이지가 최대 개수를 초과하면, 현재 페이지를 중심으로 페이지 범위를 계산합니다.
    // 현재 페이지를 기준으로 좌우에 표시할 페이지 개수를 계산합니다.
    const half = Math.floor(MAX_PAGES_TO_SHOW / 2)

    // 기본 시작 페이지는 현재 페이지에서 half를 뺀 값입니다.
    startPage = currentPage - half
    // 기본 끝 페이지는 현재 페이지에서 half를 더한 값입니다. (총 5개를 맞추기 위함)
    endPage = currentPage + half

    // **경계 조건 처리:**

    // 1) 시작 페이지가 1보다 작아지는 경우 (앞쪽에 페이지가 부족한 경우)
    if (startPage < 1) {
      startPage = 1
      // 시작 페이지를 1로 고정하고, 끝 페이지를 최대 표시 개수(5)로 맞춥니다.
      endPage = MAX_PAGES_TO_SHOW
    }

    // 2) 끝 페이지가 totalPages를 초과하는 경우 (뒤쪽에 페이지가 부족한 경우)
    if (endPage > totalPages) {
      endPage = totalPages
      // 끝 페이지를 totalPages로 고정하고, 시작 페이지를 (totalPages - 4)로 맞춥니다.
      // Math.max를 사용하여 startPage가 최소 1이 되도록 보장합니다.
      startPage = Math.max(1, totalPages - MAX_PAGES_TO_SHOW + 1)
    }
  }

  // 2. 계산된 범위에 따라 페이지 번호 배열을 생성합니다.
  const pagesToShow = Array.from(
    { length: endPage - startPage + 1 },
    (_, i) => startPage + i
  )

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
          {/* 이전 버튼 (기존 로직 유지) */}
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

          {/* 첫 페이지 (1) 버튼 - 시작 페이지가 1보다 클 경우만 표시하여 생략 표시 역할을 대신할 수 있습니다. */}
          {startPage > 1 && (
            <>
              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={e => {
                    e.preventDefault()
                    onPageChange(1)
                  }}
                >
                  1
                </PaginationLink>
              </PaginationItem>
              {startPage > 2 && ( // 1 다음 페이지가 2보다 크면 ... 표시
                <PaginationItem>
                  <span className="flex h-9 w-9 items-center justify-center">
                    ...
                  </span>
                </PaginationItem>
              )}
            </>
          )}

          {/* 동적으로 생성된 페이지 번호들 (수정된 부분) */}
          {pagesToShow.map(page => (
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

          {/* 마지막 페이지 (totalPages) 버튼 - 끝 페이지가 totalPages보다 작을 경우만 표시하여 생략 표시 역할을 대신할 수 있습니다. */}
          {endPage < totalPages && (
            <>
              {endPage < totalPages - 1 && ( // 마지막 페이지 바로 앞이 아니면 ... 표시
                <PaginationItem>
                  <span className="flex h-9 w-9 items-center justify-center">
                    ...
                  </span>
                </PaginationItem>
              )}
              <PaginationItem>
                <PaginationLink
                  href="#"
                  onClick={e => {
                    e.preventDefault()
                    onPageChange(totalPages)
                  }}
                >
                  {totalPages}
                </PaginationLink>
              </PaginationItem>
            </>
          )}

          {/* 다음 버튼 (기존 로직 유지) */}
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
