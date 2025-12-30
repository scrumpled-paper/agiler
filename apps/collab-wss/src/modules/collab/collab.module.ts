import { Module } from '@nestjs/common';
import { YdocManagerService } from './ydoc-manager.service';
import { CollabWsService } from './collab-ws.service';
import { AuthModule } from '../auth/auth.module';
import { ClientModule } from '../client/client.module';

@Module({
  imports: [AuthModule, ClientModule],
  providers: [YdocManagerService, CollabWsService],
  exports: [CollabWsService],
})
export class CollabModule {}
