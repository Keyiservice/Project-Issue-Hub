SET NAMES utf8mb4;
SET time_zone = '+08:00';
USE `opl_platform`;

INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `status`, `sort_no`, `deleted`) VALUES
  (1, 'SITE_USER', '现场人员', '现场录入与跟进', 'ENABLED', 10, 0),
  (2, 'RESP_ENGINEER', '责任工程师', '问题处理与反馈', 'ENABLED', 20, 0),
  (3, 'PROJECT_MANAGER', '项目经理', '项目问题协调', 'ENABLED', 30, 0),
  (4, 'MANAGEMENT', '管理层', '统计与查看', 'ENABLED', 40, 0),
  (5, 'ADMIN', '管理员', '系统管理', 'ENABLED', 50, 0),
  (6, 'MFG_MANAGER', '制造经理', '业务岗位角色', 'ENABLED', 110, 0),
  (7, 'PLANT_MANAGER', '厂长', '业务岗位角色', 'ENABLED', 120, 0),
  (8, 'MECH_DESIGN_SUPERVISOR', '机械设计主管', '业务岗位角色', 'ENABLED', 130, 0),
  (9, 'DESIGN_MANAGER', '设计经理', '业务岗位角色', 'ENABLED', 140, 0),
  (10, 'AUTOMATION_DESIGN_SUPERVISOR', '自动化设计主管', '业务岗位角色', 'ENABLED', 150, 0),
  (11, 'AUTOMATION_ENGINEER', '自动化工程师', '业务岗位角色', 'ENABLED', 160, 0),
  (12, 'MECHANICAL_ENGINEER', '机械工程师', '业务岗位角色', 'ENABLED', 170, 0),
  (13, 'ELECTRICAL_TECHNICIAN', '电气技术员', '业务岗位角色', 'ENABLED', 180, 0),
  (14, 'MECHANICAL_TECHNICIAN', '机械技术员', '业务岗位角色', 'ENABLED', 190, 0),
  (15, 'ME', 'ME', '业务岗位角色', 'ENABLED', 200, 0),
  (16, 'MDA', 'MDA', '业务岗位角色', 'ENABLED', 210, 0),
  (17, 'AE', 'AE', '业务岗位角色', 'ENABLED', 220, 0),
  (18, 'MD_MANAGER', 'MD经理', '业务岗位角色', 'ENABLED', 230, 0),
  (19, 'IE', 'IE', '业务岗位角色', 'ENABLED', 240, 0),
  (20, 'LOGISTICS_SUPERVISOR', '物流主管', '业务岗位角色', 'ENABLED', 250, 0),
  (21, 'PROCUREMENT_MANAGER', '采购经理', '业务岗位角色', 'ENABLED', 260, 0),
  (22, 'FINANCE_MANAGER', '财务经理', '业务岗位角色', 'ENABLED', 270, 0),
  (23, 'HR_MANAGER', '人事经理', '业务岗位角色', 'ENABLED', 280, 0),
  (24, 'QUALITY_MANAGER', '质量经理', '业务岗位角色', 'ENABLED', 290, 0),
  (25, 'QUALITY_ENGINEER', '质量工程师', '业务岗位角色', 'ENABLED', 300, 0),
  (26, 'PLANT_REPRESENTATIVE', '工厂代表', '业务岗位角色', 'ENABLED', 310, 0)
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `description` = VALUES(`description`),
  `status` = VALUES(`status`),
  `sort_no` = VALUES(`sort_no`),
  `deleted` = 0;
