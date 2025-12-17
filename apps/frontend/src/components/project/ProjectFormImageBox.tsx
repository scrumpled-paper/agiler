import { projectService } from '@/api/services/projectService'
import { s3Service } from '@/api/services/s3Service'
import { Camera, ImageIcon, Trash2 } from 'lucide-react'
import { useRef, useState } from 'react'

interface ProjectFormImageBoxRes {
  projectUrl: string
  projectImageUrl: string
}

export default function ProjectFormImageBox({
  projectUrl,
  projectImageUrl,
}: ProjectFormImageBoxRes) {
  const [isUploading, setIsUploading] = useState(false)
  const [isImageHovered, setIsImageHovered] = useState(false)
  const [preview, setPreview] = useState<string | null>(null)

  const fileInputRef = useRef<HTMLInputElement>(null)
  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]

    if (!file || isUploading) {
      return
    }
    // 이미지 미리보기 로직 (로컬 파일)
    const reader = new FileReader()
    reader.onloadend = () => {
      setPreview(reader.result as string)
    }
    reader.readAsDataURL(file)

    // API 전송 로직
    setIsUploading(true)

    try {
      //  S3 업로드 서비스 호출
      const objectKey = await s3Service.uploadProfileImage(file)
      if (projectUrl) {
        // project 유저 이미지 수정
        await projectService.updateMainImage(projectUrl, objectKey)
      }
      // 업로드 성공 후 데이터 새로고침
    } catch (error) {
      console.error('Error uploading image or updating profile:', error)
      setPreview(null)
    } finally {
      // setPreview(null)
      setIsUploading(false)
      // 파일 입력 필드 초기화
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleImageClick = () => {
    if (fileInputRef.current && !isUploading) {
      fileInputRef.current.click()
    }
  }

  const handleImageDelete = async () => {
    if (isUploading || !projectImageUrl) {
      return
    }

    if (!confirm('프로필 이미지를 삭제하시겠습니까?')) {
      return
    }

    setIsUploading(true)

    try {
      // 빈 문자열로 이미지 삭제
      if (projectUrl) {
        await projectService.deleteMainImage(projectUrl)
      }
      setPreview(null)
    } catch (error) {
      console.error('Error deleting image:', error)
    } finally {
      setIsUploading(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div>
        <h3 className="text-sm font-semibold text-gray-900 mb-1">
          Project Image
        </h3>
      </div>

      <div className="flex flex-col lg:flex-row items-start gap-6">
        <div className="relative group">
          <div
            className={`relative w-[400px] h-[280px] rounded-lg flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 overflow-hidden border-2 transition-all ${
              isUploading
                ? 'opacity-50 cursor-not-allowed border-gray-200'
                : 'cursor-pointer hover:border-blue-400 border-gray-300'
            }`}
            onMouseEnter={() => setIsImageHovered(true)}
            onMouseLeave={() => setIsImageHovered(false)}
            role="button"
            tabIndex={isUploading ? -1 : 0}
            aria-label="프로젝트 이미지 업로드"
            onClick={handleImageClick}
            onKeyDown={e => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                handleImageClick()
              }
            }}
          >
            {preview || projectImageUrl ? (
              <img
                src={preview || projectImageUrl}
                alt="프로젝트 이미지"
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="flex flex-col items-center gap-3">
                <ImageIcon className="w-20 h-20 text-gray-400" />
                <p className="text-sm text-gray-500 font-medium">
                  클릭하여 이미지 선택
                </p>
              </div>
            )}

            {/* 호버 시 오버레이 */}
            {!isUploading && isImageHovered && (
              <div className="absolute inset-0 bg-black/60 flex items-center justify-center gap-3">
                <button
                  type="button"
                  onClick={e => {
                    e.stopPropagation()
                    handleImageClick()
                  }}
                  className="p-3 bg-white/90 rounded-full hover:bg-white transition-colors"
                  title="이미지 변경"
                  aria-label="이미지 변경"
                >
                  <Camera className="w-5 h-5 text-gray-700" />
                </button>
                {(preview || projectImageUrl) && (
                  <button
                    type="button"
                    onClick={e => {
                      e.stopPropagation()
                      handleImageDelete()
                    }}
                    className="p-3 bg-white/90 rounded-full hover:bg-white transition-colors"
                    title="이미지 삭제"
                    aria-label="이미지 삭제"
                  >
                    <Trash2 className="w-5 h-5 text-red-600" />
                  </button>
                )}
              </div>
            )}

            {/* 로딩 스피너 */}
            {isUploading && (
              <div className="absolute inset-0 flex items-center justify-center bg-black/40">
                <div className="w-8 h-8 border-4 border-t-transparent border-white rounded-full animate-spin"></div>
              </div>
            )}

            <input
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              ref={fileInputRef}
              className="hidden"
              disabled={isUploading}
              aria-label="이미지 파일 선택"
            />
          </div>
        </div>

        <div className="flex flex-col gap-2 text-sm text-gray-600">
          <p className="text-s text-gray-500">
            프로젝트를 대표하는 이미지를 업로드해주세요
          </p>
          <p>• 권장 크기: 800x560px (10:7 비율)</p>
          <p>• 지원 형식: JPG, PNG, GIF, WebP</p>
          <p>• 최대 파일 크기: 5MB</p>
        </div>
      </div>
    </div>
  )
}
