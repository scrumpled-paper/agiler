import { Injectable, Logger } from '@nestjs/common';
import { Server as HttpServer, IncomingMessage } from 'http';
import { Socket } from 'net';
import WebSocket, { WebSocketServer } from 'ws';
import * as Y from 'yjs';
import { YdocManagerService } from './ydoc-manager.service';
import { AuthService } from '../auth/auth.service';
import { parsePath, buildDocKey, DocType } from '../common/util/doc-id.util';

interface DocConnections {
  clients: Set<WebSocket>;
  ydoc: Y.Doc;
}

interface ParsedRequest {
  projectUrl: string;
  docType: DocType;
  docId: number;
  token: string;
}

@Injectable()
export class CollabWsService {
  private readonly logger = new Logger(CollabWsService.name);

  private readonly wss = new WebSocketServer({ noServer: true });
  private readonly docs = new Map<string, DocConnections>();

  constructor(
    private readonly authService: AuthService,
    private readonly ydocManager: YdocManagerService,
  ) {}

  bind(server: HttpServer) {
    this.logger.log('CollabWsService.bind() called');

    server.on(
      'upgrade',
      (req: IncomingMessage, socket: Socket, head: Buffer) => {
        void (async () => {
          try {
            const parsed = this.parseRequest(req);

            if (!parsed) {
              this.rejectSocket(socket, 400, 'Invalid request');
              return;
            }

            const { projectUrl, docType, docId, token } = parsed;

            // 1. JWT 검증
            const { userId } = this.authService.verifyToken(token);

            // 2. 문서 권한 체크
            await this.authService.checkPermission(
              projectUrl,
              docType,
              docId,
              userId,
            );

            // 3. WebSocket 업그레이드
            const docKey = buildDocKey(projectId, docType, docId);

            this.wss.handleUpgrade(req, socket, head, (ws) => {
              // Sec-WebSocket-Protocol 응답 헤더 설정
              this.wss.emit('connection', ws, req);
              void this.handleConnection(ws, docKey, userId);
            });
          } catch (e) {
            this.logger.warn(
              `WebSocket upgrade failed: ${(e as Error).message}`,
            );
            try {
              this.rejectSocket(socket, 401, 'Unauthorized');
            } catch {
              // ignore
            }
          }
        })();
      },
    );
  }

  /**
   * 요청에서 경로와 토큰을 파싱
   * URL: /collab/{projectId}/{docType}/{docId}
   * Token: Sec-WebSocket-Protocol 헤더에서 추출
   */
  private parseRequest(req: IncomingMessage): ParsedRequest | null {
    try {
      const urlPath = req.url?.split('?')[0] ?? '/';
      const { projectUrl, type, id } = parsePath(urlPath);

      // Sec-WebSocket-Protocol 헤더에서 토큰 추출
      // 클라이언트: new WebSocket(url, ['auth', 'Bearer.xxx.yyy.zzz'])
      const protocols = req.headers['sec-websocket-protocol'];
      const token = this.extractTokenFromProtocol(protocols);

      if (!token) {
        this.logger.warn('Token not found in Sec-WebSocket-Protocol header');
        return null;
      }

      return {
        projectUrl,
        docType: type,
        docId: id,
        token,
      };
    } catch (e) {
      this.logger.warn(`Failed to parse request: ${(e as Error).message}`);
      return null;
    }
  }

  /**
   * Sec-WebSocket-Protocol 헤더에서 토큰 추출
   * 형식: "auth, Bearer.xxx.yyy.zzz" 또는 배열
   */
  private extractTokenFromProtocol(
    protocols: string | string[] | undefined,
  ): string | null {
    if (!protocols) return null;

    const protocolList = Array.isArray(protocols)
      ? protocols
      : protocols.split(',').map((p) => p.trim());

    // "Bearer.xxx.yyy.zzz" 형태의 프로토콜 찾기
    const bearerProtocol = protocolList.find((p) => p.startsWith('Bearer.'));

    if (bearerProtocol) {
      // Bearer.xxx.yyy.zzz → xxx.yyy.zzz
      return bearerProtocol.substring(7);
    }

    return null;
  }

  private rejectSocket(socket: Socket, statusCode: number, message: string) {
    try {
      socket.write(
        `HTTP/1.1 ${statusCode} ${message}\r\n` +
          'Connection: close\r\n' +
          '\r\n',
      );
    } finally {
      socket.destroy();
    }
  }

  private async handleConnection(ws: WebSocket, docId: string, userId: number) {
    this.logger.log(`Client connected to docId=${docId}, userId=${userId}`);

    // 1. Y.Doc 준비
    const ydoc = await this.ydocManager.getOrCreateDoc(docId);
    let docConn = this.docs.get(docId);
    if (!docConn) {
      docConn = { clients: new Set(), ydoc };
      this.docs.set(docId, docConn);
    }

    docConn.clients.add(ws);

    // 2. 접속한 클라이언트에게 현재 전체 상태 전송
    const fullState = Y.encodeStateAsUpdate(ydoc);
    ws.send(fullState);

    // 3. 메시지 수신 (Yjs update) → Y.Doc 적용 + 다른 클라이언트에게 브로드캐스트
    ws.on('message', (data) => {
      if (!docConn) return;

      let update: Uint8Array;
      if (Buffer.isBuffer(data)) {
        update = new Uint8Array(data);
      } else if (data instanceof ArrayBuffer) {
        update = new Uint8Array(data);
      } else {
        this.logger.warn(`Received non-binary message, ignoring`);
        return;
      }

      try {
        // 서버 상태에 적용
        Y.applyUpdate(docConn.ydoc, update);

        // 같은 문서의 다른 클라이언트들에게 전파
        for (const client of docConn.clients) {
          if (client !== ws && client.readyState === WebSocket.OPEN) {
            client.send(update);
          }
        }
      } catch (e) {
        this.logger.error(
          `Failed to apply update for docId=${docId}: ${(e as Error).message}`,
        );
      }
    });

    // 4. 연결 종료 처리
    ws.on('close', () => {
      void (async () => {
        this.logger.log(
          `Client disconnected from docId=${docId}, userId=${userId}`,
        );

        docConn?.clients.delete(ws);

        if (docConn && docConn.clients.size === 0) {
          // 마지막 유저가 나갔으면, 한번 더 저장 후 정리
          this.logger.log(`No clients left for docId=${docId}, releasing doc`);
          await this.ydocManager.releaseDoc(docId);
          this.docs.delete(docId);
        }
      })();
    });

    ws.on('error', (err) => {
      this.logger.warn(
        `WebSocket error for docId=${docId}, userId=${userId}: ${err.message}`,
      );
    });
  }
}
