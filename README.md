# Bella OpenAPI

## 目录

- [项目结构](#项目结构)
- [使用 Docker Compose 启动项目](#使用-docker-compose-启动项目)
- [前提条件](#前提条件)
- [启动服务](#启动服务)
- [环境变量配置](#环境变量配置)
- [停止服务](#停止服务)
- [查看日志](#查看日志)
- [重启服务](#重启服务)
- [生成系统API Key](#生成系统api-key)
- [授权管理员](#授权管理员)
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

```bash
./start.sh [选项]
```

选项:
- `-b, --build`: 重新构建服务
- `-r, --rebuild`: 强制重新构建服务（不使用缓存）
- `-e, --env ENV`: 指定环境（dev, test, prod）
- `-h, --help`: 显示帮助信息
- `--skip-auth`: 跳过授权步骤

示例:
```bash
./start.sh                       # 启动服务，不重新构建
./start.sh -b                    # 启动服务并重新构建（会使用缓存，增量修改编译较快）
./start.sh -r                    # 启动服务并强制重新构建（不使用缓存）
./start.sh -e test               # 以测试环境启动服务
./start.sh -b -e prod            # 重新构建并以生产环境启动服务
./start.sh --skip-install        # 跳过依赖安装，加快构建速度
./start.sh --skip-auth           # 启动服务但跳过授权步骤
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

## 生成系统API Key

系统初始化时需要生成一个系统级别的API Key，用于管理和访问所有API资源。我们提供了一个脚本来自动生成系统API Key并将其写入数据库。
./start.sh 启动服务时会自动生成。如需手动生成，请使用 ./generate-system-apikey.sh 脚本。

### 使用方法

#### 直接在Docker环境中执行
./generate-system-apikey.sh

#### 使用自定义数据库连接参数
DB_USER=bella_user DB_PASS=123456 ./generate-system-apikey.sh

### 脚本功能

1. 检查数据库中是否已存在系统API Key
2. 如果不存在，生成一个新的系统API Key并写入数据库
3. 如果已存在，显示现有系统API Key的信息
4. 将API Key信息保存到`system-apikey.txt`文件中

### 重要说明

- 系统API Key具有最高权限，请妥善保管
- API Key只会在生成时显示一次，之后只能看到掩码版本
- 如果丢失API Key，需要重新生成一个新的，如果是系统AK，需要将其设置为失效，并重新生成
- 生成的API Key具有`all`角色，可以访问所有端点
- 脚本需要在Docker环境中运行，会自动连接到MySQL容器

### 示例输出

```
正在生成系统API key...
检查数据库中是否已存在系统API key...
在数据库中插入新的系统API key...
成功生成并插入系统API key到数据库。
API Key Code: ak-026e84f5-8a1c-4243-a800-d44581f0f1b7
API Key: 9be9d54d-d4ae-4510-8819-a62a4e69e57b
API Key SHA-256: d58ed6447aa8da22d6fa3064d242f8f8dd74a6df4a1663084f4003b2d559b9ea
API Key Display: 9b****e57b
API key详细信息已保存到 system-apikey.txt
重要: 请妥善保管此信息，API key只会显示一次！
完成。

```

### 授权管理员

系统初始化后，需要授权管理员用户。管理员用户可以管理API Key、用户权限等系统资源。

#### 授权流程

1. 首先启动服务并生成系统API Key
   ```bash
   ./start.sh
   ```
   如果新生成系统ak时会自动进入管理员授权流程，如果不想在启动时进行管理员授权（仍会检查系统ak是否需要生成），可以使用`--skip-auth`参数：
   ```bash
   ./start.sh --skip-auth
   ```
   
   注意：如果系统中已存在API Key，脚本会自动跳过管理员授权步骤。只有在首次生成新的API Key时才会询问是否需要授权管理员。

2. 启动脚本会询问您是否需要授权管理员
   - 如果选择"是"，脚本会引导您完成整个授权流程
   - 如果选择"否"，您可以稍后手动运行授权脚本
   - 如果使用了 `--skip-auth` 参数，则会跳过询问步骤

3. 按照提示登录前端页面获取用户ID或邮箱
   - 访问 http://localhost:3000
   - 使用第三方账号登录（如Google、GitHub等）
   - 点击右上角头像，查看个人信息获取用户ID或邮箱

4. 授权脚本会自动启动，根据提示输入用户信息
   - 可以选择使用用户ID或邮箱进行授权
   - 按照脚本提示输入相关信息

5. 如果您稍后需要授权管理员，可以随时运行：
   ```bash
   ./authorize-admin.sh
   ```
6. 授权后点击头像登出系统，重新登录即可拥有管理员权限

#### 手动授权

也可以使用curl命令手动授权：

##### 使用用户ID授权
```bash
curl --location 'http://localhost:8080/console/userInfo/manager' \
--header 'Authorization: Bearer YOUR_SYSTEM_API_KEY' \
--header 'Content-Type: application/json' \
--data-raw '{
    "managerAk": "YOUR_SYSTEM_API_KEY_CODE",
    "userId": YOUR_USER_ID,
    "userName": "YOUR_USER_NAME"
}'
```

##### 使用邮箱授权
```bash
curl --location 'http://localhost:8080/console/userInfo/manager' \
--header 'Authorization: Bearer YOUR_SYSTEM_API_KEY' \
--header 'Content-Type: application/json' \
--data-raw '{
    "managerAk": "YOUR_SYSTEM_API_KEY_CODE",
    "source": "google",
    "email": "YOUR_EMAIL@gmail.com",
    "userId": 0,
    "userName": "YOUR_USER_NAME"
}'
```

## 常见问题与解决方案

### 1. 前端使用development环境时运行启动脚本编译失败

next.js在dev环境做了预加载相关的优化，如果一定需要使用dev环境，推荐使用next.dev单独启动web服务

### 2. 初始化并启动系统，清除原有数据（开发、测试环境）

1. 删除数据库：`docker exec -it bella-openapi-mysql mysql -uroot -p123456 -e "drop database bella_openapi;"` (如非默用户名和密码，请替换为您的用户名和密码)
2. 停止服务：`./stop.sh`
3. 删除mysql数据缓存：`rm -rf ./api/mysql`
4. 删除redis数据缓存：`rm -rf ./api/redis`
5. 重新构建并启动：`./start.sh -b`
---
*最后更新: 2025-03-06*
