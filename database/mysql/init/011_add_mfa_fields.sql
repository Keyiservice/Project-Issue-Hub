ALTER TABLE sys_user
    ADD COLUMN mfa_enabled TINYINT NOT NULL DEFAULT 0 COMMENT 'MFA enabled',
    ADD COLUMN mfa_secret VARCHAR(64) DEFAULT NULL COMMENT 'MFA secret (Base32)',
    ADD COLUMN mfa_verified_at DATETIME DEFAULT NULL COMMENT 'MFA verified time';
