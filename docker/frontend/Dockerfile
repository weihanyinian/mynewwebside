# =============================================
# 前端 Nginx 部署 Dockerfile
# =============================================
# 阶段一：Node 构建
FROM node:20-alpine AS builder

WORKDIR /app

# 复制 package.json
COPY frontend/package*.json ./

# 安装依赖
RUN npm ci --prefer-offline

# 复制源代码
COPY frontend/ ./

# 构建生产版本
RUN npm run build

# =============================================
# 阶段二：Nginx 运行
# =============================================
FROM nginx:1.25-alpine

# 删除默认站点配置
RUN rm -rf /usr/share/nginx/html/*

# 从构建阶段复制产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 Nginx 配置
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
