import { useRef, useState } from 'react'
import { Button } from './ui/button'
import { Upload, UserIcon } from 'lucide-react'
import { s3Service } from '@/api/services/s3Service'
import { useUserInfo, useUserProfileMutation } from '@/hooks/use-user'

type UserProfileBoxProps = {
  context: 'dashboard' | 'project'
  projectUrl?: string
}

export default function UserProfileBox({
  context,
  projectUrl,
}: UserProfileBoxProps) {
  // 1. useUserInfo를 사용하여 데이터 조회
  const userInfo = useUserInfo(context, projectUrl)

  // 2. useMutation으로 데이터 수정 기능 준비
  const { mutate } = useUserProfileMutation(context, projectUrl)

  // 로컬 상태 (편집 모드에서만 사용)
  const [isEditing, setIsEditing] = useState<boolean>(false)
  const [isUploading, setIsUploading] = useState(false)
  const [editedName, setEditedName] = useState<string>('')
  const [editedEmail, setEditedEmail] = useState<string>('')
  const [editedDescription, setEditedDescription] = useState<string>('')

  const fileInputRef = useRef<HTMLInputElement>(null)
  const nameInputRef = useRef<HTMLInputElement>(null)
  const emailInputRef = useRef<HTMLInputElement>(null)
  const descriptionInputRef = useRef<HTMLTextAreaElement>(null)

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]

    if (!file || isUploading) {
      return
    }

    // 이미지 미리보기 로직 (로컬 파일)
    const reader = new FileReader()
    reader.onloadend = () => {
      // 로컬 미리보기는 API 호출 중에도 유지될 수 있도록 임시로 설정 가능
      // setPreview(reader.result as string);
    }
    reader.readAsDataURL(file)

    // API 전송 로직
    setIsUploading(true)

    try {
      // 1. S3 업로드 서비스 호출
      const newImageUrl = await s3Service.uploadProfileImage(file)

      // 2. 서버에 새로운 이미지 URL 업데이트 요청 (useMutation 사용)
      // mutate({ imageUrl: newImageUrl })

      console.log('Upload successful:', newImageUrl)
    } catch (error) {
      console.error('Error uploading image or updating profile:', error)
    } finally {
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

  // 편집 시작
  const handleStartEdit = () => {
    setEditedName(userInfo?.nickname || '')
    setEditedEmail(userInfo?.email || '')
    setEditedDescription(userInfo?.description || '')
    setIsEditing(true)

    setTimeout(() => {
      nameInputRef.current?.focus()
      nameInputRef.current?.select()
    }, 0)
  }

  // 저장 로직
  const handleSave = () => {
    // 변경사항 검증
    if (editedName.trim() === '') {
      alert('닉네임은 비워둘 수 없습니다.')
      return
    }

    // context에 따라 다른 필드 저장
    if (context === 'dashboard') {
      // dashboard: name과 email만
      mutate({ nickname: editedName, email: editedEmail })
      console.log('Saving dashboard profile:', {
        nickname: editedName,
        email: editedEmail,
      })
    } else {
      // project: name, email, description 모두
      mutate({
        nickname: editedName,
        email: editedEmail,
        description: editedDescription,
      })
      console.log('Saving project profile:', {
        nickname: editedName,
        email: editedEmail,
        description: editedDescription,
      })
    }

    setIsEditing(false)
  }

  // 취소
  const handleCancel = () => {
    setIsEditing(false)
  }

  const handleEditToggle = () => {
    if (isEditing) {
      handleSave()
    } else {
      handleStartEdit()
    }
  }

  // 데이터 로딩 중인지 확인
  const isLoadingData = !userInfo

  return (
    <div className="rounded-xl border p-4 bg-white shadow-sm">
      <div className="flex justify-between items-start gap-5 ">
        <div className="flex flex-row gap-5">
          {/* 프로필 이미지 영역 */}
          <div
            className={`relative w-20 h-20 flex-shrink-0 rounded-full flex items-center justify-center bg-gray-100 cursor-pointer overflow-hidden border border-gray-200 ${isUploading ? 'opacity-50 cursor-not-allowed' : ''}`}
            onClick={handleImageClick}
          >
            {userInfo?.imageUrl ? (
              <img
                src={userInfo.imageUrl}
                alt="Profile Preview"
                className="w-full h-full object-cover rounded-full"
              />
            ) : (
              <UserIcon className="w-10 h-10 text-gray-400" />
            )}

            {!isUploading && (
              <div className="bg-black/50 w-20 h-20 rounded-full flex items-center justify-center opacity-0 hover:opacity-100 absolute">
                <Upload className="text-white" />
              </div>
            )}

            {(isUploading || isLoadingData) && (
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
              disabled={isUploading || isLoadingData}
            />
          </div>

          {/* 프로필 정보 영역 */}
          <div className="flex-1 flex flex-col gap-2 ">
            {/* 이름 필드 */}
            <div className="flex items-center gap-2">
              <div className="flex-1">
                <label className="text-xs text-gray-500 block mb-1">Name</label>
                <input
                  type="text"
                  value={isEditing ? editedName : userInfo?.nickname || ''}
                  ref={nameInputRef}
                  disabled={!isEditing || isLoadingData}
                  onChange={e => setEditedName(e.target.value)}
                  className="w-full text-lg font-semibold bg-transparent outline-none ring-0 disabled:border-transparent disabled:ring-0 disabled:cursor-default enabled:border enabled:border-gray-300 enabled:ring-1 enabled:ring-gray-300 rounded-md px-2 py-1"
                  onKeyDown={e => {
                    if (e.key === 'Enter' && isEditing) handleSave()
                    if (e.key === 'Escape' && isEditing) handleCancel()
                  }}
                />
              </div>
            </div>

            {/* 이메일 필드 */}
            <div className="flex items-center gap-2">
              <div className="flex-1">
                <label className="text-xs text-gray-500 block mb-1">
                  Email
                </label>
                <input
                  type="email"
                  value={
                    isEditing
                      ? editedEmail
                      : userInfo?.email || '등록된 이메일이 없습니다.'
                  }
                  ref={emailInputRef}
                  disabled={!isEditing || isLoadingData}
                  onChange={e => setEditedEmail(e.target.value)}
                  className="w-full text-sm bg-transparent outline-none ring-0 disabled:border-transparent disabled:ring-0 disabled:cursor-default enabled:border enabled:border-gray-300 enabled:ring-1 enabled:ring-gray-300 rounded-md px-2 py-1"
                  onKeyDown={e => {
                    if (e.key === 'Enter' && isEditing) handleSave()
                    if (e.key === 'Escape' && isEditing) handleCancel()
                  }}
                />
              </div>
            </div>

            {/* Description 필드 - project context일 때만 표시 */}
            {context === 'project' && (
              <div className="flex items-start gap-2">
                <div className="flex-1">
                  <label className="text-xs text-gray-500 block mb-1">
                    Description
                  </label>
                  <textarea
                    value={
                      isEditing
                        ? editedDescription
                        : userInfo?.description || '등록된 내용이 없습니다.'
                    }
                    ref={descriptionInputRef}
                    disabled={!isEditing || isLoadingData}
                    onChange={e => setEditedDescription(e.target.value)}
                    rows={2}
                    className="w-full text-sm bg-transparent outline-none ring-0 disabled:border-transparent disabled:ring-0 disabled:cursor-default enabled:border enabled:border-gray-300 enabled:ring-1 enabled:ring-gray-300 rounded-md px-2 py-1 resize-none"
                    onKeyDown={e => {
                      if (e.key === 'Enter' && e.ctrlKey && isEditing)
                        handleSave()
                      if (e.key === 'Escape' && isEditing) handleCancel()
                    }}
                  />
                </div>
              </div>
            )}
          </div>
        </div>
        {/* 편집/저장 버튼 */}
        <div className="flex flex-col gap-2 mt-1 w-24 justify-end">
          <Button
            variant="outline"
            size="sm"
            className="text-gray-500 hover:text-gray-700"
            onClick={handleEditToggle}
            disabled={isLoadingData}
          >
            {isEditing ? <div>Save</div> : <div>Edit</div>}
          </Button>
          {isEditing && (
            <Button
              variant="outline"
              size="sm"
              className="text-gray-500 hover:text-gray-700"
              onClick={handleCancel}
            >
              Cancel
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
