ALTER TABLE portfolio_work
  ADD COLUMN content_md LONGTEXT NULL,
  ADD COLUMN demo_url VARCHAR(700) NULL,
  ADD COLUMN repo_url VARCHAR(700) NULL,
  ADD COLUMN tech_stack VARCHAR(500) NULL;

UPDATE portfolio_work
SET content_md = CASE
  WHEN content_md IS NULL OR TRIM(content_md) = '' THEN CONCAT(
    '# ', title, '\n\n',
    detail, '\n\n',
    '## 项目价值\n',
    '- 可复用的工程实践\n',
    '- 可展示的技术能力\n',
    '- 适合持续迭代的作品结构\n'
  )
  ELSE content_md
END;

UPDATE portfolio_work
SET
  cover_url = 'https://c4.wallpaperflare.com/wallpaper/957/640/962/anime-girls-red-eyes-white-hair-wallpaper-preview.jpg',
  tech_stack = 'GLM4, LoRA, PyTorch, FastAPI, Docker',
  repo_url = 'https://github.com/weihanyinian/mynewwebside',
  demo_url = NULL
WHERE title = '大语言模型微调与部署';

UPDATE portfolio_work
SET
  cover_url = 'https://c4.wallpaperflare.com/wallpaper/376/70/446/anime-girls-original-characters-white-hair-wallpaper-preview.jpg',
  tech_stack = 'Python, Transformer, Attention, BLEU, Jupyter',
  repo_url = 'https://github.com/weihanyinian/mynewwebside',
  demo_url = NULL
WHERE title = 'Transformer 机器翻译';

UPDATE portfolio_work
SET
  cover_url = 'https://c4.wallpaperflare.com/wallpaper/848/764/1022/anime-girls-arknights-wallpaper-preview.jpg',
  tech_stack = 'Spring Boot 3, Vue 3, MySQL, Redis, Docker',
  repo_url = 'https://github.com/weihanyinian/mynewwebside',
  demo_url = 'https://weihanyinian.cn'
WHERE title = 'MyWebSide Blog';

UPDATE portfolio_work
SET
  cover_url = 'https://c4.wallpaperflare.com/wallpaper/365/465/159/anime-girls-computer-keyboard-wallpaper-preview.jpg',
  tech_stack = 'Judge0, Java, Vue, Sandboxed Execution',
  repo_url = 'https://github.com/weihanyinian/mynewwebside',
  demo_url = '/tools/oj'
WHERE title = '在线判题 OJ';
