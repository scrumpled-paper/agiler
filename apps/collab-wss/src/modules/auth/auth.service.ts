// src/auth/auth.service.ts
import {HttpException, HttpStatus, Injectable, Logger} from '@nestjs/common';
import {ConfigService} from '@nestjs/config';
import * as jwt from 'jsonwebtoken';
import {YjsService} from '../yjs/yjs.service';
import {WebSocket} from 'ws';

interface WssTokenPayload {
    sub: string;
    docId: string;
    iat: number;
    exp: number;
}

@Injectable()
export class AuthService {
    private readonly logger = new Logger(AuthService.name);

    constructor(
        private configService: ConfigService,
        private yjsService: YjsService,
    ) {}

    private verifyWssToken(wssToken: string): WssTokenPayload {
        const secret = this.configService.getOrThrow<string>('WSS_SECRET_KEY');
        try {
            return jwt.verify(wssToken, secret) as WssTokenPayload;
        } catch (error) {
            throw new HttpException('Invalid WSS token', HttpStatus.UNAUTHORIZED);
        }
    }

    async verifyAndPrepareDoc(wssToken: string): Promise<{
        docId: string;
        userId: string;
        ready: boolean;
    }> {
        // 1. WSS 토큰 검증
        const { sub: userId, docId } = this.verifyWssToken(wssToken);

        // 2. Y.Doc 준비 (없으면 Spring에서 조회)
        await this.yjsService.getOrCreateDoc(docId);

        return {
            docId,
            userId,
            ready: true,
        };
    }

    async connectWebSocket(
        wssToken: string | null,
        ws: WebSocket,
    ): Promise<{
        docId: string;
        userId: string;
    }> {
        if (!wssToken) {
            throw new HttpException('Missing WSS token', HttpStatus.BAD_REQUEST);
        }
        // 1. WSS 토큰 검증
        const { sub: userId, docId } = this.verifyWssToken(wssToken);

        // 2. Y.Doc 가져오기 또는 생성
        const doc = await this.yjsService.getOrCreateDoc(docId);

        // 3. WebSocket 연결
        this.yjsService.connectWebSocket(ws, docId, doc);

        return { docId, userId };
    }
}
