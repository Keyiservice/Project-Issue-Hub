# Project Issue Hub 部署指南

## 1. 文档目标

本文档用于指导 `Project Issue Hub` 在本地开发环境、企业内网服务器和正式运行环境中的部署、验收、运维与微信小程序发布。

适用对象：

- 项目负责人
- IT 运维 / 基础设施管理员
- 后端 / 前端开发人员
- 微信小程序发布负责人

---

## 2. 系统组成

当前系统由 5 个核心服务组成：

1. `mysql`：业务主数据库
2. `redis`：缓存与会话辅助
3. `minio`：图片 / 视频等附件对象存储
4. `backend`：Spring Boot 业务后端
5. `web`：Vue 3 Web 管理后台

客户端侧包括：

- 微信原生小程序：现场录入、跟进、查看项目问题库
- Web 管理端：项目问题库、驾驶舱、统计、用户与团队配置

---

## 3. 环境要求

### 3.1 本地开发环境

建议最低配置：

- Windows 11 / macOS / Linux
- CPU：4 核
- 内存：16 GB
- 可用磁盘：30 GB
- Docker Desktop 或 Docker Engine
- 微信开发者工具

建议软件版本：

- Docker / Docker Compose：最新稳定版
- Node.js：20+
- Java：17
- Maven：3.9+
- Git：2.4+

### 3.2 服务器部署环境

单机部署建议：

- 操作系统：Ubuntu 22.04 LTS / CentOS Stream 9 / Rocky Linux 9
- CPU：4 核起
- 内存：8 GB 起
- 磁盘：100 GB 起，SSD 优先
- 网络：可访问 Docker 镜像源或企业私有镜像仓库

推荐生产配置：

- CPU：8 核
- 内存：16 GB
- 数据盘：200 GB+
- 定期备份能力

### 3.3 微信小程序发布前提

- 已注册企业主体小程序
- 已获取 `AppID`
- 已配置小程序管理员 / 开发者权限
- 已准备可公网访问的 `HTTPS` 域名
- 已在微信公众平台配置合法域名

---

## 4. 端口与默认访问地址

当前 `docker-compose.yml` 默认端口如下：

| 服务 | 容器名 | 容器端口 | 宿主机端口 | 说明 |
|---|---|---:|---:|---|
| MySQL | `opl-mysql` | 3306 | 3307 | 业务数据库 |
| Redis | `opl-redis` | 6379 | 6380 | 缓存 |
| MinIO API | `opl-minio` | 9000 | 9002 | 文件对象接口 |
| MinIO Console | `opl-minio` | 9001 | 9003 | MinIO 控制台 |
| Backend | `opl-backend` | 8080 | 8081 | Spring Boot API |
| Web | `opl-web` | 80 | 80 | 管理后台 |

默认访问地址：

- Web 管理端：`http://localhost`
- 后端接口：`http://localhost:8081`
- Swagger：`http://localhost:8081/swagger-ui.html`
- MinIO Console：`http://localhost:9003`

---

## 5. 默认账号与初始化数据

系统首次启动后，数据库初始化脚本会自动执行。

默认账号：

- 用户名：`admin`
- 密码：`123456`

说明：

- 当前初始化数据中已包含示例项目和示例问题数据
- 后续正式上线前建议清理测试数据并导入正式项目与正式用户

---

## 6. 配置文件说明

### 6.1 Docker Compose 主配置

主文件位置：

- `docker-compose.yml`

主要职责：

- 编排 `mysql / redis / minio / backend / web`
- 控制依赖启动顺序
- 暴露宿主机端口
- 注入时区、数据库、对象存储、小程序登录环境变量

### 6.2 环境变量文件

示例文件：

- `.env.example`

建议上线时复制为：

- `.env`

关键环境变量：

| 变量名 | 作用 | 示例 |
|---|---|---|
| `MYSQL_HOST_PORT` | MySQL 宿主机端口 | `3307` |
| `REDIS_HOST_PORT` | Redis 宿主机端口 | `6380` |
| `MINIO_API_HOST_PORT` | MinIO API 端口 | `9002` |
| `MINIO_CONSOLE_HOST_PORT` | MinIO 控制台端口 | `9003` |
| `BACKEND_HOST_PORT` | 后端宿主机端口 | `8081` |
| `WEB_HOST_PORT` | Web 宿主机端口 | `80` |
| `MINIO_PUBLIC_ENDPOINT` | 对外访问附件的地址 | `https://files.example.com` |
| `WECHAT_MINIAPP_MOCK_ENABLED` | 小程序是否使用 mock 登录 | `false` |
| `WECHAT_MINIAPP_APP_ID` | 小程序 AppID | `wx123456...` |
| `WECHAT_MINIAPP_SECRET` | 小程序 Secret | `******` |

