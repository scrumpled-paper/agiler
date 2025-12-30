import { Module } from '@nestjs/common';
import { SpringNoteClient } from './spring-note/spring-note.client';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { HttpModule } from '@nestjs/axios';

@Module({
  imports: [
    ConfigModule,
    HttpModule.registerAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        baseURL: config.get<string>('SPRING_BASE_URL'),
        timeout: 3000,
      }),
    }),
  ],
  providers: [SpringNoteClient],
  exports: [SpringNoteClient],
})
export class ClientModule {}
