import { Test, TestingModule } from '@nestjs/testing';
import { ConfigService } from '@nestjs/config';
import { UnauthorizedException, ForbiddenException } from '@nestjs/common';
import * as jwt from 'jsonwebtoken';
import { AuthService } from './auth.service';
import { SpringNoteClient } from '../client/spring-note/spring-note.client';

describe('AuthService', () => {
  let service: AuthService;
  let springNoteClient: jest.Mocked<SpringNoteClient>;

  const JWT_SECRET = 'test-secret';

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        {
          provide: ConfigService,
          useValue: {
            get: jest.fn((key: string) => {
              if (key === 'JWT_SECRET') return JWT_SECRET;
              return null;
            }),
          },
        },
        {
          provide: SpringNoteClient,
          useValue: {
            checkPermission: jest.fn(),
          },
        },
      ],
    }).compile();

    service = module.get<AuthService>(AuthService);
    springNoteClient = module.get(SpringNoteClient);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('verifyToken', () => {
    it('should return userId from valid token with sub claim', () => {
      const token = jwt.sign({ sub: '123' }, JWT_SECRET);

      const result = service.verifyToken(token);

      expect(result).toEqual({ userId: 123 });
    });

    it('should return userId from valid token with userId claim', () => {
      const token = jwt.sign({ userId: '456' }, JWT_SECRET);

      const result = service.verifyToken(token);

      expect(result).toEqual({ userId: 456 });
    });

    it('should throw UnauthorizedException for invalid token', () => {
      const invalidToken = 'invalid-token';

      expect(() => service.verifyToken(invalidToken)).toThrow(
        UnauthorizedException,
      );
    });

    it('should throw UnauthorizedException for expired token', () => {
      const expiredToken = jwt.sign({ sub: '123', exp: 0 }, JWT_SECRET);

      expect(() => service.verifyToken(expiredToken)).toThrow(
        UnauthorizedException,
      );
    });

    it('should throw UnauthorizedException for token with wrong secret', () => {
      const token = jwt.sign({ sub: '123' }, 'wrong-secret');

      expect(() => service.verifyToken(token)).toThrow(UnauthorizedException);
    });

    it('should throw UnauthorizedException for token without userId', () => {
      const token = jwt.sign({ foo: 'bar' }, JWT_SECRET);

      expect(() => service.verifyToken(token)).toThrow(UnauthorizedException);
    });

    it('should throw UnauthorizedException for token with invalid userId format', () => {
      const token = jwt.sign({ sub: 'not-a-number' }, JWT_SECRET);

      expect(() => service.verifyToken(token)).toThrow(UnauthorizedException);
    });
  });

  describe('checkPermission', () => {
    it('should pass when user has permission', async () => {
      springNoteClient.checkPermission.mockResolvedValue(undefined);

      await expect(
        service.checkPermission('proj123', 'retro', 1, 123),
      ).resolves.toBeUndefined();

      expect(springNoteClient.checkPermission).toHaveBeenCalledWith(
        'proj123',
        'retro',
        1,
        123,
      );
    });

    it('should throw ForbiddenException when user has no permission', async () => {
      springNoteClient.checkPermission.mockRejectedValue(
        new Error('No permission'),
      );

      await expect(
        service.checkPermission('proj123', 'retro', 1, 123),
      ).rejects.toThrow(ForbiddenException);
    });

    it('should handle different docTypes correctly', async () => {
      springNoteClient.checkPermission.mockResolvedValue(undefined);

      await service.checkPermission('proj', 'scrum', 5, 100);
      expect(springNoteClient.checkPermission).toHaveBeenCalledWith(
        'proj',
        'scrum',
        5,
        100,
      );

      await service.checkPermission('proj', 'meeting', 10, 200);
      expect(springNoteClient.checkPermission).toHaveBeenCalledWith(
        'proj',
        'meeting',
        10,
        200,
      );
    });
  });
});