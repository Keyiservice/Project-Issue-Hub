# 非标设备项目现场问题协同系统

## 目标

- 微信原生小程序承担现场轻操作。
- Web 管理端承担重管理、筛选、分析和看板。
- Spring Boot 后端统一业务规则、鉴权、状态机和数据一致性。
- MySQL 作为主数据源，Redis 用于缓存与会话辅助，MinIO 用于图片/视频附件。

## 目录

- `backend`: Java 17 + Spring Boot 3 + MyBatis-Plus 后端服务
- `web-admin`: Vue 3 + TypeScript + Element Plus 管理后台
- `miniapp`: 微信原生小程序
- `database`: MySQL 8 初始化脚本
- `docker`: Docker Compose 与 Nginx 配置
- `docs`: 架构与开发文档

## 核心设计原则

1. 状态流转、编号生成、超期计算和权限判断全部后置到服务端。
2. 前端只承载展示和交互，不承载业务规则本体。
3. 附件存储抽象为统一文件存储接口，当前实现 MinIO，后续可切换 OSS/COS。
4. 所有时间字段使用 ISO 8601，服务端统一 `Asia/Shanghai` 时区。
5. 数据库字段统一 snake_case，接口字段统一 camelCase。

