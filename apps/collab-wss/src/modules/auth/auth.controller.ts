// src/auth/auth.controller.ts
import { Controller, Post, Body, Req, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { AuthService } from './auth.service';

@Controller('api/v1/wss')
export class AuthController {
    private readonly logger = new Logger(AuthController.name);

    constructor(private readonly authService: AuthService) {}

    @Post('verify')
    async verifyAndPrepare(
        @Body() dto: { wssToken: string },
    ) {
        const result = await this.authService.verifyAndPrepareDoc(
            dto.wssToken,
        );

        return result;
    }
}
