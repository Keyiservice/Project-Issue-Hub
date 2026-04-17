# Project Issue Hub Security Compliance Matrix

## 1. Scope

This matrix is used for internal go-live review of `Project Issue Hub` when
deployed on an internal RHEL server with Docker Compose, MySQL, Redis, MinIO,
Spring Boot backend, and Web frontend.

Reference implementation evidence:

- `backend/src/main/java/com/company/opl/controller/AuthController.java`
- `backend/src/main/java/com/company/opl/controller/UserController.java`
- `backend/src/main/java/com/company/opl/controller/IssueController.java`
- `backend/src/main/java/com/company/opl/service/impl/AuthServiceImpl.java`
- `backend/src/main/java/com/company/opl/service/impl/IssueFlowServiceImpl.java`
- `backend/src/main/java/com/company/opl/service/impl/IssueServiceImpl.java`
- `backend/src/main/java/com/company/opl/config/MfaProperties.java`
- `backend/src/main/java/com/company/opl/dto/ChangePasswordRequest.java`
- `docs/Project-Issue-Hub-RHEL-GitHub-Deployment-Runbook.md`
- `docs/ops/rhel-ops-checklist.md`

Status legend:

- `Implemented`: already supported by product code or existing runbook.
- `Implemented with deployment dependency`: technically supported, but must be
  enforced by deployment and operations.
- `Partial`: partially covered, with explicit go-live actions still required.

## 2. Control Matrix

| Mandatory measure | Current support | Evidence | Remaining go-live action | Owner | Status |
| --- | --- | --- | --- | --- | --- |
| Authentication and user access management | Username/password login, first-login password change, strong password validation, MFA, RBAC, project-level access separation | `AuthController`, `AuthServiceImpl`, `ChangePasswordRequest`, `MfaProperties`, `UserController` | Disable shared accounts, reset default admin password before go-live, enforce MFA for all privileged users, confirm project membership is assigned by real organization structure | Application owner + IT admin | Implemented |
| Service account management (least privilege) | System can run with dedicated DB, Redis, and MinIO credentials, but production enforcement depends on infrastructure setup | Deployment runbook + this SOP pack | Do not run runtime application with superuser credentials in production; create dedicated DB user, protect `.env`, separate human admin and service account ownership, rotate secrets | IT infra owner | Implemented with deployment dependency |
| Application and infrastructure security | Backend uses Spring Security and JWT; access control is enforced server-side; deployment supports internal Nginx reverse proxy and HTTPS | `IssueController`, `UserController`, deployment runbook, Nginx intranet config | Restrict MySQL/Redis/MinIO to internal network only, close unused ports, deploy company TLS certificate, disable or restrict Swagger in production, keep images and base OS patched | IT infra owner | Implemented with deployment dependency |
| Logging and traceability | Issue lifecycle changes, assignment, comments, attachments, and status changes are logged; entity audit fields are present | `IssueFlowServiceImpl`, `IssueServiceImpl`, `IssueCommentServiceImpl`, `BaseEntity` | Enable Nginx access log retention, backend log collection, login-failure review, and define log retention window; optionally centralize logs if required by CS | Application owner + IT ops | Partial |
| Patch management | Docker-based update path and upgrade script exist | `docs/ops/upgrade.sh`, RHEL deployment runbook | Define monthly patch window, emergency security patch SLA, rollback checkpoint, and responsible approver; keep patch records per release | IT ops + application owner | Partial |
| Backup and recovery | MySQL backup script exists; MinIO/config backup can be added; recovery path is documentable | `docs/ops/backup-mysql.sh`, `docs/ops/backup-minio.sh`, `docs/ops/backup-runtime-config.sh` | Store backups outside runtime path, define retention policy, perform periodic restore drill covering database plus attachment objects | IT ops | Partial |
| Data retention definition | Logical delete and audit history exist, but formal retention periods must be defined by policy | `BaseEntity`, issue operation log model, this security pack | Approve retention periods, archive and deletion approval flow, backup retention cycle, and manual purge governance | Business owner + CS + IT | Partial |

## 3. Detailed Interpretation

### 3.1 Authentication and user access management

Covered by product capability:

- Password-based login is implemented.
- First login forces password reset.
- Password complexity is enforced in request validation.
- MFA is supported and can be required globally.
- RBAC is enforced in backend controllers via `@PreAuthorize`.
- Web menu visibility can follow role scope.

Go-live decision:

- This domain is acceptable for go-live once default credentials are rotated and
  privileged accounts complete MFA setup.

### 3.2 Service account management

Key principle:

- Human users and technical runtime identities must not share the same account
  or secret.

Required production posture:

- MySQL runtime account must be separate from MySQL root.
- Redis password must not be the default sample password.
- MinIO root credential must be rotated; use a dedicated application access
  identity where available.
- Runtime `.env` must be readable only by administrators and the service user.

Go-live decision:

- Acceptable only if the deployment checklist is signed off by IT.

### 3.3 Application and infrastructure security

Covered by current architecture:

- Frontend and miniapp do not own business authorization logic.
- Backend centralizes status transition, permission checks, and audit trail.
- Docker Compose keeps services separated.

Still required before production:

- Restrict direct access to MySQL, Redis, and MinIO management ports.
- Expose only approved entry points.
- Terminate HTTPS with internal certificate.
- Restrict Swagger to admins or disable it outside maintenance windows.

### 3.4 Logging and traceability

Already traceable in the application:

- Who created an issue
- Who changed assignment or status
- What attachment or comment was added
- When issue state moved to verify or close

Still recommended:

- Retain web access logs for at least 180 days.
- Review failed login logs.
- Keep backup job logs.
- Keep change records of deployment and patch operations.

### 3.5 Patch management

Current capability:

- Code can be updated from GitHub.
- Containers can be rebuilt and restarted through Docker Compose.
- An upgrade script exists.

Missing governance:

- Patch cadence
- Approval flow
- Rollback criteria
- Release evidence retention

### 3.6 Backup and recovery

Minimum acceptable backup scope:

- MySQL database
- MinIO object data
- Runtime configuration (`.env`, reverse-proxy config, service unit)

Minimum acceptable recovery proof:

- Quarterly restore drill
- Restore drill output retained as evidence

### 3.7 Data retention definition

The product supports data persistence and logical deletion, but retention must
be governed by approved policy. The companion document
`data-retention-policy.md` defines the proposed baseline.

## 4. Go-Live Exit Criteria

The system should be considered ready for internal go-live only after all items
below are completed:

1. Default runtime secrets are replaced.
2. Privileged users complete MFA enrollment.
3. Dedicated service account rules are approved by IT.
4. MySQL, MinIO, and runtime configuration backups are scheduled.
5. At least one restore drill is executed and recorded.
6. Data retention periods are approved by business owner and CS.
7. Production port exposure is reviewed and documented.

## 5. Recommended Evidence Package for CS Review

Submit the following together:

1. This matrix
2. `service-account-access-sop.md`
3. `backup-recovery-sop.md`
4. `data-retention-policy.md`
5. RHEL deployment runbook
6. Screenshot or exported record showing MFA enabled for admin accounts
7. Backup job execution log and first restore drill result
