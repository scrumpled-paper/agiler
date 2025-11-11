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
 * slugify('프로젝트 관리') // 'peulojegteu-gwanri'
 * slugify('My Project 123') // 'my-project-123'
 * slugify('애자일 개발') // 'aejail-gaebal'
 */
export function slugify(
  text: string,
  options: {
    lowercase?: boolean
    separator?: string
    maxLength?: number
  } = {}
): string {
  const { lowercase = true, separator = '-', maxLength = 100 } = options

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
    slug = slug.toLowerCase()
  }

  // 최대 길이 제한
  if (maxLength && slug.length > maxLength) {
    slug = slug.substring(0, maxLength)
    // 마지막 separator로 끝나지 않도록
    slug = slug.replace(new RegExp(`${separator}$`), '')
  }

  return slug
}

/**
 * Slug가 유효한지 검증
 * - 영어 소문자, 숫자, 하이픈만 허용
 * - 최소 1자 이상
 * - 하이픈으로 시작하거나 끝나지 않음
 */
export function isValidSlug(slug: string): boolean {
  return /^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(slug)
}
