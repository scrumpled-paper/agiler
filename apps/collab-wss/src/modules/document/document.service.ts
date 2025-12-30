import {Injectable, HttpException, HttpStatus, Logger} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';
import {AuthController} from "../auth/auth.controller";
import { DocumentDto } from './dto/document-response.dto';

@Injectable()
export class DocumentService {
    private readonly logger = new Logger(AuthController.name);

    constructor(
        private configService: ConfigService,
        private httpService: HttpService,
    ) {
    }

    async fetchDocumentFromSpring(docId: string): Promise<DocumentDto> {
        const springServerUrl = this.configService.getOrThrow<string>('SPRING_SERVER_URL');
        const secret = this.configService.getOrThrow<string>('WSS_SECRET_KEY');

        const [docType, id] = docId.split('-');

        let endpoint: string;
        switch (docType.toLowerCase()) {
            case 'meeting':
                endpoint = `/internal/api/v1/docs/meeting/${id}`;
                break;
            case 'retro':
                endpoint = `/internal/api/v1/docs/retro/${id}`;
                break;
            case 'scrums':
                endpoint = `/internal/api/v1/docs/scrums/${id}`;
                break;
            default:
                this.logger.error(`❌ 지원하지 않는 문서 타입: ${docType}`);
                throw new HttpException(
                    `Unsupported document type: ${docType}`,
                    HttpStatus.BAD_REQUEST,
                );
        }

        const url = `${springServerUrl}${endpoint}`;

        try {
            const response = await firstValueFrom(
                this.httpService.get(url, {
                    headers: {
                        'Content-Type': 'application/json',
                        'X-API-KEY': secret,
                    },
                }),
            );

            this.logger.log(`✅ Spring 응답 성공: ${JSON.stringify(response.data)}`);
            return response.data;
        } catch (error: any) {
            this.logger.error(`❌ Spring 요청 실패: ${error.message}`);
            if (error.response) {
                this.logger.error(`Status: ${error.response.status}`);
                this.logger.error(`Data: ${JSON.stringify(error.response.data)}`);
            }
            throw new HttpException(
                'Failed to fetch document from Spring server',
                HttpStatus.BAD_GATEWAY,
            );
        }
    }

    async saveDocumentToSpring(
        docId: string,
        title: string,
        contents: string,
    ): Promise<{ success: boolean; message?: string; }> {
        const springServerUrl = this.configService.getOrThrow<string>('SPRING_SERVER_URL');
        const secret = this.configService.getOrThrow<string>('WSS_SECRET_KEY');

        const [docType, id] = docId.split('-');

        let endpoint: string;
        switch (docType.toLowerCase()) {
            case 'meeting':
                endpoint = `/internal/api/v1/docs/meeting/${id}`;
                break;
            case 'retro':
                endpoint = `/internal/api/v1/docs/retro/${id}`;
                break;
            case 'scrums':
                endpoint = `/internal/api/v1/docs/scrums/${id}`;
                break;
            default:
                throw new HttpException(
                    `Unsupported document type: ${docType}`,
                    HttpStatus.BAD_REQUEST,
                );
        }

        const url = `${springServerUrl}${endpoint}`;

        const payload = { title, contents };

        try {
            const response = await firstValueFrom(
                this.httpService.put(url, payload, {
                    headers: {
                        'Content-Type': 'application/json',
                        'X-API-KEY': secret,
                    },
                }),
            );

            this.logger.log(`✅ 문서 저장 성공: ${docId}`);
            return response.data;
        } catch (error: any) {
            this.logger.error(`❌ 문서 저장 실패: ${error.message}`);
            if (error.response) {
                this.logger.error(`Status: ${error.response.status}`);
                this.logger.error(`Data: ${JSON.stringify(error.response.data)}`);
            }
            throw new HttpException(
                'Failed to save document to Spring server',
                HttpStatus.BAD_GATEWAY,
            );
        }
    }
}
