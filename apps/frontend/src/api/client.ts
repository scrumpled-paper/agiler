import axios from 'axios'

// 개발 환경에서는 Vite 프록시 사용, 프로덕션에서는 실제 URL 사용
const API_BASE_URL =
  import.meta.env.MODE === 'development'
    ? '' // 개발 환경: Vite 프록시 사용 (vite.config.ts의 /api 프록시)
    : import.meta.env.VITE_API_BASE_URL || 'https://agiler.p-e.kr'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true, // 쿠키 자동 전송 활성화 (OAuth2 인증용)
})

// 요청 인터셉터 (쿠키 기반 인증이므로 Authorization 헤더 불필요)
apiClient.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

//  공통 에러 처리 로직
apiClient.interceptors.response.use(
  response => response,
  error => {
    //  HTTP 상태 코드에 따른 전역 처리
    if (error.response?.status === 401) {
      console.error('인증 실패: 401 Unauthorized')
      // 사용자에게 알림 표시 및 로그인 페이지로 리다이렉트 로직 추가
      // window.location.href = '/login'
      // return Promise.reject(error); // 여기서 처리하고 reject하거나, 다음 로직으로 진행
    }

    // 서버 응답 본문에서 상세 에러 메시지 추출
    const serverErrorMessage = error.response?.data?.message

    //  네트워크 오류/타임아웃 등 Axios 자체 에러 처리
    if (axios.isAxiosError(error) && !error.response) {
      console.error('네트워크/통신 오류:', error.message)
      // 사용자에게 네트워크 오류 알림
      error.message =
        '서버에 연결할 수 없습니다. 네트워크 상태를 확인해 주세요.'
    }

    // 추출된 에러 메시지를 사용하여 에러 객체를 업데이트하여 던지기
    if (serverErrorMessage) {
      error.message = serverErrorMessage
    }

    return Promise.reject(error)
  }
)
