# 项目成员导入模板说明

模板文件：
- [project-member-import-template.csv](C:/Users/Administrator/Projects/WeCHAT-OPL-platform/docs/project-member-import-template.csv)
- [project-member-import-template.xlsx](C:/Users/Administrator/Projects/WeCHAT-OPL-platform/docs/project-member-import-template.xlsx)

字段顺序固定如下：
1. `账号`
2. `项目岗位代码`
3. `是否项目经理`
4. `可分派`
5. `可验证`
6. `可关闭`
7. `排序`
8. `备注`

字段规则：
- `账号`：必填，必须是系统里已经存在且启用的企业账号。
- `项目岗位代码`：必填，可用值：
  - `PROJECT_MANAGER`
  - `MECH_ENGINEER`
  - `ELECTRICAL_TECH`
  - `AUTOMATION_ENGINEER`
  - `INSTALL_TECH`
  - `MANUFACTURING_MANAGER`
  - `DESIGN_MANAGER`
  - `QUALITY_ENGINEER`
  - `PROCESS_ENGINEER`
- `是否项目经理`：选填，支持 `Y/N`、`是/否`、`1/0`。
- `可分派`：选填，支持 `Y/N`、`是/否`、`1/0`。
- `可验证`：选填，支持 `Y/N`、`是/否`、`1/0`。
- `可关闭`：选填，支持 `Y/N`、`是/否`、`1/0`。
- `排序`：选填，整数，越小越靠前。
- `备注`：选填。

导入说明：
- 当前 Web 页面支持把 Excel 整表复制后直接粘贴到“项目团队 -> 批量导入”里。
- 同一个账号如果已经在项目团队中，导入时会执行更新，不会重复新增。
- 如果某一行标记为项目经理，该行成员会自动成为项目经理，并同步更新项目主表中的项目经理信息。
