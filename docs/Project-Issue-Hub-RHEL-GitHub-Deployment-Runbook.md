# Project Issue Hub RHEL GitHub Deployment Runbook

This document is the single deployment guide for installing `Project Issue Hub`
on a RHEL server by pulling the project directly from GitHub.

Target scenario:
- Operating system: `RHEL 8/9`
- Deployment path: `/data/project-issue-hub`
- Access mode: intranet Web access
- Runtime mode: Docker Compose

Repository:
- `https://github.com/Keyiservice/Project-Issue-Hub.git`

---

## 1. Recommended Server Specification

Recommended for stable internal use:
- CPU: `8 vCPU`
- Memory: `16 GB`
- Disk: `50 GB` is enough for pilot use, but attachment growth must be monitored
- Network: `1 Gbps` intranet

Notes:
- Your current `/data` disk is `50G`, which is fine for initial deployment.
- Images and videos will consume MinIO storage first, so disk monitoring matters.

---

## 2. Directory Layout

Use `/data/project-issue-hub` as the deployment root:

```bash
mkdir -p /data/project-issue-hub
mkdir -p /data/project-issue-hub/mysql
mkdir -p /data/project-issue-hub/redis
mkdir -p /data/project-issue-hub/minio
mkdir -p /data/project-issue-hub/logs
```

Recommended structure:

```text
/data/project-issue-hub
|- mysql
|- redis
|- minio
|- logs
`- source code
```

---

## 3. Install Docker and Docker Compose

For `RHEL 8/9`:

```bash
sudo dnf -y install yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf -y install docker-ce docker-ce-cli containerd.io docker-compose-plugin git
sudo systemctl enable --now docker
docker --version
docker compose version
git --version
```

If the server is behind a restricted network, make sure Docker can reach the
required image registry first.

---

## 4. Pull the Project from GitHub

Move to `/data` and clone the repository:

```bash
cd /data
git clone https://github.com/Keyiservice/Project-Issue-Hub.git project-issue-hub
cd /data/project-issue-hub
```

If the directory already exists:

```bash
cd /data/project-issue-hub
git pull origin main
```

---

## 5. Configure Environment Variables

Create the runtime environment file:

```bash
cd /data/project-issue-hub
cp .env.example .env
```

Edit `.env` and set at least the following values:

```env
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=yourStrongMysqlPassword
MYSQL_DB=opl_platform

REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=yourStrongRedisPassword

MINIO_ENDPOINT=http://minio:9000
MINIO_PUBLIC_ENDPOINT=http://your-intranet-host:9002
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=yourStrongMinioPassword
MINIO_BUCKET=opl-attachments

JWT_SECRET=replace-with-a-long-random-secret

WECHAT_MINIAPP_MOCK_ENABLED=true
WECHAT_MINIAPP_APP_ID=
WECHAT_MINIAPP_SECRET=

REPORT_PUBLIC_BASE_URL=http://your-intranet-host
```

Notes:
- For intranet Web-only deployment, `WECHAT_MINIAPP_MOCK_ENABLED=true` is acceptable.
- Replace all default passwords before production use.
- `JWT_SECRET` should be long and random.

---

## 6. Confirm Docker Compose Storage Paths

Before first startup, confirm that `docker-compose.yml` uses the intended
persistent data paths or named volumes.

If you want explicit host-path mapping under `/data/project-issue-hub`, make
sure the Compose file maps data to:
- `/data/project-issue-hub/mysql`
- `/data/project-issue-hub/redis`
- `/data/project-issue-hub/minio`
- `/data/project-issue-hub/logs`

If the current Compose file still uses Docker named volumes and you want
host-path persistence instead, update it before deployment.

---

## 7. Start the System

From the project root:

```bash
cd /data/project-issue-hub
docker compose up -d --build
```

This will start:
- `mysql`
- `redis`
- `minio`
- `backend`
- `web`

---

## 8. Verify the Deployment

Check containers:

```bash
docker compose ps
```

Expected services:
- `opl-mysql`
- `opl-redis`
- `opl-minio`
- `opl-backend`
- `opl-web`

Check Web access from the server:

```bash
curl -I http://127.0.0.1
```

Check backend:

```bash
curl -I http://127.0.0.1:8081/api/auth/login
```

Access from a browser in intranet:
- Web: `http://server-ip`
- Backend Swagger: `http://server-ip:8081/swagger-ui.html`

