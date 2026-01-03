// src/yjs/yjs.gateway.ts
import { Injectable, Logger, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { WebSocketServer, WebSocket } from 'ws';
import { AuthService } from '../auth/auth.service';
import { YjsService } from './yjs.service';
import { IncomingMessage } from 'http';
import {ConfigService} from "@nestjs/config";

@Injectable()
export class YjsGateway implements OnModuleInit, OnModuleDestroy {
    private readonly logger = new Logger(YjsGateway.name);
    private wss: WebSocketServer;

    constructor(
        private readonly authService: AuthService,
        private readonly yjsService: YjsService,
        private configService: ConfigService,
    ) {}

    onModuleInit() {
        const port = this.configService.getOrThrow<number>('SOCKET_PORT');
        const wsUrl = this.configService.get<string>('WS_URL') ?? `ws://localhost:${port}`;

        this.wss = new WebSocketServer({ port });

        this.wss.on('connection', async (ws: WebSocket, req: IncomingMessage) => {
            try {
                // ✅ URL에서 쿼리 파라미터 추출
                const url = new URL(req.url!, `http://${req.headers.host}`);

                const result = url.searchParams.get('wssToken');
                const wssToken = wssToken.replace(/\/[^\/]*$/, "");

                if (!wssToken) {
                    throw new Error('Missing wssToken parameter');
                }

                // AuthService 검증
                const { userId, docId } = await this.authService.connectWebSocket(
                    wssToken,
                    ws,
                );

                this.logger.log(`✅ 연결 성공: userId=${userId}, docId=${docId}`);

            } catch (error: any) {
                this.logger.error(`❌ 연결 실패: ${error.message}`);
                ws.close(1008, error.message || 'Connection failed');
            }
        });

        this.logger.log(`🚀 WebSocket 서버 시작: ${wsUrl}`);
    }

    onModuleDestroy() {
        this.wss.clients.forEach((client) => {
            if (client.readyState === WebSocket.OPEN) {
                client.close();
            }
        });
        this.wss.close();
        this.yjsService.cleanup();
    }
}
