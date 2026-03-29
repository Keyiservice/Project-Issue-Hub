USE `opl_platform`;

CREATE TABLE IF NOT EXISTS `project_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `project_role_code` VARCHAR(64) NOT NULL COMMENT '项目岗位编码',
  `project_role_name` VARCHAR(64) NOT NULL COMMENT '项目岗位名称',
  `is_project_manager` TINYINT NOT NULL DEFAULT 0 COMMENT '是否项目经理',
  `can_assign_issue` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可分派问题',
  `can_verify_issue` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可验证问题',
  `can_close_issue` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可关闭问题',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_member_project_user` (`project_id`, `user_id`),
  KEY `idx_project_member_project_id` (`project_id`),
  KEY `idx_project_member_role_code` (`project_role_code`),
  KEY `idx_project_member_project_manager` (`project_id`, `is_project_manager`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

INSERT INTO `project_member` (
  `project_id`,
  `user_id`,
  `project_role_code`,
  `project_role_name`,
  `is_project_manager`,
  `can_assign_issue`,
  `can_verify_issue`,
  `can_close_issue`,
  `sort_no`
)
SELECT
  p.`id`,
  p.`project_manager_id`,
  'PROJECT_MANAGER',
  '项目经理',
  1,
  1,
  1,
  1,
  0
FROM `project` p
WHERE p.`project_manager_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `project_member` pm
    WHERE pm.`project_id` = p.`id`
      AND pm.`user_id` = p.`project_manager_id`
  );