Default admin account:

```text
admin / 123456
```

Current login flow:
1. First login forces password change
2. Strong password is required
3. MFA setup is required

---

## 9. Firewall Recommendation

Open only what is needed for intranet use:

```bash
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=9003/tcp
sudo firewall-cmd --reload
```

Do not expose publicly:
- `3306`
- `6379`
- `9000`
- `9001`

If the Web will be accessed only through Nginx or an internal domain, you may
further narrow the open ports.

---

## 10. Optional Intranet HTTPS

If your company requires HTTPS even for intranet systems, use the prepared
Nginx sample:

- `docs/ops/rhel-nginx-intranet.conf`

Typical setup:
- Web domain: `pih-web.intra.company.com`
- TLS certificate stored under:
  - `/etc/pki/tls/certs/pih-web.crt`
  - `/etc/pki/tls/private/pih-web.key`

Install Nginx:

```bash
sudo dnf -y install nginx
sudo cp /data/project-issue-hub/docs/ops/rhel-nginx-intranet.conf /etc/nginx/conf.d/project-issue-hub.conf
sudo nginx -t
sudo systemctl enable --now nginx
```

---

## 11. Enable Auto-Start After Server Reboot

Prepared systemd unit:
- `docs/ops/project-issue-hub.service`

Install it:

```bash
sudo cp /data/project-issue-hub/docs/ops/project-issue-hub.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable project-issue-hub
sudo systemctl start project-issue-hub
```

Check service status:

```bash
systemctl status project-issue-hub
```

This service will run:

```bash
docker compose up -d
```

after the server restarts.

Additional reference:
- `docs/ops/rhel-autostart.md`

---

## 12. Daily Operations

Useful commands:

```bash
cd /data/project-issue-hub

docker compose ps
docker compose logs -f backend
docker compose logs -f web
docker compose restart backend web
docker compose down
docker compose up -d
```

For routine checks:
- `docs/ops/rhel-ops-checklist.md`

---

## 13. Update Procedure

To update the system from GitHub:

```bash
cd /data/project-issue-hub
git pull origin main
docker compose up -d --build
```

Recommended before update:
1. Back up MySQL
2. Back up MinIO attachment data
3. Review `.env` for local overrides

---

## 14. Backup Recommendation

Minimum recommendation:
- MySQL: daily backup
- MinIO: regular snapshot or directory backup

Suggested retention:
- Daily backups: `7-14 days`
- Weekly backups: `4 weeks`

If attachments are large, MinIO backup should be planned separately from
database backup.

---

## 15. Common Troubleshooting

### Web cannot open

```bash
docker compose ps
docker compose logs --tail=200 web
```

### Login fails

```bash
docker compose logs --tail=200 backend
```

Check:
- MySQL connection
- Redis connection
- initial password flow
- MFA requirement

### File upload fails

Check:
- MinIO container status
- available disk space on `/data`
- Nginx `client_max_body_size`

### Containers do not restart after reboot

Check:

```bash
systemctl status docker
systemctl status project-issue-hub
```

---

## 16. Final Recommendation

For your current server, this is the recommended deployment shape:
- Deploy all services under `/data/project-issue-hub`
- Keep the whole system intranet-only
- Use `docker compose` for runtime
- Use `systemd` for reboot auto-start
- Add intranet HTTPS later if company policy requires it

This gives you the shortest path from GitHub to a stable internal system.
