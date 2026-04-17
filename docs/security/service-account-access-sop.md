# Service Account and Access Control SOP

## 1. Purpose

This document defines how `Project Issue Hub` accounts, roles, and technical
runtime identities must be managed in production to satisfy least-privilege and
traceability requirements.

## 2. Scope

Applies to:

- Web users
- Application administrators
- Project managers and engineers
- Linux server administrators
- Docker runtime operators
- MySQL, Redis, and MinIO technical accounts

## 3. Core Principles

1. No shared named account for daily operation.
2. Human user accounts must be attributable to one person.
3. Service accounts must be non-human and limited to runtime purpose only.
4. Privileged human accounts must use MFA.
5. Default secrets must not remain after deployment.
6. Access must be removed or updated when personnel changes occur.

## 4. Human Account Model

### 4.1 Application user accounts

Human application accounts are stored in the common user system and mapped to:

- platform role
- project membership
- project functional role

Minimum control requirements:

- First login must trigger password change.
- Password must satisfy strong password policy.
- Admin, project manager, and other privileged roles must complete MFA setup.
- Role assignment must be approved by the project owner or system admin.

### 4.2 Role categories

Typical roles in the platform:

- `ADMIN`
- `MANAGEMENT`
- `PROJECT_MANAGER`
- `RESP_ENGINEER`
- `SITE_USER`

Principle:

- Grant the smallest global role set possible.
- Use project membership to narrow operational scope.
- Do not assign admin just to make a user visible in a selector.

## 5. Technical Service Accounts

### 5.1 Required technical identities

| Component | Account type | Purpose | Required permission model |
| --- | --- | --- | --- |
| Spring Boot backend -> MySQL | Dedicated DB runtime account | Read and write application tables | Only application database access; no superuser privilege |
| Spring Boot backend -> Redis | Dedicated password or ACL identity | Cache, token/session-related runtime data | Only required Redis access; no extra admin use |
| Spring Boot backend -> MinIO | Dedicated object storage identity | Upload and read issue attachments | Access limited to approved bucket |
| Linux service operator | OS/service account | Start or upgrade containers | Only deployment directory and Docker operation scope |
| Emergency admin account | Break-glass account | Emergency access only | Sealed, logged, rarely used |

### 5.2 Prohibited patterns

The following are not acceptable for production:

- Using MySQL root as the normal runtime account
- Sharing one Linux admin account among multiple people
- Keeping sample passwords from `.env.example`
- Allowing business users to read deployment secrets
- Letting developers keep permanent production admin accounts without approval

## 6. Production Baseline Configuration

### 6.1 MySQL

Recommended model:

- `root`: bootstrap only, not used by the application during normal runtime
- `opl_app`: runtime account used by backend
- optional `opl_maint`: temporary maintenance account for controlled schema work

Example runtime user creation:

```sql
CREATE USER 'opl_app'@'%' IDENTIFIED BY 'ReplaceWithStrongPassword';
GRANT SELECT, INSERT, UPDATE, DELETE ON opl_platform.* TO 'opl_app'@'%';
FLUSH PRIVILEGES;
```

If schema change needs elevated privilege:

- use a temporary maintenance window
- perform change with approved account
- remove elevated access after change

### 6.2 Redis

Minimum control:

- rotate the sample password
- do not expose Redis to broad internal network segments
- restrict access to backend host/container path only

### 6.3 MinIO

Minimum control:

- rotate root password
- create dedicated bucket access credential where the storage policy allows it
- restrict console access to administrators only
- do not expose console port to general office users

## 7. Linux and Deployment Access

### 7.1 Deployment directory

Recommended deployment root:

- `/data/project-issue-hub`

Permission baseline:

- owned by root or designated infra admin group
- writable only by approved operators
- runtime secret files such as `.env` must be `600` or equivalent

### 7.2 GitHub and code update

Allowed operators:

- designated IT/deployment owner
- designated application owner

Requirements:

- production update must be traceable
- keep update date, operator, version, and rollback point

## 8. Joiner / Mover / Leaver Process

### 8.1 New user

Required steps:

1. Create named account
2. Assign minimum required platform role
3. Assign project membership and functional role
4. Force first-login password change
5. Require MFA if user is privileged

### 8.2 Role change

When a user changes job scope:

1. remove old project access not needed anymore
2. adjust platform role
3. record who approved the change

### 8.3 Offboarding

When a user leaves or no longer needs access:

1. disable login immediately
2. remove project membership
3. retain audit trail data
4. do not delete historical operation records

## 9. Credential Rotation

Recommended baseline:

| Credential | Rotation baseline |
| --- | --- |
| Admin user password | every 90 days or on security event |
| DB runtime password | every 180 days or on admin turnover |
| Redis password | every 180 days or on security event |
| MinIO runtime secret | every 180 days or on security event |
| JWT secret | on security event or major environment rebuild |

Rotation rule:

- rotate in maintenance window
- validate application connection after change
- record rotation date and operator

## 10. MFA Enforcement

MFA must be mandatory for:

- system administrators
- project managers with cross-project access
- management users with reporting visibility across projects

MFA may be optional for:

- ordinary site users inside tightly controlled internal networks

If company policy requires stronger coverage, enable MFA for all named users.

## 11. Review Cadence

Monthly:

- review privileged user list
- review disabled users
- review project membership drift

Quarterly:

- review service account inventory
- confirm secrets were rotated per policy
- confirm no shared administrative account is in active use

## 12. Evidence Retention

Keep the following evidence for audit:

- user creation or privilege approval record
- privileged access review result
- credential rotation record
- emergency access usage record
