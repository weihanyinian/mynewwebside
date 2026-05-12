import type * as monaco from 'monaco-editor'

export function defineMonacoThemes(m: typeof monaco) {
  m.editor.defineTheme('alibaba-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'keyword', foreground: 'ff7b72', fontStyle: 'bold' },
      { token: 'type', foreground: '79c0ff' },
      { token: 'string', foreground: 'a5d6ff' },
      { token: 'number', foreground: 'b2d8f7' },
      { token: 'comment', foreground: '8b949e', fontStyle: 'italic' },
      { token: 'function', foreground: 'd2a8ff' },
      { token: 'variable', foreground: 'c9d1d9' },
      { token: 'operator', foreground: 'ff7b72' },
      { token: 'preprocessor', foreground: 'f97583' },
    ],
    colors: {
      'editor.background': '#1e1e1e',
      'editor.foreground': '#c9d1d9',
      'editor.lineHighlightBackground': '#264f78',
      'editor.selectionBackground': '#264f78',
      'editorCursor.foreground': '#aeafad',
      'editorLineNumber.foreground': '#6e7681',
      'editorLineNumber.activeForeground': '#c9d1d9',
      'editorIndentGuide.background': '#2d2d2d',
      'editorIndentGuide.activeBackground': '#3d3d3d',
      'editor.selectionHighlightBackground': '#3a3d4166',
      'editorBracketMatch.background': '#006400',
      'editorBracketMatch.border': '#888888',
    },
  })

  m.editor.defineTheme('alibaba-light', {
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0000ff', fontStyle: 'bold' },
      { token: 'type', foreground: '267f99' },
      { token: 'string', foreground: 'a31515' },
      { token: 'number', foreground: '098658' },
      { token: 'comment', foreground: '008000', fontStyle: 'italic' },
      { token: 'function', foreground: '795e26' },
      { token: 'variable', foreground: '001080' },
      { token: 'operator', foreground: '000000' },
    ],
    colors: {
      'editor.background': '#ffffff',
      'editor.foreground': '1e1e1e',
      'editor.lineHighlightBackground': '#f7f7f7',
      'editor.selectionBackground': '#add6ff',
      'editorCursor.foreground': '#000000',
      'editorLineNumber.foreground': '#999999',
      'editorLineNumber.activeForeground': '#1e1e1e',
      'editorIndentGuide.background': '#d3d3d3',
      'editorIndentGuide.activeBackground': '#939393',
    },
  })
}
