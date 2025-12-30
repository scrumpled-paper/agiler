import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ConfigService } from "@nestjs/config";
import { DocumentBuilder, SwaggerModule } from "@nestjs/swagger";
import { Logger } from "@nestjs/common";

async function bootstrap() {
    const logger = new Logger('Bootstrap');

    const app = await NestFactory.create(AppModule, {
        logger: ['log', 'error', 'warn', 'debug', 'verbose'],
    });
  const configService = app.get<ConfigService>(ConfigService);
  const port = configService.get<number>('PORT') ?? 4000;

  const serverUrl = configService.get<string>('SERVER_URL') ?? `http://localhost:${port}`;

  const config = new DocumentBuilder()
      .setTitle('WSS Server API')
      .setDescription('Agiler WebSocket 서버 API (토큰 검증 및 문서 조회)')
      .setVersion('1.0')
      .addCookieAuth('accessToken', {
        type: 'apiKey',
        in: 'cookie',
        name: 'accessToken',
      })
      .addServer(serverUrl, 'WSS 서버')
      .build();
    const document = SwaggerModule.createDocument(app, config);

    SwaggerModule.setup('swagger-ui', app, document, {
        customSiteTitle: 'WSS Server API',
        swaggerOptions: {
            persistAuthorization: true,
        },
    });

  await app.listen(port);
}

bootstrap();
