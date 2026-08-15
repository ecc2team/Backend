ALTER TABLE email_verification ADD COLUMN session_id VARCHAR(64);
UPDATE email_verification SET session_id = gen_random_uuid()::text WHERE session_id IS NULL;
ALTER TABLE email_verification ALTER COLUMN session_id SET NOT NULL;
CREATE UNIQUE INDEX uk_email_verification_session_id ON email_verification (session_id);