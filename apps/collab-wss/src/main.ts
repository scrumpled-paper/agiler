import { NestFactory } from '@nestjs/core';
import { NestExpressApplication } from '@nestjs/platform-express';
import type { Server } from 'http';
import { AppModule } from './app.module';
import { CollabWsService } from './modules/collab/collab-ws.service';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule);

  // HTTP 서버에 WebSocket 핸들러 바인딩
  const collabWsService = app.get(CollabWsService);
  const httpServer: Server = app.getHttpServer();
  collabWsService.bind(httpServer);

  await app.listen(process.env.PORT ?? 4000);
}
void bootstrap();
