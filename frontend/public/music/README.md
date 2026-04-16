# 在线音乐目录 `public/music`

## 如何添加歌曲

1. 将 `.mp3`（或其他浏览器支持的音频格式）文件复制到本目录，例如：`public/music/my-song.mp3`。
2. 编辑本目录下的 `playlist.json`，按数组格式追加一项：

```json
{
  "title": "显示在播放器上的歌名",
  "url": "/music/my-song.mp3"
}
```

注意：`url` 必须以 `/music/` 开头，对应 `public/music/` 下的文件（Vite 会原样映射到网站根路径）。

3. 保存后刷新页面即可；若 `playlist.json` 为空或请求失败，播放器会回退使用 `src/assets/playlist.json`（原 BGM 列表）。

## 仅使用本目录歌单

把有效的 `playlist.json` 放在此目录并保证至少一首歌曲文件存在即可；可删除示例条目，换成你自己的 `title` 与 `url`。
