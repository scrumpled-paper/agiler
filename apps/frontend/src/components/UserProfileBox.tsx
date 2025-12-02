import { useRef, useState } from 'react'
import { Button } from './ui/button'
import { Camera, Trash2, UserIcon } from 'lucide-react'
import { s3Service } from '@/api/services/s3Service'
import {
  useUserInfo,
  useDashboardProfileMutation,
  useProjectProfileMutation,
} from '@/hooks/use-user'
import { userService } from '@/api/services/userService'
import { projectService } from '@/api/services/projectService'

type UserProfileBoxProps = {
  context: 'dashboard' | 'project'
  projectUrl?: string
}

export default function UserProfileBox({
  context,
  projectUrl,
}: UserProfileBoxProps) {
  const [userInfo, setUserInfo] = useState(useUserInfo(context, projectUrl))
  // 1. useUserInfo를 사용하여 데이터 조회
  // const userInfo = useUserInfo(context, projectUrl)

  // 2. context에 따라 적절한 mutation 훅 사용
  const dashboardMutation = useDashboardProfileMutation()
  const projectMutation = useProjectProfileMutation(projectUrl || '')

  // 로컬 상태 (편집 모드에서만 사용)
  const [isEditing, setIsEditing] = useState<boolean>(false)
  const [isUploading, setIsUploading] = useState(false)
  const [isImageHovered, setIsImageHovered] = useState(false)
  const [editedName, setEditedName] = useState<string>('')
  const [editedEmail, setEditedEmail] = useState<string>('')
  const [editedDescription, setEditedDescription] = useState<string>('')
  const [preview, setPreview] = useState<string | null>(null)

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
        await projectService.updateUserImage(projectUrl, objectKey)
        setUserInfo(projectService.getUserInfo(projectUrl))
        console.log(userInfo)
      } else {
        // dashboard 유저 이미지 수정
        await userService.updateUserImage(objectKey)
      }
      // 업로드 성공 후 데이터 새로고침
      setPreview(null)
    } catch (error) {
      console.error('Error uploading image or updating profile:', error)
      setPreview(null)
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

  const handleImageDelete = async () => {
    if (isUploading || !userInfo?.imageUrl) {
      return
    }

    if (!confirm('프로필 이미지를 삭제하시겠습니까?')) {
      return
    }

    setIsUploading(true)

    try {
      // 빈 문자열로 이미지 삭제
      if (projectUrl) {
        await projectService.deleteUserImage(projectUrl)
      } else {
        await userService.deleteUserImage()
      }
      setPreview(null)
    } catch (error) {
      console.error('Error deleting image:', error)
    } finally {
      setIsUploading(false)
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
      dashboardMutation.mutate({ nickname: editedName, email: editedEmail })
    } else {
      // project: name, email, description 모두
      projectMutation.mutate({
        nickname: editedName,
        email: editedEmail,
        description: editedDescription,
      })
    }

    setIsEditing(false)
  }

  // 취소
  const handleCancel = () => {
    setPreview(null)
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
    <div className="flex flex-row gap-4 justify-center w-full">
      {/* 프로필 이미지 박스 */}
      <div className="rounded-xl border border-gray-200 p-6 bg-white shadow-sm hover:shadow-md transition-shadow">
        <div className="flex flex-col items-center gap-3">
          <h3 className="text-sm font-semibold text-gray-700 self-start">
            Profile Image
          </h3>
          <div className="relative group">
            <div
              className={`relative w-32 h-32 flex-shrink-0 rounded-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200 overflow-hidden border-2 border-gray-300 transition-all ${isUploading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:border-blue-400'}`}
              onMouseEnter={() => setIsImageHovered(true)}
              onMouseLeave={() => setIsImageHovered(false)}
            >
              {preview || userInfo?.imageUrl ? (
                <img
                  src={preview || userInfo.imageUrl}
                  alt="Profile"
                  className="w-full h-full object-cover"
                />
              ) : (
                <UserIcon className="w-16 h-16 text-gray-400" />
              )}

              {/* 호버 시 오버레이 */}
              {!isUploading && !isLoadingData && isImageHovered && (
                <div className="absolute inset-0 bg-black/60 flex items-center justify-center gap-3">
                  <button
                    onClick={handleImageClick}
                    className="p-3 bg-white/90 rounded-full hover:bg-white transition-colors"
                    title="이미지 변경"
                  >
                    <Camera className="w-6 h-6 text-gray-700" />
                  </button>
                  {(preview || userInfo?.imageUrl) && (
                    <button
                      onClick={handleImageDelete}
                      className="p-3 bg-white/90 rounded-full hover:bg-white transition-colors"
                      title="이미지 삭제"
                    >
                      <Trash2 className="w-6 h-6 text-red-600" />
                    </button>
                  )}
                </div>
              )}

              {/* 로딩 스피너 */}
              {(isUploading || isLoadingData) && (
                <div className="absolute inset-0 flex items-center justify-center bg-black/40">
                  <div className="w-10 h-10 border-3 border-t-transparent border-white rounded-full animate-spin"></div>
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
          </div>
          {/* 디자인 수정을 위한 주석 */}
          {/* <p className="text-xs text-gray-500 text-center">
            이미지에 마우스를 올려 변경하거나 삭제할 수 있습니다
          </p> */}
        </div>
      </div>

      {/* 프로필 정보 박스 */}
      <div className="flex-1 rounded-xl border border-gray-200 p-6 bg-white shadow-sm hover:shadow-md transition-shadow">
        <div className="flex justify-between items-start gap-6">
          <div className="flex-1 flex flex-col gap-3">
            <h3 className="text-sm font-semibold text-gray-700 mb-1">
              Profile Information
            </h3>

            {/* 이름 필드 */}
            <div className="flex items-center gap-2">
              <div className="flex-1">
                <label className="text-xs font-medium text-gray-600 block mb-1.5">
                  Name
                </label>
                <input
                  type="text"
                  value={isEditing ? editedName : userInfo?.nickname || ''}
                  ref={nameInputRef}
                  disabled={!isEditing || isLoadingData}
                  onChange={e => setEditedName(e.target.value)}
                  className="w-full text-lg font-semibold text-gray-900 bg-transparent outline-none ring-0 disabled:border-transparent disabled:ring-0 disabled:cursor-default enabled:border-2 enabled:border-blue-300 enabled:focus:border-blue-500 enabled:ring-0 rounded-lg px-3 py-2 transition-colors"
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
                <label className="text-xs font-medium text-gray-600 block mb-1.5">
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
                  className="w-full text-sm text-gray-700 bg-transparent outline-none ring-0 disabled:border-transparent disabled:ring-0 disabled:cursor-default enabled:border-2 enabled:border-blue-300 enabled:focus:border-blue-500 enabled:ring-0 rounded-lg px-3 py-2 transition-colors"
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
                  <label className="text-xs font-medium text-gray-600 block mb-1.5">
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
                    className="w-full text-sm text-gray-700 bg-transparent outline-none ring-0 disabled:border-transparent disabled:ring-0 disabled:cursor-default enabled:border-2 enabled:border-blue-300 enabled:focus:border-blue-500 enabled:ring-0 rounded-lg px-3 py-2 resize-none transition-colors"
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

          {/* 편집/저장 버튼 */}
          <div className="flex flex-col gap-2">
            <Button
              variant={isEditing ? 'default' : 'outline'}
              size="sm"
              className={
                isEditing
                  ? 'bg-black hover:bg-gray-700 text-white min-w-[90px]'
                  : 'border-gray-300 text-gray-700 hover:bg-gray-50 hover:text-gray-900 min-w-[90px]'
              }
              onClick={handleEditToggle}
              disabled={isLoadingData}
            >
              {isEditing ? (
                <div className="flex items-center gap-1.5">
                  <span>Save</span>
                </div>
              ) : (
                <div className="flex items-center gap-1.5">
                  {/* <Pencil className="w-3.5 h-3.5" /> */}
                  <span>Edit</span>
                </div>
              )}
            </Button>
            {isEditing && (
              <Button
                variant="outline"
                size="sm"
                className="border-gray-300 text-gray-700 hover:bg-gray-50 hover:text-gray-900 min-w-[90px]"
                onClick={handleCancel}
              >
                Cancel
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
