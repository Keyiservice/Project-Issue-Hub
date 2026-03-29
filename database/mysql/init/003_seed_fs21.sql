SET NAMES utf8mb4;
SET time_zone = '+08:00';
USE `opl_platform`;

-- Cleanup demo project and re-import FS21 issue library.
DELETE io FROM `issue_operation_log` io JOIN `issue` i ON io.issue_id = i.id JOIN `project` p ON i.project_id = p.id WHERE p.project_no IN ('PRJ-202603-001', 'FS21');
DELETE ic FROM `issue_comment` ic JOIN `issue` i ON ic.issue_id = i.id JOIN `project` p ON i.project_id = p.id WHERE p.project_no IN ('PRJ-202603-001', 'FS21');
DELETE ia FROM `issue_attachment` ia JOIN `issue` i ON ia.issue_id = i.id JOIN `project` p ON i.project_id = p.id WHERE p.project_no IN ('PRJ-202603-001', 'FS21');
DELETE i FROM `issue` i JOIN `project` p ON i.project_id = p.id WHERE p.project_no IN ('PRJ-202603-001', 'FS21');
DELETE FROM `project` WHERE project_no IN ('PRJ-202603-001', 'FS21');

-- Seed users needed by FS21 issue ownership.
INSERT INTO `sys_user` (`user_no`, `username`, `password_hash`, `real_name`, `department_code`, `department_name`, `status`) VALUES
  ('U0001', 'admin', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'System Admin', 'IT', 'IT', 'ENABLED'),
  ('U0002', 'pm01', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Project Manager', 'PM', 'Project Management', 'ENABLED'),
  ('U0101', 'zhang.jiying', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', 'ENABLED'),
  ('U0102', 'zhang.peng', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', 'ENABLED'),
  ('U0103', 'xuchu.liu', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', 'ENABLED'),
  ('U0104', 'baibaopeng', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', 'ENABLED'),
  ('U0105', 'lan.qian', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Lan Qian', 'FS21_ENG', 'FS21 Engineering', 'ENABLED'),
  ('U0106', 'haichao.bai', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Haichao Bai', 'FS21_ENG', 'FS21 Engineering', 'ENABLED'),
  ('U0107', 'jason.liu', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Jason Liu', 'FS21_ENG', 'FS21 Engineering', 'ENABLED'),
  ('U0108', 'duan.wang', '$2a$10$F2uDddvJb9YUYA3Gg1BS0.5Qu0VX1dLPWbw7E4bMnoQTCFEmqLI1e', 'Duan Wang', 'FS21_ENG', 'FS21 Engineering', 'ENABLED')
ON DUPLICATE KEY UPDATE
  `password_hash` = VALUES(`password_hash`),
  `real_name` = VALUES(`real_name`),
  `department_code` = VALUES(`department_code`),
  `department_name` = VALUES(`department_name`),
  `status` = 'ENABLED',
  `deleted` = 0;

SET @admin_id = (SELECT id FROM `sys_user` WHERE username = 'admin' LIMIT 1);
SET @pm_id = (SELECT id FROM `sys_user` WHERE username = 'pm01' LIMIT 1);
SET @pm_name = (SELECT real_name FROM `sys_user` WHERE username = 'pm01' LIMIT 1);
SET @zhang_jiying_id = (SELECT id FROM `sys_user` WHERE username = 'zhang.jiying' LIMIT 1);
SET @zhang_peng_id = (SELECT id FROM `sys_user` WHERE username = 'zhang.peng' LIMIT 1);
SET @xuchu_liu_id = (SELECT id FROM `sys_user` WHERE username = 'xuchu.liu' LIMIT 1);
SET @baibaopeng_id = (SELECT id FROM `sys_user` WHERE username = 'baibaopeng' LIMIT 1);
SET @lan_qian_id = (SELECT id FROM `sys_user` WHERE username = 'lan.qian' LIMIT 1);
SET @haichao_bai_id = (SELECT id FROM `sys_user` WHERE username = 'haichao.bai' LIMIT 1);
SET @jason_liu_id = (SELECT id FROM `sys_user` WHERE username = 'jason.liu' LIMIT 1);
SET @duan_wang_id = (SELECT id FROM `sys_user` WHERE username = 'duan.wang' LIMIT 1);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT id, 5 FROM `sys_user` WHERE username = 'admin'
UNION ALL
SELECT id, 3 FROM `sys_user` WHERE username = 'pm01'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'zhang.jiying'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'zhang.peng'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'xuchu.liu'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'baibaopeng'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'lan.qian'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'haichao.bai'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'jason.liu'
UNION ALL
SELECT id, 2 FROM `sys_user` WHERE username = 'duan.wang';

INSERT INTO `project` (`project_no`, `project_name`, `customer_name`, `project_manager_id`, `project_manager_name`, `status`, `start_date`, `planned_end_date`, `description`, `created_by`, `updated_by`) VALUES
('FS21', 'FS21', 'ICE', @pm_id, @pm_name, 'IN_PROGRESS', '2026-01-06', '2026-01-30', 'Imported from OPL-FS21 ICE 20260106.xls', @pm_id, @pm_id)
ON DUPLICATE KEY UPDATE
  `project_name` = VALUES(`project_name`),
  `customer_name` = VALUES(`customer_name`),
  `project_manager_id` = VALUES(`project_manager_id`),
  `project_manager_name` = VALUES(`project_manager_name`),
  `status` = VALUES(`status`),
  `start_date` = VALUES(`start_date`),
  `planned_end_date` = VALUES(`planned_end_date`),
  `description` = VALUES(`description`),
  `updated_by` = VALUES(`updated_by`),
  `deleted` = 0;

SET @fs21_project_id = (SELECT id FROM `project` WHERE project_no = 'FS21' LIMIT 1);

INSERT INTO `issue` (
  `issue_no`, `title`, `description`, `opl_no`, `action_plan`, `pilot_name`, `legacy_status`, `legacy_comment`, `finish_date`, `finish_evidence`,
  `project_id`, `project_name`, `category_code`, `source_code`, `priority`, `status`, `impact_level`,
  `affect_shipment`, `affect_commissioning`, `need_shutdown`, `reporter_id`, `reporter_name`,
  `owner_id`, `owner_name`, `owner_department_code`, `owner_department_name`,
  `occurred_at`, `due_at`, `closed_at`, `close_reason`, `current_handler_id`, `current_handler_name`, `last_follow_up_at`, `version`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`
) VALUES
  ('FS21-OPL-001', 'GLE DS station的入口和出口衔接部位整改\n进口未固定，出口有一个大口子属于安全项', 'GLE DS station的入口和出口衔接部位整改\n进口未固定，出口有一个大口子属于安全项', '1', '本地供应商加工；', 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-002', '现场止挡和其他部件紧固', '现场止挡和其他部件紧固', '2', '现场完成全面检查和紧固', 'Zhang peng', 'Done', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'LOW', 'CLOSED', 'LOW', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', NULL, '2026-01-06 18:00:00', 'Imported from FS21 OPL marked as Done', @zhang_peng_id, 'Zhang Peng', '2026-01-06 18:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 18:00:00', 0),
  ('FS21-OPL-003', 'GLE JIG台面高度调整', 'GLE JIG台面高度调整', '3', '调整高度', 'Lan Qian', 'Done', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'LOW', 'CLOSED', 'LOW', 0, 0, 0, @pm_id, @pm_name, @lan_qian_id, 'Lan Qian', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', NULL, '2026-01-06 18:00:00', 'Imported from FS21 OPL marked as Done', @lan_qian_id, 'Lan Qian', '2026-01-06 18:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 18:00:00', 0),
  ('FS21-OPL-004', 'layout布局按照设计落位', 'layout布局按照设计落位', '4', '调整设备位置', 'Baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'MEDIUM', 'IN_PROGRESS', 'MEDIUM', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', NULL, NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-005', 'DS站油箱进站检测传感器恢复', 'DS站油箱进站检测传感器恢复', '5', '传感器支架及反光板安装并恢复程序检测', 'Zhang peng', 'On going', '找采购下单', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-006', 'OP20 nest harting 按照现场插头适配', 'OP20 nest harting 按照现场插头适配', '6', '更改FS21 nest harting 公头', 'Lan Qian', 'Done', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'LOW', 'CLOSED', 'LOW', 0, 0, 0, @pm_id, @pm_name, @lan_qian_id, 'Lan Qian', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', NULL, '2026-01-06 18:00:00', 'Imported from FS21 OPL marked as Done', @lan_qian_id, 'Lan Qian', '2026-01-06 18:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 18:00:00', 0),
  ('FS21-OPL-007', 'DS站摧毁单元保护罩和JIG顶升路径干涉(ST30 JIG2)', 'DS站摧毁单元保护罩和JIG顶升路径干涉(ST30 JIG2)', '7', '新做安装板', 'Zhang Jiying', 'On going', '机械出图，', NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-008', 'ST30 物料盒突出设备Jig太多，操作员从左边Jig做完换到右边Jig不顺畅', 'ST30 物料盒突出设备Jig太多，操作员从左边Jig做完换到右边Jig不顺畅', '8', '待观察', 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-009', 'ST30 进料口承接平台是一个大铁片，容易划伤操作员的手，需要加保护', 'ST30 进料口承接平台是一个大铁片，容易划伤操作员的手，需要加保护', '9', '加尼龙防护', 'Zhang peng', 'On going', '现场整改', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-010', 'ST30 进料口和ST20出料台是错位的，进口有一个大的开口', 'ST30 进料口和ST20出料台是错位的，进口有一个大的开口', '10', '待确认', 'Baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-011', 'ST30出料口和装配线前后错位375mm', 'ST30出料口和装配线前后错位375mm', '11', '待确认', 'Baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-012', 'ST10  FSU 开孔尺寸大，前墙太薄＜1mm', 'ST10  FSU 开孔尺寸大，前墙太薄＜1mm', '12', '需要换垫刀块,需要新制15.4垫刀块用于换型--含15.4刀座新作一套（haipan确认）', 'Zhang Jiying', 'On going', '设计出图纸，采购', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-013', 'ST10 ICV 开孔尺寸大，开孔有拉丝现象', 'ST10 ICV 开孔尺寸大，开孔有拉丝现象', '13', '待观察', 'Baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-014', 'ST20  ICV夹爪光电开关走线和夹爪打开关闭接触，光纤易被夹爪夹到损坏 需优化', 'ST20  ICV夹爪光电开关走线和夹爪打开关闭接触，光纤易被夹爪夹到损坏 需优化', '14', '现场调整', 'Xuchu Liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-015', 'ST20 JIG 中间位缓冲器未起作用，每次到位是明显的金属碰撞声', 'ST20 JIG 中间位缓冲器未起作用，每次到位是明显的金属碰撞声', '15', '现场借了工厂两个M36*2的缓冲器，需要购买归还baopeng', 'Zhang peng', 'On going', '找采购沟通确认', NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-016', 'ST10/20扫码未完成，未启用', 'ST10/20扫码未完成，未启用', '16', NULL, 'Lan Qian', 'Done', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'LOW', 'CLOSED', 'LOW', 0, 0, 0, @pm_id, @pm_name, @lan_qian_id, 'Lan Qian', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', NULL, '2026-01-06 18:00:00', 'Imported from FS21 OPL marked as Done', @lan_qian_id, 'Lan Qian', '2026-01-06 18:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 18:00:00', 0),
  ('FS21-OPL-017', 'ST20 GLE换型改造部分未开始，未验证', 'ST20 GLE换型改造部分未开始，未验证', '17', '已验证,ST20有干涉，同54条', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-018', 'ST10 FLVV 爪子破损', 'ST10 FLVV 爪子破损', '18', '现场找维修借了3pcs，需要归还给baopeng', 'Zhang peng', 'On going', '采购下单', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-019', 'ST30 扫码换型需要换型，目前不能实现，两个版本的安装板不一样', 'ST30 扫码换型需要换型，目前不能实现，两个版本的安装板不一样', '19', 'RCH 按我们的要求制作加工件替换，jiying提供图纸', 'Jason/Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, NULL, 'Jason/Jiying', NULL, NULL, '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, NULL, 'Jason/Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-020', 'ST30 扫码  ， JIG2  FS21目前是临时绑在上面，需要改善设计', 'ST30 扫码  ， JIG2  FS21目前是临时绑在上面，需要改善设计', '20', '光杆延长设计出图纸', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-021', 'ST10 需要等高螺丝,目前FS21刀座没有,需要拆GLE的螺丝', 'ST10 需要等高螺丝,目前FS21刀座没有,需要拆GLE的螺丝', '21', '等高螺丝', 'Xuchu Liu', 'On going', '带到现场', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-022', 'ST10 FS21存放位缺传感器,设计2个现场只安装了1个', 'ST10 FS21存放位缺传感器,设计2个现场只安装了1个', '22', '接近开关，支架下单', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-023', '2小时不操作自动降温功能没有', '2小时不操作自动降温功能没有', '23', '需要增加此功能', 'haichao Bai', 'On going', '加程序', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @haichao_bai_id, 'Haichao Bai', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @haichao_bai_id, 'Haichao Bai', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-024', '换型部分标签不清晰', '换型部分标签不清晰', '24', '设计换型标牌', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-025', 'ST20 需要增加专门的Jig放置架', 'ST20 需要增加专门的Jig放置架', '25', '增加Jig放置架', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-026', 'ST20 Nest滑台上方护板需要更换', 'ST20 Nest滑台上方护板需要更换', '26', '当前切了护栏网片', 'Xuchu Liu', 'On going', '现场整改', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-027', 'UPH ST10，ST20,ST30不达标，需要提升设备UPH到72S（目前ST10/20-39 ST 30 -36）', 'UPH ST10，ST20,ST30不达标，需要提升设备UPH到72S（目前ST10/20-39 ST 30 -36）', '27', '提升设备UPH', 'haichao Bai', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @haichao_bai_id, 'Haichao Bai', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @haichao_bai_id, 'Haichao Bai', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-028', 'ST10 刀放置架需要增加固定标签', 'ST10 刀放置架需要增加固定标签', '28', '设计换型标牌', 'Zhang Jiying', 'On going', '同25', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-029', 'ST10 机器人换型机器人速度太慢', 'ST10 机器人换型机器人速度太慢', '29', NULL, NULL, 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'MEDIUM', 'IN_PROGRESS', 'MEDIUM', 0, 0, 0, @pm_id, @pm_name, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', NULL, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-030', 'ST20 JIG（压紧臂）太重  要减重', 'ST20 JIG（压紧臂）太重  要减重', '30', '1/15确认方案', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-031', 'ST20 油箱取出不方便，定位阻挡需要抬油箱', 'ST20 油箱取出不方便，定位阻挡需要抬油箱', '31', NULL, 'Zhang Jiying', 'On going', '和jiying确认', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-032', 'ST20  harting  热电偶没标识', 'ST20  harting  热电偶没标识', '32', '现场增加', 'xuchu liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-033', 'ST20 二站换型 摧毁单元和条码枪有碰伤人的风险，且摧毁单元不起作用', 'ST20 二站换型 摧毁单元和条码枪有碰伤人的风险，且摧毁单元不起作用', '33', '重新出图纸下单，买细光杆', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-034', 'ST20 底部 JIG重，一个人取出比较麻烦', 'ST20 底部 JIG重，一个人取出比较麻烦', '34', '建议改成2个人换型(ST30完成后过来支持)', 'Zhang Jiying', 'On going', '更新换型说明', NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-035', 'ST20  换型模式JIG台子移动到中间换型', 'ST20  换型模式JIG台子移动到中间换型', '35', '已修改，待确认', 'Baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-036', 'ST30   harting 标识', 'ST30   harting 标识', '36', '现场增加', 'Xuchu Liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-037', 'ST 30  小车后面做harting存放盒,且考虑线缆悬挂', 'ST 30  小车后面做harting存放盒,且考虑线缆悬挂', '37', '设计出图纸', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-038', 'ST 30  换型小车需要做标识，标示 ST1（JIG1）或 ST2 （JIG2）', 'ST 30  换型小车需要做标识，标示 ST1（JIG1）或 ST2 （JIG2）', '38', '设计出图纸', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-039', 'ST30  插锁导向位置上下空间小，地牛高低不好控制', 'ST30  插锁导向位置上下空间小，地牛高低不好控制', '39', '改变操作流程', 'Baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-040', 'ST 30 小车做拉动把手，台面划手', 'ST 30 小车做拉动把手，台面划手', '40', '四个小车加把手（GLE&FS21）', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-041', 'ST30  小车太重两个人推不动  需要三个人推', 'ST30  小车太重两个人推不动  需要三个人推', '41', '双人可换型，已验证；代工厂确认', 'baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-042', 'ST 30 左侧 按钮盒 换型容易撞', 'ST 30 左侧 按钮盒 换型容易撞', '42', '设计出图纸，加保护罩', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-043', 'ST30 .触摸屏下降会碰急停', 'ST30 .触摸屏下降会碰急停', '43', '滑块上增加Stopper', 'Xuchu Liu', 'On going', '采购下单', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-044', 'ST30   ST3  手动取件不起作用，手动按钮太远', 'ST30   ST3  手动取件不起作用，手动按钮太远', '44', '确认是哪个按钮', 'Jason Liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @jason_liu_id, 'Jason Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @jason_liu_id, 'Jason Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-045', 'ST 30 焊板防热罩缺失', 'ST 30 焊板防热罩缺失', '45', NULL, NULL, 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'MEDIUM', 'IN_PROGRESS', 'MEDIUM', 0, 0, 0, @pm_id, @pm_name, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', NULL, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-046', 'ST30 设备一致性不满足  29日出现一个FLVV无翻边  30日出现一个无翻边边', 'ST30 设备一致性不满足  29日出现一个FLVV无翻边  30日出现一个无翻边边', '46', '待CMK结果', 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-047', 'ST20 与ST30 转接口 仅靠木棍支撑', 'ST20 与ST30 转接口 仅靠木棍支撑', '47', '同第一条', 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-048', 'ST30  无台面面板', 'ST30  无台面面板', '48', '先和工厂沟通', 'Duan Wang', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @duan_wang_id, 'Duan Wang', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @duan_wang_id, 'Duan Wang', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-049', 'ST30  光栅用螺丝侧面顶住固定  固定方式不稳定', 'ST30  光栅用螺丝侧面顶住固定  固定方式不稳定', '49', NULL, NULL, 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'MEDIUM', 'IN_PROGRESS', 'MEDIUM', 0, 0, 0, @pm_id, @pm_name, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', NULL, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-050', 'ST30  光栅线路裸露于设备外，螺丝孔位未对上', 'ST30  光栅线路裸露于设备外，螺丝孔位未对上', '50', '需要现场捆扎', 'Xuchu liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-051', 'ST10 开孔双刀  FLVV ICV 均有', 'ST10 开孔双刀  FLVV ICV 均有', '51', '待观察', 'Baibaopeng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @baibaopeng_id, 'Baibaopeng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @baibaopeng_id, 'Baibaopeng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-052', 'ST10  同样的Ering  FSU需要换型，调整为FSU不需要换型', 'ST10  同样的Ering  FSU需要换型，调整为FSU不需要换型', '52', '与上面问题重合，删除', NULL, 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'MEDIUM', 'IN_PROGRESS', 'MEDIUM', 0, 0, 0, @pm_id, @pm_name, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', NULL, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-053', 'ST20  到ST30  导杆划油箱', 'ST20  到ST30  导杆划油箱', '53', '导杆调整高度,端部用防撞条包裹(推荐聚氨酯材料)', 'Xuchu Liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-054', 'ST20  GLE 换型 JIG干涉，未做细节调整仅做了干涉验证', 'ST20  GLE 换型 JIG干涉，未做细节调整仅做了干涉验证', '54', '后续需要做细节调整', 'Zhang Jiying', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_jiying_id, 'Zhang Jiying', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_jiying_id, 'Zhang Jiying', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-055', 'ST20  GLE 换型  焊接质量未调整，需以质量报告为准', 'ST20  GLE 换型  焊接质量未调整，需以质量报告为准', '55', '后续需要做细节调整', 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-056', 'ST20  GLE  ROV rear 焊接不稳定，第一个能焊接后续不能完成(捅不进去) 需要优化', 'ST20  GLE  ROV rear 焊接不稳定，第一个能焊接后续不能完成(捅不进去) 需要优化', '56', '后续需要做细节调整', 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-057', 'ST20 NEST harting 头需要有放置位且不能裸露，目前现场每次换型需要重新用绑带固定', 'ST20 NEST harting 头需要有放置位且不能裸露，目前现场每次换型需要重新用绑带固定', '57', '需要增加放置支架', 'Zhang peng', 'On going', '采购下单', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-058', 'ST30 JIG 的harting头需要有存放盒', 'ST30 JIG 的harting头需要有存放盒', '58', '与上面问题重合，删除', NULL, 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'MEDIUM', 'IN_PROGRESS', 'MEDIUM', 0, 0, 0, @pm_id, @pm_name, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', NULL, NULL, NULL, NULL, NULL, '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-059', 'M8 dekra 审核问题需要关闭', 'M8 dekra 审核问题需要关闭', '59', '目前正在向Dekra要问题清单', 'Jason Liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @jason_liu_id, 'Jason Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @jason_liu_id, 'Jason Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-060', 'ST20 FS21 ICV 焊接完成后元件存在报错', 'ST20 FS21 ICV 焊接完成后元件存在报错', '60', '现场需要调整', 'Xuchu Liu', 'On going', 'zhu sai', NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-061', 'ST30 JIG 的harting头需要有不可以损毁的标识，目前是用记号笔', 'ST30 JIG 的harting头需要有不可以损毁的标识，目前是用记号笔', '61', '与上面问题重合，删除', 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-062', 'ST30 JIG 的harting头拆卸需要有简易工具', 'ST30 JIG 的harting头拆卸需要有简易工具', '62', '送起子', 'Zhang peng', 'On going', '装配准备', NULL, NULL, @fs21_project_id, 'FS21', 'MECHANICAL', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-063', '交付物需要提交，特别是换型说明', '交付物需要提交，特别是换型说明', '63', NULL, 'Zhang peng', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @zhang_peng_id, 'Zhang Peng', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @zhang_peng_id, 'Zhang Peng', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0),
  ('FS21-OPL-064', 'ST30 harting头还有未固定的需要固定', 'ST30 harting头还有未固定的需要固定', '64', '已完成，待确认', 'Xuchu Liu', 'On going', NULL, NULL, NULL, @fs21_project_id, 'FS21', 'PROCESS', 'SITE_CHECK', 'HIGH', 'IN_PROGRESS', 'HIGH', 0, 0, 0, @pm_id, @pm_name, @xuchu_liu_id, 'Xuchu Liu', 'FS21_ENG', 'FS21 Engineering', '2026-01-06 09:00:00', '2026-01-30 23:59:59', NULL, NULL, @xuchu_liu_id, 'Xuchu Liu', '2026-01-06 09:00:00', 0, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `description` = VALUES(`description`),
  `opl_no` = VALUES(`opl_no`),
  `action_plan` = VALUES(`action_plan`),
  `pilot_name` = VALUES(`pilot_name`),
  `legacy_status` = VALUES(`legacy_status`),
  `legacy_comment` = VALUES(`legacy_comment`),
  `finish_date` = VALUES(`finish_date`),
  `finish_evidence` = VALUES(`finish_evidence`),
  `project_id` = VALUES(`project_id`),
  `project_name` = VALUES(`project_name`),
  `category_code` = VALUES(`category_code`),
  `source_code` = VALUES(`source_code`),
  `priority` = VALUES(`priority`),
  `status` = VALUES(`status`),
  `impact_level` = VALUES(`impact_level`),
  `reporter_id` = VALUES(`reporter_id`),
  `reporter_name` = VALUES(`reporter_name`),
  `owner_id` = VALUES(`owner_id`),
  `owner_name` = VALUES(`owner_name`),
  `owner_department_code` = VALUES(`owner_department_code`),
  `owner_department_name` = VALUES(`owner_department_name`),
  `occurred_at` = VALUES(`occurred_at`),
  `due_at` = VALUES(`due_at`),
  `closed_at` = VALUES(`closed_at`),
  `close_reason` = VALUES(`close_reason`),
  `current_handler_id` = VALUES(`current_handler_id`),
  `current_handler_name` = VALUES(`current_handler_name`),
  `last_follow_up_at` = VALUES(`last_follow_up_at`),
  `updated_by` = VALUES(`updated_by`),
  `updated_at` = VALUES(`updated_at`),
  `deleted` = 0;

-- Seed initial timeline records for imported issues.
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-001' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 1\\nACTION: 本地供应商加工；', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-001' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-002' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 2\\nACTION: 现场完成全面检查和紧固', 'CLOSED', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-002' LIMIT 1), 'CREATE', NULL, 'CLOSED', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-003' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 3\\nACTION: 调整高度', 'CLOSED', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-003' LIMIT 1), 'CREATE', NULL, 'CLOSED', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-004' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 4\\nACTION: 调整设备位置', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-004' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-005' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 5\\nACTION: 传感器支架及反光板安装并恢复程序检测', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-005' LIMIT 1), 'COMMENT', '找采购下单', 'IN_PROGRESS', @zhang_peng_id, 'Zhang Peng', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-005' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-006' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 6\\nACTION: 更改FS21 nest harting 公头', 'CLOSED', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-006' LIMIT 1), 'CREATE', NULL, 'CLOSED', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-007' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 7\\nACTION: 新做安装板', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-007' LIMIT 1), 'COMMENT', '机械出图，', 'IN_PROGRESS', @zhang_jiying_id, 'Zhang Jiying', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-007' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-008' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 8\\nACTION: 待观察', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-008' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-009' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 9\\nACTION: 加尼龙防护', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-009' LIMIT 1), 'COMMENT', '现场整改', 'IN_PROGRESS', @zhang_peng_id, 'Zhang Peng', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-009' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-010' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 10\\nACTION: 待确认', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-010' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-011' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 11\\nACTION: 待确认', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-011' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-012' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 12\\nACTION: 需要换垫刀块,需要新制15.4垫刀块用于换型--含15.4刀座新作一套（haipan确认）', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-012' LIMIT 1), 'COMMENT', '设计出图纸，采购', 'IN_PROGRESS', @zhang_jiying_id, 'Zhang Jiying', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-012' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-013' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 13\\nACTION: 待观察', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-013' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-014' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 14\\nACTION: 现场调整', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-014' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-015' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 15\\nACTION: 现场借了工厂两个M36*2的缓冲器，需要购买归还baopeng', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-015' LIMIT 1), 'COMMENT', '找采购沟通确认', 'IN_PROGRESS', @zhang_peng_id, 'Zhang Peng', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-015' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-016' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 16', 'CLOSED', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-016' LIMIT 1), 'CREATE', NULL, 'CLOSED', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-017' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 17\\nACTION: 已验证,ST20有干涉，同54条', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-017' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-018' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 18\\nACTION: 现场找维修借了3pcs，需要归还给baopeng', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-018' LIMIT 1), 'COMMENT', '采购下单', 'IN_PROGRESS', @zhang_peng_id, 'Zhang Peng', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-018' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-019' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 19\\nACTION: RCH 按我们的要求制作加工件替换，jiying提供图纸', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-019' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-020' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 20\\nACTION: 光杆延长设计出图纸', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-020' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-021' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 21\\nACTION: 等高螺丝', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-021' LIMIT 1), 'COMMENT', '带到现场', 'IN_PROGRESS', @xuchu_liu_id, 'Xuchu Liu', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-021' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-022' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 22\\nACTION: 接近开关，支架下单', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-022' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-023' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 23\\nACTION: 需要增加此功能', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-023' LIMIT 1), 'COMMENT', '加程序', 'IN_PROGRESS', @haichao_bai_id, 'Haichao Bai', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-023' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-024' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 24\\nACTION: 设计换型标牌', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-024' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-025' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 25\\nACTION: 增加Jig放置架', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-025' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-026' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 26\\nACTION: 当前切了护栏网片', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-026' LIMIT 1), 'COMMENT', '现场整改', 'IN_PROGRESS', @xuchu_liu_id, 'Xuchu Liu', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-026' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-027' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 27\\nACTION: 提升设备UPH', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-027' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-028' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 28\\nACTION: 设计换型标牌', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-028' LIMIT 1), 'COMMENT', '同25', 'IN_PROGRESS', @zhang_jiying_id, 'Zhang Jiying', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-028' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-029' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 29', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-029' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-030' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 30\\nACTION: 1/15确认方案', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-030' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-031' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 31', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-031' LIMIT 1), 'COMMENT', '和jiying确认', 'IN_PROGRESS', @zhang_jiying_id, 'Zhang Jiying', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-031' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-032' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 32\\nACTION: 现场增加', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-032' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-033' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 33\\nACTION: 重新出图纸下单，买细光杆', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-033' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-034' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 34\\nACTION: 建议改成2个人换型(ST30完成后过来支持)', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-034' LIMIT 1), 'COMMENT', '更新换型说明', 'IN_PROGRESS', @zhang_jiying_id, 'Zhang Jiying', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-034' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-035' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 35\\nACTION: 已修改，待确认', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-035' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-036' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 36\\nACTION: 现场增加', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-036' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-037' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 37\\nACTION: 设计出图纸', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-037' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-038' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 38\\nACTION: 设计出图纸', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-038' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-039' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 39\\nACTION: 改变操作流程', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-039' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-040' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 40\\nACTION: 四个小车加把手（GLE&FS21）', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-040' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-041' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 41\\nACTION: 双人可换型，已验证；代工厂确认', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-041' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-042' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 42\\nACTION: 设计出图纸，加保护罩', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-042' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-043' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 43\\nACTION: 滑块上增加Stopper', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-043' LIMIT 1), 'COMMENT', '采购下单', 'IN_PROGRESS', @xuchu_liu_id, 'Xuchu Liu', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-043' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-044' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 44\\nACTION: 确认是哪个按钮', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-044' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-045' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 45', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-045' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-046' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 46\\nACTION: 待CMK结果', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-046' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-047' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 47\\nACTION: 同第一条', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-047' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-048' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 48\\nACTION: 先和工厂沟通', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-048' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-049' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 49', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-049' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-050' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 50\\nACTION: 需要现场捆扎', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-050' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-051' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 51\\nACTION: 待观察', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-051' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-052' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 52\\nACTION: 与上面问题重合，删除', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-052' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-053' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 53\\nACTION: 导杆调整高度,端部用防撞条包裹(推荐聚氨酯材料)', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-053' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-054' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 54\\nACTION: 后续需要做细节调整', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-054' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-055' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 55\\nACTION: 后续需要做细节调整', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-055' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-056' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 56\\nACTION: 后续需要做细节调整', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-056' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-057' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 57\\nACTION: 需要增加放置支架', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-057' LIMIT 1), 'COMMENT', '采购下单', 'IN_PROGRESS', @zhang_peng_id, 'Zhang Peng', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-057' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-058' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 58\\nACTION: 与上面问题重合，删除', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-058' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-059' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 59\\nACTION: 目前正在向Dekra要问题清单', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-059' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-060' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 60\\nACTION: 现场需要调整', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-060' LIMIT 1), 'COMMENT', 'zhu sai', 'IN_PROGRESS', @xuchu_liu_id, 'Xuchu Liu', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-060' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-061' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 61\\nACTION: 与上面问题重合，删除', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-061' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-062' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 62\\nACTION: 送起子', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-062' LIMIT 1), 'COMMENT', '装配准备', 'IN_PROGRESS', @zhang_peng_id, 'Zhang Peng', @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-062' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-063' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 63', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-063' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);

INSERT INTO `issue_comment` (`issue_id`, `comment_type`, `content`, `status_snapshot`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-064' LIMIT 1), 'SYSTEM', 'Imported from legacy FS21 OPL row 64\\nACTION: 已完成，待确认', 'IN_PROGRESS', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
INSERT INTO `issue_operation_log` (`issue_id`, `operation_type`, `from_value`, `to_value`, `remark`, `operator_id`, `operator_name`, `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted`) VALUES
  ((SELECT id FROM `issue` WHERE issue_no = 'FS21-OPL-064' LIMIT 1), 'CREATE', NULL, 'IN_PROGRESS', 'Imported from FS21 OPL', @pm_id, @pm_name, @pm_id, @pm_id, '2026-01-06 09:00:00', '2026-01-06 09:00:00', 0);
