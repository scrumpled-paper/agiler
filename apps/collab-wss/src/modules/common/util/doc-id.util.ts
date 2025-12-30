export type DocType = 'retro' | 'scrum' | 'meeting';

export interface ParsedPath {
  projectUrl: string;
  type: DocType;
  id: number;
}

const VALID_DOC_TYPES: DocType[] = ['retro', 'scrum', 'meeting'];

/**
 * URL 경로를 파싱하여 projectUrl, docType, docId를 추출
 * 예: /collab/abc123/retro/1 → { projectUrl: 'abc123', type: 'retro', id: 1 }
 */
export function parsePath(urlPath: string): ParsedPath {
  // /collab/{projectUrl}/{docType}/{docId} 형태
  const match = urlPath.match(/^\/collab\/([^/]+)\/([^/]+)\/(\d+)$/);

  if (!match) {
    throw new Error(`Invalid path format: ${urlPath}`);
  }

  const [, projectUrl, docType, docIdStr] = match;
  const id = Number(docIdStr);

  if (!VALID_DOC_TYPES.includes(docType as DocType)) {
    throw new Error(`Invalid docType: ${docType}`);
  }

  if (Number.isNaN(id)) {
    throw new Error(`Invalid docId: ${docIdStr}`);
  }

  return {
    projectUrl,
    type: docType as DocType,
    id,
  };
}

/**
 * 내부적으로 사용하는 docKey 생성
 * 예: abc123:retro:1
 */
export function buildDocKey(
  projectUrl: string,
  type: DocType,
  id: number,
): string {
  return `${projectUrl}:${type}:${id}`;
}

/**
 * docKey를 파싱
 */
export function parseDocKey(docKey: string): ParsedPath {
  const [projectUrl, type, idStr] = docKey.split(':');
  const id = Number(idStr);

  if (
    !projectUrl ||
    !VALID_DOC_TYPES.includes(type as DocType) ||
    Number.isNaN(id)
  ) {
    throw new Error(`Invalid docKey: ${docKey}`);
  }

  return { projectUrl, type: type as DocType, id };
}
