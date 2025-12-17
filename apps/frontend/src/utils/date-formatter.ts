/**
 * Date 또는 string을 한국어 형식으로 포맷합니다.
 * @param date - Date 객체 또는 ISO 문자열
 * @returns "YYYY년 M월 D일" 형식의 문자열
 */
export function formatDate(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}
