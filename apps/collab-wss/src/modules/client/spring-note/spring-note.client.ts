import { Injectable, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { lastValueFrom } from 'rxjs';
import { NoteData } from './interface/note-data.interface';
import { DocType } from '../../common/util/doc-id.util';

@Injectable()
export class SpringNoteClient {
  private readonly logger = new Logger(SpringNoteClient.name);
  private readonly baseUrl: string;

  constructor(
    private readonly http: HttpService,
    private readonly config: ConfigService,
  ) {
    this.baseUrl = this.config.get<string>('SPRING_BASE_URL') ?? '';
    if (!this.baseUrl) {
      this.logger.warn('SPRING_BASE_URL is not set');
    }
  }

  /**
   * 프로젝트 내 문서 내용 로드
   * GET /internal/projects/{projectId}/{docType}/{docId}
   */
  async loadNote(
    projectId: string,
    docType: DocType,
    docId: number,
  ): Promise<NoteData> {
    const url = `${this.baseUrl}/internal/projects/${projectId}/${docType}/${docId}`;
    this.logger.log(`Loading note from Spring: ${url}`);

    const res = await lastValueFrom(this.http.get<NoteData>(url));
    return res.data;
  }

  /**
   * 프로젝트 내 문서 내용 저장
   * PUT /internal/projects/{projectId}/{docType}/{docId}/contents
   */
  async saveContents(
    projectId: string,
    docType: DocType,
    docId: number,
    contents: string,
  ): Promise<void> {
    const url = `${this.baseUrl}/internal/projects/${projectId}/${docType}/${docId}/contents`;
    this.logger.debug(`Saving contents to Spring: ${url}`);

    await lastValueFrom(this.http.put(url, { contents }));
  }

  /**
   * 프로젝트 내 문서 편집 권한 체크
   * GET /internal/projects/{projectId}/{docType}/{docId}/permission?userId={userId}
   */
  async checkPermission(
    projectId: string,
    docType: DocType,
    docId: number,
    userId: number,
  ): Promise<void> {
    const url = `${this.baseUrl}/internal/projects/${projectId}/${docType}/${docId}/permission`;
    this.logger.debug(
      `Checking permission: project=${projectId}, ${docType}/${docId}, userId=${userId}`,
    );

    const res = await lastValueFrom(
      this.http.get<{ editable: boolean }>(url, {
        params: { userId },
      }),
    );

    if (!res.data?.editable) {
      throw new Error('User does not have edit permission for this document');
    }
  }
}
