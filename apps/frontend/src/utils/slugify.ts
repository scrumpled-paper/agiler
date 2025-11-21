// utils/slugify.ts

/**
 * 한글 자모를 영어 발음으로 변환하는 매핑 테이블
 */
const koreanToRomanMap: Record<string, string> = {
  // 초성
  ㄱ: 'g',
  ㄲ: 'kk',
  ㄴ: 'n',
  ㄷ: 'd',
  ㄸ: 'tt',
  ㄹ: 'r',
  ㅁ: 'm',
  ㅂ: 'b',
  ㅃ: 'pp',
  ㅅ: 's',
  ㅆ: 'ss',
  ㅇ: '',
  ㅈ: 'j',
  ㅉ: 'jj',
  ㅊ: 'ch',
  ㅋ: 'k',
  ㅌ: 't',
  ㅍ: 'p',
  ㅎ: 'h',
  // 중성
  ㅏ: 'a',
  ㅐ: 'ae',
  ㅑ: 'ya',
  ㅒ: 'yae',
  ㅓ: 'eo',
  ㅔ: 'e',
  ㅕ: 'yeo',
  ㅖ: 'ye',
  ㅗ: 'o',
  ㅘ: 'wa',
  ㅙ: 'wae',
  ㅚ: 'oe',
  ㅛ: 'yo',
  ㅜ: 'u',
  ㅝ: 'wo',
  ㅞ: 'we',
  ㅟ: 'wi',
  ㅠ: 'yu',
  ㅡ: 'eu',
  ㅢ: 'ui',
  ㅣ: 'i',
  // 종성
  ㄳ: 'gs',
  ㄵ: 'nj',
  ㄶ: 'nh',
  ㄺ: 'lg',
  ㄻ: 'lm',
  ㄼ: 'lb',
  ㄽ: 'ls',
  ㄾ: 'lt',
  ㄿ: 'lp',
  ㅀ: 'lh',
  ㅄ: 'bs',
}

/**
 * 한글 음절을 초성/중성/종성으로 분리
 */
function decomposeKorean(char: string): string {
  const code = char.charCodeAt(0)

  // 한글 유니코드 범위: 0xAC00 ~ 0xD7A3
  if (code < 0xac00 || code > 0xd7a3) {
    return char
  }

  const base = code - 0xac00
  const cho = Math.floor(base / 588) // 초성
  const jung = Math.floor((base % 588) / 28) // 중성
  const jong = base % 28 // 종성

  const choSeong = [
    'ㄱ',
    'ㄲ',
    'ㄴ',
    'ㄷ',
    'ㄸ',
    'ㄹ',
    'ㅁ',
    'ㅂ',
    'ㅃ',
    'ㅅ',
    'ㅆ',
    'ㅇ',
    'ㅈ',
    'ㅉ',
    'ㅊ',
    'ㅋ',
    'ㅌ',
    'ㅍ',
    'ㅎ',
  ]
  const jungSeong = [
    'ㅏ',
    'ㅐ',
    'ㅑ',
    'ㅒ',
    'ㅓ',
    'ㅔ',
    'ㅕ',
    'ㅖ',
    'ㅗ',
    'ㅘ',
    'ㅙ',
    'ㅚ',
    'ㅛ',
    'ㅜ',
    'ㅝ',
    'ㅞ',
    'ㅟ',
    'ㅠ',
    'ㅡ',
    'ㅢ',
    'ㅣ',
  ]
  const jongSeong = [
    '',
    'ㄱ',
    'ㄲ',
    'ㄳ',
    'ㄴ',
    'ㄵ',
    'ㄶ',
    'ㄷ',
    'ㄹ',
    'ㄺ',
    'ㄻ',
    'ㄼ',
    'ㄽ',
    'ㄾ',
    'ㄿ',
    'ㅀ',
    'ㅁ',
    'ㅂ',
    'ㅄ',
    'ㅅ',
    'ㅆ',
    'ㅇ',
    'ㅈ',
    'ㅊ',
    'ㅋ',
    'ㅌ',
    'ㅍ',
    'ㅎ',
  ]

  const result =
    (koreanToRomanMap[choSeong[cho]] || '') +
    (koreanToRomanMap[jungSeong[jung]] || '') +
    (koreanToRomanMap[jongSeong[jong]] || '')

  return result
}

/**
 * 한글을 포함한 문자열을 영어 slug로 변환
 *
 * @param text - 변환할 텍스트 (한글, 영어, 숫자, 특수문자 포함 가능)
 * @param options - 변환 옵션
 * @returns URL-safe한 slug 문자열
 *
 * @example
 * slugify('프로젝트 관리') // 'peulojegteu_gwanri'
 * slugify('My Project 123') // 'my_project-123'
 * slugify('애자일 개발') // 'aejail_gaebal'
 */
export function slugify(
  text: string,
  options: {
    lowercase?: boolean
    separator?: string
    maxLength?: number
  } = {}
): string {
  const {
    lowercase = true,
    separator = '-',
    maxLength = 40,
    // addUnderscore // 이 옵션은 무시되거나 삭제되어야 합니다.
  } = options

  let slug = ''

  // 각 문자를 처리
  for (const char of text) {
    const code = char.charCodeAt(0)

    // 한글 음절 (가-힣)
    if (code >= 0xac00 && code <= 0xd7a3) {
      slug += decomposeKorean(char)
    }
    // 영어, 숫자
    else if (/[a-zA-Z0-9]/.test(char)) {
      slug += char
    }
    // 공백이나 특수문자는 separator로 변환
    else if (/[\s\-_]/.test(char)) {
      slug += separator
    }
    // 그 외 특수문자는 무시
  }

  // 후처리
  slug = slug
    .replace(new RegExp(`${separator}+`, 'g'), separator) // 연속된 separator를 하나로
    .replace(new RegExp(`^${separator}|${separator}$`, 'g'), '') // 앞뒤 separator 제거

  // 소문자 변환
  if (lowercase) {
    // 백엔드가 소문자만 허용하므로, 항상 소문자 변환을 추천합니다.
    slug = slug.toLowerCase()
  }

  // 최대 길이 제한
  if (maxLength && slug.length > maxLength) {
    slug = slug.substring(0, maxLength)
    // 마지막 separator로 끝나지 않도록
    slug = slug.replace(new RegExp(`[${separator}_]$`), '')
  }

  return slug
}

/**
 * Slug가 유효한지 검증 (백엔드 DTO 패턴에 맞춤)
 * - 소문자 알파벳, 숫자, 하이픈만 허용
 * - 0-40자 길이 제한
 * - 패턴: ^[a-z0-9-]+$
 */
export function isValidSlug(slug: string): boolean {
  // 1. 길이 체크 (0보다 크고 40자 이하)
  if (slug.length === 0 || slug.length > 40) {
    return false
  }

  // 2. 백엔드 DTO 패턴: 소문자, 숫자, 하이픈만 허용
  // 언더바 로직 제거
  return /^[a-z0-9-]+$/.test(slug)
}
