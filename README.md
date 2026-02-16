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

## 快速启动 (Docker 一键部署)

### 前置条件

- 安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- 安装 [Git](https://git-scm.com/)

### 第一步：打包后端

```bash
# 需要本地有 JDK 8 和 Maven
mvn clean package -DskipTests
```

打包后会生成 `sky-server/target/sky-server-1.0-SNAPSHOT.jar`

### 第二步：一键启动

```bash
docker-compose up -d
```

首次启动需要拉取镜像和构建，大约 3~5 分钟。

### 第三步：访问

| 服务 | 地址 |
|------|------|
| 管理端后台 | http://localhost |
| 后端接口文档 | http://localhost:8080/doc.html |
| MySQL | localhost:3306 (root / sky123456) |
| Redis | localhost:6379 |

管理端默认账号: `admin` / `123456`

### 查看小程序

小程序源码在 `deploy/wechat-mp/` 目录下，用微信开发者工具导入即可预览。

小程序需要修改接口地址，找到请求基础路径配置，改为 `http://你的电脑IP:8080`，
并在微信开发者工具中勾选「不校验合法域名」。

## 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 查看运行状态
docker-compose ps

# 查看后端日志
docker-compose logs -f java-app

# 停止所有服务
docker-compose down

# 停止并清除数据 (重新初始化数据库)
docker-compose down -v
```

## 不用 Docker？手动启动

1. 安装 MySQL 8，创建数据库并导入 `deploy/sql/sky.sql`
2. 安装 Redis
3. 修改 `sky-server/src/main/resources/application-dev.yml` 中的数据库和 Redis 连接信息
4. 运行 `mvn clean package -DskipTests`
5. 运行 `java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar`
6. 管理端前端: 用 Nginx 托管 `deploy/nginx/html/sky/` 目录，或直接用 `deploy/nginx/html/sky/` 下的原始 Nginx 方式运行
