# syntax=docker/dockerfile:1
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml ./backend/
RUN mvn -f backend/pom.xml dependency:go-offline -B
COPY backend/src ./backend/src
COPY backend/pom.xml ./backend/
# 镜像构建不依赖被 gitignore 的本地 application.yml，统一以 example 为基底（再由 application-docker.yml 覆盖容器环境）
RUN cp backend/src/main/resources/application.example.yml backend/src/main/resources/application.yml
RUN mvn -f backend/pom.xml package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/backend/target/blog-backend-*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -sf http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
