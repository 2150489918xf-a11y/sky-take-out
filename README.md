# 苍穹外卖 (Sky Take Out)

外卖点餐系统，包含管理端后台 + 用户端微信小程序。

## 项目结构

```
sky-take-out/
├── sky-common/          # 公共模块 (工具类、常量、异常)
├── sky-pojo/            # 实体类模块 (Entity、DTO、VO)
├── sky-server/          # 后端服务 (Spring Boot)
├── deploy/
│   ├── nginx/
│   │   ├── default.conf # Nginx 配置
│   │   └── html/sky/    # 管理端前端页面
│   ├── sql/sky.sql      # 数据库初始化脚本
│   └── wechat-mp/       # 微信小程序源码
├── Dockerfile
└── docker-compose.yml
```

## 技术栈

- 后端: Spring Boot 2.7.3 + MyBatis + MySQL 8 + Redis
- 前端 (管理端): Vue.js + Element UI + Apache ECharts
- 前端 (用户端): 微信小程序 (uni-app 编译)
- 部署: Docker Compose + Nginx

---

## 启动前必读：配置你自己的账号信息

项目运行需要以下 4 项外部服务的账号，请按步骤获取后填入配置文件。

配置文件位置：`sky-server/src/main/resources/application-dev.yml`

### 1. MySQL 数据库 (本地安装即可)

如果你用 Docker 方式启动，数据库会自动创建，无需额外配置。

如果你手动启动，需要自己安装 MySQL 8，默认配置如下：

```yaml
sky:
  datasource:
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: 123456          # ← 改成你自己的 MySQL 密码
```

### 2. Redis (本地安装即可)

如果你用 Docker 方式启动，Redis 会自动创建，无需额外配置。

如果你手动启动，需要自己安装 Redis，默认配置如下：

```yaml
sky:
  redis:
    host: localhost
    port: 6379
    database: 10              # 使用第 10 号数据库，一般不用改
```

Redis 默认无密码，如果你设置了密码，在 `application.yml` 的 redis 配置下加一行：
```yaml
    password: 你的Redis密码
```

### 3. 阿里云 OSS (用于图片上传)

项目中菜品图片、套餐图片的上传存储依赖阿里云 OSS 对象存储服务。

**获取步骤：**

