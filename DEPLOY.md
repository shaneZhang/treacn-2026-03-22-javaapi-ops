# CI/CD 配置说明文档

## 1. GitHub Actions CI/CD 配置

### 文件位置
`.github/workflows/ci-cd.yml`

### 触发条件
- **Push 事件**: main、develop、feature/**、release/** 分支
- **Pull Request 事件**: main、develop 分支
- **Tag 事件**: v* 标签（用于发布）

### 工作流任务

#### 1.1 代码质量检查 (code-quality)
- 使用 Checkstyle 进行代码规范检查
- 使用 SpotBugs 进行静态代码分析

#### 1.2 编译和测试 (build-and-test)
- **矩阵构建**: Java 17 和 Java 21
- 编译项目
- 运行单元测试
- 生成测试报告
- 生成 JaCoCo 代码覆盖率报告
- 上传覆盖率到 Codecov

#### 1.3 构建产物 (build-artifact)
- 构建 JAR 包
- 上传构建产物
- 更新依赖图谱

#### 1.4 Docker 镜像构建 (build-docker)
- 多平台构建 (linux/amd64, linux/arm64)
- 推送到 GitHub Container Registry (ghcr.io)
- 使用 Buildx 缓存优化

#### 1.5 安全扫描 (security-scan)
- 使用 Trivy 扫描镜像漏洞
- 上传扫描结果到 GitHub Security

#### 1.6 发布 (release)
- 基于标签创建 Release
- 上传 JAR 包到 Release 资产

---

## 2. Dockerfile 多阶段构建

### 文件位置
`Dockerfile`

### 构建阶段

#### Stage 1: Builder
- 基础镜像: `eclipse-temurin:17-jdk-alpine`
- 安装 Maven
- 下载依赖
- 编译打包

#### Stage 2: Production
- 基础镜像: `eclipse-temurin:17-jre-alpine`
- 创建非 root 用户 (charging)
- 复制构建产物
- 配置健康检查
- 使用非 root 用户运行

#### Stage 3: Development (可选)
- 用于本地开发
- 支持热重载

### 安全特性
- 非 root 用户运行
- 最小化基础镜像 (Alpine)
- 仅包含 JRE（生产环境）
- 健康检查机制

---

## 3. Docker Compose 配置

### 文件位置
- `docker-compose.yml` - 生产配置
- `docker-compose.override.yml` - 开发覆盖配置

### 使用方式

#### 生产环境
```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

#### 开发环境
```bash
# 使用开发配置启动
docker-compose -f docker-compose.yml -f docker-compose.override.yml up

# 或者
docker-compose up -d
```

### 服务配置

#### charging-management
- 端口: 8080
- 数据持久化: charging-data 卷
- 日志持久化: ./logs 目录
- 健康检查: /actuator/health
- 资源限制: 1 CPU, 1.5GB 内存

#### nginx (可选)
- 端口: 80, 443
- SSL/TLS 支持
- 反向代理
- Gzip 压缩

---

## 4. 配置文件

### 生产环境配置
`src/main/resources/application-prod.yml`
- H2 文件数据库（数据持久化）
- 禁用 H2 Console
- Actuator 健康检查
- 日志文件输出

### 开发环境配置
`src/main/resources/application-dev.yml`
- H2 内存数据库
- 启用 H2 Console
- 调试日志级别
- SQL 语句打印

---

## 5. 快速开始

### 本地构建
```bash
# Maven 构建
mvn clean package -DskipTests

# Docker 构建
docker build -t charging-management:latest .

# 运行容器
docker run -p 8080:8080 charging-management:latest
```

### 使用 Docker Compose
```bash
# 启动所有服务
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f charging-management
```

### 访问服务
- API 地址: http://localhost:8080
- 健康检查: http://localhost:8080/actuator/health
- H2 Console (开发): http://localhost:8080/h2-console

---

## 6. CI/CD 变量配置

### Secrets 配置
在 GitHub 仓库 Settings > Secrets and variables > Actions 中配置：

- `CODECOV_TOKEN`: Codecov 上传令牌（可选）

### 环境变量
- `REGISTRY`: 容器注册表 (默认: ghcr.io)
- `IMAGE_NAME`: 镜像名称

---

## 7. 最佳实践

### 安全
- 使用非 root 用户运行容器
- 定期更新基础镜像
- 启用安全扫描
- 敏感信息使用 Secrets

### 性能
- 多阶段构建减小镜像体积
- 使用 Buildx 缓存
- 合理配置 JVM 参数
- 资源限制防止滥用

### 可维护性
- 清晰的提交信息
- 版本标签管理
- 自动化测试
- 代码覆盖率追踪
