USE `opl_platform`;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND column_name = 'opl_no'
  ),
  'SELECT 1',
  'ALTER TABLE `issue` ADD COLUMN `opl_no` VARCHAR(32) DEFAULT NULL COMMENT ''Excel OPL number'' AFTER `description`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND column_name = 'action_plan'
  ),
  'SELECT 1',
  'ALTER TABLE `issue` ADD COLUMN `action_plan` TEXT COMMENT ''Excel ACTION'' AFTER `opl_no`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND column_name = 'pilot_name'
  ),
  'SELECT 1',
  'ALTER TABLE `issue` ADD COLUMN `pilot_name` VARCHAR(128) DEFAULT NULL COMMENT ''Excel PILOT'' AFTER `action_plan`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND column_name = 'legacy_status'
  ),
  'SELECT 1',
  'ALTER TABLE `issue` ADD COLUMN `legacy_status` VARCHAR(64) DEFAULT NULL COMMENT ''Excel STATUS'' AFTER `pilot_name`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND column_name = 'legacy_comment'
  ),
  'SELECT 1',
  'ALTER TABLE `issue` ADD COLUMN `legacy_comment` VARCHAR(1000) DEFAULT NULL COMMENT ''Excel COMMENTS'' AFTER `legacy_status`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND column_name = 'finish_date'
  ),
  'SELECT 1',
  'ALTER TABLE `issue` ADD COLUMN `finish_date` DATE DEFAULT NULL COMMENT ''Excel FINISH DATE'' AFTER `legacy_comment`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND column_name = 'finish_evidence'
  ),
  'SELECT 1',
  'ALTER TABLE `issue` ADD COLUMN `finish_evidence` VARCHAR(1000) DEFAULT NULL COMMENT ''Excel Finish evidence'' AFTER `finish_date`'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND index_name = 'idx_issue_opl_no'
  ),
  'SELECT 1',
  'CREATE INDEX `idx_issue_opl_no` ON `issue` (`opl_no`)'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = IF(
  EXISTS(
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'issue'
      AND index_name = 'idx_issue_pilot_name'
  ),
  'SELECT 1',
  'CREATE INDEX `idx_issue_pilot_name` ON `issue` (`pilot_name`)'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
