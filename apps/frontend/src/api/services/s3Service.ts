import { apiClient } from '../client'
import axios from 'axios' // S3에 PUT 요청을 보내기 위해 axios 또는 fetch 사용

// 1. 프리사인드 URL 응답 타입
interface PreSignedUrlResponse {
  preSignedUrl: string
  objectKey: string
}

export const s3Service = {
  // 1단계: 프리사인드 URL 요청
  async getPreSignedUrl(
    fileName: string,
    contentType: string
  ): Promise<PreSignedUrlResponse> {
    const response = await apiClient.post('/api/v1/s3/pre-signed-url', {
      fileName,
      contentType,
    })
    return response.data
  },

  // 2단계: 프리사인드 URL을 사용하여 S3에 파일 직접 업로드
  async uploadFileToS3(
    preSignedUrl: string,
    file: File,
    contentType: string
  ): Promise<void> {
    // S3는 일반적인 JSON API가 아니므로, apiClient 대신 별도의 fetch/axios 요청을 사용합니다.
    await axios.put(preSignedUrl, file, {
      headers: {
        'Content-Type': contentType,
      },
    })
    // 업로드가 성공하면 응답으로 상태 코드 200이 반환됩니다.
  },

  // **통합 함수:** 전체 업로드 프로세스를 한번에 처리
  async uploadProfileImage(file: File): Promise<string> {
    const fileName = file.name
    const contentType = file.type

    // 1. URL 요청
    const { preSignedUrl, objectKey } = await this.getPreSignedUrl(
      fileName,
      contentType
    )

    // 2. S3 업로드
    await this.uploadFileToS3(preSignedUrl, file, contentType)

    // 최종 이미지 URL 반환
    return objectKey
  },
}
