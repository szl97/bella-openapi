# Bella OpenAPI

Bella OpenAPI是一个提供AI能力的API网关，提供聊天补全(chat-completion)、文本向量化(text-embedding)、语音识别(ASR)、语音合成(TTS)、文生图、图生图等多种AI能力，同时集成了计费、限流和资源管理功能。

## 目录

- [项目概述](#项目概述)
- [核心功能](#核心功能)
  - [AI能力点](#AI能力点)
  - [元数据管理](#元数据管理)
  - [统一登录服务](#统一登录服务)
  - [计费与限流](#计费与限流)
- [系统优势](#系统优势)
  - [丰富的能力](#丰富的能力)
  - [优秀的特性](#优秀的特性)
  - [统一的元数据管理](#统一的元数据管理)
  - [高性能缓存设计](#高性能缓存设计)
  - [统一的错误处理](#统一的错误处理)
  - [安全可靠](#安全可靠)
  - [高可扩展性](#高可扩展性)
  - [Java友好的技术栈](#java友好的技术栈)
- [配置管理](#配置管理)
  - [环境变量配置](#环境变量配置)
  - [数据库配置](#数据库配置)
  - [缓存配置](#缓存配置)
  - [Apollo配置](#apollo配置)
  - [登录服务配置](#登录服务配置)
- [启动和部署](#启动和部署)
  - [前提条件](#前提条件)
  - [启动服务](#启动服务)
  - [docker-compose环境变量配置](#docker-compose环境变量配置)
  - [停止服务](#停止服务)
  - [查看日志](#查看日志)
  - [重启服务](#重启服务)
  - [系统初始化](#系统初始化)
    - [生成系统API Key](#生成系统api-key)
    - [授权管理员](#授权管理员)
  - [Docker 镜像管理](#docker-镜像管理)
    - [推送 Docker 镜像](#推送-docker-镜像)
- [常见问题与解决方案](#常见问题与解决方案)

## 项目概述

Bella OpenAPI是一个综合性的AI开放API平台，提供以下主要组件：

- **项目结构**：
  - `api/`: 后端API服务，基于Spring Boot框架
  - `web/`: 前端Web应用，基于React和Next.js

- **技术栈**：
  - 后端：Java、Spring Boot、MySQL、Redis
  - 前端：React、Next.js
  - 部署：Docker、Docker Compose，需要 Docker Compose 1.13.0+ 版本
  - 网关：Nginx（可选，使用脚本启动时会自动在容器中创建）

## 核心功能

### AI能力点

- **文本处理**
  - **聊天补全**：提供对话补全能力
  - **文本嵌入**：生成文本的向量表示，用于语义搜索和相似度计算

- **语音服务**
  - **实时语音识别**：支持流式语音识别和一句话语音识别，适用于实时交互场景
  - **文件转写**：支持上传音频文件进行离线转写
  - **语音合成**：将文本转换为自然语音输出，支持流式

- **图像服务**
  - **图生图**：对图片进行编辑，生成新的图片（待实现）
  - **文生图**：根据文本生成图片（待实现）

### 元数据管理

- **多层级结构**：采用Category-Endpoint-Model-Channel四层结构
  - **Category（类别）**：API服务的顶层分类，如语音服务、文本服务等
  - **Endpoint（端点）**：具体的API功能入口，如实时语音识别、聊天补全等
  - **Model（模型）**：支持各个端点的AI模型，如不同的语音识别模型、大语言模型等等
  - **Channel（通道）**：具体的服务提供方实现，包含供应商、协议和配置信息

- **灵活的路由机制**：基于用户请求和配置，智能选择最合适的服务通道
- **可视化管理界面**：提供直观的Web界面进行元数据配置和管理

### 统一登录服务

- **多种认证方式**：支持OAuth 2.0、CAS单点登录和API Key认证
- **会话管理**：基于Redis的分布式会话存储
- **用户权限**：细粒度的权限控制和管理

### 计费与限流

- **API Key管理**：支持层级化的API Key结构
- **配额控制**：按月度配额管理API使用量
- **限流机制**：基于Redis的分布式限流实现

## 系统优势

### 丰富的能力

- **全面的AI能力**：支持聊天补全、文本向量化、语音识别（实时、离线、一句话）、语音合成（支持流式）、文生图、图生图等多种AI能力
- **Mock能力**：内置能力点mock功能，可用于单元测试和压力测试

### 优秀的特性

- **Function Call支持**：为不支持functioncall的LLM扩展了functioncall特性
- **路由策略**：优秀的路由策略，确保高峰期能力点渠道的最大化处理能力
- **请求队列**：支持队列特性，确保高峰期请求有序处理
- **备用模型**：支持备用模型机制（待实现），提高服务可用性
- **最大等待时间**：支持设置最大等待时间（待实现），优化用户体验

### 统一的元数据管理

- **灵活的多层级结构**：采用Category-Endpoint-Model-Channel四层结构，使系统具有高度的可扩展性和灵活性
- **集中式配置**：所有API服务的配置集中管理，便于运维和监控
- **动态路由**：基于用户请求和配置，智能选择最合适的服务通道，提高服务质量

### 高性能缓存设计

- **多级缓存架构**：结合Redisson、Caffeine和JetCache，实现本地缓存与分布式缓存的协同工作
- **高吞吐量**：本地缓存减少网络开销，分布式缓存确保集群一致性
- **自动失效机制**：智能的缓存失效策略，平衡数据一致性和性能

### 统一的错误处理

- **一致的用户体验**：统一的错误提示格式和展示方式
- **细粒度错误分类**：区分服务不可用和其他错误类型，提供更精准的反馈
- **友好的错误提示**：针对不同错误类型使用不同的视觉样式，提高用户体验

### 安全可靠

- **多层次认证**：支持OAuth 2.0、CAS单点登录和API Key认证
- **细粒度权限控制**：基于角色的访问控制，确保资源安全
- **完善的审计日志**：记录关键操作，便于追踪和问题排查
- **生产环境验证**：经过大规模的生产环境的验证，稳定可靠

### 高可扩展性

- **微服务架构**：基于Spring Boot的微服务设计，便于横向扩展
- **容器化部署**：Docker和Docker Compose支持，简化部署和扩展
- **第三方服务集成**：灵活的通道机制，轻松集成各种AI服务提供商

### Java友好的技术栈

- **Spring Boot生态**：基于Spring Boot框架，对Java开发者友好
- **丰富的工具链**：集成了常用的Java开发工具和库
- **完善的文档**：提供详细的API文档和开发指南

## 配置管理

**配置管理为对配置的详细介绍，如果想使用docker直接启动**
**请直接阅读： [启动和部署](#启动和部署)**
**如果需要提供用户登录功能，请阅读 [登录服务配置](#登录服务配置), 或在启动脚本中声明oauth相关参数，详情见[启动服务](#启动服务)，否则只能使用密钥登录**

### 环境变量配置

#### 环境变量优先级

在Docker环境中，环境变量的优先级从高到低为：
1. docker-compose.yml中的environment设置
2. Dockerfile中的ENV指令
3. .env文件、 yaml文件

#### web构建时环境变量

构建时环境变量通过docker-compose.yml的args部分传递给Dockerfile：

```yaml
build:
  context: ./web
  args:
    - NODE_ENV=development
    - DEPLOY_ENV=development
    - SKIP_INSTALL=false
```

这些变量会影响应用的构建过程，例如Next.js会根据NODE_ENV的值加载不同的环境配置文件（.env.development, .env.production等）。

**Next.js构建方法：**

```bash
# 在web目录下执行
cd web

# 安装依赖
npm install

# 使用开发环境变量构建
NODE_ENV=development npm run build

# 使用生产环境变量构建
NODE_ENV=production npm run build

# 指定自定义环境变量构建
NEXT_PUBLIC_API_HOST=http://localhost:8080 npm run build
```

#### web运行时环境变量

运行时环境变量通过docker-compose.yml的environment部分设置：

```yaml
environment:
  - NODE_ENV=development
  - DEPLOY_ENV=development
```

这些变量会在容器运行时生效，但对于已经构建好的静态文件，只有以NEXT_PUBLIC_开头的环境变量才能在客户端访问。

**Next.js运行方法：**

```bash
# 在web目录下执行

# 开发模式运行（带热重载）
npm run dev

# 以生产模式运行已构建的应用
npm run start

# 使用自定义端口运行
PORT=3001 npm run start

# 同时指定环境变量和端口运行
NEXT_PUBLIC_API_HOST=http://localhost:8080 PORT=3001 npm run start
```

### 数据库配置

MySQL数据库配置示例（application.yml）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bella_openapi?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: bella_user
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### 缓存配置

**使用redisson + caffeine + jetcache**

缓存缓存配置示例（application.yml）：

```yaml
spring:
  redis:
    redisson:
      config: |
        singleServerConfig:
          address: "redis://localhost:6379"
        codec: !<org.redisson.client.codec.StringCodec> {}
jetcache:
  statIntervalMinutes: 0
  areaInCacheName: false
  hidePackages: com.ke.bella.openapi
  local:
    default:
      type: caffeine
      limit: 100
      keyConvertor: jackson
      expireAfterAccessInMillis: 0
      expireAfterWriteInMillis: 120000
  remote:
    default:
      type: redisson
      redissonClient: redissonClient
      broadcastChannel: BellaOpenapiBroadcastChannel
      keyConvertor: jackson
      expireAfterAccessInMillis: 0
      expireAfterWriteInMillis: 600000
      keyPrefix: bella-openapi-
```

### Apollo配置

**默认不启用，如果需要可以按照如下配置, 并且在启动时设置环境变量：** 
**-Dapollo.enabled=true**

Apollo动态配置管理示例（application.yml）：

```yaml
apollo:
  bootstrap:
    enabled: true
    namespaces: application,bella.openapi
  meta: http://apollo-config:8080
```

### 登录服务配置

**提供CAS和OAuth两种登录方式，可通过环境变量或配置文件进行配置，使用启动脚本时，亦可通过启动脚本参数进行配置，详情见[启动服务](#启动服务)**

#### session配置

```yaml
bella:
  session:
    cookie-name: bella_openapi_sessionId
    max-inactive-interval: 60
    cookie-max-age: -1
    cookie-domain: localhost # 改为自己的域名，脚本启动时会自动配置
    cookie-context-path: /
```

#### 登录类型配置

通过设置`bella.login.type`环境变量或在配置文件中指定登录类型：

```yaml
bella:
  login:
    type: cas  # 可选值: cas, oauth
```

#### CAS配置

CAS单点登录配置示例：

```yaml
bella:
  cas:
    client-support: true  # 是否支持客户端
    source: company_name  # 来源标识，默认为cas
    server-url-prefix: https://your-cas-server.com/  # CAS服务器URL前缀
    server-login-url: https://your-cas-server.com/login  # CAS登录URL
    client-host: http://your-app-host:8080  # 客户端主机
    client-index-url: http://your-frontend-url/apikey  # 客户端首页URL
```

#### OAuth配置

OAuth登录配置示例：

```yaml
bella:
  oauth:
    client-index: http://localhost:3000  # 客户端首页URL
    providers:  # 支持多个OAuth提供商
      google:  # Google OAuth配置
        enabled: true
        client-id: your-google-client-id
        client-secret: your-google-client-secret
        auth-uri: https://accounts.google.com/o/oauth2/v2/auth
        token-uri: https://oauth2.googleapis.com/token
        user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
        scope: profile email
      github:  # GitHub OAuth配置
        enabled: true
        client-id: your-github-client-id
        client-secret: your-github-client-secret
        scope: read:user user:email
        authUri: https://github.com/login/oauth/authorize
        tokenUri: https://github.com/login/oauth/access_token
        userInfoUri: https://api.github.com/user
```

##### GitHub OAuth配置攻略

要配置GitHub OAuth登录，请按照以下步骤操作：
Google OAuth配置可参照此方式，登录 [Google Cloud Console](https://console.cloud.google.com/)进行配置

1. **创建GitHub OAuth应用**：
   - 登录到您的GitHub账户
   - 访问 [GitHub Developer Settings](https://github.com/settings/developers)
   - 点击 "OAuth Apps" 选项卡
   - 点击 "New OAuth App" 按钮

2. **填写应用信息**：
   - **Application name**：填写您的应用名称，例如 "Bella OpenAPI"
   - **Homepage URL**：填写您的应用主页URL，例如 `http://localhost:3000`
   - **Application description**：（可选）填写应用描述
   - **Authorization callback URL**：填写回调URL，必须与配置文件中的`redirect`为域名，假设为`http://localhost:8080`， 那么url为 `/openapi/oauth/callback/github`， 如果google则为`http://localhost:8080/openapi/oauth/callback/google`
   - 点击 "Register application" 按钮

3. **获取Client ID和Client Secret**：
   - 注册成功后，您将看到应用详情页面
   - 记录下 "Client ID"
   - 点击 "Generate a new client secret" 生成Client Secret
   - 立即复制并保存Client Secret，因为它只会显示一次

4. **更新配置文件**：
   - 在应用的配置文件中填入获取的Client ID和Client Secret：
   ```yaml
   bella:
     oauth:
       providers:
         github:
           enabled: true
           client-id: 您的GitHub Client ID  # 例如：89a6d5f8c7b3e2a1d0f9
           client-secret: 您的GitHub Client Secret  # 例如：3e7d9c8b5a4f2e1d0c9b8a7f6e5d4c3b2a1f0e9d
           scope: read:user user:email
           authUri: https://github.com/login/oauth/authorize
           tokenUri: https://github.com/login/oauth/access_token
           userInfoUri: https://api.github.com/user
   ```

5. **注意事项**：
   - 确保`redirect-uri`与GitHub开发者设置中的回调URL完全匹配
   - 在生产环境中，请使用HTTPS URL
   - 如果您的应用部署在不同的域名或端口，请相应地更新配置
   - GitHub OAuth默认包含用户的公开信息，如需访问邮箱，需要添加`user:email`权限

6. **测试配置**：
   - 启动应用后，访问登录页面
   - 点击"使用GitHub账号登录"按钮
   - 您将被重定向到GitHub授权页面
   - 授权后，您将被重定向回应用，完成登录流程

#### 前端配置

前端应用需要配置对应的登录服务URL和回调地址：

1. 在前端环境配置文件中设置API服务器地址：

```javascript
// .env.development 或 .env.production
NEXT_PUBLIC_API_HOST=http://localhost:8080
```

#### 环境变量配置

可以通过环境变量覆盖配置文件中的设置：

```bash
# 登录类型
export BELLA_LOGIN_TYPE=oauth

# CAS配置
export BELLA_CAS_SERVER_URL_PREFIX=https://your-cas-server.com/
export BELLA_CAS_SERVER_LOGIN_URL=https://your-cas-server.com/login

# OAuth配置
export BELLA_OAUTH_LOGIN_PAGE_URL=http://your-frontend-url/login
export BELLA_OAUTH_CLIENT_INDEX=http://your-frontend-url

# 启动应用时指定配置文件
java -jar bella-openapi.jar --spring.profiles.active=dev
```

## 启动和部署

本项目使用Docker Compose来启动所有服务，包括后端API、前端Web、MySQL和Redis。

### 前提条件

- 安装 [Docker](https://www.docker.com/get-started)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

### 启动服务

```bash
./start.sh [选项] （如果不存在编译文件，会自动开始编译）
```
注意：windows下请使用git bash工具执行命令

选项:
- `-b, --build`: 重新构建服务
- `-r, --rebuild`: 强制重新构建服务（不使用缓存）
- `-e, --env ENV`: 指定环境（dev, test, prod）
- `-h, --help`: 显示帮助信息
- `--skip-auth`: 跳过授权步骤
- `--server URL`: 配置服务域名
- `--github-oauth CLIENT_ID:CLIENT_SECRET`: 配置GitHub OAuth登录
- `--google-oauth CLIENT_ID:CLIENT_SECRET`: 配置Google OAuth登录
- `--cas-server URL`: 配置CAS服务器URL
- `--cas-login URL`: 配置CAS登录URL

示例:
```bash
./start.sh                       # 启动服务，不重新构建
./start.sh -b                    # 启动服务并重新构建（会使用缓存，增量修改编译较快）
./start.sh -r                    # 启动服务并强制重新构建（不使用缓存）
./start.sh -e test               # 以测试环境启动服务
./start.sh -b -e prod            # 重新构建并以生产环境启动服务
./start.sh --skip-install        # 跳过依赖安装，加快构建速度
./start.sh --skip-auth           # 启动服务但跳过授权步骤
./start.sh --server http://example.com #配置服务域名
./start.sh --github-oauth ${clientId}:${secret} --google-oauth ${clientId}:${secret} #配置github和google的oauth登录
./start.sh --github-oauth ${clientId}:${secret} --google-oauth ${clientId}:${secret} --server http://example.com #配置github和google的oauth登录以及服务域名（会自动重新构建）
./start.sh --cas-server https://cas.example.com --cas-login https://cas.example.com/login --server http://example.com #配置CAS登录和服务域名（会自动重新构建）如果既配置cas登录又配置oauth登录，登录时会使用cas登录
```
### docker-compose环境变量配置

#### 环境变量优先级

在Docker环境中，环境变量的优先级从高到低为：
1. docker-compose.yml中的environment设置
2. Dockerfile中的ENV指令
3. .env文件、 yaml文件

#### web构建时环境变量

构建时环境变量通过docker-compose.yml的args部分传递给Dockerfile：

```yaml
build:
  context: ./web
  args:
    - NODE_ENV=development
    - DEPLOY_ENV=development
    - SKIP_INSTALL=false
```

这些变量会影响应用的构建过程，例如Next.js会根据NODE_ENV的值加载不同的环境配置文件（.env.development, .env.production等）。

#### web运行时环境变量

运行时环境变量通过docker-compose.yml的environment部分设置：

```yaml
environment:
  - NODE_ENV=development
  - DEPLOY_ENV=development
```

这些变量会在容器运行时生效，但对于已经构建好的静态文件，只有以NEXT_PUBLIC_开头的环境变量才能在客户端访问。

### 停止服务

```bash
./stop.sh
```
或者直接使用Docker Compose命令：

```bash
docker-compose down
```

### 查看日志

```bash
# 查看所有服务的日志
docker-compose logs

# 查看特定服务的日志
docker-compose logs api  # 后端API服务
docker-compose logs web  # 前端Web应用
docker-compose logs mysql
docker-compose logs redis

# 实时查看日志
docker-compose logs -f
```

### 重启服务

可以使用start.sh脚本重启特定服务：

```bash
# 重启前端服务
./start.sh --restart-web

# 重启后端服务
./start.sh --restart-api

# 编译并重启后端服务（会先执行Maven编译，然后重新构建Docker镜像并重启服务）
./start.sh -b --restart-api

# 重新构建并重启前端服务
./start.sh -b --restart-web
```

也可以直接使用Docker Compose命令：

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

### 系统初始化

#### 生成系统API Key

系统初始化时需要生成一个系统级别的API Key，用于管理和访问所有API资源。我们提供了一个脚本来自动生成系统API Key并将其写入数据库。
./start.sh启动服务时会自动生成。如需手动生成，请使用./generate-system-apikey.sh脚本。

#### 使用方法

##### 直接在Docker环境中执行
./generate-system-apikey.sh

##### 使用自定义数据库连接参数
DB_USER=bella_user DB_PASS=123456 ./generate-system-apikey.sh

#### 脚本功能

1. 检查数据库中是否已存在系统API Key
2. 如果不存在，生成一个新的系统API Key并写入数据库
3. 如果已存在，显示现有系统API Key的信息
4. 将API Key信息保存到`system-apikey.txt`文件中

#### 重要说明

- 系统API Key具有最高权限，请妥善保管
- API Key只会在生成时显示一次，之后只能看到掩码版本
- 如果丢失API Key，需要重新生成一个新的，如果是系统AK，需要将其设置为失效，并重新生成
- 生成的API Key具有`all`角色，可以访问所有端点
- 脚本需要在Docker环境中运行，会自动连接到MySQL容器

#### 示例输出

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

#### 授权管理员

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

### Docker 镜像管理

#### 推送 Docker 镜像

为了便于在生产环境中部署，您可以在本地构建并推送 Docker 镜像到 Docker Hub 或其他 Docker 仓库，然后在服务器上直接拉取和启动这些镜像，而无需在服务器上进行构建。

##### 构建并推送镜像

```bash
# 构建并推送镜像到 Docker Hub
./start.sh --build --push --registry 用户名 --version v1.0.0
```

参数说明：
- `--build`: 构建 Docker 镜像
- `--push`: 构建完成后推送镜像到仓库
- `--registry 用户名`: 指定 Docker Hub 的用户名
- `--version v1.0.0`: 指定镜像版本号（默认为 v1.0.0）

执行此命令后，脚本会：
1. 构建 API 和 Web 服务的 Docker 镜像
2. 将镜像标记为 `用户名/bella-openapi-api:v1.0.0` 和 `用户名/bella-openapi-web:v1.0.0`
3. 同时标记为 `用户名/bella-openapi-api:latest` 和 `用户名/bella-openapi-web:latest`
4. 推送这些镜像到 Docker Hub

##### 在生产服务器上部署

在生产服务器上，您可以直接使用已推送的镜像启动服务：

```bash
# 拉取并启动已推送的镜像
./start.sh --registry 用户名 --version v1.0.0
```

这将从 Docker Hub 拉取指定版本的镜像并启动服务，无需在服务器上进行构建过程，大大减少了部署时间和资源消耗。

##### 使用特定版本的镜像

如果您需要使用特定版本的镜像，可以通过 `--version` 参数指定：

```bash
# 使用特定版本的镜像
./start.sh --registry 用户名 --version v1.1.0
```

##### 注意事项

1. 确保您已登录到 Docker Hub：
   ```bash
   docker login
   ```

2. 推送镜像前，请确保您有足够的权限访问指定的 Docker Hub 仓库

3. 在生产环境中，建议使用具体的版本号而不是 `latest` 标签，以确保部署的一致性和可追溯性

4. 如果您使用私有 Docker 仓库，请相应地调整 `--registry` 参数

### 常见问题与解决方案

#### 1. 前端使用development环境时运行启动脚本编译失败

next.js在dev环境做了预加载相关的优化，如果一定需要使用dev环境，推荐使用next.dev单独启动web服务

#### 2. 初始化并启动系统，清除原有数据（开发、测试环境）

1. 删除数据库：`docker exec -it bella-openapi-mysql mysql -uroot -p123456 -e "drop database bella_openapi;"` (如非默用户名和密码，请替换为您的用户名和密码)
2. 停止服务：`./stop.sh`
3. 删除mysql数据缓存：`rm -rf ./api/mysql`
4. 删除redis数据缓存：`rm -rf ./api/redis`
5. 重新构建并启动：`./start.sh -b`
---
*最后更新: 2025-03-24*

#### 3. MySQL 或 Redis 容器启动失败

如果您遇到 MySQL 或 Redis 容器启动失败的问题，通常是由于数据目录权限问题或系统配置导致的。

**常见错误信息**：
- MySQL: `ls: cannot access '/docker-entrypoint-initdb.d/': Operation not permitted`
- Redis: `Fatal: Can't initialize Background Jobs`

**解决方法**：

1. 停止所有容器：
   ```bash
   ./stop.sh
   ```

2. 删除并重新创建数据目录：
   ```bash
   sudo rm -rf ./api/mysql
   sudo rm -rf ./api/redis
   mkdir -p ./api/mysql
   mkdir -p ./api/redis
   chmod -R 777 ./api/mysql
   chmod -R 777 ./api/redis
   ```

3. 对于 Redis 内存过度使用问题，在主机上运行：
   ```bash
   sudo sysctl vm.overcommit_memory=1
   ```
   要永久解决，请将 `vm.overcommit_memory = 1` 添加到 `/etc/sysctl.conf` 文件中。

4. 重新启动服务：
   ```bash
   ./start.sh
   ```

**注意**：启动脚本会自动创建数据目录并设置正确的权限，但在某些环境中可能仍需手动处理权限问题。