1. 注册 [阿里云账号](https://www.aliyun.com/)
2. 开通 OSS 服务：控制台 → 对象存储 OSS → 创建 Bucket
   - Bucket 名称：自定义，如 `my-sky-takeout`
   - 地域：选离你近的，如 `华东1（杭州）`
   - 读写权限：**公共读**（这样图片才能在前端显示）
3. 获取 AccessKey：右上角头像 → AccessKey 管理 → 创建 AccessKey
   - 会得到 `AccessKey ID` 和 `AccessKey Secret`
   - **Secret 只显示一次，务必保存好**

```yaml
sky:
  alioss:
    endpoint: oss-cn-hangzhou.aliyuncs.com   # ← 根据你选的地域修改
    access-key-id: 你的AccessKeyId            # ← 填入
    access-key-secret: 你的AccessKeySecret    # ← 填入
    bucket-name: 你的Bucket名称               # ← 填入
```

> endpoint 对照表：杭州 `oss-cn-hangzhou`，上海 `oss-cn-shanghai`，北京 `oss-cn-beijing`，深圳 `oss-cn-shenzhen`

**不想配阿里云 OSS？** 可以跳过，项目其他功能正常使用，只是菜品/套餐的图片上传功能不可用。

### 4. 微信小程序 AppID 和 Secret (用于小程序登录)

用户端小程序的微信登录功能需要小程序的 AppID 和 Secret。

**获取步骤：**

1. 注册 [微信小程序账号](https://mp.weixin.qq.com/)（选择"小程序"类型注册）
2. 登录后进入：开发管理 → 开发设置
3. 页面上方可以看到 **AppID (小程序ID)**
4. 同一页面下方"AppSecret (小程序密钥)"→ 点击"重置" → 获取 Secret
   - **Secret 只显示一次，务必保存好**

```yaml
sky:
  wechat:
    appid: 你的AppID                          # ← 填入
    secret: 你的AppSecret                     # ← 填入
```

> 如果只想看管理端后台效果，不需要小程序登录，可以先不配这项。

### 配置总览

全部填完后，你的 `application-dev.yml` 大概长这样：

```yaml
sky:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: 你的MySQL密码
  alioss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: LTAI5txxxxxxxxxx
    access-key-secret: mwsMxxxxxxxxxxxxxxxx
    bucket-name: my-sky-takeout
  redis:
    host: localhost
    port: 6379
    database: 10
  wechat:
    appid: wx1234567890abcdef
    secret: abcdef1234567890abcdef1234567890
```

---

## 快速启动

### 方式一：Docker 一键部署 (推荐)

#### 前置条件

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [JDK 8](https://adoptium.net/) + [Maven](https://maven.apache.org/download.cgi)

#### 步骤

```bash
# 1. 打包后端
mvn clean package -DskipTests

# 2. 一键启动 (MySQL + Redis + Java + Nginx 全部自动创建)
docker-compose up -d

# 首次启动需要拉取镜像，大约 3~5 分钟
# 查看启动日志：docker-compose logs -f java-app
```

Docker 方式下，MySQL 和 Redis 由容器自动管理，数据库也会自动初始化，
你只需要在 `application-dev.yml` 中配好阿里云 OSS 和微信小程序的信息即可。

> 注意：Docker 方式使用的是 `application-prod.yml`，其中 MySQL/Redis 的地址已经指向容器内部。
> 如果你需要修改阿里云 OSS 或微信配置，修改 `application-prod.yml` 或通过环境变量传入。

#### 访问

| 服务 | 地址 |
|------|------|
| 管理端后台 | http://localhost |
| 后端接口文档 (Swagger) | http://localhost:8080/doc.html |
| MySQL | localhost:3306 (root / sky123456) |
| Redis | localhost:6379 |

管理端默认账号：`admin` / `123456`

### 方式二：手动启动 (不用 Docker)

#### 前置条件

- [JDK 8](https://adoptium.net/) + [Maven](https://maven.apache.org/download.cgi)
- [MySQL 8.0](https://dev.mysql.com/downloads/mysql/)
- [Redis](https://redis.io/download/)
- [Nginx](https://nginx.org/en/download.html) (可选，用于管理端前端)

#### 步骤

```bash
# 1. 创建数据库并导入数据
mysql -u root -p < deploy/sql/sky.sql

# 2. 确保 Redis 已启动
redis-cli ping    # 应返回 PONG

# 3. 修改配置文件
#    编辑 sky-server/src/main/resources/application-dev.yml
#    填入你的 MySQL 密码、阿里云 OSS、微信小程序信息（参考上方说明）

# 4. 打包
mvn clean package -DskipTests

# 5. 启动后端
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar

# 6. 管理端前端
#    方式A：用 Nginx 托管 deploy/nginx/html/sky/ 目录
#    方式B：直接浏览器打开 deploy/nginx/html/sky/index.html
#    (方式B下接口请求需要后端跑在 8080 端口)
```

---

## 微信小程序预览

小程序源码在 `deploy/wechat-mp/` 目录下。

1. 下载 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具 → 导入项目 → 选择 `deploy/wechat-mp/` 目录
3. 填入你的小程序 AppID
4. 修改小程序中的接口地址：
   - 找到 `common/vendor.js` 中的 `baseUrl`，改为 `http://你的电脑局域网IP:8080`
   - 或在开发者工具中全局搜索 `localhost` 替换为你的 IP
5. 在开发者工具右上角 → 详情 → 本地设置 → 勾选「不校验合法域名」

> 小程序需要后端服务正在运行，且手机和电脑在同一局域网下才能正常使用。

---

## 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 查看运行状态
docker-compose ps

# 查看后端日志
docker-compose logs -f java-app

# 重启后端 (修改代码重新打包后)
mvn clean package -DskipTests
docker-compose up -d --build java-app

# 停止所有服务
docker-compose down

# 停止并清除数据 (会删除数据库，重新初始化)
docker-compose down -v
```

---

## 常见问题

**Q: Docker 启动后 java-app 一直重启？**
A: 可能是 MySQL 还没初始化完成。运行 `docker-compose logs -f java-app` 查看日志，等 MySQL 健康检查通过后会自动连接成功。

**Q: 管理端页面打开是空白？**
A: 确认 `deploy/nginx/html/sky/` 目录下有 `index.html` 等前端文件。

**Q: 图片上传失败？**
A: 检查阿里云 OSS 配置是否正确，Bucket 权限是否设为"公共读"。

**Q: 小程序登录失败？**
A: 检查 `application-dev.yml` 中的微信 appid 和 secret 是否正确，后端服务是否正在运行。

**Q: 端口被占用？**
A: 确保本地的 80、3306、6379、8080 端口没有被其他程序占用。可以用 `netstat -ano | findstr :8080` (Windows) 或 `lsof -i :8080` (Mac/Linux) 检查。
