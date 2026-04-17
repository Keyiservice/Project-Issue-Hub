# Data Retention Policy

## 1. Purpose

This document defines the baseline data retention, archive, and deletion rules
for `Project Issue Hub`.

## 2. Principles

1. Keep operational data long enough to support project traceability,
   management review, and audit.
2. Do not delete evidence that is still needed for open issues, closed-project
   review, or compliance inquiry.
3. Keep human-readable approval for non-routine deletion.
4. Prefer archive and access restriction before permanent destruction.

## 3. Data Classification

| Data category | Example content |
| --- | --- |
| Master business data | project, issue, user, role, dictionary |
| Evidence data | issue attachments, close evidence, comment attachments |
| Audit data | issue operation log, assignment history, status history |
| Security/technical logs | login failure logs, access logs, backup logs |
| Backup data | DB dumps, MinIO archive, runtime configuration archive |

## 4. Proposed Retention Baseline

| Data category | Proposed retention | Disposal rule |
| --- | --- | --- |
| Project master data | 5 years after project close | archive first, then approved deletion |
| Issue master data | 5 years after issue close or project archive, whichever is later | archive first, then approved deletion |
| Attachment evidence | same as related issue data | delete only with issue archive approval |
| Issue operation log | 5 years | retain as audit evidence |
| Comment records | same as related issue | delete only with issue archive approval |
| User account master record | duration of employment + 1 year | disable on offboarding, retain audit history |
| Login and security logs | 180 days to 1 year depending company rule | rolling deletion after retention window |
| Backup sets - daily | 14 days | rolling deletion |
| Backup sets - weekly | 8 weeks | rolling deletion |
| Backup sets - monthly | 12 months | rolling deletion |

If the business or CS team requires longer retention, the stricter period wins.

## 5. Archive Rule

Archive should be triggered when:

- project is completed and no active issue remains
- project remains inactive for long period and owner approves archive
- server capacity control requires cold storage movement

Archived data must keep:

- data readability
- project and issue identifier integrity
- attachment linkage
- audit traceability

## 6. Deletion Rule

Permanent deletion is allowed only when all conditions are met:

1. retention period is reached
2. business owner approves
3. IT owner approves
4. no legal, audit, or investigation hold exists
5. backup retention impact is understood

Permanent deletion must be recorded with:

- date
- operator
- approval reference
- dataset scope

## 7. User Offboarding Rule

When an employee leaves:

1. disable access immediately
2. keep historical issue, comment, and operation attribution
3. do not rewrite operation history to another user

## 8. Evidence Preservation Rule

The following must not be removed casually:

- close evidence attachments
- issue status history
- assignment history
- management review evidence

If storage pressure exists, archive first. Do not directly purge without
approval.

## 9. Current Enforcement Model

Current product behavior:

- business entities support logical delete
- audit history is retained in operation logs
- attachment objects persist unless explicitly removed

Current governance dependency:

- retention execution relies on operations process, scheduled backup rotation,
  and approved cleanup actions

This means:

- the system supports retention governance
- the final retention outcome still depends on approved operational execution

## 10. Review Cadence

Review this policy:

- annually
- on major compliance change
- on major architecture change
- after any recovery or security incident that exposes retention gaps

## 11. Policy Owner

Joint owners:

- business/application owner
- IT operations owner
- CS or compliance reviewer
