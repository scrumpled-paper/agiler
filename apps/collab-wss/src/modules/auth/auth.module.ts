import { Module } from '@nestjs/common';
import { AuthService } from './auth.service';
import { ConfigModule } from '@nestjs/config';
import { ClientModule } from '../client/client.module';

@Module({
  imports: [ConfigModule, ClientModule],
  providers: [AuthService],
  exports: [AuthService],
})
export class AuthModule {}
