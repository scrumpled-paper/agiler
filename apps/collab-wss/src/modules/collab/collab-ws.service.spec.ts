import { Test, TestingModule } from '@nestjs/testing';
import { Server as HttpServer, IncomingMessage } from 'http';
import { Socket } from 'net';
import * as Y from 'yjs';
import { CollabWsService } from './collab-ws.service';
import { YdocManagerService } from './ydoc-manager.service';
import { AuthService } from '../auth/auth.service';

describe('CollabWsService', () => {
  let service: CollabWsService;
  let authService: jest.Mocked<AuthService>;
  let ydocManager: jest.Mocked<YdocManagerService>;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        CollabWsService,
        {
          provide: AuthService,
          useValue: {
            verifyToken: jest.fn(),
            checkPermission: jest.fn(),
          },
        },
        {
          provide: YdocManagerService,
          useValue: {
            getOrCreateDoc: jest.fn(),
            releaseDoc: jest.fn(),
          },
        },
      ],
    }).compile();

    service = module.get<CollabWsService>(CollabWsService);
    authService = module.get(AuthService);
    ydocManager = module.get(YdocManagerService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('bind', () => {
    it('should register upgrade event handler on HTTP server', () => {
      const mockServer = {
        on: jest.fn(),
      } as unknown as HttpServer;

      service.bind(mockServer);

      expect(mockServer.on).toHaveBeenCalledWith(
        'upgrade',
        expect.any(Function),
      );
    });
  });

  describe('extractQuery (private method via upgrade handler)', () => {
    let mockServer: HttpServer;
    let upgradeHandler: (
      req: IncomingMessage,
      socket: Socket,
      head: Buffer,
    ) => void;

    beforeEach(() => {
      mockServer = {
        on: jest.fn((event, handler) => {
          if (event === 'upgrade') {
            upgradeHandler = handler;
          }
        }),
      } as unknown as HttpServer;

      service.bind(mockServer);
    });

    it('should reject connection without docId', async () => {
      const mockSocket = {
        write: jest.fn(),
        destroy: jest.fn(),
      } as unknown as Socket;

      const mockReq = {
        url: '/?token=valid-token',
      } as IncomingMessage;

      upgradeHandler(mockReq, mockSocket, Buffer.from([]));

      await new Promise((resolve) => setImmediate(resolve));

      expect(mockSocket.write).toHaveBeenCalledWith(
        expect.stringContaining('400'),
      );
      expect(mockSocket.destroy).toHaveBeenCalled();
    });

    it('should reject connection without token', async () => {
      const mockSocket = {
        write: jest.fn(),
        destroy: jest.fn(),
      } as unknown as Socket;

      const mockReq = {
        url: '/?docId=retro-1',
      } as IncomingMessage;

      upgradeHandler(mockReq, mockSocket, Buffer.from([]));

      await new Promise((resolve) => setImmediate(resolve));

      expect(mockSocket.write).toHaveBeenCalledWith(
        expect.stringContaining('400'),
      );
    });

    it('should reject connection with invalid token', async () => {
      authService.verifyToken.mockImplementation(() => {
        throw new Error('Invalid token');
      });

      const mockSocket = {
        write: jest.fn(),
        destroy: jest.fn(),
      } as unknown as Socket;

      const mockReq = {
        url: '/?docId=retro-1&token=invalid-token',
      } as IncomingMessage;

      upgradeHandler(mockReq, mockSocket, Buffer.from([]));

      await new Promise((resolve) => setImmediate(resolve));

      expect(mockSocket.write).toHaveBeenCalledWith(
        expect.stringContaining('401'),
      );
    });

    it('should reject connection when user has no permission', async () => {
      authService.verifyToken.mockReturnValue({ userId: 123 });
      authService.checkPermission.mockRejectedValue(new Error('No permission'));

      const mockSocket = {
        write: jest.fn(),
        destroy: jest.fn(),
      } as unknown as Socket;

      const mockReq = {
        url: '/?docId=retro-1&token=valid-token',
      } as IncomingMessage;

      upgradeHandler(mockReq, mockSocket, Buffer.from([]));

      await new Promise((resolve) => setImmediate(resolve));

      expect(authService.verifyToken).toHaveBeenCalledWith('valid-token');
      expect(authService.checkPermission).toHaveBeenCalledWith('retro-1', 123);
      expect(mockSocket.write).toHaveBeenCalledWith(
        expect.stringContaining('401'),
      );
    });
  });

  describe('handleConnection (integration-like test)', () => {
    it('should send full state to newly connected client', async () => {
      const ydoc = new Y.Doc();
      ydoc.getText('contents').insert(0, 'Initial content');

      ydocManager.getOrCreateDoc.mockResolvedValue(ydoc);

      // Access private method via reflection for testing
      const handleConnection = (service as any).handleConnection.bind(service);

      const mockWs = {
        send: jest.fn(),
        on: jest.fn(),
        readyState: 1, // WebSocket.OPEN
      };

      await handleConnection(mockWs, 'retro-1', 123);

      expect(ydocManager.getOrCreateDoc).toHaveBeenCalledWith('retro-1');
      expect(mockWs.send).toHaveBeenCalled();

      // Verify sent data is valid Yjs state
      const sentData = mockWs.send.mock.calls[0][0];
      expect(sentData).toBeInstanceOf(Uint8Array);
    });

    it('should register message, close, and error handlers', async () => {
      const ydoc = new Y.Doc();
      ydocManager.getOrCreateDoc.mockResolvedValue(ydoc);

      const handleConnection = (service as any).handleConnection.bind(service);

      type EventHandler = (...args: unknown[]) => void;
      const eventHandlers: Record<string, EventHandler> = {};
      const mockWs = {
        send: jest.fn(),
        on: jest.fn((event: string, handler: EventHandler) => {
          eventHandlers[event] = handler;
        }),
        readyState: 1,
      };

      await handleConnection(mockWs, 'retro-1', 123);

      expect(mockWs.on).toHaveBeenCalledWith('message', expect.any(Function));
      expect(mockWs.on).toHaveBeenCalledWith('close', expect.any(Function));
      expect(mockWs.on).toHaveBeenCalledWith('error', expect.any(Function));
    });

    it('should apply received Yjs update to server doc', async () => {
      const ydoc = new Y.Doc();
      ydocManager.getOrCreateDoc.mockResolvedValue(ydoc);

      const handleConnection = (service as any).handleConnection.bind(service);

      type EventHandler = (...args: unknown[]) => void;
      const eventHandlers: Record<string, EventHandler> = {};
      const mockWs = {
        send: jest.fn(),
        on: jest.fn((event: string, handler: EventHandler) => {
          eventHandlers[event] = handler;
        }),
        readyState: 1,
      };

      await handleConnection(mockWs, 'retro-1', 123);

      // Create an update from a client doc
      const clientDoc = new Y.Doc();
      clientDoc.getText('contents').insert(0, 'Client update');
      const update = Y.encodeStateAsUpdate(clientDoc);

      // Simulate receiving the update
      eventHandlers['message'](Buffer.from(update));

      // Verify server doc was updated
      expect(ydoc.getText('contents').toString()).toBe('Client update');
    });

    it('should release doc when last client disconnects', async () => {
      const ydoc = new Y.Doc();
      ydocManager.getOrCreateDoc.mockResolvedValue(ydoc);
      ydocManager.releaseDoc.mockResolvedValue(undefined);

      const handleConnection = (service as any).handleConnection.bind(service);

      type EventHandler = (...args: unknown[]) => void;
      const eventHandlers: Record<string, EventHandler> = {};
      const mockWs = {
        send: jest.fn(),
        on: jest.fn((event: string, handler: EventHandler) => {
          eventHandlers[event] = handler;
        }),
        readyState: 1,
      };

      await handleConnection(mockWs, 'retro-1', 123);

      // Simulate client disconnect
      eventHandlers['close']();

      await new Promise((resolve) => setImmediate(resolve));

      expect(ydocManager.releaseDoc).toHaveBeenCalledWith('retro-1');
    });

    it('should broadcast updates to other clients', async () => {
      const ydoc = new Y.Doc();
      ydocManager.getOrCreateDoc.mockResolvedValue(ydoc);

      const handleConnection = (service as any).handleConnection.bind(service);

      type EventHandler = (...args: unknown[]) => void;

      // Connect first client
      const eventHandlers1: Record<string, EventHandler> = {};
      const mockWs1 = {
        send: jest.fn(),
        on: jest.fn((event: string, handler: EventHandler) => {
          eventHandlers1[event] = handler;
        }),
        readyState: 1,
      };
      await handleConnection(mockWs1, 'retro-1', 123);

      // Connect second client
      const eventHandlers2: Record<string, EventHandler> = {};
      const mockWs2 = {
        send: jest.fn(),
        on: jest.fn((event: string, handler: EventHandler) => {
          eventHandlers2[event] = handler;
        }),
        readyState: 1,
      };
      await handleConnection(mockWs2, 'retro-1', 456);

      // Clear initial state sends
      mockWs1.send.mockClear();
      mockWs2.send.mockClear();

      // Client 1 sends an update
      const clientDoc = new Y.Doc();
      clientDoc.getText('contents').insert(0, 'Update from client 1');
      const update = Y.encodeStateAsUpdate(clientDoc);

      eventHandlers1['message'](Buffer.from(update));

      // Client 2 should receive the update, client 1 should not
      expect(mockWs2.send).toHaveBeenCalled();
      expect(mockWs1.send).not.toHaveBeenCalled();
    });
  });
});
