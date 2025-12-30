import {
  ForbiddenException,
  Injectable,
  Logger,
  UnauthorizedException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as jwt from 'jsonwebtoken';
import { SpringNoteClient } from '../client/spring-note/spring-note.client';
import { DocType } from '../common/util/doc-id.util';
import { CollabUserPayload } from './interface/collab-user-payload.interface';

interface DecodedToken {
  sub?: string;
  userId?: string;
}

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);
  private readonly jwtSecret: string;

  constructor(
    private readonly config: ConfigService,
    private readonly springNoteClient: SpringNoteClient,
  ) {
    this.jwtSecret = this.config.get<string>('JWT_SECRET') ?? '';
    if (!this.jwtSecret) {
      this.logger.warn('JWT_SECRET is not set');
    }
  }

  verifyToken(token: string): CollabUserPayload {
    try {
      const decoded = jwt.verify(token, this.jwtSecret) as DecodedToken;
      const rawUserId = decoded.sub ?? decoded.userId;

      if (rawUserId === undefined || rawUserId === null) {
        throw new Error('userId (sub) not found in token');
      }

      const userId = Number(rawUserId);
      if (Number.isNaN(userId)) {
        throw new Error('Invalid userId in token');
      }

      return { userId };
    } catch (e) {
      this.logger.warn(`JWT verify failed: ${(e as Error).message}`);
      throw new UnauthorizedException('Invalid token');
    }
  }

  /**
   * 프로젝트 내 문서에 대한 권한 체크
   */
  async checkPermission(
    projectId: string,
    docType: DocType,
    docId: number,
    userId: number,
  ): Promise<void> {
    try {
      await this.springNoteClient.checkPermission(
        projectId,
        docType,
        docId,
        userId,
      );
    } catch (e) {
      this.logger.warn(
        `Permission denied for project=${projectId}, ${docType}/${docId}, userId=${userId}: ${(e as Error).message}`,
      );
      throw new ForbiddenException('No permission to edit this document');
    }
  }
}
