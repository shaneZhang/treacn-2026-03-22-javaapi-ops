# 多阶段构建 Dockerfile
# 阶段1: 构建阶段
FROM eclipse-temurin:17-jdk-alpine AS builder

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache bash curl maven

# 先复制 Maven 配置文件以利用缓存
COPY pom.xml .

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B || true

# 复制源代码
COPY src src

# 构建应用（跳过测试，测试在 CI 中执行）
RUN mvn clean package -DskipTests -B && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# 阶段2: 生产运行阶段
FROM eclipse-temurin:17-jre-alpine AS production

# 创建非 root 用户运行应用
RUN addgroup -S charging && \
    adduser -S charging -G charging

# 安装必要的工具（用于健康检查）
RUN apk add --no-cache curl

# 设置工作目录
WORKDIR /app

# 从构建阶段复制依赖和类文件
COPY --from=builder /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=builder /app/target/dependency/META-INF /app/META-INF
COPY --from=builder /app/target/dependency/BOOT-INF/classes /app

# 创建数据目录并设置权限
RUN mkdir -p /app/data && \
    chown -R charging:charging /app

# 切换到非 root 用户
USER charging

# 暴露应用端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 环境变量配置
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app:/app/lib/* com.charging.management.ChargingManagementApplication"]

# 阶段3: 开发阶段（可选，用于本地开发）
FROM eclipse-temurin:17-jdk-alpine AS development

WORKDIR /app

# 安装开发工具
RUN apk add --no-cache bash curl git maven

# 复制 Maven 配置
COPY pom.xml .

# 复制源代码
COPY src src

# 开发模式使用 spring-boot:run
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]
