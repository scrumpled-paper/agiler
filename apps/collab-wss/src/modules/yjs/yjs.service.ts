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
                this.broadcastRaw(docId, update, origin as WebSocket);
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
            const decoder = decoding.createDecoder(uint8Msg);
            const type = decoding.readVarUint(decoder);

            if (type === 0) {
                // Sync: StateSet/Δ 추출
                const stateSet = decoding.hasContent(decoder);
                const update = decoding.readVarUint8Array(decoder);
                return `(${size}B) Sync ${stateSet ? '+' : ''}${update.length}BΔ`;
            } else if (type === 1) {
                // Awareness: JSON 파싱
                const jsonData = decoding.readVarUint8Array(decoder);
                const jsonStr = new TextDecoder().decode(jsonData);
                const json = JSON.parse(jsonStr);
                return `(${size}B) Awareness "${json.user?.name}"`;
            }
        } catch {}

        return `(${size}B) 바이너리`;
    }

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
                this.logger.debug(`📨 [${docId}] [${clientId}] 서버 수신 ${preview} type=${messageType}`);

                switch (messageType) {
                    case messageSync: {
                        const encoder = encoding.createEncoder();
                        encoding.writeVarUint(encoder, messageSync);
                        syncProtocol.readSyncMessage(decoder, encoder, doc, ws);

                        const reply = encoding.toUint8Array(encoder);
                        if (reply.length > 0) {
                            ws.send(reply);
                            this.logger.debug(`📤 [${docId}] Sync 응답 (${reply.length}B)`);
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
        if (connections && connections.size > 1) {
            // 순수 Yjs Update 형식
            const encoder = encoding.createEncoder();
            encoding.writeVarUint(encoder, messageSync);
            encoding.writeVarUint8Array(encoder, msg);
            const updateMsg = encoding.toUint8Array(encoder);

            connections.forEach((client: WebSocket) => {
                if (client.readyState === WebSocket.OPEN && client !== origin) {
                    try {
                        client.send(updateMsg);
                        this.logger.debug(`🌐 서버 To 클라이언트 CRDT 브로드캐스트 → [${docId}] 전송`);
                    } catch {
                        connections.delete(client);
                    }
                }
            });
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
