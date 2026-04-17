# Project Issue Hub Security Pack

This directory contains the security and operational governance documents
required for internal IT, CS, or go-live review.

Document set:

- `security-compliance-matrix.md`
  - Maps mandatory control domains to current system capability, evidence,
    deployment dependency, and go-live action.
- `service-account-access-sop.md`
  - Defines human account rules, service account boundaries, least-privilege
    requirements, and deployment-side control points.
- `backup-recovery-sop.md`
  - Defines backup scope, schedule, storage rules, recovery objective, and
    drill procedure.
- `data-retention-policy.md`
  - Defines retention period, archive principle, deletion approval path, and
    current enforcement model.

Supporting scripts:

- `../ops/backup-mysql.sh`
- `../ops/backup-minio.sh`
- `../ops/backup-runtime-config.sh`

Recovery runbook:

- `../Project-Issue-Hub-RHEL-Recovery-Runbook.md`
  - Rebuilds a new RHEL server from GitHub plus MySQL/MinIO/config backup.

Recommended review order:

1. Read `security-compliance-matrix.md` for the overall go-live view.
2. Read `service-account-access-sop.md` with IT or infrastructure owners.
3. Read `backup-recovery-sop.md` with operations owners.
4. Read `data-retention-policy.md` with business owner and CS reviewer.

Current status summary:

- Authentication, MFA, password policy, RBAC, and business traceability are
  already implemented in the application.
- Least-privilege service accounts, backup landing zone, and retention
  execution still depend on deployment-time configuration and operating
  procedures.
- These documents are written to close that gap and make the go-live package
  auditable.
