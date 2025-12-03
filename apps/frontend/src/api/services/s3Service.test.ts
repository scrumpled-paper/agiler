import { describe, it, expect, vi, beforeEach } from 'vitest'
import { s3Service } from './s3Service'
import { apiClient } from '../client'
import axios from 'axios'

// Mock apiClient
vi.mock('../client', () => ({
  apiClient: {
    post: vi.fn(),
  },
}))

// Mock axios
vi.mock('axios', () => ({
  default: {
    put: vi.fn(),
  },
}))

describe('s3Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getPreSignedUrl', () => {
    it('should get pre-signed URL successfully', async () => {
      const mockResponse = {
        preSignedUrl: 'https://s3.amazonaws.com/bucket/file?signature=xyz',
        objectKey: 'uploads/12345/image.jpg',
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse })

      const result = await s3Service.getPreSignedUrl('image.jpg', 'image/jpeg')

      expect(apiClient.post).toHaveBeenCalledWith('/api/v1/s3/pre-signed-url', {
        fileName: 'image.jpg',
        contentType: 'image/jpeg',
      })
      expect(result).toEqual(mockResponse)
      expect(result.preSignedUrl).toContain('s3.amazonaws.com')
      expect(result.objectKey).toContain('uploads/')
    })

    it('should handle error when getting pre-signed URL fails', async () => {
      const error = new Error('Failed to generate pre-signed URL')

      vi.mocked(apiClient.post).mockRejectedValue(error)

      await expect(
        s3Service.getPreSignedUrl('image.jpg', 'image/jpeg')
      ).rejects.toThrow('Failed to generate pre-signed URL')
    })

    it('should get pre-signed URL for different file types', async () => {
      const mockResponse = {
        preSignedUrl: 'https://s3.amazonaws.com/bucket/file.pdf',
        objectKey: 'uploads/docs/file.pdf',
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse })

      const result = await s3Service.getPreSignedUrl(
        'document.pdf',
        'application/pdf'
      )

      expect(apiClient.post).toHaveBeenCalledWith('/api/v1/s3/pre-signed-url', {
        fileName: 'document.pdf',
        contentType: 'application/pdf',
      })
      expect(result.objectKey).toContain('file.pdf')
    })

    it('should handle special characters in file name', async () => {
      const mockResponse = {
        preSignedUrl: 'https://s3.amazonaws.com/bucket/encoded-file',
        objectKey: 'uploads/special-file-name.jpg',
      }

      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse })

      await s3Service.getPreSignedUrl('파일 이름.jpg', 'image/jpeg')

      expect(apiClient.post).toHaveBeenCalledWith('/api/v1/s3/pre-signed-url', {
        fileName: '파일 이름.jpg',
        contentType: 'image/jpeg',
      })
    })
  })

  describe('uploadFileToS3', () => {
    it('should upload file to S3 successfully', async () => {
      const mockFile = new File(['test content'], 'test.jpg', {
        type: 'image/jpeg',
      })
      const preSignedUrl = 'https://s3.amazonaws.com/bucket/test.jpg'

      vi.mocked(axios.put).mockResolvedValue({ status: 200 })

      await s3Service.uploadFileToS3(preSignedUrl, mockFile, 'image/jpeg')

      expect(axios.put).toHaveBeenCalledWith(preSignedUrl, mockFile, {
        headers: {
          'Content-Type': 'image/jpeg',
        },
      })
    })

    it('should handle error when S3 upload fails', async () => {
      const mockFile = new File(['test content'], 'test.jpg', {
        type: 'image/jpeg',
      })
      const preSignedUrl = 'https://s3.amazonaws.com/bucket/test.jpg'
      const error = new Error('S3 upload failed')

      vi.mocked(axios.put).mockRejectedValue(error)

      await expect(
        s3Service.uploadFileToS3(preSignedUrl, mockFile, 'image/jpeg')
      ).rejects.toThrow('S3 upload failed')
    })

    it('should upload file with correct content type', async () => {
      const mockFile = new File(['test content'], 'test.png', {
        type: 'image/png',
      })
      const preSignedUrl = 'https://s3.amazonaws.com/bucket/test.png'

      vi.mocked(axios.put).mockResolvedValue({ status: 200 })

      await s3Service.uploadFileToS3(preSignedUrl, mockFile, 'image/png')

      expect(axios.put).toHaveBeenCalledWith(preSignedUrl, mockFile, {
        headers: {
          'Content-Type': 'image/png',
        },
      })
    })

    it('should upload large file successfully', async () => {
      const largeContent = new Array(1024 * 1024).fill('a').join('')
      const mockFile = new File([largeContent], 'large.jpg', {
        type: 'image/jpeg',
      })
      const preSignedUrl = 'https://s3.amazonaws.com/bucket/large.jpg'

      vi.mocked(axios.put).mockResolvedValue({ status: 200 })

      await s3Service.uploadFileToS3(preSignedUrl, mockFile, 'image/jpeg')

      expect(axios.put).toHaveBeenCalled()
    })
  })

  describe('uploadProfileImage', () => {
    it('should complete full upload process successfully', async () => {
      const mockFile = new File(['test content'], 'profile.jpg', {
        type: 'image/jpeg',
      })
      const mockPreSignedResponse = {
        preSignedUrl: 'https://s3.amazonaws.com/bucket/profile.jpg',
        objectKey: 'profiles/12345/profile.jpg',
      }

      vi.mocked(apiClient.post).mockResolvedValue({
        data: mockPreSignedResponse,
      })
      vi.mocked(axios.put).mockResolvedValue({ status: 200 })

      const result = await s3Service.uploadProfileImage(mockFile)

      expect(apiClient.post).toHaveBeenCalledWith('/api/v1/s3/pre-signed-url', {
        fileName: 'profile.jpg',
        contentType: 'image/jpeg',
      })
      expect(axios.put).toHaveBeenCalledWith(
        mockPreSignedResponse.preSignedUrl,
        mockFile,
        {
          headers: {
            'Content-Type': 'image/jpeg',
          },
        }
      )
      expect(result).toBe('profiles/12345/profile.jpg')
    })

    it('should handle error during pre-signed URL generation', async () => {
      const mockFile = new File(['test content'], 'profile.jpg', {
        type: 'image/jpeg',
      })
      const error = new Error('Failed to get pre-signed URL')

      vi.mocked(apiClient.post).mockRejectedValue(error)

      await expect(s3Service.uploadProfileImage(mockFile)).rejects.toThrow(
        'Failed to get pre-signed URL'
      )
      expect(axios.put).not.toHaveBeenCalled()
    })

    it('should handle error during S3 upload', async () => {
      const mockFile = new File(['test content'], 'profile.jpg', {
        type: 'image/jpeg',
      })
      const mockPreSignedResponse = {
        preSignedUrl: 'https://s3.amazonaws.com/bucket/profile.jpg',
        objectKey: 'profiles/12345/profile.jpg',
      }

      vi.mocked(apiClient.post).mockResolvedValue({
        data: mockPreSignedResponse,
      })
      vi.mocked(axios.put).mockRejectedValue(new Error('Upload failed'))

      await expect(s3Service.uploadProfileImage(mockFile)).rejects.toThrow(
        'Upload failed'
      )
    })

    it('should upload different image formats', async () => {
      const mockFile = new File(['test content'], 'avatar.png', {
        type: 'image/png',
      })
      const mockPreSignedResponse = {
        preSignedUrl: 'https://s3.amazonaws.com/bucket/avatar.png',
        objectKey: 'profiles/67890/avatar.png',
      }

      vi.mocked(apiClient.post).mockResolvedValue({
        data: mockPreSignedResponse,
      })
      vi.mocked(axios.put).mockResolvedValue({ status: 200 })

      const result = await s3Service.uploadProfileImage(mockFile)

      expect(apiClient.post).toHaveBeenCalledWith('/api/v1/s3/pre-signed-url', {
        fileName: 'avatar.png',
        contentType: 'image/png',
      })
      expect(result).toBe('profiles/67890/avatar.png')
    })

    it('should return object key for successful upload', async () => {
      const mockFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const expectedObjectKey = 'profiles/user123/avatar.jpg'

      vi.mocked(apiClient.post).mockResolvedValue({
        data: {
          preSignedUrl: 'https://s3.amazonaws.com/test',
          objectKey: expectedObjectKey,
        },
      })
      vi.mocked(axios.put).mockResolvedValue({ status: 200 })

      const objectKey = await s3Service.uploadProfileImage(mockFile)

      expect(objectKey).toBe(expectedObjectKey)
    })
  })
})
