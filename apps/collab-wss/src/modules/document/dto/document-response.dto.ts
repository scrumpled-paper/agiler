export interface ParticipantDto {
    profileId: number;
    nickname: string;
    imageUrl: string;
}

export interface MeetingDocumentDto {
    meetingId: number;
    title: string;
    contents: string;
    createdAt: string;
    participants: ParticipantDto[];
}

export interface RetroDocumentDto {
    retroId: number;
    title: string;
    contents: string;
    createdAt: string;
    participants: ParticipantDto[];
}

export type DocumentDto =
    | MeetingDocumentDto
    | RetroDocumentDto;
