import { IsString, IsNotEmpty } from 'class-validator';
import {ApiProperty} from "@nestjs/swagger";

export class WssTokenReqDto {
    @ApiProperty({
        description: 'Spring에서 발급한 WSS 전용 토큰 (JWT)',
        example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZG9jSWQiOiJzY3J1bXM6MSIsImlhdCI6MTYzNTQ4MzIwMCwiZXhwIjoxNjM1NDg2ODAwfQ.signature',
        required: true,
    })
    @IsString()
    @IsNotEmpty()
    wssToken: string;
}
