# Bella OpenAPI

## 目录

- [项目结构](#项目结构)
- [使用 Docker Compose 启动项目](#使用-docker-compose-启动项目)
  - [前提条件](#前提条件)
- [启动服务](#启动服务)
  - [使用脚本启动](#使用脚本启动)
  - [快速构建（跳过依赖安装）](#快速构建跳过依赖安装)
  - [使用 Docker Compose 命令](#使用-docker-compose-命令)
- [环境变量配置](#环境变量配置)
- [停止服务](#停止服务)
- [查看日志](#查看日志)
- [重启服务](#重启服务)
- [常见问题与解决方案](#常见问题与解决方案)

## 项目结构

- `api/`: 后端 API 服务
- `web/`: 前端 Web 应用

## 使用 Docker Compose 启动项目

本项目使用 Docker Compose 来启动所有服务，包括后端 API、前端 Web、MySQL 和 Redis。

### 前提条件

- 安装 [Docker](https://www.docker.com/get-started)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

## 启动服务

### 使用脚本启动

```bash
./start.sh [选项]
```

选项:
- `-b, --build`: 重新构建服务
- `-r, --rebuild`: 强制重新构建服务（不使用缓存）
- `-e, --env ENV`: 指定环境（dev, test, prod）
- `--skip-install`: 跳过依赖安装（加快构建速度）
- `-h, --help`: 显示帮助信息

示例:
```bash
./start.sh                       # 启动服务，不重新构建
./start.sh -b                    # 启动服务并重新构建
./start.sh -r                    # 启动服务并强制重新构建（不使用缓存）
./start.sh -e test               # 以测试环境启动服务
./start.sh -b -e prod            # 重新构建并以生产环境启动服务
./start.sh --skip-install        # 跳过依赖安装，加快构建速度
```

### 快速构建（跳过依赖安装）

快速构建脚本会自动优化构建过程，提高开发效率：

1. 如果本地已有 node_modules 目录，将直接使用它
2. 如果本地没有 node_modules 目录，脚本会自动安装依赖
3. 使用 Docker 卷挂载技术，避免在容器内重复安装依赖

```bash
./fast-build.sh [选项]
```

选项:
- `-e, --env ENV`: 指定环境（dev, test, prod），默认为 dev

示例:
```bash
./fast-build.sh                  # 快速构建开发环境
./fast-build.sh -e test          # 快速构建测试环境
./fast-build.sh -e prod          # 快速构建生产环境
```

### 使用 Docker Compose 命令

```bash
docker-compose up -d             # 启动服务
docker-compose up -d --build     # 启动服务并重新构建
```

## 环境变量配置

### 环境变量优先级

在 Docker 环境中，环境变量的优先级从高到低为：
1. docker-compose.yml 中的 environment 设置
2. Dockerfile 中的 ENV 指令
3. .env 文件

### 构建时环境变量

构建时环境变量通过 docker-compose.yml 的 args 部分传递给 Dockerfile：

```yaml
build:
  context: ./web
  args:
    - NODE_ENV=development
    - DEPLOY_ENV=development
    - SKIP_INSTALL=false
```

这些变量会影响应用的构建过程，例如 Next.js 会根据 NODE_ENV 的值加载不同的环境配置文件（.env.development, .env.production 等）。

### 运行时环境变量

运行时环境变量通过 docker-compose.yml 的 environment 部分设置：

```yaml
environment:
  - NODE_ENV=development
  - DEPLOY_ENV=development
```

这些变量会在容器运行时生效，但对于已经构建好的静态文件，只有以 NEXT_PUBLIC_ 开头的环境变量才能在客户端访问。

## 停止服务

```bash
./stop.sh
```
或者直接使用 Docker Compose 命令：

```bash
docker-compose down
```

## 查看日志

```bash
# 查看所有服务的日志
docker-compose logs

# 查看特定服务的日志
docker-compose logs api  # 后端 API 服务
docker-compose logs web  # 前端 Web 应用
docker-compose logs mysql
docker-compose logs redis

# 实时查看日志
docker-compose logs -f
```

## 重启服务

可以使用 start.sh 脚本重启特定服务：

```bash
# 重启前端服务
./start.sh --restart-web

# 重启后端服务
./start.sh --restart-api

# 编译并重启后端服务（会先执行 Maven 编译，然后重新构建 Docker 镜像并重启服务）
./start.sh -b --restart-api

# 重新构建并重启前端服务
./start.sh -b --restart-web
```

也可以直接使用 Docker Compose 命令：

```bash
# 重启前端服务
docker-compose restart web

# 查看前端日志
docker-compose logs -f web

# 重新构建并重启后端服务
docker-compose up -d --build api

# 重新构建并重启前端服务
docker-compose up -d --build web
```

## 常见问题与解决方案

### 1. 前端使用development环境时运行启动脚本编译失败

next.js在dev环境做了预加载相关的优化，如果一定需要使用dev环境，推荐使用next.dev单独启动web服务

---
*最后更新: 2025-03-05*