### 6.3 后端核心配置

后端配置文件：

- `backend/src/main/resources/application.yml`

已统一配置：

- 时区：`Asia/Shanghai`
- 时间格式：ISO 8601
- MySQL 时区：`serverTimezone=Asia/Shanghai`
- 文件上传上限：
  - 单文件：`200MB`
  - 总请求：`300MB`

注意：

- 业务上小程序仍限制图片 / 视频单文件 `5MB`
- 服务端设置更高上限，是为了兼容 Web 上传和运维场景

---

## 7. 本地开发部署

### 7.1 启动步骤

在项目根目录执行：

```powershell
docker compose up -d --build
```

### 7.2 查看状态

```powershell
docker compose ps
```

### 7.3 查看日志

```powershell
docker compose logs --tail=200 backend
docker compose logs --tail=200 web
docker compose logs --tail=200 mysql
```

### 7.4 停止服务

```powershell
docker compose down
```

如需同时删除卷数据：

```powershell
docker compose down -v
```

说明：

- `down -v` 会清空数据库、Redis 和 MinIO 的持久化数据
- 正式环境不要随意执行

---

## 8. Linux 服务器部署

### 8.1 安装 Docker

推荐使用官方稳定版 Docker Engine 与 Docker Compose 插件。

安装完成后验证：

```bash
docker version
docker compose version
```

### 8.2 准备项目目录

示例：

```bash
mkdir -p /opt/project-issue-hub
cd /opt/project-issue-hub
git clone <your-repo-url> .
cp .env.example .env
```

### 8.3 修改 `.env`

正式环境重点修改：

```env
MYSQL_HOST_PORT=3307
REDIS_HOST_PORT=6380
MINIO_API_HOST_PORT=9002
MINIO_CONSOLE_HOST_PORT=9003
BACKEND_HOST_PORT=8081
WEB_HOST_PORT=80

MINIO_PUBLIC_ENDPOINT=https://files.your-domain.com
WECHAT_MINIAPP_MOCK_ENABLED=false
WECHAT_MINIAPP_APP_ID=你的AppID
WECHAT_MINIAPP_SECRET=你的小程序Secret
```

建议新增：

```env
REPORT_PUBLIC_BASE_URL=https://your-domain.com
```

作用：

- Excel 报表中的附件访问链接会使用该地址生成

### 8.4 启动服务

```bash
docker compose up -d --build
```

### 8.5 验证服务

```bash
docker compose ps
curl http://127.0.0.1
curl http://127.0.0.1:8081/swagger-ui.html
```

---

## 9. 生产环境推荐拓扑

推荐至少准备以下公网 / 内网域名：

| 域名 | 目标服务 | 用途 |
|---|---|---|
| `opl.your-domain.com` | Web + API 网关 | 管理端访问入口 |
| `api.your-domain.com` | Backend | 后端 API |
| `files.your-domain.com` | MinIO / 文件代理 | 图片与视频附件预览 |

推荐方式：

1. 外层使用 Nginx 或企业网关统一接入
2. Web 通过 80/443 暴露给用户
3. 后端 API 通过 HTTPS 域名对外暴露
4. MinIO 建议不要直接裸暴露控制台到公网

---

## 10. HTTPS 与反向代理建议

小程序正式发布必须使用合法的 HTTPS 域名，因此正式环境建议：

1. 为 Web、API、文件访问分别规划域名
2. 使用 Nginx 或企业负载均衡配置 HTTPS 证书
3. 将 Web 与 API 分离反向代理
4. 对文件访问域名做安全控制

典型反向代理逻辑：

- `https://opl.your-domain.com` -> `opl-web:80`
- `https://api.your-domain.com` -> `opl-backend:8080`
- `https://files.your-domain.com` -> `opl-minio:9000`

---

## 11. 数据持久化与备份

当前 Docker 卷：

- `mysql-data`
- `redis-data`
- `minio-data`

### 11.1 MySQL 备份

建议每天执行：

```bash
docker exec opl-mysql mysqldump -uroot -proot123456 opl_platform > /backup/opl_platform_$(date +%F).sql
```

### 11.2 MinIO 备份

建议定期备份：

- `/var/lib/docker/volumes/.../minio-data`
- 或使用对象存储同步工具做异地备份

### 11.3 备份建议

- 数据库：每日备份
- 附件：每日增量 / 每周全量
- Docker Compose 与 `.env`：变更后归档

---

## 12. 升级与回滚

### 12.1 升级步骤

```bash
cd /opt/project-issue-hub
git pull
docker compose up -d --build
```

### 12.2 升级前建议

