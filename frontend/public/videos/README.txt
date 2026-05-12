首页背景视频（Git 一般不提交大 mp4，需自行拷贝到此目录）

1) 初音.mp4                    → 日间（浅色主题）
2) livetune feat 初音ミク「Redial」Music Video_final_ver.mp4  → 夜间（深色主题）

文件名必须与上面完全一致（含空格、日文、直角引号「」、.mp4）。
改片名时只改 frontend/src/config/homeBackgroundVideos.ts 里两个常量即可。

若你本地仍用 light.mp4 / dark.mp4 作为母片，在 frontend 目录执行：
  node scripts/copy-home-bg-videos.mjs
即可按上述两个文件名各复制一份（已执行过则覆盖）。
