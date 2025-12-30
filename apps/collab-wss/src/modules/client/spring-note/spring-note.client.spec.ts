import { Test, TestingModule } from '@nestjs/testing';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { of, throwError } from 'rxjs';
import { AxiosResponse } from 'axios';
import { SpringNoteClient } from './spring-note.client';
import { NoteData } from './interface/note-data.interface';

describe('SpringNoteClient', () => {
  let client: SpringNoteClient;
  let httpService: jest.Mocked<HttpService>;

  const BASE_URL = 'http://localhost:8080';

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        SpringNoteClient,
        {
          provide: HttpService,
          useValue: {
            get: jest.fn(),
            put: jest.fn(),
          },
        },
        {
          provide: ConfigService,
          useValue: {
            get: jest.fn((key: string) => {
              if (key === 'SPRING_BASE_URL') return BASE_URL;
              return null;
            }),
          },
        },
      ],
    }).compile();

    client = module.get<SpringNoteClient>(SpringNoteClient);
    httpService = module.get(HttpService);
  });

  it('should be defined', () => {
    expect(client).toBeDefined();
  });

  describe('loadNote', () => {
    it('should load note from Spring server', async () => {
      const mockNote: NoteData = { contents: 'test contents' };
      const mockResponse: AxiosResponse<NoteData> = {
        data: mockNote,
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      httpService.get.mockReturnValue(of(mockResponse));

      const result = await client.loadNote('proj123', 'retro', 1);

      expect(result).toEqual(mockNote);
      expect(httpService.get).toHaveBeenCalledWith(
        `${BASE_URL}/internal/projects/proj123/retro/1`,
      );
    });

    it('should handle different doc types', async () => {
      const mockResponse: AxiosResponse<NoteData> = {
        data: { contents: '' },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      httpService.get.mockReturnValue(of(mockResponse));

      await client.loadNote('proj', 'scrum', 5);
      expect(httpService.get).toHaveBeenCalledWith(
        `${BASE_URL}/internal/projects/proj/scrum/5`,
      );

      await client.loadNote('proj', 'meeting', 10);
      expect(httpService.get).toHaveBeenCalledWith(
        `${BASE_URL}/internal/projects/proj/meeting/10`,
      );
    });

    it('should throw error when request fails', async () => {
      httpService.get.mockReturnValue(
        throwError(() => new Error('Network error')),
      );

      await expect(client.loadNote('proj', 'retro', 1)).rejects.toThrow(
        'Network error',
      );
    });
  });

  describe('saveContents', () => {
    it('should save contents to Spring server', async () => {
      const mockResponse: AxiosResponse = {
        data: {},
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      httpService.put.mockReturnValue(of(mockResponse));

      await client.saveContents('proj123', 'retro', 1, 'new contents');

      expect(httpService.put).toHaveBeenCalledWith(
        `${BASE_URL}/internal/projects/proj123/retro/1/contents`,
        { contents: 'new contents' },
      );
    });

    it('should throw error when save fails', async () => {
      httpService.put.mockReturnValue(
        throwError(() => new Error('Save failed')),
      );

      await expect(
        client.saveContents('proj', 'retro', 1, 'contents'),
      ).rejects.toThrow('Save failed');
    });
  });

  describe('checkPermission', () => {
    it('should pass when user has edit permission', async () => {
      const mockResponse: AxiosResponse<{ editable: boolean }> = {
        data: { editable: true },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      httpService.get.mockReturnValue(of(mockResponse));

      await expect(
        client.checkPermission('proj123', 'retro', 1, 123),
      ).resolves.toBeUndefined();

      expect(httpService.get).toHaveBeenCalledWith(
        `${BASE_URL}/internal/projects/proj123/retro/1/permission`,
        { params: { userId: 123 } },
      );
    });

    it('should throw error when user has no permission', async () => {
      const mockResponse: AxiosResponse<{ editable: boolean }> = {
        data: { editable: false },
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {} as any,
      };

      httpService.get.mockReturnValue(of(mockResponse));

      await expect(
        client.checkPermission('proj', 'retro', 1, 123),
      ).rejects.toThrow('User does not have edit permission for this document');
    });

    it('should throw error when permission check fails', async () => {
      httpService.get.mockReturnValue(
        throwError(() => new Error('Permission check failed')),
      );

      await expect(
        client.checkPermission('proj', 'retro', 1, 123),
      ).rejects.toThrow('Permission check failed');
    });
  });
});