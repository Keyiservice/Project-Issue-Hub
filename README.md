# Project Issue Hub

非标设备项目现场问题协同系统。

这是一个面向企业现场的项目问题闭环平台，包含：

- 微信原生小程序：现场人员快速录入、跟进、拍照/视频上传
- Web 管理后台：项目经理和管理层按项目查看问题库、详情、统计和报表
- Spring Boot 后端：统一鉴权、状态流转、附件、统计、导出
- MySQL / Redis / MinIO：业务数据、缓存、对象存储

系统名称已统一为 `Project Issue Hub`。

## 1. 解决的问题

传统 Excel Open Issue List 在现场场景里有几个典型问题：

- 只有文字，没有现场图片和视频，问题不够直观
- 问题流转、责任归属、关闭验证不透明
- 管理层无法快速看懂“当前处理到哪一步、谁在负责、下一步做什么”
- 现场和管理层之间来回追问，沟通成本高
- 缺少按项目、按责任人、按状态的统计与导出能力

`Project Issue Hub` 的目标是把这些问题收口为一个真正可落地使用的企业现场系统。

## 2. 核心能力

### 2.1 小程序

- 按项目进入问题库，不同项目问题不混在一起
- 现场快速录入问题，自动带录入人和录入时间
- 支持图片、视频上传
- 查看我的问题、待我处理
- 问题详情查看、提交跟进、提交关闭证据
- 当前推进逻辑简化为：
  - `NEW -> IN_PROGRESS -> PENDING_VERIFY -> CLOSED`

### 2.2 Web 管理端

- 项目问题库按项目进入
- 问题列表筛选、详情查看、责任分派、优先级调整
- 问题创建后可继续补充图片和视频
- 附件预览支持图片和视频
- 项目统计和管理驾驶舱支持按项目切换
- 可导出项目问题 Excel Report

### 2.3 后端

- JWT 鉴权
- RBAC 权限模型
- 统一异常处理、统一返回结构
- 问题状态机和流转规则统一在后端
- MinIO 附件上传和预览
- 项目问题统计、Dashboard、Excel 报表导出

## 3. 技术栈

### 3.1 小程序端

- 微信原生小程序

### 3.2 Web 端

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts

### 3.3 后端

- Java 17
- Spring Boot 3
- Spring Web
- Spring Validation
- Spring Security
- JWT
- MyBatis-Plus
- MySQL 8
- Redis
- MinIO
- Swagger / OpenAPI

### 3.4 部署

- Docker Compose

## 4. 目录结构

```text
.
├─ backend/       Spring Boot 后端
├─ web-admin/     Vue3 管理后台
├─ miniapp/       微信原生小程序
├─ database/      MySQL 初始化脚本
├─ docker/        Nginx 等辅助配置
├─ docs/          架构、模板、PPT、截图资源
├─ docker-compose.yml
└─ README.md
```

## 5. 本地启动

### 5.1 前置条件

- Docker Desktop
- 微信开发者工具

### 5.2 一键启动

在仓库根目录执行：

```powershell
docker compose up -d --build
```

默认会启动：

- `mysql`
- `redis`
- `minio`
- `backend`
- `web`

### 5.3 默认访问地址

- Web 管理后台：`http://localhost`
- 后端接口：`http://localhost:8081`
- Swagger：`http://localhost:8081/swagger-ui.html`
- MinIO Console：`http://localhost:9003`

### 5.4 默认账号

初始化后可使用：

- 用户名：`admin`
- 密码：`123456`

系统内已带一套示例项目和问题数据，可直接联调与演示。

## 6. 小程序联调说明

开发阶段当前使用本地接口地址，通常需要在微信开发者工具中：

- 关闭合法域名校验
- 清除编译缓存后重新编译

真机或正式发布前，需要将接口切换到可访问的 HTTPS 域名，并配置微信后台合法域名。

## 7. 关键业务规则

### 7.1 问题流转

当前已简化为：

- `NEW`：新建
- `IN_PROGRESS`：处理中
- `PENDING_VERIFY`：待验证
- `CLOSED`：已关闭
- `ON_HOLD`：已挂起
- `CANCELED`：已取消

说明：

- 新问题不再单独走“已受理”阶段
- 有责任人后可直接从 `NEW` 进入 `IN_PROGRESS`
- 历史 `ACCEPTED` 数据已统一并入 `IN_PROGRESS`

### 7.2 闭环要求

- 提交待验证前必须填写处理说明
- 挂起必须填写挂起原因
- 关闭时记录关闭人、关闭时间
- 关闭证据可通过文本、图片、视频体现

### 7.3 项目维度管理

- 每个问题必须属于某个项目
- Web 和小程序都先选项目，再进入问题库
- 责任人分派仅允许当前项目团队成员

## 8. 当前已完成的主要功能

- 用户体系、角色、微信绑定登录
- 项目管理、项目团队管理
- 问题创建、问题详情、评论跟进
- 图片/视频附件上传、补充、删除、预览
- 问题状态流转和操作日志
- Web 统计分析、驾驶舱、项目级统计
- 项目问题库 Excel 报表导出
- 小程序项目工作台、项目问题库、详情推进

## 9. 报表导出

项目问题库支持导出 Excel Report。

当前导出内容包含：

- `报表封面`
- `项目概览`
- `统计图表`
- `问题清单`

其中问题清单已支持：

- 标题后紧跟问题描述
- 附件数量
- 附件入口超链接

## 10. 文档与附加材料

更多材料见：

- 架构说明：[docs/architecture.md](docs/architecture.md)
- 用户导入模板：[docs/user-import-template.xlsx](docs/user-import-template.xlsx)
- 项目成员导入模板：[docs/project-member-import-template.xlsx](docs/project-member-import-template.xlsx)
- 系统介绍与汇报材料：`docs/` 目录下多份 PPT

## 11. 版本

当前仓库首个标签版本为：

- `v0.1.0`

## 12. 后续建议

- 增加 `README` 中的系统截图
- 补充正式部署文档
- 增加 API 权限矩阵说明
- 将 Web 与后端发布流程整理成 CI/CD

