# Project Issue Hub RHEL 部署指南

## 1. 目标
在单台 RHEL 服务器上部署 Web + Backend + MySQL + Redis + MinIO，内网访问。

## 2. 服务器要求
- RHEL 8/9（RHEL 7 也可）
- 8 vCPU / 16 GB RAM / 500 GB SSD（推荐）
- 内网 1 Gbps

## 3. 安装 Docker & Compose
RHEL 8/9：
```bash
sudo dnf -y install yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf -y install docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable --now docker
docker compose version
```

RHEL 7：
```bash
sudo yum -y install yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum -y install docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable --now docker
```

## 4. 上传项目
假设部署目录：
```
/opt/project-issue-hub
```

## 5. 配置 .env
```bash
cp .env.example .env
```
建议配置：
```env
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=yourStrongPass
MYSQL_DB=opl_platform

REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=yourRedisPass

MINIO_ENDPOINT=http://minio:9000
MINIO_PUBLIC_ENDPOINT=http://intranet-host:9002

WECHAT_MINIAPP_MOCK_ENABLED=true
```

## 6. 启动服务
```bash
docker compose up -d --build
```

## 7. 验证
- Web：`http://内网IP`
- Backend：`http://内网IP:8081`

默认账号：
```
admin / 123456
```

## 8. 内网 HTTPS（可选）
使用 `docs/ops/rhel-nginx-intranet.conf`：
- 先准备内网证书与域名
- Nginx 反代 Web 与 Backend

## 9. 服务器重启自动运行
使用 systemd 方式，确保重启后自动拉起：
```bash
sudo cp /opt/project-issue-hub/docs/ops/project-issue-hub.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable project-issue-hub
sudo systemctl start project-issue-hub
```
说明文档：
`docs/ops/rhel-autostart.md`

## 10. systemd 一键启停（可选）
```bash
sudo cp docs/ops/project-issue-hub.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now project-issue-hub
```

## 11. 运维检查
参考：`docs/ops/rhel-ops-checklist.md`
