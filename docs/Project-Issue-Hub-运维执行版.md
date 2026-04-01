# Project Issue Hub 运维执行版

## 1. 目标

本文档面向运维人员，强调“照着做就能落地”。

本执行版只聚焦以下动作：

1. 服务器准备
2. Docker 安装
3. 项目部署
4. HTTPS / Nginx 反向代理
5. 微信小程序上线前配置
6. 备份、升级、回滚
7. 日常巡检与故障排查

---

## 2. 部署方式建议

当前项目最稳妥的上线方式：

- 应用部署在 1 台 Linux 服务器
- 使用 `docker compose` 编排
- 外层再挂一层宿主机 Nginx 做 HTTPS 与域名入口

推荐部署结构：

| 层级 | 组件 | 作用 |
|---|---|---|
| 入口层 | 宿主机 Nginx | HTTPS 证书、域名、反向代理 |
| 应用层 | `opl-web` / `opl-backend` | 页面与 API |
| 数据层 | `opl-mysql` / `opl-redis` / `opl-minio` | 数据、缓存、附件 |

---

## 3. 服务器最低要求

推荐正式环境至少：

- OS：Ubuntu 22.04 LTS
- CPU：4 核
- 内存：8 GB
- 磁盘：100 GB SSD
- 开放端口：`80 / 443`

推荐生产环境：

- CPU：8 核
- 内存：16 GB
- 磁盘：200 GB

---

## 4. 目录规划

建议统一部署目录：

```bash
/opt/project-issue-hub
```

建议备份目录：

```bash
/data/backups/project-issue-hub
```

建议日志归档目录：

```bash
/data/logs/project-issue-hub
```

---

## 5. Docker 安装

Ubuntu 示例：

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

验证：

```bash
docker version
docker compose version
```

---

## 6. 拉取项目

```bash
sudo mkdir -p /opt/project-issue-hub
sudo chown -R $USER:$USER /opt/project-issue-hub
cd /opt/project-issue-hub
git clone https://github.com/Keyiservice/Project-Issue-Hub.git .
cp .env.example .env
```

---

## 7. 正式环境推荐 `.env`

如果宿主机前面要挂 Nginx，建议不要让 Docker 里的 Web 直接占 80 端口。

推荐 `.env` 这样改：

```env
MYSQL_HOST_PORT=3307
REDIS_HOST_PORT=6380
MINIO_API_HOST_PORT=9002
MINIO_CONSOLE_HOST_PORT=9003
BACKEND_HOST_PORT=8081
WEB_HOST_PORT=8088

MINIO_PUBLIC_ENDPOINT=https://files.your-domain.com
REPORT_PUBLIC_BASE_URL=https://opl.your-domain.com

WECHAT_MINIAPP_MOCK_ENABLED=false
WECHAT_MINIAPP_APP_ID=你的AppID
WECHAT_MINIAPP_SECRET=你的小程序Secret
```

说明：

- `WEB_HOST_PORT=8088`
  - 是为了让宿主机 Nginx 占用 `80/443`
- `BACKEND_HOST_PORT=8081`
  - 保持不变，供宿主机 Nginx 反代
- `MINIO_PUBLIC_ENDPOINT`
  - 必须是浏览器和小程序都能访问的正式 HTTPS 地址
- `REPORT_PUBLIC_BASE_URL`
  - Excel Report 里的附件入口会使用这个地址生成

---

## 8. 首次启动

在项目根目录执行：

```bash
cd /opt/project-issue-hub
docker compose up -d --build
```

查看容器状态：

```bash
docker compose ps
```

预期结果：

- `opl-mysql` `Up (healthy)`
- `opl-redis` `Up`
- `opl-minio` `Up`
- `opl-backend` `Up`
- `opl-web` `Up`

---

## 9. 首次验收

部署后按顺序检查：

### 9.1 Web 是否可访问

```bash
curl -I http://127.0.0.1:8088
```

### 9.2 Backend 是否可访问

```bash
curl -I http://127.0.0.1:8081/swagger-ui.html
```

### 9.3 登录接口是否正常

