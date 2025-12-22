import MDEditor from '@uiw/react-md-editor'

type TemplateContentEditorProps = {
  value: string
  onChange: (value: string) => void
}

export function TemplateContentEditor({
  value,
  onChange,
}: TemplateContentEditorProps) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium text-black">Template content</label>
      {/* <Textarea
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder="Enter markdown content..."
        className="min-h-[368px] w-full resize-none rounded-md border border-[rgba(0,0,0,0.1)] bg-white px-3 py-2 text-sm text-[rgba(0,0,0,0.5)] shadow-sm"
      /> */}
      <div data-color-mode="light">
        <MDEditor
          value={value}
          onChange={val => onChange(val || '')}
          preview="edit"
          height={400}
        />
      </div>
    </div>
  )
}
