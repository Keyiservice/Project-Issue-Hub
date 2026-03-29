ALTER TABLE issue
    ADD COLUMN closed_by_id BIGINT NULL COMMENT 'closed user id' AFTER closed_at,
    ADD COLUMN closed_by_name VARCHAR(64) NULL COMMENT 'closed user name' AFTER closed_by_id,
    ADD COLUMN close_evidence VARCHAR(500) NULL COMMENT 'close evidence' AFTER close_reason;
