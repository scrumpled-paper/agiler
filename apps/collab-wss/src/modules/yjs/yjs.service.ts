// src/yjs/yjs.service.ts (문자열 로깅 완전 버전)
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

            // 변경 감지 + 자동 저장
            doc.on('update', (update: Uint8Array, origin: any) => {
                this.triggerAutoSave(docId);
            });

            this.logger.log(`📄 새 Y.Doc 생성: ${docId}`);
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
        if (!this.connections.has(docId)) {
            this.connections.set(docId, new Set());
        }
        this.connections.get(docId)!.add(ws);

        const size = this.connections.get(docId)!.size;
        this.logger.log(`✅ 연결: ${docId} (${size}명)`);

        this.setupWebSocket(ws, doc, docId);
    }

    private triggerAutoSave(docId: string) {
        if (this.autoSaveTimers.has(docId)) {
            clearTimeout(this.autoSaveTimers.get(docId)!);
        }

        const timer = setTimeout(async () => {
            try {
                await this.saveDocumentToSpring(docId);
            } catch (error: any) {
                this.logger.error(`❌ 자동 저장 실패: ${docId} - ${error.message}`);
            }
        }, 60 * 1000 * 5); // 5분

        this.autoSaveTimers.set(docId, timer);
    }

    async saveDocumentToSpring(docId: string): Promise<void> {
        const doc = this.docs.get(docId);
        if (!doc) throw new HttpException(`문서 없음: ${docId}`, HttpStatus.NOT_FOUND);

        const yTitle = doc.getText('title');
        const yContents = doc.getXmlFragment('contents');

        let contentsString = '';
        yContents.forEach((node) => {
            if (node instanceof Y.XmlText) {
                contentsString += node.toString();
            }
        });

        await this.documentService.saveDocumentToSpring(docId, yTitle.toString(), contentsString);
        this.logger.log(`💾 저장완료: ${docId}`);
    }

    private getMessagePreview(uint8Msg: Uint8Array): string {
        const size = uint8Msg.length;
        try {
            const textDecoder = new TextDecoder('utf-8');
            const previewText = textDecoder.decode(uint8Msg.slice(0, 80));
            // 제어문자 제거 후 정리
            const cleanText = previewText.replace(/[\x00-\x1F\x7F-\x9F]/g, '').trim();
            if (cleanText.length > 0) {
                return `(${size}B) "${cleanText.slice(0, 40)}..."`;
            }
        } catch {}
        return `(${size}B) 바이너리`;
    }

    typescript
    private setupWebSocket(ws: WebSocket, doc: Y.Doc, docId: string) {
        const awareness = this.getAwareness(docId, doc);

        // ✅ IP:Port 로 간단 식별
        const clientId = `${(ws as any)._socket.remoteAddress}:${(ws as any)._socket.remotePort || '??'}`;

        ws.on('message', (message: Buffer) => {
            try {
                const uint8Message = new Uint8Array(message);
                const decoder = decoding.createDecoder(uint8Message);
                const messageType = decoding.readVarUint(decoder);

                const preview = this.getMessagePreview(uint8Message);
                this.logger.debug(`📨 [${docId}] [${clientId}] 수신 ${preview} type=${messageType}`);

                switch (messageType) {
                    case messageSync: {
                        const encoder = encoding.createEncoder();
                        encoding.writeVarUint(encoder, messageSync);
                        syncProtocol.readSyncMessage(decoder, encoder, doc, ws);

                        const reply = encoding.toUint8Array(encoder);
                        if (reply.length > 1) {
                            const replyPreview = this.getMessagePreview(reply);
                            this.logger.debug(`📤 [${docId}] [${clientId}] 응답 ${replyPreview}`);
                            ws.send(reply);
                            this.broadcastRaw(docId, reply, ws);
                        }
                        break;
                    }

                    case messageAwareness: {
                        const update = decoding.readVarUint8Array(decoder);
                        const updatePreview = this.getMessagePreview(update);
                        this.logger.debug(`👥 [${docId}] [${clientId}] Awareness ${updatePreview}`);

                        awarenessProtocol.applyAwarenessUpdate(awareness, update, ws);

                        const encoder = encoding.createEncoder();
                        encoding.writeVarUint(encoder, messageAwareness);
                        encoding.writeVarUint8Array(encoder, update);
                        const msg = encoding.toUint8Array(encoder);

                        this.broadcastRaw(docId, msg, ws);
                        break;
                    }

                    default:
                        this.logger.warn(`⚠️ [${docId}] 알 수 없는 타입: ${messageType}`);
                }
            } catch (error: any) {
                this.logger.error(`❌ [${docId}] 메시지 처리 실패: ${error.message}`);
            }
        });

        ws.on('close', () => {
            this.logger.log(`👋 [${clientId}] 종료: ${docId}`);
            const connections = this.connections.get(docId);
            if (connections) {
                connections.delete(ws);
                this.logger.log(`📊 [${docId}] 남은: ${connections.size}명`);

                if (connections.size === 0) {
                    this.saveDocumentOnLastUserLeave(docId);
                    this.connections.delete(docId);
                    this.docs.delete(docId);
                    this.awarenessMap.delete(docId);
                }
            }
        });

        ws.on('error', (error) => {
            this.logger.error(`❌ [${docId}] WebSocket 에러: ${error.message}`);
        });

        // 초기 SyncStep1
        try {
            const encoder = encoding.createEncoder();
            encoding.writeVarUint(encoder, messageSync);
            syncProtocol.writeSyncStep1(encoder, doc);
            const syncMessage = encoding.toUint8Array(encoder);
            const syncPreview = this.getMessagePreview(syncMessage);
            this.logger.debug(`📤 [${docId}] [${clientId}] 초기 Sync ${syncPreview}`);
            ws.send(syncMessage);
        } catch (error: any) {
            this.logger.error(`❌ [${docId}] 초기 sync 실패: ${error.message}`);
        }
    }

    private broadcastRaw(docId: string, msg: Uint8Array, sender: WebSocket) {
        const connections = this.connections.get(docId);
        if (!connections || connections.size <= 1) return;

        const preview = this.getMessagePreview(msg);
        this.logger.debug(`🌐 [${docId}] 브로드캐스트 ${preview}`);

        let sentCount = 0;
        let failedCount = 0;

        connections.forEach((client) => {
            if (client === sender || client.readyState !== WebSocket.OPEN) return;
            if (client === sender) return;
            const clientAddr = `${(client as any)._socket.remoteAddress}:${(client as any)._socket.remotePort}`;
            this.logger.debug(`✅ [${docId}] [${clientAddr}] 전송`);

            try {
                client.send(msg);
                const clientId = (client as any).id?.slice(0,8) || '??';
                this.logger.debug(`✅ [${docId}] [${clientId}] 전송완료`);
                sentCount++;
            } catch (error: any) {
                failedCount++;
                connections.delete(client);
            }
        });

        this.logger.log(`📊 [${docId}] 브로드캐스트: ${sentCount}성공/${failedCount}실패`);
    }

    private async saveDocumentOnLastUserLeave(docId: string) {
        try {
            if (this.autoSaveTimers.has(docId)) {
                clearTimeout(this.autoSaveTimers.get(docId)!);
            }
            await this.saveDocumentToSpring(docId);
            this.logger.log(`💾 퇴장 저장: ${docId}`);
        } catch (error: any) {
            this.logger.error(`❌ 퇴장 저장 실패: ${docId}`);
        }
    }

    private loadDocumentData(doc: Y.Doc, documentData: DocumentDto) {
        if (!documentData) return;

        const yTitle = doc.getText('title');
        if (documentData.title && yTitle.length === 0) {
            yTitle.insert(0, documentData.title);
        }

        const yContents = doc.getXmlFragment('contents');
        if (documentData.contents && yContents.length === 0) {
            const xmlText = new Y.XmlText(documentData.contents);
            yContents.insert(0, [xmlText]);
        }

        const yParticipants: Y.Array<Y.Map<any>> = doc.getArray('participants');
        if (documentData.participants?.length && yParticipants.length === 0) {
            documentData.participants.forEach((p) => {
                const map = new Y.Map();
                map.set('profileId', p.profileId);
                map.set('nickname', p.nickname);
                map.set('imageUrl', p.imageUrl);
                yParticipants.push([map]);
            });
        }
    }

    cleanup() {
        this.autoSaveTimers.forEach(clearTimeout);
        this.autoSaveTimers.clear();
        this.docs.clear();
        this.connections.clear();
        this.awarenessMap.clear();
        this.logger.log('🧹 정리완료');
    }
}
