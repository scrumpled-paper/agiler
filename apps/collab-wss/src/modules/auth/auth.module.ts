// src/auth/auth.module.ts
import { Module, forwardRef } from '@nestjs/common';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { YjsModule } from '../yjs/yjs.module';

@Module({
  imports: [
    forwardRef(() => YjsModule),
  ],
  controllers: [AuthController],
  providers: [AuthService],
  exports: [AuthService],
})
export class AuthModule {}
