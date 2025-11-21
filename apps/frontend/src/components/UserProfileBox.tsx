import { useRef, useState } from 'react'
import { Button } from './ui/button'
import { PencilIcon, Upload, UserIcon, CheckIcon } from 'lucide-react'
import { s3Service } from '@/api/services/s3Service'

export default function UserProfileBox() {
  const [preview, setPreview] = useState<string | null>(null)
  const [userName, setUserName] = useState<string>('userName')
  const [isEditName, setIsEditName] = useState<boolean>(false)
  const [isUploading, setIsUploading] = useState(false) // 업로드 상태 추가

  const fileInputRef = useRef<HTMLInputElement>(null)
  const nameInputRef = useRef<HTMLInputElement>(null)

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]

    if (!file) {
      return // 파일이 없으면 아무것도 하지 않음
    }

    //이미지 미리보기 로직
    const reader = new FileReader()
    reader.onloadend = () => {
      if (typeof reader.result === 'string') {
        setPreview(reader.result)
      }
    }
    reader.readAsDataURL(file)

    //API 전송 로직 (
    setIsUploading(true) // 업로드 시작
    const formData = new FormData()
    formData.append('profileImage', file)

    try {
      // [ ] 백엔드 이미지 업로드 로직 추가 후 수정
      // **2. 통합된 S3 업로드 서비스 호출**
      const newImageUrl = await s3Service.uploadProfileImage(file)

      // 4. 새로운 이미지 URL로 상태 업데이트 (만약 setPreview를 서버 응답으로 설정하려면)
      setPreview(newImageUrl)
      console.log('Upload successful:', newImageUrl)
    } catch (error) {
      console.error('Error uploading image:', error)
      // 사용자에게 오류 알림
      // setPreview(null); // 실패 시 미리보기 초기화
    } finally {
      setIsUploading(false) // 업로드 완료
    }
  }

  const handleImageClick = () => {
    // 업로드 중이 아닐 때만 클릭 가능하도록 수정
    if (fileInputRef.current && !isUploading) {
      fileInputRef.current.click()
    }
  }

  //이름 저장 로직
  const handleSaveName = async () => {
    // 여기에 userName을 서버로 전송하는 API 호출을 구현합니다.
    console.log('Saving new name:', userName)
    // 예: await api.updateUserName(userName);
  }

  const handleEditToggle = () => {
    if (isEditName) {
      setIsEditName(false)
      handleSaveName()
    } else {
      setIsEditName(true)
      // state가 변경된 후(re-render) 포커스를 주기 위해 setTimeout 사용
      setTimeout(() => {
        nameInputRef.current?.focus()
        nameInputRef.current?.select() // 텍스트 전체 선택
      }, 0)
    }
  }

  return (
    <div className="rounded-xl border w-full p-4 inline-flex justify-between items-center bg-white shadow-sm">
      <div className="flex-1 inline-flex justify-start items-center gap-5">
        {/* 프로필 이미지 영역 */}
        <div
          // 업로드 중일 때 스타일 추가
          className={`relative w-20 h-20 rounded-full flex items-center justify-center bg-gray-100 cursor-pointer overflow-hidden border border-gray-200 ${isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}
          onClick={handleImageClick}
        >
          {preview ? (
            <img
              src={preview}
              alt="Profile Preview"
              className="w-full h-full object-cover rounded-full"
            />
          ) : (
            <UserIcon className="w-10 h-10 text-gray-400" />
          )}

          {/* 업로드 중 아닐 때만 호버 효과 표시 */}
          {!isUploading && (
            <div className="bg-black/50 w-20 h-20 rounded-full flex items-center justify-center opacity-0 hover:opacity-100 absolute">
              <Upload className="text-white" />
            </div>
          )}

          {/* 업로드 중일 때 로딩 스피너 표시 */}
          {isUploading && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/30">
              <div className="w-6 h-6 border-2 border-t-transparent border-white rounded-full animate-spin"></div>
            </div>
          )}

          <input
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            ref={fileInputRef}
            className="hidden"
            disabled={isUploading} // 업로드 중에 input 비활성화
          />
        </div>

        <div className="flex flex-col justify-center">
          <div className="inline-flex items-center gap-2">
            <div className="text-black text-xl font-semibold">
              <input
                type="text"
                value={userName}
                ref={nameInputRef}
                // 편집 중일 때(!isEditName === false) disabled가 해제됩니다.
                disabled={!isEditName}
                onChange={e => setUserName(e.target.value)}
                // 비활성화 상태일 때 input처럼 보이지 않도록 스타일링
                className="bg-transparent outline-none ring-0 disabled:border-transparent disabled:ring-0 disabled:cursor-default focus:ring-1 focus:ring-gray-300 rounded-md px-1 py-0.5 -ml-1"
                onKeyDown={e => {
                  // Enter 키로 저장
                  if (e.key === 'Enter') handleEditToggle()
                }}
              />
            </div>
            {/* 연필/저장 아이콘 토글 버튼 */}
            <Button
              variant="ghost"
              size="icon"
              className="text-gray-500 hover:text-gray-700"
              onClick={handleEditToggle} // 클릭 핸들러 변경
            >
              {isEditName ? (
                <CheckIcon className="w-4 h-4" /> // 저장 아이콘
              ) : (
                <PencilIcon className="w-4 h-4" /> // 편집 아이콘
              )}
            </Button>
          </div>
          <div className="text-gray-500 text-sm">User Profile</div>
        </div>
      </div>
    </div>
  )
}
