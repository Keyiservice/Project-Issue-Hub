SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE DATABASE IF NOT EXISTS `opl_platform`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `opl_platform`;

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_no` VARCHAR(32) NOT NULL COMMENT 'User code',
  `username` VARCHAR(64) NOT NULL COMMENT 'Login account',
  `password_hash` VARCHAR(255) NOT NULL COMMENT 'Password hash',
  `real_name` VARCHAR(64) NOT NULL COMMENT 'Real name',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT 'Mobile',
  `email` VARCHAR(128) DEFAULT NULL COMMENT 'Email',
  `wechat_openid` VARCHAR(128) DEFAULT NULL COMMENT 'Wechat openid',
  `wechat_unionid` VARCHAR(128) DEFAULT NULL COMMENT 'Wechat unionid',
  `wechat_bound_at` DATETIME DEFAULT NULL COMMENT 'Wechat bound time',
  `password_change_required` TINYINT NOT NULL DEFAULT 1 COMMENT 'Require password change',
  `password_changed_at` DATETIME DEFAULT NULL COMMENT 'Password changed time',
  `mfa_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT 'MFA enabled',
  `mfa_secret` VARCHAR(64) DEFAULT NULL COMMENT 'MFA secret (Base32)',
  `mfa_verified_at` DATETIME DEFAULT NULL COMMENT 'MFA verified time',
  `department_code` VARCHAR(64) DEFAULT NULL COMMENT 'Department code',
  `department_name` VARCHAR(128) DEFAULT NULL COMMENT 'Department name',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT 'Status ENABLED/DISABLED/LOCKED',
  `last_login_at` DATETIME DEFAULT NULL COMMENT 'Last login time',
  `created_by` BIGINT DEFAULT NULL COMMENT 'Created by',
  `updated_by` BIGINT DEFAULT NULL COMMENT 'Updated by',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_user_no` (`user_no`),
  UNIQUE KEY `uk_sys_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System user';

