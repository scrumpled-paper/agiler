// src/yjs/yjs.service.ts (로그 액티비티 제거)
import { Injectable, Logger, HttpException, HttpStatus } from '@nestjs/common';
import { WebSocket } from 'ws';
import * as Y from 'yjs';
import * as syncProtocol from 'y-protocols/sync';
import * as awarenessProtocol from 'y-protocols/awareness';
import * as encoding from 'lib0/encoding';
import * as decoding from 'lib0/decoding';
import { DocumentService } from '../document/document.service';
import { Buffer } from 'buffer';
import { DocumentDto } from '../document/dto/document-response.dto';

const messageSync = 0;
const messageAwareness = 1;

@Injectable()
export class YjsService {
    private readonly logger = new Logger(YjsService.name);
    private docs = new Map<string, Y.Doc>();
    private connections = new Map<string, Set<WebSocket>>();
    private autoSaveTimers = new Map<string, ReturnType<typeof setTimeout>>();
    private awarenessMap = new Map<string, awarenessProtocol.Awareness>();

    constructor(private readonly documentService: DocumentService) {}

    async getOrCreateDoc(docId: string): Promise<Y.Doc> {
        let doc = this.docs.get(docId);

        if (!doc) {
            doc = new Y.Doc();
            const documentData = await this.documentService.fetchDocumentFromSpring(docId);
            this.loadDocumentData(doc, documentData);
            this.docs.set(docId, doc);

            const awareness = new awarenessProtocol.Awareness(doc);
            this.awarenessMap.set(docId, awareness);

            this.logger.log(`📄 새 Y.Doc 생성: ${docId}`);
        } else {
            this.logger.log(`📄 기존 Y.Doc 재사용: ${docId}`);  // ✅ 직접 출력
        }

        return doc;
    }

    private getAwareness(docId: string, doc: Y.Doc) {
        let awareness = this.awarenessMap.get(docId);
        if (!awareness) {
            awareness = new awarenessProtocol.Awareness(doc);
            this.awarenessMap.set(docId, awareness);
        }
        return awareness;
    }

