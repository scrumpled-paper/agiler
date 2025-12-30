import {
  parsePath,
  buildDocKey,
  parseDocKey,
  DocType,
  ParsedPath,
} from './doc-id.util';

describe('parsePath', () => {
  describe('valid paths', () => {
    it('should parse retro path correctly', () => {
      const result = parsePath('/collab/abc123/retro/1');

      expect(result).toEqual<ParsedPath>({
        projectId: 'abc123',
        type: 'retro',
        id: 1,
      });
    });

    it('should parse scrum path correctly', () => {
      const result = parsePath('/collab/project-xyz/scrum/42');

      expect(result).toEqual<ParsedPath>({
        projectId: 'project-xyz',
        type: 'scrum',
        id: 42,
      });
    });

    it('should parse meeting path correctly', () => {
      const result = parsePath('/collab/my_project/meeting/100');

      expect(result).toEqual<ParsedPath>({
        projectId: 'my_project',
        type: 'meeting',
        id: 100,
      });
    });

    it('should handle UUID-style projectId', () => {
      const result = parsePath(
        '/collab/550e8400-e29b-41d4-a716-446655440000/retro/5',
      );

      expect(result).toEqual<ParsedPath>({
        projectId: '550e8400-e29b-41d4-a716-446655440000',
        type: 'retro',
        id: 5,
      });
    });
  });

  describe('invalid paths', () => {
    it('should throw error for missing /collab prefix', () => {
      expect(() => parsePath('/project/abc123/retro/1')).toThrow(
        'Invalid path format',
      );
    });

    it('should throw error for invalid docType', () => {
      expect(() => parsePath('/collab/abc123/invalid/1')).toThrow(
        'Invalid docType: invalid',
      );
    });

    it('should throw error for non-numeric docId', () => {
      expect(() => parsePath('/collab/abc123/retro/abc')).toThrow(
        'Invalid path format',
      );
    });

    it('should throw error for empty path', () => {
      expect(() => parsePath('')).toThrow('Invalid path format');
    });

    it('should throw error for missing docId', () => {
      expect(() => parsePath('/collab/abc123/retro')).toThrow(
        'Invalid path format',
      );
    });

    it('should throw error for extra segments', () => {
      expect(() => parsePath('/collab/abc123/retro/1/extra')).toThrow(
        'Invalid path format',
      );
    });
  });
});

describe('buildDocKey', () => {
  it('should build docKey from components', () => {
    expect(buildDocKey('abc123', 'retro', 1)).toBe('abc123:retro:1');
  });

  it('should handle different docTypes', () => {
    expect(buildDocKey('proj', 'scrum', 5)).toBe('proj:scrum:5');
    expect(buildDocKey('proj', 'meeting', 10)).toBe('proj:meeting:10');
  });
});

describe('parseDocKey', () => {
  it('should parse valid docKey', () => {
    const result = parseDocKey('abc123:retro:1');

    expect(result).toEqual<ParsedPath>({
      projectId: 'abc123',
      type: 'retro',
      id: 1,
    });
  });

  it('should parse docKey with different types', () => {
    expect(parseDocKey('proj:scrum:5')).toEqual({
      projectId: 'proj',
      type: 'scrum',
      id: 5,
    });

    expect(parseDocKey('proj:meeting:10')).toEqual({
      projectId: 'proj',
      type: 'meeting',
      id: 10,
    });
  });

  it('should throw error for invalid docKey format', () => {
    expect(() => parseDocKey('invalid')).toThrow('Invalid docKey');
  });

  it('should throw error for invalid docType in docKey', () => {
    expect(() => parseDocKey('proj:invalid:1')).toThrow('Invalid docKey');
  });

  it('should throw error for non-numeric id', () => {
    expect(() => parseDocKey('proj:retro:abc')).toThrow('Invalid docKey');
  });
});

describe('DocType type', () => {
  it('should only allow valid DocType values', () => {
    const validTypes: DocType[] = ['retro', 'scrum', 'meeting'];

    validTypes.forEach((type) => {
      expect(() => parsePath(`/collab/proj/${type}/1`)).not.toThrow();
    });
  });
});