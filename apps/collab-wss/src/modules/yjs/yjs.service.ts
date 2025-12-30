// src/yjs/yjs.service.ts
import { Injectable, Logger, HttpException, HttpStatus } from '@nestjs/common';
import { WebSocket } from 'ws';
import * as Y from 'yjs';
import * as syncProtocol from 'y-protocols/sync';
import * as encoding from 'lib0/encoding';
import * as decoding from 'lib0/decoding';
import { DocumentService } from '../document/document.service';
import { Buffer } from 'buffer';
import { DocumentDto } from "../document/dto/document-response.dto";

@Injectable()
export class YjsService {
    private readonly logger = new Logger(YjsService.name);
    private docs = new Map<string, Y.Doc>();
    private connections = new Map<string, Set<WebSocket>>();
    private autoSaveTimers = new Map<string, NodeJS.Timeout>();

    constructor(
        private readonly documentService: DocumentService,
    ) {}

    async getOrCreateDoc(docId: string): Promise<Y.Doc> {
        let doc = this.docs.get(docId);

        if (!doc) {
            doc = new Y.Doc();

            // Spring에서 문서 조회
            const documentData = await this.documentService.fetchDocumentFromSpring(docId);

            this.loadDocumentData(doc, documentData);
            this.docs.set(docId, doc);
            this.logger.log(`📄 새 Y.Doc 생성: ${docId}`);
        } else {
            this.logger.log(`📄 기존 Y.Doc 재사용: ${docId}`);
        }

        return doc;
    }

    connectWebSocket(ws: WebSocket, docId: string, doc: Y.Doc) {
        if (!this.connections.has(docId)) {
            this.connections.set(docId, new Set());
        }
        this.connections.get(docId)!.add(ws);

        this.setupWebSocket(ws, doc, docId);

        // ✅ 첫 연결 시 자동 저장 활성화
        if (this.connections.get(docId)!.size === 1) {
            this.setupAutoSave(docId, doc);
        }

        this.logger.log(`✅ 소켓 연결 완료: ${docId}`);
        this.logger.log(`📊 접속자: ${this.connections.get(docId)!.size}명`);
    }

    // ✅ Spring으로 문서 저장
    async saveDocumentToSpring(docId: string): Promise<void> {
        const doc = this.docs.get(docId);
        if (!doc) {
            throw new HttpException(
                `문서를 찾을 수 없습니다: ${docId}`,
                HttpStatus.NOT_FOUND,
            );
        }

        // Y.Doc에서 데이터 추출
        const yTitle = doc.getText('title');
        const yContents = doc.getText('contents');

        const title = yTitle.toString();
        const contents = yContents.toString();

        // DocumentService를 통해 Spring으로 저장
        await this.documentService.saveDocumentToSpring(docId, title, contents);

        this.logger.log(`💾 문서 저장 완료: ${docId}`);
    }

    // ✅ 자동 저장 설정 (4초 debounce)
    private setupAutoSave(docId: string, doc: Y.Doc) {
        doc.on('update', () => {
            // 기존 타이머 제거
            if (this.autoSaveTimers.has(docId)) {
                clearTimeout(this.autoSaveTimers.get(docId)!);
            }

            // 4초 후 자동 저장
            const timer = setTimeout(async () => {
                try {
                    await this.saveDocumentToSpring(docId);
                    this.logger.log(`💾 자동 저장 완료: ${docId}`);
                } catch (error: any) {
                    this.logger.error(`❌ 자동 저장 실패: ${docId} - ${error.message}`);
                }
            }, 4000);

            this.autoSaveTimers.set(docId, timer);
        });

        this.logger.log(`🔄 자동 저장 활성화: ${docId}`);
    }