    connectWebSocket(ws: WebSocket, docId: string, doc: Y.Doc) {
        const wsId = `ws-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
        (ws as any).id = wsId;

        if (!this.connections.has(docId)) {
            this.connections.set(docId, new Set());
        }
        this.connections.get(docId)!.add(ws);

        this.setupWebSocket(ws, doc, docId);

        if (this.connections.get(docId)!.size === 1) {
            this.setupAutoSave(docId, doc);
        }

        this.logger.log(`✅ [${wsId.slice(0,8)}] 소켓 연결: ${docId} (총 ${this.connections.get(docId)!.size}명)`);
    }

    async saveDocumentToSpring(docId: string): Promise<void> {
        const doc = this.docs.get(docId);
        if (!doc) {
            throw new HttpException(`문서를 찾을 수 없습니다: ${docId}`, HttpStatus.NOT_FOUND);
        }

        const yTitle = doc.getText('title');
        const yContents = doc.getText('contents');

        await this.documentService.saveDocumentToSpring(docId, yTitle.toString(), yContents.toString());
        this.logger.log(`💾 문서 저장 완료: ${docId}`);
    }

    private setupAutoSave(docId: string, doc: Y.Doc) {
        if (this.autoSaveTimers.has(docId)) {
            clearTimeout(this.autoSaveTimers.get(docId)!);
        }

        const timer = setTimeout(async () => {
            try {
                await this.saveDocumentToSpring(docId);
                this.logger.log(`💾 자동 저장 완료: ${docId}`);
            } catch (error: any) {
                this.logger.error(`❌ 자동 저장 실패: ${docId} - ${error.message}`);
            }
        }, 5 * 60 * 1000);

        this.autoSaveTimers.set(docId, timer);
        this.logger.log(`🔄 자동 저장 설정: ${docId} (5분)`);
    }

    private setupWebSocket(ws: WebSocket, doc: Y.Doc, docId: string) {
        const awareness = this.getAwareness(docId, doc);
        const wsId = (ws as any).id;

        ws.on('message', (message: Buffer) => {
            try {
                const uint8Message = new Uint8Array(message);
                const decoder = decoding.createDecoder(uint8Message);
                const messageType = decoding.readVarUint(decoder);

                const fullMsgHex = Array.from(uint8Message).map(b => b.toString(16).padStart(2,'0')).join(' ');
                const msgSummary = fullMsgHex.length > 100 ? fullMsgHex.slice(0, 100) + '...' : fullMsgHex;
                this.logger.log(`📨 수신 [${docId}] [${wsId.slice(0,8)}] (${message.length}B): ${msgSummary}`);

                switch (messageType) {
                    case messageSync: {
                        const encoder = encoding.createEncoder();
                        encoding.writeVarUint(encoder, messageSync);
                        syncProtocol.readSyncMessage(decoder, encoder, doc, ws);

                        const reply = encoding.toUint8Array(encoder);
                        if (reply.length > 1) {
                            const replyHex = Array.from(reply).map(b => b.toString(16).padStart(2,'0')).join(' ').slice(0, 100);
                            this.logger.log(`📤 응답 [${docId}] (${reply.length}B): ${replyHex}...`);

                            ws.send(reply);
                            this.broadcastRaw(docId, reply, ws);
                        }
                        this.logger.log(`📥 Sync 처리: ${docId}`);  // ✅ 직접 출력
                        break;
                    }

                    case messageAwareness: {
                        const update = decoding.readVarUint8Array(decoder);
                        const updateHex = Array.from(update).map(b => b.toString(16).padStart(2,'0')).join(' ').slice(0, 100);
                        this.logger.log(`👥 Awareness [${docId}] (${update.length}B): ${updateHex}...`);

                        awarenessProtocol.applyAwarenessUpdate(awareness, update, ws);

                        const encoder = encoding.createEncoder();
                        encoding.writeVarUint(encoder, messageAwareness);
                        encoding.writeVarUint8Array(encoder, update);
                        const msg = encoding.toUint8Array(encoder);

                        const bcHex = Array.from(msg).map(b => b.toString(16).padStart(2,'0')).join(' ').slice(0, 100);
                        this.logger.log(`📡 브로드캐스트 준비 [${docId}] (${msg.length}B): ${bcHex}...`);

                        this.broadcastRaw(docId, msg, ws);
                        break;
                    }

                    default: {
                        this.logger.warn(`⚠️ [${docId}] 알 수 없는 타입 ${messageType}: ${msgSummary}`);
                    }
                }
            } catch (error: any) {
                this.logger.error(`❌ [${docId}] 메시지 처리 실패: ${error.message}`);
            }
        });

        ws.on('close', () => {
            this.logger.log(`👋 [${wsId}] 종료: ${docId}`);
            const connections = this.connections.get(docId);
            if (connections) {
                connections.delete(ws);
                this.logger.log(`📊 [${docId}] 남은: ${connections.size}명`);

                if (connections.size === 0) {
                    this.saveDocumentOnLastUserLeave(docId);
                    this.connections.delete(docId);
                }
            }
        });

        ws.on('error', (error) => {
            this.logger.error(`❌ [${docId}] WebSocket 에러: ${error.message}`);
        });

        try {
            const encoder = encoding.createEncoder();
            encoding.writeVarUint(encoder, messageSync);
            syncProtocol.writeSyncStep1(encoder, doc);
            const syncMessage = encoding.toUint8Array(encoder);
            ws.send(syncMessage);
            this.logger.log(`📤 초기 SyncStep1: ${docId}`);  // ✅ 직접 출력
        } catch (error: any) {
            this.logger.error(`❌ [${docId}] 초기 sync 실패: ${error.message}`);
        }
    }

    private broadcastRaw(docId: string, msg: Uint8Array, sender: WebSocket) {
        const connections = this.connections.get(docId);
        if (!connections || connections.size <= 1) {
            this.logger.debug(`📭 브로드캐스트 생략: ${docId} (총 ${connections?.size || 0}명)`);
            return;
        }

        const fullMsgHex = Array.from(msg).map(b => b.toString(16).padStart(2,'0')).join(' ');
        const msgSummary = fullMsgHex.length > 150 ? fullMsgHex.slice(0, 150) + '...' : fullMsgHex;
        this.logger.log(`🌐 전송 메시지 [${docId}] (${msg.length}B): ${msgSummary}`);

        let sentCount = 0;
        let failedCount = 0;

        connections.forEach((client) => {
            if (client === sender || client.readyState !== WebSocket.OPEN) {
                return;
            }

            try {
                client.send(msg);
                sentCount++;
                const clientId = (client as any).id?.slice(0,8) || 'unknown';
                this.logger.log(`✅ 전송완료 [${docId}] -> [${clientId}] (${msg.length}B)`);
            } catch (error: any) {
                failedCount++;
                this.logger.error(`❌ 전송실패 [${docId}] -> [${(client as any).id?.slice(0,8)}]: ${error.message}`);
                connections.delete(client);
            }
        });

        this.logger.log(`📊 [${docId}] 브로드캐스트 결과: ${sentCount}성공 / ${failedCount}실패 (대상: ${connections.size-1})`);
    }

    private async saveDocumentOnLastUserLeave(docId: string) {
        try {
            if (this.autoSaveTimers.has(docId)) {
                clearTimeout(this.autoSaveTimers.get(docId)!);
                this.autoSaveTimers.delete(docId);
            }
            await this.saveDocumentToSpring(docId);
            this.logger.log(`💾 마지막 유저 퇴장 저장: ${docId}`);
        } catch (error: any) {
            this.logger.error(`❌ 퇴장 저장 실패: ${docId} - ${error.message}`);
        }
    }

    private loadDocumentData(doc: Y.Doc, documentData: DocumentDto) {
        if (!documentData) {
            this.logger.warn('⚠️ 초기 데이터 없음');
            return;
        }

        const yTitle = doc.getText('title');
        if (documentData.title && yTitle.length === 0) {
            yTitle.insert(0, documentData.title);
        }

        const yContents = doc.getText('contents');
        if (documentData.contents && yContents.length === 0) {
            yContents.insert(0, documentData.contents);
        }

        const yParticipants: Y.Array<Y.Map<any>> = doc.getArray('participants');
        if (documentData.participants && documentData.participants.length > 0 && yParticipants.length === 0) {
            documentData.participants.forEach((participant) => {
                const participantMap = new Y.Map();
                participantMap.set('profileId', participant.profileId);
                participantMap.set('nickname', participant.nickname);
                participantMap.set('imageUrl', participant.imageUrl);
                yParticipants.push([participantMap]);
            });
        }
    }

    cleanup() {
        this.autoSaveTimers.forEach((timer) => clearTimeout(timer));
        this.autoSaveTimers.clear();
        this.docs.clear();
        this.connections.clear();
        this.awarenessMap.clear();
        this.logger.log('🧹 Y.js 데이터 정리 완료');
    }
}
