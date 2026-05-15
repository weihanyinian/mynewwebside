# 基于 https://github.com/sansenjian/qq-music-api （文档 https://sansenjian.github.io/qq-music-api/api/ ）
FROM node:22-bookworm-slim
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends git ca-certificates \
  && rm -rf /var/lib/apt/lists/*
RUN git clone --depth 1 https://github.com/sansenjian/qq-music-api.git .
RUN npm install && npm run build && npm prune --omit=dev
ENV PORT=3200
EXPOSE 3200
CMD ["node", "dist/app.js"]