    private setupWebSocket(ws: WebSocket, doc: Y.Doc, docId: string) {
        ws.on('message', (message: Buffer) => {
            try {
                const uint8Message = new Uint8Array(message);
                const decoder = decoding.createDecoder(uint8Message);
                const encoder = encoding.createEncoder();
                const messageType = decoding.readVarUint(decoder);

                switch (messageType) {
                    case syncProtocol.messageYjsSyncStep1: {
                        encoding.writeVarUint(encoder, syncProtocol.messageYjsSyncStep2);
                        const update = Y.encodeStateAsUpdate(doc);
                        syncProtocol.writeUpdate(encoder, update);
                        const response = encoding.toUint8Array(encoder);
                        ws.send(response);
                        this.logger.log(`📤 SyncStep2 전송: ${docId}`);
                        break;
                    }

                    case syncProtocol.messageYjsSyncStep2: {
                        syncProtocol.readUpdate(decoder, doc, null);
                        this.logger.log(`📥 SyncStep2 수신: ${docId}`);
                        break;
                    }

                    case syncProtocol.messageYjsUpdate: {
                        syncProtocol.readUpdate(decoder, doc, null);
                        this.broadcast(docId, message, ws);
                        this.logger.log(`📝 업데이트 수신 및 브로드캐스트: ${docId}`);
                        break;
                    }
                }
            } catch (error: any) {
                this.logger.error(`❌ 메시지 처리 실패: ${error.message}`);
            }
        });

        ws.on('close', () => {
            this.logger.log(`👋 연결 종료: ${docId}`);
            const connections = this.connections.get(docId);
            if (connections) {
                connections.delete(ws);
                this.logger.log(`📊 남은 접속자: ${connections.size}명`);
                if (connections.size === 0) {
                    this.connections.delete(docId);
                    // ✅ 마지막 사용자 나가면 자동 저장 타이머 제거
                    if (this.autoSaveTimers.has(docId)) {
                        clearTimeout(this.autoSaveTimers.get(docId)!);
                        this.autoSaveTimers.delete(docId);
                        this.logger.log(`🛑 자동 저장 비활성화: ${docId}`);
                    }
                }
            }
        });

        ws.on('error', (error) => {
            this.logger.error(`❌ WebSocket 에러: ${error.message}`);
        });

        // 초기 sync
        try {
            const encoder = encoding.createEncoder();
            encoding.writeVarUint(encoder, syncProtocol.messageYjsSyncStep1);
            syncProtocol.writeSyncStep1(encoder, doc);
            const syncMessage = encoding.toUint8Array(encoder);
            ws.send(syncMessage);
            this.logger.log(`📤 초기 SyncStep1 전송: ${docId}`);
        } catch (error: any) {
            this.logger.error(`❌ 초기 sync 실패: ${error.message}`);
        }
    }

    private broadcast(docId: string, message: Buffer, sender: WebSocket) {
        const connections = this.connections.get(docId);
        if (!connections) return;

        let sentCount = 0;
        connections.forEach((client) => {
            if (client !== sender && client.readyState === WebSocket.OPEN) {
                try {
                    client.send(message);
                    sentCount++;
                } catch (error: any) {
                    this.logger.error(`❌ 브로드캐스트 실패: ${error.message}`);
                }
            }
        });

        if (sentCount > 0) {
            this.logger.log(`📡 브로드캐스트: ${sentCount}명에게 전송`);
        }
    }

    private loadDocumentData(doc: Y.Doc, documentData: DocumentDto) {
        if (!documentData) {
            this.logger.warn('⚠️ 초기 데이터 없음');
            return;
        }

        // Title 로드
        const yTitle = doc.getText('title');
        if (documentData.title && yTitle.length === 0) {
            yTitle.insert(0, documentData.title);
            this.logger.log(`✅ 제목 로드: ${documentData.title}`);
        }

        // Contents 로드
        const yContents = doc.getText('contents');
        if (documentData.contents && yContents.length === 0) {
            yContents.insert(0, documentData.contents);
            this.logger.log(`✅ 내용 로드: ${documentData.contents.length}자`);
        }

        // Participants 로드 (Y.Map으로 변환)
        type ParticipantValue = string | number;
        const yParticipants: Y.Array<Y.Map<ParticipantValue>> = doc.getArray('participants');

        if (documentData.participants && documentData.participants.length > 0 && yParticipants.length === 0) {
            documentData.participants.forEach((participant) => {
                const participantMap = new Y.Map<ParticipantValue>();
                participantMap.set('profileId', participant.profileId);
                participantMap.set('nickname', participant.nickname);
                participantMap.set('imageUrl', participant.imageUrl);

                yParticipants.push([participantMap]);
            });
            }
    }

    cleanup() {
        // 자동 저장 타이머 모두 제거
        this.autoSaveTimers.forEach(timer => clearTimeout(timer));
        this.autoSaveTimers.clear();

        this.docs.clear();
        this.connections.clear();
        this.logger.log('🧹 Y.js 데이터 정리 완료');
    }
}
