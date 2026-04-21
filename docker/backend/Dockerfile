# =============================================
# 后端多阶段构建 Dockerfile
# =============================================
# 第一阶段：Maven 构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# =============================================
# 第二阶段：运行镜像
# =============================================
FROM eclipse-temurin:17-jre-alpine

# 安全：非 root 运行
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# 复制 JAR 文件（只取最后一个 build 阶段的）
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 安全：切换到非 root 用户
USER appuser

# 启动命令 - 默认生产环境配置
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:+HeapDumpOnOutOfMemoryError", \
           "-Xms256m", "-Xmx512m", \
           "-jar", "app.jar"]
