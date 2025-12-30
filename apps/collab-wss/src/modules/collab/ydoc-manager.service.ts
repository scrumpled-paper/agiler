import { Injectable, Logger } from '@nestjs/common';
import { SpringNoteClient } from '../client/spring-note/spring-note.client';
import { parseDocKey } from '../common/util/doc-id.util';
import * as Y from 'yjs';

const AUTOSAVE_DEBOUNCE_MS = 3000;

interface DocEntry {
  ydoc: Y.Doc;
}

@Injectable()
export class YdocManagerService {
  private readonly logger = new Logger(YdocManagerService.name);

  private readonly docs = new Map<string, DocEntry>();
  private readonly timers = new Map<string, NodeJS.Timeout>();

  constructor(private readonly springNoteClient: SpringNoteClient) {}

  /**
   * docKey("projectUrl:retro:1" 등)에 대응하는 Y.Doc을 가져오거나 새로 만든다.
   * 새로 만들 경우 Spring에서 contents를 로드하여 초기화한다.
   */
  async getOrCreateDoc(docKey: string): Promise<Y.Doc> {
    const existing = this.docs.get(docKey);
    if (existing) {
      return existing.ydoc;
    }

    const { projectUrl, type, id } = parseDocKey(docKey);

    // 1. Spring에서 현재 contents 로드
    const note = await this.springNoteClient.loadNote(projectUrl, type, id);

    // 2. Y.Doc 생성 및 초기화
    const ydoc = new Y.Doc();
    const yText = ydoc.getText('contents');
    if (note.contents) {
      yText.insert(0, note.contents);
    }

    // 3. autosave 이벤트 설정
    this.attachAutosave(docKey, ydoc);

    this.docs.set(docKey, { ydoc });
    this.logger.log(`Created Y.Doc for docKey=${docKey}`);

    return ydoc;
  }

  /**
   * 마지막 유저가 나갈 때 등, 문서 수명이 끝났을 때 호출.
   * 한번 더 저장 시도 후 메모리에서 제거.
   */
  async releaseDoc(docKey: string): Promise<void> {
    const entry = this.docs.get(docKey);
    if (!entry) return;

    await this.forceSave(docKey, entry.ydoc);
    this.docs.delete(docKey);

    const timer = this.timers.get(docKey);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(docKey);
    }

    this.logger.log(`Released Y.Doc for docKey=${docKey}`);
  }

  private attachAutosave(docKey: string, ydoc: Y.Doc) {
    const yText = ydoc.getText('contents');

    ydoc.on('update', () => {
      // 디바운스: 이미 타이머 있으면 새로 안 잡음
      if (this.timers.has(docKey)) return;

      const timer = setTimeout(() => {
        void (async () => {
          this.timers.delete(docKey);
          try {
            // eslint-disable-next-line @typescript-eslint/no-base-to-string
            await this.save(docKey, yText.toString());
          } catch (e) {
            this.logger.error(
              `Autosave failed for docKey=${docKey}: ${(e as Error).message}`,
            );
          }
        })();
      }, AUTOSAVE_DEBOUNCE_MS);

      this.timers.set(docKey, timer);
    });
  }

  private async save(docKey: string, contents: string): Promise<void> {
    const { projectUrl, type, id } = parseDocKey(docKey);
    this.logger.debug(`Autosaving docKey=${docKey}`);

    await this.springNoteClient.saveContents(projectUrl, type, id, contents);
  }

  private async forceSave(docKey: string, ydoc: Y.Doc): Promise<void> {
    const yText = ydoc.getText('contents');
    // eslint-disable-next-line @typescript-eslint/no-base-to-string
    const contents = yText.toString();
    await this.save(docKey, contents);
  }
}
