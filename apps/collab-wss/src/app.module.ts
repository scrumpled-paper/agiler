import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { HealthModule } from './modules/health/health.module';
import { ClientModule } from './modules/client/client.module';
import { AuthModule } from './modules/auth/auth.module';
import { CollabAuth } from './modules/collab-auth/collab-auth';

@Module({
  imports: [HealthModule, ClientModule, AuthModule],
  controllers: [AppController],
  providers: [AppService, CollabAuth],
})
export class AppModule {}
