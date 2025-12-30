import { DocType } from '../../../common/util/doc-id.util';

export interface NoteData {
  id: number;
  type: DocType;
  title: string;
  contents: string;
}