1. 先备份 MySQL
2. 记录当前镜像版本 / Git tag
3. 确认数据库初始化脚本变更内容

### 12.3 回滚建议

如果升级失败：

1. 回退到上一个 Git tag
2. 重新构建并启动容器
3. 必要时恢复数据库备份

---

## 13. 小程序开发联调说明

开发阶段当前常用方式：

- 本地后端：`http://127.0.0.1:8081/api`
- 微信开发者工具中关闭合法域名校验

适用场景：

- 本机开发
- 界面调试
- 功能联调

注意：

- 该方式仅适用于开发者工具
- 真机调试和正式发布不能使用本机 `127.0.0.1`

---

## 14. 小程序正式发布前准备

正式发布前必须完成：

1. 在后端 `.env` 中关闭小程序 mock 登录
2. 配置真实 `AppID` 与 `Secret`
3. 将接口切换为可公网访问的 HTTPS 域名
4. 在微信公众平台配置合法域名

至少需要配置的域名类型：

- `request` 合法域名
- `uploadFile` 合法域名
- `downloadFile` 合法域名

如果后续使用 WebView，再补业务域名。

---

## 15. 小程序发布流程

建议按以下顺序操作：

### 15.1 准备发布版本

1. 确认后端正式环境已部署并稳定可访问
2. 确认合法域名已生效
3. 确认小程序代码中的 API 地址为正式域名
4. 在微信开发者工具中打开 `miniapp/`

### 15.2 上传代码

在微信开发者工具中：

1. 点击“上传”
2. 填写版本号
3. 填写版本说明

建议版本说明格式：

- `v0.1.0 首次试运行版`
- `v0.1.1 修复附件预览与项目统计`

### 15.3 提交审核

上传后，进入微信公众平台小程序后台：

1. 进入“版本管理”
2. 选择开发版本 / 体验版本
3. 填写服务类目与功能说明
4. 提交审核

### 15.4 发布上线

审核通过后：

1. 在后台点击发布
2. 生成线上版本
3. 通知试点项目团队开始使用

---

## 16. 小程序上线检查清单

上线前请逐项确认：

- [ ] 小程序 `AppID` 正确
- [ ] 后端 `WECHAT_MINIAPP_MOCK_ENABLED=false`
- [ ] 后端 `WECHAT_MINIAPP_APP_ID` 已配置
- [ ] 后端 `WECHAT_MINIAPP_SECRET` 已配置
- [ ] 所有接口已切换为 HTTPS 域名
- [ ] 微信公众平台已配置合法域名
- [ ] 图片上传、视频上传可用
- [ ] 登录、项目选择、问题创建、跟进、关闭已走通
- [ ] 报表导出可用

---

## 17. 上线后运维建议

建议按周执行以下检查：

1. 检查容器状态
2. 检查磁盘空间
3. 检查 MySQL 备份是否成功
4. 检查 MinIO 存储增长情况
5. 检查报表导出和附件预览是否正常
6. 检查小程序登录是否正常

常用命令：

```bash
docker compose ps
docker compose logs --tail=200 backend
docker compose logs --tail=200 web
docker volume ls
docker system df
```

---

## 18. 常见问题

### 18.1 Web 打不开

排查顺序：

1. `docker compose ps`
2. 检查 `web` 和 `backend` 是否都为 `Up`
3. 查看 `backend` 日志是否反复重启

### 18.2 图片上传网络异常

重点检查：

- Nginx 上传大小限制
- 后端文件上传配置
- MinIO 对外访问地址是否正确

### 18.3 小程序真机无法登录

重点检查：

- 是否仍在使用 `127.0.0.1`
- 是否配置 HTTPS 域名
- 是否已在微信后台配置合法域名
- `WECHAT_MINIAPP_APP_ID / SECRET` 是否正确

### 18.4 小程序视频上传失败

重点检查：

- 视频是否超过 15 秒
- 视频是否超过 5 MB
- 开发者工具本地是否缺少视频处理能力

---

## 19. 推荐上线顺序

建议分三步推进：

### 第一步：内网试运行

- 先部署 Web + 后端 + 数据库
- 管理层和项目经理先用 Web 管理问题库

### 第二步：小程序试点

- 选一个项目组或一个现场团队试点
- 验证图片、视频、关闭证据和时间线流程

### 第三步：正式推广

- 逐步扩展到全部项目
- 引入项目团队维护、报表导出和管理驾驶舱

---

## 20. 交付物清单

部署时建议保留以下交付物：

- 项目源码仓库
- `.env` 正式配置文件
- 数据库备份脚本
- MinIO 备份方案
- 小程序发布记录
- 系统介绍 PPT
- 本部署指南

