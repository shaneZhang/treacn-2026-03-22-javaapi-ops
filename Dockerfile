# 第一阶段：构建阶段
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# 安装Maven
RUN apk add --no-cache maven

# 复制pom.xml并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -DskipTests

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非root用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 设置时区
RUN apk add --no-cache tzdata
ENV TZ=Asia/Shanghai

# 安装curl用于健康检查
RUN apk add --no-cache curl

# 从构建阶段复制jar文件
COPY --from=builder /app/target/*.jar app.jar

# 设置文件权限
RUN chown appuser:appgroup app.jar

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM优化参数
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
