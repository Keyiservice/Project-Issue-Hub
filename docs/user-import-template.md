# 用户 Excel 导入模板说明

模板文件：
- [user-import-template.csv](C:/Users/Administrator/Projects/WeCHAT-OPL-platform/docs/user-import-template.csv)
- [user-import-template.xlsx](C:/Users/Administrator/Projects/WeCHAT-OPL-platform/docs/user-import-template.xlsx)

字段顺序固定如下：
1. `账号`
2. `姓名`
3. `部门编码`
4. `部门名称`
5. `角色代码`
6. `手机号`
7. `邮箱`

字段规则：
- `账号`：必填，企业唯一登录账号，建议使用工号或英文账号。
- `姓名`：必填，真实姓名。
- `部门编码`：选填，例如 `FS21_ENG`。
- `部门名称`：选填，例如 `FS21 Engineering`。
- `角色代码`：必填，多个角色用 `|` 分隔。
- `手机号`：选填。
- `邮箱`：选填。

角色代码分两类：

权限角色：
- `SITE_USER`
- `RESP_ENGINEER`
- `PROJECT_MANAGER`
- `MANAGEMENT`
- `ADMIN`

岗位角色：
- `MFG_MANAGER`
- `PLANT_MANAGER`
- `MECH_DESIGN_SUPERVISOR`
- `DESIGN_MANAGER`
- `AUTOMATION_DESIGN_SUPERVISOR`
- `AUTOMATION_ENGINEER`
- `MECHANICAL_ENGINEER`
- `ELECTRICAL_TECHNICIAN`
- `MECHANICAL_TECHNICIAN`
- `ME`
- `MDA`
- `AE`
- `MD_MANAGER`
- `IE`
- `LOGISTICS_SUPERVISOR`
- `PROCUREMENT_MANAGER`
- `FINANCE_MANAGER`
- `HR_MANAGER`
- `QUALITY_MANAGER`
- `QUALITY_ENGINEER`
- `PLANT_REPRESENTATIVE`

导入约束：
- 可以只填岗位角色，系统会自动补基础权限 `SITE_USER`。
- 也可以显式指定权限角色再叠加岗位角色。
- 例如：`PROJECT_MANAGER|QUALITY_MANAGER`、`RESP_ENGINEER|MECHANICAL_ENGINEER`、`QUALITY_MANAGER`

导入注意：
- 第一行可以保留表头，系统会自动跳过。
- 如果从 Excel 复制到 Web 导入框，可以直接整表复制。
- 初始密码在导入弹窗里统一设置；留空则默认为 `123456`。
