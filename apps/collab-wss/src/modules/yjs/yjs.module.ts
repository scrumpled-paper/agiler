// src/yjs/yjs.module.ts
import { Module, forwardRef } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { YjsGateway } from './yjs.gateway';
import { YjsService } from './yjs.service';
import { AuthModule } from '../auth/auth.module';
import { DocumentService } from '../document/document.service';

@Module({
    imports: [
        HttpModule,
        forwardRef(() => AuthModule),
    ],
    providers: [
        YjsGateway,
        YjsService,
        DocumentService,
    ],
    exports: [YjsService],
})
export class YjsModule {}
