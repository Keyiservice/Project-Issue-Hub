# RHEL 服务器重启后自动运行（Project Issue Hub）

## 1. 准备 systemd 服务文件
已提供模板：
`docs/ops/project-issue-hub.service`

确保 WorkingDirectory 指向实际部署目录，例如：
```
/opt/project-issue-hub
```

## 2. 安装并启用
```bash
sudo cp /opt/project-issue-hub/docs/ops/project-issue-hub.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable project-issue-hub
sudo systemctl start project-issue-hub
```

## 3. 验证
```bash
systemctl status project-issue-hub
docker compose ps
```

## 4. 常用命令
```bash
sudo systemctl restart project-issue-hub
sudo systemctl stop project-issue-hub
sudo systemctl disable project-issue-hub
```

说明：
- 该服务依赖 Docker 服务，确保 `docker.service` 已启用并在开机自启。
- 如需开机等待网络，可在 service 文件中加 `After=network-online.target`。
