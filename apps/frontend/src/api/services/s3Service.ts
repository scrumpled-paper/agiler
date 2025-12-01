import { apiClient } from '../client'
import axios from 'axios' // S3에 PUT 요청을 보내기 위해 axios 또는 fetch 사용

// 1. 프리사인드 URL 응답 타입
interface PreSignedUrlResponse {
  preSignedUrl: string
  objectKey: string
}

// 2. 업로드 확인 응답 타입 (이미지 ID와 URL)
interface ImageConfirmResponse {
  imageId: number
  imageUrl: string
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

  // 3단계: 백엔드에 업로드 완료 통보 및 이미지 정보 획득
  // [ ] 백엔드에서 이미지 업로드 로직 추가 후 수정
  async confirmUpload(objectKey: string): Promise<ImageConfirmResponse> {
    const response = await apiClient.post('/api/v1/s3/confirm-upload', {
      objectKey,
    })
    return response.data
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

    // 3. 업로드 확인 및 이미지 정보 획득
    const { imageUrl } = await this.confirmUpload(objectKey)

    // 최종 이미지 URL 반환
    return imageUrl
  },
}
