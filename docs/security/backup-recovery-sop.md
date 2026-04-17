# Backup and Recovery SOP

## 1. Purpose

This document defines the backup scope, execution standard, storage rule,
recovery objective, and restore drill process for `Project Issue Hub`.

## 2. Recovery Target

The platform is considered recoverable only when all items below can be
restored together:

- application database
- issue attachment objects
- runtime configuration and deployment files

Restoring database only is not sufficient because attachment evidence is part of
the issue record.

## 3. Backup Scope

| Data set | Source | Criticality | Backup method |
| --- | --- | --- | --- |
| MySQL application data | MySQL container / database | Critical | logical dump |
| MinIO attachment objects | MinIO data | Critical | object/raw storage archive |
| Runtime configuration | `.env`, compose file, reverse-proxy config, service unit | High | file archive |
| Application logs | container logs or mounted logs | Medium | optional, per policy |

## 4. Backup Frequency

Recommended baseline:

| Data set | Frequency | Retention baseline |
| --- | --- | --- |
| MySQL | daily | 14 days |
| MinIO objects | daily | 14 days |
| Runtime configuration | on change and weekly | 12 latest copies |
| Monthly archive set | monthly | 12 months |

If attachment growth is high, add:

- weekly full archive
- daily incremental sync to backup storage

## 5. Backup Storage Rule

Mandatory requirements:

1. Backup files must not be stored only inside the live container.
2. Backup files should not stay only on the same filesystem as runtime data.
3. At least one secondary location must exist, such as:
   - dedicated backup disk
   - internal backup share
   - approved enterprise backup platform
4. Access to backup location must be limited to administrators.

## 6. Recovery Objective

Proposed baseline:

- `RPO`: up to 24 hours
- `RTO`: 4 to 8 hours for single-server recovery

If business impact is higher, tighten these targets with IT.

## 7. Execution Scripts

Available supporting scripts:

- `docs/ops/backup-mysql.sh`
- `docs/ops/backup-minio.sh`
- `docs/ops/backup-runtime-config.sh`

These scripts are reference scripts and should be copied to the target RHEL
server, reviewed, and scheduled by cron or enterprise scheduler.

Companion recovery runbook:

- `docs/Project-Issue-Hub-RHEL-Recovery-Runbook.md`

## 8. Backup Procedure

### 8.1 MySQL backup

Execution example:

```bash
cd /data/project-issue-hub
bash docs/ops/backup-mysql.sh
```

Expected result:

- a timestamped SQL dump is generated
- script output is captured in cron or scheduler logs

### 8.2 MinIO backup

Execution example:

```bash
cd /data/project-issue-hub
bash docs/ops/backup-minio.sh
```

Expected result:

- a timestamped archive of MinIO object data is generated
- attachment evidence can be restored together with DB records

### 8.3 Runtime configuration backup

Execution example:

```bash
cd /data/project-issue-hub
bash docs/ops/backup-runtime-config.sh
```

Expected result:

- `.env`
- `docker-compose.yml`
- reverse-proxy config
- service/autostart config

are archived together.

## 9. Recovery Procedure

### 9.1 Recovery preparation

Before starting recovery:

1. identify the incident scope
2. identify the latest valid backup set
3. stop write traffic to the system
4. keep a copy of the failed state if forensics are required

### 9.2 MySQL restore

Typical recovery order:

1. rebuild or start MySQL service
2. create empty target database if needed
3. import SQL dump
4. verify tables and row counts

### 9.3 MinIO restore

Typical recovery order:

1. rebuild or start MinIO service
2. restore object archive to MinIO data path or bucket
3. verify bucket contents are readable
4. validate attachment URLs from sample issue records

### 9.4 Configuration restore

Restore these files before application start:

- `.env`
- `docker-compose.yml`
- Nginx config
- systemd service unit if used

### 9.5 Application validation after restore

Required validation:

1. login works
2. issue list loads
3. issue detail opens
4. image attachment preview works
5. video attachment playback works
6. create or comment operation can be executed in test scope

## 10. Restore Drill

Minimum requirement:

- execute a restore drill every quarter

Drill must include:

- MySQL restore
- MinIO restore
- application start
- sample business verification

Drill evidence should record:

- date
- operator
- backup version used
- duration
- result
- issues found

## 11. Failure Handling

If a backup job fails:

1. raise incident to IT owner
2. do not wait for next cycle without investigation
3. capture failure log
4. rerun after remediation

If restore drill fails:

1. do not declare backup policy complete
2. open corrective action
3. repeat drill after fix

## 12. Minimum Go-Live Evidence

Before go-live, keep the following evidence:

1. at least one successful MySQL backup log
2. at least one successful MinIO backup log
3. at least one successful runtime config backup log
4. one restore drill result signed by operations owner