```bash
curl -X POST http://127.0.0.1:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### 9.4 MinIO 控制台是否可访问

```bash
curl -I http://127.0.0.1:9003
```

---

## 10. 宿主机 Nginx 反向代理

推荐域名规划：

| 域名 | 用途 |
|---|---|
| `opl.your-domain.com` | Web 管理端 |
| `api.your-domain.com` | 后端 API |
| `files.your-domain.com` | MinIO 附件访问 |

运维可直接使用示例配置：

- [nginx-project-issue-hub.conf](C:/Users/Administrator/Projects/WeCHAT-OPL-platform/docs/ops/nginx-project-issue-hub.conf)

建议放置位置：

```bash
/etc/nginx/conf.d/project-issue-hub.conf
```

启用后：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

---

## 11. HTTPS 证书

推荐使用：

- 企业正式证书
- 或 Let’s Encrypt

证书就绪后，配置到 Nginx：

```nginx
ssl_certificate     /etc/nginx/ssl/fullchain.pem;
ssl_certificate_key /etc/nginx/ssl/privkey.pem;
```

注意：

- 微信小程序正式版必须使用 HTTPS
- `request` / `uploadFile` / `downloadFile` 域名都必须是 HTTPS

---

## 12. 微信小程序正式上线前动作

### 12.1 后端配置

确认 `.env` 已满足：

```env
WECHAT_MINIAPP_MOCK_ENABLED=false
WECHAT_MINIAPP_APP_ID=真实AppID
WECHAT_MINIAPP_SECRET=真实Secret
```

### 12.2 微信后台配置

在微信公众平台中至少配置：

- `request` 合法域名：`https://api.your-domain.com`
- `uploadFile` 合法域名：`https://api.your-domain.com`
- `downloadFile` 合法域名：`https://api.your-domain.com`

如果附件最终走 `files.your-domain.com`，也要一并配置。

### 12.3 真机联调前检查

- 登录正常
- 项目可选择
- 问题可创建
- 图片 / 视频可上传
- 问题可跟进、待验证、关闭

---

## 13. 小程序发布执行步骤

### 13.1 开发者工具上传

1. 打开 `miniapp/`
2. 确认正式 API 地址已生效
3. 点击“上传”
4. 填写版本号与版本说明

版本说明示例：

- `v0.1.0 首次试运行版`
- `v0.1.1 修复项目统计与附件预览`

### 13.2 微信后台提交审核

1. 登录微信公众平台
2. 进入“版本管理”
3. 选择开发版本
4. 填写审核说明
5. 提交审核

### 13.3 审核通过后发布

1. 发布为线上版本
2. 通知试点项目组开始使用
3. 保留发布记录和版本号

---

## 14. 日常巡检

建议每日执行：

```bash
docker compose ps
docker compose logs --tail=100 backend
docker compose logs --tail=100 web
docker system df
df -h
```

重点检查：

- 容器是否都在 `Up`
- `mysql` 是否仍为 `healthy`
- 磁盘是否接近满
- `backend` 是否有重复重启
- 上传、导出、登录是否正常

---

## 15. 数据备份

### 15.1 MySQL 备份

可直接使用示例脚本：

- [backup-mysql.sh](C:/Users/Administrator/Projects/WeCHAT-OPL-platform/docs/ops/backup-mysql.sh)

手动命令示例：

```bash
docker exec opl-mysql mysqldump -uroot -proot123456 opl_platform > /data/backups/project-issue-hub/opl_platform_$(date +%F_%H%M%S).sql
```

### 15.2 MinIO 备份

建议每日增量备份 MinIO 卷。

至少备份：

- `minio-data`

### 15.3 建议策略

- MySQL：每日备份
- MinIO：每日增量 / 每周全量
- `.env`：每次变更后归档

---

## 16. 升级流程

可直接使用示例脚本：

- [upgrade.sh](C:/Users/Administrator/Projects/WeCHAT-OPL-platform/docs/ops/upgrade.sh)

人工执行步骤：

```bash
cd /opt/project-issue-hub
git pull
docker compose up -d --build
docker compose ps
```

升级后必须验证：

- Web 登录
- 项目问题库列表
- 附件预览
- Excel Report 导出
- 小程序登录与上传

---

## 17. 回滚流程

### 17.1 回滚代码

```bash
cd /opt/project-issue-hub
git checkout <上一个稳定tag>
docker compose up -d --build
```

### 17.2 数据回滚

如果数据库结构变更导致问题：

1. 停止相关容器
2. 恢复上一个 SQL 备份
3. 再重新启动容器

---

## 18. 故障排查速查

### 18.1 Web 打不开

```bash
docker compose ps
docker compose logs --tail=200 web
docker compose logs --tail=200 backend
```

重点看：

- `opl-web` 是否 `Up`
- `opl-backend` 是否重启

### 18.2 登录提示网络异常

重点检查：

- `backend` 是否正常运行
- Nginx 是否把 `/api/` 正常代理
- 后端端口是否变更

### 18.3 附件预览失败

重点检查：

- `MINIO_PUBLIC_ENDPOINT`
- `REPORT_PUBLIC_BASE_URL`
- `files.your-domain.com` 是否可访问

### 18.4 小程序真机无法访问

重点检查：

- 是否还是在用 `127.0.0.1`
- 微信后台合法域名是否配置
- HTTPS 证书是否有效

---

## 19. 推荐运维交付清单

建议最终交付给运维团队的文件包括：

1. 本运维执行版
2. 总体部署指南
3. `.env` 正式版
4. Nginx 正式配置
5. MySQL 备份脚本
6. 升级脚本
7. 发布记录表

