UPDATE issue
SET status = 'IN_PROGRESS',
    updated_at = NOW(),
    last_follow_up_at = COALESCE(last_follow_up_at, NOW())
WHERE status = 'ACCEPTED'
  AND deleted = 0;
