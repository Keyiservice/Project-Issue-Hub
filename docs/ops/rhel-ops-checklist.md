# Project Issue Hub 运维巡检清单（RHEL）

## 每日检查
- `docker compose ps` 服务全部为 `Up` 且 `healthy`
- `curl -I http://127.0.0.1` 返回 `200`
- `curl -I http://127.0.0.1:8081/api/auth/login` 返回 `405` 或 `401`（说明服务在线）
- `docker compose logs --tail=200 backend` 无连续报错

## 每周检查
- MySQL 备份是否生成（建议每天一次，保留 7~14 天）
- MinIO 备份是否生成（或至少验证 bucket 可读写）
- 磁盘使用率 < 80%
- Redis 内存使用率 < 80%

## 每月检查
- SSL 证书有效期（若启用内网 HTTPS）
- 用户权限和角色抽查
- 项目团队成员与实际组织是否一致
- 容器镜像与依赖更新评估

## 异常排查
- Web 无法访问
  - `docker compose ps`
  - `docker compose logs --tail=200 web`
- 登录失败
  - `docker compose logs --tail=200 backend`
  - 检查 `MYSQL_HOST/REDIS_HOST` 配置
- 附件上传失败
  - `docker compose logs --tail=200 backend`
  - 确认 `minio` 服务正常
  - 检查 `client_max_body_size`

## 建议的备份
- MySQL：全量备份 + 每日备份
- MinIO：定期快照或对象存储同步

## 建议的监控指标
- Backend 5xx 错误率
- MySQL 连接数
- Redis 内存
- MinIO 存储占用
- 磁盘使用率
