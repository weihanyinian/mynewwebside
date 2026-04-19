#!/bin/bash
# MyWebSide Blog Backend - 2C2G 服务器启动脚本
# 使用方法：bash start.sh

APP_NAME="blog-backend"
JAR_FILE=$(ls -t target/*.jar 2>/dev/null | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ 未找到 JAR 文件，请先执行 mvn package"
    exit 1
fi

echo "📦 启动 $APP_NAME: $JAR_FILE"

# JVM 参数说明（2核2G 适配）：
# -Xms256m -Xmx512m  : 堆内存 256~512MB（给 MySQL/系统留 1.2G+）
# -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m : 元空间限制
# -XX:+UseG1GC        : G1 垃圾回收器，低延迟
# -XX:MaxGCPauseMillis=100 : GC 停顿目标 100ms
# -XX:+UseCompressedOops : 压缩对象指针，节省内存
# -XX:+UseCompressedClassPointers : 压缩类指针
# --add-opens         : Spring Boot 3 / JDK 17+ 模块访问

nohup java \
    -Xms256m \
    -Xmx512m \
    -XX:MetaspaceSize=128m \
    -XX:MaxMetaspaceSize=256m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseCompressedOops \
    -XX:+UseCompressedClassPointers \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
    -jar "$JAR_FILE" \
    --spring.profiles.active=prod \
    > /www/blog/backend/app.log 2>&1 &

PID=$!
echo "✅ $APP_NAME 已启动, PID: $PID"
echo "📋 查看日志: tail -f /www/blog/backend/app.log"
echo "🛑 停止服务: kill $PID"

# 保存 PID
echo $PID > /www/blog/backend/app.pid
