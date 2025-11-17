import axios from 'axios'

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://43.200.4.72:8080'

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

// 응답 인터셉터
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      console.error('인증 실패')
      // 로그인 페이지로 리다이렉트 등
    }
    return Promise.reject(error)
  }
)
