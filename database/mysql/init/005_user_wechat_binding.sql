USE `opl_platform`;

SET @schema_name = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'wechat_unionid') = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `wechat_unionid` VARCHAR(128) DEFAULT NULL COMMENT ''wechat unionid'' AFTER `wechat_openid`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'wechat_bound_at') = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `wechat_bound_at` DATETIME DEFAULT NULL COMMENT ''wechat bound at'' AFTER `wechat_unionid`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'password_change_required') = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `password_change_required` TINYINT NOT NULL DEFAULT 1 COMMENT ''password change required'' AFTER `wechat_bound_at`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'password_changed_at') = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `password_changed_at` DATETIME DEFAULT NULL COMMENT ''password changed at'' AFTER `password_change_required`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `sys_user`
SET `password_change_required` = 1
WHERE `password_change_required` IS NULL;
