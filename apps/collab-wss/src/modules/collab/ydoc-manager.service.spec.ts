import { Test, TestingModule } from '@nestjs/testing';
import * as Y from 'yjs';
import { YdocManagerService } from './ydoc-manager.service';
import { SpringNoteClient } from '../client/spring-note/spring-note.client';

describe('YdocManagerService', () => {
  let service: YdocManagerService;
  let springNoteClient: jest.Mocked<SpringNoteClient>;

  beforeEach(async () => {
    jest.useFakeTimers();

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        YdocManagerService,
        {
          provide: SpringNoteClient,
          useValue: {
            loadNote: jest.fn(),
            saveContents: jest.fn(),
          },
        },
      ],
    }).compile();

    service = module.get<YdocManagerService>(YdocManagerService);
    springNoteClient = module.get(SpringNoteClient);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('getOrCreateDoc', () => {
    it('should create new Y.Doc and load contents from Spring', async () => {
      springNoteClient.loadNote.mockResolvedValue({
        contents: 'Hello World',
      });

      const ydoc = await service.getOrCreateDoc('retro-1');

      expect(ydoc).toBeInstanceOf(Y.Doc);
      expect(ydoc.getText('contents').toString()).toBe('Hello World');
      expect(springNoteClient.loadNote).toHaveBeenCalledWith('retro', 1);
    });

    it('should return existing Y.Doc if already created', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: 'test' });

      const ydoc1 = await service.getOrCreateDoc('retro-1');
      const ydoc2 = await service.getOrCreateDoc('retro-1');

      expect(ydoc1).toBe(ydoc2);
      expect(springNoteClient.loadNote).toHaveBeenCalledTimes(1);
    });

    it('should handle empty contents', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: '' });

      const ydoc = await service.getOrCreateDoc('retro-1');

      expect(ydoc.getText('contents').toString()).toBe('');
    });

    it('should handle null contents', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: null as any });

      const ydoc = await service.getOrCreateDoc('retro-1');

      expect(ydoc.getText('contents').toString()).toBe('');
    });

    it('should create different docs for different docIds', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: 'content' });

      const ydoc1 = await service.getOrCreateDoc('retro-1');
      const ydoc2 = await service.getOrCreateDoc('retro-2');

      expect(ydoc1).not.toBe(ydoc2);
      expect(springNoteClient.loadNote).toHaveBeenCalledTimes(2);
    });
  });

  describe('releaseDoc', () => {
    it('should save contents and remove doc from memory', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: 'test' });
      springNoteClient.saveContents.mockResolvedValue(undefined);

      const ydoc = await service.getOrCreateDoc('retro-1');
      ydoc.getText('contents').delete(0, 4);
      ydoc.getText('contents').insert(0, 'updated');

      await service.releaseDoc('retro-1');

      expect(springNoteClient.saveContents).toHaveBeenCalledWith(
        'retro',
        1,
        'updated',
      );

      // After release, getOrCreateDoc should create a new doc
      springNoteClient.loadNote.mockResolvedValue({ contents: 'fresh' });
      const newYdoc = await service.getOrCreateDoc('retro-1');
      expect(newYdoc).not.toBe(ydoc);
    });

    it('should do nothing if doc does not exist', async () => {
      await expect(
        service.releaseDoc('nonexistent-1'),
      ).resolves.toBeUndefined();

      expect(springNoteClient.saveContents).not.toHaveBeenCalled();
    });

    it('should clear pending autosave timer', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: 'test' });
      springNoteClient.saveContents.mockResolvedValue(undefined);

      const ydoc = await service.getOrCreateDoc('retro-1');

      // Trigger an update to start autosave timer
      ydoc.getText('contents').insert(0, 'x');

      // Release before timer fires
      await service.releaseDoc('retro-1');

      // Advance timers - autosave should not fire again
      jest.advanceTimersByTime(5000);

      // saveContents should only be called once (from releaseDoc)
      expect(springNoteClient.saveContents).toHaveBeenCalledTimes(1);
    });
  });

  describe('autosave', () => {
    it('should autosave after debounce delay on document update', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: '' });
      springNoteClient.saveContents.mockResolvedValue(undefined);

      const ydoc = await service.getOrCreateDoc('retro-1');

      // Make an update
      ydoc.getText('contents').insert(0, 'autosaved content');

      // Before debounce
      expect(springNoteClient.saveContents).not.toHaveBeenCalled();

      // After debounce (3000ms)
      jest.advanceTimersByTime(3000);
      await Promise.resolve(); // flush promises

      expect(springNoteClient.saveContents).toHaveBeenCalledWith(
        'retro',
        1,
        'autosaved content',
      );
    });

    it('should debounce multiple rapid updates', async () => {
      springNoteClient.loadNote.mockResolvedValue({ contents: '' });
      springNoteClient.saveContents.mockResolvedValue(undefined);

      const ydoc = await service.getOrCreateDoc('retro-1');

      // Multiple rapid updates
      ydoc.getText('contents').insert(0, 'a');
      jest.advanceTimersByTime(1000);
      ydoc.getText('contents').insert(1, 'b');
      jest.advanceTimersByTime(1000);
      ydoc.getText('contents').insert(2, 'c');

      // Wait for debounce from first update
      jest.advanceTimersByTime(1000);
      await Promise.resolve();

      // Should save only once with final content
      expect(springNoteClient.saveContents).toHaveBeenCalledTimes(1);
      expect(springNoteClient.saveContents).toHaveBeenCalledWith(
        'retro',
        1,
        'abc',
      );
    });
  });
});
