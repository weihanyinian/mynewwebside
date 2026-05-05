# 基于 https://github.com/jsososo/QQMusicApi
FROM node:20-bookworm-slim
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends git ca-certificates \
  && rm -rf /var/lib/apt/lists/*
RUN git clone --depth 1 https://github.com/jsososo/QQMusicApi.git .
RUN npm install --omit=dev
ENV PORT=3300
EXPOSE 3300
CMD ["node", "./bin/www"]
